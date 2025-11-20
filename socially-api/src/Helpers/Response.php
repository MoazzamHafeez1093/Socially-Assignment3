<?php

namespace Socially\Helpers;

use Psr\Http\Message\ResponseInterface as Response;

class Response
{
    public static function success(Response $response, array $data, int $status = 200): Response
    {
        $payload = json_encode(['status' => 'success', 'data' => $data], JSON_UNESCAPED_SLASHES);
        $response->getBody()->write($payload);
        return $response->withHeader('Content-Type', 'application/json')->withStatus($status);
    }

    public static function error(Response $response, string $message, int $status): Response
    {
        $payload = json_encode(['status' => 'error', 'message' => $message], JSON_UNESCAPED_SLASHES);
        $response->getBody()->write($payload);
        return $response->withHeader('Content-Type', 'application/json')->withStatus($status);
    }
}
