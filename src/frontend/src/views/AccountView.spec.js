import { mount, flushPromises } from '@vue/test-utils';
import { createRouter, createMemoryHistory } from 'vue-router';
import AccountView from './AccountView.vue';
import { authService, userService } from '../services/api';

// services をモック
jest.mock('../services/api', () => ({
  authService: {
    logout: jest.fn()
  },
  userService: {
    getAccount: jest.fn()
  }
}));

describe('AccountView.vue', () => {
  let wrapper;
  let router;

  beforeEach(() => {
    // ルーターのセットアップ
    router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/account', name: 'Account', component: { template: '<div>Account</div>' } },
        { path: '/login', name: 'Login', component: { template: '<div>Login</div>' } },
        { path: '/points', name: 'Points', component: { template: '<div>Points</div>' } }
      ]
    });

    jest.clearAllMocks();
  });

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount();
    }
  });

  const createWrapper = async () => {
    const w = mount(AccountView, {
      global: {
        plugins: [router],
        stubs: {
          'router-link': {
            template: '<a><slot /></a>',
            props: ['to']
          }
        }
      }
    });
    await flushPromises();
    return w;
  };

  it('renders the account view', async () => {
    userService.getAccount.mockResolvedValue({
      user: {
        id: '05c66ceb-6ddc-4ada-b736-08702615ff48',
        fullName: '田中太郎',
        username: 'tanaka_taro',
        email: 'tanaka@example.com',
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-02T00:00:00Z'
      }
    });

    wrapper = await createWrapper();
    await flushPromises();

    expect(wrapper.exists()).toBe(true);
    expect(wrapper.find('h1').text()).toBe('会員マイページ');
  });

  it('shows loading state initially', async () => {
    userService.getAccount.mockImplementation(() => new Promise(() => {}));

    wrapper = await createWrapper();

    expect(wrapper.find('.animate-spin').exists()).toBe(true);
  });

  it('displays account data after loading', async () => {
    const mockAccountData = {
      user: {
        id: '05c66ceb-6ddc-4ada-b736-08702615ff48',
        fullName: '田中太郎',
        username: 'tanaka_taro',
        email: 'tanaka@example.com',
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-02T00:00:00Z'
      }
    };

    userService.getAccount.mockResolvedValue(mockAccountData);

    wrapper = await createWrapper();
    await flushPromises();

    const content = wrapper.text();
    expect(content).toContain('田中太郎');
    expect(content).toContain('tanaka_taro');
    expect(content).toContain('tanaka@example.com');
    expect(content).toContain('05c66ceb-6ddc-4ada-b736-08702615ff48');
  });

  it('displays error message when account fetch fails', async () => {
    const mockError = {
      response: {
        data: {
          error: 'Failed to fetch account data'
        }
      }
    };

    userService.getAccount.mockRejectedValue(mockError);

    wrapper = await createWrapper();
    await flushPromises();

    expect(wrapper.find('.bg-red-100').exists()).toBe(true);
    expect(wrapper.find('.bg-red-100').text()).toBe('Failed to fetch account data');
  });

  it('displays generic error message when no specific error is provided', async () => {
    const mockError = {
      response: {
        data: {}
      }
    };

    userService.getAccount.mockRejectedValue(mockError);

    wrapper = await createWrapper();
    await flushPromises();

    expect(wrapper.find('.bg-red-100').text()).toBe('アカウント情報の取得に失敗しました');
  });

  it('formats dates correctly', async () => {
    const mockAccountData = {
      user: {
        id: '05c66ceb-6ddc-4ada-b736-08702615ff48',
        fullName: '田中太郎',
        username: 'tanaka_taro',
        email: 'tanaka@example.com',
        createdAt: '2024-01-01T10:30:00Z',
        updatedAt: '2024-01-02T15:45:00Z'
      }
    };

    userService.getAccount.mockResolvedValue(mockAccountData);

    wrapper = await createWrapper();
    await flushPromises();

    // 日付がフォーマットされていることを確認
    const content = wrapper.text();
    expect(content).toContain('2024');
  });

  it('handles logout successfully', async () => {
    userService.getAccount.mockResolvedValue({
      user: {
        id: '05c66ceb-6ddc-4ada-b736-08702615ff48',
        fullName: '田中太郎',
        username: 'tanaka_taro',
        email: 'tanaka@example.com',
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-02T00:00:00Z'
      }
    });

    authService.logout.mockResolvedValue();

    wrapper = await createWrapper();
    await flushPromises();

    const logoutButton = wrapper.find('button');
    await logoutButton.trigger('click');
    await flushPromises();

    expect(authService.logout).toHaveBeenCalled();
  });

  it('redirects to login after logout even if logout fails', async () => {
    userService.getAccount.mockResolvedValue({
      user: {
        id: '05c66ceb-6ddc-4ada-b736-08702615ff48',
        fullName: '田中太郎',
        username: 'tanaka_taro',
        email: 'tanaka@example.com',
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-02T00:00:00Z'
      }
    });

    authService.logout.mockRejectedValue(new Error('Logout failed'));

    wrapper = await createWrapper();
    await flushPromises();

    const logoutButton = wrapper.find('button');
    await logoutButton.trigger('click');
    await flushPromises();

    expect(authService.logout).toHaveBeenCalled();
  });

  it('has a link to points page', async () => {
    userService.getAccount.mockResolvedValue({
      user: {
        id: '05c66ceb-6ddc-4ada-b736-08702615ff48',
        fullName: '田中太郎',
        username: 'tanaka_taro',
        email: 'tanaka@example.com',
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-02T00:00:00Z'
      }
    });

    wrapper = await createWrapper();
    await flushPromises();

    const content = wrapper.html();
    expect(content).toContain('ポイント');
  });

  it('displays account information card', async () => {
    userService.getAccount.mockResolvedValue({
      user: {
        id: '05c66ceb-6ddc-4ada-b736-08702615ff48',
        fullName: '田中太郎',
        username: 'tanaka_taro',
        email: 'tanaka@example.com',
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-02T00:00:00Z'
      }
    });

    wrapper = await createWrapper();
    await flushPromises();

    expect(wrapper.text()).toContain('アカウント情報');
    expect(wrapper.text()).toContain('ユーザーID');
    expect(wrapper.text()).toContain('氏名');
    expect(wrapper.text()).toContain('ユーザー名');
    expect(wrapper.text()).toContain('メールアドレス');
    expect(wrapper.text()).toContain('登録日時');
    expect(wrapper.text()).toContain('更新日時');
  });
});
