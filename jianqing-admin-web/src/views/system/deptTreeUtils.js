export function flattenDeptOptions(nodes, prefix = '') {
  return nodes.flatMap((node) => {
    const currentLabel = `${prefix}${node.deptName}`;
    const current = [{ id: node.id, label: currentLabel }];
    return current.concat(flattenDeptOptions(node.children || [], `${currentLabel} / `));
  });
}

export function buildDeptNameMap(nodes, prefix = '') {
  return nodes.reduce((result, node) => {
    const currentLabel = `${prefix}${node.deptName}`;
    result[node.id] = currentLabel;
    Object.assign(result, buildDeptNameMap(node.children || [], `${currentLabel} / `));
    return result;
  }, {});
}

export function flattenDeptRows(nodes, level = 1) {
  return nodes.flatMap((node) => {
    const current = {
      id: node.id,
      parentId: node.parentId,
      deptName: node.deptName,
      leaderUserId: node.leaderUserId,
      phone: node.phone,
      email: node.email,
      sortNo: node.sortNo,
      status: node.status,
      level
    };
    return [current].concat(flattenDeptRows(node.children || [], level + 1));
  });
}

export function filterDeptTree(nodes, keywordValue, statusValue) {
  return nodes.reduce((result, node) => {
    const children = filterDeptTree(node.children || [], keywordValue, statusValue);
    const keywordMatched = !keywordValue || node.deptName.includes(keywordValue);
    const statusMatched = statusValue === 'all' || node.status === statusValue;
    if (keywordMatched && statusMatched) {
      result.push({ ...node, children });
      return result;
    }
    if (children.length > 0) {
      result.push({ ...node, children });
    }
    return result;
  }, []);
}
