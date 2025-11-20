<?php

namespace Socially\Controllers;

use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;
use Socially\Helpers\MediaUploader;
use Socially\Helpers\ApiResponse;
use Socially\Repositories\UserRepository;

class ProfileController
{
    public function __construct(private UserRepository $users, private MediaUploader $uploader)
    {
    }

    public function show(Request $request, Response $response, array $args): Response
    {
        $userId = (int) $args['id'];
        $user = $this->users->findById($userId);

        if (!$user) {
            return ApiResponse::error($response, 'User not found', 404);
        }

        return ApiResponse::success($response, ['user' => $this->publicUser($user)]);
    }

    public function updateProfileImage(Request $request, Response $response): Response
    {
        $userId = (int) $request->getAttribute('userId');
        $files = $request->getUploadedFiles();

        if (!isset($files['image'])) {
            return ApiResponse::error($response, 'Image file is required', 422);
        }

        $imageUrl = $this->uploader->store($files['image'], 'profiles');
        $this->users->updateProfileImage($userId, $imageUrl);

        return ApiResponse::success($response, ['profile_image' => $imageUrl]);
    }

    public function updateCoverImage(Request $request, Response $response): Response
    {
        $userId = (int) $request->getAttribute('userId');
        $files = $request->getUploadedFiles();

        if (!isset($files['image'])) {
            return ApiResponse::error($response, 'Image file is required', 422);
        }

        $imageUrl = $this->uploader->store($files['image'], 'covers');
        $this->users->updateCoverImage($userId, $imageUrl);

        return ApiResponse::success($response, ['cover_image' => $imageUrl]);
    }

    private function publicUser(array $user): array
    {
        return [
            'id' => $user['id'],
            'username' => $user['username'],
            'email' => $user['email'],
            'profile_image' => $user['profile_image'],
            'cover_image' => $user['cover_image'],
            'created_at' => $user['created_at'],
        ];
    }
}
