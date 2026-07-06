import { addFavorite, listMyFavorites, removeFavorite } from '@/api/favorites'
import { defineStore } from 'pinia'

export const useFavoritesStore = defineStore('favorites', {
  state: () => ({
    favoriteIds: new Set(),
    initialized: false,
  }),

  actions: {
    async fetchFavorites() {
      if (this.initialized) {
        return
      }
      try {
        const response = await listMyFavorites({ page: 0, size: 1000 }) // 获取足够多的收藏
        const ids = response.content.map((fav) => fav.productId)
        this.favoriteIds = new Set(ids)
        this.initialized = true
      } catch (error) {
        console.error('Failed to fetch favorites:', error)
      }
    },

    isFavorited(productId) {
      return this.favoriteIds.has(productId)
    },

    async add(productId) {
      if (this.favoriteIds.has(productId)) return
      await addFavorite(productId)
      this.favoriteIds.add(productId)
    },

    async remove(productId) {
      if (!this.favoriteIds.has(productId)) return
      await removeFavorite(productId)
      this.favoriteIds.delete(productId)
    },
  },
})
