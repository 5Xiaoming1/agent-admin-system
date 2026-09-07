import { defineStore } from "pinia";
import { ref, computed } from "vue";
import type { LoginResponse, User } from "@/types";
import { login as loginApi, getCaptcha } from "@/api/login";
import { getUserProfile } from "@/api/system";
import type { LoginRequest } from "@/types";
import router from "@/router";

export const useUserStore = defineStore("user", () => {
  const token = ref<string>(localStorage.getItem("token") || "");
  const userId = ref<string>(localStorage.getItem("userId") || "");
  const username = ref<string>(localStorage.getItem("username") || "");
  const avatar = ref<string>(localStorage.getItem("avatar") || "");
  const admin = ref<number>(Number(localStorage.getItem("admin")) || 0);

  const isLoggedIn = computed(() => !!token.value);
  const isAdmin = computed(() => admin.value >= 1);
  const isSuperAdmin = computed(() => admin.value === 2);

  function setUser(data: LoginResponse) {
    token.value = data.token;
    userId.value = String(data.userId);
    username.value = data.username;
    avatar.value = data.avatar || "";
    admin.value = data.admin;
    localStorage.setItem("token", data.token);
    localStorage.setItem("userId", String(data.userId));
    localStorage.setItem("username", data.username);
    localStorage.setItem("avatar", data.avatar || "");
    localStorage.setItem("admin", String(data.admin));
  }

  async function loginAction(data: LoginRequest) {
    const res = await loginApi(data);
    setUser(res);
    return res;
  }

  async function fetchUserProfile() {
    try {
      const profile = await getUserProfile();
      setUser({
        token: token.value,
        userId: profile.id,
        username: profile.username,
        avatar: profile.avatar || "",
        admin: profile.admin,
      });
      return profile;
    } catch (error) {
      console.error("获取用户信息失败", error);
      throw error;
    }
  }

  function logout() {
    token.value = "";
    userId.value = "0";
    username.value = "";
    avatar.value = "";
    admin.value = 0;
    localStorage.removeItem("token");
    localStorage.removeItem("userId");
    localStorage.removeItem("username");
    localStorage.removeItem("avatar");
    localStorage.removeItem("admin");
    router.push("/login");
  }

  return {
    token,
    userId,
    username,
    avatar,
    admin,
    isLoggedIn,
    isAdmin,
    isSuperAdmin,
    loginAction,
    logout,
    getCaptcha,
    fetchUserProfile,
  };
});
