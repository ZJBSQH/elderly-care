<template>
  <view class="page-container">
    <view class="page-header">
      <text class="page-title">健康历史记录</text>
      <text class="page-desc">查看您的健康趋势变化</text>
    </view>

    <!-- 日期筛选 -->
    <view class="date-row">
      <picker mode="date" :value="startDate" @change="onStartChange">
        <view class="date-picker"><text>{{ startDate || '开始日期' }}</text></view>
      </picker>
      <text class="date-sep">至</text>
      <picker mode="date" :value="endDate" @change="onEndChange">
        <view class="date-picker"><text>{{ endDate || '结束日期' }}</text></view>
      </picker>
      <button class="search-btn" @click="reload">查询</button>
    </view>

    <!-- 列表 -->
    <view class="record-list" v-if="records.length > 0">
      <view
        class="record-item"
        :class="{ warning: r.warningFlag === 1 }"
        v-for="r in records"
        :key="r.id"
      >
        <view class="record-header">
          <text class="record-time">{{ formatDateTime(r.recordTime) }}</text>
          <text class="record-flag" v-if="r.warningFlag === 1">⚠️ 异常</text>
          <text class="record-flag normal" v-else>✅ 正常</text>
        </view>
        <view class="record-metrics">
          <view class="rm-item" v-if="r.bloodPressure">
            <text class="rm-label">血压</text>
            <text class="rm-value">{{ r.bloodPressure }}</text>
          </view>
          <view class="rm-item" v-if="r.bloodSugar !== null && r.bloodSugar !== undefined">
            <text class="rm-label">血糖</text>
            <text class="rm-value">{{ r.bloodSugar }}</text>
          </view>
          <view class="rm-item" v-if="r.heartRate">
            <text class="rm-label">心率</text>
            <text class="rm-value">{{ r.heartRate }}</text>
          </view>
          <view class="rm-item" v-if="r.weight !== null && r.weight !== undefined">
            <text class="rm-label">体重</text>
            <text class="rm-value">{{ r.weight }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-card" v-else-if="!loading">
      <text class="empty-icon">📋</text>
      <text class="empty-text">此时间段暂无健康记录</text>
    </view>

    <!-- 加载更多 -->
    <view class="load-more" v-if="hasMore">
      <text class="load-text" @click="loadMore">加载更多</text>
    </view>
    <view class="load-end" v-else-if="records.length > 0">
      <text class="load-text">— 没有更多了 —</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../store/user.js'
import { getHealthHistory } from '../../api/health.js'

const userStore = useUserStore()

const records = ref([])
const loading = ref(false)
const hasMore = ref(true)

const today = new Date()
const thirtyAgo = new Date(today.getTime() - 30 * 24 * 3600 * 1000)
const startDate = ref(formatDateStr(thirtyAgo))
const endDate = ref(formatDateStr(today))

let pageNum = 1
const pageSize = 20

function formatDateStr(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function formatDateTime(dt) {
  if (!dt) return ''
  const s = String(dt)
  return s.substring(0, 16).replace('T', ' ')
}

function onStartChange(e) { startDate.value = e.detail.value }
function onEndChange(e) { endDate.value = e.detail.value }

async function loadRecords(isLoadMore = false) {
  if (loading.value) return
  loading.value = true

  if (!isLoadMore) {
    pageNum = 1
    records.value = []
  }

  try {
    const elderId = userStore.elderId
    if (!elderId) { loading.value = false; return }

    const params = {
      elderId,
      pageNum,
      pageSize
    }
    if (startDate.value) params.startDate = startDate.value + 'T00:00:00'
    if (endDate.value) params.endDate = endDate.value + 'T23:59:59'

    const data = await getHealthHistory(params)
    const list = Array.isArray(data) ? data : []

    if (isLoadMore) {
      records.value = [...records.value, ...list]
    } else {
      records.value = list
    }

    hasMore.value = list.length >= pageSize
    pageNum++
  } catch (e) {
    console.error('加载健康记录失败:', e)
    uni.showToast({ title: '加载失败', icon: 'none', duration: 2000 })
  } finally {
    loading.value = false
  }
}

function reload() { loadRecords(false) }
function loadMore() { loadRecords(true) }

onMounted(() => loadRecords())
</script>

<style lang="scss" scoped>
@import '../../styles/common.scss';

.page-container {
  min-height: 100vh;
  background: $color-bg;
  padding: $elder-spacing-xl;
  padding-bottom: 160rpx;
}

.page-header {
  text-align: center;
  padding: 20rpx 0 30rpx;
}

.page-title { display: block; font-size: $elder-font-xl; font-weight: bold; color: $color-text; margin-bottom: 8rpx; }
.page-desc { display: block; font-size: $elder-font-base; color: $color-text-placeholder; }

// ========== 日期筛选 ==========
.date-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: $elder-spacing;
}

.date-picker {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  text-align: center;
  background: $color-white;
  border-radius: $elder-radius;
  font-size: $elder-font-small;
  color: $color-text;
  box-shadow: $elder-shadow;
  padding: 0 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.date-sep { font-size: $elder-font-small; color: $color-text-placeholder; flex-shrink: 0; }

.search-btn {
  height: 72rpx;
  line-height: 72rpx;
  background: $color-primary;
  color: #fff;
  border-radius: $elder-radius;
  font-size: $elder-font-small;
  border: none;
  padding: 0 28rpx;
  flex-shrink: 0;
}

// ========== 记录列表 ==========
.record-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.record-item {
  background: $color-white;
  border-radius: $elder-radius-lg;
  padding: 24rpx;
  box-shadow: $elder-shadow;
  border-left: 8rpx solid $color-success;

  &.warning {
    border-left-color: $color-warning;
    background: #fffdf5;
  }
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.record-time {
  font-size: $elder-font-small;
  color: $color-text-light;
}

.record-flag {
  font-size: 24rpx;
  font-weight: 600;
  color: $color-warning;

  &.normal { color: $color-success; }
}

.record-metrics {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.rm-item {
  background: #f7fcff;
  border-radius: 12rpx;
  padding: 12rpx 20rpx;
  text-align: center;
}

.rm-label {
  display: block;
  font-size: 22rpx;
  color: $color-text-placeholder;
}

.rm-value {
  display: block;
  font-size: $elder-font-base;
  font-weight: 600;
  color: $color-text;
}

// ========== 空状态 ==========
.empty-card { text-align: center; padding: 120rpx 0; }
.empty-icon { font-size: 100rpx; display: block; margin-bottom: 24rpx; }
.empty-text { font-size: $elder-font-medium; color: $color-text-placeholder; }

// ========== 加载更多 ==========
.load-more, .load-end { text-align: center; padding: 32rpx 0; }
.load-text { font-size: $elder-font-small; color: $color-text-placeholder; }
.load-more .load-text { color: $color-primary; }
</style>
