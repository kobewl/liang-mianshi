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
          <a-button type="link" v-if="isAdmin" @click="$router.push('/questions')">题目管理</a-button>
          <a-button type="primary">题库详情</a-button>
          <a-button type="link" v-if="isAdmin" @click="$router.push('/users')">用户管理</a-button>
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
            <h2 class="page-title">题库详情</h2>
            <!-- 管理员可以看到添加题库按钮，普通用户看不到 -->
            <a-button v-if="isAdmin" type="primary" @click="$router.push('/question-banks/create')" class="add-btn">
              <span class="btn-icon">➕</span>
              添加题库
            </a-button>
          </div>

          <a-table 
            :columns="columns" 
            :data-source="questionBanks" 
            :loading="loading" 
            row-key="id"
            :pagination="{ 
              pageSize: 10, 
              showTotal: (total) => `共 ${total} 条记录`,
              showSizeChanger: true,
              showQuickJumper: true,
              pageSizeOptions: ['10', '20', '50', '100']
            }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'picture'">
                <img v-if="record.picture" :src="record.picture" style="width: 50px; height: 50px; border-radius: 4px;" />
                <span v-else style="color: #999;">无封面</span>
              </template>
              <template v-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="$router.push(`/question-banks/edit/${record.id}`)">编辑</a-button>
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

          <div class="detail-container">
            <div class="detail-header">
              <div class="detail-picture">
                <img v-if="currentQuestionBank.picture" :src="currentQuestionBank.picture" alt="题库图片" />
                <div v-else class="no-picture">暂无封面</div>
              </div>
              <div class="page-header">
                <a-button type="link" @click="backToList" class="back-btn">
                  <span class="btn-icon">←</span>
                  返回列表
                </a-button>
              </div>
              <div class="detail-info">
                <h2 class="detail-title">{{ currentQuestionBank.title }}</h2>
                <p class="detail-description">{{ currentQuestionBank.description || '暂无描述' }}</p>
                <!-- 管理员可以看到元信息，普通用户看不到 -->
                <div class="detail-meta" v-if="isAdmin">
                  <p><strong>创建时间：</strong>{{ formatDateTime(currentQuestionBank.createTime) }}</p>
                  <p><strong>更新时间：</strong>{{ formatDateTime(currentQuestionBank.updateTime) }}</p>
                </div>
              </div>
            </div>

            <!-- 题目列表 -->
            <div class="questions-section">
              <div class="section-header">
                <h3>题库题目</h3>
                <!-- 管理员可以看到添加题目按钮，普通用户看不到 -->
                <a-button v-if="isAdmin" type="primary" @click="$router.push(`/questions/create?questionBankId=${currentQuestionBank.id}`)">
                  <span class="btn-icon">➕</span>
                  添加题目
                </a-button>
              </div>

              <div class="questions-list">
                <div v-if="questionBankQuestions.length === 0" class="no-questions">
                  <a-empty description="暂无题目，快去添加吧！" />
                </div>
                <div v-else class="question-item" v-for="question in questionBankQuestions" :key="question.id">
                  <div class="question-main">
                    <h4 class="question-title-text" @click="$router.push(`/question/${question.id}`)">
                      {{ question.title }}
                    </h4>
                    <div class="question-tags">
                      <a-tag v-for="tag in (typeof question.tags === 'string' ? question.tags.split(',') : question.tags || [])" :key="tag" size="small">{{ tag }}</a-tag>
                    </div>
                  </div>
                  <div class="question-actions">
                    <a-space>
                      <a-button type="link" size="small" @click="$router.push(`/question/${question.id}`)">
                        查看详情
                      </a-button>
                      <!-- 管理员可以看到编辑和删除按钮，普通用户看不到 -->
                      <a-button v-if="isAdmin" type="link" size="small" @click="$router.push(`/questions/edit/${question.id}`)">
                        编辑
                      </a-button>
                      <a-popconfirm
                        v-if="isAdmin"
                        title="确定要从题库中移除这个题目吗？"
                        @confirm="removeQuestionFromBank(question.id)"
                        ok-text="确定"
                        cancel-text="取消"
                      >
                        <a-button type="link" size="small" danger>移除</a-button>
                      </a-popconfirm>
                    </a-space>
                  </div>
                </div>
              </div>
            </div>

            <div class="detail-actions">
              <!-- 管理员可以看到编辑题库按钮，普通用户看不到 -->
              <a-button v-if="isAdmin" type="primary" @click="$router.push(`/question-banks/edit/${currentQuestionBank.id}`)">
                <span class="btn-icon">✏️</span>
                编辑题库
              </a-button>
              <a-button @click="backToList">
                <span class="btn-icon">📋</span>
                返回列表
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
</template>

<script>
import { ref, reactive, onMounted, watch } from 'vue';
import { message } from 'ant-design-vue';
import { useRouter, useRoute } from 'vue-router';
import { useStore } from 'vuex';
import { getQuestionBankList, deleteQuestionBank, getQuestionBankById, deleteQuestionBankQuestion } from '../api/questionBank';
import { getQuestionList } from '../api/question';

export default {
  name: 'QuestionBankManage',
  setup() {
    const router = useRouter();
    const route = useRoute();
    const store = useStore();
    const loading = ref(false);

    const questionBanks = ref([]);
    const isDetailView = ref(false);
    const currentQuestionBank = ref(null);
    const questionBankQuestions = ref([]);

    const columns = [
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
        title: '封面',
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

    // 获取题库详情
    const fetchQuestionBankDetail = async (id) => {
      loading.value = true;
      try {
        const response = await getQuestionBankById(id);
        if (response.code === 200) {
          currentQuestionBank.value = response.data;
          isDetailView.value = true;
          // 获取题库中的题目列表
          await fetchQuestionBankQuestions(id);
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

    // 获取题库中的题目列表
    const fetchQuestionBankQuestions = async (questionBankId) => {
      try {
        const response = await getQuestionList({
          questionBankId: questionBankId,
          current: 1,
          size: 100
        });

        if (response.code === 200) {
          questionBankQuestions.value = response.data.records || [];
        } else {
          message.error(response.message || '获取题目列表失败');
        }
      } catch (error) {
        console.error('获取题库题目列表错误:', error);
        message.error('获取题目列表失败，请检查网络连接');
      }
    };

    // 从题库中移除题目
    const removeQuestionFromBank = async (questionId) => {
      try {
        const response = await deleteQuestionBankQuestion({
          questionBankId: currentQuestionBank.value.id,
          questionId: questionId
        });

        if (response.code === 200) {
          message.success('题目已从题库中移除');
          // 重新获取题目列表
          await fetchQuestionBankQuestions(currentQuestionBank.value.id);
        } else {
          message.error(response.message || '移除失败');
        }
      } catch (error) {
        console.error('移除题目错误:', error);
        message.error('移除失败，请检查网络连接');
      }
    };

    // 返回列表视图
    const backToList = () => {
      isDetailView.value = false;
      currentQuestionBank.value = null;
      questionBankQuestions.value = [];
      router.push('/question-banks');
    };

    const fetchQuestionBanks = async () => {
      loading.value = true;
      try {
        const response = await getQuestionBankList({
          current: 1,
          size: 100
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

    const handleLogout = () => {
      // 使用Vuex store管理登出状态
      store.dispatch('logout');
      message.success('已退出登录');
      router.push('/login');
    };

    // 格式化日期时间
    const formatDateTime = (dateTime) => {
      if (!dateTime) return '';
      return new Date(dateTime).toLocaleString('zh-CN');
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
    });

    return {
      loading,
      questionBanks,
      columns,
      isDetailView,
      currentQuestionBank,
      questionBankQuestions,
      handleDeleteQuestionBank,
      handleLogout,
      backToList,
      formatDateTime,
      removeQuestionFromBank
    };
  }
}
</script>

<style scoped>
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
:deep(.ant-table) {
  border-radius: 12px;
}

:deep(.ant-table-thead > tr > th) {
  background: #FFF5E6;
  border-bottom: 2px solid #FFE8CC;
  font-weight: 600;
  color: #333;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: #FFF5E6;
}

:deep(.ant-pagination) {
  margin: 16px 0;
  text-align: center;
}

/* 题目列表样式 */
.questions-section {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.questions-list {
  margin-bottom: 24px;
}

.question-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px;
  margin-bottom: 12px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.question-item:hover {
  background: #f0f7ff;
  border-color: #1890ff;
}

.question-main {
  flex: 1;
}

.question-title-text {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
  cursor: pointer;
  transition: color 0.3s;
}

.question-title-text:hover {
  color: #1890ff;
}

.question-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.question-actions {
  display: flex;
  gap: 8px;
}

.no-questions {
  text-align: center;
  padding: 48px 0;
  color: #999;
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

  .detail-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .detail-picture {
    width: 150px;
    height: 150px;
  }

  .question-item {
    flex-direction: column;
    gap: 12px;
  }

  .question-actions {
    align-self: stretch;
  }

  .section-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
}
</style>