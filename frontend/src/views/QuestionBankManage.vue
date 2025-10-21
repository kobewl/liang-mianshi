<template>
  <PageLayout
    :variant="isAdmin ? 'admin' : 'public'"
    :hero="hero"
    @nav-click="handleNavClick"
  >
    <a-spin :spinning="loading">
      <section v-if="isDetailView" class="detail-shell">
        <div class="bank-summary glass-card" v-if="questionBank">
          <div class="bank-summary__icon">📚</div>
          <div class="bank-summary__main">
            <h2>{{ questionBank.title }}</h2>
            <p>{{ questionBank.description || '这个题库还没有简介，快去补充一条吧~' }}</p>
            <div class="bank-summary__meta">
              <span>题目数量：{{ detailQuestions.length }}</span>
              <span>创建时间：{{ formatDate(questionBank.createTime) }}</span>
              <span>最近更新：{{ formatDate(questionBank.updateTime) }}</span>
            </div>
          </div>
          <div class="bank-summary__actions">
            <a-space>
              <a-button
                type="primary"
                shape="round"
                :disabled="!detailQuestions.length"
                @click="router.push(`/question/${detailQuestions[0]?.id}`)"
              >
                开始刷题
              </a-button>
              <a-button shape="round" @click="goBack">返回</a-button>
              <a-button
                v-if="isAdmin"
                shape="round"
                @click="router.push(`/question-banks/edit/${questionBank.id}`)"
              >
                编辑题库
              </a-button>
            </a-space>
          </div>
        </div>

        <div class="question-list-card glass-card">
          <div class="question-list__head">
            <div>
              <h3>题目列表</h3>
              <p>精选题目尽在此处，点击行即可查看详情并开始练习。</p>
            </div>
            <span class="question-count">共 {{ detailQuestions.length }} 道</span>
          </div>
          <a-empty
            v-if="!detailQuestions.length"
            description="题库中还没有题目"
          />
          <div v-else class="question-list">
            <div
              v-for="question in detailQuestions"
              :key="question.id"
              class="question-row"
              @click="router.push(`/question/${question.id}`)"
            >
              <div class="question-row__main">
                <h4>{{ question.title }}</h4>
                <p>{{ summarize(question.content) }}</p>
                <div
                  v-if="normalizeTags(question.tags).length"
                  class="question-row__tags"
                >
                  <a-tag
                    v-for="tag in normalizeTags(question.tags)"
                    :key="tag"
                    class="tag-pill"
                  >
                    {{ tag }}
                  </a-tag>
                </div>
              </div>
              <div class="question-row__meta">
                <span
                  :class="['question-difficulty', getDifficulty(question.difficulty).class]"
                >
                  {{ getDifficulty(question.difficulty).label }}
                </span>
                <span class="question-updated">
                  更新于：{{ formatDate(question.updateTime || question.createTime) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section v-else class="manage-shell glass-card">
        <div class="manage-head">
          <div>
            <h2>题库列表</h2>
            <p>统一维护题库，支持搜索、编辑与快速跳转到题库详情。</p>
          </div>
          <a-space>
            <a-button shape="round" @click="refresh">刷新</a-button>
            <a-button type="primary" shape="round" @click="router.push('/question-banks/create')">
              新建题库
            </a-button>
          </a-space>
        </div>

        <div class="filter-box">
          <a-row :gutter="[16, 16]">
            <a-col :xs="24" :sm="12" :md="10">
              <a-input
                v-model:value="query.title"
                placeholder="按标题搜索题库"
                allow-clear
                @pressEnter="refresh"
              >
                <template #prefix>
                  <span class="filter-icon">🔍</span>
                </template>
              </a-input>
            </a-col>
            <a-col :xs="24" :sm="12" :md="10">
              <a-input
                v-model:value="query.description"
                placeholder="按描述关键字搜索"
                allow-clear
                @pressEnter="refresh"
              />
            </a-col>
            <a-col :xs="24" :sm="24" :md="4">
              <a-space>
                <a-button shape="round" @click="resetFilters">重置</a-button>
                <a-button type="primary" shape="round" @click="refresh">应用</a-button>
              </a-space>
            </a-col>
          </a-row>
        </div>

        <a-table
          :columns="columns"
          :data-source="questionBanks"
          :loading="loading"
          row-key="id"
          :pagination="pagination"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'title'">
              <a-space direction="vertical" size="small">
                <a-button type="link" class="link-button" @click="openDetail(record.id)">{{ record.title }}</a-button>
                <span class="meta-text">{{ record.description || '暂无描述' }}</span>
              </a-space>
            </template>
            <template v-else-if="column.key === 'createTime'">
              {{ formatDate(record.createTime) }}
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a-button type="link" size="small" @click="router.push(`/question-banks/edit/${record.id}`)">编辑</a-button>
                <a-popconfirm
                  title="确认删除该题库？"
                  ok-text="删除"
                  cancel-text="取消"
                  @confirm="handleDelete(record.id)"
                >
                  <a-button type="link" danger size="small">删除</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
      </section>
    </a-spin>
  </PageLayout>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useStore } from 'vuex';
import { message } from 'ant-design-vue';
import PageLayout from '../components/layout/PageLayout.vue';
import {
  getQuestionBankList,
  deleteQuestionBank,
  getQuestionBankById
} from '../api/questionBank';
import { searchQuestionFromEs } from '../api/question';

const store = useStore();
const router = useRouter();
const route = useRoute();

const user = computed(() => store.state.user);
const isAdmin = computed(() => (user.value?.userRole || '').toLowerCase() === 'admin');

const loading = ref(false);

const questionBanks = ref([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total) => `共 ${total} 条`
});

const query = reactive({
  title: '',
  description: ''
});

const questionBank = ref(null);
const detailQuestions = ref([]);

const isDetailView = computed(() => Boolean(route.params.id));

const hero = computed(() => {
  if (isDetailView.value) {
    return null;
  }
  return {
    badge: '题库管理',
    title: '打造高质量题库体系',
    subtitle: '集中维护题库，确保分类清晰、内容及时、体验一致。'
  };
});

const columns = [
  { title: '题库', dataIndex: 'title', key: 'title' },
  { title: '题目数量', dataIndex: 'questionCount', key: 'questionCount', width: 120 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 160 },
  { title: '操作', key: 'action', width: 140 }
];

const fetchQuestionBanks = async () => {
  loading.value = true;
  try {
    const params = {
      current: pagination.current,
      size: pagination.pageSize,
      title: query.title || undefined,
      description: query.description || undefined
    };
    const response = await getQuestionBankList(params);
    if (response.code === 200) {
      questionBanks.value = response.data?.records || [];
      pagination.total = response.data?.total || 0;
    } else {
      message.error(response.message || '获取题库列表失败');
    }
  } catch (error) {
    console.error('获取题库列表失败:', error);
    message.error('获取题库列表失败，请稍后再试');
  } finally {
    loading.value = false;
  }
};

const fetchQuestionBankDetail = async (id) => {
  loading.value = true;
  try {
    const response = await getQuestionBankById(id);
    if (response.code === 200) {
      questionBank.value = response.data;
      await fetchDetailQuestions(id);
    } else {
      message.error(response.message || '获取题库详情失败');
      goBack();
    }
  } catch (error) {
    console.error('获取题库详情失败:', error);
    message.error('获取题库详情失败，请稍后再试');
    goBack();
  } finally {
    loading.value = false;
  }
};

const fetchDetailQuestions = async (questionBankId) => {
  try {
    const response = await searchQuestionFromEs({
      questionBankId,
      current: 1,
      size: 100
    });
    if (response.code === 200) {
      detailQuestions.value = response.data?.records || [];
    } else {
      message.error(response.message || '获取题库题目失败');
    }
  } catch (error) {
    console.error('获取题库题目失败:', error);
    message.error('获取题库题目失败，请稍后再试');
  }
};

const refresh = () => {
  pagination.current = 1;
  fetchQuestionBanks();
};

const resetFilters = () => {
  query.title = '';
  query.description = '';
  refresh();
};

const handleDelete = (id) => async () => {
  try {
    const response = await deleteQuestionBank(id);
    if (response.code === 200) {
      message.success('删除题库成功');
      refresh();
    } else {
      message.error(response.message || '删除失败');
    }
  } catch (error) {
    console.error('删除题库失败:', error);
    message.error('删除失败，请稍后再试');
  }
};

const handleTableChange = (pager) => {
  pagination.current = pager.current;
  pagination.pageSize = pager.pageSize;
  fetchQuestionBanks();
};

const openDetail = (id) => {
  router.push(`/question-bank/${id}`);
};

const goBack = () => {
  router.push('/question-banks');
};

const formatDate = (value) => {
  if (!value) return '刚刚';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '刚刚';
  return date.toLocaleString();
};

const normalizeTags = (tags) => {
  if (!tags) return [];
  if (Array.isArray(tags)) return tags;
  return tags.split(',').map((tag) => tag.trim()).filter(Boolean);
};


const difficultyLegend = {
  easy: { label: '简单', class: 'difficulty--easy' },
  medium: { label: '中等', class: 'difficulty--medium' },
  hard: { label: '困难', class: 'difficulty--hard' },
  default: { label: '未标记', class: 'difficulty--default' }
};

const getDifficulty = (value) => {
  if (!value) return difficultyLegend.default;
  const key = String(value).toLowerCase();
  return difficultyLegend[key] || difficultyLegend.default;
};

const summarize = (text = '') => {
  if (!text) return '暂无题目描述，点击卡片查看详情。';
  const plain = text.replace(/<[^>]+>/g, '').replace(/\n/g, ' ');
  return plain.length > 120 ? `${plain.slice(0, 120)}...` : plain;
};

const handleNavClick = (item) => {
  if (item.path) {
    router.push(item.path);
    return;
  }
  if (item.event === 'scroll-question-banks') {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
};

watch(
  () => route.params.id,
  (id) => {
    if (id) {
      fetchQuestionBankDetail(id);
    } else {
      questionBank.value = null;
      detailQuestions.value = [];
      fetchQuestionBanks();
    }
  },
  { immediate: true }
);

onMounted(() => {
  if (!route.params.id) {
    fetchQuestionBanks();
  }
});
</script>

<style scoped>
.detail-shell {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.bank-summary {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 24px 28px;
}

.bank-summary__icon {
  width: 72px;
  height: 72px;
  border-radius: 24px;
  display: grid;
  place-items: center;
  font-size: 32px;
  box-shadow: 0 12px 28px rgba(59, 130, 246, 0.18);
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.18), rgba(99, 102, 241, 0.28));
}

.bank-summary__main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.bank-summary__main h2 {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
}

.bank-summary__main p {
  margin: 0;
  color: var(--text-secondary);
}

.bank-summary__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 14px;
  color: var(--text-secondary);
}

.bank-summary__actions {
  display: flex;
  align-items: center;
}

.question-list-card {
  padding: 20px 24px 12px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.question-list__head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.question-list__head h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.question-list__head p {
  margin: 6px 0 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.question-count {
  font-size: 14px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.question-list {
  border-top: 1px solid rgba(148, 163, 184, 0.2);
  display: flex;
  flex-direction: column;
}

.question-row {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 18px 0;
  border-bottom: 1px solid rgba(148, 163, 184, 0.14);
  cursor: pointer;
  transition: var(--transition-base);
}

.question-row:hover {
  background: rgba(59, 130, 246, 0.06);
  transform: translateY(-2px);
}

.question-row__main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.question-row__main h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.question-row__main p {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.question-row__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.question-row__meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  min-width: 160px;
}

.question-difficulty {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  background: rgba(148, 163, 184, 0.18);
  color: var(--text-secondary);
}

.difficulty--default {
  background: rgba(148, 163, 184, 0.18);
  color: var(--text-secondary);
}

.difficulty--easy {
  background: rgba(16, 185, 129, 0.18);
  color: #0f766e;
}

.difficulty--medium {
  background: rgba(250, 204, 21, 0.22);
  color: #92400e;
}

.difficulty--hard {
  background: rgba(239, 68, 68, 0.18);
  color: #b91c1c;
}

.question-updated {
  font-size: 12px;
  color: var(--text-tertiary, rgba(71, 85, 105, 0.85));
}

@media (max-width: 992px) {
  .bank-summary {
    flex-direction: column;
    align-items: flex-start;
    text-align: left;
  }

  .bank-summary__actions {
    width: 100%;
    justify-content: flex-start;
  }

  .question-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .question-row__meta {
    flex-direction: row;
    align-items: center;
    gap: 12px;
    min-width: auto;
  }
}

@media (max-width: 640px) {
  .question-list-card {
    padding: 18px 18px 10px;
  }

  .question-list__head {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .question-count {
    align-self: flex-start;
  }
}
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

.filter-icon {
  font-size: 16px;
}

.link-button {
  padding: 0;
  height: auto;
}

.meta-text {
  font-size: 12px;
  color: var(--text-secondary);
}

@media (max-width: 768px) {
  .manage-shell {
    padding: 24px;
  }

  .manage-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .info-head {
    flex-direction: column;
  }
}
</style>
