import { pointApi } from './pointApi';
import { apiClient } from './api';

// apiClient をモック
jest.mock('./api', () => ({
  apiClient: {
    get: jest.fn()
  }
}));

describe('pointApi', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('getPoints', () => {
    it('should get points balance successfully', async () => {
      const mockResponse = {
        data: {
          balance: 1000
        }
      };

      apiClient.get.mockResolvedValue(mockResponse);

      const result = await pointApi.getPoints();

      expect(apiClient.get).toHaveBeenCalledWith('/points');
      expect(result).toEqual(mockResponse.data);
    });

    it('should handle error when getting points', async () => {
      const mockError = new Error('Network error');
      apiClient.get.mockRejectedValue(mockError);

      await expect(pointApi.getPoints()).rejects.toThrow('Network error');
    });
  });

  describe('getPointHistory', () => {
    it('should get point history with default parameters', async () => {
      const mockResponse = {
        data: {
          history: [
            {
              id: '1',
              type: 'EARN',
              amount: 100,
              description: 'Test transaction',
              balanceAfter: 1100,
              createdAt: '2024-01-01T00:00:00Z'
            }
          ],
          pagination: {
            currentPage: 1,
            totalPages: 5,
            totalItems: 50,
            limit: 10
          }
        }
      };

      apiClient.get.mockResolvedValue(mockResponse);

      const result = await pointApi.getPointHistory();

      expect(apiClient.get).toHaveBeenCalledWith('/points/history?page=1&limit=10');
      expect(result).toEqual(mockResponse.data);
    });

    it('should get point history with custom parameters', async () => {
      const mockResponse = {
        data: {
          history: [],
          pagination: {
            currentPage: 2,
            totalPages: 5,
            totalItems: 50,
            limit: 20
          }
        }
      };

      apiClient.get.mockResolvedValue(mockResponse);

      const result = await pointApi.getPointHistory(2, 20);

      expect(apiClient.get).toHaveBeenCalledWith('/points/history?page=2&limit=20');
      expect(result).toEqual(mockResponse.data);
    });

    it('should handle error when getting point history', async () => {
      const mockError = new Error('Service unavailable');
      apiClient.get.mockRejectedValue(mockError);

      await expect(pointApi.getPointHistory()).rejects.toThrow('Service unavailable');
    });
  });
});
