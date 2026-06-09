import { defineStore } from 'pinia'
import { getProfile } from '@/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    profile: null,
    role: localStorage.getItem('role') || ''
  }),
  getters: {
    displayName: (state) => state.profile?.realName || state.profile?.username || localStorage.getItem('realName') || '用户',
    isAdmin: (state) => state.role === 'admin' || state.role === 'super',
    isSuper: (state) => state.role === 'super'
  },
  actions: {
    setAuth({ token, role, realName }) {
      localStorage.setItem('token', token)
      localStorage.setItem('role', role)
      localStorage.setItem('realName', realName || '')
      this.role = role
    },
    async fetchProfile() {
      const res = await getProfile()
      this.profile = res.data
      return this.profile
    },
    clear() {
      this.profile = null
      this.role = ''
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('realName')
    }
  }
})
