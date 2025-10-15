<template>
  <div class="personal-center-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <span class="logo-icon">🦆</span>
          <span class="logo-text">面试鸭刷题神器</span>
        </div>
        
        <nav class="nav-menu">
          <a-button type="link" @click="$router.push('/')">主页</a-button>
          <a-button type="link" @click="$router.push('/question-banks')">题库</a-button>
          <a-button type="link" class="active">个人中心</a-button>
        </nav>
        
        <div class="header-right">
          <a-dropdown>
            <a-button type="text" class="user-btn">
              <span class="user-icon">👤</span>
              <span>{{ user ? user.userName : '未登录' }}</span>
            </a-button>
            <template #overlay>
              <a-menu>
                <a-menu-item key="1" @click="handleLogout">
                  <span>🚪 退出登录</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <div class="content-wrapper">
        <div class="page-header">
          <h2 class="page-title">个人中心</h2>
        </div>
        
        <div class="profile-container">
          <!-- 用户头像 -->
          <div class="avatar-section">
            <div class="avatar-wrapper">
              <img v-if="form.userAvatar" :src="form.userAvatar" alt="用户头像" class="avatar" />
              <div v-else class="avatar-placeholder">
                <span class="avatar-icon">👤</span>
              </div>
            </div>
            <a-button type="link" @click="showAvatarModal = true" class="change-avatar-btn">
              更换头像
            </a-button>
          </div>
          
          <!-- 个人信息表单 -->
          <div class="form-section">
            <a-form
              :model="form"
              :rules="rules"
              ref="formRef"
              layout="vertical"
              class="profile-form"
            >
              <a-row :gutter="24">
                <a-col :span="12">
                  <a-form-item label="用户账号" name="userAccount">
                    <a-input v-model:value="form.userAccount" disabled />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="用户昵称" name="userName">
                    <a-input v-model:value="form.userName" placeholder="请输入用户昵称" />
                  </a-form-item>
                </a-col>
              </a-row>
              
              <a-row :gutter="24">
                <a-col :span="12">
                  <a-form-item label="用户角色" name="userRole">
                    <a-select v-model:value="form.userRole" disabled>
                      <a-select-option value="user">普通用户</a-select-option>
                      <a-select-option value="admin">管理员</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="注册时间">
                    <a-input :value="formatDateTime(user.createTime)" disabled />
                  </a-form-item>
                </a-col>
              </a-row>
              
              <a-form-item label="个人简介" name="userProfile">
                <a-textarea 
                  v-model:value="form.userProfile" 
                  placeholder="请输入个人简介" 
                  :rows="4" 
                  :maxlength="200"
                  show-count
                />
              </a-form-item>
              
              <a-form-item>
                <a-button type="primary" @click="handleSubmit" :loading="loading">
                  保存修改
                </a-button>
                <a-button style="margin-left: 8px" @click="handleReset">
                  重置
                </a-button>
              </a-form-item>
            </a-form>
          </div>
        </div>
      </div>
    </main>

    <!-- 更换头像弹窗 -->
    <a-modal
      title="更换头像"
      v-model:visible="showAvatarModal"
      @ok="handleAvatarChange"
      @cancel="showAvatarModal = false"
    >
      <a-form layout="vertical">
        <a-form-item label="头像URL">
          <a-input v-model:value="tempAvatarUrl" placeholder="请输入头像图片URL" />
        </a-form-item>
        <div class="avatar-preview" v-if="tempAvatarUrl">
          <img :src="tempAvatarUrl" alt="头像预览" />
        </div>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { message } from 'ant-design-vue';
import { getUserInfo, updateUser } from '../api/user';

export default {
  name: 'PersonalCenter',
  setup() {
    const router = useRouter();
    const store = useStore();
    const user = computed(() => store.state.user);
    
    const form = ref({
      id: '',
      userAccount: '',
      userName: '',
      userAvatar: '',
      userProfile: '',
      userRole: ''
    });
    
    const loading = ref(false);
    const showAvatarModal = ref(false);
    const tempAvatarUrl = ref('');
    const formRef = ref(null);
    
    const rules = {
      userName: [
        { required: true, message: '请输入用户昵称', trigger: 'blur' },
        { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
      ],
      userProfile: [
        { max: 200, message: '个人简介不能超过 200 个字符', trigger: 'blur' }
      ]
    };
    
    // 获取用户信息
    const fetchUserInfo = async () => {
      try {
        // 如果store中已有用户信息，先使用store中的信息
        if (store.state.user && store.state.user.id) {
          form.value = {
            id: store.state.user.id,
            userAccount: store.state.user.userAccount || '',
            userName: store.state.user.userName || '',
            userAvatar: store.state.user.userAvatar || '',
            userProfile: store.state.user.userProfile || '',
            userRole: store.state.user.userRole || ''
          };
          console.log('使用store中的用户信息:', store.state.user);
        }
        
        // 然后从服务器获取最新用户信息
        const response = await getUserInfo();
        console.log('getUserInfo API响应:', response);
        
        if (response.code === 200 && response.data) {
          form.value = {
            id: response.data.id,
            userAccount: response.data.userAccount,
            userName: response.data.userName,
            userAvatar: response.data.userAvatar,
            userProfile: response.data.userProfile,
            userRole: response.data.userRole
          };
          // 更新store中的用户信息
          store.commit('SET_USER', response.data);
          console.log('获取到的用户信息:', response.data);
          console.log('表单数据:', form.value);
        } else {
          message.error(response.message || '获取用户信息失败');
        }
      } catch (error) {
        console.error('获取用户信息错误:', error);
        message.error('获取用户信息失败，请检查网络连接');
      }
    };
    
    // 提交表单
    const handleSubmit = async () => {
      try {
        await formRef.value.validate();
        loading.value = true;
        
        // 确保使用当前登录用户的ID，而不是表单中的ID
        const currentUserId = user.value ? user.value.id : form.value.id;
        console.log('当前用户ID:', currentUserId);
        console.log('表单数据:', form.value);
        
        const response = await updateUser(currentUserId, form.value);
        if (response.code === 200) {
          message.success('个人信息更新成功');
          // 重新获取用户信息
          await fetchUserInfo();
        } else {
          message.error(response.message || '更新个人信息失败');
        }
      } catch (error) {
        console.error('更新个人信息错误:', error);
        message.error('更新个人信息失败，请检查网络连接');
      } finally {
        loading.value = false;
      }
    };
    
    // 重置表单
    const handleReset = () => {
      fetchUserInfo();
    };
    
    // 更换头像
    const handleAvatarChange = () => {
      if (tempAvatarUrl.value) {
        form.value.userAvatar = tempAvatarUrl.value;
        showAvatarModal.value = false;
        tempAvatarUrl.value = '';
      } else {
        message.warning('请输入头像URL');
      }
    };
    
    // 格式化日期时间
    const formatDateTime = (dateTime) => {
      if (!dateTime) return '';
      return new Date(dateTime).toLocaleString('zh-CN');
    };
    
    // 退出登录
    const handleLogout = () => {
      store.dispatch('logout');
      message.success('已退出登录');
      router.push('/login');
    };
    
    // 组件挂载时获取用户信息
    onMounted(() => {
      fetchUserInfo();
    });
    
    return {
      user,
      form,
      loading,
      showAvatarModal,
      tempAvatarUrl,
      formRef,
      rules,
      handleSubmit,
      handleReset,
      handleAvatarChange,
      formatDateTime,
      handleLogout
    };
  }
};
</script>

<style scoped>
.personal-center-container {
  min-height: 100vh;
  background: #f5f5f5;
}

/* 头部导航 */
.header {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 12px 24px;
  display: flex;
  align-items: center;
  gap: 32px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 600;
  cursor: pointer;
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

.nav-menu {
  flex: 1;
  display: flex;
  gap: 8px;
}

.nav-menu :deep(.ant-btn-link) {
  color: #666;
  font-size: 15px;
}

.nav-menu :deep(.ant-btn-link.active) {
  color: #FF9A3D;
  font-weight: 500;
}

.nav-menu :deep(.ant-btn-link:hover) {
  color: #FF9A3D;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #333;
}

/* 主要内容 */
.main-content {
  padding: 24px;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.page-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #262626;
}

/* 个人信息区域 */
.profile-container {
  display: flex;
  gap: 32px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.avatar-wrapper {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  border: 4px solid #f0f0f0;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-icon {
  font-size: 48px;
  color: #999;
}

.change-avatar-btn {
  padding: 0;
  height: auto;
  color: #1890ff;
}

.form-section {
  flex: 1;
}

.profile-form {
  max-width: 800px;
}

/* 头像预览 */
.avatar-preview {
  margin-top: 16px;
  text-align: center;
}

.avatar-preview img {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-container {
    flex-direction: column;
    align-items: center;
  }
  
  .form-section {
    width: 100%;
  }
}
</style>