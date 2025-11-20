<?php

namespace Socially\Repositories;

use PDO;
use Socially\Database\Connection;

class PostLikeRepository
{
    public function __construct(private Connection $connection)
    {
    }

    private function pdo(): PDO
    {
        return $this->connection->pdo();
    }

    public function add(int $postId, int $userId): void
    {
        $stmt = $this->pdo()->prepare('INSERT IGNORE INTO post_likes (post_id, user_id) VALUES (:post_id, :user_id)');
        $stmt->execute([
            'post_id' => $postId,
            'user_id' => $userId,
        ]);
    }

    public function remove(int $postId, int $userId): void
    {
        $stmt = $this->pdo()->prepare('DELETE FROM post_likes WHERE post_id = :post_id AND user_id = :user_id');
        $stmt->execute([
            'post_id' => $postId,
            'user_id' => $userId,
        ]);
    }

    public function likedByUser(int $postId, int $userId): bool
    {
        $stmt = $this->pdo()->prepare('SELECT 1 FROM post_likes WHERE post_id = :post_id AND user_id = :user_id');
        $stmt->execute([
            'post_id' => $postId,
            'user_id' => $userId,
        ]);

        return (bool) $stmt->fetchColumn();
    }
}
