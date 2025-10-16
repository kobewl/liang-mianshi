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
                v-model:value="tagsField"
                mode="tags"
                size="large"
                placeholder="输入标签后回车添加，示例：Java、Redis"
                :token-separators="[',', '，', ' ']"
                :max-tag-count="6"
              />
            </a-form-item>
          </div>

          <a-form-item label="题目内容" name="content">
            <MarkdownEditor
              v-model="questionForm.content"
              label="题目内容"
              description="支持 Markdown 语法、代码高亮与表格。"
              :height="420"
            />
          </a-form-item>

          <a-form-item label="参考答案" name="answer">
            <MarkdownEditor
              v-model="questionForm.answer"
              label="参考答案"
              description="可插入代码、关键点总结，帮助考生快速理解。"
              :height="360"
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
import { computed, onMounted, reactive, ref } from 'vue';
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

const navItems = [
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

const tagsField = computed({
  get: () => {
    if (!questionForm.tags) return [];
    return questionForm.tags.split(',').map((tag) => tag.trim()).filter(Boolean);
  },
  set: (value) => {
    questionForm.tags = (value || []).map((tag) => tag.trim()).filter(Boolean).join(',');
  }
});

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
      questionForm.tags = Array.isArray(data.tags) ? data.tags.join(',') : (data.tags || '');
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

const handleSubmit = async () => {
  try {
    await formRef.value.validate();

    const userId = store.state.user?.id || store.state.user?.userId;
    if (!userId) {
      message.error('请先登录后再进行题目管理');
      return;
    }

    const payload = {
      title: questionForm.title,
      content: questionForm.content,
      answer: questionForm.answer,
      tags: questionForm.tags,
      userId
    };

    saving.value = true;
    let response;
    if (isEdit.value) {
      response = await updateQuestion(questionForm.id, payload);
    } else {
      response = await addQuestion(payload);
    }

    if (response.code === 200) {
      if (!isEdit.value && questionForm.selectedQuestionBanks.length > 0) {
        const questionId = response.data;
        try {
          await Promise.all(
            questionForm.selectedQuestionBanks.map((questionBankId) =>
              addQuestionBankQuestion({
                questionBankId,
                questionId,
                userId
              })
            )
          );
          message.success('题目创建成功，已同步关联题库');
        } catch (error) {
          console.error('题库关联失败:', error);
          message.warning('题目创建成功，但题库关联失败');
        }
      } else {
        message.success(isEdit.value ? '题目更新成功' : '题目创建成功');
      }
      backToList();
    } else {
      message.error(response.message || (isEdit.value ? '更新失败' : '创建失败'));
    }
  } catch (error) {
    if (error?.errorFields) {
      return;
    }
    console.error('提交错误:', error);
    message.error('提交失败，请稍后再试');
  } finally {
    saving.value = false;
  }
};

const handleNavClick = (item) => {
  if (item.path) {
    router.push(item.path);
  }
};

onMounted(async () => {
  const userId = store.state.user?.id || store.state.user?.userId;
  if (userId) {
    questionForm.userId = userId;
  }
  if (isEdit.value) {
    await fetchQuestionDetail(route.params.id);
  } else {
    await fetchQuestionBanks();
    const questionBankId = route.query.questionBankId;
    if (questionBankId) {
      questionForm.selectedQuestionBanks = [Number(questionBankId)];
    }
  }
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
