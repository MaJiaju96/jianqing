import { MENU_TYPE_BUTTON, MENU_TYPE_DIRECTORY, MENU_TYPE_PAGE } from '../../constants/app';

export function matchMenuType(menuType, filterType) {
  if (filterType === 'menu') {
    return menuType === MENU_TYPE_DIRECTORY || menuType === MENU_TYPE_PAGE;
  }
  if (filterType === 'button') {
    return menuType === MENU_TYPE_BUTTON;
  }
  return true;
}

export function getMenuTypeText(menuType) {
  if (menuType === MENU_TYPE_DIRECTORY) {
    return '目录';
  }
  if (menuType === MENU_TYPE_PAGE) {
    return '菜单';
  }
  return '按钮';
}

export function getMenuTypeTag(menuType) {
  if (menuType === MENU_TYPE_DIRECTORY) {
    return 'info';
  }
  if (menuType === MENU_TYPE_PAGE) {
    return 'success';
  }
  return 'warning';
}
