<template>
  <view class="admin-container">
    <view class="admin-header">
      <text class="admin-title">📰 资讯管理</text>
      <button class="publish-btn" @click="navTo('/pages/admin/news-publish')">+ 发布资讯</button>
    </view>

    <!-- 筛选 -->
    <view class="filter-tabs">
      <view class="filter-tab" :class="{ active: filterStatus === null }" @click="filterStatus = null; loadList()">全部</view>
      <view class="filter-tab" :class="{ active: filterStatus === 1 }" @click="filterStatus = 1; loadList()">已发布</view>
      <view class="filter-tab" :class="{ active: filterStatus === 0 }" @click="filterStatus = 0; loadList()">草稿</view>
      <view class="filter-tab" :class="{ active: filterStatus === 2 }" @click="filterStatus = 2; loadList()">已下架</view>
    </view>

    <!-- 资讯列表 -->
    <view class="news-list" v-if="newsList.length > 0">
      <view class="news-card" v-for="n in newsList" :key="n.id">
        <view class="news-main">
          <image
            class="news-cover"
            v-if="n.coverImage"
            :src="n.coverImage"
            mode="aspectFill"
          />
          <view class="news-body">
            <text class="news-title">{{ n.title }}</text>
            <text class="news-meta">{{ n.category || '未分类' }} · {{ n.viewCount || 0 }}阅读</text>
            <view class="news-tags">
              <text class="tag" :class="statusClass(n.status)">{{ n.statusDesc || statusMap[n.status] }}</text>
              <text class="tag recommend" v-if="n.isRecommended === 1">推荐</text>
            </view>
          </view>
        </view>
        <view class="news-actions">
          <button class="act-btn" @click="editNews(n)">编辑</button>
          <button
            class="act-btn"
            :class="n.status === 1 ? 'warn' : 'ok'"
            @click="toggleStatus(n)"
          >{{ n.status === 1 ? '下架' : '上架' }}</button>
          <button class="act-btn" @click="toggleRecommend(n)">
            {{ n.isRecommended === 1 ? '取消推荐' : '推荐' }}
          </button>
          <button class="act-btn danger" @click="handleDelete(n)">删除</button>
        </view>
      </view>
    </view>

    <view class="empty" v-else>
      <text>暂无资讯数据</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import {
  getAdminNewsList,
  changeNewsStatus,
  changeNewsRecommended,
  deleteNews
} from '../../api/admin.js'

const statusMap = { 0: '草稿', 1: '已发布', 2: '已下架' }
const filterStatus = ref(null)
const newsList = ref([])

function statusClass(s) {
  if (s === 1) return 'published'
  if (s === 2) return 'offline'
  return 'draft'
}

async function loadList() {
  try {
    const params = { page: 1, size: 50 }
    if (filterStatus.value !== null) params.status = filterStatus.value
    const result = await getAdminNewsList(params)
    newsList.value = result?.list || []
  } catch (e) {
    console.error('加载资讯列表失败:', e)
  }
}

async function toggleStatus(n) {
  const newStatus = n.status === 1 ? 2 : 1
  const action = newStatus === 1 ? '上架' : '下架'
  try {
    await changeNewsStatus(n.id, newStatus)
    uni.showToast({ title: `已${action}`, icon: 'success', duration: 1500 })
    loadList()
  } catch (e) {
    uni.showToast({ title: e.message || '操作失败', icon: 'none', duration: 2000 })
  }
}

async function toggleRecommend(n) {
  const val = n.isRecommended === 1 ? 0 : 1
  try {
    await changeNewsRecommended(n.id, val)
    uni.showToast({ title: val === 1 ? '已推荐' : '已取消推荐', icon: 'success', duration: 1500 })
    loadList()
  } catch (e) {
    uni.showToast({ title: e.message || '操作失败', icon: 'none', duration: 2000 })
  }
}

async function handleDelete(n) {
  uni.showModal({
    title: '确认删除',
    content: `确定要永久删除「${n.title}」吗？此操作不可撤销。`,
    confirmText: '确定删除',
    confirmColor: '#FF6B6B',
    cancelText: '取消',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteNews(n.id)
          uni.showToast({ title: '已删除', icon: 'success', duration: 1500 })
          loadList()
        } catch (e) {
          uni.showToast({ title: e.message || '删除失败', icon: 'none', duration: 2000 })
        }
      }
    }
  })
}

function editNews(n) {
  uni.navigateTo({ url: `/pages/admin/news-publish?id=${n.id}` })
}

function navTo(url) {
  uni.navigateTo({ url })
}

loadList()
</script>

<style lang="scss" scoped>
@import '../../styles/common.scss';

.admin-container {
  min-height: 100vh;
  background: #f0f2f5;
  padding: 24rpx 28rpx 120rpx;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 0 16rpx;
}

.admin-title {
  font-size: 40rpx;
  font-weight: bold;
  color: $color-text;
}

.publish-btn {
  height: 64rpx;
  line-height: 64rpx;
  padding: 0 24rpx;
  background: $color-primary;
  color: #fff;
  border-radius: 12rpx;
  font-size: 24rpx;
  border: none;
}

// ========== 筛选 ==========
.filter-tabs {
  display: flex;
  gap: 12rpx;
  margin-bottom: 24rpx;
}

.filter-tab {
  flex: 1;
  height: 64rpx;
  line-height: 64rpx;
  text-align: center;
  background: $color-white;
  border-radius: 12rpx;
  font-size: 26rpx;
  color: $color-text-light;
  border: 2rpx solid transparent;

  &.active { border-color: $color-primary; color: $color-primary; font-weight: bold; }
}

// ========== 列表 ==========
.news-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.news-card {
  background: $color-white;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}

.news-main {
  display: flex;
  gap: 16rpx;
}

.news-cover {
  width: 140rpx;
  height: 100rpx;
  border-radius: 10rpx;
  flex-shrink: 0;
  background: #f5f5f5;
}

.news-body { flex: 1; }

.news-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: $color-text;
  margin-bottom: 6rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-meta {
  font-size: 24rpx;
  color: $color-text-placeholder;
  display: block;
  margin-bottom: 8rpx;
}

.news-tags {
  display: flex;
  gap: 8rpx;
}

.tag {
  font-size: 20rpx;
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
  background: #f5f5f5;
  color: $color-text-light;

  &.published { background: #e6ffe6; color: #52c41a; }
  &.draft { background: #fff7e6; color: #fa8c16; }
  &.offline { background: #ffe6e6; color: #ff4d4f; }
  &.recommend { background: #e6f0ff; color: $color-primary; }
}

.news-actions {
  display: flex;
  gap: 12rpx;
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 2rpx solid #f5f5f5;
}

.act-btn {
  flex: 1;
  height: 52rpx;
  line-height: 52rpx;
  text-align: center;
  background: #f5f7fa;
  border-radius: 8rpx;
  font-size: 22rpx;
  color: $color-text-light;
  border: none;

  &.warn { color: $color-warning; background: #fff8f0; }
  &.ok { color: $color-success; background: #f0fff0; }
  &.danger { color: $color-danger; background: #fff2f2; }
}

// ========== 空状态 ==========
.empty { text-align: center; padding: 80rpx 0; font-size: 28rpx; color: $color-text-placeholder; }
</style>
