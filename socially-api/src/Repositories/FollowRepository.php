<?php

namespace Socially\Repositories;

use PDO;
use Socially\Database\Connection;

class FollowRepository
{
    public function __construct(private Connection $connection)
    {
    }

    private function pdo(): PDO
    {
        return $this->connection->pdo();
    }

    public function add(int $followerId, int $followingId): void
    {
        $stmt = $this->pdo()->prepare('INSERT IGNORE INTO follows (follower_id, following_id) VALUES (:follower_id, :following_id)');
        $stmt->execute([
            'follower_id' => $followerId,
            'following_id' => $followingId,
        ]);
    }

    public function remove(int $followerId, int $followingId): void
    {
        $stmt = $this->pdo()->prepare('DELETE FROM follows WHERE follower_id = :follower_id AND following_id = :following_id');
        $stmt->execute([
            'follower_id' => $followerId,
            'following_id' => $followingId,
        ]);
    }

    public function isFollowing(int $followerId, int $followingId): bool
    {
        $stmt = $this->pdo()->prepare('SELECT 1 FROM follows WHERE follower_id = :follower_id AND following_id = :following_id');
        $stmt->execute([
            'follower_id' => $followerId,
            'following_id' => $followingId,
        ]);

        return (bool) $stmt->fetchColumn();
    }

    public function followers(int $userId): array
    {
        $stmt = $this->pdo()->prepare(
            'SELECT u.id, u.username, u.profile_image, f.created_at
             FROM follows f JOIN users u ON u.id = f.follower_id
             WHERE f.following_id = :user_id'
        );
        $stmt->execute(['user_id' => $userId]);

        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    public function following(int $userId): array
    {
        $stmt = $this->pdo()->prepare(
            'SELECT u.id, u.username, u.profile_image, f.created_at
             FROM follows f JOIN users u ON u.id = f.following_id
             WHERE f.follower_id = :user_id'
        );
        $stmt->execute(['user_id' => $userId]);

        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }
}
