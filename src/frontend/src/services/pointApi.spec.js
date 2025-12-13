import { pointApi } from './pointApi.js'
import { apiClient } from './api.js'

jest.mock('./api.js', () => ({
  apiClient: {
    get: jest.fn()
  }
}))

describe('pointApi.js', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('getPoints', () => {
    it('should get point balance', async () => {
      const mockResponse = {
        data: { balance: 1500, lastUpdated: '2024-01-01T00:00:00Z' }
      }
      apiClient.get.mockResolvedValue(mockResponse)

      const result = await pointApi.getPoints()

      expect(apiClient.get).toHaveBeenCalledWith('/points')
      expect(result).toEqual(mockResponse.data)
    })

    it('should handle point fetch failure', async () => {
      const mockError = new Error('Failed to fetch points')
      apiClient.get.mockRejectedValue(mockError)

      await expect(pointApi.getPoints()).rejects.toThrow('Failed to fetch points')
    })

    it('should handle service unavailable error (503)', async () => {
      const mockError = new Error('Service unavailable')
      mockError.response = { status: 503 }
      apiClient.get.mockRejectedValue(mockError)

      await expect(pointApi.getPoints()).rejects.toThrow('Service unavailable')
    })
  })

  describe('getPointHistory', () => {
    it('should get point history with default pagination', async () => {
      const mockResponse = {
        data: {
          items: [
            { id: '1', amount: 100, type: 'EARN', createdAt: '2024-01-01T00:00:00Z' }
          ],
          page: 1,
          limit: 10,
          total: 1
        }
      }
      apiClient.get.mockResolvedValue(mockResponse)

      const result = await pointApi.getPointHistory()

      expect(apiClient.get).toHaveBeenCalledWith('/points/history?page=1&limit=10')
      expect(result).toEqual(mockResponse.data)
    })

    it('should get point history with custom pagination', async () => {
      const mockResponse = {
        data: {
          items: [],
          page: 2,
          limit: 20,
          total: 0
        }
      }
      apiClient.get.mockResolvedValue(mockResponse)

      const result = await pointApi.getPointHistory(2, 20)

      expect(apiClient.get).toHaveBeenCalledWith('/points/history?page=2&limit=20')
      expect(result).toEqual(mockResponse.data)
    })

    it('should handle point history fetch failure', async () => {
      const mockError = new Error('Failed to fetch history')
      apiClient.get.mockRejectedValue(mockError)

      await expect(pointApi.getPointHistory()).rejects.toThrow('Failed to fetch history')
    })

    it('should handle service unavailable error (503)', async () => {
      const mockError = new Error('Service unavailable')
      mockError.response = { status: 503 }
      apiClient.get.mockRejectedValue(mockError)

      await expect(pointApi.getPointHistory(1, 10)).rejects.toThrow('Service unavailable')
    })
  })
})
