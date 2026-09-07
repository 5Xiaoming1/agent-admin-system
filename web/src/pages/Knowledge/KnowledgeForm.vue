<template>
  <el-dialog
    :model-value="visible"
    :title="editingItem ? '编辑知识库' : '新增知识库'"
    :width="editingItem ? '720px' : '520px'"
    @update:model-value="$emit('update:visible', $event)"
    @open="initForm"
    destroy-on-close
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入知识库名称" />
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入知识库描述"
        />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select
          v-model="form.type"
          placeholder="请选择类型"
          style="width: 100%"
        >
          <el-option label="文档" value="document" />
          <el-option label="问答" value="qa" />
          <el-option label="网页" value="web" />
        </el-select>
      </el-form-item>
      <el-form-item label="文件" prop="file" v-if="!editingItem">
        <FileUploader
          ref="uploadRef"
          mode="deferred"
          drag
          @change="handleFileChange"
          @remove="handleFileRemove"
        />
      </el-form-item>

      <el-form-item label="文件管理" v-if="editingItem">
        <div class="file-management">
          <FileUploader
            ref="editUploadRef"
            mode="immediate"
            :drag="false"
            :show-file-list="false"
            @success="handleUploadFile"
          />

          <el-table
            v-loading="filesLoading"
            :data="fileList"
            style="margin-top: 12px"
            size="small"
            max-height="240"
          >
            <el-table-column
              prop="name"
              label="文件名"
              min-width="120"
              show-overflow-tooltip
            />
            <el-table-column label="大小" width="100">
              <template #default="{ row }">
                {{ formatFileSize(row.size) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button
                  link
                  type="primary"
                  size="small"
                  @click="handleDownload(row)"
                >
                  下载
                </el-button>
                <el-popconfirm
                  title="确定删除该文件？"
                  @confirm="handleDeleteFile(row.name)"
                >
                  <template #reference>
                    <el-button link type="danger" size="small">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>

          <el-empty
            v-if="!filesLoading && fileList.length === 0"
            description="暂无文件"
            :image-size="60"
          />
        </div>
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
import type { KnowledgeBase, KnowledgeFile } from "@/types";
import {
  createKnowledge,
  updateKnowledge,
  getKnowledgeById,
  createKnowledgeWithFile,
  getKnowledgeFiles,
  uploadKnowledgeFile,
  deleteKnowledgeFile,
  downloadKnowledgeFile,
} from "@/api/knowledge";
import FileUploader from "@/components/FileUploader.vue";

interface Emits {
  (e: "update:visible", val: boolean): void;
  (e: "success"): void;
}

const props = defineProps<{
  visible: boolean;
  editingItem: KnowledgeBase | null;
}>();

const emit = defineEmits<Emits>();

const formRef = ref<FormInstance>();
const uploadRef = ref();
const editUploadRef = ref();
const submitting = ref(false);
const selectedFile = ref<File | null>(null);

const fileList = ref<KnowledgeFile[]>([]);
const filesLoading = ref(false);

const form = reactive({
  name: "",
  description: "",
  type: "document" as "document" | "qa" | "web",
});

const rules: FormRules = {
  name: [{ required: true, message: "请输入知识库名称", trigger: "blur" }],
  type: [{ required: true, message: "请选择类型", trigger: "change" }],
};

function formatFileSize(bytes: number): string {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
}

function handleFileChange(file: File) {
  selectedFile.value = file;
}

function handleFileRemove() {
  selectedFile.value = null;
}

async function handleUploadFile(file: File) {
  if (!props.editingItem) return;
  try {
    await uploadKnowledgeFile(props.editingItem.id, file);
    ElMessage.success("上传成功");
    await loadFiles();
  } catch {
    ElMessage.error("上传失败");
  }
}

async function handleDeleteFile(fileName: string) {
  if (!props.editingItem) return;
  try {
    await deleteKnowledgeFile(props.editingItem.id, fileName);
    ElMessage.success("删除成功");
    await loadFiles();
  } catch {
    ElMessage.error("删除失败");
  }
}

function handleDownload(file: KnowledgeFile) {
  if (!props.editingItem) return;
  downloadKnowledgeFile(props.editingItem.id, file.name);
}

async function loadFiles() {
  if (!props.editingItem) return;
  filesLoading.value = true;
  try {
    fileList.value = await getKnowledgeFiles(props.editingItem.id);
  } catch {
    fileList.value = [];
  } finally {
    filesLoading.value = false;
  }
}

async function initForm() {
  if (props.editingItem) {
    try {
      const detail = await getKnowledgeById(props.editingItem.id);
      form.name = detail.name;
      form.description = detail.description;
      form.type = detail.type;
    } catch {
      form.name = props.editingItem.name;
      form.description = props.editingItem.description;
      form.type = props.editingItem.type;
    }
    await loadFiles();
  } else {
    form.name = "";
    form.description = "";
    form.type = "document";
    fileList.value = [];
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  submitting.value = true;
  try {
    if (props.editingItem) {
      await updateKnowledge(props.editingItem.id, { ...form });
      ElMessage.success("修改成功");
    } else {
      await createKnowledgeWithFile({
        ...form,
        file: selectedFile.value || undefined,
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
.file-management {
  width: 100%;
}
</style>
