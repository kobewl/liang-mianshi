<template>
  <PageLayout
    variant="admin"
    :hero="hero"
    :nav-items="navItems"
  >
    <section class="manage-shell glass-card">
      <div class="manage-head">
        <div>
          <h2>用户列表</h2>
          <p>集中管理系统用户，支持角色调整、信息维护与账号新增。</p>
        </div>
        <a-space>
          <a-button shape="round" @click="refresh">刷新</a-button>
          <a-button type="primary" shape="round" @click="showAddModal">
            新增用户
          </a-button>
        </a-space>
      </div>

      <div class="filter-box">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12" :md="8">
            <a-input
              v-model:value="query.userAccount"
              placeholder="按账号搜索"
              allow-clear
              @pressEnter="refresh"
            />
          </a-col>
          <a-col :xs="24" :sm="12" :md="8">
            <a-input
              v-model:value="query.userName"
              placeholder="按昵称搜索"
              allow-clear
              @pressEnter="refresh"
            />
          </a-col>
          <a-col :xs="24" :sm="12" :md="6">
            <a-select
              v-model:value="query.userRole"
              placeholder="用户角色"
              allow-clear
            >
              <a-select-option value="admin">管理员</a-select-option>
              <a-select-option value="user">普通用户</a-select-option>
            </a-select>
          </a-col>
          <a-col :xs="24" :md="2">
            <a-space>
              <a-button shape="round" @click="resetFilters">重置</a-button>
              <a-button type="primary" shape="round" @click="refresh">应用</a-button>
            </a-space>
          </a-col>
        </a-row>
      </div>

      <a-table
        :columns="columns"
        :data-source="users"
        :loading="loading"
        row-key="id"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'userRole'">
            <a-tag :color="record.userRole === 'admin' ? 'red' : 'blue'" class="tag-pill">
              {{ record.userRole === 'admin' ? '管理员' : '普通用户' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="editUser(record)">编辑</a-button>
              <a-popconfirm
                title="确认删除该用户？"
                ok-text="删除"
                cancel-text="取消"
                @confirm="handleDeleteUser(record.id)"
              >
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </section>

    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      :confirm-loading="modalLoading"
      ok-text="保存"
      cancel-text="取消"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="userForm"
        :rules="rules"
        layout="vertical"
      >
        <a-form-item label="用户账号" name="userAccount">
          <a-input v-model:value="userForm.userAccount" :disabled="isEdit" placeholder="请输入用户账号" />
        </a-form-item>
        <a-form-item label="用户昵称" name="userName">
          <a-input v-model:value="userForm.userName" placeholder="请输入用户昵称" />
        </a-form-item>
        <a-form-item label="用户角色" name="userRole">
          <a-select v-model:value="userForm.userRole" placeholder="请选择用户角色">
            <a-select-option value="admin">管理员</a-select-option>
            <a-select-option value="user">普通用户</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item v-if="!isEdit" label="初始密码" name="userPassword">
          <a-input-password v-model:value="userForm.userPassword" placeholder="请输入初始密码" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageLayout>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import PageLayout from '../components/layout/PageLayout.vue';
import { getUserList, addUser, updateUser, deleteUser } from '../api';

const loading = ref(false);
const modalVisible = ref(false);
const modalLoading = ref(false);
const isEdit = ref(false);

const users = ref([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total) => `共 ${total} 条`
});

const query = reactive({
  userAccount: '',
  userName: '',
  userRole: ''
});

const userForm = reactive({
  id: null,
  userAccount: '',
  userName: '',
  userRole: 'user',
  userPassword: ''
});

const hero = {
  badge: '用户管理',
  title: '构建高效协作的团队成员',
  subtitle: '为题库运营团队分配角色，保障数据安全与操作权限。'
};

const navItems = [
  { key: 'admin-home', label: '首页', path: '/admin' },
  { key: 'question-manage', label: '题目列表', path: '/questions' },
  { key: 'bank-manage', label: '题库管理', path: '/question-banks' },
  { key: 'user-manage', label: '用户管理', path: '/users' }
];

const columns = [
  { title: '用户账号', dataIndex: 'userAccount', key: 'userAccount' },
  { title: '用户昵称', dataIndex: 'userName', key: 'userName' },
  { title: '角色', dataIndex: 'userRole', key: 'userRole', width: 140 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 160 }
];

const rules = {
  userAccount: [
    { required: true, message: '请输入用户账号', trigger: 'blur' },
    { min: 4, max: 20, message: '账号长度需在 4-20 个字符之间', trigger: 'blur' }
  ],
  userName: [
    { required: true, message: '请输入用户昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度需在 2-20 个字符之间', trigger: 'blur' }
  ],
  userRole: [
    { required: true, message: '请选择用户角色', trigger: 'change' }
  ],
  userPassword: [
    {
      validator: (_rule, value) => {
        if (isEdit.value) {
          return Promise.resolve();
        }
        if (!value) {
          return Promise.reject(new Error('请输入用户密码'));
        }
        if (value.length < 6 || value.length > 20) {
          return Promise.reject(new Error('密码长度需在 6-20 个字符之间'));
        }
        return Promise.resolve();
      },
      trigger: 'blur'
    }
  ]
};

const fetchUsers = async () => {
  loading.value = true;
  try {
    const params = {
      current: pagination.current,
      size: pagination.pageSize,
      userAccount: query.userAccount || undefined,
      userName: query.userName || undefined,
      userRole: query.userRole || undefined
    };
    const response = await getUserList(params);
    if (response.code === 200) {
      users.value = response.data?.records || [];
      pagination.total = response.data?.total || 0;
    } else {
      message.error(response.message || '获取用户列表失败');
    }
  } catch (error) {
    console.error('获取用户列表失败:', error);
    message.error('获取用户列表失败，请稍后再试');
  } finally {
    loading.value = false;
  }
};

const refresh = () => {
  pagination.current = 1;
  fetchUsers();
};

const resetFilters = () => {
  query.userAccount = '';
  query.userName = '';
  query.userRole = '';
  refresh();
};

const handleTableChange = (pager) => {
  pagination.current = pager.current;
  pagination.pageSize = pager.pageSize;
  fetchUsers();
};

const showAddModal = () => {
  isEdit.value = false;
  Object.assign(userForm, {
    id: null,
    userAccount: '',
    userName: '',
    userRole: 'user',
    userPassword: ''
  });
  modalVisible.value = true;
};

const editUser = (record) => {
  isEdit.value = true;
  Object.assign(userForm, {
    id: record.id,
    userAccount: record.userAccount,
    userName: record.userName,
    userRole: record.userRole,
    userPassword: ''
  });
  modalVisible.value = true;
};

const handleSubmit = async () => {
  try {
    await formRef.value.validate();
  } catch (error) {
    return;
  }

  modalLoading.value = true;
  try {
    const payload = {
      userAccount: userForm.userAccount,
      userName: userForm.userName,
      userRole: userForm.userRole
    };

    let response;
    if (isEdit.value) {
      response = await updateUser(userForm.id, payload);
    } else {
      payload.userPassword = userForm.userPassword;
      response = await addUser(payload);
    }

    if (response.code === 200) {
      message.success(isEdit.value ? '用户信息已更新' : '用户创建成功');
      modalVisible.value = false;
      refresh();
    } else {
      message.error(response.message || '操作失败');
    }
  } catch (error) {
    console.error('提交用户失败:', error);
    message.error('提交失败，请稍后再试');
  } finally {
    modalLoading.value = false;
  }
};

const handleDeleteUser = (id) => async () => {
  try {
    const response = await deleteUser(id);
    if (response.code === 200) {
      message.success('删除用户成功');
      refresh();
    } else {
      message.error(response.message || '删除失败');
    }
  } catch (error) {
    console.error('删除用户失败:', error);
    message.error('删除失败，请稍后再试');
  }
};

const handleCancel = () => {
  modalVisible.value = false;
};

const formatDate = (value) => {
  if (!value) return '刚刚';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '刚刚';
  return date.toLocaleString();
};

const formRef = ref();

onMounted(() => {
  fetchUsers();
});
</script>

<style scoped>
.manage-shell {
  padding: 32px 32px 40px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.manage-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
}

.manage-head h2 {
  font-size: 26px;
  font-weight: 700;
}

.manage-head p {
  margin-top: 8px;
  color: var(--text-secondary);
}

.filter-box {
  padding: 18px;
  border-radius: 16px;
  background: rgba(248, 250, 252, 0.75);
  border: 1px solid rgba(148, 163, 184, 0.18);
}

.tag-pill {
  border-radius: 999px;
}

@media (max-width: 768px) {
  .manage-shell {
    padding: 24px;
  }

  .manage-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
