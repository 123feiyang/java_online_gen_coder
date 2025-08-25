<!-- src/components/FieldConfig.vue -->
<template>
  <div>
    <el-button size="small" @click="addField" type="primary" icon="Plus">添加字段</el-button>
    <el-table :data="fields" class="mt-2" border>
      <el-table-column type="index" label="#" width="50" />
      <el-table-column label="字段名" prop="fieldName" width="120">
        <template #default="scope">
          <el-input v-model="scope.row.fieldName" size="small" />
        </template>
      </el-table-column>
      <el-table-column label="字段标签" prop="fieldLabel" width="120">
        <template #default="scope">
          <el-input v-model="scope.row.fieldLabel" size="small" />
        </template>
      </el-table-column>
      <el-table-column label="类型" prop="fieldType" width="100">
        <template #default="scope">
          <el-select v-model="scope.row.fieldType" size="small" style="width: 100%">
            <el-option label="文本" value="string" />
            <el-option label="长文本" value="text" />
            <el-option label="数字" value="number" />
            <el-option label="整数" value="integer" />
            <el-option label="布尔" value="boolean" />
            <el-option label="日期" value="date" />
            <el-option label="时间" value="datetime" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="长度" prop="length" width="80">
        <template #default="scope">
          <el-input-number v-model="scope.row.length" :min="1" :max="65535" size="small" />
        </template>
      </el-table-column>
      <el-table-column label="主键" width="60">
        <template #default="scope">
          <el-checkbox v-model="scope.row.primaryKey" />
        </template>
      </el-table-column>
      <el-table-column label="非空" width="60">
        <template #default="scope">
          <el-checkbox v-model="scope.row.nullable" :true-label="false" :false-label="true" />
        </template>
      </el-table-column>
      <el-table-column label="列表显示" width="80">
        <template #default="scope">
          <el-switch v-model="scope.row.inList" />
        </template>
      </el-table-column>
      <el-table-column label="表单显示" width="80">
        <template #default="scope">
          <el-switch v-model="scope.row.inForm" />
        </template>
      </el-table-column>
      <el-table-column label="搜索项" width="80">
        <template #default="scope">
          <el-switch v-model="scope.row.inSearch" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="60">
        <template #default="scope">
          <el-button type="danger" size="small" @click="removeField(scope.$index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const fields = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const addField = () => {
  fields.value.push({
    id: Date.now().toString(),
    fieldName: '',
    fieldLabel: '',
    fieldType: 'string',
    length: 255,
    nullable: true,
    primaryKey: false,
    inList: true,
    inForm: true,
    inSearch: false
  })
}

const removeField = (index) => {
  fields.value.splice(index, 1)
}
</script>