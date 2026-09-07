import { ref, reactive } from "vue";

interface UseFormOptions<T> {
  defaultForm: T;
  submitApi: (form: T) => Promise<any>;
}

export function useForm<T extends Record<string, any>>(
  options: UseFormOptions<T>,
) {
  const { defaultForm, submitApi } = options;

  const visible = ref(false);
  const formData = reactive<T>({ ...defaultForm }) as T;
  const isEdit = ref(false);
  const editId = ref("");
  const submitting = ref(false);

  function openDialog(mode: "add" | "edit", data?: T, id?: string) {
    visible.value = true;
    isEdit.value = mode === "edit";
    if (data) {
      Object.assign(formData, data);
    } else {
      Object.assign(formData, defaultForm);
    }
    if (id) {
      editId.value = id;
    }
  }

  function closeDialog() {
    visible.value = false;
    Object.assign(formData, defaultForm);
    isEdit.value = false;
    editId.value = "";
  }

  async function handleSubmit(): Promise<boolean> {
    submitting.value = true;
    try {
      await submitApi({ ...formData } as T);
      closeDialog();
      return true;
    } finally {
      submitting.value = false;
    }
    return false;
  }

  return {
    visible,
    formData,
    isEdit,
    editId,
    submitting,
    openDialog,
    closeDialog,
    handleSubmit,
  };
}
