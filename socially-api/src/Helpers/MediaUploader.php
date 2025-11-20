<?php

namespace Socially\Helpers;

use Psr\Http\Message\UploadedFileInterface;

class MediaUploader
{
    public function __construct(private string $rootPath, private string $baseUrl)
    {
    }

    public function store(UploadedFileInterface $file, string $subDirectory): string
    {
        $this->assertUpload($file);

        $folder = rtrim($this->rootPath, DIRECTORY_SEPARATOR) . DIRECTORY_SEPARATOR . trim($subDirectory, '/');
        if (!is_dir($folder) && !mkdir($folder, 0775, true) && !is_dir($folder)) {
            throw new \RuntimeException('Unable to create upload directory: ' . $folder);
        }

        $extension = pathinfo($file->getClientFilename() ?? '', PATHINFO_EXTENSION) ?: 'bin';
        $fileName = uniqid($subDirectory . '_', true) . '.' . $extension;
        $targetPath = $folder . DIRECTORY_SEPARATOR . $fileName;

        $file->moveTo($targetPath);

        $relativePath = str_replace(rtrim($this->rootPath, DIRECTORY_SEPARATOR), '', $targetPath);
        $relativePath = str_replace('\\', '/', $relativePath);

        return rtrim($this->baseUrl, '/') . '/' . ltrim($relativePath, '/');
    }

    private function assertUpload(UploadedFileInterface $file): void
    {
        if ($file->getError() !== UPLOAD_ERR_OK) {
            throw new \RuntimeException('File upload failed with error code ' . $file->getError());
        }
    }
}
