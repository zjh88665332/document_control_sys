# 在线文件管理系统 - 前端

Vue 3 + Vite + Element Plus 用户端界面。

## 启动

```bash
# 1. 确保后端已启动（端口 8080）
cd ..
mvn spring-boot:run

# 2. 安装依赖并启动前端
cd frontend
npm install
npm run dev
```

浏览器访问：http://localhost:5173

## 功能模块

| 页面 | 路由 | 说明 |
|------|------|------|
| 登录 | `/login` | 用户名密码登录 |
| 个人信息 | `/profile` | 查看/修改资料、修改密码 |
| 公告管理 | `/notice` | 公告列表 |
| 系统反馈 | `/feedback` | 提交反馈、查看回复 |
| 文件管理 | `/file` | 上传、列表、下载、删除 |
| 好友管理 | `/friend` | 搜索、申请、好友列表 |
| 分享管理 | `/share` | 分享文件、收/发列表 |
| 文件统计 | `/statistics` | 文件类型统计 |

## 技术说明

- API 通过 Vite 代理转发到 `http://localhost:8080/api`
- Token 存储在 `localStorage`，请求头自动携带 `Authorization: Bearer xxx`
- 主题色 `#009688`，布局参考经典后台管理风格
