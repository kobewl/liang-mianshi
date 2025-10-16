<template>
  <PageLayout
    variant="admin"
    :nav-items="navItems"
    :hero="hero"
    @nav-click="handleNavClick"
  >
    <section class="manage-shell glass-card">
      <div class="manage-head">
        <div>
          <h2>题目列表</h2>
          <p>集中管理题目的生命周期，支持搜索、标签筛选与快速跳转编辑。</p>
        </div>
        <a-space>
          <a-button shape="round" @click="refresh">刷新</a-button>
          <a-button type="primary" shape="round" @click="router.push('/questions/create')">
            新建题目
          </a-button>
        </a-space>
      </div>

      <div class="filter-box">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12" :md="10">
            <a-input
              v-model:value="query.title"
              placeholder="按标题搜索"
              allow-clear
              @pressEnter="refresh"
            >
              <template #prefix>
                <span class="filter-icon">🔍</span>
              </template>
            </a-input>
          </a-col>
          <a-col :xs="24" :sm="12" :md="8">
            <a-select
              v-model:value="query.tags"
              mode="tags"
              placeholder="输入标签回车添加"
              allow-clear
              :token-separators="[',', '，', ' ']"
            />
          </a-col>
          <a-col :xs="24" :sm="24" :md="6">
            <a-space>
              <a-button shape="round" @click="resetFilters">重置</a-button>
              <a-button type="primary" shape="round" @click="refresh">应用筛选</a-button>
            </a-space>
          </a-col>
        </a-row>
      </div>

      <a-table
        :columns="columns"
        :data-source="questions"
        :loading="loading"
        row-key="id"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'title'">
            <div class="title-cell">
              <span class="title-text" @click="openDetail(record.id)">{{ record.title }}</span>
              <span class="title-meta">ID: {{ record.id }}</span>
            </div>
          </template>

          <template v-else-if="column.key === 'tags'">
            <a-space wrap>
              <a-tag v-for="tag in normalizeTags(record.tags)" :key="tag" class="tag-pill">
                {{ tag }}
              </a-tag>
            </a-space>
          </template>

          <template v-else-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="router.push(`/questions/edit/${record.id}`)">编辑</a-button>
              <a-popconfirm
                title="确认删除该题目吗？"
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
  </PageLayout>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import PageLayout from '../components/layout/PageLayout.vue';
import { getQuestionList, deleteQuestion } from '../api/question';

const router = useRouter();

const loading = ref(false);
const questions = ref([]);

const query = reactive({
  title: '',
  tags: [],
  current: 1,
  size: 10
});

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total) => `共 ${total} 条`
});

const navItems = [
  { key: 'question-manage', label: '题目列表', path: '/questions' },
  { key: 'question-bank-manage', label: '题库管理', path: '/question-banks' },
  { key: 'user-manage', label: '用户管理', path: '/users' }
];

const hero = computed(() => ({
  badge: '题目管理',
  title: '集中管理你的知识资产',
  subtitle: '按标签搜索、批量维护、查看详情，保持题库健康可持续。'
}));

const columns = [
  { title: '题目', dataIndex: 'title', key: 'title' },
  { title: '标签', dataIndex: 'tags', key: 'tags', width: 220 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 160 },
  { title: '操作', key: 'action', width: 140 }
];

const fetchQuestions = async () => {
  loading.value = true;
  try {
    const params = {
      current: pagination.current,
      size: pagination.pageSize,
      title: query.title || undefined,
      tags: (query.tags || []).join(',')
    };
    const response = await getQuestionList(params);
    if (response.code === 200) {
      questions.value = response.data?.records || [];
      pagination.total = response.data?.total || 0;
    } else {
      message.error(response.message || '获取题目列表失败');
    }
  } catch (error) {
    console.error('获取题目列表错误:', error);
    message.error('获取题目列表失败，请检查网络');
  } finally {
    loading.value = false;
  }
};

const refresh = () => {
  pagination.current = 1;
  fetchQuestions();
};

const resetFilters = () => {
  query.title = '';
  query.tags = [];
  refresh();
};

const handleDelete = (id) => async () => {
  try {
    const response = await deleteQuestion(id);
    if (response.code === 200) {
      message.success('删除成功');
      refresh();
    } else {
      message.error(response.message || '删除失败');
    }
  } catch (error) {
    console.error('删除题目错误:', error);
    message.error('删除失败，请稍后再试');
  }
};

const normalizeTags = (tags) => {
  if (!tags) return [];
  if (Array.isArray(tags)) return tags;
  return tags.split(',').map((tag) => tag.trim()).filter(Boolean);
};

const formatDate = (value) => {
  if (!value) return '刚刚';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '刚刚';
  return date.toLocaleString();
};

const handleTableChange = (pager) => {
  pagination.current = pager.current;
  pagination.pageSize = pager.pageSize;
  fetchQuestions();
};

const openDetail = (id) => {
  router.push(`/question/${id}`);
};

const handleNavClick = (item) => {
  if (item.path) {
    router.push(item.path);
  }
};

onMounted(() => {
  fetchQuestions();
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

.filter-icon {
  font-size: 16px;
}

.title-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.title-text {
  font-weight: 600;
  cursor: pointer;
  transition: color 0.2s ease;
}

.title-text:hover {
  color: var(--brand-primary);
}

.title-meta {
  font-size: 12px;
  color: var(--text-secondary);
}

.tag-pill {
  background: rgba(59, 130, 246, 0.12);
  color: #2563eb;
  border-radius: 999px;
  border: none;
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
