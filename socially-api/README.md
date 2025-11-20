# Socially API - Backend Server

PHP REST API for Socially social media application with authentication, stories, posts, messaging, and more.

## Requirements

- **PHP** >= 8.1
- **MySQL/MariaDB** >= 5.7
- **Composer** 2.x
- **Apache/Nginx** with mod_rewrite enabled
- **XAMPP** (recommended for local development on Windows)

## Installation & Setup

### 1. Copy to XAMPP htdocs

```bash
# Copy entire socially-api folder to XAMPP htdocs
cp -r socially-api C:\xampp\htdocs\
```

Or create an Apache alias in `httpd-vhosts.conf`:

```apache
<VirtualHost *:80>
    ServerName socially.local
    DocumentRoot "D:/Project_Source/Socially-Assignment3/socially-api/public"
    
    <Directory "D:/Project_Source/Socially-Assignment3/socially-api/public">
        AllowOverride All
        Require all granted
    </Directory>
</VirtualHost>
```

### 2. Install Dependencies

```bash
cd socially-api
composer install
```

### 3. Configure Environment

Copy `.env.example` to `.env` and update:

```env
# Database
DB_HOST=localhost
DB_PORT=3306
DB_DATABASE=socially_assignment
DB_USERNAME=root
DB_PASSWORD=

# JWT Auth
JWT_SECRET=your_super_secret_jwt_key_here_change_in_production
JWT_TTL=86400

# FCM Notifications
FCM_SERVER_KEY=your_firebase_cloud_messaging_server_key

# Media Storage
MEDIA_PATH=D:/Project_Source/Socially-Assignment3/socially-api/storage/uploads
MEDIA_BASE_URL=http://localhost/socially-api/storage/uploads
```

**Important:** Change `MEDIA_PATH` and `MEDIA_BASE_URL` if you copied to htdocs!

### 4. Create Database

```sql
-- In MySQL console or phpMyAdmin
CREATE DATABASE socially_assignment CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 5. Run Migrations

```bash
# Import schema
mysql -u root -p socially_assignment < database/migrations/001_create_tables.sql
```

Or via phpMyAdmin: Import `database/migrations/001_create_tables.sql`

### 6. Set Permissions (if needed)

```bash
# Make storage writable
chmod -R 775 storage/
```

On Windows with XAMPP, ensure Apache has write permissions to `storage/` folder.

### 7. Test API

Visit: `http://localhost/socially-api/public/health`

Expected response:
```json
{
  "status": "ok",
  "timestamp": 1700000000
}
```

## API Endpoints

### Public Endpoints

- `POST /api/auth/signup` - Register new user
- `POST /api/auth/login` - Login user

### Protected Endpoints (require JWT token)

**Auth:**
- `GET /api/auth/me` - Get current user
- `POST /api/auth/logout` - Logout (invalidate session)

**Stories:**
- `GET /api/stories` - List recent stories (24h)
- `POST /api/stories` - Upload story (multipart/form-data with `media` file)

**Posts:**
- `GET /api/posts` - Feed of posts with likes/comments count
- `POST /api/posts` - Create post (optional `media` file)
- `PUT /api/posts/{id}` - Update post
- `DELETE /api/posts/{id}` - Delete post
- `POST /api/posts/{id}/likes` - Like post
- `DELETE /api/posts/{id}/likes` - Unlike post
- `POST /api/posts/{id}/comments` - Add comment
- `GET /api/posts/{id}/comments` - List comments

**Follows:**
- `POST /api/follows/request` - Send follow request
- `POST /api/follows/{id}/accept` - Accept request
- `POST /api/follows/{id}/reject` - Reject request
- `DELETE /api/follows/{id}` - Unfollow
- `GET /api/follows/pending` - Pending requests (incoming/outgoing)
- `GET /api/users/{id}/followers` - Get followers
- `GET /api/users/{id}/following` - Get following

**Messages:**
- `GET /api/messages/{userId}` - Conversation with user
- `POST /api/messages` - Send message (text + optional media)
- `PUT /api/messages/{id}` - Edit message (5min window)
- `DELETE /api/messages/{id}` - Delete message (5min window)
- `POST /api/messages/{id}/read` - Mark as read (triggers vanish delete)

**Profile & Search:**
- `GET /api/users/{id}` - Get user profile
- `POST /api/profile/image` - Update profile image
- `POST /api/profile/cover` - Update cover image
- `GET /api/search/users?q=term` - Search users

**Presence:**
- `POST /api/presence/ping` - Update online status
- `POST /api/presence/offline` - Mark as offline
- `GET /api/presence/{userId}` - Get user online status
- `POST /api/presence/bulk` - Get bulk presence (array of user_ids)

**FCM Notifications:**
- `POST /api/fcm/token` - Register FCM device token
- `DELETE /api/fcm/token` - Delete FCM token

## Authentication

All protected endpoints require JWT token in header:

```
Authorization: Bearer <your_jwt_token>
```

Obtain token from `/api/auth/login` response.

## Features Implemented

✅ **JWT Authentication** - Signup, login, session management  
✅ **Stories** - Upload with 24h auto-expiry  
✅ **Posts** - CRUD with media, likes, comments  
✅ **Follow System** - Request/accept/reject flow  
✅ **Messaging** - Text/media, edit/delete (5min window), vanish mode  
✅ **Search** - User search by username/email  
✅ **Profile** - View profile, update images  
✅ **Presence** - Online/offline status tracking  
✅ **FCM Notifications** - Push notifications for messages, follows, likes, comments  

## Database Schema

Tables:
- `users` - User accounts
- `sessions` - Active login sessions
- `stories` - Story posts with expiry
- `posts` - Feed posts with media
- `post_likes` - Post likes
- `post_comments` - Post comments
- `follows` - Follow relationships
- `follow_requests` - Pending follow requests
- `messages` - Direct messages with vanish mode
- `user_presence` - Online/offline status
- `user_fcm_tokens` - FCM device tokens

## Configuration Notes

### Base Path

API is configured with base path `/socially-api/public` in `public/index.php`:

```php
$app->setBasePath('/socially-api/public');
```

If you deploy to root or different path, update this line accordingly.

### CORS

CORS is enabled for all origins (development mode). For production, restrict to your Android app:

```php
// In public/index.php
'Access-Control-Allow-Origin' => 'http://your-production-domain.com'
```

### File Upload Limits

Update `php.ini` for larger uploads:

```ini
upload_max_filesize = 50M
post_max_size = 50M
max_execution_time = 300
```

Restart Apache after changes.

## Troubleshooting

**500 Error:**
- Check PHP error log: `C:\xampp\apache\logs\error.log`
- Verify `.env` file exists and has correct DB credentials
- Run `composer dump-autoload`

**Database Connection Failed:**
- Verify MySQL service is running
- Check DB credentials in `.env`
- Ensure database exists: `SHOW DATABASES;`

**Upload Fails:**
- Check `storage/uploads/` folder exists
- Verify Apache has write permissions
- Check `php.ini` upload limits

**JWT Errors:**
- Ensure `JWT_SECRET` is set in `.env`
- Check token is sent in `Authorization: Bearer <token>` header

## Development

### Regenerate Autoloader

After adding new classes:

```bash
composer dump-autoload
```

### Create New Migration

Add SQL file to `database/migrations/` with naming pattern: `00X_description.sql`

### Add New Endpoint

1. Create method in appropriate controller
2. Register route in `src/routes.php`
3. Add to protected group if auth required
4. Run `composer dump-autoload`

## Production Deployment

- [ ] Change `JWT_SECRET` to random 64-char string
- [ ] Update `MEDIA_BASE_URL` to production URL
- [ ] Restrict CORS to app domain only
- [ ] Enable HTTPS
- [ ] Set proper file permissions (755 folders, 644 files)
- [ ] Add rate limiting middleware
- [ ] Enable query logging for monitoring
- [ ] Add `.htaccess` security rules

## License

Educational project for CS4039 SMD Assignment 3
