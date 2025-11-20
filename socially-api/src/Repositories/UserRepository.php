<?php

namespace Socially\Repositories;

use PDO;
use Socially\Database\Connection;

class UserRepository
{
    public function __construct(private Connection $connection)
    {
    }

    private function pdo(): PDO
    {
        return $this->connection->pdo();
    }

    public function create(string $username, string $email, string $passwordHash): int
    {
        $stmt = $this->pdo()->prepare(
            'INSERT INTO users (username, email, password_hash) VALUES (:username, :email, :password_hash)'
        );
        $stmt->execute([
            'username' => $username,
            'email' => $email,
            'password_hash' => $passwordHash,
        ]);

        return (int) $this->pdo()->lastInsertId();
    }

    public function findByEmail(string $email): ?array
    {
        $stmt = $this->pdo()->prepare('SELECT * FROM users WHERE email = :email LIMIT 1');
        $stmt->execute(['email' => $email]);
        $user = $stmt->fetch(PDO::FETCH_ASSOC);

        return $user ?: null;
    }

    public function findById(int $id): ?array
    {
        $stmt = $this->pdo()->prepare('SELECT * FROM users WHERE id = :id LIMIT 1');
        $stmt->execute(['id' => $id]);
        $user = $stmt->fetch(PDO::FETCH_ASSOC);

        return $user ?: null;
    }

    public function usernameExists(string $username): bool
    {
        $stmt = $this->pdo()->prepare('SELECT 1 FROM users WHERE username = :username LIMIT 1');
        $stmt->execute(['username' => $username]);

        return (bool) $stmt->fetchColumn();
    }

    public function emailExists(string $email): bool
    {
        $stmt = $this->pdo()->prepare('SELECT 1 FROM users WHERE email = :email LIMIT 1');
        $stmt->execute(['email' => $email]);

        return (bool) $stmt->fetchColumn();
    }
}
