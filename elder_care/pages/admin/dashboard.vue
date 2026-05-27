<template>
  <view class="admin-container">
    <view class="admin-header">
      <text class="admin-title">📊 数据仪表盘</text>
    </view>

    <!-- 用户统计卡片 -->
    <view class="section-title">👥 用户统计</view>
    <view class="stats-grid">
      <view class="stat-card blue">
        <text class="stat-num">{{ stats.totalUsers ?? '--' }}</text>
        <text class="stat-label">总用户数</text>
      </view>
      <view class="stat-card green">
        <text class="stat-num">{{ stats.elderCount ?? '--' }}</text>
        <text class="stat-label">老人</text>
      </view>
      <view class="stat-card orange">
        <text class="stat-num">{{ stats.familyCount ?? '--' }}</text>
        <text class="stat-label">家属</text>
      </view>
      <view class="stat-card purple">
        <text class="stat-num">{{ stats.adminCount ?? '--' }}</text>
        <text class="stat-label">管理员</text>
      </view>
    </view>

    <!-- 健康统计卡片 -->
    <view class="section-title">💊 健康数据</view>
    <view class="stats-grid">
      <view class="stat-card cyan">
        <text class="stat-num">{{ stats.todayHealthRecords ?? '--' }}</text>
        <text class="stat-label">今日健康记录</text>
      </view>
      <view class="stat-card teal">
        <text class="stat-num">{{ stats.activeMedicines ?? '--' }}</text>
        <text class="stat-label">正在服药人数</text>
      </view>
      <view class="stat-card red">
        <text class="stat-num">{{ stats.todayWarnings ?? '--' }}</text>
        <text class="stat-label">今日预警</text>
      </view>
      <view class="stat-card lime">
        <text class="stat-num">{{ displayRate }}</text>
        <text class="stat-label">健康正常率</text>
      </view>
    </view>

    <!-- 资讯统计 -->
    <view class="section-title">📰 内容统计</view>
    <view class="stats-grid">
      <view class="stat-card indigo">
        <text class="stat-num">{{ stats.totalNews ?? '--' }}</text>
        <text class="stat-label">资讯总数</text>
      </view>
      <view class="stat-card pink">
        <text class="stat-num">{{ stats.publishedNews ?? '--' }}</text>
        <text class="stat-label">已发布资讯</text>
      </view>
      <view class="stat-card brown">
        <text class="stat-num">{{ stats.totalAnnouncements ?? '--' }}</text>
        <text class="stat-label">公告总数</text>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="section-title">⚡ 快捷操作</view>
    <view class="quick-grid">
      <view class="quick-card" @click="navTo('/pages/admin/user-list')">
        <text class="q-icon">👥</text>
        <text class="q-label">用户管理</text>
      </view>
      <view class="quick-card" @click="navTo('/pages/admin/news-list')">
        <text class="q-icon">📰</text>
        <text class="q-label">资讯管理</text>
      </view>
      <view class="quick-card" @click="navTo('/pages/admin/news-publish')">
        <text class="q-icon">✏️</text>
        <text class="q-label">发布资讯</text>
      </view>
      <view class="quick-card" @click="navTo('/pages/admin/announcement')">
        <text class="q-icon">📢</text>
        <text class="q-label">公告管理</text>
      </view>
      <view class="quick-card" @click="navTo('/pages/admin/system-config')">
        <text class="q-icon">⚙️</text>
        <text class="q-label">系统配置</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getDashboardStats } from '../../api/admin.js'

const stats = ref({})

const displayRate = computed(() => {
  const v = stats.value.normalRate
  if (v === null || v === undefined) return '--'
  return v + '%'
})

async function loadStats() {
  try {
    stats.value = await getDashboardStats()
  } catch (e) {
    console.error('加载仪表盘数据失败:', e)
  }
}

function navTo(url) {
  uni.navigateTo({ url })
}

onMounted(loadStats)
</script>

<style lang="scss" scoped>
@import '../../styles/common.scss';

.admin-container {
  min-height: 100vh;
  background: #f0f2f5;
  padding: 24rpx 28rpx 120rpx;
}

.admin-header {
  padding: 32rpx 0 16rpx;
}

.admin-title {
  font-size: 40rpx;
  font-weight: bold;
  color: $color-text;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: $color-text;
  margin: 28rpx 0 16rpx;
}

// ========== 统计网格 ==========
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
}

.stat-card {
  padding: 28rpx;
  border-radius: 16rpx;
  text-align: center;

  &.blue   { background: #e6f0ff; }
  &.green  { background: #e6ffe6; }
  &.orange { background: #fff3e6; }
  &.purple { background: #f3e6ff; }
  &.cyan   { background: #e6fbff; }
  &.teal   { background: #e6fff5; }
  &.red    { background: #ffe6e6; }
  &.lime   { background: #f5ffe6; }
  &.indigo { background: #e6e9ff; }
  &.pink   { background: #ffe6f5; }
  &.brown  { background: #f5ede6; }
}

.stat-num {
  display: block;
  font-size: 44rpx;
  font-weight: bold;
  color: $color-text;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  color: $color-text-placeholder;
}

// ========== 快捷操作 ==========
.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
}

.quick-card {
  background: $color-white;
  border-radius: 16rpx;
  padding: 32rpx 16rpx;
  text-align: center;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.05);

  &:active { background: #f5f7fa; }
}

.q-icon { font-size: 48rpx; display: block; margin-bottom: 10rpx; }
.q-label { font-size: 26rpx; color: $color-text; font-weight: 600; }
</style>
