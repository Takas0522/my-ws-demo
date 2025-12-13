jest.mock('axios')

import axios from 'axios'
import { authService, userService } from './api.js'

describe('api.js', () => {
  let mockAxiosInstance

  beforeEach(() => {
    // Get the mocked instance that was created by api.js
    mockAxiosInstance = axios.create()
    jest.clearAllMocks()
    // Clear localStorage
    localStorage.clear()
  })

  describe('authService', () => {
    describe('login', () => {
      it('should login with UUID format userId', async () => {
        const mockResponse = {
          data: {
            token: 'test-token',
            userId: '123e4567-e89b-4000-a456-426614174000'
          }
        }
        mockAxiosInstance.post.mockResolvedValue(mockResponse)

        const result = await authService.login('123e4567-e89b-4000-a456-426614174000', 'password123')

        expect(mockAxiosInstance.post).toHaveBeenCalledWith('/login', {
          userId: '123e4567-e89b-4000-a456-426614174000',
          password: 'password123'
        })
        expect(result).toEqual(mockResponse.data)
      })

      it('should login with username', async () => {
        const mockResponse = {
          data: {
            token: 'test-token',
            userId: '123e4567-e89b-4000-a456-426614174000'
          }
        }
        mockAxiosInstance.post.mockResolvedValue(mockResponse)

        const result = await authService.login('tanaka_taro', 'password123')

        expect(mockAxiosInstance.post).toHaveBeenCalledWith('/login', {
          username: 'tanaka_taro',
          password: 'password123'
        })
        expect(result).toEqual(mockResponse.data)
      })

      it('should handle login failure', async () => {
        const mockError = new Error('Login failed')
        mockAxiosInstance.post.mockRejectedValue(mockError)

        await expect(authService.login('tanaka_taro', 'wrongpassword')).rejects.toThrow('Login failed')
      })
    })

    describe('logout', () => {
      it('should logout and clear localStorage', async () => {
        localStorage.setItem('authToken', 'test-token')
        localStorage.setItem('userId', 'test-user-id')
        mockAxiosInstance.post.mockResolvedValue({})

        await authService.logout()

        expect(mockAxiosInstance.post).toHaveBeenCalledWith('/logout')
        expect(localStorage.getItem('authToken')).toBeNull()
        expect(localStorage.getItem('userId')).toBeNull()
      })

      it('should clear localStorage even if API call fails', async () => {
        localStorage.setItem('authToken', 'test-token')
        localStorage.setItem('userId', 'test-user-id')
        mockAxiosInstance.post.mockRejectedValue(new Error('Network error'))

        try {
          await authService.logout()
        } catch (error) {
          // Errors are swallowed by the finally block in authService.logout
        }

        expect(localStorage.getItem('authToken')).toBeNull()
        expect(localStorage.getItem('userId')).toBeNull()
      })
    })

    describe('verifyToken', () => {
      it('should verify token successfully', async () => {
        const mockResponse = {
          data: { valid: true, userId: 'test-user-id' }
        }
        mockAxiosInstance.get.mockResolvedValue(mockResponse)

        const result = await authService.verifyToken()

        expect(mockAxiosInstance.get).toHaveBeenCalledWith('/verify')
        expect(result).toEqual(mockResponse.data)
      })

      it('should handle verification failure', async () => {
        const mockError = new Error('Invalid token')
        mockAxiosInstance.get.mockRejectedValue(mockError)

        await expect(authService.verifyToken()).rejects.toThrow('Invalid token')
      })
    })
  })

  describe('userService', () => {
    describe('getAccount', () => {
      it('should get account information', async () => {
        const mockResponse = {
          data: {
            user: { id: 'test-id', name: 'Test User' },
            points: { balance: 1000 }
          }
        }
        mockAxiosInstance.get.mockResolvedValue(mockResponse)

        const result = await userService.getAccount()

        expect(mockAxiosInstance.get).toHaveBeenCalledWith('/account')
        expect(result).toEqual(mockResponse.data)
      })

      it('should handle account fetch failure', async () => {
        const mockError = new Error('Account not found')
        mockAxiosInstance.get.mockRejectedValue(mockError)

        await expect(userService.getAccount()).rejects.toThrow('Account not found')
      })
    })

    describe('getUser', () => {
      it('should get user information with valid UUID', async () => {
        const userId = '123e4567-e89b-4000-a456-426614174000'
        const mockResponse = {
          data: { id: userId, name: 'Test User' }
        }
        mockAxiosInstance.get.mockResolvedValue(mockResponse)

        const result = await userService.getUser(userId)

        expect(mockAxiosInstance.get).toHaveBeenCalledWith(`/users/${userId}`)
        expect(result).toEqual(mockResponse.data)
      })

      it('should throw error for invalid UUID format', async () => {
        await expect(userService.getUser('invalid-uuid')).rejects.toThrow('Invalid UUID format')
        expect(mockAxiosInstance.get).not.toHaveBeenCalled()
      })

      it('should handle user fetch failure', async () => {
        const userId = '123e4567-e89b-4000-a456-426614174000'
        const mockError = new Error('User not found')
        mockAxiosInstance.get.mockRejectedValue(mockError)

        await expect(userService.getUser(userId)).rejects.toThrow('User not found')
      })
    })
  })
})
