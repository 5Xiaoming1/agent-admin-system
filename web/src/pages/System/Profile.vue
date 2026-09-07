<template>
  <div class="profile-page">
    <PageHeader title="个人信息" />

    <div class="profile-content">
      <el-card class="info-card" v-loading="loading">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
            <el-button
              type="primary"
              size="small"
              :loading="saving"
              @click="handleSave"
            >
              保存
            </el-button>
          </div>
        </template>

        <el-form
          ref="formRef"
          :model="editForm"
          :rules="editRules"
          label-width="100px"
          style="max-width: 600px"
        >
          <el-form-item label="头像">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :http-request="handleAvatarUpload"
              :before-upload="beforeAvatarUpload"
            >
              <img
                v-if="editForm.avatar"
                :src="editForm.avatar"
                class="avatar"
              />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </el-form-item>
          <el-form-item label="用户ID">
            <span>{{ profile?.id ?? "-" }}</span>
          </el-form-item>
          <el-form-item label="用户名" prop="username">
            <el-input v-model="editForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="管理员等级">
            <el-tag>{{ adminLabel }}</el-tag>
          </el-form-item>
          <el-form-item label="状态">
            <el-tag :type="editForm.status === 1 ? 'success' : 'info'">
              {{ editForm.status === 1 ? "启用" : "禁用" }}
            </el-tag>
          </el-form-item>
          <el-form-item label="密码">
            <el-input
              v-model="passwordDisplay"
              :type="passwordVisible ? 'text' : 'password'"
              readonly
              style="width: 200px; margin-right: 12px"
            >
              <template #suffix>
                <el-icon
                  class="cursor-pointer"
                  @click="passwordVisible = !passwordVisible"
                >
                  <View v-if="!passwordVisible" />
                  <Hide v-else />
                </el-icon>
              </template>
            </el-input>
            <el-button
              type="primary"
              link
              size="small"
              @click="showPasswordDialog"
            >
              修改密码
            </el-button>
          </el-form-item>
          <el-form-item label="创建时间">
            <span>{{ profile?.createTime ?? "-" }}</span>
          </el-form-item>
          <el-form-item label="更新时间">
            <span>{{ profile?.updateTime ?? "-" }}</span>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 修改密码弹窗 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="460px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="pwdFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入原密码"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="handleChangePassword"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from "vue";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import type { UploadRequestOptions } from "element-plus";
import { View, Hide, Plus } from "@element-plus/icons-vue";
import type { User } from "@/types";
import {
  getUserProfile,
  changePassword,
  updateUserProfile,
  uploadAvatar,
} from "@/api/system";
import { useUserStore } from "@/stores/user";
import PageHeader from "@/components/PageHeader.vue";

const formRef = ref<FormInstance>();
const pwdFormRef = ref<FormInstance>();
const loading = ref(false);
const saving = ref(false);
const submitting = ref(false);
const profile = ref<User | null>(null);
const passwordDialogVisible = ref(false);
const passwordVisible = ref(false);

const userStore = useUserStore();

const editForm = reactive({
  username: "",
  admin: 0 as number,
  status: 1 as number,
  avatar: "",
});

const editRules: FormRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    {
      min: 2,
      max: 20,
      message: "用户名长度在 2 到 20 个字符",
      trigger: "blur",
    },
  ],
};

const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const passwordDisplay = computed(() => {
  return profile.value?.password ?? "-";
});

const adminLabel = computed(() => {
  switch (editForm.admin) {
    case 2:
      return "超级管理员";
    case 1:
      return "管理员";
    default:
      return "普通用户";
  }
});

async function handleAvatarUpload(options: UploadRequestOptions) {
  try {
    const url = await uploadAvatar(options.file as File);
    editForm.avatar = url;
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

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error("两次输入的密码不一致"));
  } else {
    callback();
  }
};

const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: "请输入原密码", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "密码长度不能少于6位", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请再次输入新密码", trigger: "blur" },
    { validator: validateConfirmPassword, trigger: "blur" },
  ],
};

function showPasswordDialog() {
  passwordForm.oldPassword = "";
  passwordForm.newPassword = "";
  passwordForm.confirmPassword = "";
  pwdFormRef.value?.clearValidate();
  passwordDialogVisible.value = true;
}

async function fetchProfile() {
  loading.value = true;
  try {
    profile.value = await getUserProfile();
    editForm.username = profile.value?.username ?? "";
    editForm.admin = profile.value?.admin ?? 0;
    editForm.status = profile.value?.status ?? 1;
    editForm.avatar = profile.value?.avatar ?? "";
  } catch {
    ElMessage.error("获取个人信息失败");
  } finally {
    loading.value = false;
  }
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid || !profile.value) return;

  saving.value = true;
  try {
    const updatedUser = await updateUserProfile({
      username: editForm.username,
      avatar: editForm.avatar,
    });
    ElMessage.success("保存成功");
    // 直接更新本地状态，避免额外请求
    profile.value = updatedUser;
    // 同步更新 userStore 和 localStorage 中的头像等字段
    userStore.avatar = updatedUser.avatar || "";
    userStore.username = updatedUser.username;
    localStorage.setItem("avatar", updatedUser.avatar || "");
    localStorage.setItem("username", updatedUser.username);
  } catch {
    ElMessage.error("保存失败");
  } finally {
    saving.value = false;
  }
}

async function handleChangePassword() {
  const valid = await pwdFormRef.value?.validate().catch(() => false);
  if (!valid) return;

  submitting.value = true;
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    });
    ElMessage.success("密码修改成功");
    passwordDialogVisible.value = false;
  } catch {
    ElMessage.error("密码修改失败");
  } finally {
    submitting.value = false;
  }
}

onMounted(() => {
  fetchProfile();
});
</script>

<style scoped lang="scss">
.profile-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.password-text {
  margin-right: 12px;
  font-family: monospace;
  letter-spacing: 1px;
  color: #303133;
}

.cursor-pointer {
  cursor: pointer;
}

.info-card {
  :deep(.el-form-item__label) {
    font-weight: 500;
  }
}

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
