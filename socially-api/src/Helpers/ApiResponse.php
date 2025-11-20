<?php

namespace Socially\Helpers;

use Psr\Http\Message\ResponseInterface;

class ApiResponse
{
    public static function success(ResponseInterface $response, array $data, int $status = 200): ResponseInterface
    {
        $payload = json_encode(['status' => 'success', 'data' => $data], JSON_UNESCAPED_SLASHES);
        $response->getBody()->write($payload);
        return $response->withHeader('Content-Type', 'application/json')->withStatus($status);
    }

    public static function error(ResponseInterface $response, string $message, int $status): ResponseInterface
    {
        $payload = json_encode(['status' => 'error', 'message' => $message], JSON_UNESCAPED_SLASHES);
        $response->getBody()->write($payload);
        return $response->withHeader('Content-Type', 'application/json')->withStatus($status);
    }
}
