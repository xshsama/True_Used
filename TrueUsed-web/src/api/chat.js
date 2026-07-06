import request from '@/utils/request'

export function getConversations() {
  return request({
    url: '/conversations',
    method: 'get',
  })
}

export function getMessages(conversationId, params) {
  return request({
    url: `/conversations/${conversationId}/messages`,
    method: 'get',
    params,
  })
}

export function createConversation(otherUserId) {
  // This might need a backend endpoint if not implicitly created by sending a message
  // For now, we can assume fetching messages for a conversation ID (if known) or starting a new one via some other means
  // But wait, the backend `ConversationService` has `getOrCreateConversation` but it's internal.
  // The `MessageController` creates it on message send.
  // We might need an endpoint to "start" a chat from a product page, which returns the conversation ID.
  // Let's assume we can just navigate to the chat page with a user ID and the frontend handles the rest?
  // Or maybe we need an endpoint to get conversation by user ID.
  // The backend `ConversationRepository` has `findByParticipants`.
  // Let's add a method to get conversation by target user ID in the backend if needed, or just use the list.
  // For now, let's stick to what we have.
  return request({
    url: '/conversations/start', // We might need to implement this in backend if we want to get ID before sending message
    method: 'post',
    params: { userId: otherUserId },
  })
}

export function sendMessage(data) {
  return request({
    url: '/messages',
    method: 'post',
    data,
  })
}

export function getOnlineUsers() {
  return request({
    url: '/conversations/online',
    method: 'get',
  })
}
