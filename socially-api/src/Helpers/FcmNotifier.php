<?php

namespace Socially\Helpers;

class FcmNotifier
{
    private string $serverKey;

    public function __construct(string $serverKey)
    {
        $this->serverKey = $serverKey;
    }

    public function send(string $token, array $message): bool
    {
        // TODO: integrate with FCM HTTP v1
        return false;
    }
}
