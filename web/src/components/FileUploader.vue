<template>
  <el-upload
    ref="uploadRef"
    :auto-upload="mode === 'immediate'"
    :limit="limit"
    :on-change="handleFileChange"
    :on-remove="handleFileRemove"
    :accept="accept"
    :drag="drag"
    :show-file-list="showFileList"
    :before-upload="beforeUpload"
    :http-request="httpRequest"
  >
    <slot>
      <template v-if="drag">
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
      </template>
      <template v-else>
        <el-button :type="buttonType" :size="buttonSize">
          <el-icon><upload-filled /></el-icon>
          {{ buttonText }}
        </el-button>
      </template>
    </slot>
    <template #tip v-if="showTip">
      <div class="el-upload__tip">
        支持 {{ acceptText }} 格式文件，单个文件最大 {{ maxSizeText }}
      </div>
    </template>
  </el-upload>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { UploadFilled } from "@element-plus/icons-vue";
import type {
  UploadFile,
  UploadRawFile,
  UploadRequestOptions,
} from "element-plus";

export interface FileUploaderProps {
  /** 上传模式：deferred=延迟上传（新增模式），immediate=立即上传（编辑模式） */
  mode?: "deferred" | "immediate";
  /** 文件数量限制 */
  limit?: number;
  /** 允许的文件类型 */
  accept?: string;
  /** 是否拖拽上传 */
  drag?: boolean;
  /** 是否显示文件列表 */
  showFileList?: boolean;
  /** 最大文件大小（字节），默认 50MB */
  maxSize?: number;
  /** 按钮文字（仅 button 模式） */
  buttonText?: string;
  /** 按钮类型 */
  buttonType?: "primary" | "success" | "warning" | "danger" | "info";
  /** 按钮大小 */
  buttonSize?: "large" | "default" | "small";
  /** 是否显示提示文字 */
  showTip?: boolean;
}

const props = withDefaults(defineProps<FileUploaderProps>(), {
  mode: "deferred",
  limit: 1,
  accept: ".pdf,.doc,.docx,.txt,.md,.csv,.xlsx,.xls,.jpg,.jpeg,.png,.gif,.webp",
  drag: true,
  showFileList: true,
  maxSize: 50 * 1024 * 1024,
  buttonText: "上传文件",
  buttonType: "primary",
  buttonSize: "small",
  showTip: true,
});

const emit = defineEmits<{
  (e: "change", file: File): void;
  (e: "remove"): void;
  (e: "success", file: File): void;
  (e: "error", error: Error): void;
}>();

const uploadRef = ref();

const acceptText = props.accept.replace(/\./g, "").replace(/,/g, " / ").trim();

const maxSizeText =
  props.maxSize >= 1024 * 1024 * 1024
    ? `${(props.maxSize / (1024 * 1024 * 1024)).toFixed(0)}GB`
    : props.maxSize >= 1024 * 1024
      ? `${(props.maxSize / (1024 * 1024)).toFixed(0)}MB`
      : `${(props.maxSize / 1024).toFixed(0)}KB`;

function beforeUpload(file: UploadRawFile): boolean {
  if (file.size > props.maxSize) {
    ElMessage.warning(`文件大小不能超过 ${maxSizeText}`);
    return false;
  }
  return true;
}

function handleFileChange(file: UploadFile) {
  if (file.raw && props.mode === "deferred") {
    emit("change", file.raw);
  }
}

function handleFileRemove() {
  if (props.mode === "deferred") {
    emit("remove");
  }
}

function httpRequest(options: UploadRequestOptions) {
  if (props.mode === "immediate" && options.file) {
    emit("success", options.file as File);
  }
  return Promise.resolve();
}

defineExpose({
  clearFiles: () => uploadRef.value?.clearFiles(),
  submit: () => uploadRef.value?.submit(),
});
</script>
