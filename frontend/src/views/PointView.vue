<template>
  <div class="min-h-screen bg-gray-100">
    <!-- ヘッダー -->
    <header class="bg-white shadow">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
        <h1 class="text-2xl font-bold text-gray-900">ポイント残高</h1>
        <div class="flex space-x-4">
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
        <!-- ポイント残高カード -->
        <div class="bg-white shadow rounded-lg p-6">
          <h2 class="text-xl font-semibold text-gray-800 mb-4 flex items-center">
            <svg class="w-6 h-6 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            現在のポイント残高
          </h2>
          
          <div v-if="pointData" class="text-center">
            <div class="text-5xl font-bold text-blue-600 mb-2">
              {{ pointData.balance?.toLocaleString() || 0 }}
            </div>
            <div class="text-gray-600">ポイント</div>
          </div>
        </div>

        <!-- アクションボタン -->
        <div class="bg-white shadow rounded-lg p-6">
          <router-link
            to="/points/history"
            class="block w-full bg-blue-500 text-white text-center px-6 py-3 rounded-lg hover:bg-blue-600 transition duration-200"
          >
            ポイント履歴を見る
          </router-link>
        </div>

        <!-- 情報カード -->
        <div class="bg-blue-50 border-l-4 border-blue-500 p-4">
          <div class="flex">
            <div class="flex-shrink-0">
              <svg class="h-5 w-5 text-blue-500" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
              </svg>
            </div>
            <div class="ml-3">
              <p class="text-sm text-blue-700">
                ポイントは様々なサービスでご利用いただけます。
              </p>
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
  name: 'PointView',
  setup() {
    const router = useRouter()
    const pointData = ref(null)
    const loading = ref(true)
    const errorMessage = ref('')

    const loadPointData = async () => {
      loading.value = true
      errorMessage.value = ''

      try {
        const data = await pointApi.getPoints()
        pointData.value = data
      } catch (error) {
        if (error.response?.status === 503) {
          errorMessage.value = 'ポイントサービスが一時的に利用できません。しばらくしてからお試しください。'
        } else {
          errorMessage.value = error.response?.data?.error || 'ポイント情報の取得に失敗しました'
        }
      } finally {
        loading.value = false
      }
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

    onMounted(() => {
      loadPointData()
    })

    return {
      pointData,
      loading,
      errorMessage,
      handleLogout
    }
  }
}
</script>
