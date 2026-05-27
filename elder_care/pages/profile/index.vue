<template>
  <view class="page-container">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="avatar">
        <text class="avatar-text">{{ displayAvatar }}</text>
      </view>
      <view class="user-info">
        <text class="user-name">{{ userInfo?.name || '未设置姓名' }}</text>
        <view class="user-tags">
          <text class="tag type-tag">{{ typeLabel }}</text>
          <text class="tag" v-if="userInfo?.age">{{ userInfo.age }}岁</text>
          <text class="tag">{{ sexLabel }}</text>
        </view>
        <text class="user-phone">{{ userInfo?.phone || '' }}</text>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-group">
      <view class="menu-item" @click="goTo('edit')">
        <text class="menu-icon">✏️</text>
        <text class="menu-label">编辑资料</text>
        <text class="menu-arrow">→</text>
      </view>
      <view class="menu-item" @click="goTo('password')">
        <text class="menu-icon">🔒</text>
        <text class="menu-label">修改密码</text>
        <text class="menu-arrow">→</text>
      </view>
      <view class="menu-item" @click="goTo('emergency')">
        <text class="menu-icon">🆘</text>
        <text class="menu-label">紧急联系人</text>
        <text class="menu-arrow">→</text>
      </view>
      <view class="menu-item" @click="goTo('family-bind')">
        <text class="menu-icon">👨‍👩‍👦</text>
        <text class="menu-label">家属绑定</text>
        <text class="menu-hint" v-if="userStore.isFamily">管理绑定关系</text>
        <text class="menu-arrow">→</text>
      </view>
    </view>

    <!-- 关于 -->
    <view class="menu-group">
      <view class="menu-item">
        <text class="menu-icon">📱</text>
        <text class="menu-label">关于应用</text>
        <text class="menu-value">老人用药管理 v1.0</text>
      </view>
    </view>

    <!-- 退出登录 -->
    <button class="logout-btn" @click="handleLogout">退出登录</button>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../store/user.js'
import { getUserInfo } from '../../api/auth.js'

const userStore = useUserStore()

const userInfo = ref({ ...(userStore.userInfo || {}) })

const displayAvatar = computed(() => {
  const name = userInfo.value?.name
  return name ? name.charAt(0) : '用'
})

const typeLabel = computed(() => {
  const t = userInfo.value?.userType ?? userStore.userType
  if (t === 2) return '管理员'
  if (t === 1) return '家属'
  return '老人'
})

const sexLabel = computed(() => {
  const s = userInfo.value?.sex
  if (s === '1' || s === 1) return '男'
  if (s === '0' || s === 0) return '女'
  return ''
})

/** 刷新用户信息 */
async function refreshUserInfo() {
  try {
    const data = await getUserInfo()
    const u = data?.user || data
    if (u) {
      userInfo.value = u
      userStore.setUserInfo(u)
    }
  } catch (e) {
    console.error('刷新用户信息失败:', e)
  }
}

function goTo(page) {
  uni.navigateTo({ url: `/pages/profile/${page}` })
}

function handleLogout() {
  uni.showModal({
    title: '退出登录',
    content: '确定要退出登录吗？',
    confirmText: '确定',
    cancelText: '取消',
    success: (res) => {
      if (res.confirm) userStore.logout()
    }
  })
}

onMounted(refreshUserInfo)
</script>

<style lang="scss" scoped>
@import '../../styles/common.scss';

.page-container {
  min-height: 100vh;
  background: $color-bg;
  padding-bottom: 120rpx;
}

// ========== 用户卡片 ==========
.user-card {
  background: linear-gradient(135deg, $color-primary 0%, #36cfc9 100%);
  padding: 48rpx $elder-spacing-xl;
  display: flex;
  align-items: center;
  gap: 28rpx;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid rgba(255, 255, 255, 0.5);
  flex-shrink: 0;
}

.avatar-text {
  font-size: 52rpx;
  font-weight: bold;
  color: #fff;
}

.user-info {
  flex: 1;
}

.user-name {
  display: block;
  font-size: $elder-font-xl;
  font-weight: bold;
  color: #fff;
  margin-bottom: 8rpx;
}

.user-tags {
  display: flex;
  gap: 12rpx;
  margin-bottom: 8rpx;
}

.tag {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.85);
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8rpx;
  padding: 4rpx 16rpx;
}

.type-tag {
  background: rgba(255, 255, 255, 0.35);
  font-weight: bold;
}

.user-phone {
  display: block;
  font-size: $elder-font-small;
  color: rgba(255, 255, 255, 0.8);
}

// ========== 菜单 ==========
.menu-group {
  margin: $elder-spacing $elder-spacing-xl 0;
  background: $color-white;
  border-radius: $elder-radius-lg;
  overflow: hidden;
  box-shadow: $elder-shadow;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 28rpx $elder-spacing-lg;
  border-bottom: 2rpx solid #f5f5f5;

  &:last-child { border-bottom: none; }
  &:active { background: #f9f9f9; }
}

.menu-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
}

.menu-label {
  flex: 1;
  font-size: $elder-font-base;
  font-weight: 600;
  color: $color-text;
}

.menu-hint {
  font-size: 24rpx;
  color: $color-text-placeholder;
  margin-right: 12rpx;
}

.menu-value {
  font-size: $elder-font-small;
  color: $color-text-placeholder;
}

.menu-arrow {
  font-size: $elder-font-base;
  color: #ccc;
}

// ========== 退出按钮 ==========
.logout-btn {
  margin: 48rpx $elder-spacing-xl;
  height: $elder-btn-height;
  line-height: $elder-btn-height;
  background: $color-white;
  color: $color-danger;
  border-radius: $elder-radius-lg;
  font-size: $elder-font-medium;
  font-weight: bold;
  border: 2rpx solid $color-danger;
}
</style>
