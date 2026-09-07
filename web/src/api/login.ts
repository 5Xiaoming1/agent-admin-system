import { get, post } from "@/utils/request";
import type { CaptchaResponse, LoginResponse, LoginRequest } from "@/types";
import { LOGIN } from "./paths";

export async function getCaptcha(): Promise<CaptchaResponse> {
  return get<CaptchaResponse>(LOGIN.CAPTCHA);
}

export async function login(data: LoginRequest): Promise<LoginResponse> {
  return post<LoginResponse>(LOGIN.LOGIN, data);
}
