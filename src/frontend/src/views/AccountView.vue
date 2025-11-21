<template>
  <div class="min-h-screen bg-gray-100">
    <!-- ヘッダー -->
    <header class="bg-white shadow">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
        <h1 class="text-2xl font-bold text-gray-900">会員マイページ</h1>
        <div class="flex space-x-4">
          <router-link
            to="/points"
            class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 transition duration-200"
          >
            ポイント
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
        <!-- ユーザー情報カード -->
        <div class="bg-white shadow rounded-lg p-6">
          <h2 class="text-xl font-semibold text-gray-800 mb-4 flex items-center">
            <svg class="w-6 h-6 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
            アカウント情報
          </h2>
          
          <div v-if="accountData?.user" class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <p class="text-sm text-gray-600">ユーザーID</p>
              <p class="text-lg font-medium text-gray-900">{{ accountData.user.id }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-600">氏名</p>
              <p class="text-lg font-medium text-gray-900">{{ accountData.user.fullName }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-600">ユーザー名</p>
              <p class="text-lg font-medium text-gray-900">{{ accountData.user.username }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-600">メールアドレス</p>
              <p class="text-lg font-medium text-gray-900">{{ accountData.user.email }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-600">登録日時</p>
              <p class="text-lg font-medium text-gray-900">{{ formatDate(accountData.user.createdAt) }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-600">更新日時</p>
              <p class="text-lg font-medium text-gray-900">{{ formatDate(accountData.user.updatedAt) }}</p>
            </div>
          </div>
        </div>

        <!-- 機能説明カード -->
        <div class="bg-blue-50 border-l-4 border-blue-500 p-4">
          <div class="flex">
            <div class="flex-shrink-0">
              <svg class="h-5 w-5 text-blue-500" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
              </svg>
            </div>
            <div class="ml-3">
              <p class="text-sm text-blue-700">
                このページではアカウント情報を確認できます。
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
import { authService, userService } from '../services/api'

export default {
  name: 'AccountView',
  setup() {
    const router = useRouter()
    const accountData = ref(null)
    const loading = ref(true)
    const errorMessage = ref('')

    const loadAccountData = async () => {
      loading.value = true
      errorMessage.value = ''

      try {
        const data = await userService.getAccount()
        accountData.value = data
      } catch (error) {
        errorMessage.value = error.response?.data?.error || 'アカウント情報の取得に失敗しました'
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
      loadAccountData()
    })

    return {
      accountData,
      loading,
      errorMessage,
      handleLogout,
      formatDate
    }
  }
}
</script>
