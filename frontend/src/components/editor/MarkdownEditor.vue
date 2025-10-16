<template>
  <div class="markdown-editor" :class="{ 'markdown-editor--compact': compact }">
    <div class="markdown-editor__toolbar">
      <div class="toolbar-left">
        <slot name="label">
          <span class="toolbar-title">{{ label }}</span>
        </slot>
        <span v-if="description" class="toolbar-desc">{{ description }}</span>
      </div>
      <div class="toolbar-right">
        <a-segmented
          v-if="allowModes"
          v-model:value="mode"
          :options="[
            { label: '编辑', value: 'edit' },
            { label: '预览', value: 'preview' },
            { label: '分屏', value: 'both' }
          ]"
          size="small"
        />
        <a-tooltip title="同步滚动">
          <a-switch v-model:checked="syncScroll" size="small" />
        </a-tooltip>
      </div>
    </div>

    <MdEditor
      v-model="innerValue"
      :preview="mode !== 'edit'"
      :show-code-row-number="true"
      :theme="theme"
      :preview-theme="previewTheme"
      :code-theme="codeTheme"
      :toolbars="toolbars"
      :toolbars-exclude="toolbarsExclude"
      :placeholder="placeholder"
      :preview-only="mode === 'preview'"
      :auto-fold="false"
      :auto-detect-code-language="true"
      :tab-width="2"
      :no-katex="true"
      :footers="[]"
      :class="{ 'markdown-editor__pane--sync': syncScroll }"
      :style="{ height: editorHeight }"
      @onChange="handleChange"
    />
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { MdEditor } from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  label: {
    type: String,
    default: 'Markdown 编辑'
  },
  description: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '支持 Markdown 语法，点击工具栏插入格式化内容。'
  },
  height: {
    type: [Number, String],
    default: 420
  },
  compact: {
    type: Boolean,
    default: false
  },
  allowModes: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['update:modelValue', 'change']);

const innerValue = ref(props.modelValue || '');

const mode = ref('both');
const syncScroll = ref(true);

const theme = 'light';
const previewTheme = 'github';
const codeTheme = 'atom';

const toolbars = [
  'undo',
  'redo',
  'bold',
  'italic',
  'underline',
  'strikeThrough',
  'title',
  'sub',
  'sup',
  'quote',
  'unorderedList',
  'orderedList',
  'task',
  'codeRow',
  'code',
  'link',
  'image',
  'table',
  'pageFullscreen',
  'preview',
  'previewOnly',
  'catalog',
  'github'
];

const toolbarsExclude = ['katex'];

const editorHeight = computed(() => {
  return typeof props.height === 'number' ? `${props.height}px` : props.height;
});

watch(
  () => props.modelValue,
  (val) => {
    if (val !== innerValue.value) {
      innerValue.value = val || '';
    }
  }
);

const handleChange = (val) => {
  emit('update:modelValue', val);
  emit('change', val);
};
</script>

<style scoped>
.markdown-editor {
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.28);
  overflow: hidden;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow-soft);
}

.markdown-editor--compact {
  border-radius: 14px;
}

.markdown-editor__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.24);
  background: rgba(248, 250, 252, 0.85);
}

.toolbar-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.toolbar-title {
  font-weight: 600;
  color: var(--text-primary);
}

.toolbar-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

:deep(.md-editor) {
  border: none;
  box-shadow: none;
  background: transparent;
}

:deep(.md-editor-toolbar) {
  background: transparent;
  border-bottom: 1px solid rgba(148, 163, 184, 0.16);
}

:deep(.md-editor-toolbar .md-editor-toolbar-item) {
  border-radius: 8px;
}

:deep(.md-editor-content) {
  background: transparent;
}

:deep(.md-editor-preview-wrapper) {
  background: rgba(248, 250, 252, 0.72);
}

:deep(.md-editor-preview) {
  padding: 18px 24px;
}

:deep(.md-editor .md-editor-content .md-editor-textarea) {
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 14px;
}

@media (max-width: 768px) {
  .markdown-editor__toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
