<template>
  <PageLayout
    :hero="heroBlock"
    @nav-click="handleNavClick"
  >
    <section class="home-section glass-card">
      <div class="insights-grid">
        <div class="insight-card" v-for="card in insightCards" :key="card.title">
          <div class="insight-icon" :style="{ background: card.gradient }">
            <span>{{ card.icon }}</span>
          </div>
          <div class="insight-content">
            <h3>{{ card.title }}</h3>
            <p>{{ card.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <section class="home-section" ref="questionBankSection">
      <div class="section-head">
        <div>
          <h2 class="section-title">精选题库</h2>
          <p class="section-subtitle">覆盖热门技术方向，支持 Markdown 阅读、收藏与分享。</p>
        </div>
        <a-button type="text" shape="round" @click="fetchQuestionBanks">刷新列表</a-button>
      </div>

      <div class="filter-bar glass-card">
        <a-segmented
          v-model:value="activeTab"
          :options="[{ label: '开刷推荐', value: 'popular' }, { label: '最新题库', value: 'latest' }]"
        />
        <div class="tag-cloud">
          <a-tag
            v-for="tag in categoryTags"
            :key="tag"
            :class="['tag-chip', { 'tag-chip--active': selectedTag === tag }]"
            @click="selectedTag = tag"
          >
            {{ tag }}
          </a-tag>
        </div>
      </div>

      <div class="question-grid">
        <template v-if="loading">
          <a-card v-for="index in 8" :key="index" class="question-card">
            <a-skeleton active :paragraph="{ rows: 3 }" />
          </a-card>
        </template>
        <template v-else-if="filteredBanks.length">
          <a-card
            v-for="bank in filteredBanks"
            :key="bank.id"
            class="question-card"
            hoverable
            @click="goToQuestionBank(bank.id)"
          >
            <div class="card-head">
              <div class="card-icon" :class="getIconClass(bank.title)">
                <span>{{ getIcon(bank.title) }}</span>
              </div>
              <a-tag v-if="isHotBank(bank.title)" color="orange" class="tag-pill">热门</a-tag>
            </div>
            <h3>{{ bank.title }}</h3>
            <p>{{ bank.description || '点击查看题库详情，开启高效刷题模式。' }}</p>
            <div class="card-meta">
              <span>题目数：{{ bank.questionCount ?? '--' }}</span>
              <span>更新：{{ formatDate(bank.updateTime || bank.createTime) }}</span>
            </div>
          </a-card>
        </template>
        <div v-else class="empty-state glass-card">
          <h3>暂未找到相关题库</h3>
          <p>尝试选择其他标签或稍后刷新。</p>
        </div>
      </div>
    </section>
  </PageLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { message } from 'ant-design-vue';
import PageLayout from '../components/layout/PageLayout.vue';
import { getQuestionBankList } from '../api/questionBank';

const router = useRouter();
const store = useStore();
const user = computed(() => store.state.user);

const questionBanks = ref([]);
const loading = ref(false);
const activeTab = ref('popular');
const selectedTag = ref('全部');
const questionBankSection = ref(null);

const categoryTags = [
  '全部', '热门', 'Java', '前端', '后端', '数据库', '算法', '计算机网络', '操作系统', 'Redis', '面经', 'AI'
];

const heroBlock = computed(() => ({
  badge: '面试快人一步',
  title: user.value?.userName ? `欢迎回来，${user.value.userName}` : '面试鸭刷题神 · 让刷题更轻松',
  subtitle: '智能题库、实时 Markdown 状态同步、完整的题目管理让你的备战更高效。'
}));

const insightCards = [
  {
    title: '全栈题库',
    description: '后端、前端、数据库、算法... 告别信息碎片，系统掌握知识体系。',
    icon: '📚',
    gradient: 'linear-gradient(135deg, rgba(59,130,246,0.18), rgba(14,165,233,0.28))'
  },
  {
    title: '高效协作',
    description: '题目、题库、用户统一管理，支持 Markdown 编辑与实时预览。',
    icon: '⚡',
    gradient: 'linear-gradient(135deg, rgba(244,114,182,0.18), rgba(251,191,36,0.28))'
  },
  {
    title: '沉浸体验',
    description: '全新 UI 与暗光玻璃效果，沉浸式刷题体验更专注、不分心。',
    icon: '🎧',
    gradient: 'linear-gradient(135deg, rgba(40,199,111,0.2), rgba(16,185,129,0.28))'
  }
];

const fetchQuestionBanks = async () => {
  loading.value = true;
  try {
    const response = await getQuestionBankList({ current: 1, size: 50 });
    if (response.code === 200) {
      questionBanks.value = response.data?.records || [];
    } else {
      message.error(response.message || '获取题库数据失败');
    }
  } catch (error) {
    console.error('获取题库数据错误:', error);
    message.error('获取题库数据失败，请检查网络连接');
  } finally {
    loading.value = false;
  }
};

const filteredBanks = computed(() => {
  const tag = selectedTag.value;
  const banks = [...questionBanks.value];

  if (activeTab.value === 'latest') {
    banks.sort((a, b) => new Date(b.updateTime || b.createTime) - new Date(a.updateTime || a.createTime));
  }

  if (tag === '全部') {
    return banks;
  }

  return banks.filter((item) => {
    const source = `${item.title || ''} ${item.description || ''} ${item.tags || ''}`;
    return source.includes(tag);
  });
});

const getIcon = (title = '') => {
  if (title.includes('Java')) return '☕';
  if (title.includes('MySQL') || title.includes('数据库')) return '🐬';
  if (title.includes('Redis')) return '📮';
  if (title.includes('Spring')) return '🌱';
  if (title.includes('网络')) return '🌐';
  if (title.includes('操作系统') || title.includes('OS')) return '💻';
  if (title.includes('算法')) return '🧮';
  if (title.includes('前端')) return '🎨';
  return '📚';
};

const getIconClass = (title = '') => {
  if (title.includes('Java')) return 'icon-java';
  if (title.includes('MySQL') || title.includes('数据库')) return 'icon-mysql';
  if (title.includes('Redis')) return 'icon-redis';
  if (title.includes('Spring')) return 'icon-spring';
  if (title.includes('网络')) return 'icon-network';
  if (title.includes('操作系统') || title.includes('OS')) return 'icon-os';
  if (title.includes('算法')) return 'icon-algorithm';
  if (title.includes('前端')) return 'icon-frontend';
  return 'icon-default';
};

const isHotBank = (title = '') => title.includes('热门') || title.includes('HOT') || title.includes('星标');

const goToQuestionBank = (id) => {
  router.push(`/question-bank/${id}`);
};

const formatDate = (value) => {
  if (!value) return '刚刚';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '刚刚';
  return date.toLocaleDateString();
};

const handleNavClick = (item) => {
  if (item.key === 'scroll-question-banks') {
    questionBankSection.value?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
};

onMounted(() => {
  fetchQuestionBanks();
});
</script>

<style scoped>
.home-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.insights-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 24px;
}

.insight-card {
  display: flex;
  gap: 18px;
  padding: 28px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(148, 163, 184, 0.18);
  box-shadow: var(--shadow-soft);
  backdrop-filter: blur(12px);
  transition: var(--transition-base);
}

.insight-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}

.insight-icon {
  min-width: 52px;
  height: 52px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  font-size: 24px;
}

.insight-content h3 {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 8px;
}

.insight-content p {
  font-size: 14px;
  color: var(--text-secondary);
}

.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}

.filter-bar {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px 24px;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-chip {
  cursor: pointer;
  background: rgba(148, 163, 184, 0.12);
  border-radius: 999px;
  border: none;
  padding: 6px 16px;
  color: var(--text-secondary);
  transition: var(--transition-base);
}

.tag-chip--active {
  background: rgba(255, 122, 69, 0.16);
  color: var(--brand-primary);
  box-shadow: 0 12px 24px rgba(255, 122, 69, 0.2);
}

.question-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 20px;
}

.question-card {
  cursor: pointer;
  border-radius: 20px;
  border: none;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow-soft);
  transition: var(--transition-base);
}

.question-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-hover);
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  font-size: 26px;
  color: #fff;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--text-secondary);
}

.empty-state {
  padding: 36px;
  text-align: center;
  border-radius: 20px;
}

.empty-state h3 {
  margin-bottom: 12px;
  font-weight: 700;
}

.icon-java { background: linear-gradient(135deg, #f97316, #fb923c); }
.icon-mysql { background: linear-gradient(135deg, #6366f1, #8b5cf6); }
.icon-redis { background: linear-gradient(135deg, #ef4444, #f97316); }
.icon-spring { background: linear-gradient(135deg, #22c55e, #10b981); }
.icon-network { background: linear-gradient(135deg, #0ea5e9, #38bdf8); }
.icon-os { background: linear-gradient(135deg, #64748b, #94a3b8); }
.icon-algorithm { background: linear-gradient(135deg, #facc15, #fb923c); }
.icon-frontend { background: linear-gradient(135deg, #ec4899, #f472b6); }
.icon-default { background: linear-gradient(135deg, #14b8a6, #0ea5e9); }

@media (max-width: 768px) {
  .section-head {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .question-grid {
    grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  }
}
</style>
