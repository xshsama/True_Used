import {
  sendMessage as apiSendMessage,
  getConversations,
  getMessages,
  getOnlineUsers,
} from '@/api/chat'
import { Client } from '@stomp/stompjs'
import { defineStore } from 'pinia'
import SockJS from 'sockjs-client'
import { ref } from 'vue'
import { useUserStore } from './user'

import { showNotify } from 'vant'

export const useMessageStore = defineStore('message', () => {
  const unreadCount = ref(0)
  const conversations = ref([])
  const currentConversationId = ref(null)
  const messages = ref([]) // Current conversation messages
  const stompClient = ref(null)
  const isConnected = ref(false)
  const onlineUsers = ref(new Set())
  const userStore = useUserStore()

  const setUnreadCount = (count) => {
    unreadCount.value = count
  }

  const connect = () => {
    if (isConnected.value || !userStore.token) return

    const socket = new SockJS('/api/ws')
    stompClient.value = new Client({
      webSocketFactory: () => socket,
      connectHeaders: {
        Authorization: `Bearer ${userStore.token}`,
      },
      debug: (str) => {
        console.log(str)
      },
      onConnect: () => {
        isConnected.value = true
        console.log('Connected to WebSocket')

        // Fetch initial online users
        fetchOnlineUsers()

        // Subscribe to presence updates
        stompClient.value.subscribe('/topic/presence', (message) => {
          const presence = JSON.parse(message.body)
          handlePresenceUpdate(presence)
        })

        // Subscribe to user's private topic (using ID-based topic for reliability)
        if (userStore.user && userStore.user.id) {
          console.log('Subscribing to /topic/user/' + userStore.user.id)
          stompClient.value.subscribe(
            `/topic/user/${userStore.user.id}`,
            (message) => {
              const msg = JSON.parse(message.body)
              handleIncomingMessage(msg)
            },
          )

          // Subscribe to system notifications
          console.log('Subscribing to /user/queue/notifications')
          stompClient.value.subscribe(
            '/user/queue/notifications',
            (message) => {
              const notification = JSON.parse(message.body)
              handleNotification(notification)
            },
          )
        } else {
          console.error('User ID not found, cannot subscribe to private topic')
        }
      },
      onStompError: (frame) => {
        console.error('Broker reported error: ' + frame.headers['message'])
        console.error('Additional details: ' + frame.body)
      },
      onWebSocketClose: () => {
        isConnected.value = false
        console.log('WebSocket closed')
      },
    })

    stompClient.value.activate()
  }

  const disconnect = () => {
    if (stompClient.value) {
      stompClient.value.deactivate()
      stompClient.value = null
      isConnected.value = false
    }
  }

  const handlePresenceUpdate = (presence) => {
    if (presence.status === 'ONLINE') {
      onlineUsers.value.add(presence.userId)
    } else {
      onlineUsers.value.delete(presence.userId)
    }
  }

  const fetchOnlineUsers = async () => {
    try {
      const users = await getOnlineUsers()
      if (Array.isArray(users)) {
        onlineUsers.value = new Set(users)
      }
    } catch (error) {
      console.error('Failed to fetch online users', error)
    }
  }

  const handleNotification = (notification) => {
    console.log('Received notification:', notification)
    // Show popup
    showNotify({
      type: 'success',
      message: notification.content,
      duration: 3000,
      onClick: () => {
        // Navigate if needed, e.g. to product detail
        if (notification.type === 'PRICE_DROP' && notification.relatedId) {
          // router.push(`/product/${notification.relatedId}`)
          // Note: router is not directly available here, might need to pass it or use window.location
          window.location.href = `/product/${notification.relatedId}`
        }
      },
    })
    // Refresh unread count if needed (though notifications are separate from chat messages usually)
  }

  const handleIncomingMessage = (msg) => {
    console.log('Incoming message:', msg)
    // Reload conversations to update last message and unread count
    fetchConversations()

    // If we are in the conversation view of this message
    if (
      currentConversationId.value &&
      msg.conversationId === currentConversationId.value
    ) {
      // Check if it's already there (dedup)
      if (!messages.value.some((m) => String(m.id) === String(msg.id))) {
        // Format it for frontend
        const formattedMsg = {
          id: msg.id,
          type: 'text', // Backend currently only supports text
          content: msg.content,
          isSelf: msg.senderId === userStore.user?.id, // Calculate based on ID
          timestamp: msg.timestamp
            ? new Date(msg.timestamp).getTime()
            : Date.now(),
        }
        messages.value.push(formattedMsg)
      }
    }
  }

  const sendMessage = async (receiverId, content) => {
    try {
      const res = await apiSendMessage({ receiverId, content })
      // Add to local messages
      const formattedMsg = {
        id: res.id,
        type: 'text',
        content: res.content,
        isSelf: true,
        timestamp: res.timestamp
          ? new Date(res.timestamp).getTime()
          : Date.now(),
      }
      messages.value.push(formattedMsg)

      // Refresh conversations to update last message
      fetchConversations()
    } catch (error) {
      console.error('Failed to send message', error)
    }
  }

  const fetchConversations = async () => {
    try {
      const res = await getConversations()
      conversations.value = res
      // Update unread count
      const totalUnread = res.reduce((sum, c) => sum + c.unreadCount, 0)
      unreadCount.value = totalUnread
    } catch (error) {
      console.error('Failed to fetch conversations', error)
    }
  }

  const fetchMessages = async (conversationId) => {
    currentConversationId.value = conversationId
    try {
      const res = await getMessages(conversationId)
      console.log('Fetched messages for conversation', conversationId, res)

      if (!Array.isArray(res)) {
        console.error('Expected array of messages but got:', res)
        messages.value = []
        return
      }

      // Format messages
      messages.value = res
        .map((msg) => ({
          id: msg.id,
          type: 'text',
          content: msg.content,
          isSelf:
            msg.self !== undefined
              ? msg.self
              : msg.senderId === userStore.user?.id,
          timestamp: msg.timestamp
            ? new Date(msg.timestamp).getTime()
            : Date.now(),
        }))
        .reverse() // Backend returns DESC, frontend wants ASC usually?
      // Wait, backend returns DESC (latest first). Frontend usually displays oldest at top, newest at bottom.
      // So we need to reverse it to be ASC.
    } catch (error) {
      console.error('Failed to fetch messages', error)
    }
  }

  const markConversationAsRead = (conversationId) => {
    const conversation = conversations.value.find(
      (c) => c.id === conversationId,
    )
    if (conversation && conversation.unreadCount > 0) {
      unreadCount.value -= conversation.unreadCount
      conversation.unreadCount = 0
    }
  }

  const fetchUnreadCount = async () => {
    await fetchConversations()
  }

  const clearCurrentConversation = () => {
    currentConversationId.value = null
    messages.value = []
  }

  return {
    unreadCount,
    conversations,
    messages,
    currentConversationId,
    setUnreadCount,
    connect,
    disconnect,
    sendMessage,
    fetchConversations,
    fetchMessages,
    markConversationAsRead,
    fetchUnreadCount,
    clearCurrentConversation,
    onlineUsers,
  }
})
