import {
  getNotifications,
  getUnreadCount,
  markAllAsRead,
  markAsRead,
} from '@/api/notification'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)
  const notifications = ref([])
  const loading = ref(false)
  const finished = ref(false)
  const page = ref(0)
  const size = ref(20)

  const fetchUnreadCount = async () => {
    try {
      const res = await getUnreadCount()
      unreadCount.value = res
    } catch (error) {
      console.error('Failed to fetch notification unread count', error)
    }
  }

  const fetchNotifications = async (isRefresh = false) => {
    if (isRefresh) {
      page.value = 0
      finished.value = false
      notifications.value = []
    }

    if (loading.value || finished.value) return

    loading.value = true
    try {
      const res = await getNotifications({ page: page.value, size: size.value })
      const newItems = res.content
      notifications.value.push(...newItems)

      page.value++
      if (newItems.length < size.value || res.last) {
        finished.value = true
      }
    } catch (error) {
      console.error('Failed to fetch notifications', error)
      finished.value = true
    } finally {
      loading.value = false
    }
  }

  const markRead = async (id) => {
    try {
      await markAsRead(id)
      const item = notifications.value.find((n) => n.id === id)
      if (item && !item.read) {
        item.read = true
        if (unreadCount.value > 0) unreadCount.value--
      }
    } catch (error) {
      console.error('Failed to mark notification as read', error)
    }
  }

  const markAllRead = async () => {
    try {
      await markAllAsRead()
      notifications.value.forEach((n) => (n.read = true))
      unreadCount.value = 0
    } catch (error) {
      console.error('Failed to mark all notifications as read', error)
    }
  }

  return {
    unreadCount,
    notifications,
    loading,
    finished,
    fetchUnreadCount,
    fetchNotifications,
    markRead,
    markAllRead,
  }
})
