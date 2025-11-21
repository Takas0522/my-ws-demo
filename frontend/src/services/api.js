import axios from 'axios'

const API_BASE_URL = '/api'

// Axios インスタンス作成
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

// リクエストインターセプター
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// レスポンスインターセプター
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // 認証エラーの場合、ログイン画面へリダイレクト
      localStorage.removeItem('authToken')
      localStorage.removeItem('userId')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

/**
 * UUID形式の妥当性を検証
 */
function isValidUUID(uuid) {
  const regex = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i
  return regex.test(uuid)
}

export const authService = {
  /**
   * ログイン
   * userIdはUUID形式の文字列、またはusernameで認証
   */
  async login(userIdOrUsername, password) {
    // UUID形式かどうかチェック
    const isUUID = isValidUUID(userIdOrUsername)
    
    const requestBody = {
      password
    }
    
    if (isUUID) {
      requestBody.userId = userIdOrUsername
    } else {
      requestBody.username = userIdOrUsername
    }
    
    const response = await apiClient.post('/login', requestBody)
    return response.data
  },

  /**
   * ログアウト
   */
  async logout() {
    try {
      await apiClient.post('/logout')
    } finally {
      localStorage.removeItem('authToken')
      localStorage.removeItem('userId')
    }
  },

  /**
   * トークン検証
   */
  async verifyToken() {
    const response = await apiClient.get('/verify')
    return response.data
  }
}

export const userService = {
  /**
   * アカウント情報取得
   */
  async getAccount() {
    const response = await apiClient.get('/account')
    return response.data
  },

  /**
   * ユーザー情報取得
   * @param {string} userId - UUID形式のユーザーID
   */
  async getUser(userId) {
    if (!isValidUUID(userId)) {
      throw new Error('Invalid UUID format')
    }
    const response = await apiClient.get(`/users/${userId}`)
    return response.data
  }
}

// apiClientをexportして他のサービスファイルから使えるようにする
export { apiClient }
