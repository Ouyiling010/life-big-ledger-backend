# 生活大作账 · 前端接口交付摘要

> 适用版本：后端 `life-big-ledger`（Spring Boot 3.2.5 / JDK 17）
> 完整逐接口测试步骤见同目录 `POSTMAN.md`，本摘要用于快速对接。
> 接口总数：**13 个模块 / 80 个接口**，均已在 OpenAPI 文档中登记。

---

## 1. 基础信息

| 项目 | 值 |
|---|---|
| 本地地址 | `http://localhost:8090` |
| 接口前缀 | 全部以 `/api` 开头 |
| Swagger JSON | `GET /v3/api-docs` |
| Knife4j 页面 | `http://localhost:8090/doc.html` |
| 鉴权方式 | Sa-Token，请求头 `Authorization: Bearer {token}` |
| 演示账号 | 管理员 `admin / 123456`；普通用户 `user / @#Abc123` |
| 跨域 | 已开启 CORS，允许 `*`（开发期） |

---

## 2. 统一约定（先看这一节）

### 2.1 响应结构

所有业务接口 **HTTP 状态码恒为 200**，真实结果看响应体 `code`：

```json
{ "code": 200, "msg": "success", "data": { } }
```

- `data` 为 `null` 时**不输出该字段**，前端判空请用 `resp.data == null`。
- 唯一例外：导出接口返回二进制流（见 6.1）。

### 2.2 业务码

| code | 含义 | 前端处理建议 |
|---|---|---|
| 200 | 成功 | 正常渲染 |
| 400 | 参数错误 / 业务校验失败 | toast `msg` |
| 401 | 未登录或 token 失效 | 清 token 跳登录页 |
| 403 | 无权限 | toast `msg` |
| 404 | 资源不存在 | toast / 刷新列表 |
| 409 | 冲突（重复加入、重名等） | toast `msg` |
| 429 | 触发限流 | 提示稍后重试 |
| 500 | 服务端异常 | 通用错误提示 |

### 2.3 分页结构

列表分页统一返回：

```json
{ "total": 100, "current": 1, "size": 10, "records": [] }
```

查询参数统一是 `current` / `size`（不是 pageNum / pageSize）。

### 2.4 金额与时间

- **金额单位是「分」**，`Long` 类型。前端展示需 `÷ 100` 并保留两位小数；提交时 `× 100`。
- 响应时间统一为 `yyyy-MM-dd HH:mm:ss`。
- 请求中的记账时间 `happenedAt` 兼容 4 种写法：
  - `2026-10-24 12:00:00`
  - `2026-10-24T12:00:00`
  - `2026-10-24T02:15:24.846Z`（带时区会自动换算到东八区）
  - `2026-10-24`（当天 00:00:00）

### 2.5 枚举字典

| 字段 | 取值 |
|---|---|
| `gender` | 0 未知 / 1 男 / 2 女 |
| `context` | 1 个人 / 2 家庭 |
| `type`（账单/分类） | 1 支出 / 2 收入 / 3 转账 |
| `role`（家庭成员） | 1 OWNER / 2 ADMIN / 3 MEMBER |
| `ownerType`（账本/账户/分类/账单） | 0 系统 / 1 个人 / 2 家庭 |
| `source`（账单来源） | 1 手动 / 2 CSV 导入 / 3 模板 / 4 AI |
| `isExcluded` | 0 计入统计 / 1 不计入 |
| `isRead` | 0 未读 / 1 已读 |
| `status`（用户） | 0 禁用 / 1 启用 |

### 2.6 路径参数来源

`{id}` 一律取上一步接口返回的 `data.id`；家庭成员用 `data.memberId`，不要用 `data.id`。

---

## 3. 接口清单

### 3.1 认证（5）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/auth/send-email-code` | 发送邮箱验证码（注册 / 忘记密码） |
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录（`account` 支持用户名/邮箱/手机号） |
| POST | `/api/auth/logout` | 登出 |
| POST | `/api/auth/reset-password` | 忘记密码 → 重置 |

### 3.2 用户（8）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/user/profile` | 获取个人资料 |
| PUT | `/api/user/profile` | 修改个人资料（昵称/头像/邮箱/手机号/性别） |
| POST | `/api/user/avatar` | 上传头像（`multipart/form-data`，字段名 `file`） |
| PUT | `/api/user/password` | 修改密码（改后旧 token 全部失效） |
| PUT | `/api/user/context` | 切换个人 / 家庭上下文 |
| GET | `/api/user/families` | 我加入的家庭列表（切换器用） |
| PUT | `/api/user/family-nickname` | 修改自己在某家庭的昵称 |
| PUT | `/api/user/guided` | 标记已完成新手引导 |

### 3.3 账本（5）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/books` | 当前上下文账本列表 |
| POST | `/api/books` | 新建账本 |
| PUT | `/api/books/{id}` | 编辑账本 |
| DELETE | `/api/books/{id}` | 删除账本（`?force=true` 级联删除） |
| PUT | `/api/books/{id}/default` | 设为默认账本 |

### 3.4 账户（6）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/accounts` | 账户列表 |
| GET | `/api/accounts/total` | 总资产（只统计计入总资产的账户） |
| POST | `/api/accounts` | 新建账户 |
| PUT | `/api/accounts/{id}` | 编辑账户 |
| DELETE | `/api/accounts/{id}` | 删除账户 |
| POST | `/api/accounts/{id}/adjust` | 余额校正（`targetBalance` 分） |

### 3.5 分类（4）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/categories` | 分类列表（两级结构，`type` 可筛选） |
| POST | `/api/categories` | 新建分类（`parentId` 为空即一级） |
| PUT | `/api/categories/{id}` | 编辑分类 |
| DELETE | `/api/categories/{id}` | 删除分类 |

### 3.6 账单（12）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/bills` | 账单分页 + 多条件筛选 |
| GET | `/api/bills/{id}` | 账单详情（跨用户返回 403） |
| POST | `/api/bills` | 记一笔（支出 / 收入 / 转账） |
| PUT | `/api/bills/{id}` | 编辑账单 |
| DELETE | `/api/bills/{id}` | 删除 → 进回收站 |
| GET | `/api/bills/recycle` | 回收站列表 |
| PUT | `/api/bills/{id}/restore` | 从回收站恢复 |
| DELETE | `/api/bills/{id}/purge` | 彻底删除 |
| DELETE | `/api/bills/batch` | 批量删除（body: `{"ids":[...]}`） |
| GET | `/api/bills/export` | 导出账单 Excel（二进制） |
| POST | `/api/bills/import/preview` | CSV 导入预览（支付宝 / 微信） |
| POST | `/api/bills/import/confirm` | CSV 导入确认入库 |

筛选参数：`current/size/startTime/endTime/type/categoryId/accountId/bookId/creatorId/memberId/minAmount/maxAmount/keyword/tag`。

### 3.7 统计（8）

| 方法 | 路径 | 说明 | 图表建议 |
|---|---|---|---|
| GET | `/api/stats/overview` | 总收入 / 总支出 / 结余 / 账单数 / 日均 | 卡片 |
| GET | `/api/stats/trend` | 收支趋势 | 折线图 |
| GET | `/api/stats/category` | 分类占比 | 饼图 |
| GET | `/api/stats/ranking` | Top5 分类 + 最大单笔 | 排行列表 |
| GET | `/api/stats/calendar` | 日历热力 | 日历图 |
| GET | `/api/stats/books` | 账本消费对比 | 柱状图 |
| GET | `/api/stats/accounts` | 账户余额概览 | 卡片 / 柱状 |
| GET | `/api/stats/members` | 成员消费对比（家庭上下文） | 柱状 / 排行 |

> 统计聚合全部由后端完成，前端只负责渲染。
> 时间参数：`period=yyyy-MM`（月度），或 `startDate=yyyy-MM-dd&endDate=yyyy-MM-dd`（自定义区间）。
> 额外参数：`stats/category` 支持 `type`（1 支出 / 2 收入），`stats/trend` 支持 `granularity`（按日 / 按月），`stats/accounts` 无需时间参数。

### 3.8 快捷记账模板（5）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/bill-templates` | 模板列表 |
| POST | `/api/bill-templates` | 新建模板 |
| PUT | `/api/bill-templates/{id}` | 编辑模板 |
| DELETE | `/api/bill-templates/{id}` | 删除模板 |
| POST | `/api/bill-templates/{id}/use` | 使用模板一键记账 |

### 3.9 AI 助手（2）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/ai/analyze` | AI 账单分析（月度建议） |
| POST | `/api/ai/parse-bill` | 自然语言记账 → 返回账单草稿 |

> 未配置 `ZHIPU_API_KEY` 时返回本地降级结果（`code=200`，不会 500），前端无需特判。

### 3.10 家庭组（13）

| 方法 | 路径 | 说明 | 最低角色 |
|---|---|---|---|
| GET | `/api/families` | 我的家庭列表 | 成员 |
| POST | `/api/families` | 创建家庭（自动建 1 账本 + 3 账户） | 登录 |
| GET | `/api/families/{id}` | 家庭详情（含成员 + 数据概览） | 成员 |
| PUT | `/api/families/{id}` | 编辑家庭信息 | ADMIN |
| DELETE | `/api/families/{id}` | 解散家庭 | OWNER |
| POST | `/api/families/{id}/invite/reset` | 重置邀请码 | ADMIN |
| POST | `/api/families/join` | 邀请码加入 | 登录 |
| GET | `/api/families/{id}/members` | 成员列表 | 成员 |
| PUT | `/api/families/{id}/members/{mid}/role` | 修改成员角色 | OWNER |
| DELETE | `/api/families/{id}/members/{mid}` | 移除成员 | ADMIN |
| POST | `/api/families/{id}/quit` | 退出家庭 | 成员 |
| POST | `/api/families/{id}/transfer` | 转让群主 | OWNER |
| GET | `/api/families/{id}/logs` | 家庭操作日志（`module=family/bill`） | ADMIN |

### 3.11 消息通知（4）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/notifications` | 通知分页（倒序） |
| GET | `/api/notifications/unread-count` | 未读数量（角标） |
| PUT | `/api/notifications/{id}/read` | 单条已读 |
| PUT | `/api/notifications/read-all` | 全部已读 |

### 3.12 数据导出（2）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/export/all` | 全量数据导出（JSON，含账本/账户/分类/账单/模板） |
| GET | `/api/export/all/excel` | 账单明细导出（xlsx 二进制） |

### 3.13 后台管理（6，需管理员 token）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/console/stats` | 平台数据概览 |
| GET | `/api/console/users` | 用户列表（支持 `keyword`） |
| PUT | `/api/console/users/{id}/status` | 禁用 / 启用（body `{"status":0|1}`） |
| GET | `/api/console/families` | 家庭列表 |
| GET | `/api/console/families/{id}` | 家庭详情（含统计） |
| GET | `/api/console/ai-stats` | AI 调用统计 |

> 禁止禁用 `id=1` 的超级管理员，否则后台无法登录。

---

## 4. 前端接入顺序

```
① 发验证码(send-email-code) → 注册(register) → 登录(login) 拿 token
② 首屏：profile + books + accounts + categories + notifications/unread-count
③ 记账：bills(POST) → 列表 bills(GET) → 统计 stats/overview|trend|category
④ 账本/账户/分类：对应 CRUD；账户余额变化后用 accounts/total 刷新总资产
⑤ 家庭：families(POST/GET) → 邀请码 join → PUT user/context 切到家庭 → 家庭账单/统计
⑥ 个人中心：profile / avatar / password / context 切换
⑦ 后台：console/* （仅管理员入口可见）
```

**上下文规则**：个人模式看个人数据；切到家庭模式后，账本、账户、分类、账单、统计全部自动按家庭维度返回，前端不需要额外传 `familyId`（切换接口会持久化）。

---

## 5. 核心请求体示例

```jsonc
// 发送验证码
{ "email": "your@qq.com", "scene": "REGISTER" }   // scene: REGISTER | RESET_PASSWORD

// 注册
{ "username": "tester001", "password": "Abc@1234", "nickname": "小明",
  "email": "your@qq.com", "emailCode": "123456", "phone": "13800138000" }

// 登录
{ "account": "tester001", "password": "Abc@1234" }

// 记一笔（支出）
{ "bookId": 1, "accountId": 1, "categoryId": 1, "type": 1, "amount": 3500,
  "happenedAt": "2026-10-24 12:00:00", "remark": "午餐", "isExcluded": 0, "source": 1 }

// 转账（type=3 时用 toAccountId，不需要 categoryId）
{ "bookId": 1, "accountId": 1, "toAccountId": 2, "type": 3, "amount": 10000 }

// 新建账户
{ "name": "招商银行", "type": 2, "initBalance": 100000, "icon": "bank",
  "includeInTotal": 1, "remark": "" }

// 切换家庭上下文
{ "context": 2, "familyId": 3 }
```

密码规则：8–32 位，必须同时包含大写、小写、特殊符号，不能有空格。

---

## 6. 特殊说明

### 6.1 导出接口返回二进制
`/api/bills/export`、`/api/export/all/excel` 成功时直接返回 xlsx 流（`Content-Type: ...spreadsheetml.sheet`），前端用 `responseType: 'blob'` 接收并触发下载；失败才返回 JSON。Apifox 里显示「失败」属正常，删除 JSON 断言即可。

### 6.2 CSV 导入是两步
先 `import/preview` 拿到预览与去重结果 → 用户确认后 `import/confirm` 入库。CSV 必须 UTF-8（Excel 另存为 GBK 会解析为空）。

### 6.3 头像上传
`multipart/form-data`，字段名 `file`，仅允许 jpg/jpeg/png/gif/webp，单文件 ≤ 20MB；返回的 URL 可直接用于 `<img src>`。也支持直接填网络图片 URL。

### 6.4 验证码
- 有效期 **60 秒**，只能使用一次，校验通过即删除。
- 按场景隔离：注册用 `REGISTER`，忘记密码用 `RESET_PASSWORD`，不可混用。
- 同一验证码连续输错 5 次作废。
- 应用重启（内存缓存）会清空，需重新获取。

### 6.5 限流
登录、发验证码、加入家庭等接口有频率限制，超限返回 `429`，提示用户稍后重试。

### 6.6 家庭权限
编辑/重置邀请码/移除成员需 ADMIN，解散/转让/改角色需 OWNER，普通成员操作会返回 `403 权限不足`。

### 6.7 联调建议
- 用 Apifox 在目录级统一加 `Authorization: Bearer {token}`。
- Knife4j 4.x 存在鉴权头生成异常（把安全方案名当请求头），优先用 Apifox。
- 所有接口以 body 的 `code` 为准，别被 HTTP 200 误导。

---

## 7. 对接检查清单

- [ ] 处理好 401 自动跳登录
- [ ] 金额展示/提交统一分↔元转换
- [ ] 分页用 `current/size`
- [ ] 登录后先拉 `profile` 判断 `hasGuided` 是否走引导
- [ ] 通知未读数用于角标，进入通知页后调用 `read-all`
- [ ] 导出用 blob 下载
- [ ] 家庭模式切换后重新拉账本/账户/分类/统计
- [ ] 改密成功后强制重新登录
