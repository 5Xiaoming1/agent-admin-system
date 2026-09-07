<template>
  <div class="code-editor">
    <div class="editor-container">
      <div class="line-numbers">
        <div
          v-for="line in lineCount"
          :key="line"
          class="line-number"
          :class="{ active: currentLine === line }"
        >
          {{ line }}
        </div>
      </div>
      <textarea
        ref="textareaRef"
        v-model="code"
        class="editor-textarea"
        :placeholder="placeholder"
        spellcheck="false"
        @keydown="handleKeydown"
        @input="handleInput"
        @scroll="syncScroll"
        @click="updateCursorPosition"
        @keyup="updateCursorPosition"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from "vue";

interface Props {
  modelValue: string;
  placeholder?: string;
}

interface Emits {
  (e: "update:modelValue", value: string): void;
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: "请输入代码",
});

const emit = defineEmits<Emits>();

const textareaRef = ref<HTMLTextAreaElement>();
const code = ref(props.modelValue);
const currentLine = ref(1);

const lineCount = computed(() => {
  return code.value.split("\n").length;
});

watch(
  () => props.modelValue,
  (val) => {
    code.value = val;
  },
);

watch(code, (val) => {
  emit("update:modelValue", val);
});

function handleInput() {
  updateCursorPosition();
}

function handleKeydown(e: KeyboardEvent) {
  const textarea = textareaRef.value;
  if (!textarea) return;

  const { selectionStart, selectionEnd, value } = textarea;

  // Tab key - insert spaces for indentation
  if (e.key === "Tab") {
    e.preventDefault();
    const indent = "  ";
    const newValue =
      value.substring(0, selectionStart) +
      indent +
      value.substring(selectionEnd);
    code.value = newValue;
    nextTick(() => {
      textarea.selectionStart = textarea.selectionEnd =
        selectionStart + indent.length;
    });
    return;
  }

  // Enter key - auto indent
  if (e.key === "Enter") {
    e.preventDefault();
    const lines = value.substring(0, selectionStart).split("\n");
    const currentLineText = lines[lines.length - 1];
    const indentMatch = currentLineText.match(/^(\s*)/);
    let indent = indentMatch ? indentMatch[1] : "";

    // Check if the current line ends with { or (
    const trimmedLine = currentLineText.trimEnd();
    if (trimmedLine.endsWith("{") || trimmedLine.endsWith("(")) {
      indent += "  ";
    }

    const newValue =
      value.substring(0, selectionStart) +
      "\n" +
      indent +
      value.substring(selectionEnd);
    code.value = newValue;
    nextTick(() => {
      const newPos = selectionStart + 1 + indent.length;
      textarea.selectionStart = textarea.selectionEnd = newPos;
    });
    return;
  }

  // Auto close brackets
  const bracketPairs: Record<string, string> = {
    "(": ")",
    "[": "]",
    "{": "}",
    '"': '"',
    "'": "'",
    "`": "`",
  };

  if (bracketPairs[e.key]) {
    const closingBracket = bracketPairs[e.key];
    const selectedText = value.substring(selectionStart, selectionEnd);

    if (selectedText) {
      // Wrap selected text
      e.preventDefault();
      const newValue =
        value.substring(0, selectionStart) +
        e.key +
        selectedText +
        closingBracket +
        value.substring(selectionEnd);
      code.value = newValue;
      nextTick(() => {
        textarea.selectionStart = selectionStart + 1;
        textarea.selectionEnd = selectionEnd + 1;
      });
      return;
    }

    // Check if we should auto-close
    const nextChar = value[selectionStart];
    if (
      !nextChar ||
      /\s/.test(nextChar) ||
      nextChar === ")" ||
      nextChar === "]" ||
      nextChar === "}"
    ) {
      e.preventDefault();
      const newValue =
        value.substring(0, selectionStart) +
        e.key +
        closingBracket +
        value.substring(selectionEnd);
      code.value = newValue;
      nextTick(() => {
        textarea.selectionStart = textarea.selectionEnd = selectionStart + 1;
      });
    }
  }

  // Auto close when typing closing bracket
  if (e.key === ")" || e.key === "]" || e.key === "}") {
    const nextChar = value[selectionStart];
    if (nextChar === e.key) {
      e.preventDefault();
      nextTick(() => {
        textarea.selectionStart = textarea.selectionEnd = selectionStart + 1;
      });
    }
  }

  // Backspace - auto remove matching bracket
  if (
    e.key === "Backspace" &&
    selectionStart === selectionEnd &&
    selectionStart > 0
  ) {
    const prevChar = value[selectionStart - 1];
    const nextChar = value[selectionStart];
    const bracketPairsReverse: Record<string, string> = {
      ")": "(",
      "]": "[",
      "}": "{",
      '"': '"',
      "'": "'",
      "`": "`",
    };

    if (
      bracketPairsReverse[prevChar] &&
      bracketPairsReverse[prevChar] === nextChar
    ) {
      e.preventDefault();
      const newValue =
        value.substring(0, selectionStart - 1) +
        value.substring(selectionStart + 1);
      code.value = newValue;
      nextTick(() => {
        textarea.selectionStart = textarea.selectionEnd = selectionStart - 1;
      });
    }
  }
}

function syncScroll() {
  const textarea = textareaRef.value;
  if (!textarea) return;
  const lineNumbers = textarea.parentElement?.querySelector(".line-numbers");
  if (lineNumbers) {
    lineNumbers.scrollTop = textarea.scrollTop;
  }
}

function updateCursorPosition() {
  const textarea = textareaRef.value;
  if (!textarea) return;
  const textBeforeCursor = textarea.value.substring(0, textarea.selectionStart);
  currentLine.value = textBeforeCursor.split("\n").length;
}
</script>

<style scoped lang="scss">
.code-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  background: #1e1e1e;

  .editor-container {
    display: flex;
    height: 100%;
    min-height: 400px;

    .line-numbers {
      width: 50px;
      background: #2d2d2d;
      color: #858585;
      font-family: Consolas, Monaco, "Courier New", monospace;
      font-size: 14px;
      line-height: 1.6;
      padding: 12px 0;
      overflow: hidden;
      user-select: none;
      text-align: right;
      border-right: 1px solid #3e3e3e;

      .line-number {
        padding: 0 12px 0 8px;
        height: 22.4px;

        &.active {
          color: #c6c6c6;
          background: #2a2d2e;
        }
      }
    }

    .editor-textarea {
      flex: 1;
      background: #1e1e1e;
      color: #d4d4d4;
      font-family: Consolas, Monaco, "Courier New", monospace;
      font-size: 14px;
      line-height: 1.6;
      padding: 12px;
      border: none;
      outline: none;
      resize: none;
      tab-size: 2;
      white-space: pre;
      overflow-wrap: normal;
      overflow-x: auto;

      &::placeholder {
        color: #6a6a6a;
      }

      &::-webkit-scrollbar {
        width: 10px;
        height: 10px;
      }

      &::-webkit-scrollbar-track {
        background: #1e1e1e;
      }

      &::-webkit-scrollbar-thumb {
        background: #424242;
        border-radius: 5px;

        &:hover {
          background: #4f4f4f;
        }
      }
    }
  }
}
</style>
