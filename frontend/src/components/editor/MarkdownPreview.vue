<template>
  <div class="markdown-preview">
    <MdPreview
      :editor-id="editorId"
      :modelValue="content"
      :theme="theme"
      :preview-theme="previewTheme"
      :code-theme="codeTheme"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { MdPreview } from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';

const props = defineProps({
  value: {
    type: String,
    default: ''
  },
  theme: {
    type: String,
    default: 'light'
  }
});

const previewTheme = 'github';
const codeTheme = 'atom';

const content = computed(() => props.value || '暂无内容');
const editorId = `preview-${Math.random().toString(36).slice(2, 10)}`;
</script>

<style scoped>
.markdown-preview {
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  box-shadow: var(--shadow-soft);
}

:deep(.md-editor-preview) {
  font-size: 15px;
  color: var(--text-secondary);
}

:deep(.md-editor-preview h1),
:deep(.md-editor-preview h2),
:deep(.md-editor-preview h3) {
  color: var(--text-primary);
  font-weight: 700;
}

:deep(pre code) {
  border-radius: 12px;
  font-size: 13px;
}
</style>
