<template>
  <view class="page-container">
    <view class="page-header">
      <text class="page-icon">📊</text>
      <text class="page-title">服药统计</text>
    </view>

    <!-- 周期切换 -->
    <view class="period-switch">
      <view class="period-tab" :class="{ active: period === 'week' }" @click="switchPeriod('week')">本周</view>
      <view class="period-tab" :class="{ active: period === 'month' }" @click="switchPeriod('month')">本月</view>
    </view>

    <!-- 总体概览 -->
    <view class="stats-overview" v-if="overview">
      <view class="overview-ring">
        <text class="ring-num">{{ overview.rate ?? '--' }}%</text>
        <text class="ring-label">服药率</text>
      </view>
      <view class="overview-detail">
        <view class="od-item taken">
          <text class="od-num">{{ overview.taken ?? 0 }}</text>
          <text class="od-label">已服</text>
        </view>
        <view class="od-item missed">
          <text class="od-num">{{ overview.missed ?? 0 }}</text>
          <text class="od-label">漏服</text>
        </view>
        <view class="od-item total">
          <text class="od-num">{{ overview.total ?? 0 }}</text>
          <text class="od-label">总计</text>
        </view>
      </view>
    </view>

    <!-- 每日服药柱状图 -->
    <view class="card" v-if="dailyData.length > 0">
      <text class="card-title">📈 每日服药情况</text>
      <view class="bar-chart">
        <view class="bar-row" v-for="d in dailyData" :key="d.date">
          <text class="bar-date">{{ d.dateLabel }}</text>
          <view class="bar-track">
            <view class="bar-fill taken" :style="{ width: d.takenPercent + '%' }"></view>
            <view class="bar-fill missed" :style="{ width: d.missedPercent + '%', marginLeft: d.takenPercent + '%' }"></view>
          </view>
          <text class="bar-val">{{ d.taken }}/{{ d.total }}</text>
        </view>
      </view>
    </view>

    <!-- 按药品统计 -->
    <view class="card" v-if="medicineStats.length > 0">
      <text class="card-title">💊 按药品统计</text>
      <view class="med-item" v-for="m in medicineStats" :key="m.name">
        <text class="med-name">{{ m.name }}</text>
        <text class="med-rate" :class="m.rate >= 80 ? 'good' : m.rate >= 60 ? 'warn' : 'bad'">{{ m.rate }}%</text>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty" v-if="!overview || overview.total === 0">
      <text class="empty-icon">📭</text>
      <text class="empty-text">暂无服药记录</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getMedicineRecord, getMedicineList } from '../../api/medicine.js'
import { useUserStore } from '../../store/user.js'

const userStore = useUserStore()
const period = ref('week')
const overview = ref(null)
const dailyData = ref([])
const medicineStats = ref([])

/** 根据周期计算日期范围 */
const dateRange = computed(() => {
  const now = new Date()
  const end = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
  const start = new Date()
  if (period.value === 'week') {
    start.setDate(now.getDate() - 7)
  } else {
    start.setMonth(now.getMonth() - 1)
  }
  const startStr = `${start.getFullYear()}-${String(start.getMonth() + 1).padStart(2, '0')}-${String(start.getDate()).padStart(2, '0')}`
  return { startDate: startStr, endDate: end }
})

function switchPeriod(p) {
  period.value = p
  loadData()
}

async function loadData() {
  const elderId = userStore.elderId
  if (!elderId) return

  try {
    const { startDate, endDate } = dateRange.value

    // 同时获取用药计划和历史服药记录
    const [records, medicineList] = await Promise.all([
      getMedicineRecord({ elderId, startDate, endDate }).catch(() => []),
      getMedicineList(elderId).catch(() => [])
    ])

    const recList = Array.isArray(records) ? records : []
    const medList = Array.isArray(medicineList) ? medicineList : []

    // 按状态统计
    const taken = recList.filter(r => r.status === 1).length
    const missed = recList.filter(r => r.status === 0).length
    const total = taken + missed

    overview.value = {
      taken,
      missed,
      total,
      rate: total > 0 ? Math.round((taken / total) * 100) : 0
    }

    // 按日期分组
    const dateMap = {}
    recList.forEach(r => {
      const d = String(r.recordTime || '').substring(0, 10)
      if (!d) return
      if (!dateMap[d]) dateMap[d] = { taken: 0, missed: 0, total: 0 }
      dateMap[d].total++
      if (r.status === 1) dateMap[d].taken++
      else dateMap[d].missed++
    })

    dailyData.value = Object.entries(dateMap)
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([date, stats]) => ({
        date,
        dateLabel: date.substring(5),
        ...stats,
        takenPercent: stats.total > 0 ? (stats.taken / stats.total) * 100 : 0,
        missedPercent: stats.total > 0 ? (stats.missed / stats.total) * 100 : 0
      }))

    // 按药品统计：用用药计划中的药品名关联服药记录
    const medNameMap = {}
    medList.forEach(m => {
      medNameMap[m.id] = m.medicineName || m.name || '未知药品'
    })

    const medMap = {}
    recList.forEach(r => {
      const name = r.medicineName || medNameMap[r.medicineId] || '未知药品'
      if (!medMap[name]) medMap[name] = { taken: 0, total: 0 }
      medMap[name].total++
      if (r.status === 1) medMap[name].taken++
    })

    medicineStats.value = Object.entries(medMap)
      .map(([name, stats]) => ({
        name,
        ...stats,
        rate: stats.total > 0 ? Math.round((stats.taken / stats.total) * 100) : 0
      }))
      .sort((a, b) => b.rate - a.rate)
  } catch (e) {
    console.error('加载统计数据失败:', e)
  }
}

loadData()
</script>

<style lang="scss" scoped>
@import '../../styles/common.scss';

.page-container {
  min-height: 100vh;
  background: $color-bg;
  padding-bottom: 160rpx;
}

.page-header {
  text-align: center;
  padding: 48rpx 0 24rpx;
}

.page-icon { font-size: 72rpx; display: block; margin-bottom: 12rpx; }
.page-title { display: block; font-size: $elder-font-xl; font-weight: bold; color: $color-text; }

// ========== 周期切换 ==========
.period-switch {
  display: flex;
  margin: 0 $elder-spacing-xl 24rpx;
  background: $color-white;
  border-radius: $elder-radius-lg;
  padding: 8rpx;
  box-shadow: $elder-shadow;
}

.period-tab {
  flex: 1;
  height: 64rpx;
  line-height: 64rpx;
  text-align: center;
  font-size: $elder-font-base;
  color: $color-text-light;
  border-radius: $elder-radius;
  transition: all 0.2s;

  &.active {
    background: linear-gradient(135deg, $color-primary, #36cfc9);
    color: #fff;
    font-weight: bold;
  }
}

// ========== 概览 ==========
.stats-overview {
  display: flex;
  align-items: center;
  gap: 32rpx;
  padding: 32rpx;
  margin: 0 $elder-spacing-xl 24rpx;
  background: $color-white;
  border-radius: $elder-radius-lg;
  box-shadow: $elder-shadow;
}

.overview-ring {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, $color-primary, #36cfc9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ring-num { font-size: 44rpx; font-weight: bold; color: #fff; }
.ring-label { font-size: 22rpx; color: rgba(255,255,255,0.8); }

.overview-detail {
  display: flex;
  gap: 24rpx;
}

.od-item {
  text-align: center;

  &.taken .od-num { color: $color-success; }
  &.missed .od-num { color: $color-warning; }
  &.total .od-num { color: $color-text; }
}

.od-num { display: block; font-size: 40rpx; font-weight: bold; margin-bottom: 4rpx; }
.od-label { font-size: 24rpx; color: $color-text-placeholder; }

// ========== 卡片 ==========
.card {
  background: $color-white;
  border-radius: $elder-radius-lg;
  padding: $elder-spacing-lg;
  margin: 0 $elder-spacing-xl 24rpx;
  box-shadow: $elder-shadow;
}

.card-title {
  font-size: $elder-font-large;
  font-weight: bold;
  color: $color-text;
  display: block;
  margin-bottom: 24rpx;
}

// ========== 柱状图 ==========
.bar-chart {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.bar-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.bar-date {
  width: 80rpx;
  font-size: 22rpx;
  color: $color-text-placeholder;
  flex-shrink: 0;
}

.bar-track {
  flex: 1;
  height: 28rpx;
  background: #f5f5f5;
  border-radius: 6rpx;
  position: relative;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 6rpx;
  position: absolute;
  top: 0;
  left: 0;

  &.taken { background: linear-gradient(90deg, #52c41a, #73d13d); }
  &.missed { background: linear-gradient(90deg, #fa8c16, #ffa940); }
}

.bar-val {
  width: 80rpx;
  text-align: right;
  font-size: 22rpx;
  color: $color-text-light;
  flex-shrink: 0;
}

// ========== 药品统计 ==========
.med-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 2rpx solid #f5f5f5;

  &:last-child { border-bottom: none; }
}

.med-name { font-size: $elder-font-base; color: $color-text; }
.med-rate { font-size: $elder-font-base; font-weight: bold; }

.med-rate.good { color: $color-success; }
.med-rate.warn { color: $color-warning; }
.med-rate.bad { color: $color-danger; }

// ========== 空状态 ==========
.empty { text-align: center; padding: 100rpx 0; }
.empty-icon { font-size: 80rpx; display: block; margin-bottom: 24rpx; }
.empty-text { font-size: $elder-font-medium; color: $color-text-placeholder; }
</style>
