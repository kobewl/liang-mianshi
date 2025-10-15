import { createRouter, createWebHistory } from 'vue-router'
import { useStore } from 'vuex'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import UserManage from '../views/UserManage.vue'
import QuestionManage from '../views/QuestionManage.vue'
import QuestionBankManage from '../views/QuestionBankManage.vue'

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
    meta: { requiresAuth: true }
  },
  {
    path: '/questions',
    name: 'QuestionManage',
    component: QuestionManage,
    meta: { requiresAuth: true }
  },
  {
    path: '/question-banks',
    name: 'QuestionBankManage',
    component: QuestionBankManage,
    meta: { requiresAuth: true }
  },
  {
    path: '/question-bank/:id',
    name: 'QuestionBankDetail',
    component: QuestionBankManage,
    meta: { requiresAuth: true },
    props: true
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 检查本地存储中是否有token
  const token = localStorage.getItem('token')
  const isLoggedIn = !!token
  
  // 如果页面需要登录但用户未登录，则重定向到登录页
  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && isLoggedIn) {
    // 如果用户已登录但访问登录页，则重定向到首页
    next('/')
  } else {
    next()
  }
})

export default router