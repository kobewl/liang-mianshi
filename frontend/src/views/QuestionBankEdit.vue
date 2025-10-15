<template>
  <div class="question-bank-edit-container">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <span class="logo-icon">🦆</span>
          <span class="logo-text">面试鸭刷题神器</span>
        </div>

        <nav class="nav-menu">
          <a-button type="link" @click="$router.push('/')">首页</a-button>
          <a-button type="link" v-if="isAdmin" @click="$router.push('/questions')">题目管理</a-button>
          <a-button type="link" @click="$router.push('/question-banks')">题库管理</a-button>
          <a-button type="primary">编辑题库</a-button>
          <a-button type="link" v-if="isAdmin" @click="$router.push('/users')">用户管理</a-button>
        </nav>

        <div class="header-right">
          <a-button type="link" @click="handleLogout" class="logout-btn">
            <span class="user-icon">🚪</span>
            退出登录
          </a-button>
        </div>
      </div>
    </header>

    <!-- 主要内容 -->
    <main class="main-content">
      <div class="content-wrapper">
        <div class="page-header">
          <a-button type="link" @click="backToList" class="back-btn">
            <span class="btn-icon">←</span>
            返回列表
          </a-button>
          <h2 class="page-title">{{ isEdit ? '编辑题库' : '创建题库' }}</h2>
          <div></div>
        </div>

        <!-- 题库编辑表单 -->
        <div class="edit-form-container">
          <a-alert
            v-if="isEdit && !questionBankForm.id"
            message="正在加载题库信息..."
            type="info"
            show-icon
            class="loading-alert"
          />
          <a-form
            :model="questionBankForm"
            :rules="rules"
            ref="formRef"
            layout="vertical"
            class="edit-form"
          >
            <a-row :gutter="32">
              <a-col :span="16">
                <a-form-item label="题库标题" name="title">
                  <a-input
                    v-model:value="questionBankForm.title"
                    placeholder="请输入题库标题"
                    size="large"
                  />
                </a-form-item>

                <a-form-item label="题库描述" name="description">
                  <a-textarea
                    v-model:value="questionBankForm.description"
                    :rows="4"
                    placeholder="请输入题库描述"
                    show-count
                    :maxlength="500"
                  />
                </a-form-item>
              </a-col>

              <a-col :span="8">
                <a-form-item label="题库封面" name="picture">
                  <div class="upload-container">
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
                          <div class="upload-text">点击上传封面</div>
                        </div>
                      </div>
                    </a-upload>

                    <!-- 图片预览 -->
                    <div v-if="questionBankForm.picture" class="image-preview">
                      <div class="preview-title">当前封面：</div>
                      <img :src="questionBankForm.picture" alt="题库封面" class="preview-image" />
                      <a-button
                        type="link"
                        danger
                        @click="removeImage"
                        class="remove-btn"
                      >
                        移除图片
                      </a-button>
                    </div>
                  </div>
                </a-form-item>
              </a-col>
            </a-row>

            <a-form-item class="form-actions">
              <a-space>
                <a-button
                  type="primary"
                  @click="handleSubmit"
                  :loading="loading"
                  size="large"
                >
                  {{ isEdit ? '保存修改' : '创建题库' }}
                </a-button>
                <a-button @click="backToList" size="large">
                  取消
                </a-button>
              </a-space>
            </a-form-item>
          </a-form>
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
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { useRouter, useRoute } from 'vue-router';
import { useStore } from 'vuex';
import {
  getQuestionBankById,
  addQuestionBank,
  updateQuestionBank
} from '../api/questionBank';

export default {
  name: 'QuestionBankEdit',
  setup() {
    const router = useRouter();
    const route = useRoute();
    const store = useStore();
    const loading = ref(false);
    const formRef = ref();

    const isEdit = ref(false);
    const fileList = ref([]);

    const questionBankForm = reactive({
      id: null,
      title: '',
      description: '',
      picture: '',
      userId: null
    });

    const rules = {
      title: [
        { required: true, message: '请输入题库标题', trigger: 'blur' },
        { min: 2, max: 50, message: '标题长度在2-50个字符之间', trigger: 'blur' }
      ],
      description: [
        { max: 500, message: '描述不能超过500个字符', trigger: 'blur' }
      ]
    };

    // 模拟图片上传（实际项目中需要对接真实的文件上传接口）
    const beforeUpload = (file) => {
      const isImage = file.type.startsWith('image/');
      if (!isImage) {
        message.error('只能上传图片文件！');
        return false;
      }

      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isLt2M) {
        message.error('图片大小不能超过2MB！');
        return false;
      }

      // 模拟上传成功，生成预览URL
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => {
        questionBankForm.picture = reader.result;
        message.success('图片上传成功！');
      };

      return false; // 阻止自动上传
    };

    const handleRemove = () => {
      questionBankForm.picture = '';
    };

    const removeImage = () => {
      questionBankForm.picture = '';
      fileList.value = [];
    };

    // 获取题库详情
    const fetchQuestionBankDetail = async (id) => {
      loading.value = true;
      try {
        const response = await getQuestionBankById(id);
        if (response.code === 200) {
          Object.assign(questionBankForm, response.data);
          // 如果有图片，设置文件列表
          if (response.data.picture) {
            fileList.value = [{
              uid: '-1',
              name: 'cover.jpg',
              status: 'done',
              url: response.data.picture
            }];
          }
        } else {
          message.error(response.message || '获取题库详情失败');
          router.push('/question-banks');
        }
      } catch (error) {
        console.error('获取题库详情错误:', error);
        message.error('获取题库详情失败，请检查网络连接');
        router.push('/question-banks');
      } finally {
        loading.value = false;
      }
    };

    // 返回列表
    const backToList = () => {
      router.push('/question-banks');
    };

    // 提交表单
    const handleSubmit = async () => {
      try {
        await formRef.value.validate();

        // 确保userId正确设置
        const userId = store.state.user?.id || store.state.user?.userId;
        if (!userId) {
          message.error('用户未登录，无法操作题库');
          return;
        }

        const data = {
          ...questionBankForm,
          userId: userId
        };

        // 对于编辑操作，不需要在请求体中传递id，因为id已经作为路径参数传递
        if (isEdit.value) {
          delete data.id;
        }

        loading.value = true;
        let response;
        if (isEdit.value) {
          // 编辑题库 - 需要传递题库ID和更新数据
          response = await updateQuestionBank(questionBankForm.id, data);
        } else {
          // 添加题库
          response = await addQuestionBank(data);
        }

        if (response.code === 200) {
          message.success(isEdit.value ? '编辑成功' : '创建成功');
          router.push('/question-banks');
        } else {
          message.error(response.message || (isEdit.value ? '编辑失败' : '创建失败'));
        }
      } catch (error) {
        console.log('表单验证失败:', error);
      } finally {
        loading.value = false;
      }
    };

    const handleLogout = () => {
      store.dispatch('logout');
      message.success('已退出登录');
      router.push('/login');
    };

    onMounted(() => {
      const id = route.params.id;
      if (id) {
        isEdit.value = true;
        fetchQuestionBankDetail(id);
      } else {
        // 设置当前用户ID
        const userId = store.state.user?.id || store.state.user?.userId;
        if (userId) {
          questionBankForm.userId = userId;
        }
      }
    });

    return {
      loading,
      isEdit,
      questionBankForm,
      rules,
      formRef,
      fileList,
      beforeUpload,
      handleRemove,
      removeImage,
      backToList,
      handleSubmit,
      handleLogout
    };
  }
}
</script>

<style scoped>
.question-bank-edit-container {
  height: 100vh;
  width: 100vw;
  background: #FFF5E6;
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

.nav-menu :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #FF9A3D 0%, #FF6B35 100%);
  border: none;
}

.header-right {
  display: flex;
  align-items: center;
}

.logout-btn {
  color: #666;
}

.logout-btn:hover {
  color: #FF6B35;
}

.user-icon {
  margin-right: 4px;
}

/* 主要内容 */
.main-content {
  flex: 1;
  background: #FFF5E6;
  overflow-y: auto;
  padding: 24px;
}

.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 16px rgba(255, 154, 61, 0.15);
  min-height: calc(100vh - 180px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.back-btn {
  color: #1890ff;
  font-size: 14px;
  padding: 0;
  height: auto;
  margin-right: 16px;
}

.back-btn:hover {
  color: #40a9ff;
}

.btn-icon {
  margin-right: 6px;
}

/* 编辑表单 */
.edit-form-container {
  max-width: 1000px;
  margin: 0 auto;
}

.edit-form {
  background: #fff;
  border-radius: 8px;
  padding: 0;
}

/* 上传组件样式 */
.upload-container {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  padding: 20px;
  background: #fafafa;
  transition: all 0.3s;
}

.upload-container:hover {
  border-color: #1890ff;
}

.upload-placeholder {
  text-align: center;
  color: #999;
}

.upload-icon {
  font-size: 32px;
  display: block;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 14px;
}

.image-preview {
  margin-top: 16px;
  text-align: center;
}

.preview-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.preview-image {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  margin-bottom: 8px;
}

.remove-btn {
  padding: 0;
  height: auto;
}

/* 加载提示 */
.loading-alert {
  margin-bottom: 24px;
}

/* 表单操作 */
.form-actions {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
  text-align: center;
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

  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .page-title {
    font-size: 24px;
    text-align: center;
  }
}
</style>