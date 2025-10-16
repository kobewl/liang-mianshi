<template>
  <div :class="['page-shell', `page-shell--${variant}`]">
    <AppHeader
      :nav-items="navItems"
      :pinned="headerPinned"
      @nav-click="forwardNav"
    />

    <section v-if="$slots.hero || hero" class="page-shell__hero">
      <slot name="hero">
        <div class="hero-card glass-card">
          <div class="hero-card__content">
            <p v-if="hero?.badge" class="hero-badge">{{ hero.badge }}</p>
            <h1>{{ hero?.title }}</h1>
            <p v-if="hero?.subtitle">{{ hero.subtitle }}</p>
          </div>
          <div v-if="hero?.extra" class="hero-card__extra">
            <slot name="hero-extra">{{ hero.extra }}</slot>
          </div>
        </div>
      </slot>
    </section>

    <main class="page-shell__main">
      <div :class="['page-shell__content', { 'page-shell__content--flush': !contentPadding }]">
        <slot />
      </div>
    </main>

    <AppFooter v-if="showFooter" />
  </div>
</template>

<script setup>
import AppHeader from './AppHeader.vue';
import AppFooter from './AppFooter.vue';

const props = defineProps({
  variant: {
    type: String,
    default: 'public'
  },
  hero: {
    type: Object,
    default: null
  },
  navItems: {
    type: Array,
    default: null
  },
  showFooter: {
    type: Boolean,
    default: true
  },
  headerPinned: {
    type: Boolean,
    default: false
  },
  contentPadding: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['nav-click']);

const forwardNav = (item) => {
  emit('nav-click', item);
};
</script>

<style scoped>
.page-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.page-shell__hero {
  width: min(1180px, 92vw);
  margin: 36px auto 0;
}

.hero-card {
  padding: 32px 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 28px;
  position: relative;
  overflow: hidden;
}

.hero-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at top right, rgba(59, 130, 246, 0.12), transparent 55%);
  pointer-events: none;
}

.hero-card__content h1 {
  font-size: clamp(28px, 4vw, 38px);
  line-height: 1.2;
  font-weight: 800;
  color: var(--text-primary);
}

.hero-card__content p {
  margin-top: 12px;
  font-size: 16px;
  color: var(--text-secondary);
  max-width: 600px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(59, 130, 246, 0.12);
  color: #2563eb;
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 12px;
}

.hero-card__extra {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-shell__main {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 36px 0 48px;
}

.page-shell__content {
  width: min(1180px, 92vw);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.page-shell__content--flush {
  width: 100%;
  padding: 0;
}

@media (max-width: 992px) {
  .hero-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-card__extra {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 640px) {
  .page-shell__main {
    padding: 24px 0 36px;
  }

  .page-shell__content {
    width: min(100%, 94vw);
  }
}
</style>
