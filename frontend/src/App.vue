<template>
  <a-config-provider :locale="zhCN">
    <div id="app">
      <router-view />
    </div>
  </a-config-provider>
</template>

<script>
import { defineComponent, onMounted } from 'vue';
import { useStore } from 'vuex';
import zhCN from 'ant-design-vue/es/locale/zh_CN';
import 'ant-design-vue/dist/reset.css';

export default defineComponent({
  name: 'App',
  setup() {
    const store = useStore();
    
    // 应用启动时初始化用户信息
    onMounted(() => {
      // 检查本地存储的token
      const token = localStorage.getItem('token');
      if (token) {
        store.commit('SET_TOKEN', token);
        // 先尝试从本地存储恢复用户信息
        store.dispatch('initUserInfo');
        // 然后获取最新的用户信息
        store.dispatch('fetchUserInfo');
      }
    });

    return {
      zhCN
    };
  }
});
</script>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  width: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Noto Sans', sans-serif;
  font-size: 14px;
  line-height: 1.5715;
  color: rgba(0, 0, 0, 0.85);
  background: #FFF5E6;
  overflow-x: hidden;
}

#app {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

/* 响应式设计 */
@media (max-width: 768px) {
  html, body {
    font-size: 13px;
  }
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 动画效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 卡片阴影效果 */
.shadow-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  transition: all 0.3s;
}

.shadow-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

/* 按钮样式增强 */
.btn-primary {
  background: linear-gradient(90deg, #1890ff, #36cfc9);
  border: none;
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.3);
  transition: all 0.3s;
}

.btn-primary:hover {
  background: linear-gradient(90deg, #40a9ff, #5cdbd3);
  box-shadow: 0 4px 8px rgba(24, 144, 255, 0.4);
  transform: translateY(-2px);
}

/* Ant Design全局覆盖 */
.ant-layout {
  background: transparent !important;
}

.ant-layout-header {
  background: white !important;
  padding: 0 !important;
  margin: 0 !important;
}

.ant-layout-content {
  background: transparent !important;
  padding: 0 !important;
  margin: 0 !important;
}

.ant-layout-footer {
  background: #333 !important;
  margin: 0 !important;
  padding: 24px !important;
}
</style>
