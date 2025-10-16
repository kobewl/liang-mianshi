<template>
  <header :class="['app-header', { 'app-header--pinned': isPinned }]">
    <div class="app-header__inner">
      <div class="app-header__brand" @click="goHome">
        <div class="brand-icon">🦆</div>
        <div class="brand-copy">
          <span class="brand-title">面试鸭刷题神</span>
          <span class="brand-subtitle">Interview Mastery Hub</span>
        </div>
      </div>

      <nav class="app-header__nav" aria-label="Primary Navigation">
        <a-button
          v-for="item in computedNavItems"
          :key="item.key"
          type="text"
          class="nav-button"
          :ghost="true"
          :disabled="item.disabled"
          :class="{ 'nav-button--active': isActive(item) }"
          @click="handleNav(item)"
        >
          <span>{{ item.label }}</span>
        </a-button>
      </nav>

      <div class="app-header__right">
        <slot name="right"></slot>

        <template v-if="isLoggedIn">
          <a-dropdown trigger="click">
            <a-button type="text" class="user-chip">
              <a-avatar :size="36" class="user-avatar">
                {{ avatarInitials }}
              </a-avatar>
              <div class="user-meta">
                <span class="user-name">{{ user?.userName || user?.userAccount }}</span>
                <span class="user-role">{{ isAdmin ? '管理员' : '用户' }}</span>
              </div>
              <span class="user-caret">⌄</span>
            </a-button>
            <template #overlay>
              <a-menu>
                <a-menu-item key="profile" @click="router.push('/personal-center')">
                  个人中心
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout" danger @click="logout">
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </template>

        <template v-else>
          <a-space>
            <a-button shape="round" @click="router.push('/login')">登录</a-button>
            <a-button type="primary" shape="round" @click="router.push('/register')">
              开始体验
            </a-button>
          </a-space>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useStore } from 'vuex';

const props = defineProps({
  navItems: {
    type: Array,
    default: null
  },
  pinned: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['nav-click']);

const store = useStore();
const route = useRoute();
const router = useRouter();

const user = computed(() => store.state.user);
const isLoggedIn = computed(() => store.state.isLoggedIn);
const isAdmin = computed(() => (user.value?.userRole || '').toLowerCase() === 'admin');

const defaultNavItems = computed(() => {
  if (isAdmin.value) {
    return [
      { key: 'home', label: '首页', path: '/' },
      { key: 'question-manage', label: '题目列表', path: '/questions' },
      { key: 'bank-manage', label: '题库管理', path: '/question-banks' },
      { key: 'user-manage', label: '用户管理', path: '/users' }
    ];
  }

  return [
    { key: 'home', label: '首页', path: '/' },
    { key: 'question-square', label: '题库广场', event: 'scroll-question-banks' },
    { key: 'roadmap', label: '刷题路线', disabled: true },
    { key: 'stories', label: '真实面经', disabled: true }
  ];
});

const computedNavItems = computed(() => props.navItems?.length ? props.navItems : defaultNavItems.value);

const isPinned = computed(() => props.pinned);

const avatarInitials = computed(() => {
  const name = user.value?.userName || user.value?.userAccount || '';
  return name ? name.slice(0, 1).toUpperCase() : 'U';
});

const goHome = () => {
  router.push('/');
};

const isActive = (item) => {
  if (!item.path) return false;
  return route.path === item.path || route.name === item.key;
};

const handleNav = (item) => {
  if (item.disabled) return;
  if (item.path) {
    router.push(item.path);
  } else {
    emit('nav-click', item);
  }
};

const logout = () => {
  store.dispatch('logout');
  router.push('/login');
};
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 20;
  width: 100%;
  padding: 18px 0;
  backdrop-filter: blur(18px);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(245, 247, 251, 0.78));
  border-bottom: 1px solid rgba(148, 163, 184, 0.24);
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.08);
}

.app-header--pinned {
  position: relative;
}

.app-header__inner {
  width: min(1180px, 92vw);
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.app-header__brand {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.25s ease;
}

.app-header__brand:hover {
  transform: translateY(-2px);
}

.brand-icon {
  width: 44px;
  height: 44px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  font-size: 24px;
  background: radial-gradient(circle at 30% 20%, #ffe7d4, rgba(255, 148, 74, 0.9));
  box-shadow: 0 10px 22px rgba(255, 148, 74, 0.32);
}

.brand-copy {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.brand-title {
  font-weight: 700;
  font-size: 18px;
  color: var(--text-primary);
}

.brand-subtitle {
  font-size: 12px;
  color: var(--text-secondary);
  letter-spacing: 0.5px;
}

.app-header__nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-button {
  border-radius: 24px;
  padding: 0 14px;
  height: 38px;
  color: var(--text-secondary);
  font-weight: 500;
}

.nav-button--active {
  color: var(--brand-primary);
  background: rgba(255, 122, 69, 0.14);
  box-shadow: 0 8px 16px rgba(255, 122, 69, 0.18);
}

.nav-button:not(.nav-button--active):hover {
  color: var(--brand-primary);
  background: rgba(255, 122, 69, 0.1);
}

.app-header__right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-chip {
  padding: 4px 10px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(148, 163, 184, 0.14);
  border: none;
  transition: var(--transition-base);
}

.user-chip:hover {
  background: rgba(148, 163, 184, 0.24);
}

.user-avatar {
  background: linear-gradient(135deg, rgba(255, 122, 69, 0.95), rgba(255, 148, 74, 0.8));
  color: #fff;
  font-weight: 600;
}

.user-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  line-height: 1.1;
}

.user-name {
  font-weight: 600;
  font-size: 14px;
}

.user-role {
  font-size: 12px;
  color: var(--text-secondary);
}

.user-caret {
  font-size: 16px;
  color: rgba(15, 23, 42, 0.35);
  margin-left: 2px;
}

@media (max-width: 992px) {
  .app-header__inner {
    flex-wrap: wrap;
    justify-content: center;
    gap: 16px;
  }

  .app-header__nav {
    width: 100%;
    justify-content: center;
    flex-wrap: wrap;
  }

  .app-header__right {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 640px) {
  .app-header {
    padding: 14px 0;
  }

  .brand-copy {
    display: none;
  }
}
</style>
