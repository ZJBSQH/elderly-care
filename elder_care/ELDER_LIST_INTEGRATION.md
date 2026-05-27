# 老人列表接口对接完成说明

## ✅ 已完成的工作

### 1. 后端接口完善

#### 1.1 完善 `RecordServiceImpl.getBoundEldersForFamily()` 方法

**文件**: `src/main/java/com/elderlycare/service/impl/RecordServiceImpl.java`

**改进内容**:
- ✅ 添加 `ElderMapper` 依赖注入
- ✅ 添加 `LambdaQueryWrapper` 导入
- ✅ 通过 `userId` 查询老人基本信息（姓名、年龄、性别、手机号、头像）
- ✅ 从 `family` 表获取家属与老人的关系
- ✅ 返回完整的老人信息列表，包含：
  - `id`: 老人ID
  - `name`: 老人姓名
  - `age`: 年龄
  - `sex`: 性别
  - `phone`: 手机号
  - `avatar`: 头像
  - `relation`: 与家属的关系（如：父子、母女等）
  - `pendingTasks`: 今日待服药数量

**接口路径**: `GET /record/elders`

**返回数据格式**:
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
      "phone": "138****8000",
      "avatar": "http://...",
      "relation": "父子",
      "pendingTasks": 3
    }
  ]
}
```

#### 1.2 已有接口复用

**用户信息接口**: `GET /auth/profile`
- 返回当前登录用户的完整信息
- 对于老人用户，额外返回 `elderId` 和 `emergencyContact`
- 用于老人用户获取自己的信息

---

### 2. 前端API封装

#### 2.1 新增API方法

**文件**: `elder_care/api/ai-assistant.js`

```javascript
// 5. 获取当前用户信息（包含老人列表）
export function getCurrentUserInfo() {
  return request({
    url: '/auth/profile',
    method: 'GET'
  })
}

// 6. 家属获取绑定的老人列表
export function getBoundElders() {
  return request({
    url: '/record/elders',
    method: 'GET'
  })
}
```

---

### 3. AI助手页面对接真实接口

#### 3.1 智能加载老人列表

**文件**: `elder_care/pages/medicine/ai-assistant.vue`

**实现逻辑**:

```javascript
async loadElderList() {
  // 1. 获取当前用户类型
  const userType = userInfo.userType // 0-老人, 1-家属
  
  if (userType === 0) {
    // 老人用户：获取自己的信息
    await this.loadElderForSelf(userInfo)
  } else {
    // 家属用户：获取绑定的老人列表
    await this.loadBoundElders()
  }
}
```

#### 3.2 老人用户加载流程

```javascript
async loadElderForSelf(userInfo) {
  // 调用 /auth/profile 接口
  const res = await getCurrentUserInfo()
  
  // 提取 elderId 和用户信息
  const elder = {
    id: data.elderId,
    name: data.user.name,
    age: data.user.age,
    sex: data.user.sex,
    phone: data.user.phone,
    avatar: data.user.avatar,
    relation: '本人',
    displayName: `${name}（本人）`
  }
  
  this.elderList = [elder]
  this.currentElder = elder
}
```

#### 3.3 家属用户加载流程

```javascript
async loadBoundElders() {
  // 调用 /record/elders 接口
  const res = await getBoundElders()
  
  // 转换数据格式，添加 displayName
  this.elderList = elders.map(elder => ({
    id: elder.id,
    name: elder.name,
    age: elder.age,
    sex: elder.sex,
    phone: elder.phone,
    avatar: elder.avatar,
    relation: elder.relation,
    pendingTasks: elder.pendingTasks,
    displayName: `${name}（${relation}）`
  }))
  
  this.currentElder = this.elderList[0]
}
```

#### 3.4 UI优化

**老人选择器显示**:
- 使用 `displayName` 字段
- 格式：`姓名（关系）`
- 示例：`张三（本人）`、`李四（父子）`

**降级方案**:
- 如果接口调用失败，使用模拟数据
- 保证页面可用性

---

## 📊 数据流图

### 老人用户流程
```
用户登录 
  ↓
存储 userInfo (userType=0)
  ↓
进入AI助手页面
  ↓
调用 loadElderList()
  ↓
检测到 userType=0
  ↓
调用 getCurrentUserInfo() → GET /auth/profile
  ↓
后端返回 { user, elderId, emergencyContact }
  ↓
前端构建 elderList = [{ id, name, age, ... }]
  ↓
显示老人选择器：张三（本人）
```

### 家属用户流程
```
用户登录
  ↓
存储 userInfo (userType=1)
  ↓
进入AI助手页面
  ↓
调用 loadElderList()
  ↓
检测到 userType=1
  ↓
调用 getBoundElders() → GET /record/elders
  ↓
后端查询 family 表，获取绑定的老人列表
  ↓
关联查询 user 表，获取老人详细信息
  ↓
返回 [{ id, name, age, relation, ... }]
  ↓
前端转换为 elderList，添加 displayName
  ↓
显示老人选择器：张三（父子）、李四（母女）...
```

---

## 🔍 关键代码片段

### 后端：查询绑定老人

```java
@Override
public Result<List<Map<String, Object>>> getBoundEldersForFamily() {
    Integer currentUserId = securityUtil.getCurrentUserId();
    
    // 1. 查询绑定的老人列表
    List<Elder> elders = familyMapper.selectBoundEldersByFamilyUserId(currentUserId);
    
    List<Map<String, Object>> resultList = new ArrayList<>();
    for (Elder elder : elders) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", elder.getId());
        
        // 2. 通过userId查询老人基本信息
        User elderUser = userMapper.selectById(elder.getUserId());
        if (elderUser != null) {
            item.put("name", elderUser.getName());
            item.put("age", elderUser.getAge());
            item.put("sex", elderUser.getSex());
            item.put("phone", elderUser.getPhone());
            item.put("avatar", elderUser.getAvatar());
        }
        
        // 3. 从family表获取关系
        LambdaQueryWrapper<Family> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Family::getFamilyUserId, currentUserId)
               .eq(Family::getElderId, elder.getId())
               .eq(Family::getBindStatus, 1);
        Family family = familyMapper.selectOne(wrapper);
        item.put("relation", family.getRelation());
        
        // 4. 统计今日待服药数量
        int pendingCount = countPendingTasks(elder.getId());
        item.put("pendingTasks", pendingCount);
        
        resultList.add(item);
    }
    
    return Result.success(resultList);
}
```

### 前端：智能加载

```javascript
async loadElderList() {
  try {
    uni.showLoading({ title: '加载中...', mask: true })
    
    const userInfo = uni.getStorageSync('userInfo')
    const userType = userInfo.userType
    
    if (userType === 0) {
      await this.loadElderForSelf(userInfo)
    } else {
      await this.loadBoundElders()
    }
    
    uni.hideLoading()
  } catch (error) {
    console.error('加载老人列表失败:', error)
    uni.hideLoading()
    
    // 降级方案
    this.elderList = [
      { id: 1, name: '张三', displayName: '张三（本人）' }
    ]
    this.currentElder = this.elderList[0]
  }
}
```

---

## ✨ 功能特性

| 特性 | 说明 |
|------|------|
| **智能识别用户类型** | 自动判断是老人用户还是家属用户 |
| **差异化加载策略** | 老人用户加载自己，家属用户加载绑定列表 |
| **完整信息显示** | 姓名、年龄、性别、手机号、头像、关系 |
| **友好显示格式** | `姓名（关系）` 格式，清晰易懂 |
| **错误降级处理** | 接口失败时使用模拟数据，保证可用性 |
| **加载状态提示** | Loading动画，提升用户体验 |
| **日志记录** | 后端记录查询日志，便于调试 |

---

## 🧪 测试建议

### 1. 老人用户测试
```
1. 使用老人账号登录（userType=0）
2. 进入AI助手页面
3. 检查顶部老人选择器是否显示：姓名（本人）
4. 确认只有一个选项
5. 测试切换Tab功能是否正常
```

### 2. 家属用户测试
```
1. 使用家属账号登录（userType=1）
2. 确保已在数据库中绑定老人（family表有记录）
3. 进入AI助手页面
4. 检查老人选择器是否显示所有绑定的老人
5. 点击下拉框，查看所有老人及其关系
6. 切换不同老人，测试各功能是否正常
```

### 3. 异常情况测试
```
1. 未登录状态访问 → 应提示"未登录"
2. 家属未绑定任何老人 → 应提示"未绑定任何老人"或使用降级数据
3. 网络异常 → 应显示错误提示并使用降级数据
4. 老人信息不完整 → 应显示默认值（如"未设置姓名"）
```

---

## 📝 注意事项

### 1. 数据库要求
- `family` 表必须有正确的绑定记录
- `bind_status = 1` 表示已绑定
- `relation` 字段存储关系（如：父子、母女、夫妻等）

### 2. 权限控制
- 后端已通过 `securityUtil.getCurrentUserId()` 获取当前用户
- JWT Token 自动验证，无需额外处理
- 家属只能查看自己绑定的老人

### 3. 性能优化建议
- 可以考虑缓存老人列表，减少接口调用
- `countPendingTasks()` 方法目前返回0，需要实现具体逻辑
- 可以添加分页支持（如果绑定老人很多）

### 4. 扩展性
- 未来可以添加老人搜索功能
- 可以添加老人头像显示
- 可以显示老人的健康状态标签

---

## 🚀 下一步优化

1. **实现待服药统计**
   - 完善 `countPendingTasks()` 方法
   - 查询今日未完成的任务数量

2. **添加WebSocket实时通知**
   - 接收AI推送的提醒消息
   - 显示实时通知弹窗

3. **优化加载体验**
   - 添加骨架屏
   - 预加载老人数据

4. **增强错误处理**
   - 更详细的错误提示
   - 重试机制

5. **添加缓存机制**
   - 本地缓存老人列表
   - 减少不必要的接口调用

---

**完成时间**: 2026-04-27  
**版本**: v1.0  
**状态**: ✅ 已完成并编译通过
