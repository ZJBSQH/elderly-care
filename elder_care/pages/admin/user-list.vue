<template>
  <view class="admin-container">
    <view class="admin-header">
      <text class="admin-title">👥 用户管理</text>
    </view>

    <!-- 搜索 -->
    <view class="search-bar">
      <input class="search-input" v-model="keyword" placeholder="搜索姓名/手机号" @confirm="doSearch" />
      <button class="search-btn" @click="doSearch">搜索</button>
    </view>

    <!-- 类型筛选 -->
    <view class="filter-tabs">
      <view
        class="filter-tab"
        :class="{ active: filterType === null }"
        @click="filterType = null; doSearch()"
      >全部</view>
      <view
        class="filter-tab"
        :class="{ active: filterType === 0 }"
        @click="filterType = 0; doSearch()"
      >老人</view>
      <view
        class="filter-tab"
        :class="{ active: filterType === 1 }"
        @click="filterType = 1; doSearch()"
      >家属</view>
      <view
        class="filter-tab"
        :class="{ active: filterType === 2 }"
        @click="filterType = 2; doSearch()"
      >管理员</view>
    </view>

    <!-- 用户列表 -->
    <view class="user-list" v-if="users.length > 0">
      <view class="user-card" v-for="u in users" :key="u.id">
        <view class="user-main">
          <view class="user-avatar">
            <text class="avatar-text">{{ u.name ? u.name.charAt(0) : '?' }}</text>
          </view>
          <view class="user-info">
            <text class="user-name">{{ u.name || '未设置' }}</text>
            <text class="user-phone">{{ maskPhone(u.phone) }}</text>
          </view>
          <view class="user-tags">
            <text class="tag" :class="'type-' + u.userType">{{ u.userTypeDesc || typeMap[u.userType] }}</text>
            <text class="tag" :class="u.status === 1 ? 'status-on' : 'status-off'">{{ u.statusDesc || (u.status === 1 ? '正常' : '禁用') }}</text>
          </view>
        </view>
        <view class="user-actions">
          <button class="act-btn" @click="openEdit(u)">编辑</button>
          <button
            class="act-btn"
            :class="u.status === 1 ? 'warn' : 'ok'"
            @click="toggleStatus(u)"
          >{{ u.status === 1 ? '禁用' : '启用' }}</button>
        </view>
      </view>
    </view>

    <view class="empty" v-else>
      <text>暂无用户数据</text>
    </view>

    <!-- 加载更多 -->
    <view class="loading-more" v-if="hasMore">
      <button class="more-btn" @click="loadMore">加载更多</button>
    </view>
    <view class="no-more" v-else>
      <text>— 已显示全部 —</text>
    </view>

    <!-- 编辑弹窗 -->
    <view class="modal-mask" v-if="editUser" @click="editUser = null">
      <view class="modal-card" @click.stop>
        <text class="modal-title">编辑用户信息</text>

        <view class="form-item">
          <text class="form-label">姓名</text>
          <input class="form-input" v-model="editForm.name" placeholder="姓名" />
        </view>
        <view class="form-item">
          <text class="form-label">年龄</text>
          <input class="form-input" type="number" v-model="editForm.age" placeholder="年龄" />
        </view>
        <view class="form-item">
          <text class="form-label">性别</text>
          <view class="radio-group">
            <view class="radio-item" :class="{ active: editForm.sex === '男' }" @click="editForm.sex = '男'">男</view>
            <view class="radio-item" :class="{ active: editForm.sex === '女' }" @click="editForm.sex = '女'">女</view>
          </view>
        </view>
        <view class="form-item">
          <text class="form-label">用户类型</text>
          <view class="radio-group">
            <view class="radio-item" :class="{ active: editForm.userType === 0 }" @click="editForm.userType = 0">老人</view>
            <view class="radio-item" :class="{ active: editForm.userType === 1 }" @click="editForm.userType = 1">家属</view>
            <view class="radio-item" :class="{ active: editForm.userType === 2 }" @click="editForm.userType = 2">管理员</view>
          </view>
        </view>

        <view class="modal-btns">
          <button class="cancel-btn" @click="editUser = null">取消</button>
          <button class="save-btn" :loading="saving" @click="saveEdit">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { getAdminUsers, updateAdminUser, updateUserStatus } from '../../api/admin.js'

const typeMap = { 0: '老人', 1: '家属', 2: '管理员' }

const keyword = ref('')
const filterType = ref(null)
const users = ref([])
const page = ref(1)
const hasMore = ref(true)
const editUser = ref(null)
const saving = ref(false)

const editForm = reactive({ name: '', age: '', sex: '', userType: 0 })

function maskPhone(phone) {
  if (!phone) return '--'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

async function loadUsers(reset = true) {
  if (reset) { page.value = 1; users.value = [] }

  try {
    const params = { page: page.value, size: 10 }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (filterType.value !== null) params.userType = filterType.value

    const result = await getAdminUsers(params)
    if (result && result.list) {
      if (reset) {
        users.value = result.list
      } else {
        users.value = [...users.value, ...result.list]
      }
      hasMore.value = users.value.length < result.total
    }
  } catch (e) {
    console.error('加载用户列表失败:', e)
  }
}

function doSearch() { loadUsers(true) }
function loadMore() { page.value++; loadUsers(false) }

function openEdit(u) {
  editUser.value = u
  editForm.name = u.name || ''
  editForm.age = u.age !== null ? String(u.age) : ''
  editForm.sex = u.sex || '男'
  editForm.userType = u.userType !== undefined ? u.userType : 0
}

async function saveEdit() {
  saving.value = true
  try {
    await updateAdminUser(editUser.value.id, {
      name: editForm.name || undefined,
      age: editForm.age ? parseInt(editForm.age, 10) : undefined,
      sex: editForm.sex || undefined,
      userType: editForm.userType
    })
    uni.showToast({ title: '保存成功', icon: 'success', duration: 1500 })
    editUser.value = null
    loadUsers(true)
  } catch (e) {
    uni.showToast({ title: e.message || '保存失败', icon: 'none', duration: 2000 })
  } finally {
    saving.value = false
  }
}

async function toggleStatus(u) {
  const newStatus = u.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'

  uni.showModal({
    title: '确认操作',
    content: `确定要${action}用户「${u.name || u.phone}」吗？`,
    confirmText: `确定${action}`,
    cancelText: '取消',
    success: async (res) => {
      if (res.confirm) {
        try {
          await updateUserStatus(u.id, newStatus)
          uni.showToast({ title: `已${action}`, icon: 'success', duration: 1500 })
          loadUsers(true)
        } catch (e) {
          uni.showToast({ title: e.message || '操作失败', icon: 'none', duration: 2000 })
        }
      }
    }
  })
}

// 初始化加载
loadUsers(true)
</script>

<style lang="scss" scoped>
@import '../../styles/common.scss';

.admin-container {
  min-height: 100vh;
  background: #f0f2f5;
  padding: 24rpx 28rpx 160rpx;
}

.admin-header {
  padding: 32rpx 0 16rpx;
}

.admin-title {
  font-size: 40rpx;
  font-weight: bold;
  color: $color-text;
}

// ========== 搜索 ==========
.search-bar {
  display: flex;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.search-input {
  flex: 1;
  height: 80rpx;
  background: $color-white;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
}

.search-btn {
  width: 140rpx;
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  background: $color-primary;
  color: #fff;
  border-radius: 12rpx;
  font-size: 28rpx;
  border: none;
}

// ========== 筛选 ==========
.filter-tabs {
  display: flex;
  gap: 12rpx;
  margin-bottom: 24rpx;
}

.filter-tab {
  flex: 1;
  height: 64rpx;
  line-height: 64rpx;
  text-align: center;
  background: $color-white;
  border-radius: 12rpx;
  font-size: 26rpx;
  color: $color-text-light;
  border: 2rpx solid transparent;

  &.active { border-color: $color-primary; color: $color-primary; font-weight: bold; }
}

// ========== 用户列表 ==========
.user-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.user-card {
  background: $color-white;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}

.user-main {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.user-avatar {
  width: 72rpx;
  height: 72rpx;
  background: linear-gradient(135deg, $color-primary, #36cfc9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-text { font-size: 32rpx; font-weight: bold; color: #fff; }

.user-info { flex: 1; }
.user-name { display: block; font-size: 30rpx; font-weight: 600; color: $color-text; margin-bottom: 4rpx; }
.user-phone { font-size: 24rpx; color: $color-text-placeholder; }

.user-tags {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  align-items: flex-end;
}

.tag {
  font-size: 20rpx;
  padding: 2rpx 12rpx;
  border-radius: 8rpx;

  &.type-0 { background: #e6ffe6; color: #52c41a; }
  &.type-1 { background: #fff3e6; color: #fa8c16; }
  &.type-2 { background: #f3e6ff; color: #722ed1; }
  &.status-on { background: #e6ffe6; color: #52c41a; }
  &.status-off { background: #ffe6e6; color: #ff4d4f; }
}

.user-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 2rpx solid #f5f5f5;
}

.act-btn {
  flex: 1;
  height: 56rpx;
  line-height: 56rpx;
  text-align: center;
  background: #f5f7fa;
  border-radius: 10rpx;
  font-size: 24rpx;
  color: $color-text-light;
  border: none;

  &.warn { color: $color-danger; background: #fff2f2; }
  &.ok { color: $color-success; background: #f0fff0; }
}

// ========== 加载更多 ==========
.loading-more, .no-more {
  text-align: center;
  padding: 32rpx 0;
}

.more-btn {
  width: 220rpx;
  height: 64rpx;
  line-height: 64rpx;
  background: $color-white;
  border-radius: 32rpx;
  font-size: 26rpx;
  color: $color-primary;
  border: none;
}

.no-more text { font-size: 24rpx; color: #ccc; }

// ========== 空状态 ==========
.empty { text-align: center; padding: 80rpx 0; font-size: 28rpx; color: $color-text-placeholder; }

// ========== 编辑弹窗 ==========
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 40rpx;
}

.modal-card {
  background: #fff;
  border-radius: 20rpx;
  width: 100%;
  max-width: 640rpx;
  padding: 40rpx 32rpx;
}

.modal-title {
  font-size: 36rpx;
  font-weight: bold;
  text-align: center;
  display: block;
  margin-bottom: 32rpx;
}

.form-item { margin-bottom: 28rpx; }

.form-label {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: $color-text;
  margin-bottom: 12rpx;
}

.form-input {
  height: 80rpx;
  background: #f5f7fa;
  border-radius: 12rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
}

.radio-group {
  display: flex;
  gap: 12rpx;
}

.radio-item {
  flex: 1;
  height: 64rpx;
  line-height: 64rpx;
  text-align: center;
  background: #f5f7fa;
  border-radius: 10rpx;
  font-size: 26rpx;
  color: $color-text-light;
  border: 2rpx solid transparent;

  &.active { border-color: $color-primary; color: $color-primary; background: rgba(64,158,255,0.08); }
}

.modal-btns {
  display: flex;
  gap: 20rpx;
  margin-top: 32rpx;
}

.cancel-btn {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  background: #f5f5f5;
  color: $color-text-light;
  border-radius: 12rpx;
  font-size: 28rpx;
  border: none;
}

.save-btn {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  background: $color-primary;
  color: #fff;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: bold;
  border: none;
}
</style>
