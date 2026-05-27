<template>
  <view class="page-container">
    <!-- 老人基本信息 -->
    <view class="elder-header">
      <view class="avatar">
        <text class="avatar-text">{{ getInitial(elderName) }}</text>
      </view>
      <text class="elder-name">{{ elderName || '长辈' }}</text>
    </view>

    <!-- 最新健康数据 -->
    <view class="card">
      <text class="card-title">📊 最新健康数据</text>
      <view class="metrics-row" v-if="healthData">
        <view class="metric" :class="{ warn: healthData.warningFlag === 1 }">
          <text class="m-label">血压</text>
          <text class="m-value">{{ healthData.bloodPressure || '--' }}</text>
        </view>
        <view class="metric" :class="{ warn: healthData.warningFlag === 1 }">
          <text class="m-label">血糖</text>
          <text class="m-value">{{ formatNum(healthData.bloodSugar) }}</text>
        </view>
        <view class="metric" :class="{ warn: healthData.warningFlag === 1 }">
          <text class="m-label">心率</text>
          <text class="m-value">{{ healthData.heartRate || '--' }}</text>
        </view>
        <view class="metric">
          <text class="m-label">体重</text>
          <text class="m-value">{{ formatNum(healthData.weight) }}</text>
        </view>
      </view>
      <view class="no-data" v-else>
        <text>暂无健康数据</text>
      </view>
      <text class="record-time" v-if="healthData?.recordTime">
        记录时间：{{ formatDt(healthData.recordTime) }}
      </text>
    </view>

    <!-- 今日服药记录 -->
    <view class="card">
      <text class="card-title">💊 今日服药情况</text>
      <view class="record-list" v-if="todayRecords.length > 0">
        <view
          class="record-item"
          :class="{ taken: r.status === 1, missed: r.status === 0 }"
          v-for="r in todayRecords"
          :key="r.id"
        >
          <text class="r-status">{{ r.status === 1 ? '✅' : '⚠️' }}</text>
          <text class="r-time">{{ formatTime(r.recordTime) }}</text>
          <text class="r-status-text">{{ r.status === 1 ? '已服' : '漏服' }}</text>
        </view>
      </view>
      <view class="no-data" v-else>
        <text>暂无服药记录</text>
      </view>
    </view>

    <!-- 用药计划 -->
    <view class="card">
      <text class="card-title">📋 用药计划</text>
      <view class="plan-list" v-if="medicinePlans.length > 0">
        <view class="plan-item" v-for="m in medicinePlans.slice(0, 5)" :key="m.id">
          <view class="plan-body">
            <text class="plan-name">{{ m.medicineName }}</text>
            <text class="plan-meta">{{ m.dosage }} · {{ m.frequency }}</text>
          </view>
          <text class="plan-time" v-if="m.remindTime">{{ formatTime(m.remindTime) }}</text>
        </view>
      </view>
      <view class="no-data" v-else>
        <text>暂无用药计划</text>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="actions">
      <button class="act-btn" @click="goToMedicine">💊 管理用药计划</button>
      <button class="act-btn secondary" @click="goToHistory">📋 查看健康历史</button>
      <button class="act-btn secondary" @click="callElder">📞 联系老人</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getLatestHealth } from '../../api/health.js'
import { getElderTodayRecords, getElderMedicinePlan } from '../../api/family.js'
import { getUserInfo } from '../../api/auth.js'

const elderId = ref(null)
const elderName = ref('')
const healthData = ref(null)
const todayRecords = ref([])
const medicinePlans = ref([])
const elderPhone = ref('')

function getInitial(name) {
  return name ? name.charAt(0) : '长'
}

function formatNum(val) {
  if (val === null || val === undefined || val === '') return '--'
  return String(val)
}

function formatTime(t) {
  if (!t) return ''
  return String(t).substring(0, 5)
}

function formatDt(dt) {
  if (!dt) return ''
  return String(dt).substring(0, 16).replace('T', ' ')
}

async function loadData() {
  if (!elderId.value) return
  try {
    const [health, records, plans] = await Promise.all([
      getLatestHealth({ elderId: elderId.value }).catch(() => null),
      getElderTodayRecords(elderId.value).catch(() => []),
      getElderMedicinePlan(elderId.value).catch(() => [])
    ])
    healthData.value = health
    todayRecords.value = Array.isArray(records) ? records : []
    medicinePlans.value = Array.isArray(plans) ? plans : []
  } catch (e) {
    console.error('加载老人详情失败:', e)
  }
}

function goToMedicine() {
  uni.navigateTo({ url: `/pages/family/remote-medicine?elderId=${elderId.value}` })
}

function goToHistory() {
  uni.navigateTo({
    url: `/pages/health/history?elderId=${elderId.value}`
  })
}

function callElder() {
  if (!elderPhone.value) {
    return uni.showToast({ title: '暂无联系方式', icon: 'none', duration: 2000 })
  }
  uni.makePhoneCall({ phoneNumber: elderPhone.value })
}

onMounted(() => {
  const pages = getCurrentPages()
  const opts = pages[pages.length - 1]?.$page?.options || {}
  elderId.value = parseInt(opts.elderId, 10) || null
  elderName.value = decodeURIComponent(opts.name || '')
  loadData()
})
</script>

<style lang="scss" scoped>
@import '../../styles/common.scss';

.page-container {
  min-height: 100vh;
  background: $color-bg;
  padding-bottom: 160rpx;
}

// ========== 老人头部 ==========
.elder-header {
  background: linear-gradient(135deg, #5B8FF9 0%, #36cfc9 100%);
  padding: 48rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #fff;
}

.avatar {
  width: 110rpx;
  height: 110rpx;
  background: rgba(255,255,255,0.25);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid rgba(255,255,255,0.5);
  margin-bottom: 16rpx;
}

.avatar-text { font-size: 48rpx; font-weight: bold; color: #fff; }

.elder-name {
  font-size: $elder-font-xl;
  font-weight: bold;
}

// ========== 通用卡片 ==========
.card {
  background: $color-white;
  border-radius: $elder-radius-lg;
  padding: $elder-spacing-lg;
  margin: $elder-spacing $elder-spacing-xl;
  box-shadow: $elder-shadow;
}

.card-title {
  font-size: $elder-font-large;
  font-weight: bold;
  color: $color-text;
  margin-bottom: 20rpx;
  display: block;
}

// ========== 指标网格 ==========
.metrics-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
}

.metric {
  background: #f7fcff;
  border-radius: $elder-radius;
  padding: 20rpx;
  text-align: center;
  border: 2rpx solid transparent;

  &.warn { background: #fff7e6; border-color: $color-warning; }
}

.m-label { display: block; font-size: 24rpx; color: $color-text-placeholder; margin-bottom: 6rpx; }
.m-value { display: block; font-size: $elder-font-base; font-weight: bold; color: $color-text; }

.record-time {
  display: block;
  font-size: 24rpx;
  color: $color-text-placeholder;
  margin-top: 16rpx;
  text-align: right;
}

// ========== 服药记录列表 ==========
.record-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.record-item {
  display: flex;
  align-items: center;
  padding: 16rpx;
  border-radius: $elder-radius;
  gap: 12rpx;

  &.taken { background: #f6ffed; }
  &.missed { background: #fff7e6; }
}

.r-status { font-size: 32rpx; }
.r-time { flex: 1; font-size: $elder-font-base; color: $color-text; font-weight: 600; }
.r-status-text { font-size: $elder-font-small; }

.record-item.taken .r-status-text { color: $color-success; }
.record-item.missed .r-status-text { color: $color-warning; }

// ========== 用药计划列表 ==========
.plan-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.plan-item {
  display: flex;
  align-items: center;
  padding: 16rpx;
  background: #f7fcff;
  border-radius: $elder-radius;
}

.plan-body { flex: 1; }
.plan-name { display: block; font-size: $elder-font-base; font-weight: 600; color: $color-text; }
.plan-meta { display: block; font-size: 24rpx; color: $color-text-placeholder; margin-top: 4rpx; }

.plan-time {
  font-size: $elder-font-base;
  font-weight: bold;
  color: $color-primary;
}

// ========== 空状态 ==========
.no-data {
  text-align: center;
  padding: 32rpx 0;
  font-size: $elder-font-small;
  color: $color-text-placeholder;
}

// ========== 操作按钮 ==========
.actions {
  padding: $elder-spacing $elder-spacing-xl;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.act-btn {
  width: 100%;
  height: $elder-btn-height;
  line-height: $elder-btn-height;
  background: linear-gradient(135deg, $color-primary 0%, #36cfc9 100%);
  color: #fff;
  border-radius: $elder-radius-lg;
  font-size: $elder-font-medium;
  font-weight: bold;
  border: none;

  &.secondary {
    background: $color-white;
    color: $color-text;
    border: 2rpx solid $color-border;
  }
}
</style>
