<?php

namespace Socially\Controllers;

use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;
use Respect\Validation\Exceptions\NestedValidationException;
use Respect\Validation\Validator as v;
use Socially\Helpers\MediaUploader;
use Socially\Helpers\Response as ApiResponse;
use Socially\Repositories\PostCommentRepository;
use Socially\Repositories\PostLikeRepository;
use Socially\Repositories\PostRepository;

class PostController
{
    public function __construct(
        private PostRepository $posts,
        private PostLikeRepository $likes,
        private PostCommentRepository $comments,
        private MediaUploader $uploader
    ) {
    }

    public function index(Request $request, Response $response): Response
    {
        $limit = (int) ($request->getQueryParams()['limit'] ?? 50);
        $feed = $this->posts->feed($limit);

        return ApiResponse::success($response, ['posts' => $feed]);
    }

    public function store(Request $request, Response $response): Response
    {
        $userId = (int) $request->getAttribute('userId');
        $data = (array) $request->getParsedBody();

        $validator = v::arrayType()->key('caption', v::stringType()->length(null, 2000), false);
        try {
            $validator->assert($data);
        } catch (NestedValidationException $e) {
            return ApiResponse::error($response, $e->getFullMessage(), 422);
        }

        $mediaUrl = $this->uploadMedia($request, 'posts');
        $post = $this->posts->create($userId, $data['caption'] ?? null, $mediaUrl);

        return ApiResponse::success($response, ['post' => $post], 201);
    }

    public function update(Request $request, Response $response, array $args): Response
    {
        $userId = (int) $request->getAttribute('userId');
        $postId = (int) $args['id'];
        $data = (array) $request->getParsedBody();

        $validator = v::key('caption', v::optional(v::stringType()->length(null, 2000)), false);
        try {
            $validator->assert($data);
        } catch (NestedValidationException $e) {
            return ApiResponse::error($response, $e->getFullMessage(), 422);
        }

        $mediaUrl = $this->uploadMedia($request, 'posts', allowMissing: true) ?? ($data['media_url'] ?? null);
        $post = $this->posts->update($postId, $userId, $data['caption'] ?? null, $mediaUrl);

        if (!$post) {
            return ApiResponse::error($response, 'Post not found or not owned by user', 404);
        }

        return ApiResponse::success($response, ['post' => $post]);
    }

    public function destroy(Request $request, Response $response, array $args): Response
    {
        $userId = (int) $request->getAttribute('userId');
        $postId = (int) $args['id'];

        if (!$this->posts->delete($postId, $userId)) {
            return ApiResponse::error($response, 'Post not found or not owned by user', 404);
        }

        return ApiResponse::success($response, ['message' => 'Post deleted']);
    }

    public function like(Request $request, Response $response, array $args): Response
    {
        $userId = (int) $request->getAttribute('userId');
        $postId = (int) $args['id'];

        $this->likes->add($postId, $userId);
        return ApiResponse::success($response, ['liked' => true]);
    }

    public function unlike(Request $request, Response $response, array $args): Response
    {
        $userId = (int) $request->getAttribute('userId');
        $postId = (int) $args['id'];

        $this->likes->remove($postId, $userId);
        return ApiResponse::success($response, ['liked' => false]);
    }

    public function comment(Request $request, Response $response, array $args): Response
    {
        $userId = (int) $request->getAttribute('userId');
        $postId = (int) $args['id'];
        $data = (array) $request->getParsedBody();

        $validator = v::key('comment', v::stringType()->length(1, 1000));
        try {
            $validator->assert($data);
        } catch (NestedValidationException $e) {
            return ApiResponse::error($response, $e->getFullMessage(), 422);
        }

        $comment = $this->comments->add($postId, $userId, $data['comment']);
        return ApiResponse::success($response, ['comment' => $comment], 201);
    }

    public function comments(Request $request, Response $response, array $args): Response
    {
        $postId = (int) $args['id'];
        $limit = (int) ($request->getQueryParams()['limit'] ?? 100);
        $comments = $this->comments->listForPost($postId, $limit);

        return ApiResponse::success($response, ['comments' => $comments]);
    }

    private function uploadMedia(Request $request, string $folder, bool $allowMissing = false): ?string
    {
        $files = $request->getUploadedFiles();
        if (!isset($files['media'])) {
            return $allowMissing ? null : null;
        }

        return $this->uploader->store($files['media'], $folder);
    }
}
