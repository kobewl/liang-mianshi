<template>
  <div class="question-detail-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <span class="logo-icon">🦆</span>
          <span class="logo-text">面试鸭刷题神器</span>
        </div>

        <nav class="nav-menu">
          <a-button type="link" @click="$router.push('/')">主页</a-button>
          <a-button type="link" @click="scrollToQuestionBank">题库</a-button>
          <a-button type="link">刷题路线</a-button>
          <a-button type="link">26届秋招热题</a-button>
          <a-button type="link">真实面经</a-button>
          <a-button type="link" v-if="isAdmin" @click="$router.push('/questions')">题目管理</a-button>
          <a-button type="link" v-if="isAdmin" @click="$router.push('/question-banks')">题库管理</a-button>
        </nav>

        <div class="header-right">
          <a-input-search
            placeholder="搜索题目"
            style="width: 200px"
            class="search-input"
            @search="handleSearch"
          />
          <a-dropdown v-if="user">
            <a-button type="text" class="user-btn">
              <span class="user-icon">👤</span>
              <span>{{ user.userName }}</span>
            </a-button>
            <template #overlay>
              <a-menu>
                <a-menu-item key="1" @click="$router.push('/personal-center')">
                  <span>👤 个人中心</span>
                </a-menu-item>
                <a-menu-item key="2" v-if="isAdmin" @click="$router.push('/users')">
                  <span>👥 用户管理</span>
                </a-menu-item>
                <a-menu-item key="3" v-if="isAdmin" @click="$router.push('/questions')">
                  <span>📝 题目管理</span>
                </a-menu-item>
                <a-menu-item key="4" v-if="isAdmin" @click="$router.push('/question-banks')">
                  <span>📚 题库管理</span>
                </a-menu-item>
                <a-menu-divider v-if="isAdmin" />
                <a-menu-item key="5" @click="handleLogout">
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
        <!-- 题目头部信息 -->
        <div class="question-header" v-if="question">
          <div class="question-meta">
            <h1 class="question-title">{{ question.title }}</h1>
            <div class="question-tags">
              <a-tag v-for="tag in question.tags" :key="tag" color="blue">{{ tag }}</a-tag>
            </div>
            <div class="question-info">
              <span class="difficulty" :class="getDifficultyClass(question.difficulty)">
                {{ getDifficultyText(question.difficulty) }}
              </span>
              <!-- 管理员可以看到创建时间，普通用户看不到 -->
              <span class="create-time" v-if="isAdmin">{{ formatDate(question.createTime) }}</span>
            </div>
          </div>
          <div class="question-actions">
            <a-space>
              <a-button type="primary" @click="showAnswer = !showAnswer">
                {{ showAnswer ? '隐藏答案' : '查看答案' }}
              </a-button>
              <a-button @click="toggleFavorite">
                <span class="btn-icon">⭐</span>
                {{ isFavorite ? '已收藏' : '收藏' }}
              </a-button>
              <a-button @click="shareQuestion">
                <span class="btn-icon">🔗</span>
                分享
              </a-button>
            </a-space>
          </div>
        </div>

        <div class="content-layout">
          <!-- 左侧题目内容 -->
          <div class="left-content">
            <!-- 题目内容 -->
            <div class="question-content" v-if="question">
              <div class="content-section">
                <h3>题目描述</h3>
                <div class="markdown-content" v-html="renderMarkdown(question.content)"></div>
              </div>

              <!-- 答案区域 -->
              <div class="answer-section" v-if="showAnswer">
                <h3>参考答案</h3>
                <div class="markdown-content" v-html="renderMarkdown(question.answer)"></div>
              </div>

              <!-- 答案解析 -->
              <div class="analysis-section" v-if="showAnswer && question.analysis">
                <h3>答案解析</h3>
                <div class="markdown-content" v-html="renderMarkdown(question.analysis)"></div>
              </div>

              <!-- 扩展知识 -->
              <div class="extension-section" v-if="question.extension">
                <h3>扩展知识</h3>
                <div class="markdown-content" v-html="renderMarkdown(question.extension)"></div>
              </div>
            </div>
          </div>

          <!-- 右侧侧边栏 -->
          <div class="right-sidebar">
            <!-- 题目目录 -->
            <div class="sidebar-section">
              <h4>目录</h4>
              <div class="toc-list">
                <div
                  v-for="item in tocItems"
                  :key="item.id"
                  class="toc-item"
                  :class="{ active: activeSection === item.id }"
                  @click="scrollToSection(item.id)"
                >
                  {{ item.title }}
                </div>
              </div>
            </div>

            <!-- 回答重点 -->
            <div class="sidebar-section" v-if="question && question.keyPoints">
              <h4>回答重点</h4>
              <div class="key-points">
                <div
                  v-for="(point, index) in question.keyPoints"
                  :key="index"
                  class="key-point"
                >
                  <span class="point-index">{{ index + 1 }}</span>
                  <span class="point-content">{{ point }}</span>
                </div>
              </div>
            </div>

            <!-- 相关题目推荐 -->
            <div class="sidebar-section" v-if="relatedQuestions.length > 0">
              <h4>相关题目</h4>
              <div class="related-questions">
                <div
                  v-for="item in relatedQuestions"
                  :key="item.id"
                  class="related-item"
                  @click="goToQuestion(item.id)"
                >
                  {{ item.title }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 题目导航 -->
        <div class="question-navigation" v-if="question">
          <a-space>
            <a-button
              :disabled="!hasPrev"
              @click="goToPrevQuestion"
              class="nav-btn"
            >
              <span class="btn-icon">⬅️</span>
              上一题
            </a-button>

            <!-- 管理员可以看到提交答案按钮，普通用户看不到 -->
            <a-button
              v-if="isAdmin"
              type="primary"
              @click="submitAnswer"
              :loading="submitting"
            >
              提交答案
            </a-button>

            <!-- 普通用户看到学习完成按钮 -->
            <a-button
              v-else
              type="primary"
              @click="markAsCompleted"
            >
              <span class="btn-icon">✅</span>
              标记完成
            </a-button>

            <a-button
              :disabled="!hasNext"
              @click="goToNextQuestion"
              class="nav-btn"
            >
              下一题
              <span class="btn-icon">➡️</span>
            </a-button>
          </a-space>
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
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
import { message } from 'ant-design-vue';
import { useRouter, useRoute } from 'vue-router';
import { useStore } from 'vuex';
import { getQuestionById } from '../api/question';

export default {
  name: 'QuestionDetail',
  setup() {
    const router = useRouter();
    const route = useRoute();
    const store = useStore();

    const user = computed(() => store.state.user);
    const isAdmin = computed(() => user.value?.userRole === 'admin');

    const question = ref(null);
    const showAnswer = ref(false);
    const submitting = ref(false);
    const isFavorite = ref(false);

    // 目录项
    const tocItems = ref([
      { id: 'question', title: '题目描述' },
      { id: 'answer', title: '参考答案' },
      { id: 'analysis', title: '答案解析' },
      { id: 'extension', title: '扩展知识' }
    ]);

    const activeSection = ref('question');
    const relatedQuestions = ref([]);

    // 计算属性
    const hasPrev = computed(() => {
      // 根据题库中的题目顺序判断是否有上一题
      return false; // 暂时设为false，需要从题库数据中获取
    });

    const hasNext = computed(() => {
      // 根据题库中的题目顺序判断是否有下一题
      return false; // 暂时设为false，需要从题库数据中获取
    });

    // 获取题目详情
    const fetchQuestionDetail = async (id) => {
      try {
        const response = await getQuestionById(id);
        if (response.code === 200) {
          question.value = response.data;
          // 模拟相关题目数据
          relatedQuestions.value = [
            { id: 1, title: 'Java中HashMap的原理?' },
            { id: 2, title: '什么是JDK 1.8对HashMap进行了红黑树优化?' },
            { id: 3, title: 'JDK 1.8对HashMap除了红黑树还有什么优化?' }
          ];
        } else {
          message.error(response.message || '获取题目详情失败');
        }
      } catch (error) {
        console.error('获取题目详情错误:', error);
        message.error('获取题目详情失败，请检查网络连接');
      }
    };

    // 格式化日期
    const formatDate = (dateTime) => {
      if (!dateTime) return '';
      return new Date(dateTime).toLocaleString('zh-CN');
    };

    // 获取难度样式类
    const getDifficultyClass = (difficulty) => {
      const classMap = {
        '简单': 'easy',
        '中等': 'medium',
        '困难': 'hard'
      };
      return classMap[difficulty] || 'medium';
    };

    // 获取难度文本
    const getDifficultyText = (difficulty) => {
      return difficulty || '中等';
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

    // 切换收藏状态
    const toggleFavorite = () => {
      isFavorite.value = !isFavorite.value;
      message.success(isFavorite.value ? '已收藏' : '已取消收藏');
    };

    // 分享题目
    const shareQuestion = () => {
      if (navigator.share) {
        navigator.share({
          title: question.value.title,
          text: '快来看看这道题目吧！',
          url: window.location.href
        });
      } else {
        // 复制链接到剪贴板
        navigator.clipboard.writeText(window.location.href);
        message.success('链接已复制到剪贴板');
      }
    };

    // 提交答案（管理员功能）
    const submitAnswer = () => {
      submitting.value = true;
      // 模拟提交答案
      setTimeout(() => {
        submitting.value = false;
        message.success('答案提交成功！');
      }, 1000);
    };

    // 标记完成（普通用户功能）
    const markAsCompleted = () => {
      message.success('题目已标记为完成！');
      // 这里可以添加将题目添加到用户完成列表的逻辑
    };

    // 跳转到上一题
    const goToPrevQuestion = () => {
      // 需要从题库数据中获取上一题ID
      message.info('暂无上一题');
    };

    // 跳转到下一题
    const goToNextQuestion = () => {
      // 需要从题库数据中获取下一题ID
      message.info('暂无下一题');
    };

    // 跳转到指定题目
    const goToQuestion = (id) => {
      router.push(`/question/${id}`);
    };

    // 滚动到指定部分
    const scrollToSection = (sectionId) => {
      activeSection.value = sectionId;
      const element = document.getElementById(sectionId);
      if (element) {
        element.scrollIntoView({ behavior: 'smooth' });
      }
    };

    // 搜索题目
    const handleSearch = (value) => {
      message.info(`搜索: ${value}`);
    };

    // 退出登录
    const handleLogout = () => {
      store.dispatch('logout');
      message.success('已退出登录');
      router.push('/login');
    };

    // 滚动到题库区域
    const scrollToQuestionBank = () => {
      // 可以跳转到题库页面或滚动到题库区域
      router.push('/');
    };

    // 监听滚动事件，更新活动部分
    let scrollTimer = null;
    const handleScroll = () => {
      if (scrollTimer) clearTimeout(scrollTimer);
      scrollTimer = setTimeout(() => {
        const sections = ['question', 'answer', 'analysis', 'extension'];
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;

        for (const section of sections) {
          const element = document.getElementById(section);
          if (element) {
            const offsetTop = element.offsetTop - 100;
            const offsetHeight = element.offsetHeight;

            if (scrollTop >= offsetTop && scrollTop < offsetTop + offsetHeight) {
              activeSection.value = section;
              break;
            }
          }
        }
      }, 100);
    };

    onMounted(() => {
      const questionId = route.params.id;
      if (questionId) {
        fetchQuestionDetail(questionId);
      }

      // 添加滚动监听
      window.addEventListener('scroll', handleScroll);
    });

    onUnmounted(() => {
      // 移除滚动监听
      window.removeEventListener('scroll', handleScroll);
      if (scrollTimer) clearTimeout(scrollTimer);
    });

    return {
      user,
      isAdmin,
      question,
      showAnswer,
      submitting,
      isFavorite,
      tocItems,
      activeSection,
      relatedQuestions,
      hasPrev,
      hasNext,
      formatDate,
      getDifficultyClass,
      getDifficultyText,
      renderMarkdown,
      toggleFavorite,
      shareQuestion,
      submitAnswer,
      markAsCompleted,
      goToPrevQuestion,
      goToNextQuestion,
      goToQuestion,
      scrollToSection,
      handleSearch,
      handleLogout,
      scrollToQuestionBank
    };
  }
}
</script>

<style scoped>
.question-detail-container {
  height: 100vh;
  width: 100vw;
  background: #f8f9fa;
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

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-input :deep(.ant-input) {
  border-radius: 20px;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #333;
}

.user-icon {
  font-size: 18px;
}

/* 主要内容 */
.main-content {
  flex: 1;
  background: #f8f9fa;
  overflow-y: auto;
  padding: 24px;
}

.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  min-height: calc(100vh - 180px);
}

/* 题目头部 */
.question-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.question-meta {
  flex: 1;
}

.question-title {
  font-size: 32px;
  font-weight: 700;
  color: #333;
  margin: 0 0 16px 0;
  line-height: 1.3;
}

.question-tags {
  margin-bottom: 16px;
}

.question-info {
  display: flex;
  gap: 16px;
  align-items: center;
}

.difficulty {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.difficulty.easy {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.difficulty.medium {
  background: #fff7e6;
  color: #fa8c16;
  border: 1px solid #ffd591;
}

.difficulty.hard {
  background: #fff1f0;
  color: #f5222d;
  border: 1px solid #ffccc7;
}

.create-time {
  color: #999;
  font-size: 14px;
}

.question-actions {
  display: flex;
  gap: 12px;
}

.btn-icon {
  margin-right: 4px;
}

/* 内容布局 */
.content-layout {
  display: flex;
  gap: 32px;
}

.left-content {
  flex: 1;
}

.right-sidebar {
  width: 280px;
  flex-shrink: 0;
}

/* 内容区域 */
.content-section,
.answer-section,
.analysis-section,
.extension-section {
  margin-bottom: 32px;
}

.content-section h3,
.answer-section h3,
.analysis-section h3,
.extension-section h3 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #f0f0f0;
}

.markdown-content {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
}

.markdown-content h1,
.markdown-content h2,
.markdown-content h3,
.markdown-content h4,
.markdown-content h5,
.markdown-content h6 {
  margin: 24px 0 16px 0;
  color: #262626;
}

.markdown-content p {
  margin: 12px 0;
}

.markdown-content code {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
}

.markdown-content pre {
  background: #f6f8fa;
  padding: 16px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 16px 0;
}

.markdown-content pre code {
  background: none;
  padding: 0;
}

.markdown-content blockquote {
  border-left: 4px solid #1890ff;
  padding-left: 16px;
  margin: 16px 0;
  color: #666;
}

/* 侧边栏 */
.sidebar-section {
  margin-bottom: 32px;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.sidebar-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #e8e8e8;
}

.toc-list {
  max-height: 300px;
  overflow-y: auto;
}

.toc-item {
  padding: 8px 12px;
  margin-bottom: 4px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
}

.toc-item:hover {
  background: #e8e8e8;
  color: #333;
}

.toc-item.active {
  background: #1890ff;
  color: white;
}

.key-points {
  max-height: 200px;
  overflow-y: auto;
}

.key-point {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  padding: 8px;
  background: white;
  border-radius: 4px;
  border-left: 3px solid #1890ff;
}

.point-index {
  background: #1890ff;
  color: white;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.point-content {
  font-size: 14px;
  color: #333;
  line-height: 1.4;
}

.related-questions {
  max-height: 200px;
  overflow-y: auto;
}

.related-item {
  padding: 8px 12px;
  margin-bottom: 4px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #1890ff;
  transition: all 0.3s;
}

.related-item:hover {
  background: #e6f7ff;
  color: #0050b3;
}

/* 题目导航 */
.question-navigation {
  display: flex;
  justify-content: center;
  margin-top: 48px;
  padding-top: 32px;
  border-top: 1px solid #f0f0f0;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 8px;
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
@media (max-width: 1200px) {
  .content-layout {
    flex-direction: column;
  }

  .right-sidebar {
    width: 100%;
  }
}

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

  .question-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .question-title {
    font-size: 24px;
  }

  .question-actions {
    justify-content: center;
  }
}
</style>
