<template>
  <view class="page-container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <input
        class="search-input"
        placeholder="🔍 搜索健康知识..."
        v-model="keyword"
        @confirm="handleSearch"
      />
    </view>

    <!-- 分类标签 -->
    <scroll-view class="tabs-scroll" scroll-x :show-scrollbar="false">
      <view class="tabs-row">
        <view
          class="tab-item"
          :class="{ active: activeCategory === c.value }"
          v-for="c in categories"
          :key="c.value"
          @click="switchCategory(c.value)"
        >
          {{ c.label }}
        </view>
      </view>
    </scroll-view>

    <!-- 文章列表 -->
    <view class="article-list" v-if="articles.length > 0">
      <view
        class="article-item"
        v-for="item in articles"
        :key="item.id"
        @click="goToDetail(item.id)"
      >
        <image
          class="article-cover"
          :src="item.coverImage || '/static/logo.png'"
          mode="aspectFill"
        />
        <view class="article-body">
          <text class="article-title">{{ item.title }}</text>
          <text class="article-summary">{{ item.summary }}</text>
          <view class="article-meta">
            <text class="meta-item">{{ item.category || '健康知识' }}</text>
            <text class="meta-item">👁 {{ item.viewCount || 0 }}</text>
            <text class="meta-item">👍 {{ item.likeCount || 0 }}</text>
            <text class="meta-item">⭐ {{ item.collectCount || 0 }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-card" v-else-if="!loading">
      <text class="empty-icon">📰</text>
      <text class="empty-text">{{ keyword ? '未找到相关文章' : '暂无资讯' }}</text>
    </view>

    <!-- 加载更多 -->
    <view class="load-more" v-if="hasMore">
      <text class="load-text" @click="loadMore">加载更多</text>
    </view>
    <view class="load-end" v-else-if="articles.length > 0">
      <text class="load-text">— 没有更多了 —</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getNewsList, getRecommendedArticles } from '../../api/news.js'

const categories = [
  { label: '全部', value: '' },
  { label: '慢病知识', value: '慢病知识' },
  { label: '用药指南', value: '用药指南' },
  { label: '健康科普', value: '健康科普' },
  { label: '养生保健', value: '养生保健' },
  { label: '精选', value: 'recommended' }
]

const articles = ref([])
const activeCategory = ref('')
const keyword = ref('')
const loading = ref(false)
const hasMore = ref(true)

let pageNum = 1
const pageSize = 10

/** 切换分类 */
async function switchCategory(cat) {
  activeCategory.value = cat
  keyword.value = ''
  pageNum = 1
  articles.value = []
  hasMore.value = true
  await loadArticles()
}

/** 搜索 */
async function handleSearch() {
  pageNum = 1
  articles.value = []
  hasMore.value = true
  await loadArticles()
}

/** 加载文章列表 */
async function loadArticles(isLoadMore = false) {
  if (loading.value) return
  loading.value = true

  try {
    const params = { pageNum, pageSize }
    if (activeCategory.value) {
      params.category = activeCategory.value === 'recommended'
        ? undefined
        : activeCategory.value
    }
    if (keyword.value) params.keyword = keyword.value

    // 精选走推荐接口（返回全量列表，不支持分页）
    if (activeCategory.value === 'recommended') {
      const data = await getRecommendedArticles()
      articles.value = Array.isArray(data) ? data : []
      hasMore.value = false
      loading.value = false
      return
    }

    const data = await getNewsList(params)
    const list = data?.articles || data || []
    const arr = Array.isArray(list) ? list : []

    if (isLoadMore) {
      articles.value = [...articles.value, ...arr]
    } else {
      articles.value = arr
    }

    const total = data?.total || 0
    hasMore.value = articles.value.length < total
    pageNum++
  } catch (e) {
    console.error('加载资讯失败:', e)
    uni.showToast({ title: '加载失败', icon: 'none', duration: 2000 })
  } finally {
    loading.value = false
  }
}

function loadMore() { loadArticles(true) }

function goToDetail(id) {
  uni.navigateTo({ url: `/pages/news/detail?id=${id}` })
}

onMounted(() => loadArticles())
</script>

<style lang="scss" scoped>
@import '../../styles/common.scss';

.page-container {
  min-height: 100vh;
  background: $color-bg;
  padding-bottom: 120rpx;
}

// ========== 搜索栏 ==========
.search-bar {
  padding: 20rpx $elder-spacing-xl;
  background: $color-white;
}

.search-input {
  height: 72rpx;
  background: #f5f7fa;
  border-radius: 36rpx;
  padding: 0 32rpx;
  font-size: $elder-font-base;
  color: $color-text;
}

// ========== 分类标签 ==========
.tabs-scroll {
  background: $color-white;
  padding-bottom: 16rpx;
}

.tabs-row {
  display: flex;
  padding: 0 $elder-spacing-xl;
  gap: 16rpx;
  white-space: nowrap;
}

.tab-item {
  display: inline-block;
  padding: 10rpx 28rpx;
  background: #f5f7fa;
  border-radius: 28rpx;
  font-size: $elder-font-small;
  color: $color-text-light;
  flex-shrink: 0;
  transition: all 0.2s;

  &.active {
    background: $color-primary;
    color: #fff;
    font-weight: bold;
  }
}

// ========== 文章列表 ==========
.article-list {
  padding: $elder-spacing $elder-spacing-xl;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.article-item {
  background: $color-white;
  border-radius: $elder-radius-lg;
  overflow: hidden;
  box-shadow: $elder-shadow;
  display: flex;
}

.article-cover {
  width: 200rpx;
  height: 200rpx;
  flex-shrink: 0;
  background: #f0f0f0;
}

.article-body {
  flex: 1;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.article-title {
  font-size: $elder-font-base;
  font-weight: bold;
  color: $color-text;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-summary {
  font-size: 26rpx;
  color: $color-text-placeholder;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 8rpx 0;
}

.article-meta {
  display: flex;
  gap: 16rpx;
  flex-wrap: wrap;
}

.meta-item {
  font-size: 22rpx;
  color: $color-text-light;
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
