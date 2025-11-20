<?php

namespace Socially\Repositories;

use App\Database\Connection;
use PDO;

class FcmTokenRepository
{
    private PDO $pdo;

    public function __construct(Connection $db)
    {
        $this->pdo = $db->getPdo();
    }

    /**
     * Store or update FCM token for user
     */
    public function upsert(int $userId, string $token): void
    {
        $stmt = $this->pdo->prepare("
            INSERT INTO user_fcm_tokens (user_id, token, updated_at)
            VALUES (:user_id, :token, NOW())
            ON DUPLICATE KEY UPDATE token = :token, updated_at = NOW()
        ");
        $stmt->execute(['user_id' => $userId, 'token' => $token]);
    }

    /**
     * Get FCM token for user
     */
    public function getToken(int $userId): ?string
    {
        $stmt = $this->pdo->prepare("
            SELECT token 
            FROM user_fcm_tokens 
            WHERE user_id = :user_id
        ");
        $stmt->execute(['user_id' => $userId]);
        $result = $stmt->fetch(PDO::FETCH_ASSOC);
        return $result ? $result['token'] : null;
    }

    /**
     * Delete token for user
     */
    public function delete(int $userId): void
    {
        $stmt = $this->pdo->prepare("DELETE FROM user_fcm_tokens WHERE user_id = :user_id");
        $stmt->execute(['user_id' => $userId]);
    }
}
