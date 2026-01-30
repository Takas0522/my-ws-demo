import { mount } from '@vue/test-utils';
import { createRouter, createMemoryHistory } from 'vue-router';
import LoginView from './LoginView.vue';
import { authService } from '../services/api';

// authService をモック
jest.mock('../services/api', () => ({
  authService: {
    login: jest.fn()
  }
}));

describe('LoginView.vue', () => {
  let wrapper;
  let router;

  beforeEach(() => {
    // ルーターのセットアップ
    router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/login', name: 'Login', component: { template: '<div>Login</div>' } },
        { path: '/account', name: 'Account', component: { template: '<div>Account</div>' } }
      ]
    });

    // localStorage をモック
    Storage.prototype.getItem = jest.fn();
    Storage.prototype.setItem = jest.fn();
    Storage.prototype.removeItem = jest.fn();
  });

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount();
    }
    jest.clearAllMocks();
  });

  const createWrapper = () => {
    return mount(LoginView, {
      global: {
        plugins: [router]
      }
    });
  };

  it('renders the login form', () => {
    wrapper = createWrapper();
    expect(wrapper.exists()).toBe(true);
    expect(wrapper.find('h1').text()).toBe('ログイン');
  });

  it('has userId and password input fields', () => {
    wrapper = createWrapper();
    
    const userIdInput = wrapper.find('#userId');
    const passwordInput = wrapper.find('#password');
    
    expect(userIdInput.exists()).toBe(true);
    expect(passwordInput.exists()).toBe(true);
  });

  it('has a submit button', () => {
    wrapper = createWrapper();
    
    const submitButton = wrapper.find('button[type="submit"]');
    expect(submitButton.exists()).toBe(true);
    expect(submitButton.text()).toBe('ログイン');
  });

  it('updates userId and password when user types', async () => {
    wrapper = createWrapper();
    
    const userIdInput = wrapper.find('#userId');
    const passwordInput = wrapper.find('#password');
    
    await userIdInput.setValue('tanaka_taro');
    await passwordInput.setValue('password123');
    
    expect(userIdInput.element.value).toBe('tanaka_taro');
    expect(passwordInput.element.value).toBe('password123');
  });

  it('calls handleLogin when form is submitted', async () => {
    wrapper = createWrapper();
    
    const mockResponse = {
      token: 'mock-token',
      userId: '05c66ceb-6ddc-4ada-b736-08702615ff48'
    };
    
    authService.login.mockResolvedValue(mockResponse);
    
    await wrapper.find('#userId').setValue('tanaka_taro');
    await wrapper.find('#password').setValue('password123');
    await wrapper.find('form').trigger('submit.prevent');
    
    // Vue の更新を待つ
    await wrapper.vm.$nextTick();
    
    expect(authService.login).toHaveBeenCalledWith('tanaka_taro', 'password123');
  });

  it('shows loading state during login', async () => {
    wrapper = createWrapper();
    
    // ログインを保留状態にする
    authService.login.mockImplementation(() => new Promise(() => {}));
    
    await wrapper.find('#userId').setValue('tanaka_taro');
    await wrapper.find('#password').setValue('password123');
    await wrapper.find('form').trigger('submit.prevent');
    
    await wrapper.vm.$nextTick();
    
    const submitButton = wrapper.find('button[type="submit"]');
    expect(submitButton.text()).toBe('ログイン中...');
    expect(submitButton.element.disabled).toBe(true);
  });

  it('saves token and userId to localStorage on successful login', async () => {
    wrapper = createWrapper();
    
    const mockResponse = {
      token: 'mock-token',
      userId: '05c66ceb-6ddc-4ada-b736-08702615ff48'
    };
    
    authService.login.mockResolvedValue(mockResponse);
    
    await wrapper.find('#userId').setValue('tanaka_taro');
    await wrapper.find('#password').setValue('password123');
    await wrapper.find('form').trigger('submit.prevent');
    
    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    
    expect(localStorage.setItem).toHaveBeenCalledWith('authToken', 'mock-token');
    expect(localStorage.setItem).toHaveBeenCalledWith('userId', '05c66ceb-6ddc-4ada-b736-08702615ff48');
  });

  it('displays error message on login failure', async () => {
    wrapper = createWrapper();
    
    const mockError = {
      response: {
        status: 401,
        data: {
          error: 'Invalid credentials'
        }
      }
    };
    
    authService.login.mockRejectedValue(mockError);
    
    await wrapper.find('#userId').setValue('wrong_user');
    await wrapper.find('#password').setValue('wrong_pass');
    await wrapper.find('form').trigger('submit.prevent');
    
    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    
    expect(wrapper.find('.bg-red-100').exists()).toBe(true);
    expect(wrapper.find('.bg-red-100').text()).toBe('Invalid credentials');
  });

  it('displays generic error message when response has no specific error', async () => {
    wrapper = createWrapper();
    
    const mockError = {
      response: {
        status: 500,
        data: {}
      }
    };
    
    authService.login.mockRejectedValue(mockError);
    
    await wrapper.find('#userId').setValue('test_user');
    await wrapper.find('#password').setValue('password');
    await wrapper.find('form').trigger('submit.prevent');
    
    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    
    expect(wrapper.find('.bg-red-100').text()).toContain('ログインに失敗しました');
  });

  it('clears error message before new login attempt', async () => {
    wrapper = createWrapper();
    
    // 最初のログイン試行でエラー
    const mockError = {
      response: {
        status: 401,
        data: { error: 'Invalid credentials' }
      }
    };
    authService.login.mockRejectedValueOnce(mockError);
    
    await wrapper.find('#userId').setValue('wrong_user');
    await wrapper.find('#password').setValue('wrong_pass');
    await wrapper.find('form').trigger('submit.prevent');
    
    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 10));
    
    expect(wrapper.find('.bg-red-100').exists()).toBe(true);
    
    // 2回目のログイン試行
    const mockResponse = {
      token: 'mock-token',
      userId: '05c66ceb-6ddc-4ada-b736-08702615ff48'
    };
    authService.login.mockResolvedValue(mockResponse);
    
    await wrapper.find('#userId').setValue('tanaka_taro');
    await wrapper.find('#password').setValue('password123');
    await wrapper.find('form').trigger('submit.prevent');
    
    await wrapper.vm.$nextTick();
    
    // エラーメッセージがクリアされている
    expect(wrapper.vm.errorMessage).toBe('');
  });

  it('displays test user information', () => {
    wrapper = createWrapper();
    
    const testUserSection = wrapper.find('.bg-gray-50');
    expect(testUserSection.exists()).toBe(true);
    expect(testUserSection.text()).toContain('tanaka_taro');
    expect(testUserSection.text()).toContain('suzuki_hanako');
    expect(testUserSection.text()).toContain('yamada_jiro');
  });
});
