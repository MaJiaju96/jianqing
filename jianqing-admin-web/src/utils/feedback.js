import { ElMessage } from 'element-plus';

export function showSuccessMessage(actionText) {
  ElMessage.success(`${actionText}成功`);
}
