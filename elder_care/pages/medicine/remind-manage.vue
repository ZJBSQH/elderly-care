<template>
  <view class="page-container">
    <!-- 提醒设置 -->
    <view class="card">
      <text class="card-title">⏰ 提醒设置</text>

      <view class="form-item">
        <text class="form-label">铃声</text>
        <picker :value="settings.ringtone" :range="ringtoneOptions" @change="onRingtoneChange">
          <view class="picker-value">
            <text :class="{ placeholder: !settings.ringtone }">{{ settings.ringtone || '默认铃声' }}</text>
            <text class="arrow">▼</text>
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label">音量</text>
        <view class="slider-row">
          <text class="slider-label">🔇</text>
          <slider
            class="vol-slider"
            :value="settings.volume"
            :min="0"
            :max="100"
            :step="10"
            show-value
            @change="e => settings.volume = e.detail.value"
          />
          <text class="slider-label">🔊</text>
        </view>
      </view>

      <view class="form-item">
        <text class="form-label">重复模式</text>
        <view class="chip-row">
          <view
            class="chip"
            :class="{ active: settings.repeatMode === m.value }"
            v-for="m in repeatModes"
            :key="m.value"
            @click="settings.repeatMode = m.value"
          >{{ m.label }}</view>
        </view>
      </view>

      <view class="form-item">
        <text class="form-label">勿扰时段</text>
        <view class="picker-row">
          <picker mode="time" :value="quietStart" @change="onQuietStart">
            <view class="time-pick">
              <text>{{ quietStart || '开始' }}</text>
            </view>
          </picker>
          <text class="time-sep">至</text>
          <picker mode="time" :value="quietEnd" @change="onQuietEnd">
            <view class="time-pick">
              <text>{{ quietEnd || '结束' }}</text>
            </view>
          </picker>
        </view>
      </view>

      <button class="save-btn" :loading="saving" @click="saveSettings">保存设置</button>
    </view>

    <!-- 通知记录 -->
    <view class="card">
      <view class="card-header">
        <text class="card-title">🔔 通知记录</text>
        <text class="unread-badge" v-if="unreadCount > 0">{{ unreadCount }}条未读</text>
      </view>

      <button class="mark-all-btn" v-if="unreadCount > 0" @click="markAllRead">
        全部标为已读
      </button>

      <view class="notif-list" v-if="notifications.length > 0">
        <view
          class="notif-item"
          :class="{ unread: n.status === 0 }"
          v-for="n in notifications"
          :key="n.id"
          @click="readNotif(n)"
        >
          <view class="notif-dot" v-if="n.status === 0"></view>
          <view class="notif-body">
            <text class="notif-title">{{ n.title || '通知' }}</text>
            <text class="notif-content">{{ n.content || '' }}</text>
            <text class="notif-time" v-if="n.sendTime">{{ formatDt(n.sendTime) }}</text>
          </view>
        </view>
      </view>

      <view class="empty-inner" v-else>
        <text>暂无通知</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import {
  getRemindSettings,
  updateRemindSettings,
  getMyNotifications,
  countUnread,
  markNotificationRead,
  markAllNotificationsRead
} from '../../api/remind.js'

const ringtoneOptions = ['默认铃声', '柔和提示', '经典闹铃', '鸟鸣', '钢琴']
const repeatModes = [
  { label: '不重复', value: 'none' },
  { label: '每天', value: 'daily' },
  { label: '工作日', value: 'weekday' },
  { label: '自定义', value: 'custom' }
]

const settings = reactive({
  ringtone: '默认铃声',
  volume: 80,
  repeatMode: 'daily',
  quietTime: ''
})
const quietStart = ref('')
const quietEnd = ref('')
const saving = ref(false)

const notifications = ref([])
const unreadCount = ref(0)

function formatDt(dt) {
  if (!dt) return ''
  return String(dt).substring(0, 16).replace('T', ' ')
}

function onRingtoneChange(e) {
  settings.ringtone = ringtoneOptions[e.detail.value]
}

function onQuietStart(e) {
  quietStart.value = e.detail.value
  updateQuietTime()
}

function onQuietEnd(e) {
  quietEnd.value = e.detail.value
  updateQuietTime()
}

function updateQuietTime() {
  if (quietStart.value && quietEnd.value) {
    settings.quietTime = `${quietStart.value}-${quietEnd.value}`
  }
}

async function loadSettings() {
  try {
    const s = await getRemindSettings()
    if (s) {
      settings.ringtone = s.ringtone || '默认铃声'
      settings.volume = s.volume !== undefined ? s.volume : 80
      settings.repeatMode = s.repeatMode || 'daily'
      settings.quietTime = s.quietTime || ''
      if (s.quietTime) {
        const parts = s.quietTime.split('-')
        quietStart.value = parts[0] || ''
        quietEnd.value = parts[1] || ''
      }
    }
  } catch (e) {
    console.error('加载提醒设置失败:', e)
  }
}

async function saveSettings() {
  saving.value = true
  try {
    await updateRemindSettings({
      ringtone: settings.ringtone,
      volume: settings.volume,
      repeatMode: settings.repeatMode,
      quietTime: settings.quietTime || undefined
    })
    uni.showToast({ title: '设置已保存', icon: 'success', duration: 1500 })
  } catch (e) {
    uni.showToast({ title: e.message || '保存失败', icon: 'none', duration: 2000 })
  } finally {
    saving.value = false
  }
}

async function loadNotifications() {
  try {
    const [list, count] = await Promise.all([
      getMyNotifications().catch(() => []),
      countUnread().catch(() => 0)
    ])
    notifications.value = Array.isArray(list) ? list : []
    unreadCount.value = count !== null ? count : 0
  } catch (e) {
    console.error('加载通知失败:', e)
  }
}

async function readNotif(n) {
  if (n.status === 1) return
  try {
    await markNotificationRead(n.id)
    n.status = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  } catch (e) {
    // ignore
  }
}

async function markAllRead() {
  try {
    await markAllNotificationsRead()
    notifications.value.forEach(n => n.status = 1)
    unreadCount.value = 0
    uni.showToast({ title: '已全部标为已读', icon: 'success', duration: 1500 })
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none', duration: 2000 })
  }
}

loadSettings()
loadNotifications()
</script>

<style lang="scss" scoped>
@import '../../styles/common.scss';

.page-container {
  min-height: 100vh;
  background: $color-bg;
  padding-bottom: 160rpx;
}

// ========== 卡片 ==========
.card {
  background: $color-white;
  border-radius: $elder-radius-lg;
  padding: $elder-spacing-lg;
  margin: $elder-spacing $elder-spacing-xl;
  box-shadow: $elder-shadow;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.card-title {
  font-size: $elder-font-large;
  font-weight: bold;
  color: $color-text;
  display: block;
  margin-bottom: 20rpx;
}

// ========== 表单 ==========
.form-item {
  margin-bottom: 28rpx;
}

.form-label {
  display: block;
  font-size: $elder-font-base;
  font-weight: 600;
  color: $color-text;
  margin-bottom: 12rpx;
}

.picker-value {
  height: 80rpx;
  background: #f5f7fa;
  border-radius: $elder-radius;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: $elder-font-base;
}

.placeholder { color: $color-text-placeholder; }
.arrow { font-size: 24rpx; color: #ccc; }

// ========== 音量滑块 ==========
.slider-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.slider-label { font-size: 28rpx; flex-shrink: 0; }

.vol-slider {
  flex: 1;
}

// ========== Chip ==========
.chip-row {
  display: flex;
  gap: 12rpx;
}

.chip {
  flex: 1;
  height: 64rpx;
  line-height: 64rpx;
  text-align: center;
  background: #f5f7fa;
  border-radius: $elder-radius;
  font-size: $elder-font-small;
  color: $color-text-light;
  border: 2rpx solid transparent;

  &.active {
    background: rgba(64,158,255,0.1);
    border-color: $color-primary;
    color: $color-primary;
    font-weight: bold;
  }
}

// ========== 时间选择 ==========
.picker-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.time-pick {
  height: 80rpx;
  line-height: 80rpx;
  padding: 0 32rpx;
  background: #f5f7fa;
  border-radius: $elder-radius;
  font-size: $elder-font-base;
  color: $color-text;
}

.time-sep { font-size: $elder-font-base; color: $color-text-placeholder; }

// ========== 保存按钮 ==========
.save-btn {
  width: 100%;
  height: $elder-btn-height;
  line-height: $elder-btn-height;
  background: linear-gradient(135deg, $color-primary, #36cfc9);
  color: #fff;
  border-radius: $elder-radius-lg;
  font-size: $elder-font-medium;
  font-weight: bold;
  border: none;
  margin-top: 12rpx;
}

// ========== 未读 ==========
.unread-badge {
  font-size: 22rpx;
  color: $color-white;
  background: $color-danger;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
}

.mark-all-btn {
  width: 200rpx;
  height: 56rpx;
  line-height: 56rpx;
  background: #f5f7fa;
  color: $color-primary;
  border-radius: 28rpx;
  font-size: 24rpx;
  border: none;
  margin-bottom: 20rpx;
}

// ========== 通知列表 ==========
.notif-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.notif-item {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  padding: 20rpx;
  border-radius: $elder-radius;
  background: #fafafa;

  &.unread { background: #f0f7ff; }
}

.notif-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: $color-danger;
  margin-top: 6rpx;
  flex-shrink: 0;
}

.notif-body { flex: 1; }

.notif-title {
  display: block;
  font-size: $elder-font-base;
  font-weight: 600;
  color: $color-text;
  margin-bottom: 4rpx;
}

.notif-content {
  display: block;
  font-size: $elder-font-small;
  color: $color-text-light;
  margin-bottom: 4rpx;
}

.notif-time {
  font-size: 22rpx;
  color: $color-text-placeholder;
}

// ========== 空 ==========
.empty-inner { text-align: center; padding: 40rpx 0; font-size: $elder-font-small; color: $color-text-placeholder; }
</style>
