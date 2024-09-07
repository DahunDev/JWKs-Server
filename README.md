# JWKs-Server

## Overview

`JWKs-Server` is a JSON Web Key Set (JWKS) server designed for issuing JWT tokens and managing them securely with AES encryption and SQLite. The server includes rate limiting to prevent abuse.

## Key Features

- **AES Encryption**: Securely encrypts private keys using AES encryption, ensuring that sensitive data remains protected.
- **User Management**: Facilitates user registration and securely stores user credentials. Includes a registration endpoint for creating users with hashed passwords, utilizing the Argon2 hashing algorithm.
- **Comprehensive Logging**: Tracks and logs authentication requests with detailed information, including IP addresses, timestamps, and user IDs. This aids in monitoring and auditing access to the server.
- **Rate Limiting**: Implements a time-window rate limiter to control request frequency and prevent abuse. Requests are limited to 10 per second, with excess requests receiving a `429 Too Many Requests` response.
