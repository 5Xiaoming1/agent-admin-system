<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <h1 class="login-title">Agent管理系统</h1>
        <p class="login-subtitle">欢迎回来，请登录您的账号</p>
      </div>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="formData.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item prop="captcha">
          <div class="captcha-row">
            <el-input
              v-model="formData.captcha"
              placeholder="请输入验证码"
              :prefix-icon="Key"
              size="large"
              class="captcha-input"
            />
            <div
              class="captcha-image"
              @click="refreshCaptcha"
              title="点击刷新验证码"
            >
              <img
                v-if="captchaImage"
                :src="captchaImage"
                alt="验证码"
                class="captcha-img"
              />
              <span v-else class="captcha-loading">加载中...</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? "登录中..." : "登 录" }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock, Key } from "@element-plus/icons-vue";
import type { FormInstance, FormRules } from "element-plus";
import { useUserStore } from "@/stores/user";

const router = useRouter();
const userStore = useUserStore();

const formRef = ref<FormInstance>();
const loading = ref(false);
const captchaImage = ref("");
const captchaKey = ref("");

const formData = reactive({
  username: "",
  password: "",
  captcha: "",
});

const rules: FormRules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  captcha: [{ required: true, message: "请输入验证码", trigger: "blur" }],
};

async function fetchCaptcha() {
  try {
    const data = await userStore.getCaptcha();
    captchaImage.value = data.captchaImage;
    captchaKey.value = data.captchaKey;
  } catch {
    ElMessage.error("获取验证码失败");
  }
}

function refreshCaptcha() {
  formData.captcha = "";
  fetchCaptcha();
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  loading.value = true;
  try {
    await userStore.loginAction({
      username: formData.username,
      password: formData.password,
      captcha: formData.captcha,
      captchaKey: captchaKey.value,
    });
    ElMessage.success("登录成功");
    router.push("/");
  } catch (err: any) {
    ElMessage.error(err.message || "登录失败，请重试");
    refreshCaptcha();
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetchCaptcha();
});
</script>

<style scoped lang="scss">
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-container {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.login-title {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: #303133;
  letter-spacing: 2px;
}

.login-subtitle {
  margin: 10px 0 0;
  font-size: 14px;
  color: #909399;
}

.login-form {
  :deep(.el-input--large) {
    .el-input__wrapper {
      border-radius: 8px;
    }
  }
}

.captcha-row {
  display: flex;
  gap: 12px;
  width: 100%;

  .captcha-input {
    flex: 1;
  }

  .captcha-image {
    width: 130px;
    height: 40px;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    border: 1px solid #dcdfe6;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    transition: border-color 0.3s;

    &:hover {
      border-color: #409eff;
    }
  }

  .captcha-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .captcha-loading {
    font-size: 12px;
    color: #909399;
  }
}

.login-btn {
  width: 100%;
  border-radius: 8px;
  letter-spacing: 4px;
  font-size: 16px;
}
</style>
