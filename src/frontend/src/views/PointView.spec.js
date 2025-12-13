import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import PointView from './PointView.vue'
import { authService } from '../services/api'
import { pointApi } from '../services/pointApi'

jest.mock('../services/api')
jest.mock('../services/pointApi')

describe('PointView.vue', () => {
  let router
  let wrapper

  beforeEach(() => {
    // Setup router
    router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/points', component: { template: '<div>Points</div>' } },
        { path: '/points/history', component: { template: '<div>History</div>' } },
        { path: '/account', component: { template: '<div>Account</div>' } },
        { path: '/login', component: { template: '<div>Login</div>' } }
      ]
    })

    jest.clearAllMocks()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  const mockPointData = {
    balance: 1500,
    lastUpdated: '2024-01-15T10:00:00Z'
  }

  const mountComponent = async () => {
    wrapper = mount(PointView, {
      global: {
        plugins: [router],
        stubs: {
          'router-link': {
            template: '<a><slot /></a>',
            props: ['to']
          }
        }
      }
    })
    await router.isReady()
    return wrapper
  }

  describe('initial rendering', () => {
    it('shows loading state initially', async () => {
      pointApi.getPoints.mockImplementation(() => new Promise(() => {}))
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.animate-spin').exists()).toBe(true)
      expect(wrapper.text()).toContain('ポイント残高')
    })

    it('renders header with navigation buttons', async () => {
      pointApi.getPoints.mockResolvedValue(mockPointData)
      
      const wrapper = await mountComponent()
      await flushPromises()
      
      expect(wrapper.find('h1').text()).toBe('ポイント残高')
      expect(wrapper.text()).toContain('戻る')
      expect(wrapper.text()).toContain('ログアウト')
    })
  })

  describe('successful data loading', () => {
    beforeEach(() => {
      pointApi.getPoints.mockResolvedValue(mockPointData)
    })

    it('displays point balance correctly', async () => {
      const wrapper = await mountComponent()
      await flushPromises()
      
      const balance = wrapper.find('[data-testid="current-balance"]').text()
      expect(balance).toContain('1,500')
    })

    it('displays point balance with 0 when balance is missing', async () => {
      pointApi.getPoints.mockResolvedValue({ lastUpdated: '2024-01-15T10:00:00Z' })
      
      const wrapper = await mountComponent()
      await flushPromises()
      
      const balance = wrapper.find('[data-testid="current-balance"]').text()
      expect(balance).toBe('0')
    })

    it('displays last updated time', async () => {
      const wrapper = await mountComponent()
      await flushPromises()
      
      const lastUpdated = wrapper.find('[data-testid="last-updated"]').text()
      expect(lastUpdated).toContain('最終更新')
      expect(lastUpdated).toContain('2024')
    })

    it('hides loading spinner after data loads', async () => {
      const wrapper = await mountComponent()
      await flushPromises()
      
      expect(wrapper.find('.animate-spin').exists()).toBe(false)
    })

    it('does not show error message when data loads successfully', async () => {
      const wrapper = await mountComponent()
      await flushPromises()
      
      expect(wrapper.find('.bg-red-100').exists()).toBe(false)
    })

    it('displays point history link', async () => {
      const wrapper = await mountComponent()
      await flushPromises()
      
      expect(wrapper.text()).toContain('ポイント履歴を見る')
    })

    it('displays information card', async () => {
      const wrapper = await mountComponent()
      await flushPromises()
      
      expect(wrapper.text()).toContain('ポイントは様々なサービスでご利用いただけます')
    })
  })

  describe('error handling', () => {
    it('displays 503 service unavailable error message', async () => {
      const mockError = {
        response: {
          status: 503,
          data: {}
        }
      }
      pointApi.getPoints.mockRejectedValue(mockError)
      
      const wrapper = await mountComponent()
      await flushPromises()
      
      expect(wrapper.find('.bg-red-100').exists()).toBe(true)
      expect(wrapper.find('.bg-red-100').text()).toContain('ポイントサービスが一時的に利用できません')
    })

    it('displays error message when getPoints fails', async () => {
      const mockError = {
        response: {
          status: 500,
          data: {
            error: 'ポイント取得エラー'
          }
        }
      }
      pointApi.getPoints.mockRejectedValue(mockError)
      
      const wrapper = await mountComponent()
      await flushPromises()
      
      expect(wrapper.find('.bg-red-100').exists()).toBe(true)
      expect(wrapper.find('.bg-red-100').text()).toBe('ポイント取得エラー')
    })

    it('displays default error message when no error details', async () => {
      const mockError = new Error('Network error')
      pointApi.getPoints.mockRejectedValue(mockError)
      
      const wrapper = await mountComponent()
      await flushPromises()
      
      expect(wrapper.find('.bg-red-100').exists()).toBe(true)
      expect(wrapper.find('.bg-red-100').text()).toBe('ポイント情報の取得に失敗しました')
    })

    it('hides loading spinner when error occurs', async () => {
      pointApi.getPoints.mockRejectedValue(new Error('Error'))
      
      const wrapper = await mountComponent()
      await flushPromises()
      
      expect(wrapper.find('.animate-spin').exists()).toBe(false)
    })

    it('does not display point balance when error occurs', async () => {
      pointApi.getPoints.mockRejectedValue(new Error('Error'))
      
      const wrapper = await mountComponent()
      await flushPromises()
      
      expect(wrapper.find('[data-testid="current-balance"]').exists()).toBe(false)
    })
  })

  describe('logout functionality', () => {
    beforeEach(() => {
      pointApi.getPoints.mockResolvedValue(mockPointData)
    })

    it('calls authService.logout and redirects to login', async () => {
      authService.logout.mockResolvedValue()
      
      const wrapper = await mountComponent()
      await flushPromises()
      
      const pushSpy = jest.spyOn(router, 'push')
      const logoutButton = wrapper.findAll('button').find(btn => btn.text() === 'ログアウト')
      
      await logoutButton.trigger('click')
      await flushPromises()
      
      expect(authService.logout).toHaveBeenCalled()
      expect(pushSpy).toHaveBeenCalledWith('/login')
    })

    it('redirects to login even when logout fails', async () => {
      authService.logout.mockRejectedValue(new Error('Logout failed'))
      
      const wrapper = await mountComponent()
      await flushPromises()
      
      const pushSpy = jest.spyOn(router, 'push')
      const logoutButton = wrapper.findAll('button').find(btn => btn.text() === 'ログアウト')
      
      await logoutButton.trigger('click')
      await flushPromises()
      
      expect(pushSpy).toHaveBeenCalledWith('/login')
    })
  })

  describe('date formatting', () => {
    beforeEach(() => {
      pointApi.getPoints.mockResolvedValue(mockPointData)
    })

    it('formats timestamp to Japanese locale', async () => {
      const wrapper = await mountComponent()
      await flushPromises()
      
      const vm = wrapper.vm
      const formatted = vm.formatLastUpdated('2024-01-15T10:00:00Z')
      
      expect(formatted).toMatch(/2024/)
      expect(formatted).toMatch(/01/)
      expect(formatted).toMatch(/15/)
    })

    it('returns empty string for invalid timestamp', async () => {
      const wrapper = await mountComponent()
      await flushPromises()
      
      const vm = wrapper.vm
      expect(vm.formatLastUpdated(null)).toBe('')
      expect(vm.formatLastUpdated(undefined)).toBe('')
      expect(vm.formatLastUpdated('')).toBe('')
    })

    it('handles invalid date gracefully', async () => {
      const wrapper = await mountComponent()
      await flushPromises()
      
      const vm = wrapper.vm
      const result = vm.formatLastUpdated('invalid-date')
      // Invalid dates may return 'Invalid Date' or empty string depending on browser
      expect(['', 'Invalid Date'].includes(result)).toBe(true)
    })
  })

  describe('onMounted lifecycle', () => {
    it('calls loadPointData on mount', async () => {
      pointApi.getPoints.mockResolvedValue(mockPointData)
      
      const wrapper = await mountComponent()
      
      expect(pointApi.getPoints).toHaveBeenCalled()
    })
  })

  describe('balance formatting', () => {
    it('formats large numbers with commas', async () => {
      pointApi.getPoints.mockResolvedValue({ balance: 1234567, lastUpdated: '2024-01-15T10:00:00Z' })
      
      const wrapper = await mountComponent()
      await flushPromises()
      
      const balance = wrapper.find('[data-testid="current-balance"]').text()
      expect(balance).toContain('1,234,567')
    })

    it('displays 0 for zero balance', async () => {
      pointApi.getPoints.mockResolvedValue({ balance: 0, lastUpdated: '2024-01-15T10:00:00Z' })
      
      const wrapper = await mountComponent()
      await flushPromises()
      
      const balance = wrapper.find('[data-testid="current-balance"]').text()
      expect(balance).toBe('0')
    })
  })
})
