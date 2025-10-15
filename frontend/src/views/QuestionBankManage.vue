<template>
  <div class="question-bank-manage-container">
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
          <a-button type="primary">题库管理</a-button>
          <a-button type="link" @click="$router.push('/users')">用户管理</a-button>
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
        <!-- 题库列表视图 -->
        <div v-if="!isDetailView">
          <div class="page-header">
            <h2 class="page-title">题库管理</h2>
            <a-button type="primary" @click="showAddModal" class="add-btn">
              <span class="btn-icon">➕</span>
              添加题库
            </a-button>
          </div>
          
          <a-table :columns="columns" :data-source="questionBanks" :loading="loading" row-key="id">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'picture'">
                <img v-if="record.picture" :src="record.picture" style="width: 50px; height: 50px;" />
                <span v-else>无图片</span>
              </template>
              <template v-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="editQuestionBank(record)">编辑</a-button>
                  <a-popconfirm
                    title="确定要删除这个题库吗？"
                    @confirm="handleDeleteQuestionBank(record.id)"
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

        <!-- 题库详情视图 -->
        <div v-else-if="currentQuestionBank">
          <div class="page-header">
            <a-button type="link" @click="backToList" class="back-btn">
              <span class="btn-icon">←</span>
              返回列表
            </a-button>
            <h2 class="page-title">题库详情</h2>
            <div></div>
          </div>

          <div class="detail-container">
            <div class="detail-header">
              <div class="detail-picture">
                <img v-if="currentQuestionBank.picture" :src="currentQuestionBank.picture" alt="题库图片" />
                <div v-else class="no-picture">暂无图片</div>
              </div>
              <div class="detail-info">
                <h2 class="detail-title">{{ currentQuestionBank.title }}</h2>
                <p class="detail-description">{{ currentQuestionBank.description || '暂无描述' }}</p>
                <div class="detail-meta">
                  <p><strong>创建时间：</strong>{{ currentQuestionBank.createTime }}</p>
                  <p><strong>更新时间：</strong>{{ currentQuestionBank.updateTime }}</p>
                </div>
              </div>
            </div>
            
            <div class="detail-actions">
              <a-button type="primary" @click="editQuestionBank(currentQuestionBank)">
                <span class="btn-icon">✏️</span>
                编辑题库
              </a-button>
              <a-button @click="backToList">
                <span class="btn-icon">📋</span>
                查看题目
              </a-button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 底部 -->
    <footer class="footer">
      <p>面试鸭刷题神器 ©2024 Created by LiangPiao</p>
    </footer>
  </div>

  <!-- 添加/编辑题库弹窗 -->
  <a-modal
    v-model:open="modalVisible"
    :title="isEdit ? '编辑题库' : '添加题库'"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      :model="questionBankForm"
      :rules="rules"
      ref="formRef"
      layout="vertical"
    >
      <a-form-item label="题库标题" name="title">
        <a-input v-model:value="questionBankForm.title" placeholder="请输入题库标题" />
      </a-form-item>
      <a-form-item label="题库描述" name="description">
        <a-textarea v-model:value="questionBankForm.description" :rows="4" placeholder="请输入题库描述" />
      </a-form-item>
      <a-form-item label="题库图片" name="picture">
        <a-input v-model:value="questionBankForm.picture" placeholder="请输入题库图片URL" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script>
import { ref, reactive, onMounted, watch } from 'vue';
import { message } from 'ant-design-vue';
import { useRouter, useRoute } from 'vue-router';
import { useStore } from 'vuex';
import { getQuestionBankList, addQuestionBank, updateQuestionBank, deleteQuestionBank, getQuestionBankById } from '../api/questionBank';

export default {
  name: 'QuestionBankManage',
  setup() {
    const router = useRouter();
    const route = useRoute();
    const store = useStore();
    const loading = ref(false);
    const modalVisible = ref(false);
    const isEdit = ref(false);
    const formRef = ref();
    
    const questionBanks = ref([]);
    const isDetailView = ref(false);
    const currentQuestionBank = ref(null);

    const columns = [
      {
        title: 'ID',
        dataIndex: 'id',
        key: 'id',
      },
      {
        title: '题库标题',
        dataIndex: 'title',
        key: 'title',
      },
      {
        title: '描述',
        dataIndex: 'description',
        key: 'description',
        ellipsis: true,
      },
      {
        title: '图片',
        dataIndex: 'picture',
        key: 'picture',
        width: 100,
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

    const questionBankForm = reactive({
      id: null,
      title: '',
      description: '',
      picture: '',
      userId: null
    });

    const rules = {
      title: [
        { required: true, message: '请输入题库标题', trigger: 'blur' }
      ],
      description: [
        { required: false, message: '请输入题库描述', trigger: 'blur' }
      ]
    };

    // 获取题库详情
    const fetchQuestionBankDetail = async (id) => {
      loading.value = true;
      try {
        const response = await getQuestionBankById(id);
        if (response.code === 200) {
          currentQuestionBank.value = response.data;
          isDetailView.value = true;
        } else {
          message.error(response.message || '获取题库详情失败');
          router.push('/question-banks');
        }
      } catch (error) {
        console.error('获取题库详情错误:', error);
        message.error('获取题库详情失败，请检查网络连接');
        router.push('/question-banks');
      } finally {
        loading.value = false;
      }
    };

    // 返回列表视图
    const backToList = () => {
      isDetailView.value = false;
      currentQuestionBank.value = null;
      router.push('/question-banks');
    };
    const fetchQuestionBanks = async () => {
      loading.value = true;
      try {
        const response = await getQuestionBankList({
          current: 1,
          pageSize: 100
        });
        
        if (response.code === 200) {
          questionBanks.value = response.data.records || [];
        } else {
          message.error(response.message || '获取题库列表失败');
        }
      } catch (error) {
        console.error('获取题库列表错误:', error);
        message.error('获取题库列表失败，请检查网络连接');
      } finally {
        loading.value = false;
      }
    };

    const showAddModal = () => {
      isEdit.value = false;
      modalVisible.value = true;
      resetForm();
    };

    const editQuestionBank = (record) => {
      isEdit.value = true;
      modalVisible.value = true;
      Object.assign(questionBankForm, record);
    };

    const handleDeleteQuestionBank = async (id) => {
      try {
        const response = await deleteQuestionBank(id);
        
        if (response.code === 200) {
          message.success('删除成功');
          // 重新获取题库列表
          await fetchQuestionBanks();
        } else {
          message.error(response.message || '删除失败');
        }
      } catch (error) {
        console.error('删除题库错误:', error);
        message.error('删除失败，请检查网络连接');
      }
    };

    const handleSubmit = async () => {
      try {
        await formRef.value.validate();
        
        // 确保userId正确设置
        const userId = store.state.user?.id || store.state.user?.userId;
        if (!userId) {
          message.error('用户未登录，无法创建题库');
          return;
        }
        
        const data = {
          ...questionBankForm,
          userId: userId
        };
        
        let response;
        if (isEdit.value) {
          // 编辑题库
          response = await updateQuestionBank(data);
        } else {
          // 添加题库
          response = await addQuestionBank(data);
        }
        
        if (response.code === 200) {
          message.success(isEdit.value ? '编辑成功' : '添加成功');
          modalVisible.value = false;
          // 重新获取题库列表
          await fetchQuestionBanks();
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
      Object.assign(questionBankForm, {
        id: null,
        title: '',
        description: '',
        picture: '',
        userId: null
      });
    };

    const handleLogout = () => {
      // 使用Vuex store管理登出状态
      store.dispatch('logout');
      message.success('已退出登录');
      router.push('/login');
    };

    // 监听路由参数变化
    watch(
      () => route.params.id,
      (newId) => {
        if (newId) {
          // 有ID参数，显示题库详情
          fetchQuestionBankDetail(newId);
        } else {
          // 没有ID参数，显示题库列表
          isDetailView.value = false;
          currentQuestionBank.value = null;
          fetchQuestionBanks();
        }
      },
      { immediate: true }
    );

    onMounted(() => {
      // 如果没有ID参数，获取题库列表
      if (!route.params.id) {
        fetchQuestionBanks();
      }
      // 设置当前用户ID
      const userId = store.state.user?.id || store.state.user?.userId;
      if (userId) {
        questionBankForm.userId = userId;
      }
    });

    return {
      loading,
      modalVisible,
      isEdit,
      questionBanks,
      columns,
      questionBankForm,
      rules,
      formRef,
      isDetailView,
      currentQuestionBank,
      showAddModal,
      editQuestionBank,
      handleDeleteQuestionBank,
      handleSubmit,
      handleCancel,
      handleLogout,
      backToList
    };
  }
}
</script>

<style scoped>
.site-layout-content {
  min-height: 280px;
  padding: 24px;
  background: #fff;
}

.question-bank-manage-container {
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

.back-btn {
  color: #1890ff;
  font-size: 14px;
  padding: 0;
  height: auto;
  margin-right: 16px;
}

.back-btn:hover {
  color: #40a9ff;
}

.detail-container {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.detail-header {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
}

.detail-picture {
  flex-shrink: 0;
  width: 200px;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-picture img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-picture {
  color: #999;
  font-size: 16px;
}

.detail-info {
  flex: 1;
}

.detail-title {
  margin: 0 0 16px 0;
  font-size: 28px;
  font-weight: 600;
  color: #262626;
}

.detail-description {
  font-size: 16px;
  line-height: 1.6;
  color: #595959;
  margin-bottom: 24px;
}

.detail-meta {
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}

.detail-meta p {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #8c8c8c;
}

.detail-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

/* 表格容器 */
.question-banks-table-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.question-banks-table-container :deep(.ant-table) {
  border-radius: 12px;
}

.question-banks-table-container :deep(.ant-table-thead > tr > th) {
  background: #FFF5E6;
  border-bottom: 2px solid #FFE8CC;
  font-weight: 600;
  color: #333;
}

.question-banks-table-container :deep(.ant-table-tbody > tr:hover > td) {
  background: #FFF5E6;
}

.question-banks-table-container :deep(.ant-pagination) {
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