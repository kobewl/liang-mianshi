<template>
  <PageLayout
    :nav-items="navItems"
    :show-footer="false"
    :header-pinned="true"
  >
    <section class="auth-layout">
      <div class="auth-showcase glass-card">
        <div class="showcase-badge">全新体验</div>
        <h1>欢迎回到面试鸭刷题神</h1>
        <p>全新的现代化界面、沉浸式 Markdown 编辑器与数据管理能力，只为让你的备战更轻松。</p>
        <ul>
          <li>题目、题库、用户统一管理，所见即所得。</li>
          <li>智能 Markdown 编辑，支持实时预览与快捷插入。</li>
          <li>云端同步，随时随地继续你的学习旅程。</li>
        </ul>
      </div>

      <div class="auth-card glass-card">
        <div class="auth-header">
          <h2>账号登录</h2>
          <p>使用你的账号密码登录，继续你的刷题计划。</p>
        </div>
        <a-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          layout="vertical"
          @finish="handleLogin"
        >
          <a-form-item name="userAccount" label="账号">
            <a-input
              v-model:value="loginForm.userAccount"
              size="large"
              placeholder="请输入账号（支持字母与数字组合）"
              allow-clear
            />
          </a-form-item>

          <a-form-item name="userPassword" label="密码">
            <a-input-password
              v-model:value="loginForm.userPassword"
              size="large"
              placeholder="请输入密码"
              allow-clear
            />
          </a-form-item>

          <div class="auth-actions">
            <a-button
              type="primary"
              shape="round"
              size="large"
              html-type="submit"
              block
              :loading="loading"
            >
              立即登录
            </a-button>
          </div>
        </a-form>

        <div class="auth-footer">
          <span>还没有账号？</span>
          <a-button type="link" @click="router.push('/register')">立即注册</a-button>
        </div>
      </div>
    </section>
  </PageLayout>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { message } from 'ant-design-vue';
import PageLayout from '../components/layout/PageLayout.vue';
import { login } from '../api';

const router = useRouter();
const store = useStore();

const loginFormRef = ref();
const loading = ref(false);

const loginForm = reactive({
  userAccount: '',
  userPassword: ''
});

const rules = {
  userAccount: [
    { required: true, message: '请输入用户账号', trigger: 'blur' },
    { min: 4, max: 20, message: '账号长度需在 4 到 20 个字符之间', trigger: 'blur' }
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在 6 到 20 个字符之间', trigger: 'blur' }
  ]
};

const navItems = [
  { key: 'home', label: '返回首页', path: '/' }
];

const handleLogin = async () => {
  loading.value = true;
  try {
    const response = await login(loginForm);
    if (response.code === 200) {
      const { token, ...userInfo } = response.data;
      store.dispatch('login', { token, user: userInfo });
      message.success('登录成功，欢迎回来！');
      const role = (userInfo?.userRole || '').toLowerCase();
      router.push(role === 'admin' ? '/admin' : '/');
    } else {
      message.error(response.message || '登录失败，请检查账号和密码');
    }
  } catch (error) {
    console.error('登录错误:', error);
    const errorMsg = error?.response?.data?.message || '登录失败，请稍后重试';
    message.error(errorMsg);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.auth-layout {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 32px;
  align-items: stretch;
  margin-top: 24px;
}

.auth-showcase {
  padding: 40px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.showcase-badge {
  align-self: flex-start;
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.6px;
  background: rgba(59, 130, 246, 0.18);
  color: #2563eb;
}

.auth-showcase h1 {
  font-size: clamp(28px, 4vw, 34px);
  line-height: 1.2;
  font-weight: 700;
}

.auth-showcase p {
  font-size: 15px;
  color: var(--text-secondary);
}

.auth-showcase ul {
  margin-top: 12px;
  padding-left: 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: var(--text-secondary);
}

.auth-card {
  padding: 38px 36px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.auth-header h2 {
  font-size: 26px;
  font-weight: 700;
}

.auth-header p {
  margin-top: 8px;
  color: var(--text-secondary);
}

.auth-actions {
  margin-top: 8px;
}

.auth-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--text-secondary);
}

@media (max-width: 768px) {
  .auth-card {
    padding: 28px 24px;
  }
}
</style>
