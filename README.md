# 生活大作账 · life-big-ledger

> 面向个人与家庭的 PC 端记账系统后端服务，基于 Spring Boot 3 + MySQL 8 实现。
> 支持多账本、多账户、收支转账、统计报表、CSV 导入导出、家庭共享账本、AI 财务分析与后台管理。

---

## 功能特性

| 模块 | 能力 |
|---|---|
| 账号认证 | 注册、登录、登出、邮箱验证码、忘记密码重置、Sa-Token 会话 |
| 个人资料 | 昵称/头像/邮箱/手机号/性别维护、修改密码、新手引导标记 |
| 多账本 | 新建、编辑、删除（可级联）、设为默认，个人与家庭账本隔离 |
| 账户管理 | 现金/储蓄卡/信用卡/支付账户，初始余额、计入总资产、余额校正 |
| 分类体系 | 两级分类、收支类型区分、系统预置分类 + 自定义分类 |
| 账单记账 | 支出/收入/转账、多条件分页筛选、回收站恢复/彻底删除、批量删除 |
| 数据导入导出 | 支付宝/微信 CSV 预览与确认导入（指纹去重）、Excel 导出 |
| 统计报表 | 总览、收支趋势、分类占比、Top 排行、日历热力、账本/账户/成员对比 |
| 快捷模板 | 记账模板 CRUD 与一键记账 |
| 家庭共享 | 创建家庭、邀请码加入、角色（OWNER/ADMIN/MEMBER）、转让、退出、解散、家庭日志 |
| 消息通知 | 家庭事件通知、未读数量、单条/全部已读 |
| AI 助手 | 月度账单分析、自然语言记账（智谱 GLM，未配置 Key 时本地降级） |
| 后台管理 | 平台概览、用户管理（禁用/启用）、家庭列表与详情、AI 调用统计 |

接口总数 **80 个，分 13 个模块**，全部在 OpenAPI 文档中登记。

---

## 技术栈

| 分类 | 选型 |
|---|---|
| 语言/运行时 | JDK 17 |
| 框架 | Spring Boot 3.2.5 |
| 认证 | Sa-Token 1.37 |
| ORM | MyBatis-Plus 3.5.7 |
| 数据库 | MySQL 8.0 |
| 缓存 | Caffeine（本地），预留 CacheService 抽象 |
| 文档 | SpringDoc + Knife4j 4.x |
| 导出/导入 | EasyExcel（Excel 导出）、手写 CSV 解析（支付宝/微信账单） |
| AI | OkHttp + 智谱 GLM-4-Flash |
| 邮件 | JavaMailSender（可切换 log 演示模式） |

---

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- 端口 `8090` 未被占用

### 2. 初始化数据库

```bash
mysql -uroot -p < db/life_big_ledger.sql
```

脚本已包含 `CREATE DATABASE` / `USE` 语句和演示数据，无需手动建库。

### 3. 配置环境变量

数据库默认 `root/root`，如不同则通过环境变量覆盖：

| 变量 | 说明 | 默认值 |
|---|---|---|
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | `root` |
| `MAIL_USERNAME` | SMTP 发件邮箱 | 空 |
| `MAIL_AUTH_CODE` | 邮箱授权码（非登录密码） | 空 |
| `ZHIPU_API_KEY` | 智谱 AI Key，不配则走本地降级 | 空 |
| `SPRINGDOC_ENABLED` | 是否开启接口文档 | `true` |
| `KNIFE4J_PRODUCTION` | 文档生产模式（关闭调试） | `false` |

### 4. 启动

```bash
mvn spring-boot:run
```

或直接运行 `com.life.ledger.LedgerApplication`。

- 服务地址：`http://localhost:8090`
- 接口文档：`http://localhost:8090/doc.html`
- OpenAPI JSON：`http://localhost:8090/v3/api-docs`

### 5. 演示账号

| 角色 | 账号 | 密码 |
|---|---|---|
| 管理员 | `admin` | `123456` |
| 普通用户 | `user` | `@#Abc123` |

---

## 邮箱验证码

`app.email.mode` 支持两种模式：

- `log`（推荐联调）：不发送真实邮件，验证码打印到日志；配合 `--app.email.dev-return-code=true` 可直接在响应 `devCode` 中拿到验证码。
- `smtp`：调用真实邮箱发送，需要配置 `MAIL_USERNAME` / `MAIL_AUTH_CODE`，否则会返回 500。

其他规则：有效期 60 秒、一次性使用、按场景（`REGISTER` / `RESET_PASSWORD`）隔离、同一验证码连续错 5 次作废。

```bash
# 本地免邮箱联调启动示例
mvn spring-boot:run -Dspring-boot.run.arguments="--app.email.mode=log --app.email.dev-return-code=true"
```

---

## 接口文档

| 文档 | 位置 | 用途 |
|---|---|---|
| Knife4j 在线文档 | `http://localhost:8090/doc.html` | 在线调试 |
| 前端交付摘要 | `前端API交付摘要.md` | 前端对接速查（约定、枚举、接口清单、示例） |
| 完整测试手册 | `POSTMAN.md` | 按依赖顺序的流程式接口测试，含断言 |

---

## 项目结构

```
life-big-ledger
├── db/                              # 数据库脚本（建库 + 表结构 + 演示数据）
├── src/main/java/com/life/ledger
│   ├── annotation/                  # @OpLog @RateLimit @FamilyPermission @AdminOnly
│   ├── aspect/                      # 对应切面实现
│   ├── cache/                       # CacheService 抽象 + Caffeine 实现
│   ├── common/                      # 统一响应、异常、分页、上下文、枚举
│   ├── config/                      # MyBatis-Plus、WebMvc、Knife4j、序列化配置
│   ├── controller/                  # 13 个 REST 控制器
│   ├── dto / vo / entity / mapper   # 请求、响应、实体、数据访问
│   ├── email/                       # 验证码服务与 log/smtp 双发送器
│   ├── interceptor/                 # 管理员拦截器
│   ├── job/                         # 定时任务
│   └── service/                     # 业务接口与实现
└── src/main/resources/application.yml
```

---

## 设计约定

- **金额统一以「分」为单位**，数据库 `BIGINT` 存储，避免浮点误差，前端展示时除以 100。
- **统一响应**：所有业务接口 HTTP 状态码恒为 200，结果以响应体 `code` 为准（400/401/403/404/409/429/500）；仅导出接口返回二进制流。
- **多租户**：通过 `owner_type`（0 系统 / 1 个人 / 2 家庭）与 `family_id` 区分数据归属，个人/家庭上下文切换后所有查询自动隔离。
- **逻辑删除 + 审计字段**：全表使用 `deleted` 逻辑删除，`create_time` / `update_time` 自动填充，不建物理外键。
- **权限控制**：Sa-Token 登录态 + `@AdminOnly` 管理员校验 + `@FamilyPermission` 家庭角色校验（OWNER > ADMIN > MEMBER）。
- **安全**：密码 BCrypt 加盐、验证码防暴力枚举、手机号/邮箱脱敏输出、头像格式白名单、接口限流。

---

## 常见问题

| 现象 | 原因与处理 |
|---|---|
| 发验证码 500 | `smtp` 模式未配置 `MAIL_USERNAME` / `MAIL_AUTH_CODE`，改用 `log` 模式即可 |
| 验证码提示已过期 | 有效期 60 秒；按场景隔离；应用重启会清空缓存 |
| 导出接口在 Apifox 里显示「失败」 | 返回的是 xlsx 二进制，删掉 JSON 断言即可 |
| Knife4j 鉴权失败 | 4.x 鉴权头生成异常，建议用 Apifox 统一加 `Authorization: Bearer {token}` |
| 返回 429 | 触发限流，稍后重试 |
| CSV 导入为空 | 文件必须是 UTF-8 编码 |

---

## 声明

本项目用于学习与课程实践，请勿直接用于生产环境。
