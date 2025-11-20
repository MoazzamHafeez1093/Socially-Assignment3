<?php

namespace Socially\Repositories;

use DateInterval;
use DateTimeImmutable;
use PDO;
use Socially\Database\Connection;

class MessageRepository
{
    public function __construct(private Connection $connection)
    {
    }

    private function pdo(): PDO
    {
        return $this->connection->pdo();
    }

    public function create(
        int $senderId,
        int $receiverId,
        ?string $text,
        ?string $mediaUrl,
        ?string $mediaType,
        bool $vanishMode
    ): array {
        $stmt = $this->pdo()->prepare(
            'INSERT INTO messages (sender_id, receiver_id, message, media_url, media_type, vanish_mode)
             VALUES (:sender_id, :receiver_id, :message, :media_url, :media_type, :vanish_mode)'
        );
        $stmt->execute([
            'sender_id' => $senderId,
            'receiver_id' => $receiverId,
            'message' => $text,
            'media_url' => $mediaUrl,
            'media_type' => $mediaType,
            'vanish_mode' => $vanishMode ? 1 : 0,
        ]);

        return $this->findById((int) $this->pdo()->lastInsertId());
    }

    public function findById(int $id): ?array
    {
        $stmt = $this->pdo()->prepare('SELECT * FROM messages WHERE id = :id');
        $stmt->execute(['id' => $id]);
        $message = $stmt->fetch(PDO::FETCH_ASSOC);

        return $message ?: null;
    }

    public function conversation(int $userA, int $userB, int $limit = 100): array
    {
        $stmt = $this->pdo()->prepare(
            'SELECT m.* FROM messages m
             WHERE ((m.sender_id = :user_a AND m.receiver_id = :user_b)
                    OR (m.sender_id = :user_b AND m.receiver_id = :user_a))
               AND (m.deleted_at IS NULL)
             ORDER BY m.created_at DESC
             LIMIT :limit'
        );
        $stmt->bindValue(':user_a', $userA, PDO::PARAM_INT);
        $stmt->bindValue(':user_b', $userB, PDO::PARAM_INT);
        $stmt->bindValue(':limit', $limit, PDO::PARAM_INT);
        $stmt->execute();

        return array_reverse($stmt->fetchAll(PDO::FETCH_ASSOC));
    }

    public function updateText(int $messageId, int $userId, string $text): ?array
    {
        $stmt = $this->pdo()->prepare('UPDATE messages SET message = :message WHERE id = :id AND sender_id = :user_id');
        $stmt->execute([
            'message' => $text,
            'id' => $messageId,
            'user_id' => $userId,
        ]);

        if ($stmt->rowCount() === 0) {
            return null;
        }

        return $this->findById($messageId);
    }

    public function softDelete(int $messageId, int $userId): bool
    {
        $stmt = $this->pdo()->prepare('UPDATE messages SET deleted_at = NOW() WHERE id = :id AND sender_id = :user_id');
        $stmt->execute([
            'id' => $messageId,
            'user_id' => $userId,
        ]);

        return $stmt->rowCount() > 0;
    }

    public function markRead(int $messageId, int $userId): ?array
    {
        $stmt = $this->pdo()->prepare('UPDATE messages SET read_at = NOW() WHERE id = :id AND receiver_id = :user_id');
        $stmt->execute([
            'id' => $messageId,
            'user_id' => $userId,
        ]);

        if ($stmt->rowCount() === 0) {
            return null;
        }

        return $this->findById($messageId);
    }

    public function deleteIfVanishAndRead(int $messageId): void
    {
        $stmt = $this->pdo()->prepare('UPDATE messages SET deleted_at = NOW() WHERE id = :id AND vanish_mode = 1');
        $stmt->execute(['id' => $messageId]);
    }

    public function editable(array $message, int $windowMinutes = 5): bool
    {
        $createdAt = new DateTimeImmutable($message['created_at']);
        $deadline = $createdAt->add(new DateInterval('PT' . $windowMinutes . 'M'));

        return $deadline > new DateTimeImmutable() && !$message['deleted_at'];
    }
}
