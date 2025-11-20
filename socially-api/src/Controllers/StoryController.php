<?php

namespace Socially\Controllers;

use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;
use Respect\Validation\Exceptions\NestedValidationException;
use Respect\Validation\Validator as v;
use Socially\Helpers\MediaUploader;
use Socially\Helpers\Response as ApiResponse;
use Socially\Repositories\StoryRepository;

class StoryController
{
    public function __construct(private StoryRepository $stories, private MediaUploader $uploader)
    {
    }

    public function index(Request $request, Response $response): Response
    {
        $limit = (int) ($request->getQueryParams()['limit'] ?? 50);
        $stories = $this->stories->recent($limit);

        return ApiResponse::success($response, ['stories' => $stories]);
    }

    public function upload(Request $request, Response $response): Response
    {
        $userId = (int) $request->getAttribute('userId');
        $body = (array) $request->getParsedBody();
        $validator = v::key('media_type', v::in(['image', 'video']));

        try {
            $validator->assert($body);
        } catch (NestedValidationException $e) {
            return ApiResponse::error($response, $e->getFullMessage(), 422);
        }

        $files = $request->getUploadedFiles();
        if (!isset($files['media'])) {
            return ApiResponse::error($response, 'Media file is required', 422);
        }

        $mediaUrl = $this->uploader->store($files['media'], 'stories');
        $story = $this->stories->create($userId, $mediaUrl, $body['media_type']);

        return ApiResponse::success($response, ['story' => $story], 201);
    }
}
