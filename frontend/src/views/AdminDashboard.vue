<template>
  <PageLayout
    variant="admin"
    :nav-items="navItems"
    :hero="hero"
    @nav-click="handleNavClick"
  >
    <a-spin :spinning="loading">
      <section class="dashboard">
        <div class="stat-grid">
          <div
            v-for="card in statCards"
            :key="card.key"
            class="stat-card glass-card"
          >
            <div class="stat-icon" :style="{ background: card.gradient }">
              <span>{{ card.icon }}</span>
            </div>
            <div class="stat-meta">
              <span class="stat-label">{{ card.label }}</span>
              <span class="stat-value">{{ formatNumber(card.value) }}</span>
              <span class="stat-subtitle">{{ card.subtitle }}</span>
            </div>
          </div>
        </div>

        <div class="panel-grid">
          <div class="panel glass-card">
            <div class="panel-head">
              <div>
                <h3>快速操作</h3>
                <p>高频管理入口集中在此，提升处理效率。</p>
              </div>
            </div>
            <div class="actions">
              <a-button type="primary" shape="round" block @click="router.push('/questions/create')">
                新建题目
              </a-button>
              <a-button shape="round" block @click="router.push('/question-banks/create')">
                创建题库
              </a-button>
              <a-button shape="round" block @click="router.push('/users')">
                管理用户
              </a-button>
            </div>
            <div class="system-notes">
              <h4>系统健康状况</h4>
              <ul>
                <li>
                  <span class="dot dot--success"></span>
                  题库与题目同步正常
                </li>
                <li>
                  <span class="dot dot--info"></span>
                  最近同步的题目 {{ formatNumber(latestQuestions.length) }} 条
                </li>
                <li>
                  <span class="dot dot--warning"></span>
                  建议完成题库封面素材补齐，以提升展示质感
                </li>
              </ul>
            </div>
          </div>

          <div class="panel glass-card">
            <div class="panel-head">
              <div>
                <h3>团队动态</h3>
                <p>掌握成员活跃度，合理分配协作任务。</p>
              </div>
              <a-button type="link" @click="router.push('/users')">查看全部</a-button>
            </div>
            <div v-if="!latestUsers.length" class="empty">
              <a-empty description="暂未获取到用户数据" />
            </div>
            <ul v-else class="list">
              <li v-for="userItem in latestUsers" :key="userItem.id" class="list-item">
                <div class="list-main">
                  <h4>{{ userItem.userName || userItem.userAccount }}</h4>
                  <p>角色：{{ renderRole(userItem.userRole) }}</p>
                </div>
                <span class="list-meta">{{ formatDate(userItem.createTime) }}</span>
              </li>
            </ul>
          </div>
        </div>

        <div class="list-grid">
          <div class="list-card glass-card">
            <div class="panel-head">
              <div>
                <h3>最近更新题库</h3>
                <p>掌握核心题库的维护节奏。</p>
              </div>
              <a-button type="link" @click="router.push('/question-banks')">前往管理</a-button>
            </div>
            <div v-if="!latestQuestionBanks.length" class="empty">
              <a-empty description="还没有题库数据" />
            </div>
            <ul v-else class="list">
              <li
                v-for="bank in latestQuestionBanks"
                :key="bank.id"
                class="list-item"
                @click="router.push(`/question-banks/edit/${bank.id}`)"
              >
                <div class="list-main">
                  <h4>{{ bank.title }}</h4>
                  <p>{{ summarize(bank.description) }}</p>
                </div>
                <span class="list-meta">{{ formatDate(bank.updateTime || bank.createTime) }}</span>
              </li>
            </ul>
          </div>

          <div class="list-card glass-card">
            <div class="panel-head">
              <div>
                <h3>最新题目</h3>
                <p>跟踪内容素材，及时完成质检。</p>
              </div>
              <a-button type="link" @click="router.push('/questions')">查看题目列表</a-button>
            </div>
            <div v-if="!latestQuestions.length" class="empty">
              <a-empty description="还没有新增题目" />
            </div>
            <ul v-else class="list">
              <li
                v-for="question in latestQuestions"
                :key="question.id"
                class="list-item"
                @click="router.push(`/questions/edit/${question.id}`)"
              >
                <div class="list-main">
                  <h4>{{ question.title }}</h4>
                  <p>{{ summarize(question.content) }}</p>
                </div>
                <span class="list-meta">{{ formatDate(question.updateTime || question.createTime) }}</span>
              </li>
            </ul>
          </div>
        </div>
      </section>
    </a-spin>
  </PageLayout>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { message } from 'ant-design-vue';
import PageLayout from '../components/layout/PageLayout.vue';
import { getQuestionList } from '../api/question';
import { getQuestionBankList } from '../api/questionBank';
import { getUserList } from '../api/user';

const router = useRouter();
const store = useStore();

const loading = ref(false);
const stats = reactive({
  questions: 0,
  questionBanks: 0,
  users: 0
});

const latestQuestions = ref([]);
const latestQuestionBanks = ref([]);
const latestUsers = ref([]);

const navItems = [
  { key: 'admin-home', label: '首页', path: '/admin' },
  { key: 'question-manage', label: '题目列表', path: '/questions' },
  { key: 'question-bank-manage', label: '题库管理', path: '/question-banks' },
  { key: 'user-manage', label: '用户管理', path: '/users' }
];

const user = computed(() => store.state.user);

const greeting = computed(() => {
  const hour = new Date().getHours();
  if (hour < 6) return '夜深了';
  if (hour < 12) return '早上好';
  if (hour < 14) return '中午好';
  if (hour < 18) return '下午好';
  return '晚上好';
});

const hero = computed(() => {
  const displayName = user.value?.userName || user.value?.userAccount || '管理员';
  return {
    badge: '管理驾驶舱',
    title: `${greeting.value}，${displayName}`,
    subtitle: '智能题库、实时 Markdown 状态同步、完整的题目管理让你的备战更高效。'
  };
});

const statCards = computed(() => [
  {
    key: 'questions',
    label: '题目总数',
    value: stats.questions,
    subtitle: '涵盖常见面试知识模块',
    icon: '📚',
    gradient: 'linear-gradient(135deg, rgba(59,130,246,0.22), rgba(37,99,235,0.35))'
  },
  {
    key: 'questionBanks',
    label: '题库总数',
    value: stats.questionBanks,
    subtitle: '组合式题库助力高效复盘',
    icon: '🗂️',
    gradient: 'linear-gradient(135deg, rgba(16,185,129,0.22), rgba(52,211,153,0.32))'
  },
  {
    key: 'users',
    label: '活跃用户',
    value: stats.users,
    subtitle: '保持团队协同稳定运转',
    icon: '👥',
    gradient: 'linear-gradient(135deg, rgba(250,204,21,0.22), rgba(251,191,36,0.35))'
  }
]);

const handleNavClick = (item) => {
  if (item.path) {
    router.push(item.path);
  }
};

const renderRole = (role) => {
  if (!role) return '未知';
  const normalized = role.toLowerCase();
  if (normalized === 'admin') return '管理员';
  if (normalized === 'user') return '普通用户';
  return role;
};

const summarize = (text, length = 48) => {
  if (!text) return '暂无描述';
  const plain = text.replace(/<[^>]+>/g, '');
  return plain.length > length ? `${plain.slice(0, length)}...` : plain;
};

const formatDate = (value) => {
  if (!value) return '刚刚';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '刚刚';
  return date.toLocaleString();
};

const formatNumber = (value) => {
  return Number(value || 0).toLocaleString();
};

const fetchDashboard = async () => {
  loading.value = true;
  try {
    const [questionRes, bankRes, userRes] = await Promise.allSettled([
      getQuestionList({ current: 1, size: 4 }),
      getQuestionBankList({ current: 1, size: 4 }),
      getUserList({ current: 1, size: 4 })
    ]);

    if (questionRes.status === 'fulfilled' && questionRes.value?.code === 200) {
      stats.questions = questionRes.value.data?.total ?? 0;
      latestQuestions.value = questionRes.value.data?.records ?? [];
    } else if (questionRes.status === 'fulfilled') {
      message.warning(questionRes.value?.message || '获取题目数据失败');
    }

    if (bankRes.status === 'fulfilled' && bankRes.value?.code === 200) {
      stats.questionBanks = bankRes.value.data?.total ?? 0;
      latestQuestionBanks.value = bankRes.value.data?.records ?? [];
    } else if (bankRes.status === 'fulfilled') {
      message.warning(bankRes.value?.message || '获取题库数据失败');
    }

    if (userRes.status === 'fulfilled' && userRes.value?.code === 200) {
      stats.users = userRes.value.data?.total ?? 0;
      latestUsers.value = userRes.value.data?.records ?? [];
    } else if (userRes.status === 'fulfilled') {
      message.warning(userRes.value?.message || '获取用户数据失败');
    }
  } catch (error) {
    console.error('加载仪表盘数据失败:', error);
    message.error('加载仪表盘数据失败，请稍后再试');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchDashboard();
});
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 22px;
  border-radius: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 20px;
  display: grid;
  place-items: center;
  font-size: 28px;
  color: #fff;
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.12);
}

.stat-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-subtitle {
  font-size: 13px;
  color: var(--text-secondary);
}

.panel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.panel {
  padding: 24px;
  border-radius: 22px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.panel-head h3 {
  font-size: 20px;
  font-weight: 700;
}

.panel-head p {
  margin-top: 6px;
  color: var(--text-secondary);
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.system-notes {
  border-top: 1px solid rgba(148, 163, 184, 0.18);
  padding-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.system-notes h4 {
  font-size: 15px;
  font-weight: 600;
}

.system-notes ul {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-left: 0;
  list-style: none;
}

.system-notes li {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-secondary);
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.dot--success {
  background: #22c55e;
}

.dot--info {
  background: #2563eb;
}

.dot--warning {
  background: #f97316;
}

.list-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 20px;
}

.list-card {
  padding: 24px;
  border-radius: 22px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.list-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  padding-bottom: 12px;
  border-bottom: 1px dashed rgba(148, 163, 184, 0.2);
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease;
}

.list-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.list-item:hover {
  transform: translateX(4px);
  border-color: rgba(255, 122, 69, 0.3);
}

.list-main {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.list-main h4 {
  font-size: 16px;
  font-weight: 600;
}

.list-main p {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.4;
}

.list-meta {
  font-size: 12px;
  color: var(--text-tertiary, rgba(71, 85, 105, 0.65));
  white-space: nowrap;
}

.empty {
  padding: 24px 0;
  display: grid;
  place-items: center;
}

@media (max-width: 768px) {
  .stat-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .list-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .list-meta {
    margin-top: 4px;
  }
}
</style>
