# elderId参数缺失问题修复

## 🐛 问题描述

**错误信息**:
```
MissingServletRequestParameterException: Required request parameter 'elderId' for method parameter type Integer is not present
```

**现象**:
- 前端调用AI问答接口时报错
- 后端无法接收到 `elderId` 参数
- 返回400业务错误

---

## 🔍 问题分析

### 后端接口定义

```java
@PostMapping("/medicine/question")
public Result<String> askMedicineQuestion(
        @RequestParam Integer elderId,      // ← 期望从URL查询参数获取
        @RequestBody Map<String, String> request) {  // ← 从请求体获取
    String question = request.get("question");
    return aiAssistantService.processMedicineQuestion(elderId, question);
}
```

**关键点**: 
- `@RequestParam` → 需要从 URL 查询参数获取（如 `?elderId=1`）
- `@RequestBody` → 从请求体 JSON 中获取

### 前端调用方式

```javascript
// api/ai-assistant.js
export function askMedicineQuestion(elderId, question) {
  return request({
    url: '/ai-assistant/medicine/question',
    method: 'POST',
    params: { elderId },  // ← 这里定义了params
    data: { question }     // ← 这里定义了data
  })
}
```

### 问题根源

**`request.js` 的bug**: 

```javascript
// ❌ 原来的代码 - 只处理GET请求的params
if (options.method === 'GET' && options.params) {
  // 拼接params到URL
}
```

**结果**: 
- POST请求的 `params` 没有被拼接到URL上
- 后端无法通过 `@RequestParam` 获取 `elderId`
- 抛出 `MissingServletRequestParameterException`

---

## ✅ 修复方案

### 修改 `request.js`

**文件**: `elder_care/utils/request.js`

**修改前**:
```javascript
// GET请求需要把params拼接到URL上
if (options.method === 'GET' && options.params) {
  const queryString = Object.keys(options.params)
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(options.params[key])}`)
    .join('&')
  if (queryString) {
    url += (url.includes('?') ? '&' : '?') + queryString
  }
}
```

**修改后**:
```javascript
// GET和POST请求都需要把params拼接到URL上（后端@RequestParam需要从URL获取）
if (options.params) {
  const queryString = Object.keys(options.params)
    .filter(key => options.params[key] !== undefined && options.params[key] !== null)
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(options.params[key])}`)
    .join('&')
  if (queryString) {
    url += (url.includes('?') ? '&' : '?') + queryString
  }
}
```

**改进点**:
1. ✅ 移除 `options.method === 'GET'` 限制，所有请求都处理params
2. ✅ 添加 `filter` 过滤掉 `undefined` 和 `null` 值
3. ✅ 增加 `Data` 日志输出，方便调试

---

## 🧪 验证修复

### 1. 检查控制台日志

刷新页面后，发送AI问答，应该看到：

```javascript
=== Request Debug ===
URL: http://localhost:8080/ai-assistant/medicine/question?elderId=1
Token: eyJhbGciOiJIUzI1NiJ9...
Method: POST
Params: {elderId: 1}
Data: {question: "阿司匹林什么时候吃？"}
Authorization: Bearer eyJhbG...
```

**关键**: URL中应该包含 `?elderId=1`

### 2. 检查Network标签

找到 `/ai-assistant/medicine/question` 请求：

**Request URL**:
```
http://localhost:8080/ai-assistant/medicine/question?elderId=1
```

**Request Method**: `POST`

**Request Payload**:
```json
{
  "question": "阿司匹林什么时候吃？"
}
```

### 3. 检查后端日志

后端应该显示：
```
处理老人 ID: 1 的用药问题咨询
成功生成老人 ID: 1 的用药咨询回答
```

不再出现 `MissingServletRequestParameterException` 错误。

---

## 📋 其他受影响的接口

这个修复同时解决了以下接口的问题：

| 接口 | 方法 | params参数 | 说明 |
|------|------|-----------|------|
| `/ai-assistant/medicine/question` | POST | elderId | AI问答 ✅ |
| `/ai-assistant/medicine/missed` | POST | elderId, taskId | 漏服干预 ✅ |
| `/remind/task/today` | GET | elderId | 今日任务（原本正常） |
| `/medicine/look/{elderId}` | GET | - | 路径参数（不受影响） |

---

## 💡 最佳实践

### Spring Boot参数接收方式

#### 1. @RequestParam（查询参数）
```java
@GetMapping("/users")
public Result list(@RequestParam Integer page, @RequestParam Integer size)
// URL: /users?page=1&size=10
```

**前端调用**:
```javascript
request({
  url: '/users',
  method: 'GET',
  params: { page: 1, size: 10 }
})
// → GET /users?page=1&size=10
```

#### 2. @PathVariable（路径参数）
```java
@GetMapping("/users/{id}")
public Result detail(@PathVariable Integer id)
// URL: /users/1
```

**前端调用**:
```javascript
request({
  url: `/users/${id}`,
  method: 'GET'
})
// → GET /users/1
```

#### 3. @RequestBody（请求体）
```java
@PostMapping("/users")
public Result create(@RequestBody UserDTO user)
// Body: {"name": "张三", "age": 25}
```

**前端调用**:
```javascript
request({
  url: '/users',
  method: 'POST',
  data: { name: '张三', age: 25 }
})
// → POST /users
// Body: {"name": "张三", "age": 25}
```

#### 4. 混合使用
```java
@PostMapping("/users/{id}/orders")
public Result createOrder(
    @PathVariable Integer id,
    @RequestParam String type,
    @RequestBody OrderDTO order)
```

**前端调用**:
```javascript
request({
  url: `/users/${id}/orders`,
  method: 'POST',
  params: { type: 'online' },
  data: { productId: 123, quantity: 2 }
})
// → POST /users/1/orders?type=online
// Body: {"productId": 123, "quantity": 2}
```

---

## ⚠️ 注意事项

### 1. params vs data 的区别

| 特性 | params | data |
|------|--------|------|
| 位置 | URL查询参数 | 请求体 |
| 编码 | URL编码 | JSON格式 |
| 适用方法 | GET/POST/PUT等 | POST/PUT/PATCH |
| 后端注解 | @RequestParam | @RequestBody |
| 可见性 | URL中可见 | 不可见 |
| 长度限制 | 有（约2KB） | 无限制 |

### 2. 特殊字符处理

`request.js` 已使用 `encodeURIComponent` 处理特殊字符：
```javascript
.map(key => `${encodeURIComponent(key)}=${encodeURIComponent(options.params[key])}`)
```

### 3. 空值过滤

修复后的代码会过滤掉 `undefined` 和 `null`：
```javascript
.filter(key => options.params[key] !== undefined && options.params[key] !== null)
```

避免生成无效的参数：
```
❌ /api?elderId=&name=null
✅ /api?elderId=1&name=张三
```

---

## 🎯 总结

### 问题原因
- `request.js` 只在GET请求时处理params
- POST请求的params未被拼接到URL
- 后端无法通过@RequestParam获取参数

### 解决方案
- 移除GET请求限制，所有请求都处理params
- 添加空值过滤，避免无效参数
- 增强日志输出，便于调试

### 影响范围
- 修复所有使用 `params` 的POST请求
- 不影响现有的GET请求
- 不影响使用 `data` 的请求体传参

---

**修复时间**: 2026-04-27  
**修复版本**: v1.1  
**状态**: ✅ 已完成
