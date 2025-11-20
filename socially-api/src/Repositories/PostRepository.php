<?php

namespace Socially\Repositories;

use PDO;
use Socially\Database\Connection;

class PostRepository
{
    public function __construct(private Connection $connection)
    {
    }

    private function pdo(): PDO
    {
        return $this->connection->pdo();
    }

    public function create(int $userId, ?string $caption, ?string $mediaUrl): array
    {
        $stmt = $this->pdo()->prepare('INSERT INTO posts (user_id, caption, media_url) VALUES (:user_id, :caption, :media_url)');
        $stmt->execute([
            'user_id' => $userId,
            'caption' => $caption,
            'media_url' => $mediaUrl,
        ]);

        return $this->findById((int) $this->pdo()->lastInsertId());
    }

    public function update(int $postId, int $userId, ?string $caption, ?string $mediaUrl): ?array
    {
        $stmt = $this->pdo()->prepare('UPDATE posts SET caption = :caption, media_url = :media_url WHERE id = :id AND user_id = :user_id');
        $stmt->execute([
            'caption' => $caption,
            'media_url' => $mediaUrl,
            'id' => $postId,
            'user_id' => $userId,
        ]);

        if ($stmt->rowCount() === 0) {
            return null;
        }

        return $this->findById($postId);
    }

    public function delete(int $postId, int $userId): bool
    {
        $stmt = $this->pdo()->prepare('DELETE FROM posts WHERE id = :id AND user_id = :user_id');
        $stmt->execute([
            'id' => $postId,
            'user_id' => $userId,
        ]);

        return $stmt->rowCount() > 0;
    }

    public function findById(int $id): ?array
    {
        $stmt = $this->pdo()->prepare(
            'SELECT p.*, u.username, u.profile_image FROM posts p JOIN users u ON u.id = p.user_id WHERE p.id = :id'
        );
        $stmt->execute(['id' => $id]);
        $post = $stmt->fetch(PDO::FETCH_ASSOC);

        return $post ?: null;
    }

    public function feed(int $limit = 50): array
    {
        $stmt = $this->pdo()->prepare(
            'SELECT p.*, u.username, u.profile_image,
                    (SELECT COUNT(*) FROM post_likes pl WHERE pl.post_id = p.id) AS like_count,
                    (SELECT COUNT(*) FROM post_comments pc WHERE pc.post_id = p.id) AS comment_count
             FROM posts p
             JOIN users u ON u.id = p.user_id
             ORDER BY p.created_at DESC
             LIMIT :limit'
        );
        $stmt->bindValue(':limit', $limit, PDO::PARAM_INT);
        $stmt->execute();

        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }
}
