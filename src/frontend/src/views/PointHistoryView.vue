<template>
  <div class="min-h-screen bg-gray-100">
    <!-- ヘッダー -->
    <header class="bg-white shadow">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
        <h1 class="text-2xl font-bold text-gray-900">ポイント履歴</h1>
        <div class="flex space-x-4">
          <router-link
            to="/points"
            class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 transition duration-200"
          >
            残高へ戻る
          </router-link>
          <router-link
            to="/account"
            class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600 transition duration-200"
          >
            アカウント
          </router-link>
          <button
            @click="handleLogout"
            class="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600 transition duration-200"
          >
            ログアウト
          </button>
        </div>
      </div>
    </header>

    <!-- メインコンテンツ -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div v-if="loading" class="flex justify-center items-center h-64">
        <div class="animate-spin rounded-full h-16 w-16 border-b-2 border-blue-500"></div>
      </div>

      <div v-else-if="errorMessage" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
        {{ errorMessage }}
      </div>

      <div v-else class="space-y-6">
        <!-- ポイント履歴リスト -->
        <div class="bg-white shadow rounded-lg overflow-hidden">
          <div v-if="historyData?.history && historyData.history.length > 0">
            <ul class="divide-y divide-gray-200">
              <li
                v-for="item in historyData.history"
                :key="item.id"
                class="p-4 hover:bg-gray-50 transition duration-150"
              >
                <div class="flex justify-between items-start">
                  <div class="flex-1">
                    <div class="flex items-center space-x-2">
                      <span
                        class="px-2 py-1 text-xs font-semibold rounded"
                        :class="{
                          'bg-green-100 text-green-800': item.type === 'EARN',
                          'bg-red-100 text-red-800': item.type === 'USE' || item.type === 'SPEND'
                        }"
                      >
                        {{ item.type === 'EARN' ? '獲得' : '使用' }}
                      </span>
                    </div>
                    <p class="mt-2 text-sm text-gray-900 font-medium">{{ item.description }}</p>
                    <p class="mt-1 text-xs text-gray-500">{{ formatDate(item.createdAt) }}</p>
                  </div>
                  <div class="text-right ml-4">
                    <div
                      class="text-lg font-bold"
                      :class="{
                        'text-green-600': item.type === 'EARN',
                        'text-red-600': item.type === 'USE' || item.type === 'SPEND'
                      }"
                    >
                      {{ item.type === 'EARN' ? '+' : '-' }}{{ (item.amount || 0).toLocaleString() }}
                    </div>
                    <div class="text-xs text-gray-500 mt-1">
                      残高: {{ (item.balanceAfter || 0).toLocaleString() }}
                    </div>
                  </div>
                </div>
              </li>
            </ul>
          </div>
          <div v-else class="p-8 text-center text-gray-500">
            ポイント履歴がありません
          </div>
        </div>

        <!-- ページネーション -->
        <div v-if="historyData?.pagination" class="bg-white shadow rounded-lg p-4">
          <div class="flex justify-between items-center">
            <div class="text-sm text-gray-600">
              全 {{ historyData.pagination.totalItems }} 件中
              {{ ((historyData.pagination.currentPage - 1) * historyData.pagination.limit + 1) }}
              -
              {{ Math.min(historyData.pagination.currentPage * historyData.pagination.limit, historyData.pagination.totalItems) }}
              件を表示
            </div>
            <div class="flex space-x-2">
              <button
                @click="goToPage(currentPage - 1)"
                :disabled="currentPage <= 1"
                class="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition duration-200"
              >
                前へ
              </button>
              <span class="px-4 py-2 text-gray-700">
                {{ currentPage }} / {{ historyData.pagination.totalPages }}
              </span>
              <button
                @click="goToPage(currentPage + 1)"
                :disabled="currentPage >= historyData.pagination.totalPages"
                class="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition duration-200"
              >
                次へ
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authService } from '../services/api'
import { pointApi } from '../services/pointApi'

export default {
  name: 'PointHistoryView',
  setup() {
    const router = useRouter()
    const historyData = ref(null)
    const loading = ref(true)
    const errorMessage = ref('')
    const currentPage = ref(1)
    const limit = ref(10)

    const loadHistoryData = async (page = 1) => {
      loading.value = true
      errorMessage.value = ''

      try {
        const data = await pointApi.getPointHistory(page, limit.value)
        historyData.value = data
        currentPage.value = page
      } catch (error) {
        if (error.response?.status === 503) {
          errorMessage.value = 'ポイントサービスが一時的に利用できません。しばらくしてからお試しください。'
        } else {
          errorMessage.value = error.response?.data?.error || 'ポイント履歴の取得に失敗しました'
        }
      } finally {
        loading.value = false
      }
    }

    const goToPage = (page) => {
      if (page < 1 || (historyData.value && page > historyData.value.pagination.totalPages)) {
        return
      }
      loadHistoryData(page)
    }

    const handleLogout = async () => {
      try {
        await authService.logout()
      } catch (error) {
        console.error('Logout error:', error)
      } finally {
        router.push('/login')
      }
    }

    const formatDate = (dateString) => {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleString('ja-JP', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    onMounted(() => {
      loadHistoryData(1)
    })

    return {
      historyData,
      loading,
      errorMessage,
      currentPage,
      goToPage,
      handleLogout,
      formatDate
    }
  }
}
</script>
