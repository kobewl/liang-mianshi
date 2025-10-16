<template>
  <PageLayout
    :nav-items="navItems"
    :show-footer="false"
    :header-pinned="true"
  >
    <section class="auth-layout">
      <div class="auth-card glass-card">
        <div class="auth-header">
          <h2>创建新账号</h2>
          <p>填写基础信息即可完成注册，解锁全部题库与管理能力。</p>
        </div>

        <a-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="rules"
          layout="vertical"
          @finish="handleRegister"
        >
          <a-form-item label="账号" name="userAccount">
            <a-input
              v-model:value="registerForm.userAccount"
              size="large"
              placeholder="请输入账号（4-20 位字母/数字）"
              allow-clear
            />
          </a-form-item>

          <a-form-item label="昵称" name="userName">
            <a-input
              v-model:value="registerForm.userName"
              size="large"
              placeholder="请输入昵称（展示给其他用户）"
              allow-clear
            />
          </a-form-item>

          <a-form-item label="密码" name="userPassword">
            <a-input-password
              v-model:value="registerForm.userPassword"
              size="large"
              placeholder="请输入密码（6-20 位）"
              allow-clear
            />
          </a-form-item>

          <a-form-item label="确认密码" name="checkPassword">
            <a-input-password
              v-model:value="registerForm.checkPassword"
              size="large"
              placeholder="请再次输入密码"
              allow-clear
            />
          </a-form-item>

          <a-button
            type="primary"
            shape="round"
            size="large"
            html-type="submit"
            block
            :loading="loading"
          >
            完成注册
          </a-button>
        </a-form>

        <div class="auth-footer">
          <span>已经有账号？</span>
          <a-button type="link" @click="router.push('/login')">立即登录</a-button>
        </div>
      </div>

      <div class="auth-showcase glass-card">
        <h3>注册后你将获得</h3>
        <div class="showcase-grid">
          <div class="showcase-item" v-for="item in benefits" :key="item.title">
            <div class="icon" :style="{ background: item.background }">{{ item.icon }}</div>
            <div>
              <h4>{{ item.title }}</h4>
              <p>{{ item.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </PageLayout>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import PageLayout from '../components/layout/PageLayout.vue';
import { register } from '../api';

const router = useRouter();

const registerFormRef = ref();
const loading = ref(false);

const registerForm = reactive({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
  userName: ''
});

const validateConfirmPassword = async (_rule, value) => {
  if (!value) {
    return Promise.reject(new Error('请再次输入密码'));
  }
  if (value !== registerForm.userPassword) {
    return Promise.reject(new Error('两次输入的密码不一致'));
  }
  return Promise.resolve();
};

const rules = {
  userAccount: [
    { required: true, message: '请输入用户账号', trigger: 'blur' },
    { min: 4, max: 20, message: '账号长度需在 4 到 20 个字符之间', trigger: 'blur' }
  ],
  userName: [
    { required: true, message: '请输入用户昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度需在 2 到 20 个字符之间', trigger: 'blur' }
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在 6 到 20 个字符之间', trigger: 'blur' }
  ],
  checkPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
};

const navItems = [
  { key: 'home', label: '返回首页', path: '/' },
  { key: 'login', label: '前往登录', path: '/login' }
];

const benefits = [
  {
    title: '个性化题库',
    description: '根据你的学习进度与标签生成高频题单，助你快速查漏补缺。',
    icon: '🧠',
    background: 'linear-gradient(135deg, rgba(59,130,246,0.2), rgba(14,165,233,0.22))'
  },
  {
    title: 'Markdown 编辑器',
    description: '全新富文本 Markdown 编辑，实时预览与快捷命令让整理答案效率翻倍。',
    icon: '✍️',
    background: 'linear-gradient(135deg, rgba(251,191,36,0.22), rgba(253,186,116,0.24))'
  },
  {
    title: '进度追踪',
    description: '自动记录刷题轨迹与签到情况，助你构建长期稳定的学习节奏。',
    icon: '📈',
    background: 'linear-gradient(135deg, rgba(16,185,129,0.2), rgba(5,150,105,0.22))'
  }
];

const handleRegister = async () => {
  loading.value = true;
  try {
    const payload = {
      userAccount: registerForm.userAccount,
      userPassword: registerForm.userPassword,
      userName: registerForm.userName
    };
    const response = await register(payload);
    if (response.code === 200) {
      message.success('注册成功，快去登录吧！');
      router.push('/login');
    } else {
      message.error(response.message || '注册失败，请稍后重试');
    }
  } catch (error) {
    console.error('注册错误:', error);
    const errorMsg = error?.response?.data?.message || '注册失败，请稍后重试';
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
  align-items: start;
  margin-top: 24px;
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

.auth-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--text-secondary);
}

.auth-showcase {
  padding: 40px;
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.showcase-grid {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.showcase-item {
  display: flex;
  gap: 16px;
  padding: 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid rgba(148, 163, 184, 0.18);
}

.showcase-item h4 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 6px;
}

.showcase-item p {
  font-size: 14px;
  color: var(--text-secondary);
}

.showcase-item .icon {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-size: 22px;
}

@media (max-width: 768px) {
  .auth-card {
    padding: 28px 24px;
  }
}
</style>
