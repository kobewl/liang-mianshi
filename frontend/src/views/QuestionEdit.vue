<template>
  <PageLayout
    variant="admin"
    :nav-items="navItems"
    :hero="hero"
    @nav-click="handleNavClick"
  >
    <a-spin :spinning="loading">
      <section class="form-shell glass-card">
        <div class="form-head">
          <div>
            <h2>{{ isEdit ? '编辑题目' : '新增题目' }}</h2>
            <p>完善题目信息，并使用升级后的 Markdown 编辑器快速编写题干与答案。</p>
          </div>
          <a-space>
            <a-button shape="round" @click="backToList">返回列表</a-button>
            <a-button type="primary" shape="round" :loading="saving" @click="handleSubmit">
              {{ isEdit ? '保存修改' : '创建题目' }}
            </a-button>
          </a-space>
        </div>

        <a-form
          ref="formRef"
          :model="questionForm"
          :rules="rules"
          layout="vertical"
          class="question-form"
        >
          <div class="form-grid">
            <a-form-item label="题目标题" name="title">
              <a-input
                v-model:value="questionForm.title"
                size="large"
                placeholder="请输入题目标题，建议简洁明了"
                allow-clear
              />
            </a-form-item>

            <a-form-item label="标签" name="tags">
              <a-select
                v-model:value="questionForm.tags"
                mode="tags"
                size="large"
                placeholder="输入标签后回车添加，示例：Java、Redis"
                :token-separators="[',', ' ']"
                :max-tag-count="6"
              />
            </a-form-item>
            <a-form-item label="题目难度" name="difficulty">
              <a-select
                v-model:value="questionForm.difficulty"
                size="large"
                :options="difficultyOptions"
              />
            </a-form-item>
          </div>

          <a-form-item label="题目内容" name="content">
            <MarkdownEditor
              v-model="questionForm.content"
              label="题目内容"
              description="支持 Markdown 语法、代码高亮与表格。"
              :height="300"
            />
          </a-form-item>

          <a-form-item label="参考答案" name="answer">
            <MarkdownEditor
              v-model="questionForm.answer"
              label="参考答案"
              description="可插入代码、关键点总结，帮助考生快速理解。"
              :height="520"
            />
          </a-form-item>

          <a-form-item v-if="!isEdit" label="所属题库" name="selectedQuestionBanks">
            <a-select
              v-model:value="questionForm.selectedQuestionBanks"
              mode="multiple"
              placeholder="请选择题目关联的题库"
              :options="questionBanks.map((bank) => ({ label: bank.title, value: bank.id }))"
              allow-clear
              show-search
            />
          </a-form-item>
        </a-form>
      </section>
    </a-spin>
  </PageLayout>
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { message } from 'ant-design-vue';
import PageLayout from '../components/layout/PageLayout.vue';
import MarkdownEditor from '../components/editor/MarkdownEditor.vue';
import { addQuestion, updateQuestion, getQuestionById } from '../api/question';
import { getQuestionBankList, addQuestionBankQuestion } from '../api/questionBank';

const router = useRouter();
const route = useRoute();
const store = useStore();

const formRef = ref();
const loading = ref(false);
const saving = ref(false);
const autoSaving = ref(false);
const isDirty = ref(false);

const AUTO_SAVE_INTERVAL = 10000;
const AUTO_SAVE_MESSAGE_KEY = 'question-auto-save';
let autoSaveTimer = null;

const questionBanks = ref([]);

const questionForm = reactive({
  id: null,
  title: '',
  content: '',
  answer: '',
  tags: [],
  difficulty: 1,
  userId: null,
  selectedQuestionBanks: []
});

watch(
  questionForm,
  () => {
    isDirty.value = true;
  },
  { deep: true }
);

const difficultyOptions = [
  { value: 1, label: '基础' },
  { value: 2, label: '简单' },
  { value: 3, label: '中等' },
  { value: 4, label: '困难' }
];

const navItems = [
  { key: 'admin-home', label: '首页', path: '/admin' },
  { key: 'question-manage', label: '题目列表', path: '/questions' },
  { key: 'question-bank-manage', label: '题库管理', path: '/question-banks' },
  { key: 'user-manage', label: '用户管理', path: '/users' }
];

const isEdit = computed(() => Boolean(route.params.id));

const hero = computed(() => ({
  badge: isEdit.value ? '编辑题目' : '新建题目',
  title: isEdit.value ? '更新题目内容，保持知识鲜度' : '创建新题目，丰富你的题库',
  subtitle: '支持 Markdown 双栏编辑、可视化标签与题库关联，一次编写多端一致。'
}));

const rules = {
  title: [
    { required: true, message: '请输入题目标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度需在 2-100 个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入题目内容', trigger: 'blur' }
  ],
  answer: [
    { required: true, message: '请输入参考答案', trigger: 'blur' }
  ],
  selectedQuestionBanks: [
    {
      validator: (_rule, value) => {
        if (isEdit.value) {
          return Promise.resolve();
        }
        if (!value || value.length === 0) {
          return Promise.reject(new Error('请至少选择一个题库以便分类管理'));
        }
        return Promise.resolve();
      },
      trigger: 'change'
    }
  ]
};

const fetchQuestionBanks = async () => {
  try {
    const response = await getQuestionBankList({ current: 1, size: 100 });
    if (response.code === 200) {
      questionBanks.value = response.data?.records || [];
    } else {
      message.error(response.message || '获取题库列表失败');
    }
  } catch (error) {
    console.error('获取题库列表错误:', error);
    message.error('获取题库列表失败，请检查网络连接');
  }
};

const fetchQuestionDetail = async (id) => {
  loading.value = true;
  try {
    const response = await getQuestionById(id);
    if (response.code === 200) {
      const data = response.data;
      questionForm.id = data.id;
      questionForm.title = data.title || '';
      questionForm.content = data.content || '';
      questionForm.answer = data.answer || '';
      questionForm.tags = Array.isArray(data.tags)
        ? data.tags
        : (typeof data.tags === 'string' && data.tags.length > 0
          ? data.tags.split(',').map((tag) => tag.trim()).filter(Boolean)
          : []);
      questionForm.difficulty = data.difficulty ?? 1;
      isDirty.value = false;
    } else {
      message.error(response.message || '获取题目详情失败');
      backToList();
    }
  } catch (error) {
    console.error('获取题目详情错误:', error);
    message.error('获取题目详情失败，请稍后重试');
    backToList();
  } finally {
    loading.value = false;
  }
};

const backToList = () => {
  router.push('/questions');
};

const getCurrentUserId = () => store.state.user?.id || store.state.user?.userId;

const validateForm = async (auto = false) => {
  if (auto) {
    if (!questionForm.title?.trim()) {
      return false;
    }
    if (!questionForm.content?.trim()) {
      return false;
    }
    if (!questionForm.answer?.trim()) {
      return false;
    }
    if (
      !isEdit.value &&
      (!Array.isArray(questionForm.selectedQuestionBanks) ||
        questionForm.selectedQuestionBanks.length === 0)
    ) {
      return false;
    }
    return true;
  }

  if (!formRef.value) {
    return false;
  }

  await formRef.value.validate();
  return true;
};

const performSave = async ({ auto = false } = {}) => {
  if (auto) {
    if (autoSaving.value || saving.value || !isDirty.value) {
      return;
    }
  } else if (saving.value) {
    return;
  }

  const userId = getCurrentUserId();
  if (!userId) {
    if (!auto) {
      message.error('?????????????');
    }
    return;
  }
  questionForm.userId = userId;

  let formValid = false;
  try {
    formValid = await validateForm(auto);
  } catch (error) {
    if (error?.errorFields) {
      return;
    }
    console.error('??????:', error);
    if (!auto) {
      message.error('????????????');
    }
    return;
  }

  if (!formValid) {
    return;
  }

  const payload = {
    title: questionForm.title,
    content: questionForm.content,
    answer: questionForm.answer,
    tags: (questionForm.tags || []).map((tag) => tag.trim()).filter(Boolean),
    difficulty: questionForm.difficulty,
    userId
  };

  const stateRef = auto ? autoSaving : saving;
  stateRef.value = true;

  try {
    const shouldUpdate = Boolean(questionForm.id || isEdit.value);
    const targetId = questionForm.id || route.params.id;

    let response;
    if (shouldUpdate && targetId) {
      response = await updateQuestion(targetId, payload);
    } else {
      response = await addQuestion(payload);
    }

    if (response.code === 200) {
      const persistedId = questionForm.id || response.data;
      questionForm.id = persistedId;
      isDirty.value = false;

      if (auto) {
        message.success({ content: '?????', key: AUTO_SAVE_MESSAGE_KEY, duration: 1.2 });
        return;
      }

      if (!isEdit.value && questionForm.selectedQuestionBanks.length > 0 && persistedId) {
        try {
          await Promise.all(
            questionForm.selectedQuestionBanks.map((questionBankId) =>
              addQuestionBankQuestion({
                questionBankId,
                questionId: persistedId,
                userId
              })
            )
          );
          message.success('??????????????');
        } catch (error) {
          console.error('??????:', error);
          message.warning('??????????????');
        }
      } else {
        message.success(shouldUpdate ? '??????' : '??????');
      }
      backToList();
    } else if (!auto) {
      message.error(
        response.message || (Boolean(questionForm.id || isEdit.value) ? '????' : '????')
      );
    }
  } catch (error) {
    console.error(auto ? '??????:' : '????:', error);
    if (!auto) {
      message.error('??????????');
    }
  } finally {
    stateRef.value = false;
  }
};

const handleSubmit = async () => {
  await performSave({ auto: false });
};

const handleAutoSave = () => {
  performSave({ auto: true });
};

const startAutoSave = () => {
  if (autoSaveTimer) {
    clearInterval(autoSaveTimer);
  }
  autoSaveTimer = setInterval(handleAutoSave, AUTO_SAVE_INTERVAL);
};

const stopAutoSave = () => {
  if (autoSaveTimer) {
    clearInterval(autoSaveTimer);
    autoSaveTimer = null;
  }
};

const handleNavClick = (item) => {
  if (item.path) {
    router.push(item.path);
  }
};

onMounted(async () => {
  const userId = getCurrentUserId();
  if (userId) {
    questionForm.userId = userId;
  }

  try {
    if (isEdit.value) {
      await fetchQuestionDetail(route.params.id);
    } else {
      await fetchQuestionBanks();
      const questionBankId = route.query.questionBankId;
      if (questionBankId) {
        questionForm.selectedQuestionBanks = [Number(questionBankId)];
      }
    }
  } finally {
    isDirty.value = false;
    startAutoSave();
  }
});

onBeforeUnmount(() => {
  stopAutoSave();
});
</script>

<style scoped>
.form-shell {
  padding: 32px 36px;
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.form-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
}

.form-head h2 {
  font-size: 26px;
  font-weight: 700;
}

.form-head p {
  margin-top: 8px;
  color: var(--text-secondary);
  max-width: 520px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 20px;
}

.question-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

@media (max-width: 768px) {
  .form-shell {
    padding: 24px;
  }

  .form-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
