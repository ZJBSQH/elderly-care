<template>
  <view class="page-container">
    <view class="page-header">
      <text class="page-icon">🔗</text>
      <text class="page-title">添加家人</text>
      <text class="page-desc">扫描老人端二维码或输入手机号绑定</text>
    </view>

    <!-- 绑定方式选择 -->
    <view class="bind-tabs">
      <view class="bind-tab" :class="{ active: bindMode === 'scan' }" @click="bindMode = 'scan'">
        <text class="tab-icon">📷</text>
        <text class="tab-label">扫码绑定</text>
      </view>
      <view class="bind-tab" :class="{ active: bindMode === 'phone' }" @click="bindMode = 'phone'">
        <text class="tab-icon">📱</text>
        <text class="tab-label">手机号绑定</text>
      </view>
    </view>

    <!-- 扫码绑定 -->
    <view class="card" v-if="bindMode === 'scan'">
      <text class="card-title">扫描老人端的二维码</text>
      <text class="card-desc">请让老人打开个人中心 → 我的二维码，扫描后即可绑定</text>
      <button class="scan-btn" @click="handleScan">
        <text class="scan-btn-icon">📷</text>
        <text>点击扫描二维码</text>
      </button>
      <view class="result-area" v-if="scanToken">
        <text class="result-label">已识别二维码</text>
        <button class="primary-btn" :loading="binding" @click="confirmScanBind">确认绑定</button>
      </view>
    </view>

    <!-- 手机号绑定 -->
    <view class="card" v-if="bindMode === 'phone'">
      <text class="card-title">通过手机号绑定</text>
      <view class="form-item">
        <text class="form-label">老人手机号</text>
        <input class="form-input" v-model="phoneForm.phone" type="number" placeholder="请输入老人手机号" maxlength="11" />
      </view>
      <view class="form-item">
        <text class="form-label">您与老人的关系</text>
        <view class="relation-group">
          <view
            class="relation-item"
            :class="{ active: relation === r }"
            v-for="r in relationOptions"
            :key="r"
            @click="relation = r"
          >
            <text>{{ r }}</text>
          </view>
        </view>
        <input
          class="form-input"
          v-if="relation === '其他'"
          v-model="customRelation"
          placeholder="请填写关系"
          maxlength="20"
        />
      </view>
      <view class="form-item">
        <text class="form-label">验证码</text>
        <view class="sms-row">
          <input class="form-input sms-input" v-model="phoneForm.code" type="number" placeholder="请输入验证码" maxlength="6" />
          <button class="sms-btn" :disabled="smsCountdown > 0" @click="sendSms">
            {{ smsCountdown > 0 ? smsCountdown + 's' : '获取验证码' }}
          </button>
        </view>
      </view>
      <button class="primary-btn" :loading="binding" @click="confirmPhoneBind">确认绑定</button>
    </view>

    <!-- 绑定成功 -->
    <view class="card success-card" v-if="step === 'success'">
      <text class="success-icon">✅</text>
      <text class="success-title">绑定成功</text>
      <text class="success-desc">现在你可以查看家人的健康数据了</text>
      <button class="primary-btn" @click="goBack">返回家属首页</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { parseQRCode, confirmBindElder } from '../../api/family.js'
import { sendSmsCode } from '../../api/auth.js'

const step = ref('input')
const bindMode = ref('scan')
const binding = ref(false)
const smsCountdown = ref(0)

// 扫码
const scanToken = ref('')

// 手机号表单
const phoneForm = reactive({ phone: '', code: '' })
const relation = ref('子女')
const customRelation = ref('')
const relationOptions = ['子女', '配偶', '兄弟姐妹', '父母', '孙辈', '其他']

/** 扫描二维码 */
async function handleScan() {
  try {
    const res = await uni.scanCode({ onlyFromCamera: true, scanType: ['qrCode'] })
    scanToken.value = res.result
    uni.showToast({ title: '已识别二维码', icon: 'success', duration: 1500 })
  } catch (e) {
    uni.showToast({ title: '扫码取消或失败', icon: 'none', duration: 2000 })
  }
}

/** 确认扫码绑定 */
async function confirmScanBind() {
  if (!scanToken.value) {
    return uni.showToast({ title: '请先扫描二维码', icon: 'none', duration: 2000 })
  }
  binding.value = true
  try {
    const elderInfo = await parseQRCode(scanToken.value)
    if (!elderInfo) {
      throw new Error('二维码无效或已过期')
    }
    await confirmBindElder({ qrCodeToken: scanToken.value, elderId: elderInfo.elderId })
    step.value = 'success'
  } catch (e) {
    uni.showToast({ title: e.message || '绑定失败', icon: 'none', duration: 2000 })
  } finally {
    binding.value = false
  }
}

/** 发送短信验证码 */
async function sendSms() {
  if (!phoneForm.phone || !/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    return uni.showToast({ title: '请输入正确的手机号', icon: 'none', duration: 2000 })
  }
  try {
    await sendSmsCode({ phone: phoneForm.phone, type: 'bind' })
    uni.showToast({ title: '验证码已发送', icon: 'success', duration: 1500 })
    smsCountdown.value = 60
    const timer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) {
    uni.showToast({ title: e.message || '发送失败', icon: 'none', duration: 2000 })
  }
}

/** 确认手机号绑定 */
async function confirmPhoneBind() {
  if (!phoneForm.phone || !/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    return uni.showToast({ title: '请输入正确的手机号', icon: 'none', duration: 2000 })
  }
  if (!phoneForm.code) {
    return uni.showToast({ title: '请输入验证码', icon: 'none', duration: 2000 })
  }
  const rel = relation.value === '其他' ? customRelation.value.trim() : relation.value
  if (!rel) {
    return uni.showToast({ title: '请选择与老人的关系', icon: 'none', duration: 2000 })
  }
  binding.value = true
  try {
    await confirmBindElder({
      elderPhone: phoneForm.phone,
      code: phoneForm.code,
      relation: rel
    })
    step.value = 'success'
  } catch (e) {
    uni.showToast({ title: e.message || '绑定失败', icon: 'none', duration: 2000 })
  } finally {
    binding.value = false
  }
}

function goBack() {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
@import '../../styles/common.scss';

.page-container {
  min-height: 100vh;
  background: $color-bg;
  padding-bottom: 160rpx;
}

.page-header { text-align: center; padding: 48rpx $elder-spacing-xl 32rpx; }
.page-icon { font-size: 80rpx; display: block; margin-bottom: 16rpx; }
.page-title { display: block; font-size: $elder-font-xl; font-weight: bold; color: $color-text; margin-bottom: 8rpx; }
.page-desc { display: block; font-size: $elder-font-base; color: $color-text-placeholder; }

// 绑定方式切换
.bind-tabs { display: flex; gap: 20rpx; margin: 0 $elder-spacing-xl 24rpx; }
.bind-tab {
  flex: 1; background: $color-white; border-radius: $elder-radius-lg; padding: 32rpx 0;
  text-align: center; border: 3rpx solid transparent; box-shadow: $elder-shadow; transition: all 0.2s;
  &.active { border-color: $color-primary; background: rgba(64, 158, 255, 0.06); }
}
.tab-icon { font-size: 48rpx; display: block; margin-bottom: 8rpx; }
.tab-label { font-size: $elder-font-base; color: $color-text; font-weight: 600; }

// 卡片
.card {
  background: $color-white; border-radius: $elder-radius-lg; padding: $elder-spacing-lg;
  margin: 0 $elder-spacing-xl 24rpx; box-shadow: $elder-shadow;
}
.card-title { display: block; font-size: $elder-font-large; font-weight: bold; color: $color-text; margin-bottom: 12rpx; }
.card-desc { display: block; font-size: $elder-font-small; color: $color-text-placeholder; margin-bottom: 24rpx; line-height: 1.6; }

// 扫码按钮
.scan-btn {
  width: 100%; height: 120rpx; line-height: 120rpx;
  background: linear-gradient(135deg, $color-primary, #36cfc9); color: #fff;
  border-radius: $elder-radius-lg; font-size: $elder-font-medium; font-weight: bold;
  border: none; display: flex; align-items: center; justify-content: center; gap: 12rpx;
}
.scan-btn-icon { font-size: 40rpx; }

.result-area { margin-top: 24rpx; padding: 20rpx; background: #f5f7fa; border-radius: $elder-radius; text-align: center; }
.result-label { font-size: $elder-font-small; color: $color-text-placeholder; display: block; margin-bottom: 16rpx; }

// 表单
.form-item { margin-bottom: 28rpx; }
.form-label { display: block; font-size: $elder-font-base; font-weight: 600; color: $color-text; margin-bottom: 12rpx; }
.form-input { height: 88rpx; background: #f5f7fa; border-radius: $elder-radius; padding: 0 24rpx; font-size: $elder-font-base; width: 100%; box-sizing: border-box; }

// 关系选择
.relation-group { display: flex; flex-wrap: wrap; gap: 16rpx; margin-bottom: 12rpx; }
.relation-item {
  height: 64rpx; line-height: 64rpx; padding: 0 24rpx;
  background: #f5f7fa; border-radius: $elder-radius; font-size: $elder-font-small;
  color: $color-text-light; border: 2rpx solid transparent;
  &.active { background: rgba(64, 158, 255, 0.1); border-color: $color-primary; color: $color-primary; font-weight: bold; }
}

// 验证码
.sms-row { display: flex; gap: 16rpx; align-items: center; }
.sms-input { flex: 1; }
.sms-btn {
  width: 200rpx; height: 88rpx; line-height: 88rpx; text-align: center;
  background: $color-primary; color: #fff; border-radius: $elder-radius;
  font-size: 26rpx; border: none; flex-shrink: 0;
  &[disabled] { background: #ccc; }
}

// 按钮
.primary-btn {
  width: 100%; height: $elder-btn-height; line-height: $elder-btn-height;
  background: linear-gradient(135deg, $color-primary 0%, #36cfc9 100%); color: #fff;
  border-radius: $elder-radius-lg; font-size: $elder-font-medium; font-weight: bold; border: none;
  &[disabled] { opacity: 0.7; }
}

// 成功
.success-card { text-align: center; padding: 64rpx $elder-spacing-lg; }
.success-icon { font-size: 80rpx; display: block; margin-bottom: 20rpx; }
.success-title { display: block; font-size: $elder-font-xl; font-weight: bold; color: $color-text; margin-bottom: 12rpx; }
.success-desc { display: block; font-size: $elder-font-base; color: $color-text-placeholder; margin-bottom: 40rpx; }
</style>
