<?php
namespace Socially\Controllers;

use Firebase\JWT\JWT;
use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;
use Respect\Validation\Exceptions\NestedValidationException;
use Respect\Validation\Validator as v;
use Socially\Helpers\Response as ApiResponse;
use Socially\Repositories\SessionRepository;
use Socially\Repositories\UserRepository;
use Throwable;

class AuthController
{
    public function __construct(
        private UserRepository $users,
        private SessionRepository $sessions,
        private string $jwtSecret,
        private int $jwtTtl = 86400
    ) {
    }

    public function signup(Request $request, Response $response): Response
    {
        $data = (array) ($request->getParsedBody() ?? []);
        $validator = v::key('email', v::email())
            ->key('password', v::stringType()->length(6))
            ->key('username', v::stringType()->length(3));

        try {
            $validator->assert($data);
        } catch (NestedValidationException $e) {
            return $this->validationError($response, $e);
        }

        if ($this->users->emailExists($data['email'])) {
            return ApiResponse::error($response, 'Email already registered', 409);
        }

        if ($this->users->usernameExists($data['username'])) {
            return ApiResponse::error($response, 'Username is taken', 409);
        }

        try {
            $userId = $this->users->create(
                $data['username'],
                $data['email'],
                password_hash($data['password'], PASSWORD_BCRYPT)
            );
        } catch (Throwable $e) {
            return ApiResponse::error($response, 'Unable to create user at this time', 500);
        }

        $user = $this->users->findById($userId);
        [$token, $jti] = $this->issueToken($userId);
        $this->sessions->create($userId, $jti, $data['device_info'] ?? null);

        return ApiResponse::success($response, [
            'token' => $token,
            'requiresProfileSetup' => true,
            'user' => $this->publicUser($user)
        ], 201);
    }

    public function login(Request $request, Response $response): Response
    {
        $data = (array) ($request->getParsedBody() ?? []);
        $validator = v::key('email', v::email())
            ->key('password', v::stringType()->length(6));

        try {
            $validator->assert($data);
        } catch (NestedValidationException $e) {
            return $this->validationError($response, $e);
        }

        $user = $this->users->findByEmail($data['email']);
        if (!$user || !password_verify($data['password'], $user['password_hash'])) {
            return ApiResponse::error($response, 'Invalid credentials', 401);
        }

        [$token, $jti] = $this->issueToken((int) $user['id']);
        $this->sessions->create((int) $user['id'], $jti, $data['device_info'] ?? null);

        return ApiResponse::success($response, [
            'token' => $token,
            'user' => $this->publicUser($user)
        ]);
    }

    public function me(Request $request, Response $response): Response
    {
        $userId = (int) $request->getAttribute('userId');
        $user = $this->users->findById($userId);

        if (!$user) {
            return ApiResponse::error($response, 'User not found', 404);
        }

        return ApiResponse::success($response, ['user' => $this->publicUser($user)]);
    }

    public function logout(Request $request, Response $response): Response
    {
        $tokenJti = (string) $request->getAttribute('tokenJti');
        if ($tokenJti) {
            $this->sessions->deleteByToken($tokenJti);
        }

        return ApiResponse::success($response, ['message' => 'Logged out']);
    }

    private function issueToken(int $userId): array
    {
        $jti = bin2hex(random_bytes(16));
        $payload = [
            'sub' => $userId,
            'jti' => $jti,
            'iat' => time(),
            'exp' => time() + $this->jwtTtl,
        ];

        $token = JWT::encode($payload, $this->jwtSecret, 'HS256');
        return [$token, $jti];
    }

    private function publicUser(?array $user): array
    {
        return [
            'id' => $user['id'] ?? null,
            'username' => $user['username'] ?? null,
            'email' => $user['email'] ?? null,
            'profile_image' => $user['profile_image'] ?? null,
            'cover_image' => $user['cover_image'] ?? null,
            'created_at' => $user['created_at'] ?? null,
        ];
    }

    private function validationError(Response $response, NestedValidationException $e): Response
    {
        $message = implode('; ', array_map('trim', $e->getMessages()));
        return ApiResponse::error($response, $message, 422);
    }
}
