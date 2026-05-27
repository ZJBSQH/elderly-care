<template>
  <view class="register-container">
    <!-- Logo 区域 -->
    <view class="logo-section">
      <image class="logo" src="/static/logo.png" mode="aspectFit" />
      <text class="title">创建账号</text>
      <text class="desc">注册后即可使用用药管理服务</text>
    </view>

    <!-- 注册表单 -->
    <view class="form-section">
      <!-- 手机号 -->
      <view class="input-item">
        <text class="input-label">手机号</text>
        <input
          class="input-field"
          type="number"
          v-model="form.phone"
          placeholder="请输入11位手机号"
          maxlength="11"
        />
      </view>

      <!-- 验证码 -->
      <view class="input-item">
        <text class="input-label">验证码</text>
        <view class="code-row">
          <input
            class="input-field code-input"
            type="number"
            v-model="form.smsCode"
            placeholder="6位验证码"
            maxlength="6"
          />
          <button
            class="code-btn"
            :disabled="countdown > 0"
            @click="sendCode"
          >
            {{ countdown > 0 ? countdown + 's后重试' : '获取验证码' }}
          </button>
        </view>
      </view>

      <!-- 密码 -->
      <view class="input-item">
        <text class="input-label">密码</text>
        <input
          class="input-field"
          type="password"
          v-model="form.password"
          placeholder="6-20位密码"
          maxlength="20"
        />
      </view>

      <!-- 姓名 -->
      <view class="input-item">
        <text class="input-label">姓名</text>
        <input
          class="input-field"
          v-model="form.name"
          placeholder="请输入真实姓名"
          maxlength="20"
        />
      </view>

      <!-- 年龄 -->
      <view class="input-item">
        <text class="input-label">年龄</text>
        <input
          class="input-field"
          type="number"
          v-model="form.age"
          placeholder="请输入年龄"
          maxlength="3"
        />
      </view>

      <!-- 性别 -->
      <view class="input-item">
        <text class="input-label">性别</text>
        <view class="radio-group">
          <view
            class="radio-item"
            :class="{ active: form.sex === 1 }"
            @click="form.sex = 1"
          >
            <text class="radio-text">男</text>
          </view>
          <view
            class="radio-item"
            :class="{ active: form.sex === 0 }"
            @click="form.sex = 0"
          >
            <text class="radio-text">女</text>
          </view>
        </view>
      </view>

      <!-- 用户类型 -->
      <view class="input-item">
        <text class="input-label">身份</text>
        <view class="radio-group">
          <view
            class="radio-item"
            :class="{ active: form.userType === 0 }"
            @click="form.userType = 0"
          >
            <text class="radio-label">老人</text>
            <text class="radio-hint">60岁以上自动分配</text>
          </view>
          <view
            class="radio-item"
            :class="{ active: form.userType === 1 }"
            @click="form.userType = 1"
          >
            <text class="radio-label">家属</text>
            <text class="radio-hint">绑定老人账号</text>
          </view>
        </view>
      </view>

      <button
        class="submit-btn"
        :loading="loading"
        :disabled="loading"
        @click="handleRegister"
      >
        注册
      </button>

      <view class="back-link">
        <text class="link" @click="goBack">已有账号？去登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { register, sendSmsCode } from '../../api/auth.js'

const PHONE_REGEX = /^1[3-9]\d{9}$/

const form = reactive({
  phone: '',
  smsCode: '',
  password: '',
  name: '',
  age: '',
  sex: 1,          // 默认男
  userType: 0       // 默认老人
})

const loading = ref(false)
const countdown = ref(0)
let timer = null

/** 发送验证码 */
async function sendCode() {
  if (!PHONE_REGEX.test(form.phone)) {
    return uni.showToast({ title: '请输入正确的手机号', icon: 'none', duration: 2000 })
  }

  try {
    uni.showLoading({ title: '发送中...', mask: true })
    await sendSmsCode({ phone: form.phone, type: 'register' })
    uni.hideLoading()
    uni.showToast({ title: '验证码已发送', icon: 'success', duration: 2000 })

    // 60 秒倒计时
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)
  } catch (e) {
    uni.hideLoading()
    uni.showToast({ title: e.message || '发送失败', icon: 'none', duration: 2000 })
  }
}

/** 提交注册 */
async function handleRegister() {
  // 逐字段校验
  if (!PHONE_REGEX.test(form.phone)) {
    return uni.showToast({ title: '请输入正确的手机号', icon: 'none', duration: 2000 })
  }
  if (!/^\d{6}$/.test(form.smsCode)) {
    return uni.showToast({ title: '请输入6位验证码', icon: 'none', duration: 2000 })
  }
  if (!form.password || form.password.length < 6) {
    return uni.showToast({ title: '密码至少6位', icon: 'none', duration: 2000 })
  }
  if (!form.name || !form.name.trim()) {
    return uni.showToast({ title: '请输入姓名', icon: 'none', duration: 2000 })
  }
  if (!form.age || form.age < 1 || form.age > 150) {
    return uni.showToast({ title: '请输入有效年龄', icon: 'none', duration: 2000 })
  }

  loading.value = true
  try {
    await register({
      phone: form.phone,
      smsCode: form.smsCode,
      password: form.password,
      name: form.name.trim(),
      age: Number(form.age),
      sex: form.sex,
      userType: form.userType
    })

    uni.showToast({ title: '注册成功，请登录', icon: 'success', duration: 2000 })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e) {
    uni.showToast({ title: e.message || '注册失败', icon: 'none', duration: 2000 })
  } finally {
    loading.value = false
  }
}

/** 返回登录页 */
function goBack() {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
$primary-blue: #409eff;
$primary-green: #36cfc9;

.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, $primary-blue 0%, $primary-green 100%);
  padding: 40rpx 40rpx 60rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.logo-section {
  text-align: center;
  margin-bottom: 40rpx;
}

.logo {
  width: 140rpx;
  height: 140rpx;
  border-radius: 20rpx;
}

.title {
  display: block;
  margin-top: 16rpx;
  font-size: 48rpx;
  font-weight: bold;
  color: #ffffff;
}

.desc {
  display: block;
  margin-top: 8rpx;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.85);
}

.form-section {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx 36rpx;
}

.input-item {
  margin-bottom: 32rpx;
}

.input-label {
  display: block;
  margin-bottom: 12rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #333333;
}

.input-field {
  height: 88rpx;
  background: #f5f7fa;
  border-radius: 14rpx;
  padding: 0 20rpx;
  font-size: 32rpx;
  color: #333333;
}

.code-row {
  display: flex;
  gap: 16rpx;
}

.code-input {
  flex: 1;
}

.code-btn {
  width: 220rpx;
  height: 88rpx;
  line-height: 88rpx;
  background: $primary-blue;
  color: #ffffff;
  border-radius: 14rpx;
  font-size: 28rpx;
  border: none;
  white-space: nowrap;
  padding: 0;

  &[disabled] {
    background: #cccccc;
    color: #999999;
  }
}

/* 性别 & 身份单选组 */
.radio-group {
  display: flex;
  gap: 20rpx;
}

.radio-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  background: #f5f7fa;
  border-radius: 14rpx;
  border: 2rpx solid transparent;
  transition: all 0.2s;

  &.active {
    background: rgba(64, 158, 255, 0.1);
    border-color: $primary-blue;
  }
}

.radio-text,
.radio-label {
  font-size: 30rpx;
  font-weight: 600;
  color: #333333;
}

.radio-hint {
  font-size: 22rpx;
  color: #999999;
  margin-top: 4rpx;
}

.submit-btn {
  height: 96rpx;
  line-height: 96rpx;
  background: linear-gradient(135deg, $primary-blue 0%, $primary-green 100%);
  color: #ffffff;
  border-radius: 16rpx;
  font-size: 36rpx;
  font-weight: bold;
  border: none;
  margin-top: 20rpx;

  &[disabled] {
    opacity: 0.7;
  }
}

.back-link {
  text-align: center;
  margin-top: 24rpx;
}

.link {
  font-size: 28rpx;
  color: $primary-blue;
}
</style>
