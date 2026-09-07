<template>
  <el-dialog
    :model-value="visible"
    :title="editingItem ? '编辑 Agent' : '新增 Agent'"
    width="640px"
    @update:model-value="$emit('update:visible', $event)"
    @open="initForm"
    destroy-on-close
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入 Agent 名称" />
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="2"
          placeholder="请输入 Agent 描述"
        />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select
          v-model="form.type"
          placeholder="请选择类型"
          style="width: 100%"
        >
          <el-option label="对话" value="chat" />
          <el-option label="任务" value="task" />
          <el-option label="工作流" value="workflow" />
        </el-select>
      </el-form-item>
      <el-form-item label="配置" prop="setupId">
        <el-select
          v-model="form.setupId"
          placeholder="请选择 API 配置"
          style="width: 100%"
        >
          <el-option
            v-for="item in configOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="知识库" prop="knowledgeIds">
        <el-select
          v-model="form.knowledgeIds"
          placeholder="请选择关联知识库（可多选）"
          multiple
          clearable
          style="width: 100%"
        >
          <el-option
            v-for="item in knowledgeOptions"
            :key="item.id"
            :label="item.name"
            :value="Number(item.id)"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit"
        >确定</el-button
      >
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import type { Agent } from "@/types";
import { createAgent, updateAgent, getAgentById } from "@/api/agents";
import { getAllConfigs } from "@/api/config";
import { getAllKnowledgeBases } from "@/api/knowledge";
import type { ApiConfig, KnowledgeBase } from "@/types";

interface Emits {
  (e: "update:visible", val: boolean): void;
  (e: "success"): void;
}

const props = defineProps<{
  visible: boolean;
  editingItem: Agent | null;
}>();

const emit = defineEmits<Emits>();

const formRef = ref<FormInstance>();
const submitting = ref(false);

const form = reactive({
  name: "",
  description: "",
  type: "",
  setupId: "",
  knowledgeIds: [] as number[],
});

const rules: FormRules = {
  name: [{ required: true, message: "请输入 Agent 名称", trigger: "blur" }],
  setupId: [{ required: true, message: "请选择 API 配置", trigger: "change" }],
};

const configOptions = ref<ApiConfig[]>([]);
const knowledgeOptions = ref<KnowledgeBase[]>([]);

async function initForm() {
  try {
    const configs = await getAllConfigs();
    configOptions.value = configs;
  } catch {
    // 后端未启动时忽略
  }
  try {
    const knowledgeBases = await getAllKnowledgeBases();
    knowledgeOptions.value = knowledgeBases;
  } catch {
    // 后端未启动时忽略
  }

  if (props.editingItem) {
    try {
      const detail = await getAgentById(props.editingItem.id);
      form.name = detail.name;
      form.description = detail.description;
      form.type = detail.type;
      form.setupId = detail.setupId;
      form.knowledgeIds = detail.knowledgeIds
        ? detail.knowledgeIds.map(Number)
        : [];
    } catch {
      form.name = props.editingItem.name;
      form.description = props.editingItem.description;
      form.type = props.editingItem.type;
      form.setupId = props.editingItem.setupId;
      form.knowledgeIds = props.editingItem.knowledgeIds
        ? props.editingItem.knowledgeIds.map(Number)
        : [];
    }
  } else {
    form.name = "";
    form.description = "";
    form.type = "";
    form.setupId = "";
    form.knowledgeIds = [];
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  submitting.value = true;
  try {
    if (props.editingItem) {
      await updateAgent(props.editingItem.id, form);
      ElMessage.success("修改成功");
    } else {
      await createAgent({ ...form, status: "active" });
      ElMessage.success("新增成功");
    }
    emit("update:visible", false);
    emit("success");
  } finally {
    submitting.value = false;
  }
}
</script>
