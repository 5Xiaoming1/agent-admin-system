<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑配置' : '新增配置'"
    width="520px"
    :close-on-click-modal="false"
    @close="closeDialog"
  >
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <el-form-item label="名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入配置名称" />
      </el-form-item>
      <el-form-item label="API Key" prop="apiKey">
        <el-input
          v-model="formData.apiKey"
          placeholder="请输入 API Key"
          show-password
        />
      </el-form-item>
      <el-form-item label="Base URL" prop="baseUrl">
        <el-input v-model="formData.baseUrl" placeholder="请输入 Base URL" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="closeDialog">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import { useForm } from "@/composables";
import { createConfig, updateConfig } from "@/api/config";
import type { ApiConfig } from "@/types";

const emit = defineEmits<{
  success: [];
}>();

const defaultForm: Partial<ApiConfig> = {
  name: "",
  apiKey: "",
  baseUrl: "",
};

const rules: FormRules = {
  name: [{ required: true, message: "请输入配置名称", trigger: "blur" }],
  apiKey: [{ required: true, message: "请输入 API Key", trigger: "blur" }],
  baseUrl: [{ required: true, message: "请输入 Base URL", trigger: "blur" }],
};

const formRef = ref<FormInstance>();

const {
  visible,
  formData,
  isEdit,
  editId,
  submitting,
  openDialog,
  closeDialog,
  handleSubmit,
} = useForm<Partial<ApiConfig>>({
  defaultForm,
  submitApi: async (form) => {
    if (isEdit.value) {
      await updateConfig(editId.value, form);
      ElMessage.success("编辑成功");
    } else {
      await createConfig(form);
      ElMessage.success("新增成功");
    }
    emit("success");
  },
});

defineExpose({ openDialog });
</script>
