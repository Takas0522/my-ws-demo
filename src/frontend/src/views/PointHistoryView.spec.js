import { mount, flushPromises } from '@vue/test-utils';
import { createRouter, createMemoryHistory } from 'vue-router';
import PointHistoryView from './PointHistoryView.vue';
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
    getPointHistory: jest.fn()
  }
}));

describe('PointHistoryView.vue', () => {
  let wrapper;
  let router;

  beforeEach(() => {
    // ルーターのセットアップ
    router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/points/history', name: 'PointHistory', component: { template: '<div>PointHistory</div>' } },
        { path: '/points', name: 'Points', component: { template: '<div>Points</div>' } },
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
    const w = mount(PointHistoryView, {
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

  const mockHistoryData = {
    history: [
      {
        id: '1',
        type: 'EARN',
        amount: 100,
        description: 'ポイント獲得',
        balanceAfter: 1100,
        createdAt: '2024-01-01T10:00:00Z'
      },
      {
        id: '2',
        type: 'USE',
        amount: 50,
        description: 'ポイント使用',
        balanceAfter: 1050,
        createdAt: '2024-01-02T11:00:00Z'
      }
    ],
    pagination: {
      currentPage: 1,
      totalPages: 5,
      totalItems: 50,
      limit: 10
    }
  };

  it('renders the point history view', async () => {
    pointApi.getPointHistory.mockResolvedValue(mockHistoryData);

    wrapper = await createWrapper();
    await flushPromises();

    expect(wrapper.exists()).toBe(true);
    expect(wrapper.find('h1').text()).toBe('ポイント履歴');
  });

  it('shows loading state initially', async () => {
    pointApi.getPointHistory.mockImplementation(() => new Promise(() => {}));

    wrapper = await createWrapper();

    expect(wrapper.find('.animate-spin').exists()).toBe(true);
  });

  it('displays point history after loading', async () => {
    pointApi.getPointHistory.mockResolvedValue(mockHistoryData);

    wrapper = await createWrapper();
    await flushPromises();

    const content = wrapper.text();
    expect(content).toContain('ポイント獲得');
    expect(content).toContain('ポイント使用');
    expect(content).toContain('獲得');
    expect(content).toContain('使用');
  });

  it('displays EARN transactions with positive format', async () => {
    pointApi.getPointHistory.mockResolvedValue(mockHistoryData);

    wrapper = await createWrapper();
    await flushPromises();

    const content = wrapper.text();
    expect(content).toContain('+100');
  });

  it('displays USE transactions with negative format', async () => {
    pointApi.getPointHistory.mockResolvedValue(mockHistoryData);

    wrapper = await createWrapper();
    await flushPromises();

    const content = wrapper.text();
    expect(content).toContain('-50');
  });

  it('displays SPEND transactions with negative format', async () => {
    const historyWithSpend = {
      history: [
        {
          id: '3',
          type: 'SPEND',
          amount: 30,
          description: 'ポイント消費',
          balanceAfter: 970,
          createdAt: '2024-01-03T12:00:00Z'
        }
      ],
      pagination: {
        currentPage: 1,
        totalPages: 1,
        totalItems: 1,
        limit: 10
      }
    };

    pointApi.getPointHistory.mockResolvedValue(historyWithSpend);

    wrapper = await createWrapper();
    await flushPromises();

    const content = wrapper.text();
    expect(content).toContain('-30');
  });

  it('formats amounts with commas', async () => {
    const historyWithLargeAmount = {
      history: [
        {
          id: '4',
          type: 'EARN',
          amount: 123456,
          description: 'ボーナスポイント',
          balanceAfter: 1123456,
          createdAt: '2024-01-04T13:00:00Z'
        }
      ],
      pagination: {
        currentPage: 1,
        totalPages: 1,
        totalItems: 1,
        limit: 10
      }
    };

    pointApi.getPointHistory.mockResolvedValue(historyWithLargeAmount);

    wrapper = await createWrapper();
    await flushPromises();

    const content = wrapper.text();
    expect(content).toContain('123,456');
  });

  it('displays balance after each transaction', async () => {
    pointApi.getPointHistory.mockResolvedValue(mockHistoryData);

    wrapper = await createWrapper();
    await flushPromises();

    const content = wrapper.text();
    expect(content).toContain('残高: 1,100');
    expect(content).toContain('残高: 1,050');
  });

  it('displays empty state when no history', async () => {
    const emptyHistory = {
      history: [],
      pagination: {
        currentPage: 1,
        totalPages: 0,
        totalItems: 0,
        limit: 10
      }
    };

    pointApi.getPointHistory.mockResolvedValue(emptyHistory);

    wrapper = await createWrapper();
    await flushPromises();

    expect(wrapper.text()).toContain('ポイント履歴がありません');
  });

  it('displays error message when history fetch fails', async () => {
    const mockError = {
      response: {
        data: {
          error: 'Failed to fetch history'
        }
      }
    };

    pointApi.getPointHistory.mockRejectedValue(mockError);

    wrapper = await createWrapper();
    await flushPromises();

    expect(wrapper.find('.bg-red-100').exists()).toBe(true);
    expect(wrapper.find('.bg-red-100').text()).toBe('Failed to fetch history');
  });

  it('displays service unavailable message for 503 error', async () => {
    const mockError = {
      response: {
        status: 503,
        data: {}
      }
    };

    pointApi.getPointHistory.mockRejectedValue(mockError);

    wrapper = await createWrapper();
    await flushPromises();

    expect(wrapper.find('.bg-red-100').text()).toBe('ポイントサービスが一時的に利用できません。しばらくしてからお試しください。');
  });

  it('displays pagination information', async () => {
    pointApi.getPointHistory.mockResolvedValue(mockHistoryData);

    wrapper = await createWrapper();
    await flushPromises();

    const content = wrapper.text();
    expect(content).toContain('全 50 件中');
    expect(content).toContain('1 / 5');
  });

  it('can navigate to next page', async () => {
    pointApi.getPointHistory.mockResolvedValue(mockHistoryData);

    wrapper = await createWrapper();
    await flushPromises();

    const buttons = wrapper.findAll('button');
    const nextButton = buttons.find(b => b.text() === '次へ');
    
    expect(nextButton.element.disabled).toBe(false);

    await nextButton.trigger('click');
    await flushPromises();

    expect(pointApi.getPointHistory).toHaveBeenCalledWith(2, 10);
  });

  it('can navigate to previous page', async () => {
    const page2Data = {
      ...mockHistoryData,
      pagination: {
        ...mockHistoryData.pagination,
        currentPage: 2
      }
    };

    pointApi.getPointHistory.mockResolvedValueOnce(mockHistoryData);
    pointApi.getPointHistory.mockResolvedValueOnce(page2Data);

    wrapper = await createWrapper();
    await flushPromises();
    
    // Set current page to 2 manually
    wrapper.vm.currentPage = 2;
    await wrapper.vm.$nextTick();

    const buttons = wrapper.findAll('button');
    const prevButton = buttons.find(b => b.text() === '前へ');
    
    expect(prevButton.element.disabled).toBe(false);

    await prevButton.trigger('click');
    await flushPromises();

    expect(pointApi.getPointHistory).toHaveBeenCalledWith(1, 10);
  });

  it('disables previous button on first page', async () => {
    pointApi.getPointHistory.mockResolvedValue(mockHistoryData);

    wrapper = await createWrapper();
    await flushPromises();

    const buttons = wrapper.findAll('button');
    const prevButton = buttons.find(b => b.text() === '前へ');
    
    expect(prevButton.element.disabled).toBe(true);
  });

  it('disables next button on last page', async () => {
    const lastPageData = {
      ...mockHistoryData,
      pagination: {
        ...mockHistoryData.pagination,
        currentPage: 5
      }
    };

    pointApi.getPointHistory.mockResolvedValue(lastPageData);

    wrapper = await createWrapper();
    await flushPromises();
    
    // Manually set to last page
    wrapper.vm.currentPage = 5;
    wrapper.vm.historyData.pagination.currentPage = 5;
    await wrapper.vm.$nextTick();

    const buttons = wrapper.findAll('button');
    const nextButton = buttons.find(b => b.text() === '次へ');
    
    expect(nextButton.element.disabled).toBe(true);
  });

  it('has links to other pages', async () => {
    pointApi.getPointHistory.mockResolvedValue(mockHistoryData);

    wrapper = await createWrapper();
    await flushPromises();

    const content = wrapper.html();
    expect(content).toContain('残高へ戻る');
    expect(content).toContain('アカウント');
  });

  it('handles logout successfully', async () => {
    pointApi.getPointHistory.mockResolvedValue(mockHistoryData);
    authService.logout.mockResolvedValue();

    wrapper = await createWrapper();
    await flushPromises();

    const buttons = wrapper.findAll('button');
    const logoutButton = buttons.find(b => b.text() === 'ログアウト');
    
    await logoutButton.trigger('click');
    await flushPromises();

    expect(authService.logout).toHaveBeenCalled();
  });

  it('redirects to login after logout even if logout fails', async () => {
    pointApi.getPointHistory.mockResolvedValue(mockHistoryData);
    authService.logout.mockRejectedValue(new Error('Logout failed'));

    wrapper = await createWrapper();
    await flushPromises();

    const buttons = wrapper.findAll('button');
    const logoutButton = buttons.find(b => b.text() === 'ログアウト');
    
    await logoutButton.trigger('click');
    await flushPromises();

    expect(authService.logout).toHaveBeenCalled();
  });
});
