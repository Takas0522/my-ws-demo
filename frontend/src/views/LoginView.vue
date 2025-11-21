<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-r from-blue-500 to-purple-600">
    <div class="bg-white p-8 rounded-lg shadow-2xl w-full max-w-md">
      <h1 class="text-3xl font-bold text-center text-gray-800 mb-6">ログイン</h1>
      
      <form @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <label for="userId" class="block text-sm font-medium text-gray-700 mb-2">
            ユーザーID / ユーザー名
          </label>
          <input
            id="userId"
            v-model="userId"
            type="text"
            required
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            placeholder="tanaka_taro または 123e4567-..."
          />
        </div>

        <div>
          <label for="password" class="block text-sm font-medium text-gray-700 mb-2">
            パスワード
          </label>
          <input
            id="password"
            v-model="password"
            type="password"
            required
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            placeholder="password123"
          />
        </div>

        <div v-if="errorMessage" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
          {{ errorMessage }}
        </div>

        <button
          type="submit"
          :disabled="loading"
          class="w-full bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition duration-200 disabled:bg-gray-400"
        >
          {{ loading ? 'ログイン中...' : 'ログイン' }}
        </button>
      </form>

      <div class="mt-6 p-4 bg-gray-50 rounded-lg">
        <p class="text-sm text-gray-600 mb-2">テストユーザー:</p>
        <ul class="text-xs text-gray-500 space-y-1">
          <li>• ユーザー名: tanaka_taro (田中太郎)</li>
          <li>• ユーザー名: suzuki_hanako (鈴木花子)</li>
          <li>• ユーザー名: yamada_jiro (山田次郎)</li>
          <li>• UUID: 123e4567-e89b-12d3-a456-426614174000 (田中太郎)</li>
          <li>• パスワード: password123</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authService } from '../services/api'

export default {
  name: 'LoginView',
  setup() {
    const router = useRouter()
    const userId = ref('')
    const password = ref('')
    const errorMessage = ref('')
    const loading = ref(false)

    const handleLogin = async () => {
      errorMessage.value = ''
      loading.value = true

      try {
        const response = await authService.login(userId.value, password.value)
        
        // トークンとユーザーIDを保存
        localStorage.setItem('authToken', response.token)
        localStorage.setItem('userId', response.userId)
        
        // アカウント画面へ遷移
        router.push('/account')
      } catch (error) {
        errorMessage.value = error.response?.data?.error || 'ログインに失敗しました'
      } finally {
        loading.value = false
      }
    }

    return {
      userId,
      password,
      errorMessage,
      loading,
      handleLogin
    }
  }
}
</script>
