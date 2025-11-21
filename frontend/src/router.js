import { createRouter, createWebHistory } from 'vue-router'
import LoginView from './views/LoginView.vue'
import AccountView from './views/AccountView.vue'
import PointView from './views/PointView.vue'
import PointHistoryView from './views/PointHistoryView.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView
  },
  {
    path: '/account',
    name: 'Account',
    component: AccountView,
    meta: { requiresAuth: true }
  },
  {
    path: '/points',
    name: 'Points',
    component: PointView,
    meta: { requiresAuth: true }
  },
  {
    path: '/points/history',
    name: 'PointHistory',
    component: PointHistoryView,
    meta: { requiresAuth: true }
  }
]

export const router = createRouter({
  history: createWebHistory(),
  routes
})

// ナビゲーションガード
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('authToken')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})
