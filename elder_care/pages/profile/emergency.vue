<template>
  <view class="page-container">
    <!-- 说明 -->
    <view class="info-card">
      <text class="info-icon">🆘</text>
      <text class="info-title">紧急联系人</text>
      <text class="info-desc">设置紧急联系人后，首页可以一键拨打联系人的电话</text>
    </view>

    <!-- 当前联系人 -->
    <view class="contact-card" v-if="emergencyContact">
      <text class="contact-label">当前紧急联系人</text>
      <text class="contact-phone">{{ emergencyContact }}</text>
      <view class="contact-actions">
        <button class="call-btn" @click="makeCall">📞 拨打</button>
        <button class="clear-btn" @click="clearContact">清除</button>
      </view>
    </view>

    <!-- 设置/修改联系人 -->
    <view class="form-card">
      <text class="form-title">{{ emergencyContact ? '修改紧急联系人' : '设置紧急联系人' }}</text>
      <view class="form-item">
        <text class="form-label">联系人手机号</text>
        <input
          class="form-input"
          type="number"
          v-model="contactPhone"
          placeholder="请输入11位手机号"
          maxlength="11"
        />
        <text class="form-hint">建议填写子女或其他家属的手机号</text>
      </view>

      <button class="submit-btn" :loading="saving" @click="saveContact">保存联系人</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../store/user.js'
import { getUserInfo } from '../../api/auth.js'

const userStore = useUserStore()

const emergencyContact = ref('')
const contactPhone = ref('')
const saving = ref(false)

/** 加载紧急联系人 */
async function loadContact() {
  try {
    const data = await getUserInfo()
    const contact = data?.emergencyContact
      || userStore.userInfo?.emergencyContact
      || uni.getStorageSync('emergencyContact')
      || ''

    emergencyContact.value = contact
    contactPhone.value = contact
  } catch (e) {
    // 降级使用本地存储
    const contact = uni.getStorageSync('emergencyContact') || ''
    emergencyContact.value = contact
    contactPhone.value = contact
  }
}

/** 保存联系人 */
async function saveContact() {
  const phone = contactPhone.value.trim()
  if (!phone) {
    return uni.showToast({ title: '请输入联系人手机号', icon: 'none', duration: 2000 })
  }
  if (!/^1[3-9]\d{9}$/.test(phone)) {
    return uni.showToast({ title: '请输入正确的手机号', icon: 'none', duration: 2000 })
  }

  saving.value = true
  try {
    // 保存到本地存储（后续可对接后端 Elder 更新接口）
    uni.setStorageSync('emergencyContact', phone)
    emergencyContact.value = phone
    userStore.setUserInfo({ emergencyContact: phone })
    uni.showToast({ title: '紧急联系人已保存', icon: 'success', duration: 1500 })
  } catch (e) {
    uni.showToast({ title: '保存失败', icon: 'none', duration: 2000 })
  } finally {
    saving.value = false
  }
}

/** 拨打 */
function makeCall() {
  if (!emergencyContact.value) {
    return uni.showToast({ title: '未设置紧急联系人', icon: 'none', duration: 2000 })
  }
  uni.showModal({
    title: '拨打电话',
    content: `即将拨打 ${emergencyContact.value}`,
    confirmText: '拨打',
    cancelText: '取消',
    success: (res) => {
      if (res.confirm) {
        uni.makePhoneCall({ phoneNumber: emergencyContact.value })
      }
    }
  })
}

/** 清除联系人 */
function clearContact() {
  uni.showModal({
    title: '清除联系人',
    content: '确定要清除紧急联系人吗？',
    confirmText: '确定',
    cancelText: '取消',
    success: (res) => {
      if (res.confirm) {
        emergencyContact.value = ''
        contactPhone.value = ''
        uni.removeStorageSync('emergencyContact')
        uni.showToast({ title: '已清除', icon: 'success', duration: 1500 })
      }
    }
  })
}

onMounted(loadContact)
</script>

<style lang="scss" scoped>
@import '../../styles/common.scss';

.page-container {
  min-height: 100vh;
  background: $color-bg;
  padding: $elder-spacing-xl;
}

// ========== 说明卡片 ==========
.info-card {
  background: linear-gradient(135deg, $color-warning 0%, $color-danger 100%);
  border-radius: $elder-radius-lg;
  padding: 36rpx;
  text-align: center;
  color: #fff;
  margin-bottom: $elder-spacing;
}

.info-icon { font-size: 64rpx; display: block; margin-bottom: 12rpx; }
.info-title { display: block; font-size: $elder-font-large; font-weight: bold; margin-bottom: 8rpx; }
.info-desc { display: block; font-size: $elder-font-small; opacity: 0.9; }

// ========== 当前联系人卡片 ==========
.contact-card {
  background: $color-white;
  border-radius: $elder-radius-lg;
  padding: $elder-spacing-lg;
  text-align: center;
  margin-bottom: $elder-spacing;
  box-shadow: $elder-shadow;
  border-left: 8rpx solid $color-success;
}

.contact-label {
  display: block;
  font-size: $elder-font-small;
  color: $color-text-placeholder;
  margin-bottom: 8rpx;
}

.contact-phone {
  display: block;
  font-size: 48rpx;
  font-weight: bold;
  color: $color-primary;
  margin-bottom: 20rpx;
}

.contact-actions {
  display: flex;
  gap: 20rpx;
}

.call-btn {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  background: $color-success;
  color: #fff;
  border-radius: $elder-radius;
  font-size: $elder-font-base;
  font-weight: bold;
  border: none;
}

.clear-btn {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  background: #f5f5f5;
  color: $color-text-placeholder;
  border-radius: $elder-radius;
  font-size: $elder-font-base;
  border: none;
}

// ========== 设置表单 ==========
.form-card {
  background: $color-white;
  border-radius: $elder-radius-lg;
  padding: $elder-spacing-lg;
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
  margin-bottom: 24rpx;
}

.form-label {
  display: block;
  font-size: $elder-font-base;
  font-weight: 600;
  color: $color-text;
  margin-bottom: 12rpx;
}

.form-input {
  height: 88rpx;
  background: #f5f7fa;
  border-radius: $elder-radius;
  padding: 0 24rpx;
  font-size: $elder-font-base;
  color: $color-text;
}

.form-hint {
  display: block;
  font-size: 24rpx;
  color: $color-text-placeholder;
  margin-top: 8rpx;
}

.submit-btn {
  width: 100%;
  height: $elder-btn-height;
  line-height: $elder-btn-height;
  background: linear-gradient(135deg, $color-danger 0%, $color-warning 100%);
  color: #fff;
  border-radius: $elder-radius-lg;
  font-size: $elder-font-medium;
  font-weight: bold;
  border: none;

  &[disabled] { opacity: 0.7; }
}
</style>
