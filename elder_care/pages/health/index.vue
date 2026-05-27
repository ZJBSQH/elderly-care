<template>
  <view class="page-container">
    <!-- 头部 -->
    <view class="header-card">
      <text class="header-date">{{ currentDate }}</text>
      <text class="header-label">健康数据概览</text>
      <view class="alert-badge" v-if="unreadAlerts > 0" @click="goToAlerts">
        <text>{{ unreadAlerts }}条未读预警</text>
      </view>
    </view>

    <!-- 最新数据指标 -->
    <view class="metrics-card">
      <text class="card-title">📊 最新测量数据</text>
      <view class="metrics-grid" v-if="latestData">
        <!-- 血压 -->
        <view class="metric-item" :class="{ warning: bpWarning }">
          <text class="metric-icon">🩸</text>
          <text class="metric-value">{{ latestData.bloodPressure || '--' }}</text>
          <text class="metric-unit">mmHg</text>
          <text class="metric-label">血压</text>
          <text class="metric-status" :class="bpWarning ? 'warn' : 'ok'">
            {{ bpWarning ? '异常' : '正常' }}
          </text>
        </view>
        <!-- 血糖 -->
        <view class="metric-item" :class="{ warning: sugarWarning }">
          <text class="metric-icon">🩸</text>
          <text class="metric-value">{{ formatNumber(latestData.bloodSugar) }}</text>
          <text class="metric-unit">mmol/L</text>
          <text class="metric-label">血糖</text>
          <text class="metric-status" :class="sugarWarning ? 'warn' : 'ok'">
            {{ sugarWarning ? '异常' : '正常' }}
          </text>
        </view>
        <!-- 心率 -->
        <view class="metric-item" :class="{ warning: hrWarning }">
          <text class="metric-icon">💓</text>
          <text class="metric-value">{{ latestData.heartRate || '--' }}</text>
          <text class="metric-unit">bpm</text>
          <text class="metric-label">心率</text>
          <text class="metric-status" :class="hrWarning ? 'warn' : 'ok'">
            {{ hrWarning ? '异常' : '正常' }}
          </text>
        </view>
        <!-- 体重 -->
        <view class="metric-item">
          <text class="metric-icon">⚖️</text>
          <text class="metric-value">{{ formatNumber(latestData.weight) }}</text>
          <text class="metric-unit">kg</text>
          <text class="metric-label">体重</text>
        </view>
      </view>
      <view class="no-data" v-else>
        <text class="no-data-icon">📋</text>
        <text class="no-data-text">暂无健康数据，请录入</text>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="actions-card">
      <view class="action-item" @click="goToRecord">
        <text class="action-icon">📝</text>
        <text class="action-label">录入数据</text>
      </view>
      <view class="action-item" @click="goToHistory">
        <text class="action-icon">📋</text>
        <text class="action-label">历史记录</text>
      </view>
      <view class="action-item" @click="goToTrend">
        <text class="action-icon">📈</text>
        <text class="action-label">趋势图表</text>
      </view>
    </view>

    <!-- 预警列表 -->
    <view class="alerts-card" v-if="alerts.length > 0">
      <view class="card-header">
        <text class="card-title">⚠️ 健康预警</text>
        <text class="mark-all" @click="markAllRead">全部已读</text>
      </view>
      <view
        class="alert-item"
        :class="{ unread: !a.isRead }"
        v-for="a in alerts.slice(0, 3)"
        :key="a.id"
        @click="handleAlertClick(a)"
      >
        <view class="alert-dot" v-if="!a.isRead"></view>
        <view class="alert-body">
          <text class="alert-type">{{ a.abnormalType }}</text>
          <text class="alert-desc">{{ a.abnormalDetail }}</text>
        </view>
        <text class="alert-time">{{ formatDateTime(a.warningTime) }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../store/user.js'
import { getLatestHealth, getUnreadWarnings, getAlertList, markAlertRead, markAllAlertsRead } from '../../api/health.js'

const userStore = useUserStore()

const currentDate = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}年${String(d.getMonth() + 1).padStart(2, '0')}月${String(d.getDate()).padStart(2, '0')}日`
})

const latestData = ref(null)
const unreadAlerts = ref(0)
const alerts = ref([])

// 单项是否异常（latestData.warningFlag 是整体标记，这里简化为统一展示）
const bpWarning = computed(() => latestData.value?.warningFlag === 1)
const sugarWarning = computed(() => latestData.value?.warningFlag === 1)
const hrWarning = computed(() => latestData.value?.warningFlag === 1)

function formatNumber(val) {
  if (val === null || val === undefined || val === '') return '--'
  return String(val)
}

function formatDateTime(dt) {
  if (!dt) return ''
  const s = String(dt)
  return s.substring(11, 16) // HH:mm
}

/** 加载最新数据 */
async function loadLatest() {
  try {
    const elderId = userStore.elderId
    if (!elderId) return
    latestData.value = await getLatestHealth({ elderId })
  } catch (e) {
    console.error('加载健康数据失败:', e)
    latestData.value = null
  }
}

/** 加载预警 — 基于健康记录中 warning_flag=1 的数据 */
async function loadAlerts() {
  try {
    const elderId = userStore.elderId
    if (!elderId) return
    unreadAlerts.value = await getUnreadWarnings({ elderId }) || 0
    alerts.value = await getAlertList({ elderId }) || []
  } catch (e) {
    console.error('加载预警失败:', e)
    unreadAlerts.value = 0
    alerts.value = []
  }
}

/** 点击预警 → 标记已读 */
async function handleAlertClick(alert) {
  if (!alert.isRead) {
    try {
      await markAlertRead(alert.id)
      alert.isRead = true
      unreadAlerts.value = Math.max(0, unreadAlerts.value - 1)
    } catch (e) { /* ignore */ }
  }
}

/** 全部已读 */
async function markAllRead() {
  try {
    const elderId = userStore.elderId
    if (!elderId) return
    await markAllAlertsRead(elderId)
    alerts.value.forEach(a => { a.isRead = true })
    unreadAlerts.value = 0
    uni.showToast({ title: '已全部标记已读', icon: 'success', duration: 1500 })
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none', duration: 2000 })
  }
}

function goToRecord() {
  uni.navigateTo({ url: '/pages/health/record' })
}
function goToHistory() {
  uni.navigateTo({ url: '/pages/health/history' })
}
function goToTrend() {
  uni.navigateTo({ url: '/pages/health/trend' })
}
function goToAlerts() {
  // 直接滚动到预警区域
}

onMounted(() => {
  loadLatest()
  loadAlerts()
})
</script>

<style lang="scss" scoped>
@import '../../styles/common.scss';

.page-container {
  min-height: 100vh;
  background: $color-bg;
  padding: $elder-spacing-xl;
  padding-bottom: 120rpx;
}

// ========== 头部 ==========
.header-card {
  background: linear-gradient(135deg, $color-success 0%, #36cfc9 100%);
  border-radius: $elder-radius-lg;
  padding: 36rpx $elder-spacing-lg;
  margin-bottom: $elder-spacing;
  color: #fff;
  text-align: center;
}

.header-date {
  display: block;
  font-size: $elder-font-small;
  opacity: 0.85;
}

.header-label {
  display: block;
  font-size: $elder-font-large;
  font-weight: bold;
  margin-top: 8rpx;
}

.alert-badge {
  display: inline-block;
  margin-top: 12rpx;
  background: $color-danger;
  border-radius: 24rpx;
  padding: 6rpx 24rpx;
  font-size: $elder-font-small;
}

// ========== 指标卡片 ==========
.metrics-card {
  background: $color-white;
  border-radius: $elder-radius-lg;
  padding: $elder-spacing-lg;
  margin-bottom: $elder-spacing;
  box-shadow: $elder-shadow;
}

.card-title {
  font-size: $elder-font-large;
  font-weight: bold;
  color: $color-text;
  margin-bottom: $elder-spacing;
  display: block;
}

.metrics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
}

.metric-item {
  background: #f7fcff;
  border-radius: $elder-radius;
  padding: 24rpx;
  text-align: center;
  border: 2rpx solid transparent;

  &.warning {
    background: #fff7e6;
    border-color: $color-warning;
  }
}

.metric-icon {
  font-size: 36rpx;
  display: block;
  margin-bottom: 8rpx;
}

.metric-value {
  display: block;
  font-size: 40rpx;
  font-weight: bold;
  color: $color-text;
}

.metric-unit {
  display: block;
  font-size: 22rpx;
  color: $color-text-placeholder;
}

.metric-label {
  display: block;
  font-size: $elder-font-small;
  color: $color-text-light;
  margin-top: 8rpx;
}

.metric-status {
  display: inline-block;
  font-size: 22rpx;
  padding: 2rpx 16rpx;
  border-radius: 12rpx;
  margin-top: 4rpx;

  &.ok { background: #f6ffed; color: $color-success; }
  &.warn { background: #fff7e6; color: $color-warning; }
}

// ========== 无数据 ==========
.no-data {
  text-align: center;
  padding: 40rpx 0;
}

.no-data-icon {
  font-size: 80rpx;
  display: block;
  margin-bottom: 16rpx;
}

.no-data-text {
  font-size: $elder-font-base;
  color: $color-text-placeholder;
}

// ========== 快捷操作 ==========
.actions-card {
  display: flex;
  gap: 20rpx;
  margin-bottom: $elder-spacing;
}

.action-item {
  flex: 1;
  background: $color-white;
  border-radius: $elder-radius-lg;
  padding: 28rpx;
  text-align: center;
  box-shadow: $elder-shadow;
}

.action-icon {
  font-size: 48rpx;
  display: block;
  margin-bottom: 8rpx;
}

.action-label {
  font-size: $elder-font-small;
  color: $color-text;
  font-weight: 600;
}

// ========== 预警卡片 ==========
.alerts-card {
  background: $color-white;
  border-radius: $elder-radius-lg;
  padding: $elder-spacing-lg;
  box-shadow: $elder-shadow;
  border-left: 8rpx solid $color-warning;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.mark-all {
  font-size: $elder-font-small;
  color: $color-primary;
}

.alert-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 2rpx solid #f5f5f5;

  &:last-child { border-bottom: none; }

  &.unread { background: #fafafa; margin: 0 -24rpx; padding: 20rpx 24rpx; border-radius: 8rpx; }
}

.alert-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: $color-danger;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.alert-body {
  flex: 1;
}

.alert-type {
  display: block;
  font-size: $elder-font-base;
  font-weight: 600;
  color: $color-text;
}

.alert-desc {
  display: block;
  font-size: $elder-font-small;
  color: $color-text-light;
  margin-top: 4rpx;
}

.alert-time {
  font-size: $elder-font-small;
  color: $color-text-placeholder;
  flex-shrink: 0;
}
</style>
