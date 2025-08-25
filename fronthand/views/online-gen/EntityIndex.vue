<!-- src/views/DynamicEntity/index.vue -->
<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="flex justify-between">
          <h3>动态实体管理</h3>
          <el-button type="primary" @click="handleCreate" icon="Plus">新增实体</el-button>
        </div>
      </template>

      <!-- 搜索区 -->
      <el-form :inline="true" :model="queryParams" class="mb-4">
        <el-form-item label="实体名称">
          <el-input v-model="queryParams.entityName" placeholder="请输入名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 列表 -->
      <el-table :data="list" border v-loading="loading">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column label="实体名称" prop="entityName" min-width="120" />
        <el-table-column label="表名" prop="tableName" min-width="120" />
        <el-table-column label="描述" prop="description" min-width="150" />
        <el-table-column label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" />
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)" type="primary">编辑</el-button>
            <el-button size="small" @click="handleDelete(scope.row)" type="danger">删除</el-button>
            <el-button size="small" @click="goDataPage(scope.row)" type="success">数据管理</el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getEntityList, deleteEntity } from '@/api/tool/online-gen'
import { ElMessageBox, ElMessage } from 'element-plus'
import router from '@/router'

const loading = ref(false)
const list = ref([])
const total = ref(0)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  entityName: '',
  status: null
})

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getEntityList(queryParams.value)
    list.value = res.data.records || res.data
    total.value = res.data.total || res.data.length
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 重置查询
const resetQuery = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    entityName: '',
    status: null
  }
  getList()
}

// 新增
const handleCreate = () => {
  router.push({ name: 'DynamicForm', query: { action: 'create' } })
}

// 编辑
const handleEdit = (row) => {
  router.push({ name: 'DynamicForm', query: { action: 'edit', id: row.id } })
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除实体 "${row.entityName}" 吗？`, '警告', {
      type: 'warning'
    })
    await deleteEntity(row.id)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    // 取消或失败
  }
}

// 跳转数据管理页
const goDataPage = (row) => {
  router.push({ name: 'DynamicData', params: { entityId: row.id } })
}

onMounted(() => {
  getList()
})
</script>