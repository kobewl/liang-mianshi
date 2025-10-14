<template>
  <div class="user-manage-container">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <span class="logo-icon">🦆</span>
          <span class="logo-text">面试鸭刷题神器</span>
        </div>

        <nav class="nav-menu">
          <a-button type="link" @click="$router.push('/')">首页</a-button>
          <a-button type="link" @click="$router.push('/questions')">题目管理</a-button>
          <a-button type="link" @click="$router.push('/question-banks')">题库管理</a-button>
          <a-button type="primary">用户管理</a-button>
        </nav>

        <div class="header-right">
          <a-button type="link" @click="handleLogout" class="logout-btn">
            <span class="user-icon">🚪</span>
            退出登录
          </a-button>
        </div>
      </div>
    </header>

    <!-- 主要内容 -->
    <main class="main-content">
      <div class="content-wrapper">
        <div class="page-header">
          <h2 class="page-title">用户管理</h2>
          <a-button type="primary" @click="showAddModal" class="add-btn">
            <span class="btn-icon">➕</span>
            添加用户
          </a-button>
        </div>
        
        <!-- 用户表格 -->
        <div class="users-table-container">
          <a-table
            :columns="columns"
            :data-source="users"
            :loading="loading"
            row-key="id"
            :pagination="{ pageSize: 10, showTotal: (total) => `共 ${total} 条记录` }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'userRole'">
                <a-tag :color="record.userRole === 'admin' ? 'red' : 'blue'">
                  {{ record.userRole === 'admin' ? '管理员' : '普通用户' }}
                </a-tag>
              </template>
              <template v-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="editUser(record)">编辑</a-button>
                  <a-popconfirm
                    title="确定要删除这个用户吗？"
                    @confirm="deleteUser(record.id)"
                    ok-text="确定"
                    cancel-text="取消"
                  >
                    <a-button type="link" size="small" danger>删除</a-button>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
        </div>
      </div>
    </main>

    <!-- 底部 -->
    <footer class="footer">
      <p>面试鸭刷题神器 ©2024 Created by LiangPiao</p>
    </footer>
  </div>

  <!-- 添加/编辑用户弹窗 -->
  <a-modal
    v-model:open="modalVisible"
    :title="isEdit ? '编辑用户' : '添加用户'"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      :model="userForm"
      :rules="rules"
      ref="formRef"
      layout="vertical"
    >
      <a-form-item label="用户账号" name="userAccount">
        <a-input v-model:value="userForm.userAccount" placeholder="请输入用户账号" />
      </a-form-item>
      <a-form-item label="用户名" name="userName">
        <a-input v-model:value="userForm.userName" placeholder="请输入用户名" />
      </a-form-item>
      <a-form-item label="用户角色" name="userRole">
        <a-select v-model:value="userForm.userRole" placeholder="请选择用户角色">
          <a-select-option value="admin">管理员</a-select-option>
          <a-select-option value="user">普通用户</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="用户密码" name="userPassword" v-if="!isEdit">
        <a-input-password v-model:value="userForm.userPassword" placeholder="请输入用户密码" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { getUserList, addUser, updateUser, deleteUser as deleteUserApi } from '../api';

export default {
  name: 'UserManage',
  setup() {
    const router = useRouter();
    const store = useStore();
    const loading = ref(false);
    const modalVisible = ref(false);
    const isEdit = ref(false);
    const formRef = ref();
    
    const users = ref([]);

    const columns = [
      {
        title: 'ID',
        dataIndex: 'id',
        key: 'id',
      },
      {
        title: '用户账号',
        dataIndex: 'userAccount',
        key: 'userAccount',
      },
      {
        title: '用户名',
        dataIndex: 'userName',
        key: 'userName',
      },
      {
        title: '用户角色',
        dataIndex: 'userRole',
        key: 'userRole',
      },
      {
        title: '创建时间',
        dataIndex: 'createTime',
        key: 'createTime',
      },
      {
        title: '操作',
        key: 'action',
      },
    ];

    const userForm = reactive({
      id: null,
      userAccount: '',
      userName: '',
      userRole: '',
      userPassword: ''
    });

    const rules = {
      userAccount: [
        { required: true, message: '请输入用户账号', trigger: 'blur' },
        { min: 4, max: 20, message: '账号长度在 4 到 20 个字符', trigger: 'blur' }
      ],
      userName: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
      ],
      userRole: [
        { required: true, message: '请选择用户角色', trigger: 'change' }
      ],
      userPassword: [
        { required: true, message: '请输入用户密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
      ]
    };

    // 获取用户列表
    const fetchUsers = async () => {
      loading.value = true;
      try {
        const response = await getUserList({
          current: 1,
          pageSize: 100
        });
        
        if (response.code === 200) {
          users.value = response.data.records || [];
        } else {
          message.error(response.message || '获取用户列表失败');
        }
      } catch (error) {
        console.error('获取用户列表错误:', error);
        message.error('获取用户列表失败，请检查网络连接');
      } finally {
        loading.value = false;
      }
    };

    const showAddModal = () => {
      isEdit.value = false;
      modalVisible.value = true;
      resetForm();
    };

    const editUser = (record) => {
      isEdit.value = true;
      modalVisible.value = true;
      Object.assign(userForm, record);
    };

    const handleDeleteUser = async (id) => {
      try {
        const response = await deleteUserApi(id);
        
        if (response.code === 200) {
          message.success('删除成功');
          // 重新获取用户列表
          await fetchUsers();
        } else {
          message.error(response.message || '删除失败');
        }
      } catch (error) {
        console.error('删除用户错误:', error);
        message.error('删除失败，请检查网络连接');
      }
    };

    const handleSubmit = async () => {
      try {
        await formRef.value.validate();
        
        let response;
        if (isEdit.value) {
          // 编辑用户
          response = await updateUser(userForm);
        } else {
          // 添加用户
          response = await addUser(userForm);
        }
        
        if (response.code === 200) {
          message.success(isEdit.value ? '编辑成功' : '添加成功');
          modalVisible.value = false;
          // 重新获取用户列表
          await fetchUsers();
        } else {
          message.error(response.message || (isEdit.value ? '编辑失败' : '添加失败'));
        }
      } catch (error) {
        console.log('表单验证失败:', error);
      }
    };

    const handleCancel = () => {
      modalVisible.value = false;
      resetForm();
    };

    const resetForm = () => {
      Object.assign(userForm, {
        id: null,
        userAccount: '',
        userName: '',
        userRole: '',
        userPassword: ''
      });
    };

    const handleLogout = () => {
      // 使用Vuex store管理登出状态
      store.dispatch('logout');
      message.success('已退出登录');
      router.push('/login');
    };

    onMounted(() => {
      fetchUsers();
    });

    return {
      loading,
      modalVisible,
      isEdit,
      users,
      columns,
      userForm,
      rules,
      formRef,
      showAddModal,
      editUser,
      handleDeleteUser,
      handleSubmit,
      handleCancel,
      handleLogout
    };
  }
}
</script>

<style scoped>
.user-manage-container {
  height: 100vh;
  width: 100vw;
  background: #FFF5E6;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 头部导航 */
.header {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  flex-shrink: 0;
}

.header-content {
  max-width: 1400px;
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

.nav-menu :deep(.ant-btn-link:hover) {
  color: #FF9A3D;
}

.nav-menu :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #FF9A3D 0%, #FF6B35 100%);
  border: none;
}

.header-right {
  display: flex;
  align-items: center;
}

.logout-btn {
  color: #666;
}

.logout-btn:hover {
  color: #FF6B35;
}

.user-icon {
  margin-right: 4px;
}

/* 主要内容 */
.main-content {
  flex: 1;
  background: #FFF5E6;
  overflow-y: auto;
  padding: 24px;
}

.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 16px rgba(255, 154, 61, 0.15);
  min-height: calc(100vh - 180px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.add-btn {
  background: linear-gradient(135deg, #FF9A3D 0%, #FF6B35 100%);
  border: none;
  border-radius: 8px;
  height: 40px;
  padding: 0 20px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(255, 154, 61, 0.3);
  transition: all 0.3s;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 154, 61, 0.4);
}

.btn-icon {
  margin-right: 6px;
}

/* 表格容器 */
.users-table-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.users-table-container :deep(.ant-table) {
  border-radius: 12px;
}

.users-table-container :deep(.ant-table-thead > tr > th) {
  background: #FFF5E6;
  border-bottom: 2px solid #FFE8CC;
  font-weight: 600;
  color: #333;
}

.users-table-container :deep(.ant-table-tbody > tr:hover > td) {
  background: #FFF5E6;
}

.users-table-container :deep(.ant-pagination) {
  margin: 16px 0;
  text-align: center;
}

/* 底部 */
.footer {
  background: #333;
  color: rgba(255, 255, 255, 0.65);
  text-align: center;
  padding: 24px;
  font-size: 14px;
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-wrap: wrap;
    gap: 12px;
  }

  .nav-menu {
    width: 100%;
    overflow-x: auto;
  }

  .content-wrapper {
    padding: 20px 16px;
    margin: 0 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .page-title {
    font-size: 24px;
    text-align: center;
  }
}
</style>