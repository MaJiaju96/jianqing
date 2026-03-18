export function formatDateTimeText(value, fallback = '-') {
  if (value === null || value === undefined || value === '') {
    return fallback;
  }
  if (value instanceof Date) {
    return formatDateTimeText(value.toISOString(), fallback);
  }
  let text = String(value).trim();
  if (!text) {
    return fallback;
  }
  text = text.replace('T', ' ').replace('Z', '');
  const millisIndex = text.indexOf('.');
  if (millisIndex > 0) {
    text = text.slice(0, millisIndex);
  }
  return text;
}
