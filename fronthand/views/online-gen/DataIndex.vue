<!-- src/views/DynamicData/index.vue -->
<template>
  <div class="app-container">
    <el-card shadow="never">
      <!-- 头部信息 -->
      <template #header>
        <div class="flex justify-between">
          <h3>数据管理 - {{ entity?.entityName }}</h3>
          <div>
            <el-button type="primary" @click="handleCreate" icon="Plus">新增</el-button>
            <el-button @click="$router.back()">返回</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :model="queryParams" :inline="true" class="mb-4">
        <template v-for="field in searchFields" :key="field.id">
          <el-form-item :label="field.fieldLabel">
            <template v-if="['string', 'text'].includes(field.fieldType)">
              <el-input
                v-model="queryParams[field.fieldName]"
                :placeholder="`请输入${field.fieldLabel}`"
                clearable
                @keyup.enter="getList"
              />
            </template>
            <template v-else-if="['number', 'integer'].includes(field.fieldType)">
              <el-input-number
                v-model="queryParams[field.fieldName]"
                :placeholder="`请输入${field.fieldLabel}`"
                class="w-full"
              />
            </template>
            <template v-else-if="field.fieldType === 'boolean'">
              <el-select v-model="queryParams[field.fieldName]" placeholder="请选择" clearable>
                <el-option label="是" :value="true" />
                <el-option label="否" :value="false" />
              </el-select>
            </template>
            <template v-else-if="['date', 'datetime'].includes(field.fieldType)">
              <el-date-picker
                v-model="queryParams[field.fieldName]"
                :type="field.fieldType === 'date' ? 'date' : 'datetime'"
                value-format="YYYY-MM-DD HH:mm:ss"
                :placeholder="`选择${field.fieldLabel}`"
                class="w-full"
              />
            </template>
          </el-form-item>
        </template>
        <el-form-item>
          <el-button type="primary" @click="getList" icon="Search">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据列表 -->
      <el-table
        :data="list"
        border
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column type="index" label="#" width="50" />

        <!-- 动态列 -->
        <template v-for="field in listFields" :key="field.id">
          <el-table-column
            :label="field.fieldLabel"
            :prop="field.fieldName"
            :width="getColWidth(field)"
            show-overflow-tooltip
          >
            <template #default="scope">
              <!-- 布尔值 -->
              <template v-if="field.fieldType === 'boolean'">
                <el-tag :type="scope.row[field.fieldName] ? 'success' : 'info'">
                  {{ scope.row[field.fieldName] ? '是' : '否' }}
                </el-tag>
              </template>
              <!-- 日期 -->
              <template v-else-if="['date', 'datetime'].includes(field.fieldType)">
                {{ scope.row[field.fieldName] }}
              </template>
              <!-- 默认文本 -->
              <template v-else>
                {{ scope.row[field.fieldName] }}
              </template>
            </template>
          </el-table-column>
        </template>

        <!-- 操作列 -->
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)" type="primary">编辑</el-button>
            <el-button size="small" @click="handleDelete(scope.row)" type="danger">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getList"
        @current-change="getList"
        class="mt-4 justify-end"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <template v-for="field in formFields" :key="field.id">
            <!-- 主键不显示 -->
            <template v-if="!field.primaryKey">
              <el-col :span="24">
                <el-form-item :label="field.fieldLabel" :prop="field.fieldName">
                  <template v-if="['string', 'text'].includes(field.fieldType)">
                    <el-input
                      v-model="formData[field.fieldName]"
                      :placeholder="`请输入${field.fieldLabel}`"
                      :type="field.fieldType === 'text' ? 'textarea' : 'text'"
                      :rows="3"
                    />
                  </template>
                  <template v-else-if="['number', 'integer'].includes(field.fieldType)">
                    <el-input-number
                      v-model="formData[field.fieldName]"
                      :placeholder="`请输入${field.fieldLabel}`"
                      :min="0"
                      class="w-full"
                    />
                  </template>
                  <template v-else-if="field.fieldType === 'boolean'">
                    <el-switch v-model="formData[field.fieldName]" />
                  </template>
                  <template v-else-if="field.fieldType === 'date'">
                    <el-date-picker
                      v-model="formData[field.fieldName]"
                      type="date"
                      value-format="YYYY-MM-DD"
                      placeholder="选择日期"
                      class="w-full"
                    />
                  </template>
                  <template v-else-if="field.fieldType === 'datetime'">
                    <el-date-picker
                      v-model="formData[field.fieldName]"
                      type="datetime"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      placeholder="选择时间"
                      class="w-full"
                    />
                  </template>
                </el-form-item>
              </el-col>
            </template>
          </template>
        </el-row>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { queryList, saveData, updateData, deleteData, getEntityById } from '@/api/tool/online-gen'
import { ElMessageBox, ElMessage } from 'element-plus'

const route = useRoute()

// 实体ID
const entityId = route.params.entityId

// 实体配置
const entity = ref(null)

// 列表数据
const list = ref([])
const total = ref(0)
const loading = ref(false)

// 查询参数
const queryParams = ref({
  pageNum: 1,
  pageSize: 10
})

// 弹窗相关
const dialogVisible = ref(false)
const dialogType = ref('create') // create | edit
const formData = ref({})
const formRef = ref()

// 获取实体配置
const getConfig = async () => {
  try {
    const res = await getEntityById(entityId)
    entity.value = res.data
  } catch (error) {
    console.error('获取实体配置失败:', error)
  }
}

// 获取数据列表
const getList = async () => {
  if (!entityId) return
  loading.value = true
  try {
    const res = await queryList(entityId, queryParams.value)
    list.value = res.data.records || res.data
    total.value = res.data.total || res.data.length
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false}
}

// 重置查询
const resetQuery = () => {
  queryParams.value = { pageNum: 1, pageSize: 10 }
  getList()
}

// 字段过滤
const searchFields = computed(() => {
  return entity.value?.fields?.filter(f => f.inSearch) || []
})

const listFields = computed(() => {
  return entity.value?.fields?.filter(f => f.inList) || []
})

const formFields = computed(() => {
  return entity.value?.fields || []
})

// 列宽计算
const getColWidth = (field) => {
  if (field.fieldType === 'text') return 300
  if (['number', 'integer'].includes(field.fieldType)) return 120
  if (['date', 'datetime'].includes(field.fieldType)) return 160
  return 140
}

// 弹窗标题
const dialogTitle = computed(() => {
  return dialogType.value === 'create' ? '新增数据' : '编辑数据'
})

// 表单规则（可扩展）
const formRules = computed(() => {
  const rules = {}
  formFields.value.forEach(field => {
    if (!field.nullable && !field.primaryKey) {
      rules[field.fieldName] = [{ required: true, message: `请输入${field.fieldLabel}`, trigger: 'blur' }]
    }
  })
  return rules
})

// 新增
const handleCreate = () => {
  dialogType.value = 'create'
  formData.value = {}
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogType.value = 'edit'
  formData.value = { ...row }
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除该数据吗？`, '警告', {
      type: 'warning'
    })
    await deleteData(entityId, row.id)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    // 取消或失败
  }
}

// 提交表单
const submitForm = async () => {
  await formRef.value.validate()
  try {
    if (dialogType.value === 'create') {
      await saveData(entityId, formData.value)
      ElMessage.success('新增成功')
    } else {
      await updateData(entityId, formData.value.id, formData.value)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

onMounted(() => {
  getConfig().then(() => {
    getList()
  })
})
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}
</style>