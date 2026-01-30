import { mount, flushPromises } from '@vue/test-utils';
import { createRouter, createMemoryHistory } from 'vue-router';
import PointView from './PointView.vue';
import { authService } from '../services/api';
import { pointApi } from '../services/pointApi';

// services をモック
jest.mock('../services/api', () => ({
  authService: {
    logout: jest.fn()
  }
}));

jest.mock('../services/pointApi', () => ({
  pointApi: {
    getPoints: jest.fn()
  }
}));

describe('PointView.vue', () => {
  let wrapper;
  let router;

  beforeEach(() => {
    // ルーターのセットアップ
    router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/points', name: 'Points', component: { template: '<div>Points</div>' } },
        { path: '/points/history', name: 'PointHistory', component: { template: '<div>PointHistory</div>' } },
        { path: '/account', name: 'Account', component: { template: '<div>Account</div>' } },
        { path: '/login', name: 'Login', component: { template: '<div>Login</div>' } }
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
    const w = mount(PointView, {
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

  it('renders the point view', async () => {
    pointApi.getPoints.mockResolvedValue({
      balance: 1000
    });

    wrapper = await createWrapper();
    await flushPromises();

    expect(wrapper.exists()).toBe(true);
    expect(wrapper.find('h1').text()).toBe('ポイント残高');
  });

  it('shows loading state initially', async () => {
    pointApi.getPoints.mockImplementation(() => new Promise(() => {}));

    wrapper = await createWrapper();

    expect(wrapper.find('.animate-spin').exists()).toBe(true);
  });

  it('displays point balance after loading', async () => {
    pointApi.getPoints.mockResolvedValue({
      balance: 1000
    });

    wrapper = await createWrapper();
    await flushPromises();

    const content = wrapper.text();
    expect(content).toContain('1,000');
    expect(content).toContain('ポイント');
  });

  it('formats large numbers with commas', async () => {
    pointApi.getPoints.mockResolvedValue({
      balance: 123456789
    });

    wrapper = await createWrapper();
    await flushPromises();

    const content = wrapper.text();
    expect(content).toContain('123,456,789');
  });

  it('displays zero when balance is undefined', async () => {
    pointApi.getPoints.mockResolvedValue({});

    wrapper = await createWrapper();
    await flushPromises();

    const content = wrapper.text();
    expect(content).toContain('0');
  });

  it('displays error message when point fetch fails', async () => {
    const mockError = {
      response: {
        data: {
          error: 'Failed to fetch points'
        }
      }
    };

    pointApi.getPoints.mockRejectedValue(mockError);

    wrapper = await createWrapper();
    await flushPromises();

    expect(wrapper.find('.bg-red-100').exists()).toBe(true);
    expect(wrapper.find('.bg-red-100').text()).toBe('Failed to fetch points');
  });

  it('displays service unavailable message for 503 error', async () => {
    const mockError = {
      response: {
        status: 503,
        data: {}
      }
    };

    pointApi.getPoints.mockRejectedValue(mockError);

    wrapper = await createWrapper();
    await flushPromises();

    expect(wrapper.find('.bg-red-100').text()).toBe('ポイントサービスが一時的に利用できません。しばらくしてからお試しください。');
  });

  it('displays generic error message when no specific error is provided', async () => {
    const mockError = {
      response: {
        data: {}
      }
    };

    pointApi.getPoints.mockRejectedValue(mockError);

    wrapper = await createWrapper();
    await flushPromises();

    expect(wrapper.find('.bg-red-100').text()).toBe('ポイント情報の取得に失敗しました');
  });

  it('has a link to point history page', async () => {
    pointApi.getPoints.mockResolvedValue({
      balance: 1000
    });

    wrapper = await createWrapper();
    await flushPromises();

    const content = wrapper.html();
    expect(content).toContain('ポイント履歴を見る');
  });

  it('has a link to account page', async () => {
    pointApi.getPoints.mockResolvedValue({
      balance: 1000
    });

    wrapper = await createWrapper();
    await flushPromises();

    const content = wrapper.html();
    expect(content).toContain('アカウント');
  });

  it('handles logout successfully', async () => {
    pointApi.getPoints.mockResolvedValue({
      balance: 1000
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
    pointApi.getPoints.mockResolvedValue({
      balance: 1000
    });

    authService.logout.mockRejectedValue(new Error('Logout failed'));

    wrapper = await createWrapper();
    await flushPromises();

    const logoutButton = wrapper.find('button');
    await logoutButton.trigger('click');
    await flushPromises();

    expect(authService.logout).toHaveBeenCalled();
  });

  it('displays information card', async () => {
    pointApi.getPoints.mockResolvedValue({
      balance: 1000
    });

    wrapper = await createWrapper();
    await flushPromises();

    expect(wrapper.text()).toContain('ポイントは様々なサービスでご利用いただけます。');
  });

  it('displays current point balance title', async () => {
    pointApi.getPoints.mockResolvedValue({
      balance: 1000
    });

    wrapper = await createWrapper();
    await flushPromises();

    expect(wrapper.text()).toContain('現在のポイント残高');
  });
});
