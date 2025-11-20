<?php

namespace Socially\Repositories;

use PDO;
use Socially\Database\Connection;

class FollowRequestRepository
{
    public function __construct(private Connection $connection)
    {
    }

    private function pdo(): PDO
    {
        return $this->connection->pdo();
    }

    public function create(int $requesterId, int $targetId): array
    {
        $stmt = $this->pdo()->prepare('INSERT INTO follow_requests (requester_id, target_id, status) VALUES (:requester_id, :target_id, "pending")
            ON DUPLICATE KEY UPDATE status = "pending", created_at = CURRENT_TIMESTAMP, responded_at = NULL');
        $stmt->execute([
            'requester_id' => $requesterId,
            'target_id' => $targetId,
        ]);

        return $this->findByUsers($requesterId, $targetId);
    }

    public function findById(int $id): ?array
    {
        $stmt = $this->pdo()->prepare('SELECT * FROM follow_requests WHERE id = :id');
        $stmt->execute(['id' => $id]);
        $request = $stmt->fetch(PDO::FETCH_ASSOC);

        return $request ?: null;
    }

    public function findByUsers(int $requesterId, int $targetId): ?array
    {
        $stmt = $this->pdo()->prepare('SELECT * FROM follow_requests WHERE requester_id = :requester_id AND target_id = :target_id');
        $stmt->execute([
            'requester_id' => $requesterId,
            'target_id' => $targetId,
        ]);
        $request = $stmt->fetch(PDO::FETCH_ASSOC);

        return $request ?: null;
    }

    public function updateStatus(int $requestId, string $status): ?array
    {
        $stmt = $this->pdo()->prepare('UPDATE follow_requests SET status = :status, responded_at = CURRENT_TIMESTAMP WHERE id = :id');
        $stmt->execute([
            'status' => $status,
            'id' => $requestId,
        ]);

        if ($stmt->rowCount() === 0) {
            return null;
        }

        return $this->findById($requestId);
    }

    public function incoming(int $userId): array
    {
        $stmt = $this->pdo()->prepare(
            'SELECT fr.*, u.username AS requester_username, u.profile_image
             FROM follow_requests fr
             JOIN users u ON u.id = fr.requester_id
             WHERE fr.target_id = :user_id AND fr.status = "pending"'
        );
        $stmt->execute(['user_id' => $userId]);

        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    public function outgoing(int $userId): array
    {
        $stmt = $this->pdo()->prepare(
            'SELECT fr.*, u.username AS target_username, u.profile_image
             FROM follow_requests fr
             JOIN users u ON u.id = fr.target_id
             WHERE fr.requester_id = :user_id AND fr.status = "pending"'
        );
        $stmt->execute(['user_id' => $userId]);

        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }
}
