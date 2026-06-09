<template>
  <view class="page-container">
    <!-- 头部 -->
    <view class="page-header">
      <text class="page-title">用药记录</text>
      <text class="page-desc">查看历史服药情况</text>
    </view>

    <!-- 状态筛选 -->
    <view class="filter-tabs">
      <view
        class="tab-item"
        :class="{ active: filterStatus === null }"
        @click="filterStatus = null; reload()"
      >全部</view>
      <view
        class="tab-item"
        :class="{ active: filterStatus === 1 }"
        @click="filterStatus = 1; reload()"
      >✅ 已服</view>
      <view
        class="tab-item"
        :class="{ active: filterStatus === 2 }"
        @click="filterStatus = 2; reload()"
      >⚠️ 漏服</view>
    </view>

    <!-- 日期范围 -->
    <view class="date-row">
      <picker mode="date" :value="startDate" @change="onStartChange">
        <view class="date-picker">
          <text>{{ startDate || '开始日期' }}</text>
        </view>
      </picker>
      <text class="date-sep">至</text>
      <picker mode="date" :value="endDate" @change="onEndChange">
        <view class="date-picker">
          <text>{{ endDate || '结束日期' }}</text>
        </view>
      </picker>
      <button class="search-btn" @click="reload">查询</button>
    </view>

    <!-- 记录列表 -->
    <view class="record-list" v-if="records.length > 0">
      <view
        class="record-item"
        :class="{ taken: r.status === 1, missed: r.status === 2 }"
        v-for="r in records"
        :key="r.id"
      >
        <view class="record-left">
          <view class="record-status-icon">
            {{ r.status === 1 ? '✅' : r.status === 2 ? '⚠️' : '⏳' }}
          </view>
        </view>
        <view class="record-body">
          <text class="record-name">{{ r.medicineName || '药品' }}</text>
          <text class="record-meta">
            {{ r.dosage || '' }} · {{ r.frequency || '' }}
          </text>
        </view>
        <view class="record-right">
          <text class="record-date">{{ formatDate(r.remindDate) }}</text>
          <text class="record-status-text" :class="r.status === 1 ? 'status-taken' : 'status-missed'">
            {{ r.statusText }}
          </text>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-card" v-else-if="!loading">
      <text class="empty-icon">📋</text>
      <text class="empty-text">暂无服药记录</text>
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
import { getMedicineExport } from '../../api/medicine.js'

const userStore = useUserStore()

const records = ref([])
const filterStatus = ref(null)
const loading = ref(false)

// 日期默认最近30天
const today = new Date()
const thirtyAgo = new Date(today.getTime() - 30 * 24 * 3600 * 1000)
const startDate = ref(formatDateStr(thirtyAgo))
const endDate = ref(formatDateStr(today))

let pageNum = 1
let hasMore = ref(true)
const pageSize = 20

function formatDateStr(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const s = String(dateStr)
  return s.substring(5, 10) // MM-DD
}

function onStartChange(e) {
  startDate.value = e.detail.value
}

function onEndChange(e) {
  endDate.value = e.detail.value
}

/** 加载记录 */
async function loadRecords(isLoadMore = false) {
  if (loading.value) return
  loading.value = true

  if (!isLoadMore) {
    pageNum = 1
    records.value = []
  }

  try {
    const elderId = userStore.elderId
    if (!elderId) {
      loading.value = false
      return
    }

    const params = {
      elderId,
      pageNum,
      pageSize,
      startDate: startDate.value || undefined,
      endDate: endDate.value || undefined
    }
    if (filterStatus.value !== null) {
      params.status = filterStatus.value
    }

    const data = await getMedicineExport(params)
    const list = Array.isArray(data) ? data : []

    if (isLoadMore) {
      records.value = [...records.value, ...list]
    } else {
      records.value = list
    }

    hasMore.value = list.length >= pageSize
    pageNum++
  } catch (e) {
    console.error('加载用药记录失败:', e)
    uni.showToast({ title: '加载失败', icon: 'none', duration: 2000 })
  } finally {
    loading.value = false
  }
}

function reload() {
  loadRecords(false)
}

function loadMore() {
  loadRecords(true)
}

onMounted(() => {
  loadRecords()
})
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

.page-title {
  display: block;
  font-size: $elder-font-xl;
  font-weight: bold;
  color: $color-text;
  margin-bottom: 8rpx;
}

.page-desc {
  display: block;
  font-size: $elder-font-base;
  color: $color-text-placeholder;
}

// ========== 状态筛选 ==========
.filter-tabs {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.tab-item {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  text-align: center;
  background: $color-white;
  border-radius: $elder-radius;
  font-size: $elder-font-small;
  color: $color-text-light;
  box-shadow: $elder-shadow;

  &.active {
    background: $color-primary;
    color: #fff;
    font-weight: bold;
  }
}

// ========== 日期选择 ==========
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
  padding: 0 16rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.date-sep {
  font-size: $elder-font-small;
  color: $color-text-placeholder;
  flex-shrink: 0;
}

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
  background: $color-white;
  border-radius: $elder-radius-lg;
  overflow: hidden;
  box-shadow: $elder-shadow;
}

.record-item {
  display: flex;
  align-items: center;
  padding: 24rpx $elder-spacing;
  border-bottom: 2rpx solid #f5f5f5;

  &:last-child { border-bottom: none; }
}

.record-left {
  margin-right: 20rpx;
}

.record-status-icon {
  font-size: 44rpx;
}

.record-body {
  flex: 1;
}

.record-name {
  display: block;
  font-size: $elder-font-base;
  font-weight: 600;
  color: $color-text;
  margin-bottom: 4rpx;
}

.record-meta {
  display: block;
  font-size: $elder-font-small;
  color: $color-text-placeholder;
}

.record-right {
  text-align: right;
  flex-shrink: 0;
}

.record-date {
  display: block;
  font-size: $elder-font-small;
  color: $color-text-light;
  margin-bottom: 4rpx;
}

.record-status-text {
  font-size: $elder-font-small;
  font-weight: 600;
}

.status-taken {
  color: $color-success;
}

.status-missed {
  color: $color-warning;
}

// ========== 空状态 ==========
.empty-card {
  text-align: center;
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 100rpx;
  display: block;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: $elder-font-medium;
  color: $color-text-placeholder;
}

// ========== 加载更多 ==========
.load-more,
.load-end {
  text-align: center;
  padding: 32rpx 0;
}

.load-text {
  font-size: $elder-font-small;
  color: $color-text-placeholder;
}

.load-more .load-text {
  color: $color-primary;
}
</style>
