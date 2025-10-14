import { createStore } from 'vuex';
import { getUserInfo } from '../api';

export default createStore({
  state: {
    user: null,
    token: localStorage.getItem('token') || '',
    isLoggedIn: !!localStorage.getItem('token')
  },

  getters: {
    user: state => state.user,
    token: state => state.token,
    isLoggedIn: state => state.isLoggedIn
  },

  mutations: {
    SET_TOKEN(state, token) {
      state.token = token;
      state.isLoggedIn = !!token;
      if (token) {
        localStorage.setItem('token', token);
      } else {
        localStorage.removeItem('token');
      }
    },

    SET_USER(state, user) {
      state.user = user;
      if (user) {
        localStorage.setItem('userInfo', JSON.stringify(user));
      } else {
        localStorage.removeItem('userInfo');
      }
    },

    CLEAR_AUTH(state) {
      state.token = '';
      state.user = null;
      state.isLoggedIn = false;
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
    }
  },

  actions: {
    // 登录
    login({ commit }, { token, user }) {
      commit('SET_TOKEN', token);
      commit('SET_USER', user);
    },

    // 登出
    logout({ commit }) {
      commit('CLEAR_AUTH');
    },

    // 获取用户信息
    async fetchUserInfo({ commit }) {
      try {
        const response = await getUserInfo();
        if (response.code === 200) {
          commit('SET_USER', response.data);
          return response.data;
        }
        return null;
      } catch (error) {
        console.error('获取用户信息失败:', error);
        return null;
      }
    },

    // 初始化用户信息（从本地存储恢复）
    initUserInfo({ commit }) {
      const userInfo = localStorage.getItem('userInfo');
      if (userInfo) {
        try {
          const user = JSON.parse(userInfo);
          commit('SET_USER', user);
        } catch (error) {
          console.error('解析用户信息失败:', error);
          localStorage.removeItem('userInfo');
        }
      }
    }
  }
});