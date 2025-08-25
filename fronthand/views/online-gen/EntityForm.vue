<!-- src/views/DynamicEntity/form.vue -->
<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <h3>{{ action === 'create' ? '新增实体' : '编辑实体' }}</h3>
      </template>

      <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="实体名称" prop="entityName">
              <el-input v-model="form.entityName" placeholder="请输入实体名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="表名" prop="tableName">
              <el-input v-model="form.tableName" placeholder="请输入表名（英文）" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="描述">
              <el-input v-model="form.description" type="textarea" rows="2" placeholder="请输入描述" />
          </el-form-item>
        </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="0">草稿</el-radio>
                <el-radio :label="1">已发布</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 字段配置 -->
        <el-form-item label="字段配置">
          <FieldConfig v-model="form.fields" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm">保存</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getEntityById, createEntity, updateEntity } from '@/api/tool/online-gen'
import { ElMessage } from 'element-plus'
import FieldConfig from '@/views/tool/online-gen/components/FieldConfig.vue'

const router = useRouter()
const route = useRoute()

const action = ref(route.query.action)
const formRef = ref()
const form = ref({
  entityName: '',
  tableName: '',
  description: '',
  status: 0,
  fields: []
})

const rules = {
  entityName: [{ required: true, message: '请输入实体名称', trigger: 'blur' }],
  tableName: [{ required: true, message: '请输入表名', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 获取详情
const getDetail = async () => {
  if (action.value === 'edit') {
    const res = await getEntityById(route.query.id)
    form.value = res.data
  }
}

// 提交
const submitForm = async () => {
  await formRef.value.validate()
  try {
    if (action.value === 'create') {
      await createEntity(form.value)
    } else {
      await updateEntity(route.query.id, form.value)
    }
    ElMessage.success('操作成功')
    router.back()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  getDetail()
})
</script>