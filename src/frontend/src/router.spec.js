import { createRouter, createMemoryHistory } from 'vue-router';
import { router } from './router';

// localStorage をモック
const localStorageMock = (() => {
  let store = {};
  return {
    getItem: jest.fn((key) => store[key] || null),
    setItem: jest.fn((key, value) => {
      store[key] = value.toString();
    }),
    removeItem: jest.fn((key) => {
      delete store[key];
    }),
    clear: jest.fn(() => {
      store = {};
    })
  };
})();

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock
});

describe('router.js', () => {
  beforeEach(() => {
    localStorageMock.clear();
    jest.clearAllMocks();
  });

  it('should have correct routes defined', () => {
    const routes = router.getRoutes();
    
    expect(routes).toHaveLength(5);
    
    const routePaths = routes.map(r => r.path);
    expect(routePaths).toContain('/');
    expect(routePaths).toContain('/login');
    expect(routePaths).toContain('/account');
    expect(routePaths).toContain('/points');
    expect(routePaths).toContain('/points/history');
  });

  it('should redirect from / to /login', () => {
    const rootRoute = router.getRoutes().find(r => r.path === '/');
    expect(rootRoute.redirect).toBe('/login');
  });

  it('should have correct route names', () => {
    const routes = router.getRoutes();
    
    const loginRoute = routes.find(r => r.path === '/login');
    expect(loginRoute.name).toBe('Login');
    
    const accountRoute = routes.find(r => r.path === '/account');
    expect(accountRoute.name).toBe('Account');
    
    const pointsRoute = routes.find(r => r.path === '/points');
    expect(pointsRoute.name).toBe('Points');
    
    const historyRoute = routes.find(r => r.path === '/points/history');
    expect(historyRoute.name).toBe('PointHistory');
  });

  it('should have requiresAuth meta for protected routes', () => {
    const routes = router.getRoutes();
    
    const accountRoute = routes.find(r => r.path === '/account');
    expect(accountRoute.meta.requiresAuth).toBe(true);
    
    const pointsRoute = routes.find(r => r.path === '/points');
    expect(pointsRoute.meta.requiresAuth).toBe(true);
    
    const historyRoute = routes.find(r => r.path === '/points/history');
    expect(historyRoute.meta.requiresAuth).toBe(true);
  });

  it('should not have requiresAuth meta for login route', () => {
    const routes = router.getRoutes();
    const loginRoute = routes.find(r => r.path === '/login');
    expect(loginRoute.meta.requiresAuth).toBeUndefined();
  });

  describe('Navigation Guards', () => {
    let testRouter;

    beforeEach(async () => {
      // テスト用のルーターを作成（メモリ履歴を使用）
      testRouter = createRouter({
        history: createMemoryHistory(),
        routes: router.options.routes
      });

      // ナビゲーションガードを適用
      testRouter.beforeEach((to, from, next) => {
        const token = localStorage.getItem('authToken');
        
        if (to.meta.requiresAuth && !token) {
          next('/login');
        } else {
          next();
        }
      });

      await testRouter.push('/');
      await testRouter.isReady();
    }, 10000);

    it('should allow access to login page without token', async () => {
      localStorageMock.getItem.mockReturnValue(null);

      await testRouter.push('/login');
      expect(testRouter.currentRoute.value.path).toBe('/login');
    });

    it('should redirect to login when accessing protected route without token', async () => {
      localStorageMock.getItem.mockReturnValue(null);

      await testRouter.push('/account');
      expect(testRouter.currentRoute.value.path).toBe('/login');
    });

    it('should allow access to protected route with token', async () => {
      localStorageMock.getItem.mockReturnValue('mock-token');

      await testRouter.push('/account');
      expect(testRouter.currentRoute.value.path).toBe('/account');
    });

    it('should allow access to points page with token', async () => {
      localStorageMock.getItem.mockReturnValue('mock-token');

      await testRouter.push('/points');
      expect(testRouter.currentRoute.value.path).toBe('/points');
    });

    it('should allow access to point history page with token', async () => {
      localStorageMock.getItem.mockReturnValue('mock-token');

      await testRouter.push('/points/history');
      expect(testRouter.currentRoute.value.path).toBe('/points/history');
    });
  });
});
