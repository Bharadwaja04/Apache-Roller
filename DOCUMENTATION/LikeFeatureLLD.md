# Like (Star) Feature — Low-Level Design

## Overview

This document describes the **Low-Level Design (LLD)** of the *Like/Star* feature for the Apache Roller blog platform. The feature allows any registered user to **like (star) a blog post exactly once**, stores the like in the database, and exposes the total like count to all users along with whether the currently logged-in user has liked a given post.

---

## 1. Domain Model

### 1.1 Classes

#### `User` (existing — `org.apache.roller.weblogger.pojos.User`)

| Field          | Type    | Description                        |
|----------------|---------|------------------------------------|
| `id`           | String  | UUID primary key                   |
| `userName`     | String  | Unique username (login)            |
| `emailAddress` | String  | Email address                      |
| `enabled`      | Boolean | Whether the account is active      |
| `dateCreated`  | Date    | Account creation timestamp         |

**Responsibilities:**
- Represents a registered user who can like blog posts.
- Identity is established by `userName`.

---

#### `WeblogEntry` / BlogPost (existing — `org.apache.roller.weblogger.pojos.WeblogEntry`)

| Field           | Type    | Description                             |
|-----------------|---------|-----------------------------------------|
| `id`            | String  | UUID primary key                        |
| `title`         | String  | Title of the blog post                  |
| `pubTime`       | Timestamp | Publication timestamp                 |
| `status`        | String  | PUBLISHED / DRAFT / etc.               |
| `website`       | Weblog  | Parent weblog                           |
| `creatorUserName` | String | Author's username                     |

**Responsibilities:**
- Represents the blog post entity that can be liked.
- Each `WeblogEntry` can receive likes from multiple users.

---

#### `StarredEntry` / Like (existing — `org.apache.roller.weblogger.pojos.StarredEntry`)

| Field         | Type         | Description                              |
|---------------|--------------|------------------------------------------|
| `id`          | String       | UUID primary key                         |
| `user`        | User         | The user who liked the post              |
| `entry`       | WeblogEntry  | The blog post that was liked             |
| `dateStarred` | Date         | Timestamp when the like was made         |

**Responsibilities:**
- Join-table entity linking a `User` to a `WeblogEntry`.
- A `(user, entry)` pair is stored at most once, enforcing **one like per user per post**.
- Mapped to database table `roller_starred_entry`.

**Relationships:**

```
User ──(1)──< StarredEntry >──(1)── WeblogEntry
             (likes)
```

- A `User` can have many `StarredEntry` records (liked many posts)
- A `WeblogEntry` can have many `StarredEntry` records (liked by many users)
- A pair `(User, WeblogEntry)` can have **at most one** `StarredEntry` (enforced by check-before-store in service layer)

---

### 1.2 Class Relationship Diagram

```
┌─────────────┐        ┌──────────────────┐        ┌──────────────────┐
│    User      │ 1    * │   StarredEntry   │ *    1  │  WeblogEntry     │
│─────────────│───────►│─────────────────-│◄───────│──────────────────│
│ id           │        │ id               │         │ id               │
│ userName     │        │ user (FK)        │         │ title            │
│ emailAddress │        │ entry (FK)       │         │ pubTime          │
│ enabled      │        │ dateStarred      │         │ status           │
└─────────────┘        └──────────────────┘         │ creatorUserName  │
                                                     └──────────────────┘
```

---

## 2. Repository Layer (Repository Pattern)

The **Repository Pattern** abstracts all database access. Each entity has a dedicated repository responsible for CRUD and query operations. This decouples business logic from persistence technology (JPA/EclipseLink in this case).

**Why Repository Pattern?**
- Centralizes all database queries in one place per entity.
- Makes it easy to swap the persistence provider (e.g., from JPA to JDBC) without touching service logic.
- Improves testability — repositories can be mocked in unit tests.

### 2.1 `UserRepository`

**Interface:** `org.apache.roller.weblogger.business.UserService`  
**Implementation:** `org.apache.roller.weblogger.business.jpa.JPAUserManagerImpl`

| Method                                                         | Description                               |
|----------------------------------------------------------------|-------------------------------------------|
| `addUser(User)`                                                | Persist a new user                        |
| `saveUser(User)`                                               | Update existing user                      |
| `removeUser(User)`                                             | Delete user and cascade-delete their likes|
| `getUserByUserName(String)`                                    | Lookup user by username                   |
| `getUsers(Boolean, Date, Date, int, int)`                      | Paginated user list                       |

---

### 2.2 `BlogPostRepository`

**Interface:** `org.apache.roller.weblogger.business.WeblogEntryManager`  
**Implementation:** `org.apache.roller.weblogger.business.jpa.JPAWeblogEntryManagerImpl`

| Method                              | Description                           |
|-------------------------------------|---------------------------------------|
| `getWeblogEntry(String id)`         | Load a blog post by ID                |
| `saveWeblogEntry(WeblogEntry)`      | Persist a blog post                   |
| `removeWeblogEntry(WeblogEntry)`    | Delete a blog post (cascades likes)   |
| `getWeblogEntries(criteria...)`     | Query blog posts with filters         |

---

### 2.3 `LikeRepository`

**Embedded in:** `org.apache.roller.weblogger.business.UserService` / `JPAUserManagerImpl`  
**Backed by ORM query file:** `StarredEntry.orm.xml`

| Method                                     | Named Query Used                              | Description                          |
|--------------------------------------------|-----------------------------------------------|--------------------------------------|
| `toggleStarEntry(User, WeblogEntry)`       | `StarredEntry.getByUser&Entry`                | Like or unlike (toggle)              |
| `isStarredEntry(User, WeblogEntry)`        | `StarredEntry.getByUser&Entry`                | Check if user liked this post        |
| `getStarredEntries(User, offset, length)`  | `StarredEntry.getStarredEntriesByUser`        | Get all posts liked by user          |
| `getStarredEntriesCount(User)`             | `StarredEntry.getCountStarredEntriesByUser`   | Count posts liked by user            |
| `getLikeCount(WeblogEntry)`                | `StarredEntry.getCountByEntry` *(new)*        | Count total likes on a post          |

#### Named Queries (in `StarredEntry.orm.xml`)

```xml
<!-- Check if user liked a specific post -->
<named-query name="StarredEntry.getByUser&amp;Entry">
  <query>SELECT s FROM StarredEntry s WHERE s.user = ?1 AND s.entry = ?2</query>
</named-query>

<!-- Get all posts starred by a user, newest first -->
<named-query name="StarredEntry.getStarredEntriesByUser">
  <query>SELECT s.entry FROM StarredEntry s WHERE s.user = ?1 ORDER BY s.dateStarred DESC</query>
</named-query>

<!-- Count posts liked by user -->
<named-query name="StarredEntry.getCountStarredEntriesByUser">
  <query>SELECT COUNT(s) FROM StarredEntry s WHERE s.user = ?1</query>
</named-query>

<!-- NEW: Count total likes (from all users) on one post -->
<named-query name="StarredEntry.getCountByEntry">
  <query>SELECT COUNT(s) FROM StarredEntry s WHERE s.entry = ?1</query>
</named-query>

<!-- Cleanup on user delete -->
<named-query name="StarredEntry.removeByUser">
  <query>DELETE FROM StarredEntry s WHERE s.user = ?1</query>
</named-query>

<!-- Cleanup on entry delete -->
<named-query name="StarredEntry.removeByEntry">
  <query>DELETE FROM StarredEntry s WHERE s.entry = ?1</query>
</named-query>
```

---

## 3. Service Layer (Service Layer Pattern)

The **Service Layer Pattern** encapsulates all business logic for the like feature. Services depend on repositories, not on the database directly.

**Why Service Layer Pattern?**
- Business rules (e.g., "one like per user per post") are centralized and not scattered in UI actions.
- Services are reusable across multiple entry points (web actions, REST endpoints, tests).
- Supports transaction management at the service boundary.

### 3.1 `UserService` (includes `LikeService` responsibilities)

**Interface:** `org.apache.roller.weblogger.business.UserService`

```java
// Like/star operations
void toggleStarEntry(User user, WeblogEntry entry) throws WebloggerException;
boolean isStarredEntry(User user, WeblogEntry entry) throws WebloggerException;
List<WeblogEntry> getStarredEntries(User user, int offset, int length) throws WebloggerException;
long getStarredEntriesCount(User user) throws WebloggerException;
long getLikeCount(WeblogEntry entry) throws WebloggerException;  // NEW
```

**Duplicate-like prevention** (in `toggleStarEntry`):
```
1. Query DB for StarredEntry WHERE user=? AND entry=?
2. If found → DELETE (unlike)
3. If NOT found → INSERT (like)
```
This guarantees atomicity and prevents duplicate rows at the service level.

---

### 3.2 `BlogPostService`

**Interface:** `org.apache.roller.weblogger.business.WeblogEntryManager`

Handles CRUD for blog posts. When a blog post is deleted, all associated `StarredEntry` records must be removed. This is handled via the `StarredEntry.removeByEntry` named query in `WeblogRemovalService`.

---

## 4. UI Layer (Struts 2 Actions)

### 4.1 `StarAction` (existing)

**Class:** `org.apache.roller.weblogger.ui.struts2.core.StarAction`  
**URL:** `/roller-ui/star`  
**Parameters:** `entryId` or `weblogId`  
**Behaviour:** Calls `UserManager.toggleStarEntry(user, entry)` then redirects.

### 4.2 `StarredEntriesAction` (existing)

**Class:** `org.apache.roller.weblogger.ui.struts2.core.StarredEntriesAction`  
**URL:** `/roller-ui/starredEntries`  
**Behaviour:** Lists all entries liked by the logged-in user with pagination. Also exposes a `likeCounts` map so the JSP can show total likes per entry.

### 4.3 `LikeCountAction` (new)

**Class:** `org.apache.roller.weblogger.ui.struts2.core.LikeCountAction`  
**URL:** `/roller-ui/likeCount`  
**Parameters:** `entryId`  
**Returns:** `likeCount` (long) and `userLiked` (boolean)  
**Behaviour:** Loads the entry, calls `getLikeCount(entry)` and `isStarredEntry(user, entry)`. Used to power a like count badge visible to all logged-in users.

---

## 5. Database Schema

### Existing Tables (relevant)

```sql
-- Users
CREATE TABLE roller_user (
    id           VARCHAR(48) NOT NULL PRIMARY KEY,
    username     VARCHAR(255) NOT NULL UNIQUE,
    passphrase   VARCHAR(255) NOT NULL,
    fullname     VARCHAR(255) NOT NULL,
    emailaddress VARCHAR(255) NOT NULL,
    datecreated  TIMESTAMP NOT NULL,
    isenabled    SMALLINT DEFAULT 1 NOT NULL
);

-- Blog posts
CREATE TABLE weblogentry (
    id           VARCHAR(48) NOT NULL PRIMARY KEY,
    title        VARCHAR(255),
    pubtime      TIMESTAMP,
    status       VARCHAR(20),
    websiteid    VARCHAR(48) NOT NULL  -- FK → weblog.id
    -- ...other columns omitted for brevity
);

-- Likes / Stars (junction table)
CREATE TABLE roller_starred_entry (
    id          VARCHAR(48) NOT NULL PRIMARY KEY,
    userid      VARCHAR(48) NOT NULL,   -- FK → roller_user.id
    entryid     VARCHAR(48) NOT NULL,   -- FK → weblogentry.id
    datestarred TIMESTAMP NOT NULL
);
-- Implicit uniqueness enforced at service level (check-before-insert)
-- Recommended index for performance:
CREATE INDEX rse_entryid_idx ON roller_starred_entry(entryid);
CREATE INDEX rse_userid_idx  ON roller_starred_entry(userid);
```

**Preventing Duplicate Likes:**
- The service layer checks for an existing `StarredEntry` before inserting.
- For high-traffic deployments, add a `UNIQUE(userid, entryid)` constraint for DB-level enforcement.

---

## 6. Observer Pattern (Optional — Notification Extension)

The **Observer Pattern** can be used to trigger side-effects when a post is liked, such as sending a notification email to the post author.

**Why Observer Pattern?**
- Decouples the like action from notification logic.
- Notifications can be added/removed without changing the core service.
- Multiple observers can react to the same like event (e.g., analytics, notifications).

### Design

```java
// Event
public class EntryLikedEvent {
    private final User liker;
    private final WeblogEntry entry;
}

// Observer interface
public interface LikeObserver {
    void onEntryLiked(EntryLikedEvent event);
}

// Example: email notification observer
public class LikeNotificationObserver implements LikeObserver {
    @Override
    public void onEntryLiked(EntryLikedEvent event) {
        // send email to entry.getCreatorUserName()
    }
}

// In UserService.toggleStarEntry():
// After storing new StarredEntry:
//   for (LikeObserver observer : observers) { observer.onEntryLiked(new EntryLikedEvent(user, entry)); }
```

> **Note:** Observer-based notifications are **not implemented** in the current codebase. The toggle action is synchronous and returns immediately after DB update. This extension point is noted for future scalability.

---

## 7. Example API Endpoints

The application uses **Struts 2 actions** routed via `struts.xml`. The following patterns describe the effective HTTP interface:

| Action               | Method | URL                                             | Parameters               | Description                                 |
|----------------------|--------|-------------------------------------------------|--------------------------|---------------------------------------------|
| User Registration    | POST   | `/roller-ui/register`                           | username, password, email| Register a new user account                 |
| Login                | POST   | `/roller-ui/login`                              | j_username, j_password   | Authenticate user (Spring Security)         |
| List Blog Posts      | GET    | `/{weblog-handle}/`                             | —                        | View blog entries (public)                  |
| Like / Unlike Post   | GET    | `/roller-ui/star?entryId={id}`                  | `entryId`                | Toggle like on a blog post (requires login) |
| View Liked Posts     | GET    | `/roller-ui/starredEntries`                     | `page` (optional)        | List all posts liked by current user        |
| Get Like Count       | GET    | `/roller-ui/likeCount?entryId={id}`             | `entryId`                | Returns total like count + userLiked flag   |

### Example: Like a Post

```
GET /roller-ui/star?entryId=abc-123
→ Redirects to /roller-ui/menu.rol
→ DB: INSERT INTO roller_starred_entry (id, userid, entryid, datestarred) VALUES (...)
      (or DELETE if already liked — toggle behaviour)
```

### Example: Get Like Count

```
GET /roller-ui/likeCount?entryId=abc-123
→ Response properties: likeCount=42, userLiked=true
→ DB: SELECT COUNT(s) FROM StarredEntry s WHERE s.entry = ?1
      SELECT s FROM StarredEntry s WHERE s.user = ?1 AND s.entry = ?2
```

---

## 8. Scalability Considerations

| Concern                   | Design Decision                                                              |
|---------------------------|------------------------------------------------------------------------------|
| Duplicate like prevention | Service-level check + optional DB UNIQUE constraint on (userid, entryid)     |
| Read performance          | Indexed `entryid` and `userid` columns on `roller_starred_entry`             |
| Count query cost          | `COUNT(*)` with index is O(1) on indexed column; cache if needed             |
| Many users liking         | Table grows linearly with likes; no joins needed for count query             |
| Notification decoupling   | Observer pattern can fire async emails without slowing the like response     |
