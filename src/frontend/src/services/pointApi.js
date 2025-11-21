import { apiClient } from './api.js'

export const pointApi = {
  /**
   * ポイント残高取得
   * GET /api/points
   */
  async getPoints() {
    const response = await apiClient.get('/points')
    return response.data
  },
  
  /**
   * ポイント履歴取得
   * GET /api/points/history?page=1&limit=10
   * @param {number} page - ページ番号（デフォルト: 1）
   * @param {number} limit - 1ページあたりの取得件数（デフォルト: 10）
   */
  async getPointHistory(page = 1, limit = 10) {
    const response = await apiClient.get(`/points/history?page=${page}&limit=${limit}`)
    return response.data
  }
}
