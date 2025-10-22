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
          <a-col :xs="24" :sm="12" :md="6">
            <a-select
              v-model:value="query.difficulty"
              placeholder="选择难度"
              allow-clear
              :options="difficultyOptions"
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

      <div class="batch-toolbar">
        <div class="batch-summary">
          <span>已选择 <strong>{{ selectedCount }}</strong> 道题目</span>
          <a-button type="link" size="small" @click="clearSelection" :disabled="!selectedCount">
            清空选择
          </a-button>
        </div>
        <a-space>
          <a-button
            type="primary"
            shape="round"
            @click="openRepoModal('add')"
            :disabled="!canBatchOperate || batchSubmitting"
          >
            批量加入题库
          </a-button>
          <a-button
            shape="round"
            @click="openRepoModal('remove')"
            :disabled="!canBatchOperate || batchSubmitting"
          >
            批量移出题库
          </a-button>
          <a-popconfirm
            title="确认删除选中的题目吗？"
            ok-text="删除"
            cancel-text="取消"
            :ok-button-props="{ danger: true }"
            @confirm="confirmBatchDelete"
          >
            <a-button danger shape="round" :disabled="!canBatchOperate" :loading="batchSubmitting">
              批量删除
            </a-button>
          </a-popconfirm>
        </a-space>
      </div>

      <a-table
        :columns="columns"
        :data-source="questions"
        :loading="tableLoading"
        :row-selection="rowSelection"
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
          <template v-else-if="column.key === 'difficulty'">
            <a-tag color="processing">{{ formatDifficulty(record.difficulty) }}</a-tag>
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

      <a-modal
        v-model:open="repoModalVisible"
        :title="repoModalTitle"
        :confirm-loading="repoModalLoading"
        ok-text="确认"
        cancel-text="取消"
        @ok="handleRepoConfirm"
        @cancel="handleRepoCancel"
      >
        <a-spin :spinning="repoOptionsLoading">
          <a-select
            v-model:value="selectedRepoId"
            show-search
            placeholder="请选择题库"
            style="width: 100%;"
            :options="repoOptions"
            :filter-option="repoFilterOption"
          />
          <p v-if="!repoOptionsLoading && !repoOptions.length" class="repo-empty-tip">
            暂无题库，请先创建题库后再执行该操作。
          </p>
        </a-spin>
      </a-modal>
    </section>
  </PageLayout>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import PageLayout from '../components/layout/PageLayout.vue';
import {
  searchQuestionFromEs,
  deleteQuestion,
  batchDeleteQuestions,
  batchAddQuestionsToRepo,
  batchRemoveQuestionsFromRepo
} from '../api/question';
import { getQuestionBankList } from '../api/questionBank';

const router = useRouter();

const loading = ref(false);
const batchSubmitting = ref(false);
const questions = ref([]);

const query = reactive({
  title: '',
  tags: [],
  difficulty: null,
  current: 1,
  size: 10
});

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total) => '共 ' + total + ' 条'
});

const difficultyOptions = [
  { value: 1, label: '基础' },
  { value: 2, label: '简单' },
  { value: 3, label: '中等' },
  { value: 4, label: '困难' }
];

const difficultyTextMap = {
  1: '基础',
  2: '简单',
  3: '中等',
  4: '困难'
};

const navItems = [
  { key: 'admin-home', label: '首页', path: '/admin' },
  { key: 'question-manage', label: '题目列表', path: '/questions' },
  { key: 'question-bank-manage', label: '题库管理', path: '/question-banks' },
  { key: 'user-manage', label: '用户管理', path: '/users' }
];

const hero = computed(() => ({
  badge: '题目管理',
  title: '集中管理你的知识资产',
  subtitle: '按标签搜索、批量维护、查看详情，保持题库健康可持续运行。'
}));

const columns = [
  { title: '题目', dataIndex: 'title', key: 'title' },
  { title: '标签', dataIndex: 'tags', key: 'tags', width: 220 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 160 },
  { title: '操作', key: 'action', width: 140 }
];

const selectedRowKeys = ref([]);
const canBatchOperate = computed(() => selectedRowKeys.value.length > 0);
const selectedCount = computed(() => selectedRowKeys.value.length);

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys) => {
    selectedRowKeys.value = keys;
  },
  preserveSelectedRowKeys: true
}));

const tableLoading = computed(() => loading.value || batchSubmitting.value);

const repoModalVisible = ref(false);
const repoModalMode = ref('add');
const repoModalLoading = ref(false);
const repoOptionsLoading = ref(false);
const repoOptions = ref([]);
const selectedRepoId = ref(null);
const repoModalTitle = computed(() =>
  repoModalMode.value === 'add' ? '选择题库 - 批量加入' : '选择题库 - 批量移除'
);
const repoFilterOption = (input, option) =>
  ((option && option.label) || '').toLowerCase().includes(input.toLowerCase());

const fetchQuestions = async () => {
  loading.value = true;
  try {
    const params = {
      current: pagination.current,
      size: pagination.pageSize,
      title: query.title || undefined,
      tags: (query.tags || []).join(','),
      difficulty: query.difficulty || undefined
    };
    const response = await searchQuestionFromEs(params);
    if (response.code === 200) {
      const records = response.data?.records || [];
      questions.value = records;
      pagination.total = response.data?.total || 0;
      const currentIdSet = new Set(records.map((item) => String(item.id)));
      selectedRowKeys.value = selectedRowKeys.value.filter((key) =>
        currentIdSet.has(String(key))
      );
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

const loadRepoOptions = async () => {
  repoOptionsLoading.value = true;
  try {
    const response = await getQuestionBankList({ current: 1, size: 100 });
    if (response.code === 200) {
      const records = response.data?.records || [];
      repoOptions.value = records.map((item) => ({
        value: item.id,
        label: item.title
      }));
    } else {
      message.error(response.message || '获取题库列表失败');
    }
  } catch (error) {
    console.error('获取题库列表失败:', error);
    message.error('获取题库列表失败，请稍后再试');
  } finally {
    repoOptionsLoading.value = false;
  }
};

const clearSelection = () => {
  selectedRowKeys.value = [];
};

const refresh = () => {
  pagination.current = 1;
  clearSelection();
  fetchQuestions();
};

const resetFilters = () => {
  query.title = '';
  query.tags = [];
  query.difficulty = null;
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

const confirmBatchDelete = async () => {
  if (!selectedRowKeys.value.length) {
    message.warning('请先选择题目');
    return;
  }
  batchSubmitting.value = true;
  try {
    const response = await batchDeleteQuestions({ questionIds: selectedRowKeys.value });
    if (response.code === 200) {
      message.success('批量删除成功');
      clearSelection();
      refresh();
    } else {
      message.error(response.message || '批量删除失败');
    }
  } catch (error) {
    console.error('批量删除题目错误:', error);
    message.error('批量删除失败，请稍后再试');
  } finally {
    batchSubmitting.value = false;
  }
};

const openRepoModal = (mode) => {
  if (!selectedRowKeys.value.length) {
    message.warning('请先选择题目');
    return;
  }
  repoModalMode.value = mode;
  repoModalVisible.value = true;
  selectedRepoId.value = null;
  loadRepoOptions();
};

const handleRepoConfirm = async () => {
  if (!selectedRepoId.value) {
    message.warning('请选择题库');
    return;
  }
  repoModalLoading.value = true;
  try {
    const payload = {
      questionIds: selectedRowKeys.value,
      repoId: selectedRepoId.value
    };
    const response =
      repoModalMode.value === 'add'
        ? await batchAddQuestionsToRepo(payload)
        : await batchRemoveQuestionsFromRepo(payload);
    if (response.code === 200) {
      message.success(
        repoModalMode.value === 'add' ? '批量加入题库成功' : '批量移除题库成功'
      );
      repoModalVisible.value = false;
      clearSelection();
    } else {
      message.error(response.message || '操作失败');
    }
  } catch (error) {
    console.error('批量题库操作失败:', error);
    message.error('操作失败，请稍后再试');
  } finally {
    repoModalLoading.value = false;
  }
};

const handleRepoCancel = () => {
  repoModalVisible.value = false;
};

const formatDifficulty = (value) => difficultyTextMap[value] || '中等';

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
  clearSelection();
  fetchQuestions();
};

const openDetail = (id) => {
  router.push('/question/' + id);
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

.batch-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  border-radius: 16px;
  background: rgba(248, 250, 252, 0.85);
  border: 1px solid rgba(148, 163, 184, 0.18);
  margin-top: 16px;
}

.batch-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.batch-summary strong {
  color: var(--brand-primary, #2563eb);
}

.batch-summary :deep(.ant-btn-link) {
  padding: 0;
}

.repo-empty-tip {
  margin-top: 12px;
  color: var(--text-secondary);
  font-size: 13px;
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
  .batch-toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .manage-shell {
    padding: 24px;
  }

  .manage-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
