<?php
namespace Socially\Middleware;

use Firebase\JWT\JWT;
use Firebase\JWT\Key;
use Psr\Http\Message\ResponseInterface;
use Psr\Http\Message\ServerRequestInterface;
use Slim\Exception\HttpUnauthorizedException;
use Socially\Repositories\SessionRepository;

class AuthMiddleware
{
    public function __construct(private string $secret, private SessionRepository $sessions) {}

    public function __invoke(ServerRequestInterface $request, $handler): ResponseInterface
    {
        $authHeader = $request->getHeaderLine('Authorization');
        if (!str_starts_with($authHeader, 'Bearer ')) {
            throw new HttpUnauthorizedException($request, 'Missing token');
        }

        $token = substr($authHeader, 7);

        try {
            $payload = JWT::decode($token, new Key($this->secret, 'HS256'));
        } catch (\Throwable $e) {
            throw new HttpUnauthorizedException($request, 'Invalid token');
        }

        $jti = $payload->jti ?? null;
        if (!$jti || !$this->sessions->exists($jti)) {
            throw new HttpUnauthorizedException($request, 'Session expired');
        }

        $request = $request
            ->withAttribute('userId', (int) $payload->sub)
            ->withAttribute('tokenJti', $jti);

        return $handler->handle($request);
    }
}
