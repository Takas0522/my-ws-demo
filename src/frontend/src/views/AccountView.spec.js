import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import AccountView from './AccountView.vue'
import { authService, userService } from '../services/api'

jest.mock('../services/api')

describe('AccountView.vue', () => {
  let router
  let wrapper

  beforeEach(() => {
    // Setup router
    router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/account', component: { template: '<div>Account</div>' } },
        { path: '/login', component: { template: '<div>Login</div>' } },
        { path: '/points', component: { template: '<div>Points</div>' } }
      ]
    })

    jest.clearAllMocks()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  const mockAccountData = {
    user: {
      id: '123e4567-e89b-4000-a456-426614174000',
      fullName: '田中太郎',
      username: 'tanaka_taro',
      email: 'tanaka@example.com',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-15T10:30:00Z'
    },
    points: {
      balance: 1500,
      lastUpdated: '2024-01-15T10:00:00Z'
    }
  }

  const mountComponent = async () => {
    wrapper = mount(AccountView, {
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
      userService.getAccount.mockImplementation(() => new Promise(() => {}))
      
      const wrapper = await mountComponent()
      
      expect(wrapper.find('.animate-spin').exists()).toBe(true)
      expect(wrapper.text()).toContain('会員マイページ')
    })

    it('renders header with navigation buttons', async () => {
      userService.getAccount.mockResolvedValue(mockAccountData)
      
      const wrapper = await mountComponent()
      await flushPromises()
      
      expect(wrapper.find('h1').text()).toBe('会員マイページ')
      expect(wrapper.text()).toContain('ポイント')
      expect(wrapper.text()).toContain('ログアウト')
    })
  })

  describe('successful data loading', () => {
    beforeEach(() => {
      userService.getAccount.mockResolvedValue(mockAccountData)
    })

    it('displays account information correctly', async () => {
      const wrapper = await mountComponent()
      await flushPromises()
      
      expect(wrapper.find('[data-testid="user-id"]').text()).toBe(mockAccountData.user.id)
      expect(wrapper.find('[data-testid="user-name"]').text()).toBe(mockAccountData.user.fullName)
      expect(wrapper.find('[data-testid="user-email"]').text()).toBe(mockAccountData.user.email)
      expect(wrapper.text()).toContain(mockAccountData.user.username)
    })

    it('displays point balance correctly', async () => {
      const wrapper = await mountComponent()
      await flushPromises()
      
      const balanceText = wrapper.find('[data-testid="point-balance"]').text()
      expect(balanceText).toContain('1500')
      expect(balanceText).toContain('ポイント')
    })

    it('formats dates correctly', async () => {
      const wrapper = await mountComponent()
      await flushPromises()
      
      const text = wrapper.text()
      // Check that dates are formatted (Japanese locale format)
      expect(text).toContain('2024')
      expect(text).toContain('01')
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

    it('displays account information card', async () => {
      const wrapper = await mountComponent()
      await flushPromises()
      
      expect(wrapper.text()).toContain('アカウント情報')
      expect(wrapper.text()).toContain('ユーザーID')
      expect(wrapper.text()).toContain('氏名')
      expect(wrapper.text()).toContain('メールアドレス')
    })

    it('displays point information card', async () => {
      const wrapper = await mountComponent()
      await flushPromises()
      
      expect(wrapper.text()).toContain('ポイント情報')
      expect(wrapper.text()).toContain('現在のポイント残高')
    })
  })

  describe('error handling', () => {
    it('displays error message when getAccount fails', async () => {
      const mockError = {
        response: {
          data: {
            error: 'アカウント情報の取得に失敗しました'
          }
        }
      }
      userService.getAccount.mockRejectedValue(mockError)
      
      const wrapper = await mountComponent()
      await flushPromises()
      
      expect(wrapper.find('.bg-red-100').exists()).toBe(true)
      expect(wrapper.find('.bg-red-100').text()).toBe('アカウント情報の取得に失敗しました')
    })

    it('displays default error message when no error details', async () => {
      const mockError = new Error('Network error')
      userService.getAccount.mockRejectedValue(mockError)
      
      const wrapper = await mountComponent()
      await flushPromises()
      
      expect(wrapper.find('.bg-red-100').exists()).toBe(true)
      expect(wrapper.find('.bg-red-100').text()).toBe('アカウント情報の取得に失敗しました')
    })

    it('hides loading spinner when error occurs', async () => {
      userService.getAccount.mockRejectedValue(new Error('Error'))
      
      const wrapper = await mountComponent()
      await flushPromises()
      
      expect(wrapper.find('.animate-spin').exists()).toBe(false)
    })

    it('does not display account cards when error occurs', async () => {
      userService.getAccount.mockRejectedValue(new Error('Error'))
      
      const wrapper = await mountComponent()
      await flushPromises()
      
      expect(wrapper.find('[data-testid="user-id"]').exists()).toBe(false)
      expect(wrapper.find('[data-testid="point-balance"]').exists()).toBe(false)
    })
  })

  describe('logout functionality', () => {
    beforeEach(() => {
      userService.getAccount.mockResolvedValue(mockAccountData)
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
    it('formats date strings to Japanese locale', async () => {
      userService.getAccount.mockResolvedValue(mockAccountData)
      
      const wrapper = await mountComponent()
      await flushPromises()
      
      // The component should format dates using Japanese locale
      const vm = wrapper.vm
      const formatted = vm.formatDate('2024-01-15T10:30:00Z')
      
      expect(formatted).toMatch(/2024/)
      expect(formatted).toMatch(/01/)
      expect(formatted).toMatch(/15/)
    })

    it('returns "-" for null or undefined dates', async () => {
      userService.getAccount.mockResolvedValue(mockAccountData)
      
      const wrapper = await mountComponent()
      await flushPromises()
      
      const vm = wrapper.vm
      expect(vm.formatDate(null)).toBe('-')
      expect(vm.formatDate(undefined)).toBe('-')
      expect(vm.formatDate('')).toBe('-')
    })
  })

  describe('onMounted lifecycle', () => {
    it('calls loadAccountData on mount', async () => {
      userService.getAccount.mockResolvedValue(mockAccountData)
      
      const wrapper = await mountComponent()
      
      expect(userService.getAccount).toHaveBeenCalled()
    })
  })
})
