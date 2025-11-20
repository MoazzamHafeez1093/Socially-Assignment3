<?php

namespace Socially\Controllers;

use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;
use Socially\Helpers\Response as ApiResponse;
use Socially\Repositories\UserRepository;

class SearchController
{
    public function __construct(private UserRepository $users)
    {
    }

    public function users(Request $request, Response $response): Response
    {
        $query = $request->getQueryParams()['q'] ?? '';
        if (strlen($query) < 2) {
            return ApiResponse::error($response, 'Search query must be at least 2 characters', 422);
        }

        $results = $this->users->search($query);
        return ApiResponse::success($response, ['users' => $results]);
    }
}
