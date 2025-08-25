import request from '@/utils/request'
import SHSANYCONST from '@/utils/shsanyConst'

/**
 * ========================
 * 动态实体元数据管理
 * 对应 DynamicEntityController
 * ========================
 */

// 获取实体列表
export function getEntityList() {
  return request({
    url: `/${SHSANYCONST.SHSANY}/api/entity`,
    method: 'get'
  })
}

// 根据 ID 获取实体详情（含字段配置）
export function getEntityById(id) {
  return request({
    url: `/${SHSANYCONST.SHSANY}/api/entity/${id}`,
    method: 'get'
  })
}

// 创建动态实体
export function createEntity(data) {
  return request({
    url: `/${SHSANYCONST.SHSANY}/api/entity`,
    method: 'post',
    data
  })
}

// 更新动态实体
export function updateEntity(id, data) {
  return request({
    url: `/${SHSANYCONST.SHSANY}/api/entity/${id}`,
    method: 'put',
    data
  })
}

// 删除动态实体
export function deleteEntity(id) {
  return request({
    url: `/${SHSANYCONST.SHSANY}/api/entity/${id}`,
    method: 'delete'
  })
}

/**
 * ========================
 * 动态数据记录操作
 * 对应 DynamicDataController
 * ========================
 */

// 查询数据列表
export function queryList(entityId, params = {}) {
  return request({
    url: `/${SHSANYCONST.SHSANY}/api/dynamic/${entityId}/list`,
    method: 'post',
    data: params
  })
}

// // 获取实体配置（字段结构）
// export function getEntityById(entityId) {
//   return request({
//     url: `/${SHSANYCONST.SHSANY}/api/dynamic/${entityId}/config`,
//     method: 'get'
//   })
// }

// 保存新数据
export function saveData(entityId, data) {
  return request({
    url: `/${SHSANYCONST.SHSANY}/api/dynamic/${entityId}`,
    method: 'post',
    data
  })
}

// 更新数据
export function updateData(entityId, id, data) {
  return request({
    url: `/${SHSANYCONST.SHSANY}/api/dynamic/${entityId}/${id}`,
    method: 'put',
    data
  })
}

// 删除数据
export function deleteData(entityId, id) {
  return request({
    url: `/${SHSANYCONST.SHSANY}/api/dynamic/${entityId}/${id}`,
    method: 'delete'
  })
}