<?php

namespace Socially\Controllers;

use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;
use Respect\Validation\Exceptions\NestedValidationException;
use Respect\Validation\Validator as v;
use Socially\Helpers\ApiResponse;
use Socially\Helpers\FcmNotifier;
use Socially\Repositories\FollowRepository;
use Socially\Repositories\FollowRequestRepository;
use Socially\Repositories\UserRepository;
use Socially\Repositories\FcmTokenRepository;

class FollowController
{
    public function __construct(
        private FollowRepository $follows,
        private FollowRequestRepository $requests,
        private UserRepository $users,
        private FcmNotifier $fcm,
        private FcmTokenRepository $fcmTokens
    ) {
    }

    public function request(Request $request, Response $response): Response
    {
        $userId = (int) $request->getAttribute('userId');
        $data = (array) $request->getParsedBody();

        $validator = v::key('target_id', v::intType()->positive());
        try {
            $validator->assert($data);
        } catch (NestedValidationException $e) {
            return ApiResponse::error($response, $e->getFullMessage(), 422);
        }

        $targetId = (int) $data['target_id'];
        if ($targetId === $userId) {
            return ApiResponse::error($response, 'Cannot follow yourself', 422);
        }

        if (!$this->users->findById($targetId)) {
            return ApiResponse::error($response, 'Target user not found', 404);
        }

        if ($this->follows->isFollowing($userId, $targetId)) {
            return ApiResponse::success($response, ['message' => 'Already following']);
        }

        $requestRecord = $this->requests->create($userId, $targetId);
        
        // Send FCM notification to target user
        $requesterUser = $this->users->findById($userId);
        $targetToken = $this->fcmTokens->getToken($targetId);
        if ($targetToken && $requesterUser) {
            $this->fcm->notifyFollowRequest(
                $targetToken,
                $requesterUser['username'],
                $requestRecord['id']
            );
        }
        
        return ApiResponse::success($response, ['request' => $requestRecord], 201);
    }

    public function accept(Request $request, Response $response, array $args): Response
    {
        $userId = (int) $request->getAttribute('userId');
        $requestId = (int) $args['id'];
        $followRequest = $this->requests->findById($requestId);

        if (!$followRequest || (int) $followRequest['target_id'] !== $userId) {
            return ApiResponse::error($response, 'Follow request not found', 404);
        }

        $updated = $this->requests->updateStatus($requestId, 'accepted');
        $this->follows->add((int) $followRequest['requester_id'], (int) $followRequest['target_id']);

        return ApiResponse::success($response, ['request' => $updated]);
    }

    public function reject(Request $request, Response $response, array $args): Response
    {
        $userId = (int) $request->getAttribute('userId');
        $requestId = (int) $args['id'];
        $followRequest = $this->requests->findById($requestId);

        if (!$followRequest || (int) $followRequest['target_id'] !== $userId) {
            return ApiResponse::error($response, 'Follow request not found', 404);
        }

        $updated = $this->requests->updateStatus($requestId, 'rejected');
        return ApiResponse::success($response, ['request' => $updated]);
    }

    public function followers(Request $request, Response $response, array $args): Response
    {
        $userId = (int) $args['id'];
        $followers = $this->follows->followers($userId);

        return ApiResponse::success($response, ['followers' => $followers]);
    }

    public function following(Request $request, Response $response, array $args): Response
    {
        $userId = (int) $args['id'];
        $following = $this->follows->following($userId);

        return ApiResponse::success($response, ['following' => $following]);
    }

    public function pending(Request $request, Response $response): Response
    {
        $userId = (int) $request->getAttribute('userId');
        return ApiResponse::success($response, [
            'incoming' => $this->requests->incoming($userId),
            'outgoing' => $this->requests->outgoing($userId),
        ]);
    }

    public function unfollow(Request $request, Response $response, array $args): Response
    {
        $userId = (int) $request->getAttribute('userId');
        $targetId = (int) $args['id'];
        $this->follows->remove($userId, $targetId);

        return ApiResponse::success($response, ['message' => 'Unfollowed']);
    }
}
