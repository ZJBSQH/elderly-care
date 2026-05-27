<template>
  <view class="admin-container">
    <view class="admin-header">
      <text class="admin-title">📢 公告管理</text>
    </view>

    <!-- 发布公告表单 -->
    <view class="form-card">
      <text class="section-title">发布新公告</text>

      <view class="form-item">
        <text class="form-label">公告标题 <text class="required">*</text></text>
        <input class="form-input" v-model="form.title" placeholder="请输入公告标题" maxlength="200" />
      </view>

      <view class="form-item">
        <text class="form-label">公告内容 <text class="required">*</text></text>
        <textarea
          class="form-textarea"
          v-model="form.content"
          placeholder="请输入公告内容"
          auto-height
        />
      </view>

      <view class="form-item">
        <text class="form-label">通知类型</text>
        <view class="radio-group">
          <view class="radio-item" :class="{ active: form.notifyType === 1 }" @click="form.notifyType = 1">系统通知</view>
          <view class="radio-item" :class="{ active: form.notifyType === 2 }" @click="form.notifyType = 2">全局公告</view>
        </view>
      </view>

      <button class="submit-btn" :loading="publishing" @click="handlePublish">发布公告</button>
    </view>

    <!-- 公告列表 -->
    <view class="section-title" style="margin-top: 40rpx;">已发布公告</view>
    <view class="anno-list" v-if="announcements.length > 0">
      <view class="anno-card" v-for="a in announcements" :key="a.id">
        <view class="anno-body">
          <text class="anno-title">{{ a.title }}</text>
          <text class="anno-content">{{ a.content }}</text>
          <text class="anno-time" v-if="a.sendTime">{{ formatDt(a.sendTime) }}</text>
        </view>
        <view class="anno-actions">
          <button class="del-btn" @click="handleDelete(a)">删除</button>
        </view>
      </view>
    </view>

    <view class="empty" v-else>
      <text>暂无公告</text>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { publishAnnouncement, getAnnouncements, deleteAnnouncement } from '../../api/admin.js'

const form = reactive({ title: '', content: '', notifyType: 2 })
const publishing = ref(false)
const announcements = ref([])

function formatDt(dt) {
  if (!dt) return ''
  return String(dt).substring(0, 16).replace('T', ' ')
}

async function handlePublish() {
  if (!form.title.trim()) return uni.showToast({ title: '请输入公告标题', icon: 'none', duration: 2000 })
  if (!form.content.trim()) return uni.showToast({ title: '请输入公告内容', icon: 'none', duration: 2000 })

  publishing.value = true
  try {
    await publishAnnouncement({
      title: form.title.trim(),
      content: form.content.trim(),
      notifyType: form.notifyType
    })
    uni.showToast({ title: '公告已发布', icon: 'success', duration: 1500 })
    form.title = ''
    form.content = ''
    loadAnnouncements()
  } catch (e) {
    uni.showToast({ title: e.message || '发布失败', icon: 'none', duration: 2000 })
  } finally {
    publishing.value = false
  }
}

async function loadAnnouncements() {
  try {
    const list = await getAnnouncements({ page: 1, size: 50 })
    announcements.value = Array.isArray(list) ? list : []
  } catch (e) {
    console.error('加载公告列表失败:', e)
  }
}

async function handleDelete(a) {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除公告「${a.title}」吗？`,
    confirmText: '确定删除',
    confirmColor: '#FF6B6B',
    cancelText: '取消',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteAnnouncement(a.id)
          uni.showToast({ title: '已删除', icon: 'success', duration: 1500 })
          loadAnnouncements()
        } catch (e) {
          uni.showToast({ title: e.message || '删除失败', icon: 'none', duration: 2000 })
        }
      }
    }
  })
}

loadAnnouncements()
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
  margin-bottom: 16rpx;
}

// ========== 表单 ==========
.form-card {
  background: $color-white;
  border-radius: 20rpx;
  padding: 32rpx 28rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}

.form-item {
  margin-bottom: 28rpx;
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
  min-height: 160rpx;
  width: 100%;
}

.radio-group {
  display: flex;
  gap: 12rpx;
}

.radio-item {
  flex: 1;
  height: 64rpx;
  line-height: 64rpx;
  text-align: center;
  background: #f5f7fa;
  border-radius: 10rpx;
  font-size: 26rpx;
  color: $color-text-light;
  border: 2rpx solid transparent;

  &.active { border-color: $color-primary; color: $color-primary; background: rgba(64,158,255,0.08); }
}

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
  margin-top: 12rpx;

  &[disabled] { opacity: 0.7; }
}

// ========== 公告列表 ==========
.anno-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.anno-card {
  background: $color-white;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}

.anno-body { margin-bottom: 16rpx; }

.anno-title {
  display: block;
  font-size: 30rpx;
  font-weight: bold;
  color: $color-text;
  margin-bottom: 10rpx;
}

.anno-content {
  display: block;
  font-size: 26rpx;
  color: $color-text-light;
  margin-bottom: 8rpx;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.anno-time {
  font-size: 22rpx;
  color: $color-text-placeholder;
}

.anno-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 16rpx;
  border-top: 2rpx solid #f5f5f5;
}

.del-btn {
  height: 52rpx;
  line-height: 52rpx;
  padding: 0 24rpx;
  background: #fff2f2;
  color: $color-danger;
  border-radius: 8rpx;
  font-size: 22rpx;
  border: none;
}

// ========== 空 ==========
.empty { text-align: center; padding: 80rpx 0; font-size: 28rpx; color: $color-text-placeholder; }
</style>
