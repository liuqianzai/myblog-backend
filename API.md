# MyBlog 后端 API 文档

> 基础路径: `http://localhost:8080/api`
>
> 认证方式: `Authorization: Bearer {token}`（登录后获取）
>
> 统一响应格式:
> ```json
> { "code": 200, "message": "success", "data": ... }
> ```

---

## 目录

- [1. 认证模块](#1-认证模块)
- [2. 文章管理](#2-文章管理)
- [3. 分类管理](#3-分类管理)
- [4. 标签管理](#4-标签管理)
- [5. 评论管理](#5-评论管理)
- [6. 文件上传](#6-文件上传)
- [7. 友情链接](#7-友情链接)
- [8. 独立页面](#8-独立页面)
- [9. 配置管理](#9-配置管理)
- [10. 归档](#10-归档)
- [11. 仪表盘](#11-仪表盘)
- [12. 智能体助手模块](#12-智能体助手模块)
- [13. 通用说明](#13-通用说明)

---

## 1. 认证模块

### 1.1 登录

```
POST /auth/login
```

**Request Body:**
```json
{
  "username": "admin",
  "password": "123456"
}
```

**Response:**
```json
{
  "token": "a1b2c3d4e5f6...",
  "username": "admin",
  "nickname": "管理员"
}
```

### 1.2 登出

```
POST /auth/logout
Header: Authorization: Bearer {token}
```

### 1.3 修改密码

```
POST /auth/password
Header: Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

---

## 2. 文章管理

### 2.1 前台 - 文章列表

```
GET /articles?page=1&size=10&keyword=&categoryId=&tagId=
```

参数均为可选：
| 参数 | 类型 | 说明 |
|------|------|------|
| page | Long | 页码，默认 1 |
| size | Long | 每页条数，默认 10，最大 100 |
| keyword | String | 标题/摘要搜索 |
| categoryId | Long | 分类筛选 |
| tagId | Long | 标签筛选 |

**Response:**
```json
{
  "total": 100,
  "page": 1,
  "size": 10,
  "records": [
    {
      "id": 1,
      "title": "文章标题",
      "summary": "摘要",
      "content": "正文Markdown",
      "cover": "封面URL",
      "categoryId": 1,
      "viewCount": 42,
      "isTop": true,
      "status": true,
      "createTime": "2024-01-01T00:00:00",
      "updateTime": "2024-01-01T00:00:00",
      "category": { "id": 1, "name": "分类名", "slug": "slug", "description": "描述", "sort": 0 },
      "tags": [{ "id": 1, "name": "标签名", "color": "#2563EB" }]
    }
  ]
}
```

### 2.2 前台 - 文章详情

```
GET /articles/{id}
```

阅读量自动 +1。

### 2.3 后台 - 文章列表

```
GET /admin/articles?page=1&size=10&keyword=&status=&categoryId=&tagId=
```

比前台多一个 `status` 参数，可筛选草稿/已发布。

### 2.4 后台 - 文章详情

```
GET /admin/articles/{id}
```
阅读量不增加，可查看草稿。

### 2.5 后台 - 创建文章

```
POST /admin/articles
Header: Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "title": "标题（必填）",
  "content": "Markdown正文（必填）",
  "summary": "摘要",
  "cover": "封面图URL",
  "categoryId": 1,
  "isTop": false,
  "status": true,
  "tagIds": [1, 2, 3]
}
```

**Response:** `{ "id": 1 }`

### 2.6 后台 - 更新文章

```
PUT /admin/articles/{id}
Header: Authorization: Bearer {token}
```

Body 字段同创建。

### 2.7 后台 - 删除文章

```
DELETE /admin/articles/{id}
Header: Authorization: Bearer {token}
```

逻辑删除（`deleted = 1`）。

---

## 3. 分类管理

所有接口无需认证。

### 3.1 分类列表

```
GET /categories
```

**Response:**
```json
[
  { "id": 1, "name": "技术", "slug": "tech", "description": "技术相关", "sort": 0, "createTime": "...", "updateTime": "..." }
]
```

### 3.2 分类详情

```
GET /categories/{id}
```

### 3.3 创建分类

```
POST /categories
```

**Request Body:**
```json
{
  "name": "分类名（必填）",
  "slug": "tech",
  "description": "描述",
  "sort": 0
}
```

### 3.4 更新分类

```
PUT /categories/{id}
```

### 3.5 删除分类

```
DELETE /categories/{id}
```

自动将属于该分类的文章的 `categoryId` 置空。

---

## 4. 标签管理

所有接口无需认证。

### 4.1 标签列表

```
GET /tags
```

**Response:**
```json
[
  { "id": 1, "name": "Java", "color": "#2563EB" }
]
```

### 4.2 标签详情

```
GET /tags/{id}
```

### 4.3 创建标签

```
POST /tags
```

**Request Body:**
```json
{
  "name": "标签名（必填）",
  "color": "#2563EB"
}
```

### 4.4 更新标签

```
PUT /tags/{id}
```

### 4.5 删除标签

```
DELETE /tags/{id}
```

自动删除关联关系。

---

## 5. 评论管理

### 5.1 前台 - 评论列表

```
GET /comments?articleId=1&page=1&size=10
```

只返回已审核通过的评论。

**Response 中的单条评论：**
```json
{
  "id": 1,
  "articleId": 1,
  "nickname": "访客",
  "content": "写得很清楚",
  "createTime": "2026-11-06T10:00:00"
}
```

> 公开列表**不返回 `email`** —— 评论者邮箱属于个人信息，不应随匿名接口下发。
> 后台接口 `GET /admin/comments` 仍会返回完整字段。

### 5.2 前台 - 提交评论

```
POST /comments
```

**Request Body:**
```json
{
  "articleId": 1,
  "nickname": "访客名（必填）",
  "email": "xxx@example.com",
  "content": "评论内容（必填）"
}
```

提交后默认待审核（`approved = false`）。

### 5.3 后台 - 评论列表

```
GET /admin/comments?articleId=&approved=&page=&size=
Header: Authorization: Bearer {token}
```

### 5.4 后台 - 审核评论

```
PATCH /admin/comments/{id}/review
Header: Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "approved": true
}
```

### 5.5 后台 - 删除评论

```
DELETE /admin/comments/{id}
Header: Authorization: Bearer {token}
```

---

## 6. 文件上传

### 6.1 上传文件（通用）

```
POST /files
（或 POST /files/images）
Header: Authorization: Bearer {token}
Content-Type: multipart/form-data
```

| 参数 | 类型 | 说明 |
|------|------|------|
| file | File | 文件对象（必填），单个文件最大 10MB |

**支持的文件格式白名单：**
- **图片**：`jpg`, `jpeg`, `png`, `gif`, `webp`, `ico`, `bmp`（**不含 `svg`**：SVG 可内嵌脚本，而上传目录与站点同源，打开即执行，会形成存储型 XSS）
- **文档**：`pdf`, `doc`, `docx`, `xls`, `xlsx`, `ppt`, `pptx`, `txt`, `md`
- **压缩包**：`zip`, `rar`, `7z`, `tar`, `gz`
- **音视频**：`mp3`, `mp4`, `wav`, `webm`

**Response:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "url": "/api/files/2026-09-03/xxx.pdf",
    "filename": "xxx.pdf"
  }
}
```

前端访问文件: `http://localhost:8080/api/files/2026-09-03/xxx.pdf`

---

## 7. 友情链接

### 7.1 前台 - 友链列表

```
GET /friend-links
```

只返回 `status = true` 的友链，按 `sort` 升序。

**Response:**
```json
[
  {
    "id": 1,
    "name": "示例博客",
    "url": "https://example.com",
    "avatar": "头像URL",
    "description": "描述",
    "sort": 0,
    "status": true,
    "createTime": "..."
  }
]
```

### 7.2 后台 - 友链列表

```
GET /admin/friend-links
Header: Authorization: Bearer {token}
```

返回全部友链。

### 7.3 后台 - 友链详情

```
GET /admin/friend-links/{id}
```

### 7.4 后台 - 创建友链

```
POST /admin/friend-links
```

**Request Body:**
```json
{
  "name": "名称（必填）",
  "url": "https://example.com（必填）",
  "avatar": "头像URL",
  "description": "描述",
  "sort": 0,
  "status": true
}
```

### 7.5 后台 - 更新友链

```
PUT /admin/friend-links/{id}
```

### 7.6 后台 - 删除友链

```
DELETE /admin/friend-links/{id}
```

---

## 8. 独立页面

### 8.1 前台 - 按 slug 获取页面

```
GET /pages/{slug}
```

只返回 `status = true` 的页面。
例如：`/pages/about`、`/pages/friends`

**Response:**
```json
{
  "id": 1,
  "title": "关于我",
  "slug": "about",
  "content": "Markdown内容",
  "status": true,
  "createTime": "...",
  "updateTime": "..."
}
```

### 8.2 后台 - 页面列表

```
GET /admin/pages
Header: Authorization: Bearer {token}
```

### 8.3 后台 - 页面详情

```
GET /admin/pages/{id}
```

### 8.4 后台 - 创建页面

```
POST /admin/pages
```

**Request Body:**
```json
{
  "title": "标题（必填）",
  "slug": "about（必填，唯一）",
  "content": "正文Markdown（必填）",
  "status": true
}
```

### 8.5 后台 - 更新页面

```
PUT /admin/pages/{id}
```

### 8.6 后台 - 删除页面

```
DELETE /admin/pages/{id}
```

---

## 9. 配置管理

读接口无需认证，但只返回**站点级公开配置**（键名以 `site_`、`home_`、`profile_`、`about_`、`social_` 开头）。
写接口与全量读取需要**管理员**身份。

### 9.1 配置列表（公开，已过滤）

```
GET /configs
```

**Response:**
```json
[
  { "configKey": "site_title", "configValue": "Liu Yang's Blog" }
]
```

> 不会返回 `remark` 字段，也不会返回白名单以外的任何配置项（例如将来可能出现的
> `llm_api_key`、`oss_secret` 等密钥类配置）。

### 9.2 配置详情（公开，已过滤）

```
GET /configs/{key}
```

非白名单键返回 `404 config not found`（不确认该键是否存在）。

### 9.3 保存配置（新增/更新，需管理员）

```
POST /configs
Header: Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "configKey": "author_name",
  "configValue": "刘洋",
  "remark": "作者名称"
}
```

### 9.4 删除配置（需管理员）

```
DELETE /configs/{key}
Header: Authorization: Bearer {token}
```

### 9.5 后台查看全部配置（需管理员）

```
GET /admin/configs
Header: Authorization: Bearer {token}
```

返回全部配置项（含 `remark` 备注），供后台编辑页使用。

---

## 10. 归档

### 10.1 获取文章归档

```
GET /archives
```

无需认证。

**Response:**
```json
[
  { "month": "2024-01", "count": 5 },
  { "month": "2023-12", "count": 3 }
]
```

按月份降序排列。

---

## 11. 仪表盘

### 11.1 获取统计概览

```
GET /admin/dashboard/stats
Header: Authorization: Bearer {token}
```

**Response:**
```json
{
  "totalArticles": 100,
  "publishedArticles": 80,
  "hiddenArticles": 20,
  "totalTags": 10,
  "totalCategories": 5,
  "totalComments": 200,
  "pendingComments": 15,
  "totalViews": 5000
}
```

---

## 12. 智能体助手模块

### 12.1 行为感知上报与小宠物气泡响应

```
POST /agent/pet/bubble
```

免密接口（游客可用，带 Token 可识别管理员身份）。用于前端小宠物常驻挂件上报用户互动行为（如算运势、做测验、深度阅读、页面进入等），并获取小宠物的气泡文案与纯文本下一步操作按钮。

**Request Body:**
```json
{
  "eventType": "ACTION_CUSTOM",
  "scene": "FORTUNE_PAGE",
  "metaTags": ["事业", "转机", "自强不息"],
  "summary": "摇得未济卦，事未成，充满转机",
  "targetId": null,
  "visitorSessionId": "session_visitor_12345",
  "payload": {
    "divinationName": "未济卦",
    "omen": "未成转机"
  }
}
```

**Response:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "bubbleText": "算得未济卦，心境顺应天时。为你推荐这篇《程序员如何对抗焦虑与技术瓶颈》助益前行：",
    "petMood": "THINKING",
    "autoDismissSec": 10,
    "suggestActions": [
      {
        "actionCode": "NAVIGATE_ARTICLE",
        "label": "顺应卦象",
        "payload": { "articleId": 100, "title": "程序员如何对抗焦虑与技术瓶颈" }
      },
      {
        "actionCode": "OPEN_CHAT",
        "label": "与助手聊聊",
        "payload": { "contextToken": "c8f3e2b1..." }
      },
      {
        "actionCode": "DISMISS_BUBBLE",
        "label": "收起气泡",
        "payload": {}
      }
    ],
    "relatedArticle": {
      "id": 100,
      "title": "程序员如何对抗焦虑与技术瓶颈",
      "summary": "面对转机与低谷时的心境修炼指南",
      "cover": "http://...",
      "viewCount": 352,
      "createTime": "2026-10-20T10:00:00",
      "categoryName": "随笔杂谈",
      "tags": ["修心", "职场"],
      "matchReason": "匹配心境标签: 事业、转机"
    },
    "contextToken": "c8f3e2b1...",
    "cooldownSec": 0
  }
}
```

### 12.2 获取文章相关推荐 (Item-to-Item)

```
GET /agent/recommend/related?articleId=12&limit=5
```

基于标签重合度、同分类加权与热度，为当前阅读文章推荐最相关的优质文章。

### 12.3 获取探索精选推荐 (冷启动/时间衰减)

```
GET /agent/recommend/explore?limit=5
```

基于 Hacker News 时间衰减算法与置顶加权，推荐近期优质文章。

### 12.4 根据业务语义标签匹配推荐

```
GET /agent/recommend/tags?tags=高并发,Redis&limit=5
```

### 12.5 管理端操作联想 (后台专用)

```
POST /admin/agent/actions/suggest
Header: Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "scene": "ADMIN_POST_SAVED",
  "targetId": 15
}
```

**Response:**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "actionCode": "PUSH_QQ",
      "label": "推送QQ群",
      "payload": { "articleId": 15 }
    },
    {
      "actionCode": "PIN_TOP",
      "label": "设为置顶",
      "payload": { "articleId": 15 }
    },
    {
      "actionCode": "VIEW_PAGE",
      "label": "查看前台",
      "payload": { "articleId": 15 }
    }
  ]
}
```

---

## 13. 通用说明

### 13.1 统一响应格式

成功:
```json
{ "code": 200, "message": "success", "data": ... }
```

失败:
```json
{ "code": 400, "message": "错误描述", "data": null }
```

常见错误码:

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误（如必填字段缺失、路径参数类型不匹配） |
| 401 | 未登录或 token 无效、账号已停用 |
| 403 | 已登录但无管理员权限 |
| 404 | 资源不存在、接口不存在 |
| 405 | 请求方法不支持 |
| 415 | Content-Type 不支持 |
| 500 | 服务器内部错误 |

### 13.2 分页参数

所有分页接口统一：
- `page`: 页码，从 1 开始，默认 1
- `size`: 每页条数，默认 10，最大 100

分页响应：
```json
{
  "total": 100,
  "page": 1,
  "size": 10,
  "records": [...]
}
```

### 13.3 认证范围对照表

| 接口范围 | 是否需要认证 | 需要管理员角色 |
|----------|:----------:|:----------:|
| `POST /auth/login` | ❌ | — |
| `GET /articles/**`, `/archives` | ❌ | — |
| `GET /tags/**`, `/categories/**` | ❌ | — |
| `GET /configs/**`（仅白名单配置） | ❌ | — |
| `GET /friend-links` | ❌ | — |
| `GET /pages/**` | ❌ | — |
| `GET /comments`（仅已审核，不含邮箱） | ❌ | — |
| `POST /comments`（提交评论） | ❌ | — |
| `GET /article-tags` | ❌ | — |
| `POST /agent/pet/bubble`（行为感知小宠物气泡） | ❌ | — |
| `GET /agent/recommend/**`（智能文章推荐） | ❌ | — |
| 所有 `/admin/**` 路径（包含 `/admin/agent/**`） | ✅ | ✅ |
| `GET /admin/configs`（全量配置） | ✅ | ✅ |
| `POST /files`, `POST /files/images` | ✅ | ✅ |
| `POST/PUT/DELETE /categories/**`, `/tags/**`, `/article-tags/**` | ✅ | ✅ |
| `POST/DELETE /configs/**` | ✅ | ✅ |
| `POST /auth/logout`, `POST /auth/password` | ✅ | ❌（本人即可） |

> 令牌通过验证并不等于拥有后台权限：`/admin/**` 与所有站点数据写操作都要求账号
> `role = admin` 且 `status = 1`。
