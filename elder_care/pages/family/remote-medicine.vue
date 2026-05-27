<template>
  <view class="page-container">
    <view class="page-header">
      <text class="page-title">💊 用药管理</text>
      <text class="page-desc">远程管理家人的用药计划</text>
    </view>

    <!-- 用药计划列表 -->
    <view class="section-title" v-if="medicineList.length > 0">用药计划列表</view>
    <view class="plan-list" v-if="medicineList.length > 0 && !editingItem">
      <view class="plan-card" v-for="m in medicineList" :key="m.id">
        <view class="plan-main">
          <view class="plan-info">
            <text class="plan-name">{{ m.medicineName }}</text>
            <text class="plan-meta">{{ m.dosage || '--' }} · {{ m.frequency || '--' }}</text>
            <text class="plan-date" v-if="m.startDate || m.endDate">
              {{ m.startDate || '?' }} ~ {{ m.endDate || '?' }}
            </text>
          </view>
          <view class="plan-status" :class="m.status === 0 ? 'inactive' : 'active'">
            {{ m.status === 0 ? '已停用' : '使用中' }}
          </view>
        </view>
        <view class="plan-actions">
          <button class="edit-btn" @click="startEdit(m)">✏️ 修改</button>
        </view>
      </view>
    </view>

    <!-- 编辑表单 -->
    <view class="form-card" v-if="editingItem">
      <text class="form-title">修改用药计划</text>

      <view class="form-item">
        <text class="form-label">药品名称 <text class="required">*</text></text>
        <input class="form-input" v-model="editForm.medicineName" placeholder="药品名称" maxlength="50" />
      </view>

      <view class="form-item">
        <text class="form-label">剂量</text>
        <input class="form-input" v-model="editForm.dosage" placeholder="如：100mg / 1片" maxlength="20" />
      </view>

      <view class="form-item">
        <text class="form-label">提醒时间</text>
        <picker mode="time" :value="editForm.remindTime" @change="onTimeChange">
          <view class="picker-value">
            <text :class="{ placeholder: !editForm.remindTime }">
              {{ editForm.remindTime || '选择时间（可选）' }}
            </text>
            <text class="picker-arrow">⏰</text>
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label">服药频率</text>
        <view class="freq-group">
          <view
            class="freq-item"
            :class="{ active: editForm.frequency === f.value }"
            v-for="f in freqOptions"
            :key="f.value"
            @click="editForm.frequency = f.value"
          >
            <text>{{ f.label }}</text>
          </view>
        </view>
      </view>

      <view class="form-item">
        <text class="form-label">开始日期</text>
        <picker mode="date" :value="editForm.startDate" @change="onStartChange">
          <view class="picker-value">
            <text :class="{ placeholder: !editForm.startDate }">{{ editForm.startDate || '选择日期' }}</text>
            <text class="picker-arrow">📅</text>
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label">结束日期</text>
        <picker mode="date" :value="editForm.endDate" @change="onEndChange">
          <view class="picker-value">
            <text :class="{ placeholder: !editForm.endDate }">{{ editForm.endDate || '选择日期' }}</text>
            <text class="picker-arrow">📅</text>
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="form-label">状态</text>
        <view class="status-switch">
          <view
            class="status-opt"
            :class="{ active: editForm.status === 1 }"
            @click="editForm.status = 1"
          >使用中</view>
          <view
            class="status-opt"
            :class="{ active: editForm.status === 0 }"
            @click="editForm.status = 0"
          >已停用</view>
        </view>
      </view>

      <view class="form-btns">
        <button class="submit-btn" :loading="loading" @click="handleUpdate">保存修改</button>
        <button class="cancel-btn" @click="cancelEdit">取消</button>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-card" v-if="medicineList.length === 0 && !loading">
      <text class="empty-icon">💊</text>
      <text class="empty-text">暂无用药计划</text>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getElderMedicinePlan, remoteUpdateMedicine } from '../../api/family.js'

const elderId = ref(null)
const medicineList = ref([])
const editingItem = ref(null)
const loading = ref(false)

const freqOptions = [
  { label: '每日一次', value: '每日一次' },
  { label: '每日两次', value: '每日两次' },
  { label: '每日三次', value: '每日三次' },
  { label: '按需服用', value: '按需服用' }
]

const editForm = reactive({
  medicineName: '',
  dosage: '',
  remindTime: '',
  frequency: '每日一次',
  startDate: '',
  endDate: '',
  status: 1
})

function onTimeChange(e) { editForm.remindTime = e.detail.value }
function onStartChange(e) { editForm.startDate = e.detail.value }
function onEndChange(e) { editForm.endDate = e.detail.value }

function startEdit(item) {
  editingItem.value = item
  editForm.medicineName = item.medicineName || ''
  editForm.dosage = item.dosage || ''
  editForm.remindTime = item.remindTime || ''
  editForm.frequency = item.frequency || '每日一次'
  editForm.startDate = item.startDate || ''
  editForm.endDate = item.endDate || ''
  editForm.status = item.status !== undefined ? item.status : 1
}

function cancelEdit() {
  editingItem.value = null
}

async function handleUpdate() {
  if (!editForm.medicineName.trim()) {
    return uni.showToast({ title: '请输入药品名称', icon: 'none', duration: 2000 })
  }
  if (editForm.startDate && editForm.endDate && editForm.startDate > editForm.endDate) {
    return uni.showToast({ title: '结束日期不能早于开始日期', icon: 'none', duration: 2000 })
  }

  loading.value = true
  try {
    await remoteUpdateMedicine({
      medicineId: editingItem.value.id,
      medicineName: editForm.medicineName.trim(),
      dosage: editForm.dosage.trim() || undefined,
      remindTime: editForm.remindTime || undefined,
      frequency: editForm.frequency || undefined,
      startDate: editForm.startDate || undefined,
      endDate: editForm.endDate || undefined,
      status: editForm.status
    })
    uni.showToast({ title: '修改成功', icon: 'success', duration: 1500 })
    editingItem.value = null
    await loadPlan()
  } catch (e) {
    uni.showToast({ title: e.message || '修改失败', icon: 'none', duration: 2000 })
  } finally {
    loading.value = false
  }
}

async function loadPlan() {
  if (!elderId.value) return
  try {
    const list = await getElderMedicinePlan(elderId.value)
    medicineList.value = Array.isArray(list) ? list : []
  } catch (e) {
    console.error('加载用药计划失败:', e)
    medicineList.value = []
  }
}

onMounted(() => {
  const pages = getCurrentPages()
  const opts = pages[pages.length - 1]?.$page?.options || {}
  elderId.value = parseInt(opts.elderId, 10) || null
  loadPlan()
})
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
  padding: 40rpx $elder-spacing-xl 24rpx;
}

.page-title {
  display: block;
  font-size: $elder-font-xl;
  font-weight: bold;
  color: $color-text;
  margin-bottom: 8rpx;
}

.page-desc {
  display: block;
  font-size: $elder-font-base;
  color: $color-text-placeholder;
}

.section-title {
  font-size: $elder-font-large;
  font-weight: bold;
  color: $color-text;
  padding: $elder-spacing $elder-spacing-xl 8rpx;
}

// ========== 计划列表 ==========
.plan-list {
  padding: $elder-spacing $elder-spacing-xl;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.plan-card {
  background: $color-white;
  border-radius: $elder-radius-lg;
  padding: 28rpx;
  box-shadow: $elder-shadow;
}

.plan-main {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
}

.plan-info { flex: 1; }

.plan-name {
  display: block;
  font-size: $elder-font-medium;
  font-weight: bold;
  color: $color-text;
  margin-bottom: 6rpx;
}

.plan-meta {
  display: block;
  font-size: $elder-font-small;
  color: $color-text-light;
}

.plan-date {
  display: block;
  font-size: 22rpx;
  color: $color-text-placeholder;
  margin-top: 4rpx;
}

.plan-status {
  font-size: 22rpx;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
  &.active { background: #f6ffed; color: $color-success; }
  &.inactive { background: #f5f5f5; color: $color-text-placeholder; }
}

.plan-actions {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 2rpx solid #f5f5f5;
}

.edit-btn {
  width: 100%;
  height: 64rpx;
  line-height: 64rpx;
  text-align: center;
  background: #f5f7fa;
  border-radius: $elder-radius;
  font-size: $elder-font-small;
  color: $color-primary;
  border: none;

  &:active { background: #eef2f8; }
}

// ========== 编辑表单 ==========
.form-card {
  background: $color-white;
  border-radius: $elder-radius-lg;
  padding: $elder-spacing-lg;
  margin: $elder-spacing $elder-spacing-xl;
  box-shadow: $elder-shadow;
}

.form-title {
  display: block;
  font-size: $elder-font-large;
  font-weight: bold;
  color: $color-text;
  margin-bottom: 24rpx;
}

.form-item {
  margin-bottom: 32rpx;
}

.form-label {
  display: block;
  font-size: $elder-font-base;
  font-weight: 600;
  color: $color-text;
  margin-bottom: 12rpx;
}

.required { color: $color-danger; }

.form-input {
  height: 88rpx;
  background: #f5f7fa;
  border-radius: $elder-radius;
  padding: 0 24rpx;
  font-size: $elder-font-base;
  color: $color-text;
}

.picker-value {
  height: 88rpx;
  background: #f5f7fa;
  border-radius: $elder-radius;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: $elder-font-base;
  color: $color-text;
}

.placeholder { color: $color-text-placeholder; }
.picker-arrow { font-size: 36rpx; }

.freq-group {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.freq-item {
  flex: 1;
  min-width: 150rpx;
  height: 72rpx;
  line-height: 72rpx;
  text-align: center;
  background: #f5f7fa;
  border-radius: $elder-radius;
  font-size: $elder-font-small;
  color: $color-text-light;
  border: 2rpx solid transparent;

  &.active {
    background: rgba(64, 158, 255, 0.1);
    border-color: $color-primary;
    color: $color-primary;
    font-weight: bold;
  }
}

.status-switch {
  display: flex;
  gap: 16rpx;
}

.status-opt {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  text-align: center;
  background: #f5f7fa;
  border-radius: $elder-radius;
  font-size: $elder-font-small;
  color: $color-text-light;
  border: 2rpx solid transparent;

  &.active {
    background: rgba(64, 158, 255, 0.1);
    border-color: $color-primary;
    color: $color-primary;
    font-weight: bold;
  }
}

// ========== 按钮 ==========
.form-btns {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-top: 32rpx;
}

.submit-btn {
  width: 100%;
  height: $elder-btn-height;
  line-height: $elder-btn-height;
  background: linear-gradient(135deg, $color-primary 0%, #36cfc9 100%);
  color: #fff;
  border-radius: $elder-radius-lg;
  font-size: $elder-font-medium;
  font-weight: bold;
  border: none;
}

.cancel-btn {
  width: 100%;
  height: $elder-btn-height;
  line-height: $elder-btn-height;
  background: $color-white;
  color: $color-text-light;
  border-radius: $elder-radius-lg;
  font-size: $elder-font-medium;
  border: 2rpx solid $color-border;
}

// ========== 空状态 ==========
.empty-card {
  text-align: center;
  padding: 120rpx 0;
}

.empty-icon { font-size: 80rpx; display: block; margin-bottom: 24rpx; }
.empty-text { font-size: $elder-font-medium; color: $color-text-placeholder; }
</style>
