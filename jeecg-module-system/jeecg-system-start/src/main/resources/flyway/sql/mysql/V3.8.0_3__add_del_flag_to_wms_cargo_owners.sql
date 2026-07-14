ALTER TABLE `wms_cargo_owners` ADD COLUMN `del_flag` int DEFAULT '0' COMMENT '删除状态' AFTER `sys_org_code`;
