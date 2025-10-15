<template>
  <div class="question-edit-container">
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
          <a-button type="link" v-if="isAdmin" @click="$router.push('/question-banks')">题库管理</a-button>
          <a-button type="primary">{{ isEdit ? '编辑题目' : '添加题目' }}</a-button>
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
          <a-button type="link" @click="backToList" class="back-btn">
            <span class="btn-icon">←</span>
            返回列表
          </a-button>
          <h2 class="page-title">{{ isEdit ? '编辑题目' : '添加题目' }}</h2>
          <div></div>
        </div>

        <!-- 题目编辑表单 -->
        <div class="edit-form-container">
          <a-alert
            v-if="isEdit && !questionForm.id"
            message="正在加载题目信息..."
            type="info"
            show-icon
            class="loading-alert"
          />
          <a-form
            :model="questionForm"
            :rules="rules"
            ref="formRef"
            layout="vertical"
            class="edit-form"
          >
            <a-form-item label="题目标题" name="title">
              <a-input v-model:value="questionForm.title" placeholder="请输入题目标题" size="large" />
            </a-form-item>

            <a-form-item label="题目内容" name="content">
              <div class="split-editor-container">
                <div class="split-editor-left">
                  <div class="editor-header">
                    <span class="editor-title">✏️ 编辑器</span>
                  </div>
                  <a-textarea
                    v-model:value="questionForm.content"
                    :rows="12"
                    placeholder="请输入题目内容（支持Markdown语法）"
                    class="split-textarea"
                  />
                </div>
                <div class="split-editor-right">
                  <div class="editor-header">
                    <span class="editor-title">👁️ 实时预览</span>
                  </div>
                  <div class="split-preview">
                    <div class="markdown-preview" v-html="renderMarkdown(questionForm.content) || '<p class=\'empty-hint\'>在左侧编辑器中输入内容即可实时预览...</p>'"></div>
                  </div>
                </div>
              </div>
            </a-form-item>

            <a-form-item label="题目答案" name="answer">
              <div class="split-editor-container">
                <div class="split-editor-left">
                  <div class="editor-header">
                    <span class="editor-title">✏️ 编辑器</span>
                  </div>
                  <a-textarea
                    v-model:value="questionForm.answer"
                    :rows="10"
                    placeholder="请输入题目答案（支持Markdown语法）"
                    class="split-textarea"
                  />
                </div>
                <div class="split-editor-right">
                  <div class="editor-header">
                    <span class="editor-title">👁️ 实时预览</span>
                  </div>
                  <div class="split-preview">
                    <div class="markdown-preview" v-html="renderMarkdown(questionForm.answer) || '<p class=\'empty-hint\'>在左侧编辑器中输入答案即可实时预览...</p>'"></div>
                  </div>
                </div>
              </div>
            </a-form-item>

            <a-form-item label="标签" name="tags">
              <a-input v-model:value="questionForm.tags" placeholder="请输入标签，多个标签用逗号分隔" />
            </a-form-item>

            <!-- 题库选择（仅在添加题目时显示） -->
            <a-form-item v-if="!isEdit" label="所属题库" name="selectedQuestionBanks" :rules="[{ required: true, message: '请至少选择一个题库' }]">
              <a-select
                v-model:value="questionForm.selectedQuestionBanks"
                mode="multiple"
                placeholder="请选择题目所属的题库"
                :options="questionBanks.map(bank => ({ label: bank.title, value: bank.id }))"
                style="width: 100%"
              />
            </a-form-item>

            <a-form-item class="form-actions">
              <a-space>
                <a-button
                  type="primary"
                  @click="handleSubmit"
                  :loading="loading"
                  size="large"
                >
                  {{ isEdit ? '保存修改' : '创建题目' }}
                </a-button>
                <a-button @click="backToList" size="large">
                  取消
                </a-button>
              </a-space>
            </a-form-item>
          </a-form>
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
import { useRouter, useRoute } from 'vue-router';
import { useStore } from 'vuex';
import { getQuestionById, addQuestion, updateQuestion } from '../api/question';
import { getQuestionBankList, addQuestionBankQuestion } from '../api/questionBank';

export default {
  name: 'QuestionEdit',
  setup() {
    const router = useRouter();
    const route = useRoute();
    const store = useStore();
    const loading = ref(false);
    const formRef = ref();

    const isEdit = ref(false);
    const questionBanks = ref([]);

    const questionForm = reactive({
      id: null,
      title: '',
      content: '',
      answer: '',
      tags: '',
      userId: null,
      selectedQuestionBanks: []
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

    // 获取题库列表
    const fetchQuestionBanks = async () => {
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
      }
    };

    // 获取题目详情
    const fetchQuestionDetail = async (id) => {
      loading.value = true;
      try {
        const response = await getQuestionById(id);
        if (response.code === 200) {
          Object.assign(questionForm, response.data);
          // 将标签数组转换为逗号分隔的字符串
          questionForm.tags = Array.isArray(response.data.tags) ? response.data.tags.join(', ') : response.data.tags || '';
        } else {
          message.error(response.message || '获取题目详情失败');
          router.push('/questions');
        }
      } catch (error) {
        console.error('获取题目详情错误:', error);
        message.error('获取题目详情失败，请检查网络连接');
        router.push('/questions');
      } finally {
        loading.value = false;
      }
    };

    // 返回列表
    const backToList = () => {
      router.push('/questions');
    };

    // 提交表单
    const handleSubmit = async () => {
      try {
        await formRef.value.validate();

        // 表单验证会自动检查题库选择（验证字段名为selectedQuestionBanks）

        // 将标签字符串转换为逗号分隔的字符串（后端期望字符串格式）
        const tagsString = questionForm.tags ? questionForm.tags.split(',').map(tag => tag.trim()).filter(tag => tag).join(',') : '';

        // 确保userId正确设置
        const userId = store.state.user?.id || store.state.user?.userId;
        if (!userId) {
          message.error('用户未登录，无法操作题目');
          return;
        }

        const data = {
          title: questionForm.title,
          content: questionForm.content,
          answer: questionForm.answer,
          tags: tagsString,
          userId: userId
        };

        loading.value = true;
        let response;
        if (isEdit.value) {
          // 编辑题目
          response = await updateQuestion(questionForm.id, data);
        } else {
          // 添加题目
          response = await addQuestion(data);
        }

        if (response.code === 200) {
          // 如果是添加题目，需要建立题目与题库的关联
          if (!isEdit.value && questionForm.selectedQuestionBanks.length > 0) {
            const questionId = response.data;
            // 为每个选中的题库创建关联
            const associationPromises = questionForm.selectedQuestionBanks.map(questionBankId =>
              addQuestionBankQuestion({
                questionBankId,
                questionId,
                userId
              })
            );

            try {
              await Promise.all(associationPromises);
              message.success('题目创建成功，并已关联到选中的题库');
            } catch (error) {
              console.error('创建题库题目关联失败:', error);
              message.warning('题目创建成功，但题库关联失败');
            }
          } else {
            message.success(isEdit.value ? '编辑成功' : '创建成功');
          }
          router.push('/questions');
        } else {
          message.error(response.message || (isEdit.value ? '编辑失败' : '创建失败'));
        }
      } catch (error) {
        console.log('表单验证失败:', error);
      } finally {
        loading.value = false;
      }
    };

    const handleLogout = () => {
      store.dispatch('logout');
      message.success('已退出登录');
      router.push('/login');
    };

    onMounted(() => {
      const id = route.params.id;
      if (id) {
        isEdit.value = true;
        fetchQuestionDetail(id);
      } else {
        // 设置当前用户ID
        const userId = store.state.user?.id || store.state.user?.userId;
        if (userId) {
          questionForm.userId = userId;
        }
        // 初始化题库选择字段
        questionForm.selectedQuestionBanks = [];

        // 检查是否有题库ID参数（从题库详情页面跳转过来）
        const questionBankId = route.query.questionBankId;
        if (questionBankId) {
          questionForm.selectedQuestionBanks = [parseInt(questionBankId)];
        }

        // 获取题库列表
        fetchQuestionBanks();
      }
    });

    return {
      loading,
      isEdit,
      questionForm,
      rules,
      formRef,
      questionBanks,
      backToList,
      handleSubmit,
      handleLogout,
      renderMarkdown
    };
  }
}
</script>

<style scoped>
.question-edit-container {
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
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0;
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

.btn-icon {
  margin-right: 6px;
}

/* 编辑表单 */
.edit-form-container {
  max-width: 1000px;
  margin: 0 auto;
}

.edit-form {
  background: #fff;
  border-radius: 8px;
  padding: 0;
}

/* 分屏编辑器样式 */
.split-editor-container {
  display: flex;
  gap: 16px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.split-editor-left,
.split-editor-right {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.split-editor-left {
  border-right: 1px solid #e8e8e8;
}

.editor-header {
  background: linear-gradient(135deg, #FFF5E6 0%, #FFE8CC 100%);
  padding: 12px 16px;
  border-bottom: 1px solid #e8e8e8;
  font-weight: 600;
  color: #333;
}

.editor-title {
  font-size: 14px;
}

.split-textarea {
  flex: 1;
  border: none;
  resize: none;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
  padding: 16px;
}

.split-textarea:focus {
  box-shadow: none;
  outline: none;
}

.split-preview {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: #fafafa;
}

.markdown-preview {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  line-height: 1.6;
  color: #333;
  min-height: 100px;
}

.markdown-preview .empty-hint {
  color: #999;
  font-style: italic;
  text-align: center;
  margin-top: 40px;
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

/* 加载提示 */
.loading-alert {
  margin-bottom: 24px;
}

/* 表单操作 */
.form-actions {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
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

  .split-editor-container {
    flex-direction: column;
  }

  .split-editor-left {
    border-right: none;
    border-bottom: 1px solid #e8e8e8;
  }
}
</style>