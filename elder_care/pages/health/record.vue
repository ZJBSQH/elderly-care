<template>
  <view class="page-container">
    <view class="page-header">
      <text class="page-title">{{ isEdit ? '修改健康数据' : '录入健康数据' }}</text>
      <text class="page-desc">记录您的健康状况</text>
    </view>

    <view class="form-card">
      <!-- 血压 -->
      <view class="form-item">
        <text class="form-label">血压</text>
        <input
          class="form-input"
          v-model="form.bloodPressure"
          placeholder="收缩压/舒张压，如 120/80"
          maxlength="10"
        />
        <text class="form-hint">格式：收缩压/舒张压 (mmHg)</text>
      </view>

      <!-- 血糖 -->
      <view class="form-item">
        <text class="form-label">血糖</text>
        <view class="input-with-unit">
          <input
            class="form-input flex-1"
            type="digit"
            v-model="bloodSugarStr"
            placeholder="如 5.6"
          />
          <text class="unit-text">mmol/L</text>
        </view>
        <text class="form-hint">空腹血糖正常值：3.9-6.1 mmol/L</text>
      </view>

      <!-- 心率 -->
      <view class="form-item">
        <text class="form-label">心率</text>
        <view class="input-with-unit">
          <input
            class="form-input flex-1"
            type="number"
            v-model="heartRateStr"
            placeholder="如 72"
            maxlength="3"
          />
          <text class="unit-text">次/分</text>
        </view>
        <text class="form-hint">正常安静心率：60-100 次/分</text>
      </view>

      <!-- 体重 -->
      <view class="form-item">
        <text class="form-label">体重</text>
        <view class="input-with-unit">
          <input
            class="form-input flex-1"
            type="digit"
            v-model="weightStr"
            placeholder="如 65.0"
          />
          <text class="unit-text">kg</text>
        </view>
      </view>

      <!-- 预警开关 -->
      <view class="form-item">
        <view class="switch-row" @click="enableAlert = !enableAlert">
          <view class="switch-label">
            <text class="form-label no-mb">自动健康预警</text>
            <text class="form-hint">提交后自动分析指标是否异常</text>
          </view>
          <view class="switch" :class="{ on: enableAlert }">
            <view class="switch-knob"></view>
          </view>
        </view>
      </view>

      <button class="submit-btn" :loading="loading" @click="handleSubmit">
        {{ isEdit ? '保存修改' : '提交记录' }}
      </button>

      <button class="delete-btn" v-if="isEdit" @click="handleDelete">删除此记录</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../../store/user.js'
import { saveHealth, updateHealthRecord, deleteHealthRecord } from '../../api/health.js'

const userStore = useUserStore()

const isEdit = ref(false)
const editId = ref(null)
const loading = ref(false)
const enableAlert = ref(true)

const form = reactive({
  bloodPressure: '',
  bloodSugar: null,
  heartRate: null,
  weight: null
})

// 字符串代理字段，用于 input v-model
const bloodSugarStr = ref('')
const heartRateStr = ref('')
const weightStr = ref('')

function parseForm() {
  form.bloodSugar = bloodSugarStr.value ? parseFloat(bloodSugarStr.value) : null
  form.heartRate = heartRateStr.value ? parseInt(heartRateStr.value, 10) : null
  form.weight = weightStr.value ? parseFloat(weightStr.value) : null
}

/** 加载编辑数据 */
async function loadRecord(id) {
  try {
    // 从历史列表中加载需要额外查询，这里通过 URL 参数传入数据较复杂
    // 简化方案：仅支持核心字段回填
    const pages = getCurrentPages()
    const current = pages[pages.length - 1]
    const opts = current?.$page?.options || {}

    if (opts.bp) form.bloodPressure = decodeURIComponent(opts.bp)
    if (opts.sugar) { bloodSugarStr.value = opts.sugar; parseForm() }
    if (opts.hr) { heartRateStr.value = opts.hr; parseForm() }
    if (opts.weight) { weightStr.value = opts.weight; parseForm() }
  } catch (e) {
    console.error('加载记录失败:', e)
  }
}

/** 提交 */
async function handleSubmit() {
  parseForm()

  if (!form.bloodPressure && form.bloodSugar === null && form.heartRate === null && form.weight === null) {
    return uni.showToast({ title: '请至少填写一项健康数据', icon: 'none', duration: 2000 })
  }

  const elderId = userStore.elderId
  if (!elderId) {
    return uni.showToast({ title: '无法获取老人信息', icon: 'none', duration: 2000 })
  }

  loading.value = true
  try {
    const payload = { ...form, elderId }

    if (isEdit.value) {
      await updateHealthRecord(editId.value, payload)
      uni.showToast({ title: '修改成功', icon: 'success', duration: 1500 })
    } else {
      await saveHealth(payload)
      uni.showToast({ title: '录入成功', icon: 'success', duration: 1500 })
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
    content: '删除后无法恢复',
    confirmText: '确定',
    confirmColor: '#FF6B6B',
    cancelText: '取消',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteHealthRecord(editId.value)
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
  const opts = current?.$page?.options || {}
  if (opts.id) {
    isEdit.value = true
    editId.value = parseInt(opts.id, 10)
    loadRecord(editId.value)
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
  padding: 20rpx 0 30rpx;
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

// ========== 表单 ==========
.form-card {
  background: $color-white;
  border-radius: $elder-radius-lg;
  padding: $elder-spacing-lg;
  box-shadow: $elder-shadow;
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

  &.no-mb { margin-bottom: 0; }
}

.form-input {
  height: 88rpx;
  background: #f5f7fa;
  border-radius: $elder-radius;
  padding: 0 24rpx;
  font-size: $elder-font-base;
  color: $color-text;
}

.flex-1 { flex: 1; }

.form-hint {
  display: block;
  font-size: 24rpx;
  color: $color-text-placeholder;
  margin-top: 8rpx;
}

.input-with-unit {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.unit-text {
  font-size: $elder-font-base;
  color: $color-text-light;
  flex-shrink: 0;
}

// ========== 预警开关 ==========
.switch-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.switch {
  width: 88rpx;
  height: 52rpx;
  background: #ddd;
  border-radius: 26rpx;
  position: relative;
  transition: background 0.3s;
  flex-shrink: 0;

  &.on {
    background: $color-success;
  }
}

.switch-knob {
  width: 44rpx;
  height: 44rpx;
  background: #fff;
  border-radius: 50%;
  position: absolute;
  top: 4rpx;
  left: 4rpx;
  transition: left 0.3s;
  box-shadow: 0 2rpx 6rpx rgba(0,0,0,0.15);
}

.switch.on .switch-knob { left: 40rpx; }

// ========== 按钮 ==========
.submit-btn {
  width: 100%;
  height: $elder-btn-height;
  line-height: $elder-btn-height;
  background: linear-gradient(135deg, $color-success 0%, #36cfc9 100%);
  color: #fff;
  border-radius: $elder-radius-lg;
  font-size: $elder-font-medium;
  font-weight: bold;
  border: none;
  margin-top: 16rpx;

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
