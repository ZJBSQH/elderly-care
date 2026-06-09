<template>
  <view class="page-container">
    <!-- 加载中 -->
    <view class="loading-card" v-if="loading">
      <text>加载中...</text>
    </view>

    <!-- 文章内容 -->
    <template v-else-if="article">
      <!-- 封面图 -->
      <image
        class="cover-image"
        :src="article.coverImage || '/static/logo.png'"
        mode="aspectFill"
      />

      <!-- 文章信息 -->
      <view class="article-info">
        <text class="article-title">{{ article.title }}</text>
        <view class="article-meta">
          <text class="meta-tag">{{ article.category || '健康知识' }}</text>
          <text class="meta-text">{{ formatDate(article.publishTime) }}</text>
          <text class="meta-text">👁 {{ article.viewCount || 0 }}</text>
        </view>
      </view>

      <!-- 正文 -->
      <view class="article-content">
        <text class="content-text">{{ article.summary || '暂无内容' }}</text>
      </view>

      <!-- AI 提问 -->
      <view class="ai-ask-card" @click="askAIAboutArticle">
        <text class="ai-ask-icon">🤖</text>
        <text class="ai-ask-text">对这篇文章有疑问？AI 帮你解答</text>
        <text class="ai-ask-arrow">→</text>
      </view>

      <!-- 互动栏 -->
      <view class="action-bar">
        <view class="action-item" :class="{ active: isLiked }" @click="toggleLike">
          <text class="action-icon">{{ isLiked ? '❤️' : '🤍' }}</text>
          <text class="action-count">{{ likeCount }}</text>
        </view>
        <view class="action-item" :class="{ active: isCollected }" @click="toggleCollect">
          <text class="action-icon">{{ isCollected ? '⭐' : '☆' }}</text>
          <text class="action-count">{{ collectCount }}</text>
        </view>
      </view>
    </template>

    <!-- 文章不存在 -->
    <view class="empty-card" v-else>
      <text class="empty-icon">📄</text>
      <text class="empty-text">文章不存在或已下架</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../store/user.js'
import {
  getNewsDetail,
  likeArticle,
  cancelLikeArticle,
  collectArticle,
  cancelCollectArticle
} from '../../api/news.js'

const userStore = useUserStore()

const article = ref(null)
const loading = ref(true)
const isLiked = ref(false)
const isCollected = ref(false)
const likeCount = ref(0)
const collectCount = ref(0)

let newsId = null

function formatDate(dt) {
  if (!dt) return ''
  return String(dt).substring(0, 10)
}

/** 加载文章详情 */
async function loadDetail() {
  loading.value = true
  try {
    const userId = userStore.userId
    const data = await getNewsDetail(newsId, { userId })
    article.value = data
    isLiked.value = data?.isLiked === 1
    isCollected.value = data?.isCollected === 1
    likeCount.value = data?.likeCount || 0
    collectCount.value = data?.collectCount || 0
  } catch (e) {
    console.error('加载文章失败:', e)
    article.value = null
  } finally {
    loading.value = false
  }
}

/** 点赞/取消点赞 */
async function toggleLike() {
  if (likeLoading.value) return
  likeLoading.value = true
  try {
    if (isLiked.value) {
      await cancelLikeArticle(newsId)
      isLiked.value = false
      likeCount.value = Math.max(0, likeCount.value - 1)
      uni.showToast({ title: '已取消点赞', icon: 'none', duration: 1500 })
    } else {
      await likeArticle(newsId)
      isLiked.value = true
      likeCount.value++
      uni.showToast({ title: '点赞成功', icon: 'success', duration: 1500 })
    }
  } catch (e) {
    uni.showToast({ title: e.message || '操作失败', icon: 'none', duration: 2000 })
  } finally {
    likeLoading.value = false
  }
}

const likeLoading = ref(false)
const collectLoading = ref(false)

/** 收藏/取消收藏 */
async function toggleCollect() {
  if (collectLoading.value) return
  collectLoading.value = true
  try {
    if (isCollected.value) {
      await cancelCollectArticle(newsId)
      isCollected.value = false
      collectCount.value = Math.max(0, collectCount.value - 1)
      uni.showToast({ title: '已取消收藏', icon: 'none', duration: 1500 })
    } else {
      await collectArticle(newsId)
      isCollected.value = true
      collectCount.value++
      uni.showToast({ title: '收藏成功', icon: 'success', duration: 1500 })
    }
  } catch (e) {
    uni.showToast({ title: e.message || '操作失败', icon: 'none', duration: 2000 })
  } finally {
    collectLoading.value = false
  }
}

/** 跳转到 AI 问答，预填入文章标题 */
function askAIAboutArticle() {
  const title = article.value?.title || ''
  uni.navigateTo({ url: `/pages/ai/chat?question=${encodeURIComponent(title)}` })
}

onMounted(() => {
  const pages = getCurrentPages()
  const current = pages[pages.length - 1]
  newsId = parseInt(current?.$page?.options?.id, 10)
  if (newsId) {
    loadDetail()
  } else {
    loading.value = false
    article.value = null
  }
})
</script>

<style lang="scss" scoped>
@import '../../styles/common.scss';

.page-container {
  min-height: 100vh;
  background: $color-bg;
  padding-bottom: 120rpx;
}

// ========== 封面 ==========
.cover-image {
  width: 100%;
  height: 420rpx;
  background: #f0f0f0;
}

// ========== 文章信息 ==========
.article-info {
  background: $color-white;
  padding: $elder-spacing-lg;
  border-radius: 0 0 $elder-radius-lg $elder-radius-lg;
  margin-bottom: $elder-spacing-sm;
}

.article-title {
  display: block;
  font-size: $elder-font-large;
  font-weight: bold;
  color: $color-text;
  line-height: 1.5;
  margin-bottom: 16rpx;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 20rpx;
  flex-wrap: wrap;
}

.meta-tag {
  font-size: 24rpx;
  color: #fff;
  background: $color-primary;
  border-radius: 8rpx;
  padding: 4rpx 16rpx;
}

.meta-text {
  font-size: 26rpx;
  color: $color-text-placeholder;
}

// ========== 正文 ==========
.article-content {
  background: $color-white;
  border-radius: $elder-radius-lg;
  padding: $elder-spacing-lg;
  margin-bottom: $elder-spacing-sm;
  box-shadow: $elder-shadow;
}

.content-text {
  font-size: $elder-font-base;
  color: $color-text;
  line-height: 1.8;
  white-space: pre-wrap;
}

// ========== AI 提问卡片 ==========
.ai-ask-card {
  background: linear-gradient(135deg, rgba(91,143,249,0.06) 0%, rgba(54,207,201,0.06) 100%);
  border-radius: $elder-radius-lg;
  padding: $elder-spacing;
  margin-bottom: $elder-spacing-sm;
  display: flex;
  align-items: center;
  gap: 12rpx;
  border: 2rpx dashed rgba(91,143,249,0.3);

  &:active { background: rgba(91,143,249,0.1); }
}

.ai-ask-icon { font-size: 40rpx; }

.ai-ask-text {
  flex: 1;
  font-size: $elder-font-small;
  color: $color-primary;
  font-weight: 600;
}

.ai-ask-arrow {
  font-size: $elder-font-small;
  color: $color-primary;
}

// ========== 互动栏 ==========
.action-bar {
  display: flex;
  justify-content: center;
  gap: 80rpx;
  background: $color-white;
  border-radius: $elder-radius-lg;
  padding: $elder-spacing;
  box-shadow: $elder-shadow;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  padding: 16rpx 32rpx;
  transition: transform 0.15s;

  &.active {
    .action-icon { transform: scale(1.15); }
  }
}

.action-icon {
  font-size: 48rpx;
}

.action-count {
  font-size: $elder-font-small;
  color: $color-text-light;
  font-weight: 600;
}

// ========== 加载/空状态 ==========
.loading-card {
  text-align: center;
  padding: 200rpx 0;
  font-size: $elder-font-medium;
  color: $color-text-placeholder;
}

.empty-card { text-align: center; padding: 200rpx 0; }
.empty-icon { font-size: 100rpx; display: block; margin-bottom: 24rpx; }
.empty-text { font-size: $elder-font-medium; color: $color-text-placeholder; }
</style>
