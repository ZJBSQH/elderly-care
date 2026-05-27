<template>
  <view class="page-container">
    <view class="page-header">
      <text class="page-title">健康趋势</text>
      <text class="page-desc">查看指标变化趋势</text>
    </view>

    <!-- 指标选择 -->
    <view class="metric-tabs">
      <view
        class="tab"
        :class="{ active: activeMetric === 'bp' }"
        @click="activeMetric = 'bp'"
      >🩸 血压</view>
      <view
        class="tab"
        :class="{ active: activeMetric === 'sugar' }"
        @click="activeMetric = 'sugar'"
      >🩸 血糖</view>
      <view
        class="tab"
        :class="{ active: activeMetric === 'hr' }"
        @click="activeMetric = 'hr'"
      >💓 心率</view>
      <view
        class="tab"
        :class="{ active: activeMetric === 'weight' }"
        @click="activeMetric = 'weight'"
      >⚖️ 体重</view>
    </view>

    <!-- 日期范围 -->
    <view class="date-row">
      <picker mode="date" :value="startDate" @change="onStartChange">
        <view class="date-picker"><text>{{ startDate || '开始' }}</text></view>
      </picker>
      <text class="date-sep">至</text>
      <picker mode="date" :value="endDate" @change="onEndChange">
        <view class="date-picker"><text>{{ endDate || '结束' }}</text></view>
      </picker>
      <button class="search-btn" @click="loadTrend">查询</button>
    </view>

    <!-- 图表区域 -->
    <view class="chart-card" v-if="trendData">
      <text class="chart-title">{{ metricLabel }}趋势</text>

      <!-- 柱状图 -->
      <view class="bar-chart">
        <view class="bar-row" v-for="(item, idx) in chartItems" :key="idx">
          <text class="bar-date">{{ item.date }}</text>
          <view class="bar-track">
            <view
              class="bar-fill"
              :class="{ warning: item.warning }"
              :style="{ width: item.percent + '%' }"
            >
              <text class="bar-value">{{ item.value }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 无数据 -->
      <view class="no-data" v-if="chartItems.length === 0">
        <text>此时间段暂无趋势数据</text>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-card" v-else-if="!loading">
      <text class="empty-icon">📈</text>
      <text class="empty-text">选择日期范围查看趋势</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../store/user.js'
import { getHealthTrend } from '../../api/health.js'

const userStore = useUserStore()

const activeMetric = ref('bp')
const trendData = ref(null)
const loading = ref(false)

const today = new Date()
const thirtyAgo = new Date(today.getTime() - 30 * 24 * 3600 * 1000)
const startDate = ref(formatStr(thirtyAgo))
const endDate = ref(formatStr(today))

const metricLabel = computed(() => ({
  bp: '血压', sugar: '血糖', hr: '心率', weight: '体重'
}[activeMetric.value]))

function formatStr(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function onStartChange(e) { startDate.value = e.detail.value }
function onEndChange(e) { endDate.value = e.detail.value }

/** 构建当前指标图表数据 */
const chartItems = computed(() => {
  if (!trendData.value) return []

  const dates = trendData.value.dates || []
  let items = []

  if (activeMetric.value === 'bp') {
    const sys = trendData.value.bloodPressure?.systolic || []
    const dia = trendData.value.bloodPressure?.diastolic || []
    const flags = trendData.value.bloodPressure?.warningFlags || []
    items = dates.map((d, i) => ({
      date: String(d).substring(5),
      value: sys[i] !== undefined ? `${sys[i]}/${dia[i] !== undefined ? dia[i] : '--'}` : '--',
      numValue: sys[i] || 0,
      warning: flags[i] === 1
    }))
  } else if (activeMetric.value === 'sugar') {
    const vals = trendData.value.bloodSugar?.values || []
    const flags = trendData.value.bloodSugar?.warningFlags || []
    items = dates.map((d, i) => ({
      date: String(d).substring(5),
      value: vals[i] !== undefined ? String(vals[i]) : '--',
      numValue: parseFloat(vals[i]) || 0,
      warning: flags[i] === 1
    }))
  } else if (activeMetric.value === 'hr') {
    const vals = trendData.value.heartRate?.values || []
    const flags = trendData.value.heartRate?.warningFlags || []
    items = dates.map((d, i) => ({
      date: String(d).substring(5),
      value: vals[i] !== undefined ? String(vals[i]) : '--',
      numValue: parseInt(vals[i]) || 0,
      warning: flags[i] === 1
    }))
  } else if (activeMetric.value === 'weight') {
    const vals = trendData.value.weight?.values || []
    const flags = trendData.value.weight?.warningFlags || []
    items = dates.map((d, i) => ({
      date: String(d).substring(5),
      value: vals[i] !== undefined ? String(vals[i]) : '--',
      numValue: parseFloat(vals[i]) || 0,
      warning: flags[i] === 1
    }))
  }

  // 计算百分比（相对于最大值）
  const maxVal = Math.max(...items.map(i => i.numValue).filter(v => v > 0), 1)
  items.forEach(i => { i.percent = Math.round((i.numValue / maxVal) * 100) })

  // 最多展示最近14条
  return items.slice(-14)
})

/** 加载趋势数据 */
async function loadTrend() {
  loading.value = true
  try {
    const elderId = userStore.elderId
    if (!elderId) { loading.value = false; return }

    const params = {
      elderId,
      startDate: startDate.value + 'T00:00:00',
      endDate: endDate.value + 'T23:59:59'
    }
    trendData.value = await getHealthTrend(params)
  } catch (e) {
    console.error('加载趋势数据失败:', e)
    uni.showToast({ title: '加载失败', icon: 'none', duration: 2000 })
    trendData.value = null
  } finally {
    loading.value = false
  }
}

onMounted(loadTrend)
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
  padding: 20rpx 0 24rpx;
}

.page-title { display: block; font-size: $elder-font-xl; font-weight: bold; color: $color-text; margin-bottom: 8rpx; }
.page-desc { display: block; font-size: $elder-font-base; color: $color-text-placeholder; }

// ========== 指标选择 ==========
.metric-tabs {
  display: flex;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.tab {
  flex: 1;
  height: 68rpx;
  line-height: 68rpx;
  text-align: center;
  background: $color-white;
  border-radius: $elder-radius;
  font-size: 26rpx;
  color: $color-text-light;
  box-shadow: $elder-shadow;
  transition: all 0.2s;

  &.active {
    background: $color-primary;
    color: #fff;
    font-weight: bold;
  }
}

// ========== 日期筛选 ==========
.date-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: $elder-spacing;
}

.date-picker {
  flex: 1;
  height: 68rpx;
  line-height: 68rpx;
  text-align: center;
  background: $color-white;
  border-radius: $elder-radius;
  font-size: 26rpx;
  color: $color-text;
  box-shadow: $elder-shadow;
}

.date-sep { font-size: $elder-font-small; color: $color-text-placeholder; flex-shrink: 0; }

.search-btn {
  height: 68rpx;
  line-height: 68rpx;
  background: $color-primary;
  color: #fff;
  border-radius: $elder-radius;
  font-size: 26rpx;
  border: none;
  padding: 0 24rpx;
  flex-shrink: 0;
}

// ========== 图表卡片 ==========
.chart-card {
  background: $color-white;
  border-radius: $elder-radius-lg;
  padding: $elder-spacing-lg;
  box-shadow: $elder-shadow;
}

.chart-title {
  display: block;
  font-size: $elder-font-large;
  font-weight: bold;
  color: $color-text;
  margin-bottom: 24rpx;
  text-align: center;
}

// ========== CSS 柱状图 ==========
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
  color: $color-text-light;
  text-align: right;
  flex-shrink: 0;
}

.bar-track {
  flex: 1;
  height: 48rpx;
  background: #f5f7fa;
  border-radius: 24rpx;
  overflow: hidden;
  position: relative;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, $color-primary, #36cfc9);
  border-radius: 24rpx;
  min-width: 48rpx;
  display: flex;
  align-items: center;
  padding-left: 12rpx;
  transition: width 0.5s ease;

  &.warning {
    background: linear-gradient(90deg, $color-warning, $color-danger);
  }
}

.bar-value {
  font-size: 22rpx;
  color: #fff;
  font-weight: bold;
  white-space: nowrap;
}

// ========== 空状态 ==========
.no-data {
  text-align: center;
  padding: 40rpx 0;
  font-size: $elder-font-base;
  color: $color-text-placeholder;
}

.empty-card { text-align: center; padding: 120rpx 0; }
.empty-icon { font-size: 100rpx; display: block; margin-bottom: 24rpx; }
.empty-text { font-size: $elder-font-medium; color: $color-text-placeholder; }
</style>
