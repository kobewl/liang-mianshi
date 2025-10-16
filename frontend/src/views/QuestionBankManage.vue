<template>
  <PageLayout
    :variant="isAdmin ? 'admin' : 'public'"
    :hero="hero"
    @nav-click="handleNavClick"
  >
    <a-spin :spinning="loading">
      <section v-if="isDetailView" class="detail-shell">
        <div class="info-card glass-card" v-if="questionBank">
          <div class="info-head">
            <div>
              <h2>{{ questionBank.title }}</h2>
              <p>{{ questionBank.description || '这个题库还没有简介，快去补充一条吧。' }}</p>
            </div>
            <div class="info-meta">
              <span>题目数量：{{ detailQuestions.length }}</span>
              <span>创建时间：{{ formatDate(questionBank.createTime) }}</span>
              <span>最近更新：{{ formatDate(questionBank.updateTime) }}</span>
            </div>
          </div>
          <div class="info-actions">
            <a-space>
              <a-button shape="round" @click="goBack">返回</a-button>
              <a-button v-if="isAdmin" type="primary" shape="round" @click="router.push(`/question-banks/edit/${questionBank.id}`)">
                编辑题库
              </a-button>
            </a-space>
          </div>
        </div>

        <div class="question-cards">
          <a-empty v-if="!detailQuestions.length" description="题库中还没有题目" />
          <a-card
            v-for="question in detailQuestions"
            :key="question.id"
            class="question-card"
            hoverable
            @click="router.push(`/question/${question.id}`)"
          >
            <div class="question-card__head">
              <h3>{{ question.title }}</h3>
              <a-tag v-for="tag in normalizeTags(question.tags)" :key="tag" class="tag-pill">
                {{ tag }}
              </a-tag>
            </div>
            <p>{{ summarize(question.content) }}</p>
          </a-card>
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
import { getQuestionList } from '../api/question';

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
    return {
      badge: '题库详情',
      title: questionBank.value?.title || '题库加载中...',
      subtitle: '查看题库下的题目，掌握更新情况与题库亮点。'
    };
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
    const response = await getQuestionList({
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

.info-card {
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
}

.info-head h2 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 12px;
}

.info-head p {
  color: var(--text-secondary);
  max-width: 520px;
}

.info-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 14px;
  color: var(--text-secondary);
}

.info-actions {
  display: flex;
  justify-content: flex-end;
}

.question-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.question-card {
  border-radius: 18px;
  border: none;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  box-shadow: var(--shadow-soft);
  transition: var(--transition-base);
}

.question-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-hover);
}

.question-card__head {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
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
