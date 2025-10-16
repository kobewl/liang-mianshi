<template>
  <PageLayout
    variant="admin"
    :hero="hero"
    @nav-click="handleNavClick"
  >
    <a-spin :spinning="loading">
      <section class="form-shell glass-card">
        <div class="form-head">
          <div>
            <h2>{{ isEdit ? '编辑题库' : '创建题库' }}</h2>
            <p>完善题库信息，帮助团队快速定位并使用对应的题目集合。</p>
          </div>
          <a-space>
            <a-button shape="round" @click="backToList">返回列表</a-button>
            <a-button type="primary" shape="round" :loading="saving" @click="handleSubmit">
              {{ isEdit ? '保存修改' : '创建题库' }}
            </a-button>
          </a-space>
        </div>

        <a-form
          ref="formRef"
          :model="questionBankForm"
          :rules="rules"
          layout="vertical"
          class="edit-form"
        >
          <a-row :gutter="[24, 24]">
            <a-col :xs="24" :lg="16">
              <a-form-item label="题库标题" name="title">
                <a-input
                  v-model:value="questionBankForm.title"
                  size="large"
                  placeholder="请输入题库标题，例如：Java 基础题库"
                  allow-clear
                />
              </a-form-item>

              <a-form-item label="题库描述" name="description">
                <a-textarea
                  v-model:value="questionBankForm.description"
                  :rows="6"
                  show-count
                  :maxlength="500"
                  placeholder="补充题库覆盖范围、适用场景、更新频率等信息"
                />
              </a-form-item>
            </a-col>

            <a-col :xs="24" :lg="8">
              <a-form-item label="题库封面" name="picture">
                <div class="upload-shell">
                  <a-upload
                    v-model:file-list="fileList"
                    list-type="picture-card"
                    :before-upload="beforeUpload"
                    @remove="handleRemove"
                    :max-count="1"
                    accept="image/*"
                  >
                    <div v-if="fileList.length === 0">
                      <div class="upload-placeholder">
                        <span class="upload-icon">📷</span>
                        <p>上传封面</p>
                      </div>
                    </div>
                  </a-upload>

                  <div v-if="questionBankForm.picture" class="preview-card glass-card">
                    <span class="preview-title">当前封面</span>
                    <img :src="questionBankForm.picture" alt="题库封面" />
                    <a-button type="link" danger @click="removeImage">移除封面</a-button>
                  </div>
                </div>
              </a-form-item>
            </a-col>
          </a-row>

          <div class="form-footer">
            <a-space>
              <a-button type="primary" shape="round" :loading="saving" @click="handleSubmit">
                {{ isEdit ? '保存修改' : '创建题库' }}
              </a-button>
              <a-button shape="round" @click="backToList">取消</a-button>
            </a-space>
          </div>
        </a-form>
      </section>
    </a-spin>
  </PageLayout>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { message } from 'ant-design-vue';
import PageLayout from '../components/layout/PageLayout.vue';
import { addQuestionBank, updateQuestionBank, getQuestionBankById } from '../api/questionBank';

const router = useRouter();
const route = useRoute();

const formRef = ref();
const loading = ref(false);
const saving = ref(false);
const fileList = ref([]);

const questionBankForm = reactive({
  id: null,
  title: '',
  description: '',
  picture: ''
});

const isEdit = computed(() => Boolean(route.params.id));

const hero = computed(() => ({
  badge: isEdit.value ? '编辑题库' : '新建题库',
  title: isEdit.value ? '更新题库信息保持最新状态' : '创建题库以系统组织题目',
  subtitle: '高质量的题库描述和封面可以帮助考生快速了解内容定位。'
}));

const rules = {
  title: [
    { required: true, message: '请输入题库标题', trigger: 'blur' },
    { min: 2, max: 40, message: '标题长度需在 2-40 个字符之间', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请填写题库描述', trigger: 'blur' },
    { min: 10, message: '描述内容至少 10 个字符', trigger: 'blur' }
  ]
};

const handleSubmit = async () => {
  try {
    await formRef.value.validate();
  } catch (error) {
    return;
  }

  saving.value = true;
  try {
    const payload = {
      title: questionBankForm.title,
      description: questionBankForm.description,
      picture: questionBankForm.picture
    };

    let response;
    if (isEdit.value) {
      response = await updateQuestionBank(questionBankForm.id, payload);
    } else {
      response = await addQuestionBank(payload);
    }

    if (response.code === 200) {
      message.success(isEdit.value ? '题库更新成功' : '题库创建成功');
      backToList();
    } else {
      message.error(response.message || (isEdit.value ? '更新失败' : '创建失败'));
    }
  } catch (error) {
    console.error('提交题库失败:', error);
    message.error('提交失败，请稍后再试');
  } finally {
    saving.value = false;
  }
};

const fetchDetail = async (id) => {
  loading.value = true;
  try {
    const response = await getQuestionBankById(id);
    if (response.code === 200) {
      Object.assign(questionBankForm, response.data || {});
      if (questionBankForm.picture) {
        fileList.value = [
          {
            uid: '-1',
            name: 'cover.png',
            status: 'done',
            url: questionBankForm.picture
          }
        ];
      }
    } else {
      message.error(response.message || '获取题库信息失败');
      backToList();
    }
  } catch (error) {
    console.error('获取题库信息失败:', error);
    message.error('获取题库信息失败，请稍后再试');
    backToList();
  } finally {
    loading.value = false;
  }
};

const beforeUpload = (file) => {
  const isImage = file.type?.startsWith('image/');
  if (!isImage) {
    message.error('只能上传图片文件');
    return false;
  }
  const reader = new FileReader();
  reader.onload = (e) => {
    questionBankForm.picture = e.target?.result || '';
  };
  reader.readAsDataURL(file);
  fileList.value = [file];
  return false;
};

const handleRemove = () => {
  fileList.value = [];
  questionBankForm.picture = '';
};

const removeImage = () => {
  handleRemove();
};

const backToList = () => {
  router.push('/question-banks');
};

const handleNavClick = (item) => {
  if (item.path) {
    router.push(item.path);
  }
};

onMounted(() => {
  if (isEdit.value) {
    fetchDetail(route.params.id);
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
  max-width: 540px;
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.upload-shell {
  border: 2px dashed rgba(148, 163, 184, 0.35);
  border-radius: 16px;
  padding: 18px;
  background: rgba(248, 250, 252, 0.65);
  transition: var(--transition-base);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.upload-shell:hover {
  border-color: var(--brand-primary);
}

.upload-placeholder {
  text-align: center;
  color: var(--text-secondary);
}

.upload-placeholder p {
  margin-top: 6px;
}

.upload-icon {
  font-size: 28px;
  display: block;
}

.preview-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 12px;
}

.preview-card img {
  max-width: 100%;
  border-radius: 12px;
}

.preview-title {
  font-size: 14px;
  color: var(--text-secondary);
}

.form-footer {
  display: flex;
  justify-content: center;
  padding-top: 12px;
  border-top: 1px solid rgba(148, 163, 184, 0.18);
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
