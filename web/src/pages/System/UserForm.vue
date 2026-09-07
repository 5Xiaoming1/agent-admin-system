<template>
  <el-dialog
    :key="editingItem ? `edit-${editingItem.id}` : 'add'"
    :model-value="visible"
    :title="editingItem ? '编辑用户' : '新增用户'"
    width="520px"
    @update:model-value="$emit('update:visible', $event)"
    destroy-on-close
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="80px"
      autocomplete="off"
    >
      <el-form-item label="头像">
        <el-upload
          class="avatar-uploader"
          :show-file-list="false"
          :http-request="handleAvatarUpload"
          :before-upload="beforeAvatarUpload"
        >
          <img v-if="form.avatar" :src="form.avatar" class="avatar" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
      </el-form-item>
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="form.username"
          placeholder="请输入用户名"
          autocomplete="off"
        />
      </el-form-item>
      <el-form-item label="密码" :prop="editingItem ? '' : 'password'">
        <el-input
          v-model="form.password"
          type="password"
          show-password
          :placeholder="editingItem ? '留空则不修改密码' : '请输入密码'"
          autocomplete="new-password"
        />
      </el-form-item>
      <el-form-item label="管理员等级" prop="admin">
        <el-select
          v-model="form.admin"
          placeholder="请选择管理员等级"
          style="width: 100%"
        >
          <el-option label="超级管理员" :value="2" />
          <el-option label="管理员" :value="1" />
          <el-option label="普通用户" :value="0" />
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
import { ref, reactive, nextTick, watch, onMounted } from "vue";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import type { UploadRequestOptions } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import type { User } from "@/types";
import {
  createUser,
  updateUser,
  getUserById,
  uploadAvatar,
} from "@/api/system";

interface Emits {
  (e: "update:visible", val: boolean): void;
  (e: "success"): void;
}

const props = defineProps<{
  visible: boolean;
  editingItem: User | null;
}>();

const emit = defineEmits<Emits>();

const formRef = ref<FormInstance>();
const submitting = ref(false);

const form = reactive({
  username: "",
  password: "",
  avatar: "",
  admin: 0 as number,
});

const rules: FormRules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
};

async function handleAvatarUpload(options: UploadRequestOptions) {
  try {
    const url = await uploadAvatar(options.file as File);
    form.avatar = url;
    ElMessage.success("头像上传成功");
  } catch {
    // 错误消息由 axios 拦截器统一处理
  }
}

function beforeAvatarUpload(file: File) {
  const isImage = file.type.startsWith("image/");
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error("只能上传图片文件!");
  }
  if (!isLt2M) {
    ElMessage.error("图片大小不能超过 2MB!");
  }
  return isImage && isLt2M;
}

async function initForm() {
  form.username = "";
  form.password = "";
  form.avatar = "";
  form.admin = 0;

  await nextTick();

  formRef.value?.clearValidate();

  if (props.editingItem) {
    try {
      const detail = await getUserById(props.editingItem.id);
      form.username = detail.username;
      form.password = detail.password || "";
      form.avatar = detail.avatar || "";
      form.admin = detail.admin;
    } catch {
      form.username = props.editingItem.username;
      form.password = props.editingItem.password || "";
      form.avatar = props.editingItem.avatar || "";
      form.admin = props.editingItem.admin;
    }
  }
}

onMounted(() => {
  if (props.visible) {
    initForm();
  }
});

watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      initForm();
    }
  },
);

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  submitting.value = true;
  try {
    if (props.editingItem) {
      const payload: any = {
        username: form.username,
        admin: form.admin,
        avatar: form.avatar,
      };
      if (form.password) {
        payload.password = form.password;
      }
      await updateUser(props.editingItem.id, payload);
      ElMessage.success("修改成功");
    } else {
      await createUser({
        username: form.username,
        password: form.password,
        admin: form.admin,
        avatar: form.avatar,
        status: 1,
      });
      ElMessage.success("新增成功");
    }
    emit("update:visible", false);
    emit("success");
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped lang="scss">
.avatar-uploader {
  :deep(.el-upload) {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);

    &:hover {
      border-color: var(--el-color-primary);
    }
  }

  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 100px;
    height: 100px;
    text-align: center;
    line-height: 100px;
  }

  .avatar {
    width: 100px;
    height: 100px;
    display: block;
    object-fit: cover;
  }
}
</style>
