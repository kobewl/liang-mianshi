<template>
  <div class="register-container">
    <div class="register-header">
      <div class="logo">
        <span class="logo-icon">🦆</span>
        <span class="logo-text">面试鸭刷题神器</span>
      </div>
      <div class="header-actions">
        <a-button type="link" @click="$router.push('/')">主页</a-button>
        <a-button type="link" @click="$router.push('/login')">登录账号</a-button>
      </div>
    </div>
    
    <div class="register-content">
      <div class="register-wrapper">
        <div class="register-card">
          <h2 class="register-title">用户注册</h2>
          <p class="register-subtitle">加入我们，开启刷题之旅</p>
          <a-form
            :model="registerForm"
            :rules="rules"
            ref="registerFormRef"
            @finish="handleRegister"
            class="register-form"
          >
            <a-form-item name="userAccount">
              <a-input 
                v-model:value="registerForm.userAccount" 
                placeholder="请输入用户账号"
                size="large"
                class="custom-input"
              >
                <template #prefix>
                  <span class="input-icon">👤</span>
                </template>
              </a-input>
            </a-form-item>
            
            <a-form-item name="userName">
              <a-input 
                v-model:value="registerForm.userName" 
                placeholder="请输入用户昵称"
                size="large"
                class="custom-input"
              >
                <template #prefix>
                  <span class="input-icon">😊</span>
                </template>
              </a-input>
            </a-form-item>
            
            <a-form-item name="userPassword">
              <a-input-password 
                v-model:value="registerForm.userPassword" 
                placeholder="请输入密码"
                size="large"
                class="custom-input"
              >
                <template #prefix>
                  <span class="input-icon">🔒</span>
                </template>
              </a-input-password>
            </a-form-item>
            
            <a-form-item name="checkPassword">
              <a-input-password 
                v-model:value="registerForm.checkPassword" 
                placeholder="请确认密码"
                size="large"
                class="custom-input"
              >
                <template #prefix>
                  <span class="input-icon">✅</span>
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
                class="register-btn"
              >
                立即注册
              </a-button>
            </a-form-item>
            
            <div class="form-footer">
              <span>已有账号？</span>
              <a-button type="link" @click="$router.push('/login')" class="login-link">
                立即登录
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
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { register } from '../api';

export default {
  name: 'Register',
  setup() {
    const router = useRouter();
    const registerFormRef = ref();
    const loading = ref(false);
    
    const registerForm = reactive({
      userAccount: '',
      userPassword: '',
      checkPassword: '',
      userName: ''
    });

    const validatePass = async (rule, value) => {
      if (value === '') {
        return Promise.reject('请再次输入密码');
      } else if (value !== registerForm.userPassword) {
        return Promise.reject('两次输入的密码不一致');
      } else {
        return Promise.resolve();
      }
    };

    const rules = {
      userAccount: [
        { required: true, message: '请输入用户账号', trigger: 'blur' },
        { min: 4, max: 20, message: '账号长度在 4 到 20 个字符', trigger: 'blur' }
      ],
      userPassword: [
        { required: true, message: '请输入用户密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
      ],
      checkPassword: [
        { required: true, validator: validatePass, trigger: 'blur' }
      ],
      userName: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
      ]
    };

    const handleRegister = async () => {
      loading.value = true;
      try {
        const { checkPassword, ...registerData } = registerForm;
        const response = await register(registerData);
        
        if (response.code === 200) {
          message.success('注册成功！');
          router.push('/login');
        } else {
          // 注册失败，显示错误信息
          message.error(response.message || '注册失败，请重试');
        }
      } catch (error) {
        console.error('注册错误:', error);
        const errorMsg = error.response?.data?.message || '注册失败，请检查网络连接';
        message.error(errorMsg);
      } finally {
        loading.value = false;
      }
    };

    return {
      registerForm,
      rules,
      registerFormRef,
      loading,
      handleRegister
    };
  }
}
</script>

<style scoped>
.register-container {
  height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #FFF5E6 0%, #FFE8CC 100%);
  overflow: hidden;
}

.register-header {
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

.register-content {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 68px);
  padding: 40px 20px;
}

.register-wrapper {
  width: 100%;
  max-width: 500px;
}

.register-card {
  background: white;
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 8px 32px rgba(255, 154, 61, 0.15);
}

.register-title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  text-align: center;
  margin-bottom: 8px;
}

.register-subtitle {
  font-size: 14px;
  color: #999;
  text-align: center;
  margin-bottom: 32px;
}

.register-form {
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

.register-btn {
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

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 154, 61, 0.4);
}

.form-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #666;
}

.login-link {
  color: #FF9A3D;
  font-weight: 500;
  padding: 0 4px;
}

.login-link:hover {
  color: #FF6B35;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-header {
    padding: 12px 20px;
  }
  
  .logo {
    font-size: 18px;
  }
  
  .logo-icon {
    font-size: 24px;
  }
  
  .register-card {
    padding: 36px 24px;
  }
  
  .register-title {
    font-size: 24px;
  }
}
</style>