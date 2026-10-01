# 星城WMS Agent开发指南

> 基于 JeecgBoot 3.8.1 + Spring Boot 3.4.5 的仓库管理系统

---

## 项目概述

本项目是一个企业级仓库管理系统（WMS），在 JeecgBoot 低代码平台基础上扩展了完整的仓储业务能力，覆盖商品、入库、库存、出库、波次、发运、运单等核心链路。

**技术栈：**

| 分类 | 技术 | 版本 |
| --- | --- | --- |
| 基础框架 | Spring Boot | 3.4.5 |
| 低代码平台 | JeecgBoot | 3.8.1 |
| JDK | Java | 17 |
| 持久层 | MyBatis-Plus | 3.5.12 |
| 安全框架 | Apache Shiro | 2.0.4 |
| 认证 | JWT (java-jwt) | 4.5.0 |
| 数据库连接池 | Druid | 1.2.24 |
| 工具类 | Hutool | 5.8.12 |
| Excel | EasyExcel | 3.3.4 |
| 分页 | PageHelper | 6.1.1 |
| 二维码 | ZXing (core) | 3.5.0 |
| 顺丰快递 | SF-CSIM-EXPRESS-SDK | 2.1.7 |

---

## 模块结构

```
xincheng-wms-java-course_1
├─ jeecg-boot-base-core          公共基础模块（工具类、配置、权限、注解）
├─ jeecg-module-system           系统管理模块
│  ├─ jeecg-system-biz           系统业务实现（用户、角色、权限、字典...）
│  ├─ jeecg-system-start         单体启动入口
│  └─ jeecg-system-api           对外API
│     ├─ jeecg-system-local-api  单体接口
│     └─ jeecg-system-cloud-api  微服务Feign接口
├─ jeecg-boot-module             示例与扩展模块
│  ├─ jeecg-module-demo          示例代码
│  └─ jeecg-boot-module-airag    AI RAG模块
└─ jeecg-module-wms              WMS仓储业务模块（核心）
```

---

## WMS 核心业务模块

WMS 模块路径：[jeecg-module-wms](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms)

### 1. 商品管理 goods

- 商品 [WmsProducts](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/goods/entity/WmsProducts.java)：商品主数据，支持批次号 [WmsProductsBatchnum](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/goods/entity/WmsProductsBatchnum.java)
- 货主 [WmsCargoOwners](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/goods/entity/WmsCargoOwners.java)
- 承运商 [WmsCarrier](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/goods/entity/WmsCarrier.java)
- 包装材料 [WmsPackagingMaterial](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/goods/entity/WmsPackagingMaterial.java)
- 品牌、分类、图片：WmsProductBrand / WmsProductCategories / WmsProductImages

### 2. 仓库管理 warehouse

- 仓库 [WmsWarehouses](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/warehouse/entity/WmsWarehouses.java)
- 库区 [WmsStorageZones](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/warehouse/entity/WmsStorageZones.java)
- 库位 [WmsStorageLocations](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/warehouse/entity/WmsStorageLocations.java)

### 3. 入库管理 inorder

- 入库单 [WmsStockInOrders](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/inorder/entity/WmsStockInOrders.java) + 明细 [WmsStockInOrderItems](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/inorder/entity/WmsStockInOrderItems.java)
- 收货任务 ReceiveTasks
- 上架任务 PutawayTasks

### 4. 库存管理 inventory

- 实时库存 [WmsInventory](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/inventory/entity/WmsInventory.java)
- 库存异动流水 [WmsInventoryTrans](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/inventory/entity/WmsInventoryTrans.java)（记录每一次库存变更）
- 库存调拨 WmsInventoryTransController

### 5. 出库管理 outorder

- 出库单 [WmsOutOrders](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/outorder/entity/WmsOutOrders.java)
- 出库明细 [WmsOutOrdersItems](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/outorder/entity/WmsOutOrdersItems.java)
- 分配记录 [WmsOutOrdersAllocation](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/outorder/entity/WmsOutOrdersAllocation.java)

### 6. 波次管理 wave（策略模式 + 责任链模式）

波次创建入口：[WaveCreateClient](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/wave/strategy/WaveCreateClient.java)

抽象策略基类：[IWaveStrategy](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/wave/strategy/IWaveStrategy.java)
- `process()` — 当前策略匹配并创建波次
- `processNext()` — 剩余订单流转到下一个策略（责任链）
- `getPriority()` — 优先级，数值越小越先执行

**四种波次策略：**

| 策略编码 | 类名 | 优先级 | 说明 |
| --- | --- | --- | --- |
| SISQ | [SISQWaveStrategy](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/wave/strategy/SISQWaveStrategy.java) | 1 | 单品单件：单订单只有1个SKU且数量=1，按同SKU聚合创建波次 |
| SIFQ | SIFQWaveStrategy | 2 | 单品多件：单订单只有1个SKU但数量>1 |
| SIW | SIWWaveStrategy | 3 | 同品波次：多订单中含相同SKU的聚合 |
| EOW | EOWWaveStrategy | 4 | 汇总兜底：剩余订单统一汇总 |

- 波次主表 [WmsWaveMaster](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/wave/entity/WmsWaveMaster.java)
- 波次SKU汇总 [WmsWaveSkuSummary](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/wave/entity/WmsWaveSkuSummary.java)
- 波次策略配置 [WmsWaveStrategy](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/wave/entity/WmsWaveStrategy.java)（含最小订单数阈值）
- 缺货登记 [WmsShortageRegistration](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/wave/entity/WmsShortageRegistration.java)
- 拣货任务 PickingTasks

### 7. 发运管理 shipment（策略模式 + 工厂模式）

- 策略工厂 [ShipmentStrategyFactory](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/shipment/strategy/ShipmentStrategyFactory.java)
- 抽象策略 [AbstractShipmentStrategy](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/shipment/strategy/AbstractShipmentStrategy.java)
- 标准发运 [StandardShipmentStrategy](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/shipment/strategy/StandardShipmentStrategy.java)
- 按重量拆分 [SplitByWeightShipmentStrategy](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/shipment/strategy/SplitByWeightShipmentStrategy.java)
- 发运主表/明细：[WmsShipment](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/shipment/entity/WmsShipment.java) + [WmsShipmentDetail](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/shipment/entity/WmsShipmentDetail.java)

### 8. 运单管理 waybill（顺丰快递对接）

- 顺丰API工具 [SfExpressUtil](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/waybill/sf/SfExpressUtil.java)
- 下单请求体 [SfOrderInfo](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/waybill/sf/SfOrderInfo.java)
- 面单打印 [SfPrintWayBillRequest](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/waybill/sf/SfPrintWayBillRequest.java)
- 异步任务工具 [FutureTaskUtils](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/waybill/task/FutureTaskUtils.java)

### 9. WMS任务 wmstask

- 任务主表 [WmsTasks](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/wmstask/entity/WmsTasks.java)
- 任务执行记录 [WmsTasksRecords](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/wmstask/entity/WmsTasksRecords.java)

---

## 设计模式示例

项目在 [designmode](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/designmode) 和 [designmode2](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/jeecg-module-wms/src/main/java/org/jeecg/modules/wms/designmode2) 包中提供了两种经典模式教学示例：

**审批链（责任链模式）**：`Approver` → `DepartmentManager` → `ProjectManager` → `GeneralManager`

**折扣策略链（策略 + 责任链）**：`DiscountStrategyChain` 依次执行 `FirstOrderDiscount` → `MemberDiscount` → `BigOrderDiscount` → `HolidayDiscount`

---

## 工程约定（开发时务必遵守）

| 约定 | 说明 |
| --- | --- |
| Service 接口命名 | `I` + 业务名 + `Service`，例如 `IWmsProductsService` |
| 逻辑删除 | 实体类必须有 `delFlag` 字段并标注 `@TableLogic`，对应表 `del_flag` 列（int，默认0） |
| 非数据库字段 | 使用 `@TableField(exist = false)` 标注 |
| 链式调用 | 实体类可加 `@Accessors(chain = true)` |
| Controller 方法 | 同时支持 PUT 与 POST，避免前端 method not allowed |
| 批量更新 | 优先 `LambdaUpdateWrapper` + `setSql`，不要循环单条更新 |
| Mapper XML 关联字段 | 关联查询字段不满足驼峰映射时，必须在 `resultMap` 中显式写 `column-property` 映射 |
| 表名 / 类名 | 数据库表名用复数（如 wms_tasks），Java 实体用单数（如 WmsTask） |
| 开发期权限 | 移除 Controller 上的 `@RequiresPermissions`，避免 403 |

---

## 配置文件要点

- 启动入口模块：`jeecg-system-start`
- 数据库脚本位于 [db/](file:///d:/javaProjects/XingChengWms/xincheng-wms-java-course_1/db/)：`xingchenwms-20250912.sql`、`xingchenwms-20251114.sql`
- OSS / Minio 配置密钥必须使用真实值，不可占位
- OSS Bucket ACL 需设为 Public Read，否则上传后文件无法预览访问
- `file-view-domain` 必须与 Bucket endpoint + 名称一致

---

## 数据库支持

MySQL 5.7+ / Oracle 11g / SQL Server 2017 / PostgreSQL / MariaDB / 达梦 / 人大金仓 / TiDB

---

## 常见坑点速查

1. **删除操作不生效** → 检查实体是否有 `delFlag` + `@TableLogic`，表是否有 `del_flag` 列
2. **关联查询字段显示为空** → 检查 Mapper XML 是否显式映射了 column→property，必要时 `mvn clean compile`
3. **OSS 上传报 AccessDenied** → Bucket ACL 非 Public Read，或 accessKey/secretKey 是占位符
4. **批量 update 无变化** → LambdaUpdateWrapper 必须赋值给变量再传入 service.update，不能只创建不使用
5. **前端调 PUT 接口 405** → Controller 方法加 `@PutMapping` 的同时保留 `@PostMapping`
6. **SQL 表不存在 / 列不存在** → 检查表名单复数、列名单复数是否与 Mapper XML 一致

---

## 启动方式

1. 导入 `db/xingchenwms-*.sql` 到 MySQL
2. 修改 `jeecg-system-start` 下 `application-dev.yml` 中的数据库、Redis、OSS 配置
3. IDEA 运行 `jeecg-system-start` 启动类（单体模式，默认 8080）
4. 前端使用对应 `jeecgboot-vue3` 仓库，`pnpm install && pnpm dev`
