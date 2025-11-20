<?php
require __DIR__ . '/../src/bootstrap.php';

// Register routes FIRST
require __DIR__ . '/../src/routes.php';

// Set base path for Apache (when not in document root)
$app->setBasePath('/socially-api/public');

// Then add middleware
$app->addBodyParsingMiddleware();
$app->addRoutingMiddleware();

// Simple CORS
$app->add(function ($request, $handler) {
    $response = $handler->handle($request);
    return $response
        ->withHeader('Access-Control-Allow-Origin', '*')
        ->withHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization')
        ->withHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
});

// Add error middleware last
$errorMiddleware = $app->addErrorMiddleware(true, true, true);

$app->run();
