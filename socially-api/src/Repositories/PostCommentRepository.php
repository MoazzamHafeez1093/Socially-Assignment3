<?php

namespace Socially\Repositories;

use PDO;
use Socially\Database\Connection;

class PostCommentRepository
{
    public function __construct(private Connection $connection)
    {
    }

    private function pdo(): PDO
    {
        return $this->connection->pdo();
    }

    public function add(int $postId, int $userId, string $comment): array
    {
        $stmt = $this->pdo()->prepare('INSERT INTO post_comments (post_id, user_id, comment) VALUES (:post_id, :user_id, :comment)');
        $stmt->execute([
            'post_id' => $postId,
            'user_id' => $userId,
            'comment' => $comment,
        ]);

        return $this->findById((int) $this->pdo()->lastInsertId());
    }

    public function listForPost(int $postId, int $limit = 100): array
    {
        $stmt = $this->pdo()->prepare(
            'SELECT pc.*, u.username, u.profile_image
             FROM post_comments pc
             JOIN users u ON u.id = pc.user_id
             WHERE pc.post_id = :post_id
             ORDER BY pc.created_at ASC
             LIMIT :limit'
        );
        $stmt->bindValue(':post_id', $postId, PDO::PARAM_INT);
        $stmt->bindValue(':limit', $limit, PDO::PARAM_INT);
        $stmt->execute();

        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    public function findById(int $id): ?array
    {
        $stmt = $this->pdo()->prepare('SELECT * FROM post_comments WHERE id = :id');
        $stmt->execute(['id' => $id]);
        $comment = $stmt->fetch(PDO::FETCH_ASSOC);

        return $comment ?: null;
    }
}
