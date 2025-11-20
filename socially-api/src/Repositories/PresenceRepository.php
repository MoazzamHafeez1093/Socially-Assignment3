<?php

namespace App\Repositories;

use App\Database\Connection;
use PDO;

class PresenceRepository
{
    private PDO $pdo;

    public function __construct(Connection $db)
    {
        $this->pdo = $db->getPdo();
    }

    /**
     * Update user's last seen timestamp (upsert)
     */
    public function updateLastSeen(int $userId): void
    {
        $stmt = $this->pdo->prepare("
            INSERT INTO user_presence (user_id, last_seen, is_online)
            VALUES (:user_id, NOW(), 1)
            ON DUPLICATE KEY UPDATE last_seen = NOW(), is_online = 1
        ");
        $stmt->execute(['user_id' => $userId]);
    }

    /**
     * Mark user as offline
     */
    public function setOffline(int $userId): void
    {
        $stmt = $this->pdo->prepare("
            UPDATE user_presence 
            SET is_online = 0 
            WHERE user_id = :user_id
        ");
        $stmt->execute(['user_id' => $userId]);
    }

    /**
     * Get user's presence status
     */
    public function getPresence(int $userId): ?array
    {
        $stmt = $this->pdo->prepare("
            SELECT user_id, last_seen, is_online, created_at
            FROM user_presence
            WHERE user_id = :user_id
        ");
        $stmt->execute(['user_id' => $userId]);
        $result = $stmt->fetch(PDO::FETCH_ASSOC);
        return $result ?: null;
    }

    /**
     * Get presence for multiple users
     */
    public function getBulkPresence(array $userIds): array
    {
        if (empty($userIds)) {
            return [];
        }

        $placeholders = implode(',', array_fill(0, count($userIds), '?'));
        $stmt = $this->pdo->prepare("
            SELECT user_id, last_seen, is_online
            FROM user_presence
            WHERE user_id IN ($placeholders)
        ");
        $stmt->execute($userIds);
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    /**
     * Clean up stale online statuses (mark as offline if last_seen > 2 minutes ago)
     */
    public function cleanupStalePresence(): int
    {
        $stmt = $this->pdo->prepare("
            UPDATE user_presence 
            SET is_online = 0 
            WHERE is_online = 1 
            AND last_seen < DATE_SUB(NOW(), INTERVAL 2 MINUTE)
        ");
        $stmt->execute();
        return $stmt->rowCount();
    }
}
