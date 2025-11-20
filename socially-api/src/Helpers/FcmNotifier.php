<?php

namespace Socially\Helpers;

class FcmNotifier
{
    private string $serverKey;

    public function __construct(string $serverKey)
    {
        $this->serverKey = $serverKey;
    }

    /**
     * Send FCM notification to device token
     * 
     * @param string $token Device FCM token
     * @param array $data Data payload
     * @param array|null $notification Optional notification payload
     * @return bool Success status
     */
    public function send(string $token, array $data, ?array $notification = null): bool
    {
        if (empty($this->serverKey)) {
            error_log('FCM: Server key not configured');
            return false;
        }

        $payload = [
            'to' => $token,
            'data' => $data,
            'priority' => 'high'
        ];

        if ($notification) {
            $payload['notification'] = $notification;
        }

        $headers = [
            'Authorization: key=' . $this->serverKey,
            'Content-Type: application/json'
        ];

        $ch = curl_init('https://fcm.googleapis.com/fcm/send');
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($payload));

        $result = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);

        if ($httpCode !== 200) {
            error_log("FCM: Failed with HTTP $httpCode - $result");
            return false;
        }

        return true;
    }

    /**
     * Send notification to multiple tokens
     */
    public function sendMultiple(array $tokens, array $data, ?array $notification = null): array
    {
        $results = [];
        foreach ($tokens as $token) {
            $results[$token] = $this->send($token, $data, $notification);
        }
        return $results;
    }

    /**
     * Helper to send new message notification
     */
    public function notifyNewMessage(string $recipientToken, string $senderName, string $messageText, int $messageId): bool
    {
        return $this->send(
            $recipientToken,
            [
                'type' => 'new_message',
                'message_id' => $messageId,
                'sender_name' => $senderName,
                'message_text' => $messageText
            ],
            [
                'title' => $senderName,
                'body' => $messageText,
                'sound' => 'default'
            ]
        );
    }

    /**
     * Helper to send follow request notification
     */
    public function notifyFollowRequest(string $recipientToken, string $requesterName, int $requestId): bool
    {
        return $this->send(
            $recipientToken,
            [
                'type' => 'follow_request',
                'request_id' => $requestId,
                'requester_name' => $requesterName
            ],
            [
                'title' => 'New Follow Request',
                'body' => "$requesterName wants to follow you",
                'sound' => 'default'
            ]
        );
    }

    /**
     * Helper to send screenshot alert notification
     */
    public function notifyScreenshot(string $recipientToken, string $screenshotterName): bool
    {
        return $this->send(
            $recipientToken,
            [
                'type' => 'screenshot_alert',
                'user_name' => $screenshotterName
            ],
            [
                'title' => 'Screenshot Alert',
                'body' => "$screenshotterName took a screenshot of your chat",
                'sound' => 'default'
            ]
        );
    }

    /**
     * Helper to send post like notification
     */
    public function notifyPostLike(string $recipientToken, string $likerName, int $postId): bool
    {
        return $this->send(
            $recipientToken,
            [
                'type' => 'post_like',
                'post_id' => $postId,
                'liker_name' => $likerName
            ],
            [
                'title' => "$likerName liked your post",
                'body' => 'Tap to view',
                'sound' => 'default'
            ]
        );
    }

    /**
     * Helper to send post comment notification
     */
    public function notifyPostComment(string $recipientToken, string $commenterName, string $commentText, int $postId): bool
    {
        return $this->send(
            $recipientToken,
            [
                'type' => 'post_comment',
                'post_id' => $postId,
                'commenter_name' => $commenterName,
                'comment_text' => $commentText
            ],
            [
                'title' => "$commenterName commented on your post",
                'body' => $commentText,
                'sound' => 'default'
            ]
        );
    }
}
