import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import UserManage from '../views/UserManage.vue'
import QuestionManage from '../views/QuestionManage.vue'
import QuestionEdit from '../views/QuestionEdit.vue'
import QuestionBankManage from '../views/QuestionBankManage.vue'
import QuestionBankEdit from '../views/QuestionBankEdit.vue'
import QuestionDetail from '../views/QuestionDetail.vue'
import PersonalCenter from '../views/PersonalCenter.vue'
import store from '../store'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/users',
    name: 'UserManage',
    component: UserManage,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/questions',
    name: 'QuestionManage',
    component: QuestionManage,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/questions/create',
    name: 'QuestionCreate',
    component: QuestionEdit,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/questions/edit/:id',
    name: 'QuestionEdit',
    component: QuestionEdit,
    meta: { requiresAuth: true, requiresAdmin: true },
    props: true
  },
  {
    path: '/question-banks',
    name: 'QuestionBankManage',
    component: QuestionBankManage,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/question-banks/create',
    name: 'QuestionBankCreate',
    component: QuestionBankEdit,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/question-banks/edit/:id',
    name: 'QuestionBankEdit',
    component: QuestionBankEdit,
    meta: { requiresAuth: true, requiresAdmin: true },
    props: true
  },
  {
    path: '/question-bank/:id',
    name: 'QuestionBankDetail',
    component: QuestionBankManage,
    meta: { requiresAuth: true },
    props: true
  },
  {
    path: '/question/:id',
    name: 'QuestionDetail',
    component: QuestionDetail,
    meta: { requiresAuth: true },
    props: true
  },
  {
    path: '/personal-center',
    name: 'PersonalCenter',
    component: PersonalCenter,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const ensureUser = () => {
  const stateUser = store.state.user
  if (stateUser && stateUser.userRole) {
    return stateUser
  }

  const cached = localStorage.getItem('userInfo')
  if (cached) {
    try {
      const parsed = JSON.parse(cached)
      store.commit('SET_USER', parsed)
      return parsed
    } catch (error) {
      console.error('恢复用户信息失败:', error)
    }
  }
  return null
}

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = store.state.token || localStorage.getItem('token')
  const isLoggedIn = !!token

  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login')
    return
  }

  if (to.path === '/login' && isLoggedIn) {
    next('/')
    return
  }

  if (to.meta.requiresAuth && to.meta.requiresAdmin) {
    const user = ensureUser()
    const role = user?.userRole?.toLowerCase()
    if (role === 'admin') {
      next()
    } else if (isLoggedIn) {
      next('/')
    } else {
      next('/login')
    }
    return
  }

  next()
})

export default router
