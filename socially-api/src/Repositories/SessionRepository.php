<?php

namespace Socially\Repositories;

use PDO;
use Socially\Database\Connection;

class SessionRepository
{
    public function __construct(private Connection $connection)
    {
    }

    private function pdo(): PDO
    {
        return $this->connection->pdo();
    }

    public function create(int $userId, string $token, ?string $deviceInfo = null): void
    {
        $stmt = $this->pdo()->prepare(
            'INSERT INTO sessions (user_id, token, device_info) VALUES (:user_id, :token, :device_info)'
        );
        $stmt->execute([
            'user_id' => $userId,
            'token' => $token,
            'device_info' => $deviceInfo,
        ]);
    }

    public function deleteByToken(string $token): void
    {
        $stmt = $this->pdo()->prepare('DELETE FROM sessions WHERE token = :token');
        $stmt->execute(['token' => $token]);
    }

    public function deleteAllForUser(int $userId): void
    {
        $stmt = $this->pdo()->prepare('DELETE FROM sessions WHERE user_id = :user_id');
        $stmt->execute(['user_id' => $userId]);
    }

    public function exists(string $token): bool
    {
        $stmt = $this->pdo()->prepare('SELECT 1 FROM sessions WHERE token = :token LIMIT 1');
        $stmt->execute(['token' => $token]);

        return (bool) $stmt->fetchColumn();
    }
}
