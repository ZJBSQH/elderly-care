<template>
  <view class="page-container">
    <!-- 标题 -->
    <view class="page-header">
      <text class="page-title">{{ isEdit ? '编辑用药计划' : '添加用药计划' }}</text>
      <text class="page-desc">设置提醒，按时服药</text>
    </view>

    <!-- 表单 -->
    <view class="form-card">
      <!-- 药品名称 -->
      <view class="form-item">
        <text class="form-label">药品名称 <text class="required">*</text></text>
        <input
          class="form-input"
          v-model="form.medicineName"
          placeholder="如：阿司匹林肠溶片"
          maxlength="50"
        />
      </view>

      <!-- 剂量 -->
      <view class="form-item">
        <text class="form-label">剂量 <text class="required">*</text></text>
        <input
          class="form-input"
          v-model="form.dosage"
          placeholder="如：100mg / 1片"
          maxlength="20"
        />
      </view>

      <!-- 提醒时间 -->
      <view class="form-item">
        <text class="form-label">提醒时间</text>
        <picker mode="time" :value="form.remindTime" @change="onTimeChange">
          <view class="picker-value">
            <text :class="{ placeholder: !form.remindTime }">
              {{ form.remindTime || '选择时间（可选）' }}
            </text>
            <text class="picker-arrow">⏰</text>
          </view>
        </picker>
      </view>

      <!-- 服药频率 -->
      <view class="form-item">
        <text class="form-label">服药频率 <text class="required">*</text></text>
        <view class="freq-group">
          <view
            class="freq-item"
            :class="{ active: form.frequency === f.value }"
            v-for="f in freqOptions"
            :key="f.value"
            @click="form.frequency = f.value"
          >
            <text>{{ f.label }}</text>
          </view>
        </view>
      </view>

      <!-- 开始日期 -->
      <view class="form-item">
        <text class="form-label">开始日期 <text class="required">*</text></text>
        <picker mode="date" :value="form.startDate" @change="onStartDateChange">
          <view class="picker-value">
            <text :class="{ placeholder: !form.startDate }">
              {{ form.startDate || '选择开始日期' }}
            </text>
            <text class="picker-arrow">📅</text>
          </view>
        </picker>
      </view>

      <!-- 结束日期 -->
      <view class="form-item">
        <text class="form-label">结束日期 <text class="required">*</text></text>
        <picker mode="date" :value="form.endDate" @change="onEndDateChange">
          <view class="picker-value">
            <text :class="{ placeholder: !form.endDate }">
              {{ form.endDate || '选择结束日期' }}
            </text>
            <text class="picker-arrow">📅</text>
          </view>
        </picker>
      </view>

      <!-- 提交按钮 -->
      <button class="submit-btn" :loading="loading" @click="handleSubmit">
        {{ isEdit ? '保存修改' : '添加用药计划' }}
      </button>

      <!-- 编辑模式下显示删除 -->
      <button class="delete-btn" v-if="isEdit" @click="handleDelete">
        删除此用药计划
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../../store/user.js'
import { addMedicine, updateMedicine, deleteMedicine, getMedicineList } from '../../api/medicine.js'

const userStore = useUserStore()

const isEdit = ref(false)
const editId = ref(null)
const loading = ref(false)

const freqOptions = [
  { label: '每日一次', value: '每日一次' },
  { label: '每日两次', value: '每日两次' },
  { label: '每日三次', value: '每日三次' },
  { label: '按需服用', value: '按需服用' }
]

const form = reactive({
  medicineName: '',
  dosage: '',
  remindTime: '',
  frequency: '每日一次',
  startDate: '',
  endDate: ''
})

/** UniApp picker change 返回 { detail: { value } } */
function onTimeChange(e) {
  form.remindTime = e.detail.value
}

function onStartDateChange(e) {
  form.startDate = e.detail.value
}

function onEndDateChange(e) {
  form.endDate = e.detail.value
}

/** 加载待编辑的用药计划 */
async function loadMedicine(id) {
  try {
    const elderId = userStore.elderId
    if (!elderId) return
    const list = await getMedicineList(elderId)
    const item = Array.isArray(list) ? list.find(m => m.id === id) : null
    if (item) {
      form.medicineName = item.medicineName || ''
      form.dosage = item.dosage || ''
      form.remindTime = item.remindTime || ''
      form.frequency = item.frequency || '每日一次'
      form.startDate = item.startDate || ''
      form.endDate = item.endDate || ''
    }
  } catch (e) {
    console.error('加载用药计划失败:', e)
  }
}

/** 提交 */
async function handleSubmit() {
  if (!form.medicineName.trim()) {
    return uni.showToast({ title: '请输入药品名称', icon: 'none', duration: 2000 })
  }
  if (!form.dosage.trim()) {
    return uni.showToast({ title: '请输入剂量', icon: 'none', duration: 2000 })
  }
  if (!form.frequency) {
    return uni.showToast({ title: '请选择服用频率', icon: 'none', duration: 2000 })
  }
  if (!form.startDate) {
    return uni.showToast({ title: '请选择开始日期', icon: 'none', duration: 2000 })
  }
  if (!form.endDate) {
    return uni.showToast({ title: '请选择结束日期', icon: 'none', duration: 2000 })
  }
  if (form.startDate > form.endDate) {
    return uni.showToast({ title: '结束日期不能早于开始日期', icon: 'none', duration: 2000 })
  }

  const elderId = userStore.elderId
  if (!elderId) {
    return uni.showToast({ title: '无法获取用户信息', icon: 'none', duration: 2000 })
  }

  loading.value = true
  try {
    const payload = {
      medicineName: form.medicineName.trim(),
      dosage: form.dosage.trim(),
      remindTime: form.remindTime || undefined,
      frequency: form.frequency,
      startDate: form.startDate,
      endDate: form.endDate,
      elderId
    }

    if (isEdit.value) {
      payload.id = editId.value
      await updateMedicine(payload)
      uni.showToast({ title: '修改成功', icon: 'success', duration: 1500 })
    } else {
      await addMedicine(payload)
      uni.showToast({ title: '添加成功', icon: 'success', duration: 1500 })
    }

    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) {
    uni.showToast({ title: e.message || '操作失败', icon: 'none', duration: 2000 })
  } finally {
    loading.value = false
  }
}

/** 删除 */
async function handleDelete() {
  uni.showModal({
    title: '确认删除',
    content: '删除后无法恢复，确定要删除此用药计划吗？',
    confirmText: '确定删除',
    confirmColor: '#FF6B6B',
    cancelText: '取消',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteMedicine(editId.value)
          uni.showToast({ title: '已删除', icon: 'success', duration: 1500 })
          setTimeout(() => uni.navigateBack(), 1500)
        } catch (e) {
          uni.showToast({ title: e.message || '删除失败', icon: 'none', duration: 2000 })
        }
      }
    }
  })
}

onMounted(() => {
  const pages = getCurrentPages()
  const current = pages[pages.length - 1]
  const options = current?.$page?.options || {}
  if (options.id) {
    isEdit.value = true
    editId.value = parseInt(options.id, 10)
    loadMedicine(editId.value)
  }
})
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
  padding: 20rpx 0 40rpx;
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

// ========== 表单卡片 ==========
.form-card {
  background: $color-white;
  border-radius: $elder-radius-lg;
  padding: $elder-spacing-lg;
  box-shadow: $elder-shadow;
}

.form-item {
  margin-bottom: 36rpx;
}

.form-label {
  display: block;
  font-size: $elder-font-base;
  font-weight: 600;
  color: $color-text;
  margin-bottom: 12rpx;
}

.required {
  color: $color-danger;
}

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

.placeholder {
  color: $color-text-placeholder;
}

.picker-arrow {
  font-size: 36rpx;
}

// ========== 频率选择 ==========
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
  transition: all 0.2s;

  &.active {
    background: rgba(64, 158, 255, 0.1);
    border-color: $color-primary;
    color: $color-primary;
    font-weight: bold;
  }
}

// ========== 提交按钮 ==========
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
  margin-top: 20rpx;

  &[disabled] { opacity: 0.7; }
}

.delete-btn {
  width: 100%;
  height: $elder-btn-height;
  line-height: $elder-btn-height;
  background: $color-white;
  color: $color-danger;
  border-radius: $elder-radius-lg;
  font-size: $elder-font-medium;
  border: 2rpx solid $color-danger;
  margin-top: 24rpx;
}
</style>
