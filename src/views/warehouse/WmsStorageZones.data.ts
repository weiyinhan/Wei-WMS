import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { rules } from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
import { list } from './WmsWarehouses.api';

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '库区编码',
    align: 'center',
    dataIndex: 'zoneCode',
  },
  {
    title: '库区名称',
    align: 'center',
    dataIndex: 'zoneName',
  },
  {
    title: '库区类型',
    align: 'center',
    dataIndex: 'zoneType',
    customRender: ({ text }) => {
      return render.renderDict(text, 'zone_type');
    },
  },
  {
    title: '状态',
    align: 'center',
    dataIndex: 'status',
    customRender: ({ text }) => {
      return render.renderDict(text, 'wms_status');
    },
  },
  {
    title: '是否可售库存',
    align: 'center',
    dataIndex: 'isSellable_dictText',
  },
  {
    title: '所属仓库',
    align: 'center',
    dataIndex: 'warehouseName',
  },
];

//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '所属仓库',
    field: 'warehouseId',
    component: 'ApiSelect',
    componentProps: {
      api: list,
      resultField: 'records',
      labelField: 'warehouseName',
      valueField: 'id',
      placeholder: '请选择所属仓库',
    },
  },
];

//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '库区编码',
    field: 'zoneCode',
    component: 'Input',
    dynamicRules: ({ model, schema }) => {
      return [{ required: true, message: '请输入库区编码!' }];
    },
  },
  {
    label: '库区名称',
    field: 'zoneName',
    component: 'Input',
    dynamicRules: ({ model, schema }) => {
      return [{ required: true, message: '请输入库区名称!' }];
    },
  },
  {
    label: '库区类型',
    field: 'zoneType',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'zone_type',
    },
    dynamicRules: ({ model, schema }) => {
      return [{ required: true, message: '请输入库区类型!' }];
    },
  },
  {
    label: '状态',
    field: 'status',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'wms_status',
      disabled: true,
    },
    dynamicRules: ({ model, schema }) => {
      return [{ required: true, message: '请输入状态!' }];
    },
  },
  {
    label: '是否可售库存',
    field: 'isSellable',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'yn',
    },
    dynamicRules: ({ model, schema }) => {
      return [{ required: true, message: '请输入是否可售库存!' }];
    },
  },
  {
    label: '所属仓库',
    field: 'warehouseId',
    component: 'ApiSelect',
    componentProps: {
      api: list,
      resultField: 'records',
      labelField: 'warehouseName',
      valueField: 'id',
    },
    colProps: {
      span: 12,
    },
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
];

// 高级查询数据
export const superQuerySchema = {
  zoneCode: { title: '库区编码', order: 0, view: 'text', type: 'string' },
  zoneName: { title: '库区名称', order: 1, view: 'text', type: 'string' },
  zoneType: { title: '库区类型', order: 2, view: 'text', type: 'string' },
  status: { title: '状态', order: 3, view: 'list', type: 'string', dictCode: 'dict_item_status' },
  isSellable: { title: '是否可售库存', order: 4, view: 'list', type: 'string', dictCode: 'yn' },
  warehouseId: { title: '所属仓库', order: 5, view: 'link_table', type: 'string' },
};

export function getBpmFormSchema(_formData): FormSchema[] {
  return formSchema;
}
