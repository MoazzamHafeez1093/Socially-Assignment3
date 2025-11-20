<?php
use Slim\Routing\RouteCollectorProxy;
use Socially\Controllers\AuthController;
use Socially\Controllers\StoryController;
use Socially\Controllers\PostController;
use Socially\Controllers\FollowController;
use Socially\Controllers\MessageController;
use Socially\Controllers\ProfileController;
use Socially\Controllers\SearchController;
use Socially\Controllers\PresenceController;
use Socially\Controllers\FcmController;
use Socially\Middleware\AuthMiddleware;
use Socially\Repositories\SessionRepository;
use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;

// Health check endpoint
$app->get('/health', function (Request $request, Response $response) {
    $response->getBody()->write(json_encode(['status' => 'ok', 'timestamp' => time()]));
    return $response->withHeader('Content-Type', 'application/json');
});

// Test endpoint to verify routing works
$app->get('/test', function (Request $request, Response $response) {
    $response->getBody()->write(json_encode([
        'message' => 'Socially API is running',
        'endpoints' => [
            'POST /api/auth/signup' => 'Register new user',
            'POST /api/auth/login' => 'Login user',
            'GET /api/stories' => 'Get stories (protected)',
        ]
    ]));
    return $response->withHeader('Content-Type', 'application/json');
});

// Handle OPTIONS requests for CORS preflight
$app->options('/{routes:.+}', function (Request $request, Response $response) {
    return $response;
});

// Resolve services from container
$container = $app->getContainer();
$sessionRepository = $container->get(SessionRepository::class);
$authMiddleware = new AuthMiddleware($_ENV['JWT_SECRET'], $sessionRepository);

// Public API routes
$app->group('/api', function (RouteCollectorProxy $group) {
    $group->post('/auth/signup', [AuthController::class, 'signup']);
    $group->post('/auth/login', [AuthController::class, 'login']);
});

// Protected routes
$app->group('/api', function (RouteCollectorProxy $group) {
    $group->get('/auth/me', [AuthController::class, 'me']);
    $group->post('/auth/logout', [AuthController::class, 'logout']);

    // Stories
    $group->get('/stories', [StoryController::class, 'index']);
    $group->post('/stories', [StoryController::class, 'upload']);

    // Posts
    $group->get('/posts', [PostController::class, 'index']);
    $group->post('/posts', [PostController::class, 'store']);
    $group->put('/posts/{id}', [PostController::class, 'update']);
    $group->delete('/posts/{id}', [PostController::class, 'destroy']);
    $group->post('/posts/{id}/likes', [PostController::class, 'like']);
    $group->delete('/posts/{id}/likes', [PostController::class, 'unlike']);
    $group->post('/posts/{id}/comments', [PostController::class, 'comment']);
    $group->get('/posts/{id}/comments', [PostController::class, 'comments']);

    // Follows & Requests
    $group->post('/follows/request', [FollowController::class, 'request']);
    $group->post('/follows/{id}/accept', [FollowController::class, 'accept']);
    $group->post('/follows/{id}/reject', [FollowController::class, 'reject']);
    $group->delete('/follows/{id}', [FollowController::class, 'unfollow']);
    $group->get('/follows/pending', [FollowController::class, 'pending']);
    $group->get('/users/{id}/followers', [FollowController::class, 'followers']);
    $group->get('/users/{id}/following', [FollowController::class, 'following']);

    // Messages
    $group->get('/messages/{userId}', [MessageController::class, 'conversation']);
    $group->post('/messages', [MessageController::class, 'send']);
    $group->put('/messages/{id}', [MessageController::class, 'update']);
    $group->delete('/messages/{id}', [MessageController::class, 'destroy']);
    $group->post('/messages/{id}/read', [MessageController::class, 'markRead']);

    // Profile & Search
    $group->get('/users/{id}', [ProfileController::class, 'show']);
    $group->post('/profile/image', [ProfileController::class, 'updateProfileImage']);
    $group->post('/profile/cover', [ProfileController::class, 'updateCoverImage']);
    $group->get('/search/users', [SearchController::class, 'users']);

    // Presence
    $group->post('/presence/ping', [PresenceController::class, 'ping']);
    $group->post('/presence/offline', [PresenceController::class, 'setOffline']);
    $group->get('/presence/{userId}', [PresenceController::class, 'show']);
    $group->post('/presence/bulk', [PresenceController::class, 'bulk']);

    // FCM Token Management
    $group->post('/fcm/token', [FcmController::class, 'registerToken']);
    $group->delete('/fcm/token', [FcmController::class, 'deleteToken']);
})->add($authMiddleware);
