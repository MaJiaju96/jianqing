import { ref } from 'vue';

export function useEntityDialogForm(options) {
  const dialogVisible = ref(false);
  const isEdit = ref(false);
  const editingId = ref(null);
  const form = ref(options.createForm());

  function resetForm() {
    form.value = options.createForm();
  }

  function openCreate(customizeForm) {
    isEdit.value = false;
    editingId.value = null;
    resetForm();
    if (typeof customizeForm === 'function') {
      customizeForm(form.value);
    }
    dialogVisible.value = true;
  }

  function openEdit(row) {
    isEdit.value = true;
    editingId.value = row.id;
    form.value = options.mapForm(row);
    dialogVisible.value = true;
  }

  function closeDialog() {
    dialogVisible.value = false;
  }

  return {
    dialogVisible,
    isEdit,
    editingId,
    form,
    resetForm,
    openCreate,
    openEdit,
    closeDialog
  };
}
