<?php
require_once __DIR__ . '/../vendor/autoload.php';

use DI\Container;
use Dotenv\Dotenv;
use Slim\Factory\AppFactory;
use Socially\Controllers\AuthController;
use Socially\Controllers\StoryController;
use Socially\Controllers\PostController;
use Socially\Controllers\FollowController;
use Socially\Controllers\MessageController;
use Socially\Controllers\ProfileController;
use Socially\Controllers\SearchController;
use Socially\Controllers\PresenceController;
use Socially\Controllers\FcmController;
use Socially\Helpers\MediaUploader;
use Socially\Helpers\FcmNotifier;
use Socially\Repositories\SessionRepository;
use Socially\Repositories\UserRepository;
use Socially\Repositories\StoryRepository;
use Socially\Repositories\PostRepository;
use Socially\Repositories\PostLikeRepository;
use Socially\Repositories\PostCommentRepository;
use Socially\Repositories\FollowRepository;
use Socially\Repositories\FollowRequestRepository;
use Socially\Repositories\MessageRepository;
use Socially\Repositories\PresenceRepository;
use Socially\Repositories\FcmTokenRepository;

$dotenv = Dotenv::createImmutable(__DIR__ . '/../');
$dotenv->safeLoad();

$container = new Container();
AppFactory::setContainer($container);
$app = AppFactory::create();

// Register shared services
$container->set('db', function () {
    return new Socially\Database\Connection(
        $_ENV['DB_HOST'],
        $_ENV['DB_DATABASE'],
        $_ENV['DB_USERNAME'],
        $_ENV['DB_PASSWORD'],
        (int) ($_ENV['DB_PORT'] ?? 3306)
    );
});

$container->set(UserRepository::class, fn ($c) => new UserRepository($c->get('db')));
$container->set(SessionRepository::class, fn ($c) => new SessionRepository($c->get('db')));
$container->set(StoryRepository::class, fn ($c) => new StoryRepository($c->get('db')));
$container->set(PostRepository::class, fn ($c) => new PostRepository($c->get('db')));
$container->set(PostLikeRepository::class, fn ($c) => new PostLikeRepository($c->get('db')));
$container->set(PostCommentRepository::class, fn ($c) => new PostCommentRepository($c->get('db')));
$container->set(FollowRepository::class, fn ($c) => new FollowRepository($c->get('db')));
$container->set(FollowRequestRepository::class, fn ($c) => new FollowRequestRepository($c->get('db')));
$container->set(MessageRepository::class, fn ($c) => new MessageRepository($c->get('db')));
$container->set(PresenceRepository::class, fn ($c) => new PresenceRepository($c->get('db')));
$container->set(FcmTokenRepository::class, fn ($c) => new FcmTokenRepository($c->get('db')));

$container->set(MediaUploader::class, fn () => new MediaUploader(
    $_ENV['MEDIA_PATH'],
    $_ENV['MEDIA_BASE_URL']
));

$container->set(FcmNotifier::class, fn () => new FcmNotifier($_ENV['FCM_SERVER_KEY'] ?? ''));

$container->set(AuthController::class, function ($c) {
    return new AuthController(
        $c->get(UserRepository::class),
        $c->get(SessionRepository::class),
        $_ENV['JWT_SECRET'],
        (int) ($_ENV['JWT_TTL'] ?? 86400)
    );
});

$container->set(StoryController::class, function ($c) {
    return new StoryController(
        $c->get(StoryRepository::class),
        $c->get(MediaUploader::class)
    );
});

$container->set(PostController::class, function ($c) {
    return new PostController(
        $c->get(PostRepository::class),
        $c->get(PostLikeRepository::class),
        $c->get(PostCommentRepository::class),
        $c->get(MediaUploader::class),
        $c->get(FcmNotifier::class),
        $c->get(UserRepository::class),
        $c->get(FcmTokenRepository::class)
    );
});

$container->set(FollowController::class, function ($c) {
    return new FollowController(
        $c->get(FollowRepository::class),
        $c->get(FollowRequestRepository::class),
        $c->get(UserRepository::class),
        $c->get(FcmNotifier::class),
        $c->get(FcmTokenRepository::class)
    );
});

$container->set(MessageController::class, function ($c) {
    return new MessageController(
        $c->get(MessageRepository::class),
        $c->get(MediaUploader::class),
        $c->get(FcmNotifier::class),
        $c->get(UserRepository::class),
        $c->get(FcmTokenRepository::class)
    );
});

$container->set(ProfileController::class, function ($c) {
    return new ProfileController(
        $c->get(UserRepository::class),
        $c->get(MediaUploader::class)
    );
});

$container->set(SearchController::class, function ($c) {
    return new SearchController(
        $c->get(UserRepository::class)
    );
});

$container->set(PresenceController::class, function ($c) {
    return new PresenceController(
        $c->get(PresenceRepository::class)
    );
});

$container->set(FcmController::class, function ($c) {
    return new FcmController(
        $c->get(FcmTokenRepository::class)
    );
});
