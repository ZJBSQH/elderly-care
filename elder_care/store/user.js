import { defineStore } from 'pinia'

/**
 * 用户状态管理
 * 管理登录态、Token、用户信息
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    token: uni.getStorageSync('token') || '',
    userInfo: uni.getStorageSync('userInfo') || null,
    isLoggedIn: !!uni.getStorageSync('token')
  }),

  getters: {
    /** 当前用户 ID */
    userId: (state) => state.userInfo?.id || null,
    /** 老人档案 ID */
    elderId: (state) => state.userInfo?.elderId || null,
    /** 用户类型：0-老人 1-家属 2-管理员 */
    userType: (state) => state.userInfo?.userType ?? null,
    /** 是否为老人 */
    isElder: (state) => state.userInfo?.userType === 0,
    /** 是否为家属 */
    isFamily: (state) => state.userInfo?.userType === 1
  },

  actions: {
    /** 登录成功：保存 Token 和用户信息 */
    loginSuccess(token, user, elderId) {
      this.token = token
      this.userInfo = { ...user, elderId: elderId || null }
      this.isLoggedIn = true
      uni.setStorageSync('token', token)
      uni.setStorageSync('userInfo', this.userInfo)
    },

    /** 更新用户资料 */
    setUserInfo(userInfo) {
      this.userInfo = { ...this.userInfo, ...userInfo }
      uni.setStorageSync('userInfo', this.userInfo)
    },

    /** 退出登录：清除全部状态 */
    logout() {
      this.token = ''
      this.userInfo = null
      this.isLoggedIn = false
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      uni.reLaunch({ url: '/pages/login/login' })
    }
  }
})
