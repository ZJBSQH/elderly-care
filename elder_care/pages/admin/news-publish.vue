<template>
  <view class="admin-container">
    <view class="admin-header">
      <text class="admin-title">{{ isEdit ? '编辑资讯' : '发布资讯' }}</text>
    </view>

    <view class="form-card">
      <!-- 标题 -->
      <view class="form-item">
        <text class="form-label">标题 <text class="required">*</text></text>
        <input class="form-input" v-model="form.title" placeholder="请输入资讯标题" maxlength="255" />
      </view>

      <!-- 分类 -->
      <view class="form-item">
        <text class="form-label">分类 <text class="required">*</text></text>
        <view class="chip-group">
          <view
            class="chip"
            :class="{ active: form.category === c }"
            v-for="c in categories"
            :key="c"
            @click="form.category = c"
          >{{ c }}</view>
        </view>
      </view>

      <!-- 摘要 -->
      <view class="form-item">
        <text class="form-label">摘要</text>
        <textarea
          class="form-textarea"
          v-model="form.summary"
          placeholder="请输入资讯摘要（选填）"
          :maxlength="500"
          auto-height
        />
      </view>

      <!-- 封面图 -->
      <view class="form-item">
        <text class="form-label">封面图 URL</text>
        <input class="form-input" v-model="form.coverImage" placeholder="请输入封面图URL（选填）" />
        <image v-if="form.coverImage" class="cover-preview" :src="form.coverImage" mode="aspectFill" />
      </view>

      <!-- 正文 -->
      <view class="form-item">
        <text class="form-label">正文内容 <text class="required">*</text></text>
        <textarea
          class="form-textarea content-area"
          v-model="form.content"
          placeholder="请输入正文内容（支持纯文本）"
          auto-height
        />
      </view>

      <!-- 推荐 -->
      <view class="form-item">
        <text class="form-label">设为推荐</text>
        <switch :checked="form.isRecommended === 1" @change="form.isRecommended = $event.detail.value ? 1 : 0" />
      </view>

      <!-- 提交 -->
      <button class="submit-btn" :loading="loading" @click="handleSubmit">
        {{ isEdit ? '保存修改' : '发布资讯' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { publishNews, updateNews, getAdminNewsDetail } from '../../api/admin.js'

const isEdit = ref(false)
const editId = ref(null)
const loading = ref(false)

const categories = ['慢病知识', '用药安全', '养生保健', '饮食营养', '运动康复', '心理健康']

const form = reactive({
  title: '',
  category: '慢病知识',
  summary: '',
  coverImage: '',
  content: '',
  isRecommended: 0
})

async function loadNews(id) {
  try {
    const detail = await getAdminNewsDetail(id)
    if (detail) {
      form.title = detail.title || ''
      form.category = detail.category || '慢病知识'
      form.summary = detail.summary || ''
      form.coverImage = detail.coverImage || ''
      form.content = detail.content || ''
      form.isRecommended = detail.isRecommended || 0
    }
  } catch (e) {
    console.error('加载资讯详情失败:', e)
  }
}

async function handleSubmit() {
  if (!form.title.trim()) return uni.showToast({ title: '请输入标题', icon: 'none', duration: 2000 })
  if (!form.category) return uni.showToast({ title: '请选择分类', icon: 'none', duration: 2000 })
  if (!form.content.trim()) return uni.showToast({ title: '请输入正文内容', icon: 'none', duration: 2000 })

  loading.value = true
  try {
    const payload = {
      title: form.title.trim(),
      category: form.category,
      content: form.content.trim(),
      summary: form.summary.trim() || undefined,
      coverImage: form.coverImage.trim() || undefined,
      isRecommended: form.isRecommended
    }

    if (isEdit.value) {
      payload.id = editId.value
      await updateNews(payload)
      uni.showToast({ title: '修改成功', icon: 'success', duration: 1500 })
    } else {
      await publishNews(payload)
      uni.showToast({ title: '发布成功', icon: 'success', duration: 1500 })
    }

    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) {
    uni.showToast({ title: e.message || '操作失败', icon: 'none', duration: 2000 })
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const pages = getCurrentPages()
  const opts = pages[pages.length - 1]?.$page?.options || {}
  if (opts.id) {
    isEdit.value = true
    editId.value = parseInt(opts.id, 10)
    loadNews(editId.value)
  }
})
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

// ========== 表单 ==========
.form-card {
  background: $color-white;
  border-radius: 20rpx;
  padding: 32rpx 28rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}

.form-item {
  margin-bottom: 32rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: $color-text;
  margin-bottom: 12rpx;
}

.required { color: $color-danger; }

.form-input {
  height: 80rpx;
  background: #f5f7fa;
  border-radius: 12rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
}

.form-textarea {
  background: #f5f7fa;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
  min-height: 120rpx;
  width: 100%;

  &.content-area {
    min-height: 320rpx;
  }
}

.chip-group {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
}

.chip {
  height: 60rpx;
  line-height: 60rpx;
  padding: 0 20rpx;
  background: #f5f7fa;
  border-radius: 10rpx;
  font-size: 24rpx;
  color: $color-text-light;
  border: 2rpx solid transparent;

  &.active {
    border-color: $color-primary;
    color: $color-primary;
    background: rgba(64,158,255,0.08);
  }
}

.cover-preview {
  width: 300rpx;
  height: 180rpx;
  border-radius: 10rpx;
  margin-top: 16rpx;
  background: #f5f5f5;
}

// ========== 提交 ==========
.submit-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: linear-gradient(135deg, $color-primary, #36cfc9);
  color: #fff;
  border-radius: 16rpx;
  font-size: 32rpx;
  font-weight: bold;
  border: none;
  margin-top: 20rpx;

  &[disabled] { opacity: 0.7; }
}
</style>
