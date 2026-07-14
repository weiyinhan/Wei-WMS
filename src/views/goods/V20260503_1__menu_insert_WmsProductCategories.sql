-- 注意：该页面对应的前台目录为views/goods文件夹下
-- 如果你想更改到其他目录，请修改sql中component字段对应的值


INSERT INTO sys_permission(id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, hide_tab, description, status, del_flag, rule_flag, create_by, create_time, update_by, update_time, internal_or_external) 
VALUES ('202605030757770510', NULL, '商品类别', '/goods/wmsProductCategoriesList', 'goods/WmsProductCategoriesList', NULL, NULL, 0, NULL, '1', 0.00, 0, NULL, 1, 0, 0, 0, 0, NULL, '1', 0, 0, 'admin', '2026-05-03 19:57:51', NULL, NULL, 0);

-- 权限控制sql
-- 新增
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('202605030757790511', '202605030757770510', '添加商品类别', NULL, NULL, 0, NULL, NULL, 2, 'goods:wms_product_categories:add', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-05-03 19:57:51', NULL, NULL, 0, 0, '1', 0);
-- 编辑
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('202605030757790512', '202605030757770510', '编辑商品类别', NULL, NULL, 0, NULL, NULL, 2, 'goods:wms_product_categories:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-05-03 19:57:51', NULL, NULL, 0, 0, '1', 0);
-- 删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('202605030757790513', '202605030757770510', '删除商品类别', NULL, NULL, 0, NULL, NULL, 2, 'goods:wms_product_categories:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-05-03 19:57:51', NULL, NULL, 0, 0, '1', 0);
-- 批量删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('202605030757790514', '202605030757770510', '批量删除商品类别', NULL, NULL, 0, NULL, NULL, 2, 'goods:wms_product_categories:deleteBatch', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-05-03 19:57:51', NULL, NULL, 0, 0, '1', 0);
-- 导出excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('202605030757790515', '202605030757770510', '导出excel_商品类别', NULL, NULL, 0, NULL, NULL, 2, 'goods:wms_product_categories:exportXls', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-05-03 19:57:51', NULL, NULL, 0, 0, '1', 0);
-- 导入excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('202605030757790516', '202605030757770510', '导入excel_商品类别', NULL, NULL, 0, NULL, NULL, 2, 'goods:wms_product_categories:importExcel', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-05-03 19:57:51', NULL, NULL, 0, 0, '1', 0);