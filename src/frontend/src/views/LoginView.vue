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
            placeholder="tanaka_taro または 05c66ceb-..."
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
          <li>• UUID: 05c66ceb-6ddc-4ada-b736-08702615ff48 (田中太郎)</li>
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
        console.log('[LOGIN] Step 1: Login attempt with:', userId.value)
        const response = await authService.login(userId.value, password.value)
        console.log('[LOGIN] Step 2: Login response received:', { userId: response.userId, hasToken: !!response.token, tokenLength: response.token?.length })
        
        try {
          // トークンとユーザーIDを保存
          console.log('[LOGIN] Step 3: Saving token to localStorage...')
          localStorage.setItem('authToken', response.token)
          console.log('[LOGIN] Step 4: authToken saved successfully')
          
          localStorage.setItem('userId', response.userId)
          console.log('[LOGIN] Step 5: userId saved successfully')
          
          // アカウント画面へ遷移
          console.log('[LOGIN] Step 6: Attempting to navigate to /account')
          await router.push('/account')
          console.log('[LOGIN] Step 7: Navigation to /account completed')
        } catch (innerError) {
          console.error('[LOGIN] Inner error during token save or navigation:', innerError)
          throw innerError
        }
      } catch (error) {
        console.error('[LOGIN] Main catch block - Login error:', error)
        console.error('[LOGIN] Error details:', { name: error.name, message: error.message })
        // エラーメッセージを設定
        if (error.response) {
          // レスポンスがある場合
          const data = error.response.data
          if (typeof data === 'string') {
            errorMessage.value = data
          } else if (data && data.error) {
            errorMessage.value = data.error
          } else if (data && data.message) {
            errorMessage.value = data.message
          } else {
            errorMessage.value = `ログインに失敗しました (${error.response.status})`
          }
        } else if (error.message) {
          errorMessage.value = error.message
        } else {
          errorMessage.value = 'ログインに失敗しました'
        }
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
