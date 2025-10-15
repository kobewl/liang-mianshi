import { createRouter, createWebHistory } from 'vue-router'
import { useStore } from 'vuex'
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
  } else if (to.meta.requiresAuth && to.meta.requiresAdmin) {
    // 如果页面需要管理员权限，检查用户角色
    try {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        const user = JSON.parse(userStr)
        if (user.userRole === 'admin') {
          next()
        } else {
          // 普通用户尝试访问管理员页面，重定向到首页
          next('/')
        }
      } else {
        next('/login')
      }
    } catch (error) {
      console.error('解析用户信息失败:', error)
      next('/login')
    }
  } else {
    next()
  }
})

export default router