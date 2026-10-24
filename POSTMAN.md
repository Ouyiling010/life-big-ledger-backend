# 生活大作账 · 全接口流程测试文档（Postman / Apifox）

> 适用对象：后端自测、前端联调、功能验收
> 覆盖范围：13 个分组、80 个接口，按「注册 → 登录 → 记账 → 统计 → 家庭 → 后台 → 改密登出」的顺序串成一条可执行流程
> 编写基准：`2026-10`（文中的日期、月份均按 2026 年 10 月编写；换月执行时请同步替换日期与 `period`）

---

## 零、开始之前（必读）

### 0.1 启动应用

```powershell
cd c:\Users\xhCON\Desktop\code\backend\life-big-ledger
mvn spring-boot:run "-Dspring-boot.run.arguments=--app.email.mode=log --app.email.dev-return-code=true"
```

- `app.email.mode=log`：验证码只打日志，不依赖 SMTP。
- `app.email.dev-return-code=true`：`send-email-code` 的响应会带 `data.devCode`，注册/重置密码可直接拿到验证码。
- 不加重启参数时，`application.yml` 当前是 `smtp` 模式且不返回验证码，注册需真实收邮件。
- 启动成功标志：控制台出现 `Tomcat started on port 8090`，`http://localhost:8090/doc.html` 可打开。

> **报错 `{"code":500,"msg":"验证码发送失败，请稍后重试"}`**：说明启动时没带上面的 log 参数，走的是 `smtp` 模式，而 `MAIL_USERNAME` / `MAIL_AUTH_CODE` 没配置（或授权码错误），发信必然失败。两种解决办法：
> 1. **推荐（测试用）**：按 0.1 带 `--app.email.mode=log --app.email.dev-return-code=true` 重启，验证码直接在响应 `data.devCode` 里拿；
> 2. **要真收邮件**：先设环境变量再启动（QQ 邮箱需先在「设置→账户」开启 SMTP 并生成授权码，不是登录密码）：
> ```powershell
> $env:MAIL_USERNAME="你的QQ邮箱@qq.com"
> $env:MAIL_AUTH_CODE="邮箱授权码"
> mvn spring-boot:run
> ```

> 本轮后端修复（账本级联删除、分类父子判断、家庭日志 module、CSV 分类类型、退出家庭上下文残留）需**重启应用**才会生效。

### 0.2 导入数据库（建议每次完整回归前执行）

最新单文件脚本：`db/life_big_ledger.sql`（含 `CREATE DATABASE` / `USE`，可直接执行）。

```powershell
cmd /c "mysql -uroot -proot < db\life_big_ledger.sql"
```

> PowerShell 不支持 `<` 重定向，必须套一层 `cmd /c`；也可用 Navicat 直接运行该 SQL 文件。

### 0.3 统一约定

| 项目 | 约定 |
|---|---|
| Base URL | `http://localhost:8090` |
| 鉴权头 | `Authorization: Bearer {{token}}`（Sa-Token 固定前缀 `Bearer`） |
| JSON 请求头 | `Content-Type: application/json` |
| 金额单位 | 全部为「分」，`3500` = ¥35.00 |
| 业务状态 | **HTTP 状态码基本都是 200，业务结果看响应体 `code`**（401/403/404/409 都在 body 里） |
| 分页参数 | `current`（页码，从 1 开始）、`size`（每页条数） |
| 时间格式 | 记账时间 `happenedAt` 兼容四种写法：`2026-10-24 12:00:00`、`2026-10-24T12:00:00`、`2026-10-24T02:15:24.846Z`（带时区会自动换算到东八区）、`2026-10-24`（当天 00:00:00）；统计日期 `yyyy-MM-dd`；统计月份 `yyyy-MM` |
| 枚举值 | 账单类型 `1支出 2收入 3转账`；账户类型 `1现金 2储蓄卡 3信用卡 4虚拟`；上下文 `1个人 2家庭`；家庭角色 `1OWNER 2ADMIN 3MEMBER`；性别 `0未知 1男 2女` |
| 唯一响应外壳 | `{ "code": 200, "msg": "success", "data": ..., "timestamp": ... }` |

### 0.4 环境变量（按需创建，不必一次建全）

> **不需要提前把下表全部建好**。唯一必须先有的是 `{{baseUrl}}`；其余变量都来自后续接口的返回值，测到哪一步、就把那一步响应里的 `data.id`/`data.token` 存成变量即可。
> 如果只想先验证「发验证码 + 注册」，只建 `{{baseUrl}}`、`{{regCode}}`、`{{token}}` 三个就够跑通第一章。

| 变量 | 含义 / 来源 |
|---|---|
| `{{baseUrl}}` | `http://localhost:8090` |
| `{{token}}` | tester001 登录返回的 `data.token` |
| `{{token2}}` | tester002 登录返回的 `data.token` |
| `{{token3}}` | tester003 登录返回的 `data.token` |
| `{{adminToken}}` | admin 登录返回的 `data.token` |
| `{{bookId}}` | tester001 默认「日常账本」ID（GET /api/books 第 1 条） |
| `{{bookTravelId}}` | 新建「旅行账本」ID |
| `{{bookTempId}}` | 新建「临时账本」ID |
| `{{cashId}}` | 现金账户 ID |
| `{{alipayId}}` | 支付宝账户 ID |
| `{{wechatId}}` | 微信账户 ID |
| `{{savingsId}}` | 新建「招商银行」账户 ID |
| `{{catRootId}}` | 新建一级分类 ID |
| `{{catChildId}}` | 新建二级分类 ID |
| `{{billExpenseId}}` | 新建支出账单 ID |
| `{{billBatchA}}` / `{{billBatchB}}` | 批量删除用的两条账单 ID |
| `{{billTempId}}` | 临时账本下的账单 ID |
| `{{familyId}}` | 家庭 ID |
| `{{inviteCode}}` | 家庭邀请码 |
| `{{member2Id}}` | tester002 的家庭成员记录 ID（`family_member.id`） |
| `{{member3Id}}` | tester003 的家庭成员记录 ID |
| `{{familyBookId}}` | 家庭账本 ID |
| `{{familyCashId}}` | 家庭现金账户 ID |
| `{{notificationId}}` | 通知列表第一条的 `id` |
| `{{tester1UserId}}` / `{{tester3UserId}}` | 用户 ID（注册返回 `data.id`） |

> `{{var}}` 语法 Postman 与 Apifox 通用。所有创建类接口执行后，请立刻把响应里的 `data.id` 存入对应变量。

> **所有 URL 里的路径参数（`/api/xxx/{id}`）都是前面接口返回的资源 id，不需要自己猜。** 对照表：
>
> | 路径参数 | 从哪来 |
> |---|---|
> | `{{bookId}}` / `{{bookTravelId}}` / `{{bookTempId}}` | `GET /api/books` 的 `data[].id`，或 `POST /api/books` 的 `data.id` |
> | `{{cashId}}` / `{{alipayId}}` / `{{wechatId}}` / `{{savingsId}}` | `GET /api/accounts` 的 `data[].id`，或 `POST /api/accounts` 的 `data.id` |
> | `{{catRootId}}` / `{{catChildId}}` | `GET /api/categories/tree` 的节点 `id`，或 `POST /api/categories` 的 `data.id` |
> | `{{billExpenseId}}` / `{{billBatchA}}` / `{{billBatchB}}` / `{{billTempId}}` | `GET /api/bills` 的 `data.records[].id`，或 `POST /api/bills` 的 `data.id` |
> | `{{familyId}}` / `{{member2Id}}` / `{{member3Id}}` | `POST /api/families` 的 `data.id` / `GET /api/families/{familyId}/members` 的 `data[].id` |
> | `{{notificationId}}` | `GET /api/notifications` 的 `data.records[].id` |
>
> 前端同理：id 都来自列表接口返回的字段，或页面路由参数，不存在「拿不到 id」的情况。

### 0.5 JSON Body 里的变量写法

数字型字段不要加引号：

```json
{ "bookId": {{bookId}}, "amount": 3500 }
```

字符串型字段要加引号：

```json
{ "inviteCode": "{{inviteCode}}" }
```

---

## 一、认证（5 个接口，公开）

### 1.1 发送注册验证码

`POST {{baseUrl}}/api/auth/send-email-code`（无需 token）

```json
{
  "email": "tester001@example.com",
  "scene": "REGISTER"
}
```

预期：

```json
{ "code": 200, "msg": "success", "data": { "sent": true, "devCode": "482913" } }
```

> 把 `data.devCode` 记到 `{{regCode}}`。`smtp` 模式下 `devCode` 为 `null`，需去邮箱取码。
> **验证码有效期 60 秒**（`app.email.code-ttl`），只能使用一次；应用重启会清空（Caffeine 内存缓存），重启后必须重新获取。
> 限流：同一 IP 60 秒 5 次，超限返回 `code=429`、`msg="操作过于频繁，请稍后再试"`。
> 本文档共需发送 4 次验证码（tester001 注册、tester002、tester003、tester001 重置），若 60 秒内反复整篇重跑会触发限流，稍等再试即可。

### 1.2 注册

`POST {{baseUrl}}/api/auth/register`

```json
{
  "username": "tester001",
  "password": "Test@1234",
  "nickname": "测试一号",
  "email": "tester001@example.com",
  "emailCode": "{{regCode}}",
  "phone": "13800138001"
}
```

预期：`code=200`，`data.id` 为数字（记入 `{{tester1UserId}}`），`data.currentContext=1`、`data.hasGuided=0`、`data.isAdmin=0`，`data.email` 已脱敏为 `t***@example.com`。

> 注册成功会自动创建：1 个「日常账本」+ 3 个账户（现金/支付宝/微信，余额 0）。

负向用例（重复用户名）：

```json
{
  "username": "tester001",
  "password": "Test@1234",
  "nickname": "重复",
  "email": "other001@example.com",
  "emailCode": "123456"
}
```

预期：`code=409`，`msg="用户名已存在"`。

### 1.3 登录

`POST {{baseUrl}}/api/auth/login`

```json
{
  "account": "tester001",
  "password": "Test@1234"
}
```

预期：`code=200`，`data.token` 为 UUID 字符串，`data.user.username="tester001"`。

> **Token 提取路径：`$.data.token`**，存到 `{{token}}`。`account` 也支持传邮箱。

负向用例：

```json
{ "account": "tester001", "password": "wrong-pass" }
```

预期：`code=400`，`msg="用户名或密码错误"`。

### 1.4 未登录访问受保护接口（鉴权校验）

`GET {{baseUrl}}/api/user/profile`（不带 Authorization）

预期：`code=401`，`msg="未登录或登录已过期"`。

### 1.5 登出（放到最后 14.5 执行）

`POST {{baseUrl}}/api/auth/logout`（带 `{{token}}`）

预期：`code=200`，`data=null`；之后再访问 `/api/user/profile` 返回 `code=401`。

---

## 二、用户（8 个接口）

### 2.1 获取个人资料

`GET {{baseUrl}}/api/user/profile`（`{{token}}`）

预期：`code=200`，`data.username="tester001"`，`data.email` 脱敏，`data.phone="138****8001"`。

### 2.2 修改个人资料

`PUT {{baseUrl}}/api/user/profile`（`{{token}}`）

```json
{
  "nickname": "测试一号改名",
  "avatar": "/uploads/avatar/demo.png",
  "email": "tester001@example.com",
  "phone": "13900139001",
  "gender": 1
}
```

预期：`code=200`，返回更新后的 `data`（`nickname`、`gender` 已变化，手机号脱敏）。

> `email`/`phone` 允许传空串表示清空；填了就必须合法；邮箱不能与他人重复（否则 `409 邮箱已被使用`）。
> `gender` 只接受 `0/1/2`，传 `3` 等其它值 → `code=400`，`msg="性别只能是 0未知、1男、2女"`（前端可直接用这三项做下拉框）。

### 2.3 上传头像（multipart，非 JSON）

`POST {{baseUrl}}/api/user/avatar`（`{{token}}`）

Body 选 `form-data`：

| Key | Type | Value |
|---|---|---|
| `file` | File | 任意 `jpg/jpeg/png/gif/webp` 图片 |

预期：`code=200`，`data="/uploads/avatar/xxxx.png"`。
> 只能传**本地图片文件**，不能填网络图片 URL。上传拿到的路径**不会自动写进用户表**，还需把它填到 2.2 的 `avatar` 字段再调一次 `PUT /api/user/profile`。
> 浏览器访问地址 = `{{baseUrl}}` + 返回值，如 `http://localhost:8090/uploads/avatar/xxxx.png`（`/uploads/**` 不在登录拦截范围内，可直接打开）。
> 文件实际落在启动目录下的 `./uploads/avatar/`。

负向：上传 `.txt` → `code=400`，`msg="头像仅支持 jpg/jpeg/png/gif/webp 格式"`。

### 2.4 我的家庭列表（此时为空）

`GET {{baseUrl}}/api/user/families`（`{{token}}`）

预期：`code=200`，`data=[]`。

### 2.5 切换上下文（个人）

`PUT {{baseUrl}}/api/user/context`（`{{token}}`）

```json
{ "context": 1 }
```

预期：`code=200`，`data.currentContext=1`、`data.currentFamilyId` 字段不返回（为 null 时被 `non_null` 过滤）。

> 家庭模式 body 见 10.7：`{"context":2,"familyId":{{familyId}}}`。
> `context` 只接受 `1/2`，传 `3` → `code=400`，`msg="上下文只能是 1个人 或 2家庭"`；传 `2` 但没给 `familyId` → `code=400`，`msg="请选择家庭"`。

### 2.6 标记已完成引导

`PUT {{baseUrl}}/api/user/guided`（`{{token}}`）

无请求体。预期：`code=200`；随后 `GET /api/user/profile` 的 `hasGuided=1`。

### 2.7 修改自己在家庭中的昵称（家庭建立后执行，见 10.9）

`PUT {{baseUrl}}/api/user/family-nickname?familyId={{familyId}}&nicknameInFamily=爸爸`（`{{token}}`）

无请求体（参数在 URL 上）。预期：`code=200`。

> `nicknameInFamily` 可省略（表示清空），但 `familyId` 必填。

### 2.8 家庭模式获取个人资料

`GET {{baseUrl}}/api/user/profile`（已切家庭上下文）

预期：`code=200`，`data.currentContext=2`，`data.currentFamilyId={{familyId}}`。

---

## 三、账本（5 个接口）

### 3.1 账本列表

`GET {{baseUrl}}/api/books`（`{{token}}`）

预期：`code=200`，`data` 长度 1，第 1 条名称「日常账本」、`isDefault=1`、`billCount=0`、`expense=0`、`income=0`。

> 把第 1 条 `id` 存到 `{{bookId}}`。

### 3.2 新建「旅行账本」

`POST {{baseUrl}}/api/books`（`{{token}}`）

```json
{
  "name": "旅行账本",
  "cover": "/covers/travel.jpg",
  "icon": "plane",
  "currency": "CNY",
  "description": "国庆出行专用"
}
```

预期：`code=200`，`data.isDefault=0`，`data.billCount=0`。记 `data.id` → `{{bookTravelId}}`。

### 3.3 新建「临时账本」（用于 6.17 级联删除验证）

`POST {{baseUrl}}/api/books`

```json
{
  "name": "临时账本",
  "currency": "CNY",
  "description": "仅用于 force 删除测试"
}
```

预期：`code=200`。记 `data.id` → `{{bookTempId}}`。

### 3.4 编辑「旅行账本」

`PUT {{baseUrl}}/api/books/{{bookTravelId}}`（`{{token}}`）

```json
{
  "name": "旅行账本（已改）",
  "cover": "/covers/travel2.jpg",
  "icon": "car",
  "currency": "CNY",
  "description": "改为自驾"
}
```

预期：`code=200`；`GET /api/books` 可看到名称已变。

> **路径参数必须写双花括号 `{{bookTravelId}}`**。写成单花括号 `{bookTravelId}` 不会被替换（会返回 `400 参数格式不正确：id 应为整数`）；变量还没有值时 URL 会变成 `/api/books/`，返回 `404 资源不存在`——这时先回头执行 3.2 新建账本，把响应 `data.id` 存进变量，或临时把 URL 写成真实数字如 `/api/books/12`。
> **Apifox 变量必须在右上角选中的那个环境里定义**（环境 → 环境管理 → 开发环境 → 添加 `bookTravelId` 并保存）。没定义时 Apifox 会原样发送 `{{bookTravelId}}`，服务端报 `参数格式不正确：id 应为整数，当前值：{{bookTravelId}}`；在 Params 里加同名 **Query** 参数是无效的，服务端只读路径中的值。

### 3.5 设为默认账本 / 切换回默认

`PUT {{baseUrl}}/api/books/{{bookTravelId}}/default` → `code=200`（旅行账本 `isDefault=1`）

`PUT {{baseUrl}}/api/books/{{bookId}}/default` → `code=200`（日常账本恢复默认）

### 3.6 删除无账单的账本

`DELETE {{baseUrl}}/api/books/{{bookTravelId}}`（`{{token}}`）

预期：`code=200`；`GET /api/books` 不再出现「旅行账本」。

> 有账单的账本普通删除返回 `409`，需要 `?force=true` 才级联删除（见 6.18）。

---

## 四、账户（6 个接口）

### 4.1 账户列表

`GET {{baseUrl}}/api/accounts`（`{{token}}`）

预期：`code=200`，`data` 长度 3：现金、支付宝、微信；`balance` 均为 0，`includeInTotal=1`。

> 按名称记录：现金 `{{cashId}}`、支付宝 `{{alipayId}}`、微信 `{{wechatId}}`。

### 4.2 总资产

`GET {{baseUrl}}/api/accounts/total`（`{{token}}`）

预期：`code=200`，`data=0`。

### 4.3 新建账户

`POST {{baseUrl}}/api/accounts`（`{{token}}`）

```json
{
  "name": "招商银行",
  "type": 2,
  "initBalance": 100000,
  "icon": "bank",
  "remark": "工资卡",
  "includeInTotal": 1
}
```

预期：`code=200`，`data.balance=100000`、`data.typeName="储蓄卡"`。记 `data.id` → `{{savingsId}}`。
此时 `GET /api/accounts/total` → `100000`。

### 4.4 编辑账户

`PUT {{baseUrl}}/api/accounts/{{savingsId}}`（`{{token}}`）

```json
{
  "name": "招商银行储蓄卡",
  "type": 2,
  "initBalance": 100000,
  "icon": "bank",
  "remark": "工资卡（已改）",
  "includeInTotal": 1
}
```

预期：`code=200`；列表里名称、备注已更新，余额仍为 100000。

### 4.5 余额校正

`POST {{baseUrl}}/api/accounts/{{savingsId}}/adjust`（`{{token}}`）

```json
{ "targetBalance": 120000 }
```

预期：`code=200`，`data.balance=120000`。

> **`{{savingsId}}` 就是账户 id**：从 4.1 `GET /api/accounts` 的 `data[].id` 里取，也就是 4.3 新建「招商银行储蓄卡」返回的 `data.id`。换别的账户做校正，就用那个账户在列表里的 `id`，例如 `POST /api/accounts/16/adjust`。

> 差额 +20000 会生成一笔 `收入 20000` 的「余额校正」账单（计入默认账本），后续统计会包含它。

负向：`{"targetBalance": null}` → `code=400`，`msg="目标余额不能为空"`。

### 4.6 删除账户

有账单引用（刚才的余额校正账单）：

`DELETE {{baseUrl}}/api/accounts/{{savingsId}}`
预期：`code=409`，`msg` 含「笔账单引用，无法删除」。

删除干净账户：

```json
POST {{baseUrl}}/api/accounts
{ "name": "待删除账户", "type": 1, "initBalance": 0 }
```
→ `code=200`，记 `data.id`，再 `DELETE /api/accounts/{{id}}` → `code=200`。

> 家庭共享账户的编辑/删除/校正仅 ADMIN 及以上可操作。

---

## 五、分类（4 个接口）

### 5.1 分类树（支出）

`GET {{baseUrl}}/api/categories?type=1`（`{{token}}`）

预期：`code=200`，`data` 为一级分类数组，共 11 条（餐饮/交通/购物/居住/娱乐/医疗/学习/人情/育儿/宠物/其他），每条含 `children`。

### 5.2 分类树（收入）

`GET {{baseUrl}}/api/categories?type=2`

预期：`code=200`，`data` 共 7 条（工资/奖金/兼职/理财/红包/报销/其他）。

### 5.3 新建一级分类

`POST {{baseUrl}}/api/categories`（`{{token}}`）

```json
{
  "name": "数码",
  "icon": "phone",
  "color": "#3B82F6",
  "type": 1
}
```

预期：`code=200`，`data.parentId=0`（或 null）、`data.isSystem=0`。记 `data.id` → `{{catRootId}}`。

### 5.4 新建二级分类

`POST {{baseUrl}}/api/categories`

```json
{
  "parentId": {{catRootId}},
  "name": "手机",
  "icon": "mobile",
  "color": "#60A5FA",
  "type": 1
}
```

预期：`code=200`，`data.parentId={{catRootId}}`。记 `data.id` → `{{catChildId}}`。

> 子分类会继承父分类的 `type`。

负向 1（三级分类）：

```json
{ "parentId": {{catChildId}}, "name": "旗舰机", "type": 1 }
```
预期：`code=400`，`msg="分类最多两级，不能在子分类下继续创建"`。

负向 2（删除有子分类的一级分类）：

`DELETE {{baseUrl}}/api/categories/{{catRootId}}`
预期：`code=409`，`msg="请先删除子分类"`。

负向 3（删除系统分类）：

`DELETE {{baseUrl}}/api/categories/1`
预期：`code=403`，`msg="系统预置分类不允许删除"`。

### 5.5 编辑二级分类

`PUT {{baseUrl}}/api/categories/{{catChildId}}`（`{{token}}`）

```json
{
  "name": "手机数码",
  "icon": "mobile",
  "color": "#2563EB",
  "type": 1
}
```

预期：`code=200`。

### 5.6 依次删除子分类、父分类

`DELETE /api/categories/{{catChildId}}` → `code=200`

`DELETE /api/categories/{{catRootId}}` → `code=200`

> 分类接口共 4 个：GET 树、POST 新建、PUT 编辑、DELETE 删除；上面已全部覆盖。

---

## 六、账单（12 个接口）

### 6.1 账单分页（初始为空）

`GET {{baseUrl}}/api/bills?current=1&size=10`（`{{token}}`）

预期：`code=200`，`data.total=0`、`data.pages=0`、`data.records=[]`。

### 6.2 记一笔支出

`POST {{baseUrl}}/api/bills`（`{{token}}`）

```json
{
  "bookId": {{bookId}},
  "accountId": {{cashId}},
  "categoryId": 1,
  "type": 1,
  "amount": 3500,
  "currency": "CNY",
  "happenedAt": "2026-10-24 12:30:00",
  "remark": "午餐",
  "tags": "工作餐,午餐",
  "isExcluded": 0,
  "source": 1
}
```

预期：`code=200`，`data.id` 为数字，`data.categoryName="餐饮"`、`data.accountName="现金"`、`data.bookName="日常账本"`、`data.amount=3500`。

记 `data.id` → `{{billExpenseId}}`；此时现金账户余额 `-3500`。

### 6.3 记一笔收入

`POST {{baseUrl}}/api/bills`

```json
{
  "bookId": {{bookId}},
  "accountId": {{alipayId}},
  "categoryId": 43,
  "type": 2,
  "amount": 1000000,
  "happenedAt": "2026-10-24 09:00:00",
  "remark": "工资",
  "tags": "工资",
  "isExcluded": 0,
  "source": 1
}
```

预期：`code=200`，`data.categoryName="工资"`；支付宝余额 `+1000000`。

### 6.4 记一笔转账

`POST {{baseUrl}}/api/bills`

```json
{
  "bookId": {{bookId}},
  "accountId": {{alipayId}},
  "toAccountId": {{wechatId}},
  "type": 3,
  "amount": 200000,
  "happenedAt": "2026-10-24 13:00:00",
  "remark": "支付宝转微信"
}
```

预期：`code=200`，`data.type=3`、`data.toAccountName="微信"`；支付宝 `800000`、微信 `+200000`。

负向：`toAccountId` 与 `accountId` 相同 → `code=400`，`msg="转账目标账户不能与转出账户相同"`。

### 6.5 账单详情

`GET {{baseUrl}}/api/bills/{{billExpenseId}}`（`{{token}}`）

预期：`code=200`，`data.remark="午餐"`、`data.amount=3500`。

### 6.6 编辑账单

`PUT {{baseUrl}}/api/bills/{{billExpenseId}}`（`{{token}}`）

```json
{
  "bookId": {{bookId}},
  "accountId": {{cashId}},
  "categoryId": 1,
  "type": 1,
  "amount": 4000,
  "happenedAt": "2026-10-24 12:30:00",
  "remark": "午餐（改）",
  "tags": "工作餐",
  "isExcluded": 0
}
```

预期：`code=200`；详情 `amount=4000`、`remark="午餐（改）"`；现金余额 `-4000`。

### 6.7 多条件分页筛选

`GET {{baseUrl}}/api/bills?current=1&size=10&type=1&categoryId=1&startTime=2026-10-24%2000:00:00&endTime=2026-10-24%2023:59:59&keyword=%E5%8D%88%E9%A4%90&minAmount=1000&maxAmount=10000`

预期：`code=200`，`data.total=1`，`records[0].id={{billExpenseId}}`。

> 中文关键字必须 URL 编码：`午餐` = `%E5%8D%88%E9%A4%90`。

### 6.8 删除账单（进回收站）

`DELETE {{baseUrl}}/api/bills/{{billExpenseId}}`（`{{token}}`）

预期：`code=200`；现金余额回到 `0`。

### 6.9 回收站列表

`GET {{baseUrl}}/api/bills/recycle?current=1&size=10`（`{{token}}`）

预期：`code=200`，`data.total=1`，`records[0].id={{billExpenseId}}`。

### 6.10 恢复账单

`PUT {{baseUrl}}/api/bills/{{billExpenseId}}/restore`（`{{token}}`）

预期：`code=200`；回收站 `total=0`；现金余额回到 `-4000`。

### 6.11 再次删除并彻底删除

`DELETE {{baseUrl}}/api/bills/{{billExpenseId}}` → `code=200`

`DELETE {{baseUrl}}/api/bills/{{billExpenseId}}/purge` → `code=200`

再查 `GET /api/bills/{{billExpenseId}}` → `code=404`，`msg="账单不存在"`；回收站 `total=0`。

### 6.12 准备两条账单用于批量删除

`POST {{baseUrl}}/api/bills`

```json
{
  "bookId": {{bookId}},
  "accountId": {{cashId}},
  "categoryId": 3,
  "type": 1,
  "amount": 1000,
  "happenedAt": "2026-10-22 10:00:00",
  "remark": "批量测试A",
  "source": 1
}
```

记 `data.id` → `{{billBatchA}}`。

```json
{
  "bookId": {{bookId}},
  "accountId": {{wechatId}},
  "categoryId": 2,
  "type": 1,
  "amount": 2000,
  "happenedAt": "2026-10-22 11:00:00",
  "remark": "批量测试B",
  "source": 1
}
```

记 `data.id` → `{{billBatchB}}`。

### 6.13 批量删除

`DELETE {{baseUrl}}/api/bills/batch`（`{{token}}`）

```json
{ "ids": [{{billBatchA}}, {{billBatchB}}] }
```

预期：`code=200`，`data=2`；回收站 `total=2`。

### 6.14 导出账单 Excel（按条件）

`GET {{baseUrl}}/api/bills/export?startTime=2026-10-01%2000:00:00&endTime=2026-10-31%2023:59:59`（`{{token}}`）

预期：返回二进制 `.xlsx` 文件（`Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`），用 Excel 打开可见「账单」工作表与表头。

### 6.15 CSV 导入预览（multipart）

先准备文件 `alipay_test.csv`（**必须 UTF-8 无 BOM，逗号分隔**；用 Excel 另存容易变成 GBK，会解析失败）：

```csv
交易时间,交易对方,商品说明,收/支,金额
2026-10-20 12:30:00,美团外卖,午餐,支出,35.00
2026-10-21 08:10:00,公司,工资,收入,10000.00
```

`POST {{baseUrl}}/api/bills/import/preview`（`{{token}}`）
Body 选 `form-data`：`file` = 上述 CSV 文件。

预期：`code=200`

```json
{
  "total": 2,
  "duplicated": 0,
  "list": [
    {
      "happenedAt": "2026-10-20 12:30:00",
      "amount": 3500,
      "counterparty": "美团外卖",
      "remark": "午餐",
      "type": 1,
      "suggestedCategoryId": 1,
      "suggestedCategoryName": "餐饮",
      "isDuplicate": false,
      "selected": true
    },
    {
      "happenedAt": "2026-10-21 08:10:00",
      "amount": 1000000,
      "counterparty": "公司",
      "remark": "工资",
      "type": 2,
      "suggestedCategoryId": 49,
      "suggestedCategoryName": "其他",
      "isDuplicate": false,
      "selected": true
    }
  ]
}
```

> 重复导入同一文件时，`duplicated` 会变成 2、`selected=false`（MD5 指纹去重）。

### 6.16 CSV 导入确认

`POST {{baseUrl}}/api/bills/import/confirm`（`{{token}}`）

```json
{
  "bookId": {{bookId}},
  "accountId": {{cashId}},
  "items": [
    {
      "happenedAt": "2026-10-20 12:30:00",
      "amount": 3500,
      "counterparty": "美团外卖",
      "remark": "午餐-美团",
      "type": 1,
      "suggestedCategoryId": 1,
      "suggestedCategoryName": "餐饮",
      "isDuplicate": false,
      "selected": true
    },
    {
      "happenedAt": "2026-10-21 08:10:00",
      "amount": 1000000,
      "counterparty": "公司",
      "remark": "工资",
      "type": 2,
      "suggestedCategoryId": 49,
      "suggestedCategoryName": "其他",
      "isDuplicate": false,
      "selected": true
    }
  ]
}
```

预期：`code=200`，`data=2`；`GET /api/bills` 出现「午餐-美团」「工资」两条，`source=2`。

> 重复提交同样内容会因指纹复查返回 `data=0`（幂等）。
> 若分类类型与账单类型不一致，返回 `400 分类类型与账单类型不一致`。

### 6.17 账本 force 级联删除

在「临时账本」下记一笔：

`POST {{baseUrl}}/api/bills`

```json
{
  "bookId": {{bookTempId}},
  "accountId": {{cashId}},
  "categoryId": 3,
  "type": 1,
  "amount": 5000,
  "happenedAt": "2026-10-23 15:00:00",
  "remark": "临时账本购物",
  "source": 1
}
```

记 `data.id` → `{{billTempId}}`。

`DELETE {{baseUrl}}/api/books/{{bookTempId}}?force=true`（`{{token}}`）

预期：`code=200`；随后 `GET /api/bills/{{billTempId}}` → `code=404`；现金账户余额自动回算（该笔 5000 不再计入）。

### 6.18 有账单账本普通删除（负向）

`DELETE {{baseUrl}}/api/books/{{bookId}}`

预期：`code=409`，`msg` 形如「该账本下存在 N 笔账单，请确认后使用 force=true 删除」。

> 不要对 `{{bookId}}` 执行 `force=true`，否则会清空后续统计所需的账单。

---

## 七、统计报表（8 个接口）

> 以下按 6.1–6.18 执行完后的个人上下文计算，精确值如下；若中途跳过步骤，数值会变化。

### 7.1 总览

`GET {{baseUrl}}/api/stats/overview?period=2026-10`（`{{token}}`）

预期：`code=200`

```json
{
  "period": "2026-10",
  "totalIncome": 2020000,
  "totalExpense": 3500,
  "balance": 2016500,
  "billCount": 5,
  "transferCount": 1
}
```

> 收入 = 工资 1000000 + 余额校正 20000 + CSV 工资 1000000；支出 = CSV 美团 3500；账单数 5 含 1 笔转账。
> 自定义区间：`?startDate=2026-10-01&endDate=2026-10-31`（只传一个会返回 `400 自定义区间需同时传 startDate 和 endDate`）。

### 7.2 分类占比（饼图）

`GET {{baseUrl}}/api/stats/category?period=2026-10&type=1`

预期：`code=200`，`data` 中「餐饮」一条 `amount=3500`、`billCount=1`。

> `type=2` 可看收入分类占比。

### 7.3 收支趋势（折线图）

`GET {{baseUrl}}/api/stats/trend?period=2026-10&granularity=day`

预期：`code=200`，`data` 为按日数组，元素含 `date`、`income`、`expense`；其中 `2026-10-21` 的 `income=1000000`。

按月：`?period=2026&granularity=month` → 返回 `2026-10` 一条。

### 7.4 日历热力图

`GET {{baseUrl}}/api/stats/calendar?period=2026-10`

预期：`code=200`，`data` 为按日数组，`2026-10-20` 的 `expense=3500`。

### 7.5 账户余额概览

`GET {{baseUrl}}/api/stats/accounts`

预期：`code=200`，4 个账户：

| 账户 | balance |
|---|---|
| 现金 | 996500 |
| 支付宝 | 800000 |
| 微信 | 200000 |
| 招商银行储蓄卡 | 120000 |

### 7.6 账本消费对比

`GET {{baseUrl}}/api/stats/books?period=2026-10`

预期：`code=200`，只有「日常账本」一条：`expense=3500`、`income=2020000`、`billCount=5`。

### 7.7 成员消费对比（个人上下文为空）

`GET {{baseUrl}}/api/stats/members?period=2026-10`

预期：`code=200`，`data=[]`（仅家庭上下文有数据，见 10.12）。

### 7.8 排行榜

`GET {{baseUrl}}/api/stats/ranking?period=2026-10`

预期：`code=200`，`data.topCategories` 首位「餐饮」`amount=3500`；`data.maxSingleExpense.amount=3500`、`categoryName="餐饮"`。

---

## 八、快捷记账模板（5 个接口）

### 8.1 模板列表（初始为空）

`GET {{baseUrl}}/api/bill-templates`（`{{token}}`）

预期：`code=200`，`data=[]`。

### 8.2 新建模板

`POST {{baseUrl}}/api/bill-templates`

```json
{
  "name": "公司早餐",
  "type": 1,
  "amount": 1000,
  "categoryId": 12,
  "accountId": {{cashId}},
  "bookId": {{bookId}},
  "remark": "早餐",
  "sort": 1
}
```

预期：`code=200`，`data.categoryName="早餐"`、`data.accountName="现金"`、`data.bookName="日常账本"`、`data.useCount=0`。记 `data.id` → `{{templateId}}`。

> `categoryId=12` 是系统子分类「早餐」；也可用一级分类 `1`（餐饮）。

### 8.3 编辑模板

`PUT {{baseUrl}}/api/bill-templates/{{templateId}}`

```json
{
  "name": "公司早餐（改）",
  "type": 1,
  "amount": 1200,
  "categoryId": 12,
  "accountId": {{cashId}},
  "bookId": {{bookId}},
  "remark": "早餐加豆浆",
  "sort": 2
}
```

预期：`code=200`。

### 8.4 使用模板

`POST {{baseUrl}}/api/bill-templates/{{templateId}}/use`（无请求体）

预期：`code=200`，`data.useCount` 由 0 变 1（再次调用变 2）。

### 8.5 删除模板

`DELETE {{baseUrl}}/api/bill-templates/{{templateId}}`

预期：`code=200`；列表为空。

负向：用其他账号删除该模板 → `code=403/404`（模板仅本人可操作）。

---

## 九、AI 助手（2 个接口）

### 9.1 自然语言记账

`POST {{baseUrl}}/api/ai/parse-bill`（`{{token}}`）

```json
{ "text": "今天中午吃饭花了35元" }
```

预期：`code=200`，关键字段断言：

| 字段 | 期望值 |
|---|---|
| `type` | `1`（支出） |
| `amount` | `3500` |
| `categoryId` / `categoryName` | `1` / `餐饮` |
| `accountId` / `accountName` | `{{alipayId}}` / `支付宝` |
| `remark` | `今天中午吃饭花了35元` |
| `confidence` | `0.5` |
| `happenedAt` | 当天 12:00:00 的 ISO 时间串 |

> 未配置 `ZHIPU_API_KEY` 时自动走本地规则降级（`confidence=0.5`），不会 500。
> 账户选择规则：优先名称含「支付宝」的账户，否则取当前上下文第一个账户。

负向：`{"text": ""}` → `code=400`，`msg="请输入要解析的记账内容"`。

### 9.2 AI 账单分析

`GET {{baseUrl}}/api/ai/analyze?period=2026-10`（`{{token}}`）

预期：`code=200`，`data.analysis` 为非空文本（无 Key 时返回本地规则生成的概览分析），`data.cached` 首次为 `false`，重复调用为 `true`。

---

## 十、家庭组（13 个接口 + 通知场景）

> 本节需要 3 个账号：tester001（创建者）、tester002、tester003。
> tester002 / tester003 的注册方式同 1.1–1.3，邮箱分别为 `tester002@example.com`、`tester003@example.com`，密码同样 `Test@1234`。

### 10.1 注册并登录 tester002

1. `POST /api/auth/send-email-code` → `{"email":"tester002@example.com","scene":"REGISTER"}` → 记 `{{regCode2}}`
2. `POST /api/auth/register`

```json
{
  "username": "tester002",
  "password": "Test@1234",
  "nickname": "测试二号",
  "email": "tester002@example.com",
  "emailCode": "{{regCode2}}"
}
```

3. `POST /api/auth/login` → `{"account":"tester002","password":"Test@1234"}` → `{{token2}}`

### 10.2 注册并登录 tester003

同样三步，邮箱 `tester003@example.com`，用户名 `tester003`，昵称「测试三号」，`{{regCode3}}` → `{{token3}}`；注册响应 `data.id` → `{{tester3UserId}}`。

### 10.3 创建家庭

`POST {{baseUrl}}/api/families`（`{{token}}`）

```json
{
  "name": "幸福一家",
  "avatar": "/uploads/family/happy.png",
  "description": "家庭共享账本"
}
```

预期：`code=200`，`data.id` → `{{familyId}}`，`data.inviteCode` → `{{inviteCode}}`，`data.myRole=1`、`data.myRoleName="OWNER"`、`data.memberCount=1`、`data.maxMember=10`。

> 创建家庭会自动生成「家庭账本」+ 3 个家庭账户（现金/支付宝/微信）。

### 10.4 我的家庭列表

`GET {{baseUrl}}/api/families`（`{{token}}`）

预期：`code=200`，`data` 长度 1，`myRole=1`。

### 10.5 家庭详情

`GET {{baseUrl}}/api/families/{{familyId}}`（`{{token}}`）

预期：`code=200`

```json
{
  "id": {{familyId}},
  "memberCount": 1,
  "myRole": 1,
  "bookCount": 1,
  "accountCount": 3,
  "totalAsset": 0
}
```

### 10.6 编辑家庭信息

`PUT {{baseUrl}}/api/families/{{familyId}}`（`{{token}}`）

```json
{
  "name": "幸福一家（改）",
  "avatar": "/uploads/family/happy2.png",
  "description": "家庭共享账本 v2",
  "notice": "本月节约用电"
}
```

预期：`code=200`；详情中 `notice` 已更新。

### 10.7 重置邀请码

`POST {{baseUrl}}/api/families/{{familyId}}/invite/reset`（`{{token}}`，无请求体）

预期：`code=200`，`data` 为新的 8 位大写邀请码 → 覆盖 `{{inviteCode}}`。

### 10.8 tester002 通过邀请码加入

`POST {{baseUrl}}/api/families/join`（`{{token2}}`）

```json
{ "inviteCode": "{{inviteCode}}" }
```

预期：`code=200`，`data.myRole=3`、`data.memberCount=2`。

负向：

- 再提交一次 → `code=409`，`msg="您已是该家庭成员"`。
- 乱填邀请码 → `code=404`，`msg="邀请码无效"`。

### 10.9 切换 tester002 到家庭上下文

`PUT {{baseUrl}}/api/user/context`（`{{token2}}`）

```json
{ "context": 2, "familyId": {{familyId}} }
```

预期：`code=200`，`data.currentContext=2`、`data.currentFamilyId={{familyId}}`。

随后：

- `GET /api/books`（`{{token2}}`）→ 只有「家庭账本」，记 `id` → `{{familyBookId}}`
- `GET /api/accounts`（`{{token2}}`）→ 3 个家庭账户，记现金 `id` → `{{familyCashId}}`

### 10.10 修改 tester002 在家庭中的昵称

`PUT {{baseUrl}}/api/user/family-nickname?familyId={{familyId}}&nicknameInFamily=妈妈`（`{{token2}}`）

预期：`code=200`；成员列表中该成员 `nickname="妈妈"`。

### 10.11 成员列表

`GET {{baseUrl}}/api/families/{{familyId}}/members`（`{{token}}`）

预期：`code=200`，`data` 长度 2，`role=1` 在前、`role=3` 在后；tester002 的 `memberId` → `{{member2Id}}`，`totalExpense=0`。

### 10.12 修改成员角色（tester002 → ADMIN）

`PUT {{baseUrl}}/api/families/{{familyId}}/members/{{member2Id}}/role`（`{{token}}`）

```json
{ "role": 2 }
```

预期：`code=200`；成员列表里 tester002 `role=2`、`roleName="ADMIN"`。

### 10.13 tester003 加入家庭（MEMBER）

1. `POST /api/families/join`（`{{token3}}`）：`{"inviteCode":"{{inviteCode}}"}` → `code=200`
2. `GET /api/families/{{familyId}}/members`（`{{token}}`）→ tester003 的 `memberId` → `{{member3Id}}`

负向（MEMBER 编辑家庭）：

`PUT {{baseUrl}}/api/families/{{familyId}}`（`{{token3}}`）：`{"notice":"普通成员改公告"}`

预期：`code=403`，`msg="无权限操作"`。

### 10.14 提升 tester003 为 ADMIN 并移除

1. `PUT /api/families/{{familyId}}/members/{{member3Id}}/role`（`{{token}}`）：`{"role":2}` → `code=200`（正向修改角色）
2. `DELETE /api/families/{{familyId}}/members/{{member3Id}}`（`{{token}}`）→ `code=200`（正向移除成员）
3. 再用 tester003 访问 `GET /api/families/{{familyId}}`（`{{token3}}`）→ `code=403`，`msg="您不是该家庭成员"`（移除后失去访问权）

负向：

- 移除创建者：`DELETE /api/families/{{familyId}}/members/{{ownerMemberId}}`（`{{token2}}`，ADMIN）→ `code=403`，`msg="不能移除家庭创建者"`。
- 修改自己角色：`PUT .../members/{{ownerMemberId}}/role`（`{{token}}`）→ `code=400`，`msg="不能修改自己的角色"`。

> `ownerMemberId` 从 10.11 成员列表里 `role=1` 那条的 `memberId` 取。

### 10.15 家庭记账（tester002 在家庭上下文）

`POST {{baseUrl}}/api/bills`（`{{token2}}`）

```json
{
  "bookId": {{familyBookId}},
  "accountId": {{familyCashId}},
  "categoryId": 1,
  "memberId": {{member2Id}},
  "type": 1,
  "amount": 5000,
  "happenedAt": "2026-10-24 18:00:00",
  "remark": "家庭晚餐",
  "isExcluded": 0,
  "source": 1
}
```

预期：`code=200`，`data.memberName="妈妈"`、`data.creatorName="妈妈"`；家庭现金余额 `-5000`；家庭详情 `totalAsset=-5000`。

> 家庭上下文记账会给其他成员推送「家庭新账单」通知。

### 10.16 家庭操作日志

`GET {{baseUrl}}/api/families/{{familyId}}/logs?module=family&current=1&size=20`（`{{token}}`）

预期：`code=200`，`data.total >= 1`，`records[0].module="family"`（至少包含 tester002 在 10.10 修改家庭昵称产生的日志；转让后再查会增加 `transfer` 记录）。

`GET .../logs?module=bill&current=1&size=20` → `data.total >= 1`（10.15 家庭记账产生 `module="bill"` 日志）。

> **日志是异步落库的**：刚操作完立刻查可能少 1 条，等 1 秒重查即可。
> 日志里的 `familyId` 取的是「操作时的上下文」：在个人上下文执行的家庭管理操作（创建/加入/改角色）`familyId` 为空，因此不会出现在该家庭的日志列表里；家庭上下文下的记账、改昵称、转让、退出会产生带 `familyId` 的日志。
> 早期版本 `module` 参数是 Integer 导致永远查不到数据，现已改为 `String`，此处必须用 `family` / `bill`。

### 10.17 家庭统计（成员消费对比）

先切换 tester001 到家庭上下文：

`PUT {{baseUrl}}/api/user/context`（`{{token}}`）

```json
{ "context": 2, "familyId": {{familyId}} }
```

再调用：

- `GET /api/stats/members?period=2026-10`（`{{token}}`）→ `data` 长度 2，妈妈 `totalExpense=5000`，创建者 `0`
- `GET /api/stats/books?period=2026-10`（`{{token}}`）→「家庭账本」`expense=5000`、`billCount=1`
- `GET /api/stats/overview?period=2026-10`（`{{token}}`）→ `totalExpense=5000`、`totalIncome=0`、`billCount=1`

### 10.18 移除成员 / 退出家庭（负向）

- `POST /api/families/{{familyId}}/quit`（`{{token}}`，OWNER）→ `code=409`，`msg="家庭创建者请先转让或解散家庭"`
- `DELETE /api/families/{{familyId}}`（`{{token2}}`，此时 ADMIN）→ `code=403`（解散仅 OWNER）

### 10.19 转让群主

`POST {{baseUrl}}/api/families/{{familyId}}/transfer`（`{{token}}`）

```json
{ "targetMemberId": {{member2Id}} }
```

预期：`code=200`；家庭详情 `ownerId` 变为 tester002 的 userId；成员列表 tester002 `role=1`、tester001 `role=2`。

### 10.20 原创建者退出家庭

`POST {{baseUrl}}/api/families/{{familyId}}/quit`（`{{token}}`）

预期：`code=200`；成员列表只剩 tester002（`memberCount=1`）；tester001 的 `GET /api/user/profile` 显示 `currentContext=1`（自动切回个人，家庭上下文已清空）。

### 10.21 解散家庭

`DELETE {{baseUrl}}/api/families/{{familyId}}`（`{{token2}}`，OWNER）

预期：`code=200`；`GET /api/families`（`{{token2}}`）→ `data=[]`；家庭账本/账户/分类/账单全部逻辑删除。

---

## 十一、消息通知（4 个接口）

> 在 10.21 解散后用 `{{token2}}` 测试（通知不随家庭解散删除）。

### 11.1 我的通知分页

`GET {{baseUrl}}/api/notifications?current=1&size=10`（`{{token2}}`）

预期：`code=200`，`data.total >= 3`（加入成功 / 角色变更 / 成为创建者 / 新成员加入 等），`records[0].isRead=0`。记 `records[0].id` → `{{notificationId}}`。

### 11.2 未读数量

`GET {{baseUrl}}/api/notifications/unread-count`（`{{token2}}`）

预期：`code=200`，`data` 为数字（>= 3）。

### 11.3 标记单条已读

`PUT {{baseUrl}}/api/notifications/{{notificationId}}/read`（`{{token2}}`，无请求体）

预期：`code=200`；再查未读数减少 1。

负向：用 `{{token}}`（tester001）标记 tester002 的通知 → `code=403`，`msg="无权操作该通知"`。

### 11.4 全部标记已读

`PUT {{baseUrl}}/api/notifications/read-all`（`{{token2}}`，无请求体）

预期：`code=200`；`unread-count` → `0`。

---

## 十二、数据导出（2 个接口）

### 12.1 全量数据导出（JSON）

`GET {{baseUrl}}/api/export/all`（`{{token}}`，个人上下文）

预期：`code=200`，`data` 含 `generateTime`、`ownerType`、`books`、`accounts`、`categories`、`bills`、`billTemplates`。响应中**不含任何密码字段**。

### 12.2 账单明细导出（Excel）

`GET {{baseUrl}}/api/export/all/excel`（`{{token}}`）

预期：返回二进制 `.xlsx`，表头为：账单ID、类型、金额(分)、发生时间、分类ID、账户ID、账本ID、备注、标签。

---

## 十三、隐藏后台（6 个接口，仅 admin）

先登录管理员（数据库脚本内置）：

`POST {{baseUrl}}/api/auth/login`

```json
{ "account": "admin", "password": "123456" }
```

预期：`code=200`，`data.user.isAdmin=1`；`data.token` → `{{adminToken}}`。

负向（普通用户越权）：`GET /api/console/stats`（`{{token}}`）→ `code=403`，`msg="无权限"`。

### 13.1 平台数据概览

`GET {{baseUrl}}/api/console/stats`（`{{adminToken}}`）

预期：`code=200`，`data` 含 `totalUsers`、`totalFamilies`、`totalBooks`、`totalBills`、`todayNewUsers`、`todayNewBills`（均为数字）。

> 全新导入脚本后未跑本流程时参考值：`totalUsers=2`、`totalBooks=2`、`totalBills=214`、`totalFamilies=0`。

### 13.2 用户列表

`GET {{baseUrl}}/api/console/users?current=1&size=10&keyword=tester`（`{{adminToken}}`）

预期：`code=200`，`data.total=3`（tester001/002/003），字段含 `id/username/nickname/email/phone/status/isAdmin/createTime`。

### 13.3 禁用 / 启用用户

`PUT {{baseUrl}}/api/console/users/{{tester3UserId}}/status`（`{{adminToken}}`）

```json
{ "status": 0 }
```

预期：`code=200`；随后 tester003 登录 → `code=403`，`msg="账号已被禁用"`。

恢复：

```json
{ "status": 1 }
```

预期：`code=200`；tester003 再登录 → `code=200`。

负向：`{"status": 9}` → `code=400`，`msg="状态不合法"`。

### 13.4 家庭列表

`GET {{baseUrl}}/api/console/families?current=1&size=10`（`{{adminToken}}`）

预期：`code=200`；由于 10.21 已解散，`data.total=0`，`records=[]`。
（若未解散，则能看到「幸福一家」及 `memberCount`。）

### 13.5 家庭详情（仅统计）

`GET {{baseUrl}}/api/console/families/{{familyId}}`（`{{adminToken}}`）

预期：`code=200`，`data` 含 `familyId`、`memberCount`、`bookCount`、`accountCount`、`billCount`、`totalExpense`、`totalIncome`。
家庭已解散时各计数为 `0`。

### 13.6 AI 调用统计

`GET {{baseUrl}}/api/console/ai-stats?period=2026-10`（`{{adminToken}}`）

预期：`code=200`，`data` 含 `totalCalls`（>=2：9.1 解析 + 9.2 分析）、`successCalls`、`successRate`、`avgCostMs`、`totalTokens`。

> 未配置 Key 时调用走降级，`successCalls` 可能为 0，属正常。

---

## 十四、修改密码 / 忘记密码 / 登出

### 14.1 修改密码

`PUT {{baseUrl}}/api/user/password`（`{{token}}`）

```json
{
  "oldPassword": "Test@1234",
  "newPassword": "NewTest@5678"
}
```

预期：`code=200`；**改密会踢掉该用户全部会话**，旧 `{{token}}` 立即失效（`GET /api/user/profile` → `code=401`）。

负向：原密码错误 → `code=400`，`msg="原密码错误"`；新密码不含大写/特殊符号 → `code=400`（校验提示）。

### 14.2 用新密码登录

`POST {{baseUrl}}/api/auth/login`

```json
{ "account": "tester001", "password": "NewTest@5678" }
```

预期：`code=200`，`data.token` → 覆盖 `{{token}}`。

### 14.3 忘记密码：发送验证码

`POST {{baseUrl}}/api/auth/send-email-code`

```json
{
  "email": "tester001@example.com",
  "scene": "RESET_PASSWORD"
}
```

预期：`code=200`，`data.sent=true`，`data.devCode` → `{{resetCode}}`。

> **`scene` 必须是 `RESET_PASSWORD`**。注册用的是 `REGISTER`，两种场景的验证码分开存储、不能混用；用错场景重置密码会提示 `400 验证码已过期，请重新获取`。
> 验证码 **60 秒内有效且只能用一次**，请发完码立刻执行 14.4，不要中途重启应用。

### 14.4 重置密码

`POST {{baseUrl}}/api/auth/reset-password`

```json
{
  "email": "tester001@example.com",
  "code": "{{resetCode}}",
  "newPassword": "FinalTest@9012"
}
```

预期：`code=200`；所有旧 token 失效；旧密码登录 `code=400`，新密码登录 `code=200`。

负向：

- 重复使用同一验证码 → `code=400`（验证码已失效/错误）。
- 邮箱不存在 → `code=400`，`msg="邮箱或验证码错误"`。

### 14.5 登出

14.4 重置密码已踢掉全部会话，先重新登录：

`POST {{baseUrl}}/api/auth/login`

```json
{ "account": "tester001", "password": "FinalTest@9012" }
```

预期：`code=200`，`data.token` → 覆盖 `{{token}}`。

再 `POST {{baseUrl}}/api/auth/logout`（`{{token}}`）

预期：`code=200`；随后 `GET /api/user/profile` → `code=401`。

---

## 十五、全量接口清单核对（80 个）

| 分组 | 数量 | 接口 |
|---|---|---|
| 认证 | 5 | send-email-code / register / login / logout / reset-password |
| 用户 | 8 | GET·PUT profile / PUT password / POST avatar / GET families / PUT context / PUT guided / PUT family-nickname |
| 账本 | 5 | GET / POST / PUT {id} / DELETE {id} / PUT {id}/default |
| 账户 | 6 | GET / POST / PUT {id} / DELETE {id} / GET total / POST {id}/adjust |
| 分类 | 4 | GET / POST / PUT {id} / DELETE {id} |
| 账单 | 12 | GET 分页 / POST / GET {id} / PUT {id} / DELETE {id} / DELETE batch / GET export / GET recycle / PUT {id}/restore / DELETE {id}/purge / POST import/preview / POST import/confirm |
| 统计报表 | 8 | overview / category / trend / calendar / accounts / books / members / ranking |
| 快捷记账模板 | 5 | GET / POST / PUT {id} / DELETE {id} / POST {id}/use |
| 家庭组 | 13 | POST / GET / GET {id} / PUT {id} / DELETE {id} / POST {id}/invite/reset / POST join / GET {id}/members / PUT {id}/members/{mid}/role / DELETE {id}/members/{mid} / POST {id}/quit / POST {id}/transfer / GET {id}/logs |
| 消息通知 | 4 | GET / PUT {id}/read / PUT read-all / GET unread-count |
| AI 助手 | 2 | POST parse-bill / GET analyze |
| 数据导出 | 2 | GET all / GET all/excel |
| 隐藏后台 | 6 | GET stats / GET users / PUT users/{id}/status / GET families / GET families/{id} / GET ai-stats |
| **合计** | **80** | — |

---

## 附录 A：基准数据回归（admin / 123456）

> 在**刚导入 `db/life_big_ledger.sql`、未执行第十五节流程**的干净库上验证；数值为 2026-10 实测参考值。

| 校验项 | 请求 | 期望 |
|---|---|---|
| 管理员登录 | `POST /api/auth/login` `{"account":"admin","password":"123456"}` | `code=200`，`isAdmin=1` |
| 账本列表 | `GET /api/books` | 1 条「日常账本」，`billCount=214`、`expense=2797182`、`income=5250714` |
| 账户列表 | `GET /api/accounts` | 现金 80000 / 支付宝 3067161 / 微信 86371 |
| 总资产 | `GET /api/accounts/total` | `3233532` |
| 分类树 | `GET /api/categories` | 一级 18 条、全量节点 41 条 |
| 账单分页 | `GET /api/bills?current=1&size=5` | `total=214` |
| 统计总览 | `GET /api/stats/overview?period=2026-10` | `totalIncome=1241649`、`totalExpense=709601`、`balance=532048`、`billCount=54`、`transferCount=2` |
| 模板列表 | `GET /api/bill-templates` | 3 条，首条「早餐」`amount=1000`、`useCount=12` |
| 后台概览 | `GET /api/console/stats` | `totalUsers=2`、`totalBooks=2`、`totalBills=214` |
| AI 解析 | `POST /api/ai/parse-bill` `{"text":"今天中午吃饭花了35元"}` | `amount=3500`、`categoryId=1`、`accountId=2`、`confidence=0.5` |
| 家庭列表 | `GET /api/families` | `[]`（脚本无家庭数据） |

---

## 附录 B：错误码与常见问题

### B.1 业务错误码

| code | 含义 | 典型场景 |
|---|---|---|
| 200 | 成功 | — |
| 400 | 参数/业务校验失败 | 金额≤0、账本/账户不属于当前上下文、密码不合规、日期格式错误、CSV 分类类型不一致 |
| 401 | 未登录 / 登录过期 | 没带 token、改密/重置后旧 token |
| 403 | 无权限 | 普通用户访问后台、MEMBER 操作家庭共享配置、操作他人账单/通知 |
| 404 | 资源不存在 | 账单/账本/模板/家庭/成员不存在 |
| 409 | 冲突 | 用户名/邮箱已注册、账本或账户仍有账单引用、邀请码重复加入、解散前需转让 |
| 429 | 触发限流 | 验证码 5 次/60 秒、记账 60 次/60 秒、AI 解析 10 次/60 秒 |
| 500 | 系统异常 | 一般不应出现；出现时看后端日志 |

### B.2 高频踩坑

1. **看 body 的 `code`，不要看 HTTP 状态码**：业务异常同样返回 HTTP 200。
2. **金额永远是分**：`35.00 元` 要传 `3500`。
3. **分页参数是 `current/size`**，不是 `pageNum/pageSize`。
4. **登录响应是嵌套的**：token 在 `data.token`，不是根节点。
5. **中文查询参数要 URL 编码**（如 `keyword=%E5%8D%88%E9%A4%90`）。
6. **CSV 必须是 UTF-8**：Excel 默认另存为 GBK 会导致解析为空。
7. **`?force=true` 的 URL 不要写变量插值错误**：PowerShell 里 `"/api/books/$id?force=true"` 会把 `?force` 当变量名，必须写 `"${id}?force=true"`。
8. **家庭日志 `module` 传字符串**：`family` / `bill`，传数字永远查不到。
9. **改密/重置会踢会话**：保存新 token，旧 token 立即 401。
10. **Knife4j 4.x 的鉴权头异常**：团队联调优先用 Apifox，并在目录级统一加 `Authorization: Bearer {token}`。
11. **登录限流 10 次/60 秒**：本文档含多账号登录与禁用/启用复测，若 60 秒内整篇重跑两次以上可能触发 `429`，间隔一分钟再执行即可。
12. **家庭操作日志的 `familyId`**：个人上下文执行的家庭管理操作不会挂到家庭日志；查不到时先确认操作时是否已切到家庭上下文。

### B.3 一键清理测试数据

需要回到干净基准时，重新执行 0.2 的导入命令即可；应用无需重启（连接池会自动感知）。

---

## 附录 C：导入 OpenAPI 到 Apifox（可选）

1. 打开 Apifox → 新建项目 → 接口管理 → 导入 → OpenAPI/Swagger → URL 导入。
2. 填 `http://localhost:8090/v3/api-docs`。
3. 导入位置选「接口」，可得到 13 个中文分组、80 个接口。
4. 在「环境」里配置 `baseUrl` 与 `token` 变量，目录级请求头加 `Authorization: Bearer {{token}}`。

> 后端新增接口后重复导入一次（选择「覆盖同名接口」）即可增量更新。
