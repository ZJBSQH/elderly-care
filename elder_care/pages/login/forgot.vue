<template>
  <view class="forgot-container">
    <view class="logo-section">
      <image class="logo" src="/static/logo.png" mode="aspectFit"></image>
      <text class="title">重置密码</text>
      <text class="desc">通过手机号验证重置您的密码</text>
    </view>

    <view class="form-section">
      <view class="input-item">
        <text class="input-label">📱 手机号</text>
        <input 
          class="input-field" 
          type="number" 
          v-model="form.phone" 
          placeholder="请输入11位手机号" 
          maxlength="11" 
        />
      </view>

      <view class="input-item">
        <text class="input-label">🔐 验证码</text>
        <view class="code-input-wrapper">
          <input 
            class="input-field code-input" 
            type="number" 
            v-model="form.smsCode" 
            placeholder="请输入6位验证码" 
            maxlength="6" 
          />
          <button 
            class="code-btn" 
            @click="sendCode" 
            :disabled="countdown > 0"
          >
            {{ countdown > 0 ? `${countdown}s后重试` : '获取验证码' }}
          </button>
        </view>
      </view>

      <view class="input-item">
        <text class="input-label">🔒 新密码</text>
        <input 
          class="input-field" 
          type="password" 
          v-model="form.newPassword" 
          placeholder="请设置新密码（6-20位）" 
          maxlength="20" 
        />
      </view>

      <button class="submit-btn" @click="handleReset" :loading="loading">确认重置</button>

      <view class="back-link">
        <text class="link" @click="goBack">← 返回登录</text>
      </view>
    </view>
  </view>
</template>

<script>
import { sendSmsCode, resetPassword } from '../../api/auth.js'

export default {
  data() {
    return { 
      form: { 
        phone: '', 
        smsCode: '',
        newPassword: '' 
      },
      loading: false,
      countdown: 0,
      timer: null
    }
  },
  onUnload() {
    // 清除定时器
    if (this.timer) {
      clearInterval(this.timer)
    }
  },
  methods: {
    // 发送验证码
    async sendCode() {
      // 验证手机号
      if (!this.form.phone) {
        return uni.showToast({ title: '请输入手机号', icon: 'none', duration: 2000 })
      }
      if (!/^1[3-9]\d{9}$/.test(this.form.phone)) {
        return uni.showToast({ title: '手机号格式不正确', icon: 'none', duration: 2000 })
      }

      try {
        uni.showLoading({ title: '发送中...', mask: true })
        await sendSmsCode({ 
          phone: this.form.phone,
          type: 'reset' // 重置密码类型
        })
        uni.hideLoading()
        uni.showToast({ title: '验证码已发送', icon: 'success', duration: 2000 })
        
        // 开始倒计时
        this.countdown = 60
        this.timer = setInterval(() => {
          this.countdown--
          if (this.countdown <= 0) {
            clearInterval(this.timer)
          }
        }, 1000)
      } catch (e) {
        uni.hideLoading()
        console.error('发送验证码失败：', e)
        uni.showToast({ title: e.message || '发送失败，请稍后重试', icon: 'none', duration: 2000 })
      }
    },

    // 重置密码
    async handleReset() {
      // 表单验证
      if (!this.form.phone) {
        return uni.showToast({ title: '请输入手机号', icon: 'none', duration: 2000 })
      }
      if (!/^1[3-9]\d{9}$/.test(this.form.phone)) {
        return uni.showToast({ title: '手机号格式不正确', icon: 'none', duration: 2000 })
      }
      if (!this.form.smsCode) {
        return uni.showToast({ title: '请输入验证码', icon: 'none', duration: 2000 })
      }
      if (!/^\d{6}$/.test(this.form.smsCode)) {
        return uni.showToast({ title: '验证码格式不正确', icon: 'none', duration: 2000 })
      }
      if (!this.form.newPassword) {
        return uni.showToast({ title: '请设置新密码', icon: 'none', duration: 2000 })
      }
      if (this.form.newPassword.length < 6 || this.form.newPassword.length > 20) {
        return uni.showToast({ title: '密码长度为6-20位', icon: 'none', duration: 2000 })
      }

      this.loading = true
      try {
        await resetPassword(this.form)
        uni.showToast({ title: '密码重置成功', icon: 'success', duration: 2000 })
        
        // 延迟返回登录页
        setTimeout(() => {
          uni.navigateBack()
        }, 2000)
      } catch (e) {
        console.error('重置密码失败：', e)
        uni.showToast({ title: e.message || '重置失败，请检查验证码', icon: 'none', duration: 2000 })
      } finally {
        this.loading = false
      }
    },

    // 返回登录页
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
$primary-blue: #409EFF;
$primary-green: #36CFC9;

.forgot-container {
  min-height: 100vh;
  background: linear-gradient(135deg, $primary-blue 0%, $primary-green 100%);
  padding: 60rpx 40rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.logo-section { 
  text-align: center; 
  margin-bottom: 60rpx; 
}

.logo { 
  width: 180rpx; 
  height: 180rpx; 
  border-radius: 20rpx; 
}

.title { 
  font-size: 52rpx; 
  font-weight: bold; 
  color: #fff;
  display: block;
  margin-top: 20rpx;
}

.desc { 
  font-size: 32rpx; 
  color: rgba(255,255,255,0.9);
  display: block;
  margin-top: 10rpx;
}

.form-section { 
  background: #fff; 
  border-radius: 24rpx; 
  padding: 50rpx 40rpx; 
}

.input-item { 
  margin-bottom: 40rpx; 
}

.input-label { 
  font-size: 34rpx; 
  font-weight: 600; 
  display: block;
  margin-bottom: 16rpx;
}

.input-field { 
  height: 96rpx; 
  background: #f5f7fa; 
  border-radius: 16rpx; 
  padding: 0 24rpx; 
  font-size: 34rpx; 
}

.code-input-wrapper {
  display: flex;
  gap: 20rpx;
}

.code-input {
  flex: 1;
}

.code-btn {
  width: 240rpx;
  height: 96rpx;
  line-height: 96rpx;
  background: $primary-blue;
  color: #fff;
  border-radius: 16rpx;
  font-size: 30rpx;
  border: none;
  white-space: nowrap;
  
  &[disabled] {
    background: #ccc;
    color: #999;
  }
}

.submit-btn { 
  height: 96rpx; 
  background: linear-gradient(135deg, $primary-blue 0%, $primary-green 100%); 
  color: #fff; 
  border-radius: 16rpx; 
  font-size: 36rpx; 
  font-weight: bold; 
  border: none;
  margin-top: 20rpx;
}

.back-link {
  text-align: center;
  margin-top: 30rpx;
}

.link {
  color: $primary-blue;
  font-size: 30rpx;
}
</style>