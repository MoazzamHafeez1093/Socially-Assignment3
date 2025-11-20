<?php

namespace Socially\Controllers;

use Socially\Helpers\ApiResponse;
use Socially\Repositories\FcmTokenRepository;
use Psr\Http\Message\ResponseInterface;
use Psr\Http\Message\ServerRequestInterface;

class FcmController
{
    private FcmTokenRepository $fcmTokenRepo;

    public function __construct(FcmTokenRepository $fcmTokenRepo)
    {
        $this->fcmTokenRepo = $fcmTokenRepo;
    }

    /**
     * Register FCM token for current user
     * POST /api/fcm/token
     * Body: { "token": "fcm_device_token_here" }
     */
    public function registerToken(ServerRequestInterface $request, ResponseInterface $response): ResponseInterface
    {
        $userId = $request->getAttribute('userId');
        $body = $request->getParsedBody();
        $token = $body['token'] ?? null;

        if (!$token) {
            return ApiResponse::error($response, 'FCM token is required', 400);
        }

        $this->fcmTokenRepo->upsert($userId, $token);

        return ApiResponse::success($response, [
            'message' => 'FCM token registered successfully'
        ]);
    }

    /**
     * Delete FCM token for current user
     * DELETE /api/fcm/token
     */
    public function deleteToken(ServerRequestInterface $request, ResponseInterface $response): ResponseInterface
    {
        $userId = $request->getAttribute('userId');
        
        $this->fcmTokenRepo->delete($userId);

        return ApiResponse::success($response, [
            'message' => 'FCM token deleted successfully'
        ]);
    }
}
