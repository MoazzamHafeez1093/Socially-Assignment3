<?php

namespace Socially\Repositories;

use DateInterval;
use DateTimeImmutable;
use PDO;
use Socially\Database\Connection;

class StoryRepository
{
    public function __construct(private Connection $connection)
    {
    }

    private function pdo(): PDO
    {
        return $this->connection->pdo();
    }

    public function create(int $userId, string $mediaUrl, string $mediaType): array
    {
        $expiresAt = (new DateTimeImmutable())->add(new DateInterval('P1D'));
        $stmt = $this->pdo()->prepare(
            'INSERT INTO stories (user_id, media_url, media_type, expires_at) VALUES (:user_id, :media_url, :media_type, :expires_at)'
        );
        $stmt->execute([
            'user_id' => $userId,
            'media_url' => $mediaUrl,
            'media_type' => $mediaType,
            'expires_at' => $expiresAt->format('Y-m-d H:i:s'),
        ]);

        return $this->findById((int) $this->pdo()->lastInsertId());
    }

    public function findById(int $id): ?array
    {
        $stmt = $this->pdo()->prepare('SELECT * FROM stories WHERE id = :id');
        $stmt->execute(['id' => $id]);
        $story = $stmt->fetch(PDO::FETCH_ASSOC);

        return $story ?: null;
    }

    public function recent(int $limit = 50): array
    {
        $stmt = $this->pdo()->prepare(
            'SELECT s.*, u.username, u.profile_image FROM stories s JOIN users u ON u.id = s.user_id WHERE s.expires_at IS NULL OR s.expires_at > NOW() ORDER BY s.created_at DESC LIMIT :limit'
        );
        $stmt->bindValue(':limit', $limit, PDO::PARAM_INT);
        $stmt->execute();

        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    public function deleteExpired(): void
    {
        $this->pdo()->exec('DELETE FROM stories WHERE expires_at IS NOT NULL AND expires_at <= NOW()');
    }
}
