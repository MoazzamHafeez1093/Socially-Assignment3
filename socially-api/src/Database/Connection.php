<?php
namespace Socially\Database;

use PDO;
use PDOException;

class Connection
{
    private PDO $pdo;

    public function __construct(string $host, string $db, string $user, string $pass, int $port)
    {
        $dsn = "mysql:host=$host;port=$port;dbname=$db;charset=utf8mb4";
        $this->pdo = new PDO($dsn, $user, $pass, [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION
        ]);
    }

    public function pdo(): PDO
    {
        return $this->pdo;
    }
}
