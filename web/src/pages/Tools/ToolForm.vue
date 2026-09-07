<template>
  <el-dialog
    :model-value="visible"
    :title="editingItem ? '编辑工具' : '新增工具'"
    width="540px"
    @update:model-value="$emit('update:visible', $event)"
    @open="initForm"
    destroy-on-close
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入工具名称" />
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入工具描述"
        />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select
          v-model="form.type"
          placeholder="请选择类型"
          style="width: 100%"
        >
          <el-option label="API" value="api" />
          <el-option label="函数" value="function" />
          <el-option label="内置" value="builtin" />
        </el-select>
      </el-form-item>
      <el-form-item label="代码" prop="code">
        <div class="code-input-wrapper">
          <el-input
            v-model="form.code"
            type="textarea"
            :rows="1"
            placeholder="请输入工具代码"
            readonly
            class="code-preview-input"
          />
          <el-button type="primary" @click="codeEditorVisible = true">
            编写代码
          </el-button>
        </div>
      </el-form-item>
      <el-form-item label="参数说明" prop="parameterDescriptions">
        <el-input
          v-model="form.parameterDescriptions"
          type="textarea"
          :rows="5"
          placeholder='请输入 JSON 格式参数说明，如 {"参数名": "参数说明"}'
          @blur="validateJson"
        />
        <span v-if="jsonError" class="json-error">{{ jsonError }}</span>
      </el-form-item>
      <el-form-item label="接口地址" prop="endpoint">
        <el-input
          v-model="form.endpoint"
          placeholder="请输入接口地址（api 类型工具可填）"
        />
      </el-form-item>
      <el-form-item label="参数配置" prop="parameters">
        <el-input
          v-model="form.parameters"
          type="textarea"
          :rows="3"
          placeholder='请输入 JSON 格式参数配置，如 {"city": "string"}'
          @blur="validateParametersJson"
        />
        <span v-if="parametersError" class="json-error">
          {{ parametersError }}
        </span>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit"
        >确定</el-button
      >
    </template>
  </el-dialog>

  <!-- 代码编辑器弹窗 -->
  <el-dialog
    v-model="codeEditorVisible"
    title="编写工具代码"
    width="800px"
    destroy-on-close
    class="code-editor-dialog"
  >
    <CodeEditor v-model="codeEditorContent" placeholder="请输入工具代码" />
    <template #footer>
      <el-button @click="codeEditorVisible = false">取消</el-button>
      <el-button type="primary" @click="handleCodeConfirm">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from "vue";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import type { Tool } from "@/types";
import { createTool, updateTool, getToolById } from "@/api/tools";
import CodeEditor from "@/components/CodeEditor.vue";

interface Emits {
  (e: "update:visible", val: boolean): void;
  (e: "success"): void;
}

const props = defineProps<{
  visible: boolean;
  editingItem: Tool | null;
}>();

const emit = defineEmits<Emits>();

const formRef = ref<FormInstance>();
const submitting = ref(false);
const jsonError = ref("");
const parametersError = ref("");
const codeEditorVisible = ref(false);
const codeEditorContent = ref("");

watch(codeEditorVisible, (val) => {
  if (val) {
    codeEditorContent.value = form.code;
  }
});

const form = reactive({
  name: "",
  description: "",
  type: "api" as "api" | "function" | "builtin",
  code: "",
  endpoint: "",
  parameterDescriptions: "",
  parameters: "",
});

const rules: FormRules = {
  name: [{ required: true, message: "请输入工具名称", trigger: "blur" }],
  type: [{ required: true, message: "请选择类型", trigger: "change" }],
  parameterDescriptions: [
    {
      validator: (_rule, _value, callback) => {
        if (jsonError.value) {
          callback(new Error(jsonError.value));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
  parameters: [
    {
      validator: (_rule, _value, callback) => {
        if (parametersError.value) {
          callback(new Error(parametersError.value));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
};

function toJsonText(value: unknown): string {
  if (!value) return "";
  if (typeof value === "string") return value;
  try {
    return JSON.stringify(value, null, 2);
  } catch {
    return String(value);
  }
}

function parseJsonObject(text: string): Record<string, string> {
  const trimmed = text.trim();
  if (!trimmed) return {};
  const parsed = JSON.parse(trimmed);
  return parsed && typeof parsed === "object" ? parsed : {};
}

function validateJson() {
  jsonError.value = "";
  if (!form.parameterDescriptions.trim()) return;
  try {
    JSON.parse(form.parameterDescriptions);
  } catch {
    jsonError.value = "JSON 格式不正确";
  }
}

function validateParametersJson() {
  parametersError.value = "";
  if (!form.parameters.trim()) return;
  try {
    JSON.parse(form.parameters);
  } catch {
    parametersError.value = "JSON 格式不正确";
  }
}

async function initForm() {
  jsonError.value = "";
  parametersError.value = "";
  if (props.editingItem) {
    try {
      const detail = await getToolById(props.editingItem.id);
      form.name = detail.name;
      form.description = detail.description;
      form.type = detail.type;
      form.code = detail.code || "";
      form.endpoint = detail.endpoint || "";
      form.parameterDescriptions = toJsonText(detail.parameterDescriptions);
      form.parameters = toJsonText(detail.parameters);
    } catch {
      form.name = props.editingItem.name;
      form.description = props.editingItem.description;
      form.type = props.editingItem.type;
      form.code = props.editingItem.code || "";
      form.endpoint = props.editingItem.endpoint || "";
      form.parameterDescriptions = toJsonText(
        props.editingItem.parameterDescriptions,
      );
      form.parameters = toJsonText(props.editingItem.parameters);
    }
  } else {
    form.name = "";
    form.description = "";
    form.type = "api";
    form.code = "";
    form.endpoint = "";
    form.parameterDescriptions = "";
    form.parameters = "";
  }
}

function handleCodeConfirm() {
  form.code = codeEditorContent.value;
  codeEditorVisible.value = false;
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  validateJson();
  validateParametersJson();
  if (jsonError.value || parametersError.value) {
    ElMessage.error("参数 JSON 格式不正确");
    return;
  }

  submitting.value = true;
  try {
    const payload = {
      name: form.name,
      description: form.description,
      type: form.type,
      code: form.code,
      endpoint: form.endpoint.trim() || undefined,
      parameterDescriptions: form.parameterDescriptions.trim()
        ? parseJsonObject(form.parameterDescriptions)
        : undefined,
      parameters: form.parameters.trim()
        ? parseJsonObject(form.parameters)
        : undefined,
    };

    if (props.editingItem) {
      await updateTool(props.editingItem.id, payload);
      ElMessage.success("修改成功");
    } else {
      await createTool(payload);
      ElMessage.success("新增成功");
    }
    emit("update:visible", false);
    emit("success");
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.json-error {
  color: #f56c6c;
  font-size: 12px;
  line-height: 1.5;
}

.code-input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  width: 100%;
}

.code-input-wrapper :deep(.el-textarea) {
  flex: 1;
}

.code-preview-input :deep(textarea) {
  font-family: Consolas, Monaco, "Courier New", monospace;
  font-size: 13px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>

<style lang="scss">
.code-editor-dialog {
  .el-dialog__body {
    padding: 20px;
  }
}
</style>
