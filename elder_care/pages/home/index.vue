<template>
  <view class="home-container">
    <!-- 欢迎区域 -->
    <view class="header-section">
      <text class="welcome-text">👋 您好，{{ displayName }}</text>
      <text class="date-text">{{ currentDate }}</text>
    </view>

    <!-- 今日用药提醒 -->
    <view class="medicine-card" v-if="todayTasks.length > 0">
      <view class="card-header">
        <text class="card-title">💊 今日用药提醒</text>
        <text class="card-count">{{ todayTasks.length }}项</text>
      </view>
      <view class="task-list">
        <view class="task-item" v-for="task in todayTasks.slice(0, 3)" :key="task.id">
          <text class="task-time">{{ formatTime(task.remindTime) }}</text>
          <text class="task-name">{{ task.title || task.content }}</text>
          <text class="task-status">{{ task.status === 2 ? '✅' : '⏰' }}</text>
        </view>
      </view>
      <button class="card-btn primary-btn" @click="goToMedicine">查看全部用药 →</button>
    </view>

    <view class="medicine-card empty-card" v-else>
      <text class="empty-icon">✅</text>
      <text class="empty-text">今日暂无用药安排</text>
    </view>

    <!-- 健康数据概览 -->
    <view class="health-card">
      <text class="card-title">📊 健康数据概览</text>
      <view class="health-grid">
        <view class="health-item">
          <text class="health-label">血压</text>
          <text class="health-value">{{ healthData?.bloodPressure || '--' }}</text>
          <text class="health-status" :class="healthStatusClass">
            {{ healthStatusText }}
          </text>
        </view>
        <view class="health-item">
          <text class="health-label">血糖</text>
          <text class="health-value">{{ healthData?.bloodSugar || '--' }}</text>
          <text class="health-status" :class="healthStatusClass">
            {{ healthStatusText }}
          </text>
        </view>
        <view class="health-item">
          <text class="health-label">心率</text>
          <text class="health-value">{{ healthData?.heartRate || '--' }}<text v-if="healthData?.heartRate" class="unit">bpm</text></text>
          <text class="health-status" :class="healthStatusClass">
            {{ healthStatusText }}
          </text>
        </view>
      </view>
      <button class="card-btn health-btn" @click="goToHealth">查看更多数据 →</button>
    </view>

    <!-- 最近通知 -->
    <view class="notice-card" v-if="notices.length > 0">
      <view class="card-header">
        <text class="card-title">🔔 最近通知</text>
        <text class="notice-badge" v-if="unreadCount > 0">{{ unreadCount }}条未读</text>
      </view>
      <view class="notice-list">
        <view class="notice-item" v-for="notice in notices.slice(0, 3)" :key="notice.id">
          <view class="notice-dot" :class="{ unread: notice.readStatus === 0 }"></view>
          <view class="notice-body">
            <text class="notice-title">{{ notice.title }}</text>
            <text class="notice-content">{{ notice.content }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="quick-actions">
      <view class="quick-action" @click="goToAI">
        <text class="qa-icon">💬</text>
        <text class="qa-label">健康问答</text>
      </view>
      <view class="quick-action" @click="goToMedicine">
        <text class="qa-icon">💊</text>
        <text class="qa-label">用药管理</text>
      </view>
      <view class="quick-action" @click="goToHealth">
        <text class="qa-icon">📊</text>
        <text class="qa-label">健康数据</text>
      </view>
      <view class="quick-action sos-action" @click="emergencyHelp">
        <text class="qa-icon">🆘</text>
        <text class="qa-label">紧急求助</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../store/user.js'
import { getTodayMedicine } from '../../api/medicine.js'
import { getLatestHealth } from '../../api/health.js'
import { getMyNotifications } from '../../api/news.js'

const userStore = useUserStore()

// 显示名称
const displayName = computed(() => {
  return userStore.userInfo?.name || userStore.userInfo?.username || '长辈'
})

// 当前日期
const currentDate = computed(() => {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const weeks = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return `${y}年${m}月${day}日 ${weeks[d.getDay()]}`
})

const todayTasks = ref([])
const healthData = ref(null)
const notices = ref([])
const unreadCount = ref(0)

const healthStatusClass = computed(() => {
  if (!healthData.value) return ''
  return healthData.value.warningFlag === 1 ? 'warning' : 'normal'
})

const healthStatusText = computed(() => {
  if (!healthData.value) return '未录入'
  return healthData.value.warningFlag === 1 ? '⚠️ 异常' : '✅ 正常'
})

/** 格式化时间 "08:00:00" → "08:00" */
function formatTime(timeStr) {
  if (!timeStr) return ''
  return String(timeStr).substring(0, 5)
}

/** 加载今日用药 */
async function loadTodayTasks() {
  try {
    const elderId = userStore.elderId
    const tasks = await getTodayMedicine({ elderId })
    todayTasks.value = Array.isArray(tasks) ? tasks : []
  } catch (e) {
    console.error('加载用药数据失败:', e)
    todayTasks.value = []
  }
}

/** 加载最新健康数据 */
async function loadHealthData() {
  try {
    const elderId = userStore.elderId
    if (!elderId) {
      healthData.value = null
      return
    }
    healthData.value = await getLatestHealth({ elderId })
  } catch (e) {
    console.error('加载健康数据失败:', e)
    healthData.value = null
  }
}

/** 加载通知 */
async function loadNotices() {
  try {
    const list = await getMyNotifications()
    notices.value = Array.isArray(list) ? list : []
    unreadCount.value = notices.value.filter(n => n.readStatus === 0).length
  } catch (e) {
    console.error('加载通知失败:', e)
    notices.value = []
    unreadCount.value = 0
  }
}

function goToAI() {
  uni.navigateTo({ url: '/pages/ai/chat' })
}

function goToMedicine() {
  uni.switchTab({ url: '/pages/medicine/today' })
}

function goToHealth() {
  uni.switchTab({ url: '/pages/health/index' })
}

/** 紧急呼救 */
function emergencyHelp() {
  uni.showModal({
    title: '紧急呼救',
    content: '确定要发送紧急求助吗？将通知所有绑定家属！',
    confirmText: '确定求助',
    cancelText: '取消',
    confirmColor: '#FF6B6B',
    success: (res) => {
      if (res.confirm) {
        uni.vibrateLong()
        uni.showToast({ title: '已发送求助信息', icon: 'success', duration: 2000 })
      }
    }
  })
}

onMounted(() => {
  loadTodayTasks()
  loadHealthData()
  loadNotices()
})
</script>

<style lang="scss" scoped>
@import '../../styles/common.scss';

.home-container {
  min-height: 100vh;
  background: $color-bg;
  padding: $elder-spacing-xl;
  padding-bottom: 120rpx;
}

// ========== 欢迎区域 ==========
.header-section {
  text-align: center;
  margin-bottom: $elder-spacing-xl;
}

.welcome-text {
  display: block;
  font-size: $elder-font-xl;
  font-weight: bold;
  color: $color-text;
  margin-bottom: $elder-spacing-sm;
}

.date-text {
  display: block;
  font-size: $elder-font-medium;
  color: $color-text-light;
}

// ========== 通用卡片样式 ==========
.medicine-card,
.health-card,
.notice-card {
  background: $color-white;
  border-radius: $elder-radius-lg;
  padding: $elder-spacing-lg;
  margin-bottom: $elder-spacing;
  box-shadow: $elder-shadow;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $elder-spacing;
}

.card-title {
  font-size: $elder-font-large;
  font-weight: bold;
  color: $color-text;
}

.card-count {
  font-size: $elder-font-medium;
  color: $color-warning;
  font-weight: bold;
}

.card-btn {
  width: 100%;
  height: $elder-btn-height;
  line-height: $elder-btn-height;
  border-radius: 44rpx;
  font-size: $elder-font-medium;
  font-weight: bold;
  border: none;
  color: #fff;
  margin-top: $elder-spacing-sm;
}

.primary-btn {
  background: linear-gradient(135deg, $color-primary 0%, #36cfc9 100%);
}

// ========== 今日用药卡片 ==========
.medicine-card {
  border-left: 8rpx solid $color-warning;
}

.task-list {
  margin-bottom: $elder-spacing-sm;
}

.task-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 2rpx solid #f0f0f0;

  &:last-child { border-bottom: none; }
}

.task-time {
  font-size: $elder-font-medium;
  color: $color-primary;
  font-weight: bold;
  width: 120rpx;
}

.task-name {
  flex: 1;
  font-size: $elder-font-medium;
  color: $color-text;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-status {
  font-size: $elder-font-large;
}

// ========== 空状态 ==========
.empty-card {
  text-align: center;
  padding: 60rpx $elder-spacing-lg;
}

.empty-icon {
  font-size: 80rpx;
  display: block;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: $elder-font-medium;
  color: $color-text-placeholder;
}

// ========== 健康数据卡片 ==========
.health-card {
  border-left: 8rpx solid $color-success;
}

.health-grid {
  display: flex;
  gap: $elder-spacing-sm;
  margin-bottom: $elder-spacing;
}

.health-item {
  flex: 1;
  background: #f7fcff;
  border-radius: $elder-radius;
  padding: $elder-spacing;
  text-align: center;
}

.health-label {
  display: block;
  font-size: $elder-font-base;
  color: $color-text-light;
  margin-bottom: 8rpx;
}

.health-value {
  display: block;
  font-size: $elder-font-large;
  color: $color-text;
  font-weight: bold;
  margin-bottom: 4rpx;
}

.unit {
  font-size: $elder-font-small;
  font-weight: normal;
  color: $color-text-placeholder;
}

.health-status {
  font-size: $elder-font-small;

  &.normal { color: $color-success; }
  &.warning { color: $color-warning; }
}

.health-btn {
  background: $color-success;
}

// ========== 通知卡片 ==========
.notice-card {
  border-left: 8rpx solid $color-primary;
}

.notice-badge {
  font-size: $elder-font-small;
  color: #fff;
  background: $color-danger;
  border-radius: 20rpx;
  padding: 4rpx 16rpx;
}

.notice-list {
  margin-top: $elder-spacing-sm;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  padding: 16rpx 0;

  &:not(:last-child) {
    border-bottom: 2rpx solid #f0f0f0;
  }
}

.notice-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: $color-border;
  margin-top: 12rpx;
  margin-right: 16rpx;
  flex-shrink: 0;

  &.unread {
    background: $color-danger;
  }
}

.notice-body {
  flex: 1;
}

.notice-title {
  display: block;
  font-size: $elder-font-base;
  color: $color-text;
  font-weight: 600;
  margin-bottom: 4rpx;
}

.notice-content {
  display: block;
  font-size: $elder-font-small;
  color: $color-text-light;
  line-height: 1.5;
}

// ========== 快捷入口 ==========
.quick-actions {
  display: flex;
  gap: 16rpx;
  margin-top: $elder-spacing-xl;
}

.quick-action {
  flex: 1;
  background: $color-white;
  border-radius: $elder-radius-lg;
  padding: 28rpx 0;
  text-align: center;
  box-shadow: $elder-shadow;

  &:active { background: #f8f9fb; }
}

.qa-icon { font-size: 44rpx; display: block; margin-bottom: 8rpx; }

.qa-label {
  font-size: 24rpx;
  color: $color-text;
  font-weight: 600;
}

.sos-action {
  background: linear-gradient(135deg, #fff5f5 0%, #fff0f0 100%);
  border: 2rpx solid #ffcccc;

  .qa-label { color: $color-danger; }
}
</style>
