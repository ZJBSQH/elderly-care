<template>
  <view class="page-container">
    <!-- 头部 -->
    <view class="header-card">
      <view class="header-top-row">
        <text class="welcome-text">👋 {{ displayName }}</text>
        <button class="logout-btn" @click="handleLogout">退出</button>
      </view>
      <text class="header-desc">家人健康，时刻关注</text>
    </view>

    <!-- 已绑定老人列表 -->
    <view class="section-title" v-if="elders.length > 0">已绑定的家人</view>
    <view class="elder-list" v-if="elders.length > 0">
      <view
        class="elder-card"
        v-for="elder in elders"
        :key="elder.elderId || elder.id"
      >
        <view class="elder-main" @click="goToDetail(elder)">
          <view class="elder-avatar">
            <text class="avatar-text">{{ getInitial(elder.name) }}</text>
          </view>
          <view class="elder-info">
            <text class="elder-name">{{ elder.name || elder.elderName || '长辈' }}</text>
            <text class="elder-meta" v-if="elder.age">{{ elder.age }}岁</text>
          </view>
          <view class="health-brief" v-if="elder.latestHealth">
            <text class="brief-item" :class="elder.latestHealth.warningFlag === 1 ? 'warn' : 'ok'">
              {{ elder.latestHealth.warningFlag === 1 ? '⚠️' : '✅' }}
            </text>
          </view>
          <text class="arrow">→</text>
        </view>

        <!-- 快捷操作 -->
        <view class="elder-actions">
          <button class="action-btn" @click="goToMedicine(elder)">💊 用药</button>
          <button class="action-btn" @click="goToDetail(elder)">📊 健康</button>
          <button class="action-btn" @click="makeCall(elder)">📞 联系</button>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-card" v-else-if="!loading">
      <text class="empty-icon">👨‍👩‍👦</text>
      <text class="empty-text">还没有绑定的家人</text>
      <button class="bind-btn" @click="goToBind">+ 添加家人</button>
    </view>

    <!-- 添加入口 -->
    <view class="add-section" v-if="elders.length > 0">
      <button class="add-btn" @click="goToBind">+ 添加家人</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../store/user.js'
import { getBoundElders } from '../../api/family.js'
import { getLatestHealth } from '../../api/health.js'

const userStore = useUserStore()

const displayName = computed(() => userStore.userInfo?.name || '家属')
const elders = ref([])
const loading = ref(true)

function getInitial(name) {
  return name ? name.charAt(0) : '长'
}

/** 加载绑定老人列表 */
async function loadElders() {
  loading.value = true
  try {
    const list = await getBoundElders()
    const arr = Array.isArray(list) ? list : []

    // 为每个老人加载最新健康数据
    for (const elder of arr) {
      try {
        const eId = elder.elderId || elder.id
        if (eId) {
          elder.latestHealth = await getLatestHealth({ elderId: eId })
        }
      } catch (e) { /* ignore */ }
    }

    elders.value = arr
  } catch (e) {
    console.error('加载绑定老人失败:', e)
    elders.value = []
  } finally {
    loading.value = false
  }
}

function goToDetail(elder) {
  const id = elder.elderId || elder.id
  const name = elder.name || elder.elderName || ''
  uni.navigateTo({ url: `/pages/family/elder-detail?elderId=${id}&name=${encodeURIComponent(name)}` })
}

function goToMedicine(elder) {
  const id = elder.elderId || elder.id
  uni.navigateTo({ url: `/pages/family/remote-medicine?elderId=${id}` })
}

function goToBind() {
  uni.navigateTo({ url: '/pages/family/bind' })
}

function makeCall(elder) {
  const phone = elder.phone || elder.emergencyContact
  if (!phone) {
    return uni.showToast({ title: '暂无联系方式', icon: 'none', duration: 2000 })
  }
  uni.makePhoneCall({ phoneNumber: phone })
}

onMounted(loadElders)

function handleLogout() {
  uni.showModal({
    title: '退出登录',
    content: '确定要退出当前账号吗？',
    confirmText: '确定退出',
    cancelText: '取消',
    success(res) {
      if (res.confirm) {
        userStore.logout()
      }
    }
  })
}
</script>

<style lang="scss" scoped>
@import '../../styles/common.scss';

.page-container {
  min-height: 100vh;
  background: $color-bg;
  padding-bottom: 120rpx;
}

// ========== 头部 ==========
.header-card {
  background: linear-gradient(135deg, #5B8FF9 0%, #36cfc9 100%);
  padding: 48rpx $elder-spacing-xl;
  text-align: center;
  color: #fff;
}

.header-top-row {
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  margin-bottom: 8rpx;
}

.welcome-text {
  font-size: $elder-font-xl;
  font-weight: bold;
}

.logout-btn {
  position: absolute;
  right: 0;
  height: 52rpx;
  line-height: 52rpx;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: 2rpx solid rgba(255, 255, 255, 0.4);
  border-radius: 26rpx;
  font-size: 22rpx;
  padding: 0 20rpx;
}

.header-desc {
  font-size: $elder-font-small;
  opacity: 0.85;
  display: block;
}

// ========== 老人列表 ==========
.section-title {
  font-size: $elder-font-large;
  font-weight: bold;
  color: $color-text;
  padding: $elder-spacing $elder-spacing-xl 8rpx;
}

.elder-list {
  padding: $elder-spacing $elder-spacing-xl;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.elder-card {
  background: $color-white;
  border-radius: $elder-radius-lg;
  box-shadow: $elder-shadow;
  overflow: hidden;
}

.elder-main {
  display: flex;
  align-items: center;
  padding: 28rpx;
  gap: 20rpx;
}

.elder-avatar {
  width: 90rpx;
  height: 90rpx;
  background: linear-gradient(135deg, $color-primary, #36cfc9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-text {
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
}

.elder-info {
  flex: 1;
}

.elder-name {
  display: block;
  font-size: $elder-font-medium;
  font-weight: bold;
  color: $color-text;
}

.elder-meta {
  font-size: $elder-font-small;
  color: $color-text-placeholder;
}

.health-brief {
  margin-right: 8rpx;
}

.brief-item {
  font-size: 36rpx;
  &.warn { color: $color-warning; }
  &.ok { color: $color-success; }
}

.arrow {
  font-size: $elder-font-base;
  color: #ccc;
}

.elder-actions {
  display: flex;
  border-top: 2rpx solid #f5f5f5;
}

.action-btn {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  text-align: center;
  background: none;
  font-size: $elder-font-small;
  color: $color-text-light;
  border: none;
  border-radius: 0;

  &:not(:last-child) { border-right: 2rpx solid #f5f5f5; }
  &:active { background: #f9f9f9; }
}

// ========== 空状态 ==========
.empty-card { text-align: center; padding: 120rpx 0; }
.empty-icon { font-size: 100rpx; display: block; margin-bottom: 24rpx; }
.empty-text { font-size: $elder-font-medium; color: $color-text-placeholder; margin-bottom: 32rpx; }

.bind-btn {
  display: inline-block;
  height: $elder-btn-height;
  line-height: $elder-btn-height;
  background: linear-gradient(135deg, $color-primary 0%, #36cfc9 100%);
  color: #fff;
  border-radius: $elder-radius-lg;
  font-size: $elder-font-medium;
  font-weight: bold;
  border: none;
  padding: 0 48rpx;
}

// ========== 添加入口 ==========
.add-section {
  padding: $elder-spacing $elder-spacing-xl;
}

.add-btn {
  width: 100%;
  height: $elder-btn-height;
  line-height: $elder-btn-height;
  background: $color-white;
  color: $color-primary;
  border-radius: $elder-radius-lg;
  font-size: $elder-font-medium;
  font-weight: bold;
  border: 2rpx dashed $color-primary;
}
</style>
