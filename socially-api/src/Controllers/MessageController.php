<?php

namespace Socially\Controllers;

use DateInterval;
use DateTimeImmutable;
use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;
use Respect\Validation\Exceptions\NestedValidationException;
use Respect\Validation\Validator as v;
use Socially\Helpers\MediaUploader;
use Socially\Helpers\Response as ApiResponse;
use Socially\Helpers\FcmNotifier;
use Socially\Repositories\MessageRepository;
use Socially\Repositories\UserRepository;
use Socially\Repositories\FcmTokenRepository;

class MessageController
{
    public function __construct(
        private MessageRepository $messages, 
        private MediaUploader $uploader,
        private FcmNotifier $fcm,
        private UserRepository $users,
        private FcmTokenRepository $fcmTokens
    ) {
    }

    public function conversation(Request $request, Response $response, array $args): Response
    {
        $userId = (int) $request->getAttribute('userId');
        $peerId = (int) $args['userId'];
        $limit = (int) ($request->getQueryParams()['limit'] ?? 100);
        $messages = $this->messages->conversation($userId, $peerId, $limit);

        return ApiResponse::success($response, ['messages' => $messages]);
    }

    public function send(Request $request, Response $response): Response
    {
        $senderId = (int) $request->getAttribute('userId');
        $data = (array) $request->getParsedBody();

        $validator = v::key('receiver_id', v::intType()->positive())
            ->key('message', v::optional(v::stringType()->length(0, 2000)))
            ->key('vanish_mode', v::optional(v::boolType()));

        try {
            $validator->assert($data);
        } catch (NestedValidationException $e) {
            return ApiResponse::error($response, $e->getFullMessage(), 422);
        }

        $files = $request->getUploadedFiles();
        $mediaUrl = null;
        $mediaType = null;
        if (isset($files['media'])) {
            $mediaUrl = $this->uploader->store($files['media'], 'messages');
            $mediaType = $files['media']->getClientMediaType();
        }

        if (!$mediaUrl && empty($data['message'])) {
            return ApiResponse::error($response, 'Message text or media is required', 422);
        }

        $receiverId = (int) $data['receiver_id'];
        if ($receiverId === $senderId) {
            return ApiResponse::error($response, 'Cannot message yourself', 422);
        }

        $message = $this->messages->create(
            $senderId,
            $receiverId,
            $data['message'] ?? null,
            $mediaUrl,
            $mediaType,
            (bool) ($data['vanish_mode'] ?? false)
        );

        // Send FCM notification to receiver
        $senderUser = $this->users->findById($senderId);
        $receiverToken = $this->fcmTokens->getToken($receiverId);
        if ($receiverToken && $senderUser) {
            $messageText = $data['message'] ?? 'Sent a media file';
            $this->fcm->notifyNewMessage(
                $receiverToken,
                $senderUser['username'],
                $messageText,
                $message['id']
            );
        }

        return ApiResponse::success($response, ['message' => $message], 201);
    }

    public function update(Request $request, Response $response, array $args): Response
    {
        $userId = (int) $request->getAttribute('userId');
        $messageId = (int) $args['id'];
        $data = (array) $request->getParsedBody();

        $validator = v::key('message', v::stringType()->length(1, 2000));
        try {
            $validator->assert($data);
        } catch (NestedValidationException $e) {
            return ApiResponse::error($response, $e->getFullMessage(), 422);
        }

        $message = $this->messages->findById($messageId);
        if (!$message || (int) $message['sender_id'] !== $userId) {
            return ApiResponse::error($response, 'Message not found', 404);
        }

        if (!$this->canModify($message)) {
            return ApiResponse::error($response, 'Edit window (5 minutes) has passed', 403);
        }

        $updated = $this->messages->updateText($messageId, $userId, $data['message']);
        return ApiResponse::success($response, ['message' => $updated]);
    }

    public function destroy(Request $request, Response $response, array $args): Response
    {
        $userId = (int) $request->getAttribute('userId');
        $messageId = (int) $args['id'];
        $message = $this->messages->findById($messageId);

        if (!$message || (int) $message['sender_id'] !== $userId) {
            return ApiResponse::error($response, 'Message not found', 404);
        }

        if (!$this->canModify($message)) {
            return ApiResponse::error($response, 'Delete window (5 minutes) has passed', 403);
        }

        $this->messages->softDelete($messageId, $userId);
        return ApiResponse::success($response, ['message' => 'Message deleted']);
    }

    public function markRead(Request $request, Response $response, array $args): Response
    {
        $userId = (int) $request->getAttribute('userId');
        $messageId = (int) $args['id'];
        $data = (array) $request->getParsedBody();
        $message = $this->messages->markRead($messageId, $userId);

        if (!$message) {
            return ApiResponse::error($response, 'Message not found', 404);
        }

        $chatClosed = filter_var($data['chat_closed'] ?? false, FILTER_VALIDATE_BOOLEAN);
        if ($chatClosed && (int) $message['vanish_mode'] === 1) {
            $this->messages->deleteIfVanishAndRead($messageId);
        }

        return ApiResponse::success($response, ['message' => $message]);
    }

    private function canModify(array $message): bool
    {
        $createdAt = new DateTimeImmutable($message['created_at']);
        $deadline = $createdAt->add(new DateInterval('PT5M'));
        return $deadline > new DateTimeImmutable() && !$message['deleted_at'];
    }
}
