// localStorage をモジュールが読み込まれる前にモック
const localStorageMock = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn()
};
Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
  writable: true
});

// window.location をモック
delete window.location;
window.location = { href: '', pathname: '/account' };

// axiosをモジュール全体でモック
const mockPost = jest.fn();
const mockGet = jest.fn();
const mockInterceptors = {
  request: {
    use: jest.fn()
  },
  response: {
    use: jest.fn()
  }
};

jest.mock('axios', () => ({
  create: jest.fn(() => ({
    post: mockPost,
    get: mockGet,
    interceptors: mockInterceptors
  }))
}));

// モジュールをインポート
const { authService, userService } = require('./api');

describe('api.js', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('authService', () => {
    describe('login', () => {
      it('should login with UUID format userId', async () => {
        const userId = '05c66ceb-6ddc-4ada-b736-08702615ff48';
        const password = 'password123';
        const mockResponse = {
          data: {
            token: 'mock-token',
            userId: userId
          }
        };

        mockPost.mockResolvedValue(mockResponse);

        const result = await authService.login(userId, password);

        expect(mockPost).toHaveBeenCalledWith('/login', {
          userId: userId,
          password: password
        });
        expect(result).toEqual(mockResponse.data);
      });

      it('should login with username', async () => {
        const username = 'tanaka_taro';
        const password = 'password123';
        const mockResponse = {
          data: {
            token: 'mock-token',
            userId: '05c66ceb-6ddc-4ada-b736-08702615ff48'
          }
        };

        mockPost.mockResolvedValue(mockResponse);

        const result = await authService.login(username, password);

        expect(mockPost).toHaveBeenCalledWith('/login', {
          username: username,
          password: password
        });
        expect(result).toEqual(mockResponse.data);
      });

      it('should differentiate between UUID and username correctly', async () => {
        const validUUID = '05c66ceb-6ddc-4ada-b736-08702615ff48';
        const invalidUUID = '05c66ceb-6ddc-4ada-b736-invalid';
        const mockResponse = {
          data: {
            token: 'mock-token',
            userId: validUUID
          }
        };

        mockPost.mockResolvedValue(mockResponse);

        // Valid UUID
        await authService.login(validUUID, 'pass');
        expect(mockPost).toHaveBeenCalledWith('/login', {
          userId: validUUID,
          password: 'pass'
        });

        // Invalid UUID should be treated as username
        await authService.login(invalidUUID, 'pass');
        expect(mockPost).toHaveBeenCalledWith('/login', {
          username: invalidUUID,
          password: 'pass'
        });
      });
    });

    describe('logout', () => {
      it('should logout and clear localStorage', async () => {
        mockPost.mockResolvedValue({});

        await authService.logout();

        expect(mockPost).toHaveBeenCalledWith('/logout');
        expect(localStorage.removeItem).toHaveBeenCalledWith('authToken');
        expect(localStorage.removeItem).toHaveBeenCalledWith('userId');
      });

      it('should clear localStorage even if API call fails', async () => {
        mockPost.mockRejectedValue(new Error('Network error'));

        try {
          await authService.logout();
        } catch (error) {
          // エラーは無視
        }

        expect(localStorage.removeItem).toHaveBeenCalledWith('authToken');
        expect(localStorage.removeItem).toHaveBeenCalledWith('userId');
      });
    });

    describe('verifyToken', () => {
      it('should verify token', async () => {
        const mockResponse = {
          data: { valid: true }
        };

        mockGet.mockResolvedValue(mockResponse);

        const result = await authService.verifyToken();

        expect(mockGet).toHaveBeenCalledWith('/verify');
        expect(result).toEqual(mockResponse.data);
      });
    });
  });

  describe('userService', () => {
    describe('getAccount', () => {
      it('should get account data', async () => {
        const mockResponse = {
          data: {
            user: {
              id: '05c66ceb-6ddc-4ada-b736-08702615ff48',
              username: 'tanaka_taro',
              fullName: '田中太郎',
              email: 'tanaka@example.com'
            }
          }
        };

        mockGet.mockResolvedValue(mockResponse);

        const result = await userService.getAccount();

        expect(mockGet).toHaveBeenCalledWith('/account');
        expect(result).toEqual(mockResponse.data);
      });
    });

    describe('getUser', () => {
      it('should get user by valid UUID', async () => {
        const userId = '05c66ceb-6ddc-4ada-b736-08702615ff48';
        const mockResponse = {
          data: {
            id: userId,
            username: 'tanaka_taro',
            fullName: '田中太郎'
          }
        };

        mockGet.mockResolvedValue(mockResponse);

        const result = await userService.getUser(userId);

        expect(mockGet).toHaveBeenCalledWith(`/users/${userId}`);
        expect(result).toEqual(mockResponse.data);
      });

      it('should throw error for invalid UUID', async () => {
        const invalidUserId = 'invalid-id';

        await expect(userService.getUser(invalidUserId)).rejects.toThrow('Invalid UUID format');
      });

      it('should throw error for UUID with wrong format', async () => {
        const invalidUserId = '05c66ceb-6ddc-3ada-b736-08702615ff48'; // 3 instead of 4

        await expect(userService.getUser(invalidUserId)).rejects.toThrow('Invalid UUID format');
      });
    });
  });
});
