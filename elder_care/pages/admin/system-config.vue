<template>
  <view class="admin-container">
    <view class="admin-header">
      <text class="admin-title">⚙️ 系统配置</text>
      <text class="admin-desc">管理系统运行参数</text>
    </view>

    <!-- 配置列表 -->
    <view class="config-list" v-if="configs.length > 0">
      <view class="config-card" v-for="c in configs" :key="c.id || c.configKey">
        <view class="config-header">
          <text class="config-key">{{ c.configKey }}</text>
          <text class="config-desc" v-if="c.description">{{ c.description }}</text>
        </view>

        <view class="edit-row" v-if="editingKey === c.configKey">
          <input class="edit-input" v-model="editValue" />
          <button class="save-inline" @click="saveEdit(c)">保存</button>
          <button class="cancel-inline" @click="cancelEdit">取消</button>
        </view>

        <view class="view-row" v-else>
          <text class="config-value">{{ c.configValue || '未设置' }}</text>
          <text class="update-time" v-if="c.updateTime">更新: {{ formatDt(c.updateTime) }}</text>
          <button class="edit-inline" @click="startEdit(c)">编辑</button>
        </view>
      </view>
    </view>

    <view class="empty" v-else>
      <text>暂无配置项</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { getAllConfigs, updateConfig } from '../../api/admin.js'

const configs = ref([])
const editingKey = ref('')
const editValue = ref('')

function formatDt(dt) {
  if (!dt) return ''
  return String(dt).substring(0, 16).replace('T', ' ')
}

async function loadConfigs() {
  try {
    const list = await getAllConfigs()
    configs.value = Array.isArray(list) ? list : []
  } catch (e) {
    console.error('加载系统配置失败:', e)
  }
}

function startEdit(c) {
  editingKey.value = c.configKey
  editValue.value = c.configValue || ''
}

function cancelEdit() {
  editingKey.value = ''
  editValue.value = ''
}

async function saveEdit(c) {
  try {
    await updateConfig(c.configKey, editValue.value)
    uni.showToast({ title: '更新成功', icon: 'success', duration: 1500 })
    editingKey.value = ''
    loadConfigs()
  } catch (e) {
    uni.showToast({ title: e.message || '更新失败', icon: 'none', duration: 2000 })
  }
}

loadConfigs()
</script>

<style lang="scss" scoped>
@import '../../styles/common.scss';

.admin-container {
  min-height: 100vh;
  background: #f0f2f5;
  padding: 24rpx 28rpx 120rpx;
}

.admin-header {
  padding: 32rpx 0 24rpx;
}

.admin-title {
  font-size: 40rpx;
  font-weight: bold;
  color: $color-text;
  display: block;
  margin-bottom: 8rpx;
}

.admin-desc {
  font-size: 26rpx;
  color: $color-text-placeholder;
}

// ========== 配置列表 ==========
.config-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.config-card {
  background: $color-white;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}

.config-header {
  margin-bottom: 14rpx;
}

.config-key {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: $color-text;
  font-family: monospace;
  margin-bottom: 4rpx;
}

.config-desc {
  font-size: 22rpx;
  color: $color-text-placeholder;
}

// ========== 查看行 ==========
.view-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.config-value {
  flex: 1;
  font-size: 28rpx;
  color: $color-primary;
  font-weight: 600;
  word-break: break-all;
}

.update-time {
  font-size: 20rpx;
  color: #ccc;
  flex-shrink: 0;
}

.edit-inline {
  height: 52rpx;
  line-height: 52rpx;
  padding: 0 20rpx;
  background: #f5f7fa;
  color: $color-primary;
  border-radius: 8rpx;
  font-size: 22rpx;
  border: none;
  flex-shrink: 0;
}

// ========== 编辑行 ==========
.edit-row {
  display: flex;
  gap: 12rpx;
  align-items: center;
}

.edit-input {
  flex: 1;
  height: 64rpx;
  background: #f5f7fa;
  border-radius: 10rpx;
  padding: 0 16rpx;
  font-size: 26rpx;
  border: 2rpx solid $color-primary;
}

.save-inline {
  height: 64rpx;
  line-height: 64rpx;
  padding: 0 20rpx;
  background: $color-primary;
  color: #fff;
  border-radius: 10rpx;
  font-size: 24rpx;
  border: none;
}

.cancel-inline {
  height: 64rpx;
  line-height: 64rpx;
  padding: 0 20rpx;
  background: #f5f5f5;
  color: $color-text-light;
  border-radius: 10rpx;
  font-size: 24rpx;
  border: none;
}

// ========== 空 ==========
.empty { text-align: center; padding: 80rpx 0; font-size: 28rpx; color: $color-text-placeholder; }
</style>
