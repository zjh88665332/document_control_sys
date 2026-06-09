import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getNotificationBadges, markNotificationRead } from '@/api'

export const useNotificationStore = defineStore('notification', () => {
  const badges = ref({
    friend: false,
    share: false,
    feedback: false,
    file: false,
    notice: false
  })

  async function fetchBadges() {
    try {
      const res = await getNotificationBadges()
      badges.value = res.data
    } catch {
      // 忽略通知接口异常，避免影响主流程
    }
  }

  async function markRead(type) {
    try {
      await markNotificationRead(type)
      await fetchBadges()
    } catch {
      // 忽略标记已读失败
    }
  }

  return { badges, fetchBadges, markRead }
})
