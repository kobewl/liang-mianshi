<template>
  <div class="login-container">
    <div class="login-header">
      <div class="logo">
        <span class="logo-icon">🦆</span>
        <span class="logo-text">面试鸭刷题神器</span>
      </div>
      <div class="header-actions">
        <a-button type="link" @click="$router.push('/')">主页</a-button>
        <a-button type="link" @click="$router.push('/register')">注册账号</a-button>
      </div>
    </div>
    
    <div class="login-content">
      <div class="login-wrapper">
        <div class="login-card">
          <h2 class="login-title">用户登录</h2>
          <p class="login-subtitle">登录后畅享海量面试题库</p>
          
          <a-form
            :model="loginForm"
            :rules="rules"
            ref="loginFormRef"
            @finish="handleLogin"
            class="login-form"
          >
            <a-form-item name="userAccount">
              <a-input 
                v-model:value="loginForm.userAccount" 
                placeholder="请输入用户账号"
                size="large"
                class="custom-input"
              >
                <template #prefix>
                  <span class="input-icon">👤</span>
                </template>
              </a-input>
            </a-form-item>
            
            <a-form-item name="userPassword">
              <a-input-password 
                v-model:value="loginForm.userPassword" 
                placeholder="请输入密码"
                size="large"
                class="custom-input"
              >
                <template #prefix>
                  <span class="input-icon">🔒</span>
                </template>
              </a-input-password>
            </a-form-item>
            
            <a-form-item>
              <a-button 
                type="primary" 
                html-type="submit" 
                :loading="loading" 
                block 
                size="large"
                class="login-btn"
              >
                立即登录
              </a-button>
            </a-form-item>
            
            <div class="form-footer">
              <span>还没有账号？</span>
              <a-button type="link" @click="$router.push('/register')" class="register-link">
                立即注册
              </a-button>
            </div>
          </a-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue';
import { message } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { login } from '../api';

export default {
  name: 'Login',
  setup() {
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
        { min: 4, max: 20, message: '账号长度在 4 到 20 个字符', trigger: 'blur' }
      ],
      userPassword: [
        { required: true, message: '请输入用户密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
      ]
    };

    const handleLogin = async () => {
      loading.value = true;
      try {
        const response = await login(loginForm);
        
        if (response.code === 200) {
          // 登录成功
          const { token, ...userInfo } = response.data;
          
          // 使用Vuex store管理登录状态
          store.dispatch('login', { token, user: userInfo });
          
          message.success('登录成功！');
          // 跳转到主页
          router.push('/');
        } else {
          // 登录失败，显示错误信息
          message.error(response.message || '登录失败，请检查账号和密码');
        }
      } catch (error) {
        console.error('登录错误:', error);
        const errorMsg = error.response?.data?.message || '登录失败，请检查网络连接';
        message.error(errorMsg);
      } finally {
        loading.value = false;
      }
    };

    return {
      loginForm,
      rules,
      loginFormRef,
      loading,
      handleLogin
    };
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #FFF5E6 0%, #FFE8CC 100%);
  overflow: hidden;
}

.login-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 48px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  background: linear-gradient(135deg, #FF9A3D 0%, #FF6B35 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-actions {
  display: flex;
  gap: 16px;
}

.header-actions :deep(.ant-btn-link) {
  color: #666;
  font-size: 15px;
}

.header-actions :deep(.ant-btn-link:hover) {
  color: #FF9A3D;
}

.login-content {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 68px);
  padding: 40px 20px;
}

.login-wrapper {
  width: 100%;
  max-width: 450px;
}

.login-card {
  background: white;
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 8px 32px rgba(255, 154, 61, 0.15);
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  text-align: center;
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 14px;
  color: #999;
  text-align: center;
  margin-bottom: 32px;
}

.login-form {
  margin-top: 24px;
}

.custom-input {
  border-radius: 8px;
  border: 2px solid #f0f0f0;
  transition: all 0.3s;
}

.custom-input:hover,
.custom-input:focus {
  border-color: #FF9A3D;
}

.custom-input :deep(.ant-input) {
  font-size: 15px;
}

.input-icon {
  font-size: 18px;
  margin-right: 4px;
}

.login-btn {
  height: 48px;
  border-radius: 8px;
  background: linear-gradient(135deg, #FF9A3D 0%, #FF6B35 100%);
  border: none;
  font-size: 16px;
  font-weight: 600;
  margin-top: 8px;
  box-shadow: 0 4px 12px rgba(255, 154, 61, 0.3);
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 154, 61, 0.4);
}

.form-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #666;
}

.register-link {
  color: #FF9A3D;
  font-weight: 500;
  padding: 0 4px;
}

.register-link:hover {
  color: #FF6B35;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-header {
    padding: 12px 20px;
  }
  
  .logo {
    font-size: 18px;
  }
  
  .logo-icon {
    font-size: 24px;
  }
  
  .login-card {
    padding: 36px 24px;
  }
  
  .login-title {
    font-size: 24px;
  }
}
</style>