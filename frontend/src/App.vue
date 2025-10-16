<template>
  <a-config-provider :locale="zhCN">
    <div id="app-root">
      <router-view />
    </div>
  </a-config-provider>
</template>

<script setup>
import { onMounted } from 'vue';
import { useStore } from 'vuex';
import zhCN from 'ant-design-vue/es/locale/zh_CN';
import 'ant-design-vue/dist/reset.css';

const store = useStore();

onMounted(() => {
  const token = localStorage.getItem('token');
  if (token) {
    store.commit('SET_TOKEN', token);
    store.dispatch('initUserInfo');
    store.dispatch('fetchUserInfo');
  }
});
</script>

<style scoped>
#app-root {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
</style>
