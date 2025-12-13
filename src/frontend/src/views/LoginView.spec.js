import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import LoginView from './LoginView.vue'
import { authService } from '../services/api'

jest.mock('../services/api')

describe('LoginView.vue', () => {
  let router
  let wrapper

  beforeEach(() => {
    // Setup router
    router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/login', component: { template: '<div>Login</div>' } },
        { path: '/account', component: { template: '<div>Account</div>' } }
      ]
    })

    localStorage.clear()
    jest.clearAllMocks()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  const mountComponent = async () => {
    wrapper = mount(LoginView, {
      global: {
        plugins: [router]
      }
    })
    await router.isReady()
    return wrapper
  }

  describe('rendering', () => {
    it('renders login form', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('h1').text()).toBe('ログイン')
      expect(wrapper.find('#userId').exists()).toBe(true)
      expect(wrapper.find('#password').exists()).toBe(true)
      expect(wrapper.find('button[type="submit"]').exists()).toBe(true)
    })

    it('displays test user information', async () => {
      const wrapper = await mountComponent()
      
      const text = wrapper.text()
      expect(text).toContain('tanaka_taro')
      expect(text).toContain('suzuki_hanako')
      expect(text).toContain('yamada_jiro')
      expect(text).toContain('password123')
    })

    it('does not show error message initially', async () => {
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.bg-red-100').exists()).toBe(false)
    })
  })

  describe('form interaction', () => {
    it('updates userId and password on input', async () => {
      const wrapper = await mountComponent()
      
      const userIdInput = wrapper.find('#userId')
      const passwordInput = wrapper.find('#password')
      
      await userIdInput.setValue('test_user')
      await passwordInput.setValue('test_password')
      
      expect(userIdInput.element.value).toBe('test_user')
      expect(passwordInput.element.value).toBe('test_password')
    })

    it('submit button shows "ログイン" text by default', async () => {
      const wrapper = await mountComponent()
      
      const button = wrapper.find('button[type="submit"]')
      expect(button.text()).toBe('ログイン')
    })
  })

  describe('successful login', () => {
    it('should login successfully and redirect to account page', async () => {
      const mockResponse = {
        token: 'test-token-123',
        userId: 'test-user-id'
      }
      authService.login.mockResolvedValue(mockResponse)
      
      const wrapper = await mountComponent()
      const pushSpy = jest.spyOn(router, 'push')
      
      await wrapper.find('#userId').setValue('tanaka_taro')
      await wrapper.find('#password').setValue('password123')
      await wrapper.find('form').trigger('submit.prevent')
      
      // Wait for async operations
      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 100))
      
      expect(authService.login).toHaveBeenCalledWith('tanaka_taro', 'password123')
      expect(localStorage.getItem('authToken')).toBe('test-token-123')
      expect(localStorage.getItem('userId')).toBe('test-user-id')
      expect(pushSpy).toHaveBeenCalledWith('/account')
    })

    it('shows loading state during login', async () => {
      authService.login.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)))
      
      const wrapper = await mountComponent()
      
      await wrapper.find('#userId').setValue('test_user')
      await wrapper.find('#password').setValue('test_password')
      
      const button = wrapper.find('button[type="submit"]')
      await wrapper.find('form').trigger('submit.prevent')
      
      // Check immediately after submit
      await wrapper.vm.$nextTick()
      expect(button.element.disabled).toBe(true)
      expect(button.text()).toBe('ログイン中...')
    })
  })

  describe('failed login', () => {
    it('should display error message on login failure with response', async () => {
      const mockError = {
        response: {
          status: 401,
          data: { error: '認証に失敗しました' }
        }
      }
      authService.login.mockRejectedValue(mockError)
      
      const wrapper = await mountComponent()
      
      await wrapper.find('#userId').setValue('wrong_user')
      await wrapper.find('#password').setValue('wrong_password')
      await wrapper.find('form').trigger('submit.prevent')
      
      // Wait for async operations
      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 50))
      
      expect(wrapper.find('.bg-red-100').exists()).toBe(true)
      expect(wrapper.find('.bg-red-100').text()).toBe('認証に失敗しました')
    })

    it('should display error message with status code when no error details', async () => {
      const mockError = {
        response: {
          status: 500,
          data: {}
        }
      }
      authService.login.mockRejectedValue(mockError)
      
      const wrapper = await mountComponent()
      
      await wrapper.find('#userId').setValue('test_user')
      await wrapper.find('#password').setValue('test_password')
      await wrapper.find('form').trigger('submit.prevent')
      
      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 50))
      
      expect(wrapper.find('.bg-red-100').exists()).toBe(true)
      expect(wrapper.find('.bg-red-100').text()).toContain('500')
    })

    it('should display error message when error has no response', async () => {
      const mockError = new Error('Network error')
      authService.login.mockRejectedValue(mockError)
      
      const wrapper = await mountComponent()
      
      await wrapper.find('#userId').setValue('test_user')
      await wrapper.find('#password').setValue('test_password')
      await wrapper.find('form').trigger('submit.prevent')
      
      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 50))
      
      expect(wrapper.find('.bg-red-100').exists()).toBe(true)
      expect(wrapper.find('.bg-red-100').text()).toBe('Network error')
    })

    it('should clear error message on new login attempt', async () => {
      const mockError = new Error('Login failed')
      authService.login.mockRejectedValueOnce(mockError)
        .mockResolvedValueOnce({ token: 'token', userId: 'id' })
      
      const wrapper = await mountComponent()
      
      // First attempt - fails
      await wrapper.find('#userId').setValue('test_user')
      await wrapper.find('#password').setValue('test_password')
      await wrapper.find('form').trigger('submit.prevent')
      
      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 50))
      
      expect(wrapper.find('.bg-red-100').exists()).toBe(true)
      
      // Second attempt - should clear error
      await wrapper.find('form').trigger('submit.prevent')
      
      // Error should be cleared immediately
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.bg-red-100').exists()).toBe(false)
    })

    it('should not store token on login failure', async () => {
      const mockError = new Error('Login failed')
      authService.login.mockRejectedValue(mockError)
      
      const wrapper = await mountComponent()
      
      await wrapper.find('#userId').setValue('test_user')
      await wrapper.find('#password').setValue('test_password')
      await wrapper.find('form').trigger('submit.prevent')
      
      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 50))
      
      expect(localStorage.getItem('authToken')).toBeNull()
      expect(localStorage.getItem('userId')).toBeNull()
    })
  })
})
