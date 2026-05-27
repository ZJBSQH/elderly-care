<template>
  <view class="page-container">
    <!-- 头部汇总 -->
    <view class="header-card">
      <view class="header-top">
        <text class="header-date">{{ currentDate }}</text>
        <button class="add-entry" @click="goToAdd">+ 添加</button>
      </view>
      <view class="header-summary">
        <view class="summary-item done">
          <text class="summary-num">{{ takenCount }}</text>
          <text class="summary-label">已服</text>
        </view>
        <view class="summary-item pending">
          <text class="summary-num">{{ pendingCount }}</text>
          <text class="summary-label">待服</text>
        </view>
        <view class="summary-item missed">
          <text class="summary-num">{{ missedCount }}</text>
          <text class="summary-label">漏服</text>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-card" v-if="groupedTasks.length === 0">
      <text class="empty-icon">✅</text>
      <text class="empty-text">今日暂无用药安排</text>
    </view>

    <!-- 按早中晚分组 -->
    <view class="period-group" v-for="group in groupedTasks" :key="group.label">
      <view class="period-header">
        <text class="period-icon">{{ group.icon }}</text>
        <text class="period-label">{{ group.label }}</text>
        <text class="period-count">{{ group.tasks.length }}项</text>
      </view>

      <view class="medicine-list">
        <view
          class="medicine-item"
          :class="{ taken: task._recordStatus === 'taken', missed: task._recordStatus === 'missed' }"
          v-for="task in group.tasks"
          :key="task.id"
        >
          <view class="med-info">
            <text class="med-name">{{ task.title || task.content }}</text>
            <text class="med-time">⏰ {{ formatTime(task.remindTime) }}</text>
          </view>

          <view class="med-actions">
            <text class="status-text" v-if="task._recordStatus === 'taken'">✅ 已服</text>
            <text class="status-text missed-text" v-else-if="task._recordStatus === 'missed'">⚠️ 漏服</text>
            <template v-else>
              <button class="take-btn" @click="handleTake(task.id)">确认服药</button>
              <text class="miss-link" @click="handleMiss(task.id)">漏服</text>
            </template>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部快捷入口 -->
    <view class="footer-links">
      <button class="link-btn" @click="goToAdd">+ 添加用药计划</button>
      <button class="link-btn outline" @click="goToRecord">📋 用药记录</button>
    </view>

    <!-- AI 助手悬浮球 -->
    <view class="ai-float-btn" @click="goToAI">
      <text class="ai-float-icon">💊</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { getTodayMedicine, takeMedicine, checkRecord, markMissed } from '../../api/medicine.js'
import { useMedicineReminder } from '../../composables/useMedicineReminder.js'

// 当前日期
const currentDate = computed(() => {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}年${m}月${day}日`
})

const tasks = ref([])
const recordMap = ref({}) // taskId → record status

const takenCount = computed(() => Object.values(recordMap.value).filter(s => s === 1).length)
const pendingCount = computed(() => {
  const recorded = Object.keys(recordMap.value).map(Number)
  return tasks.value.filter(t => !recorded.includes(t.id)).length
})
const missedCount = computed(() => Object.values(recordMap.value).filter(s => s === 0).length)

/** 判断时段 */
function getPeriod(remindTime) {
  if (!remindTime) return 'evening'
  const h = parseInt(String(remindTime).substring(0, 2), 10) || 0
  if (h < 10) return 'morning'
  if (h < 14) return 'noon'
  return 'evening'
}

const periods = [
  { key: 'morning', label: '早上', icon: '🌅' },
  { key: 'noon', label: '中午', icon: '☀️' },
  { key: 'evening', label: '晚上', icon: '🌙' }
]

const groupedTasks = computed(() => {
  const groups = { morning: [], noon: [], evening: [] }
  tasks.value.forEach(t => {
    const p = getPeriod(t.remindTime)
    if (groups[p]) {
      groups[p].push({
        ...t,
        _recordStatus: recordMap.value[t.id] === 1 ? 'taken'
          : recordMap.value[t.id] === 0 ? 'missed'
          : 'pending'
      })
    }
  })
  return periods
    .map(p => ({ ...p, tasks: groups[p.key] }))
    .filter(p => p.tasks.length > 0)
})

function formatTime(timeStr) {
  if (!timeStr) return ''
  return String(timeStr).substring(0, 5)
}

/** 加载今日任务和记录 */
async function loadData() {
  try {
    const result = await getTodayMedicine()
    tasks.value = Array.isArray(result) ? result : []

    // 为每个任务查记录
    for (const task of tasks.value) {
      try {
        const checkRes = await checkRecord(task.id)
        if (checkRes?.status !== undefined) {
          recordMap.value[task.id] = checkRes.status
        }
      } catch (e) { /* 无记录，保持 pending */ }
    }
  } catch (e) {
    console.error('加载用药数据失败:', e)
    tasks.value = []
  }
}

/** 确认服药 */
async function handleTake(taskId) {
  try {
    uni.showLoading({ title: '记录中...', mask: true })
    await takeMedicine(taskId)
    recordMap.value[taskId] = 1
    uni.hideLoading()
    uni.showToast({ title: '已记录服药', icon: 'success', duration: 1500 })
  } catch (e) {
    uni.hideLoading()
    uni.showToast({ title: e.message || '操作失败', icon: 'none', duration: 2000 })
  }
}

/** 标记漏服 */
async function handleMiss(taskId) {
  try {
    uni.showLoading({ title: '标记中...', mask: true })
    await markMissed(taskId)
    recordMap.value[taskId] = 0
    uni.hideLoading()
    uni.showToast({ title: '已标记漏服', icon: 'none', duration: 1500 })
  } catch (e) {
    uni.hideLoading()
    uni.showToast({ title: e.message || '操作失败', icon: 'none', duration: 2000 })
  }
}

function goToAdd() {
  uni.navigateTo({ url: '/pages/medicine/add' })
}

function goToRecord() {
  uni.navigateTo({ url: '/pages/medicine/record' })
}

function goToAI() {
  uni.navigateTo({ url: '/pages/medicine/ai-assistant' })
}

onMounted(loadData)

// 用药提醒：加载完任务后启动定时检查
const { startReminder, stopReminder, isReminding } = useMedicineReminder(tasks)
watch(tasks, (newTasks) => {
  if (newTasks.length > 0) startReminder()
}, { immediate: false })
</script>

<style lang="scss" scoped>
@import '../../styles/common.scss';

.page-container {
  min-height: 100vh;
  background: $color-bg;
  padding: $elder-spacing-xl;
  padding-bottom: 120rpx;
}

// ========== 头部汇总卡片 ==========
.header-card {
  background: linear-gradient(135deg, $color-primary 0%, #36cfc9 100%);
  border-radius: $elder-radius-lg;
  padding: 40rpx $elder-spacing-lg;
  margin-bottom: $elder-spacing-lg;
  text-align: center;
  color: #fff;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-date {
  font-size: $elder-font-base;
  opacity: 0.9;
}

.add-entry {
  height: 56rpx;
  line-height: 56rpx;
  background: rgba(255,255,255,0.25);
  color: #fff;
  border-radius: 28rpx;
  font-size: $elder-font-small;
  border: 2rpx solid rgba(255,255,255,0.5);
  padding: 0 20rpx;
}

.header-summary {
  display: flex;
  justify-content: space-around;
  margin-top: 24rpx;
}

.summary-item {
  text-align: center;
}

.summary-num {
  display: block;
  font-size: 56rpx;
  font-weight: bold;
}

.summary-label {
  display: block;
  font-size: $elder-font-small;
  opacity: 0.85;
  margin-top: 4rpx;
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

// ========== 时段分组 ==========
.period-group {
  margin-bottom: $elder-spacing-lg;
}

.period-header {
  display: flex;
  align-items: center;
  padding: 0 8rpx 16rpx;
}

.period-icon {
  font-size: 40rpx;
  margin-right: 12rpx;
}

.period-label {
  font-size: $elder-font-large;
  font-weight: bold;
  color: $color-text;
  flex: 1;
}

.period-count {
  font-size: $elder-font-small;
  color: $color-text-placeholder;
}

// ========== 药品列表 ==========
.medicine-list {
  background: $color-white;
  border-radius: $elder-radius-lg;
  overflow: hidden;
  box-shadow: $elder-shadow;
}

.medicine-item {
  display: flex;
  align-items: center;
  padding: 28rpx $elder-spacing;
  border-bottom: 2rpx solid #f5f5f5;

  &:last-child { border-bottom: none; }

  &.taken { background: #f6ffed; }
  &.missed { background: #fff7e6; }
}

.med-info {
  flex: 1;
}

.med-name {
  display: block;
  font-size: $elder-font-medium;
  font-weight: 600;
  color: $color-text;
  margin-bottom: 6rpx;
}

.med-time {
  font-size: $elder-font-small;
  color: $color-text-light;
}

.med-actions {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-shrink: 0;
}

.status-text {
  font-size: $elder-font-base;
  font-weight: 600;
  color: $color-success;
}

.missed-text {
  color: $color-warning;
}

.take-btn {
  height: 64rpx;
  line-height: 64rpx;
  background: $color-success;
  color: #fff;
  border-radius: 32rpx;
  font-size: $elder-font-small;
  font-weight: bold;
  border: none;
  padding: 0 24rpx;
}

.miss-link {
  font-size: $elder-font-small;
  color: $color-text-placeholder;
  padding: 8rpx;
}

// ========== 底部快捷入口 ==========
.footer-links {
  display: flex;
  gap: 20rpx;
  margin-top: $elder-spacing-xl;
}

.link-btn {
  flex: 1;
  height: $elder-btn-height;
  line-height: $elder-btn-height;
  text-align: center;
  background: $color-white;
  color: $color-primary;
  border-radius: $elder-radius-lg;
  font-size: $elder-font-base;
  font-weight: bold;
  border: 2rpx solid $color-primary;
  box-shadow: $elder-shadow;

  &.outline {
    background: $color-white;
    color: $color-text-light;
    border-color: $color-border;
  }
}

// ========== AI 悬浮球 ==========
.ai-float-btn {
  position: fixed;
  right: 36rpx;
  bottom: 200rpx;
  width: 104rpx;
  height: 104rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #5B8FF9 0%, #36cfc9 100%);
  box-shadow: 0 8rpx 28rpx rgba(91, 143, 249, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99;
  animation: ai-float 2.5s ease-in-out infinite;
}

.ai-float-icon {
  font-size: 52rpx;
}

@keyframes ai-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-14rpx); }
}
</style>
