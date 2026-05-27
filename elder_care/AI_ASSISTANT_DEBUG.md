# AI助手功能调试指南

## 🔍 常见错误及解决方案

### 错误1: "业务错误" - elderId为空

**现象**:
```
request.js:100 业务错误: Object
ai-assistant.vue:386 AI问答失败: Object
```

**原因**: 
- `currentElder.id` 为 `undefined` 或 `null`
- 老人列表加载失败
- 数据格式解析错误

**解决步骤**:

#### 1. 检查控制台日志

打开浏览器开发者工具（F12），查看Console标签，应该看到以下日志：

```javascript
=== 加载老人用户信息 ===
用户信息响应: {...}
解析后的数据: {...}
elderId: 1
user: {...}
✅ 老人用户加载成功: {id: 1, name: "张三", ...}
```

如果没有这些日志，说明老人列表加载失败。

#### 2. 检查登录状态

在Console中执行：
```javascript
console.log('Token:', uni.getStorageSync('token'))
console.log('UserInfo:', uni.getStorageSync('userInfo'))
```

**预期结果**:
- Token应该存在且不为空
- UserInfo应该包含 `userType` 字段

**如果Token为空**:
- 重新登录
- 检查登录接口是否正常返回Token

#### 3. 检查用户类型

```javascript
const userInfo = uni.getStorageSync('userInfo')
console.log('userType:', userInfo.userType) // 0-老人, 1-家属
```

#### 4. 检查API响应

查看Network标签，找到 `/auth/profile` 请求：

**老人用户预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "user": {
      "id": 1,
      "name": "张三",
      "age": 75,
      "sex": "男",
      "phone": "13800138000",
      "userType": 0
    },
    "elderId": 1,
    "emergencyContact": "13900000000"
  }
}
```

**家属用户预期响应** (`/record/elders`):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "张三",
      "age": 75,
      "sex": "男",
      "phone": "13800138000",
      "relation": "父子",
      "pendingTasks": 3
    }
  ]
}
```

---

### 错误2: 403 Forbidden

**现象**:
```
❌ 403 Forbidden Response: {...}
```

**原因**:
- Token无效或过期
- 未登录访问需要认证的接口

**解决**:
1. 清除缓存：`uni.removeStorageSync('token')`
2. 重新登录
3. 检查后端JWT配置

---

### 错误3: AI服务调用失败

**现象**:
```
AI 调用失败 (尝试 1/3): ...
```

**原因**:
- AI API密钥未配置
- 网络连接问题
- AI服务超时

**解决**:
1. 检查 `application.yml` 中的AI配置
2. 确认阿里云百炼API Key已配置
3. 检查网络连接

---

## 🛠️ 调试技巧

### 1. 查看详细请求日志

在 `request.js` 中已有详细日志：
```javascript
=== Request Debug ===
URL: http://localhost:8080/ai-assistant/medicine/question?elderId=1
Token: eyJhbGciOiJIUzI1NiJ9...
Method: POST
Params: {elderId: 1}
Authorization: Bearer eyJhbG...
```

### 2. 手动测试API

使用Postman或curl测试：

```bash
# 测试用户信息接口
curl -X GET http://localhost:8080/auth/profile \
  -H "Authorization: Bearer YOUR_TOKEN"

# 测试AI问答接口
curl -X POST "http://localhost:8080/ai-assistant/medicine/question?elderId=1" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"question": "阿司匹林什么时候吃？"}'
```

### 3. 检查后端日志

查看后端控制台输出：
```
处理老人 ID: 1 的用药问题咨询
成功生成老人 ID: 1 的用药咨询回答
```

如果有错误，会显示：
```
用药问题咨询参数无效: elderId=null, userInput=...
```

---

## 📋 检查清单

### 前端检查
- [ ] Token已保存且有效
- [ ] UserInfo包含userType字段
- [ ] 老人列表加载成功
- [ ] currentElder有id字段
- [ ] API路径正确
- [ ] 请求头包含Authorization

### 后端检查
- [ ] 服务正常运行（端口8080）
- [ ] 数据库连接正常
- [ ] JWT配置正确
- [ ] AI API密钥已配置
- [ ] Elder表有对应记录
- [ ] Family表有绑定记录（家属用户）

### 数据库检查
```sql
-- 检查老人用户
SELECT * FROM user WHERE phone = '13800138000';
SELECT * FROM elder WHERE user_id = 1;

-- 检查家属绑定
SELECT * FROM family WHERE family_user_id = 2;
```

---

## 🔧 快速修复步骤

### 如果elderId为空：

1. **清除缓存并重新登录**
```javascript
// 在Console中执行
uni.removeStorageSync('token')
uni.removeStorageSync('userInfo')
// 然后重新登录
```

2. **检查登录响应**
```javascript
// 登录成功后检查
console.log(uni.getStorageSync('userInfo'))
// 应该包含 userType 和 elderId
```

3. **手动设置测试数据**（仅用于调试）
```javascript
// 在 ai-assistant.vue 的 loadElderList 方法中
this.elderList = [{
  id: 1,
  name: '测试老人',
  displayName: '测试老人（本人）'
}]
this.currentElder = this.elderList[0]
```

---

## 📊 数据流验证

### 完整的请求流程：

```
1. 页面加载
   ↓
2. onLoad() → loadElderList()
   ↓
3. 判断 userType
   ├─ userType=0 → loadElderForSelf()
   │                ↓
   │            GET /auth/profile
   │                ↓
   │            提取 elderId 和 user信息
   │                ↓
   │            构建 elderList
   │
   └─ userType=1 → loadBoundElders()
                    ↓
                GET /record/elders
                    ↓
                获取绑定老人列表
                    ↓
                构建 elderList
   ↓
4. 用户输入问题
   ↓
5. 点击发送
   ↓
6. 验证 currentElder.id 存在
   ↓
7. POST /ai-assistant/medicine/question?elderId=X
   ↓
8. 后端处理并返回AI回答
   ↓
9. 前端显示回答
```

---

## 🎯 关键代码位置

### 前端
- **API调用**: `elder_care/api/ai-assistant.js`
- **页面逻辑**: `elder_care/pages/medicine/ai-assistant.vue`
- **请求封装**: `elder_care/utils/request.js`

### 后端
- **控制器**: `AIAssistantController.java`
- **服务层**: `AIAssistantServiceImpl.java`
- **上下文构建**: `MedicineContextBuilder.java`
- **AI提供者**: `MedicineAIProvider.java`

---

## 💡 常见问题FAQ

**Q: 为什么res.data是undefined？**  
A: `request.js` 已经提取了 `responseData.data`，所以直接使用 `res` 即可，不需要再访问 `res.data`。

**Q: 如何知道当前是老人用户还是家属用户？**  
A: 查看 `uni.getStorageSync('userInfo').userType`，0表示老人，1表示家属。

**Q: AI响应很慢怎么办？**  
A: 正常响应时间为5-15秒，后端有3次重试机制。如果超过30秒，检查AI API配置。

**Q: 如何测试WebSocket推送？**  
A: 目前WebSocket主要用于接收AI提醒消息，可以在发起提醒后查看控制台是否有消息到达。

---

## 📞 需要帮助？

如果以上步骤都无法解决问题，请提供：
1. 浏览器Console的完整日志
2. Network标签中的请求和响应详情
3. 后端控制台的错误日志
4. 数据库中相关表的记录

---

**最后更新**: 2026-04-27  
**版本**: v1.0
