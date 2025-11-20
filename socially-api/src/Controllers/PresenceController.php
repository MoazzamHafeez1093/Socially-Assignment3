<?php

namespace App\Controllers;

use App\Helpers\Response;
use App\Repositories\PresenceRepository;
use Psr\Http\Message\ResponseInterface;
use Psr\Http\Message\ServerRequestInterface;

class PresenceController
{
    private PresenceRepository $presenceRepo;

    public function __construct(PresenceRepository $presenceRepo)
    {
        $this->presenceRepo = $presenceRepo;
    }

    /**
     * Ping to update user's online status
     * POST /api/presence/ping
     */
    public function ping(ServerRequestInterface $request, ResponseInterface $response): ResponseInterface
    {
        $userId = $request->getAttribute('userId');
        
        $this->presenceRepo->updateLastSeen($userId);
        
        return Response::success($response, [
            'message' => 'Presence updated',
            'timestamp' => date('Y-m-d H:i:s')
        ]);
    }

    /**
     * Set user as offline
     * POST /api/presence/offline
     */
    public function setOffline(ServerRequestInterface $request, ResponseInterface $response): ResponseInterface
    {
        $userId = $request->getAttribute('userId');
        
        $this->presenceRepo->setOffline($userId);
        
        return Response::success($response, [
            'message' => 'Marked as offline'
        ]);
    }

    /**
     * Get user's presence status
     * GET /api/presence/{userId}
     */
    public function show(ServerRequestInterface $request, ResponseInterface $response, array $args): ResponseInterface
    {
        $targetUserId = (int) $args['userId'];
        
        $presence = $this->presenceRepo->getPresence($targetUserId);
        
        if (!$presence) {
            return Response::success($response, [
                'user_id' => $targetUserId,
                'is_online' => false,
                'last_seen' => null
            ]);
        }
        
        return Response::success($response, $presence);
    }

    /**
     * Get bulk presence for multiple users
     * POST /api/presence/bulk
     * Body: { "user_ids": [1, 2, 3] }
     */
    public function bulk(ServerRequestInterface $request, ResponseInterface $response): ResponseInterface
    {
        $body = $request->getParsedBody();
        $userIds = $body['user_ids'] ?? [];

        if (!is_array($userIds) || empty($userIds)) {
            return Response::error($response, 'user_ids array is required', 400);
        }

        // Sanitize to integers
        $userIds = array_map('intval', $userIds);
        
        $presences = $this->presenceRepo->getBulkPresence($userIds);
        
        return Response::success($response, [
            'presences' => $presences
        ]);
    }
}
