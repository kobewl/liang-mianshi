<template>
  <div class="question-manage-container">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <span class="logo-icon">🦆</span>
          <span class="logo-text">面试鸭刷题神器</span>
        </div>

        <nav class="nav-menu">
          <a-button type="link" @click="$router.push('/')">首页</a-button>
          <a-button type="primary">题目管理</a-button>
          <a-button type="link" v-if="isAdmin" @click="$router.push('/question-banks')">题库管理</a-button>
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
        <div class="page-header">
          <h2 class="page-title">题目管理</h2>
          <!-- 管理员可以看到添加题目按钮，普通用户看不到 -->
          <a-button v-if="isAdmin" type="primary" @click="$router.push('/questions/create')" class="add-btn">
            <span class="btn-icon">➕</span>
            添加题目
          </a-button>
        </div>
        
        <!-- 题目表格 -->
        <div class="questions-table-container">
          <a-table
            :columns="columns"
            :data-source="questions"
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
              <template v-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="$router.push(`/questions/edit/${record.id}`)">编辑</a-button>
                  <a-popconfirm
                    title="确定要删除这个题目吗？"
                    @confirm="handleDeleteQuestion(record.id)"
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

</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { getQuestionList, addQuestion, updateQuestion, deleteQuestion } from '../api/question';

export default {
  name: 'QuestionManage',
  setup() {
    const router = useRouter();
    const store = useStore();
    const loading = ref(false);
    const modalVisible = ref(false);
    const isEdit = ref(false);
    const formRef = ref();

    const questions = ref([]);
    const contentTab = ref('edit');
    const answerTab = ref('edit');

    const columns = [
      {
        title: '题目',
        dataIndex: 'title',
        key: 'title',
      },
      {
        title: '内容',
        dataIndex: 'content',
        key: 'content',
        ellipsis: true,
      },
      {
        title: '答案',
        dataIndex: 'answer',
        key: 'answer',
        ellipsis: true,
      },
      {
        title: '标签',
        dataIndex: 'tags',
        key: 'tags',
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

    const questionForm = reactive({
      id: null,
      title: '',
      content: '',
      answer: '',
      tags: '',
      userId: null
    });

    const rules = {
      title: [
        { required: true, message: '请输入题目标题', trigger: 'blur' }
      ],
      content: [
        { required: false, message: '请输入题目内容', trigger: 'blur' }
      ],
      answer: [
        { required: false, message: '请输入答案', trigger: 'blur' }
      ]
    };

    // 获取题目列表
    const fetchQuestions = async () => {
      loading.value = true;
      try {
        const response = await getQuestionList({
          current: 1,
          size: 100
        });
        
        if (response.code === 200) {
          questions.value = response.data.records || [];
        } else {
          message.error(response.message || '获取题目列表失败');
        }
      } catch (error) {
        console.error('获取题目列表错误:', error);
        message.error('获取题目列表失败，请检查网络连接');
      } finally {
        loading.value = false;
      }
    };

    const showAddModal = () => {
      isEdit.value = false;
      modalVisible.value = true;
      resetForm();
    };

    const editQuestion = (record) => {
      isEdit.value = true;
      modalVisible.value = true;
      questionForm.id = record.id;
      questionForm.title = record.title;
      questionForm.content = record.content;
      questionForm.answer = record.answer;
      // 将标签数组转换为逗号分隔的字符串
      questionForm.tags = Array.isArray(record.tags) ? record.tags.join(', ') : record.tags || '';
      questionForm.userId = record.userId;
    };

    const handleDeleteQuestion = async (id) => {
      try {
        const response = await deleteQuestion(id);

        if (response.code === 200) {
          message.success('删除成功');
          // 重新获取题目列表
          await fetchQuestions();
        } else {
          message.error(response.message || '删除失败');
        }
      } catch (error) {
        console.error('删除题目错误:', error);
        message.error('删除失败，请检查网络连接');
      }
    };

    const handleSubmit = async () => {
      try {
        await formRef.value.validate();
        
        // 将标签字符串转换为逗号分隔的字符串（后端期望字符串格式）
        const tagsString = questionForm.tags ? questionForm.tags.split(',').map(tag => tag.trim()).filter(tag => tag).join(',') : '';

        const data = {
          title: questionForm.title,
          content: questionForm.content,
          answer: questionForm.answer,
          tags: tagsString,
          userId: questionForm.userId || (store.state.user ? store.state.user.id : null)
        };
        
        let response;
        if (isEdit.value) {
          // 编辑题目
          response = await updateQuestion(questionForm.id, data);
        } else {
          // 添加题目
          response = await addQuestion(data);
        }
        
        if (response.code === 200) {
          message.success(isEdit.value ? '编辑成功' : '添加成功');
          modalVisible.value = false;
          // 重新获取题目列表
          await fetchQuestions();
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
      Object.assign(questionForm, {
        id: null,
        title: '',
        content: '',
        answer: '',
        tags: '',
        userId: null
      });
    };

    const handleLogout = () => {
      // 使用Vuex store管理登出状态
      store.dispatch('logout');
      message.success('已退出登录');
      router.push('/login');
    };

    // Markdown渲染函数
    const renderMarkdown = (text) => {
      if (!text) return '';

      // 简单的Markdown解析
      return text
        // 标题
        .replace(/^### (.*$)/gim, '<h3>$1</h3>')
        .replace(/^## (.*$)/gim, '<h2>$1</h2>')
        .replace(/^# (.*$)/gim, '<h1>$1</h1>')
        // 粗体
        .replace(/\*\*(.*)\*\*/gim, '<strong>$1</strong>')
        // 斜体
        .replace(/\*(.*)\*/gim, '<em>$1</em>')
        // 代码块
        .replace(/```([\s\S]*?)```/gim, '<pre><code>$1</code></pre>')
        // 行内代码
        .replace(/`(.*?)`/gim, '<code>$1</code>')
        // 链接
        .replace(/\[([^\]]+)\]\(([^\)]+)\)/gim, '<a href="$2" target="_blank">$1</a>')
        // 换行
        .replace(/\n/gim, '<br>');
    };

    onMounted(() => {
      fetchQuestions();
      // 设置当前用户ID
      if (store.state.user && store.state.user.id) {
        questionForm.userId = store.state.user.id;
      }
    });

    return {
      loading,
      modalVisible,
      isEdit,
      questions,
      columns,
      questionForm,
      rules,
      formRef,
      contentTab,
      answerTab,
      showAddModal,
      editQuestion,
      handleDeleteQuestion,
      handleSubmit,
      handleCancel,
      handleLogout,
      renderMarkdown
    };
  }
}
</script>

<style scoped>
.question-manage-container {
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
.questions-table-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.questions-table-container :deep(.ant-table) {
  border-radius: 12px;
}

.questions-table-container :deep(.ant-table-thead > tr > th) {
  background: #FFF5E6;
  border-bottom: 2px solid #FFE8CC;
  font-weight: 600;
  color: #333;
}

.questions-table-container :deep(.ant-table-tbody > tr:hover > td) {
  background: #FFF5E6;
}

.questions-table-container :deep(.ant-pagination) {
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

/* Markdown编辑器样式 */
.markdown-editor-container {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  overflow: hidden;
}

.editor-tabs {
  background: #fafafa;
  padding: 8px 12px;
  border-bottom: 1px solid #d9d9d9;
}

.editor-content {
  min-height: 200px;
}

.editor-area {
  padding: 12px;
}

.markdown-textarea {
  border: none;
  resize: vertical;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.5;
}

.markdown-textarea:focus {
  box-shadow: none;
}

.preview-area {
  padding: 16px;
  min-height: 200px;
  background: #fff;
}

.markdown-preview {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  line-height: 1.6;
  color: #333;
}

.markdown-preview h1,
.markdown-preview h2,
.markdown-preview h3 {
  margin: 16px 0 8px 0;
  color: #262626;
}

.markdown-preview h1 {
  font-size: 24px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 8px;
}

.markdown-preview h2 {
  font-size: 20px;
}

.markdown-preview h3 {
  font-size: 16px;
}

.markdown-preview p {
  margin: 8px 0;
}

.markdown-preview code {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
}

.markdown-preview pre {
  background: #f6f8fa;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 12px 0;
}

.markdown-preview pre code {
  background: none;
  padding: 0;
}

.markdown-preview strong {
  font-weight: 600;
}

.markdown-preview em {
  font-style: italic;
}

.markdown-preview a {
  color: #1890ff;
  text-decoration: none;
}

.markdown-preview a:hover {
  text-decoration: underline;
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

  .markdown-editor-container {
    margin: 0 -12px;
  }
}
</style>