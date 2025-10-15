<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <span class="logo-icon">🦆</span>
          <span class="logo-text">面试鸭刷题神器</span>
        </div>
        
        <nav class="nav-menu">
          <a-button type="link" @click="$router.push('/')">主页</a-button>
          <a-button type="link" @click="scrollToQuestionBank">题库</a-button>
          <a-button type="link">刷题路线</a-button>
          <a-button type="link">26届秋招热题</a-button>
          <a-button type="link">真实面经</a-button>
          <a-button type="link" v-if="isAdmin" @click="$router.push('/questions')">题目管理</a-button>
        </nav>
        
        <div class="header-right">
          <a-input-search 
            placeholder="搜索题目" 
            style="width: 200px"
            class="search-input"
          />
          <a-dropdown>
            <a-button type="text" class="user-btn">
              <span class="user-icon">👤</span>
              <span>{{ user ? user.userName : '未登录' }}</span>
            </a-button>
            <template #overlay>
              <a-menu>
                <a-menu-item key="1" @click="$router.push('/personal-center')">
                  <span>👤 个人中心</span>
                </a-menu-item>
                <a-menu-item key="2" v-if="isAdmin" @click="$router.push('/users')">
                  <span>👥 用户管理</span>
                </a-menu-item>
                <a-menu-item key="3" v-if="isAdmin" @click="$router.push('/questions')">
                  <span>📝 题目管理</span>
                </a-menu-item>
                <a-menu-item key="4" v-if="isAdmin" @click="$router.push('/question-banks')">
                  <span>📚 题库管理</span>
                </a-menu-item>
                <a-menu-divider v-if="isAdmin" />
                <a-menu-item key="5" @click="handleLogout">
                  <span>🚪 退出登录</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <!-- 分类标签区域 -->
      <div class="category-section">
        <div class="category-container">
          <div class="category-header">
            <a-button 
              :type="activeTab === '开刷' ? 'primary' : 'default'"
              @click="activeTab = '开刷'"
              class="tab-btn"
            >
              <span class="tab-icon">🔥</span>
              开刷
            </a-button>
            <a-button 
              :type="activeTab === '热门' ? 'primary' : 'default'"
              @click="activeTab = '热门'"
              class="tab-btn hot-tab"
            >
              <span class="tab-icon">🔥</span>
              热门
            </a-button>
          </div>

          <!-- 分类标签 -->
          <div class="tags-wrapper">
            <a-tag 
              v-for="tag in categoryTags" 
              :key="tag"
              :color="selectedTag === tag ? 'orange' : 'default'"
              @click="selectedTag = tag"
              class="category-tag"
            >
              {{ tag }}
            </a-tag>
          </div>
        </div>
      </div>

      <!-- 题库卡片区域 -->
      <div class="question-bank-section" ref="questionBankSection">
        <div class="cards-container">
          <a-row :gutter="[24, 24]" v-if="loading">
            <!-- 加载状态 -->
            <a-col :xs="24" :sm="12" :md="8" :lg="6" v-for="i in 12" :key="i">
              <a-skeleton active :paragraph="{ rows: 3 }" />
            </a-col>
          </a-row>
          <a-row :gutter="[24, 24]" v-else-if="questionBanks.length > 0">
            <!-- 真实题库数据 -->
            <a-col :xs="24" :sm="12" :md="8" :lg="6" v-for="bank in questionBanks" :key="bank.id">
              <div class="question-card" @click="goToQuestionBank(bank.id)">
                <div class="card-icon" :class="getIconClass(bank.title)">
                  <span>{{ getIcon(bank.title) }}</span>
                </div>
                <h3 class="card-title">{{ bank.title }}</h3>
                <p class="card-desc">{{ bank.description || '暂无描述' }}</p>
                <div class="card-badge hot-badge" v-if="isHotBank(bank.title)">HOT</div>
              </div>
            </a-col>
          </a-row>
          <a-empty v-else description="暂无题库数据" />
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
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { message } from 'ant-design-vue';
import { getQuestionBankList } from '../api/questionBank';

export default {
  name: 'Home',
  setup() {
    const router = useRouter();
    const store = useStore();
    const user = computed(() => store.state.user);
    
    const activeTab = ref('开刷');
    const selectedTag = ref('热门');
    const questionBankSection = ref(null);
    const questionBanks = ref([]);
    const loading = ref(false);
    
    const categoryTags = [
      '热门', '后端', '前端', 'Java', 'C++', 'Python', 'Go', 'PHP',
      '真实面经', '项目', '测试', '运维', '计算机网络', '操作系统',
      '数据库', '计算机基础', '大数据', '考研', '移动开发', '人工智能',
      '游戏开发', '算法', '其他', '全部'
    ];

    // 获取题库数据
    const fetchQuestionBanks = async () => {
      loading.value = true;
      try {
        const params = {
          current: 1,
          size: 50 // 获取前50个题库
        };
        const response = await getQuestionBankList(params);
        if (response.code === 200 && response.data) {
          questionBanks.value = response.data.records || [];
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

    // 根据题库名称获取图标
    const getIcon = (title) => {
      if (title.includes('Java')) {
        return '☕';
      } else if (title.includes('MySQL') || title.includes('数据库')) {
        return '🐬';
      } else if (title.includes('Redis')) {
        return '📮';
      } else if (title.includes('Spring')) {
        return '🌱';
      } else if (title.includes('网络')) {
        return '🌐';
      } else if (title.includes('操作系统') || title.includes('OS')) {
        return '💻';
      } else if (title.includes('算法')) {
        return '🧮';
      } else if (title.includes('前端')) {
        return '🎨';
      } else {
        return '📚'; // 默认图标
      }
    };

    // 根据题库名称获取图标样式类
    const getIconClass = (title) => {
      if (title.includes('Java')) {
        return 'java-icon';
      } else if (title.includes('MySQL') || title.includes('数据库')) {
        return 'mysql-icon';
      } else if (title.includes('Redis')) {
        return 'redis-icon';
      } else if (title.includes('Spring')) {
        return 'spring-icon';
      } else if (title.includes('网络')) {
        return 'network-icon';
      } else if (title.includes('操作系统') || title.includes('OS')) {
        return 'os-icon';
      } else if (title.includes('算法')) {
        return 'algorithm-icon';
      } else if (title.includes('前端')) {
        return 'frontend-icon';
      } else {
        return 'java-basic-icon'; // 默认样式
      }
    };

    // 判断是否为热门题库
    const isHotBank = (title) => {
      return title.includes('热门') || title.includes('HOT') || title.includes('最新');
    };

    // 跳转到题库详情页
    const goToQuestionBank = (id) => {
      router.push(`/question-bank/${id}`);
    };

    const scrollToQuestionBank = () => {
      questionBankSection.value?.scrollIntoView({ behavior: 'smooth' });
    };

    const handleLogout = () => {
      store.dispatch('logout');
      message.success('已退出登录');
      router.push('/login');
    };

    // 组件挂载时获取题库数据
    onMounted(() => {
      fetchQuestionBanks();
    });

    return {
      user,
      activeTab,
      selectedTag,
      categoryTags,
      questionBankSection,
      questionBanks,
      loading,
      getIcon,
      getIconClass,
      isHotBank,
      goToQuestionBank,
      scrollToQuestionBank,
      handleLogout
    };
  }
};
</script>

<style scoped>
.home-container {
  height: 100vh;
  width: 100vw;
  background: #FFF5E6;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 头部导航 */
.header {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 8px 24px;
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

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-input :deep(.ant-input) {
  border-radius: 20px;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #333;
}

.user-icon {
  font-size: 18px;
}

/* 主要内容区域 */
.main-content {
  width: 100%;
  min-height: calc(100vh - 68px);
}

/* Banner区域 */
.banner-section {
  background: linear-gradient(135deg, #FFE8CC 0%, #FFF5E6 100%);
  padding: 60px 24px;
  width: 100%;
}

.banner-content {
  max-width: 1400px;
  margin: 0 auto;
  text-align: center;
}

.banner-title {
  font-size: 48px;
  font-weight: 700;
  color: #333;
  margin-bottom: 16px;
}

.banner-desc {
  font-size: 18px;
  color: #666;
}

/* 分类区域 */
.category-section {
  background: transparent;
  padding: 16px 24px 12px;
  margin-top: 0;
  width: 100%;
}

.category-container {
  max-width: 1400px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 16px 20px;
  backdrop-filter: blur(10px);
}

.category-header {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.tab-btn {
  border-radius: 20px;
  height: 36px;
  padding: 0 20px;
  font-weight: 500;
}

.tab-btn :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #FF9A3D 0%, #FF6B35 100%);
  border: none;
}

.hot-tab .tab-icon {
  animation: fire 1s ease-in-out infinite;
}

@keyframes fire {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

.tab-icon {
  margin-right: 4px;
}

.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.category-tag {
  cursor: pointer;
  padding: 6px 16px;
  border-radius: 16px;
  font-size: 14px;
  transition: all 0.3s;
}

.category-tag:hover {
  transform: translateY(-2px);
}

/* 题库卡片区域 */
.question-bank-section {
  background: transparent;
  padding: 20px 24px 60px;
}

.cards-container {
  max-width: 1400px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 32px;
  backdrop-filter: blur(10px);
}

.question-card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid #FFE8CC;
  position: relative;
  min-height: 200px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 12px rgba(255, 154, 61, 0.15);
}

.question-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 24px rgba(255, 154, 61, 0.2);
  border-color: #FF9A3D;
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-bottom: 16px;
}

.java-icon { background: linear-gradient(135deg, #F89820 0%, #FF6B35 100%); }
.java-basic-icon { background: linear-gradient(135deg, #667EEA 0%, #764BA2 100%); }
.collection-icon { background: linear-gradient(135deg, #F093FB 0%, #F5576C 100%); }
.concurrent-icon { background: linear-gradient(135deg, #4FACFE 0%, #00F2FE 100%); }
.mysql-icon { background: linear-gradient(135deg, #FA709A 0%, #FEE140 100%); }
.redis-icon { background: linear-gradient(135deg, #FF6B6B 0%, #FFE66D 100%); }
.spring-icon { background: linear-gradient(135deg, #6FD195 0%, #A7E6B7 100%); }
.springboot-icon { background: linear-gradient(135deg, #667EEA 0%, #764BA2 100%); }
.network-icon { background: linear-gradient(135deg, #89F7FE 0%, #66A6FF 100%); }
.os-icon { background: linear-gradient(135deg, #A8EDEA 0%, #FED6E3 100%); }
.algorithm-icon { background: linear-gradient(135deg, #FFD89B 0%, #19547B 100%); }
.frontend-icon { background: linear-gradient(135deg, #FFA8A8 0%, #FCFF00 100%); }

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  flex-grow: 0;
}

.card-desc {
  font-size: 13px;
  color: #999;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex-grow: 1;
  margin-bottom: auto;
}

.card-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  align-self: flex-start;
}

.hot-badge {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  color: white;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

/* 底部 */
.footer {
  background: #333;
  color: rgba(255, 255, 255, 0.65);
  text-align: center;
  padding: 24px;
  font-size: 14px;
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
  
  .banner-title {
    font-size: 32px;
  }
  
  .banner-desc {
    font-size: 16px;
  }
  
  .category-header {
    justify-content: center;
  }
  
  .tags-wrapper {
    justify-content: center;
  }
}
</style>
