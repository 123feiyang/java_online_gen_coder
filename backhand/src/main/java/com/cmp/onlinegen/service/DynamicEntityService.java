package com.cmp.onlinegen.service;

import com.cmp.onlinegen.entity.DynamicEntityMeta;
import com.cmp.onlinegen.entity.DynamicFieldMeta;
import com.cmp.onlinegen.repository.DynamicEntityRepository;
import com.cmp.onlinegen.repository.DynamicFieldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DynamicEntityService {
    
    @Autowired
    private DynamicEntityRepository entityRepository;
    
    @Autowired
    private DynamicFieldRepository fieldRepository;
    
    @Autowired
    private DatabaseService databaseService;
    
    public List<DynamicEntityMeta> listAll() {
        return entityRepository.findAll();
    }
    
    public DynamicEntityMeta getById(String id) {
        return entityRepository.findById(id).orElse(null);
    }
    
    public void createEntity(DynamicEntityMeta entity) {
        // 生成ID
        if (entity.getId() == null || entity.getId().isEmpty()) {
            entity.setId(UUID.randomUUID().toString());
        }
        
        // 设置表名
        if (entity.getTableName() == null || entity.getTableName().isEmpty()) {
            entity.setTableName("t_" + entity.getEntityName().toLowerCase());
        }
        
        // 默认状态为草稿
        if (entity.getStatus() == null) {
            entity.setStatus(0);
        }

        // 关键：设置字段的实体引用
        for (DynamicFieldMeta field : entity.getFields()) {
            field.setEntity(entity);
        }
        
        // 保存实体
        entityRepository.save(entity);
        
        // 创建数据库表
        databaseService.createTable(entity);
    }

    public void updateEntity(DynamicEntityMeta entity) {
        // 从数据库获取现有实体
        DynamicEntityMeta existingEntity = entityRepository.findById(entity.getId())
                .orElseThrow(() -> new RuntimeException("实体不存在"));

        // 复制属性到现有实体
        existingEntity.setEntityName(entity.getEntityName());
        existingEntity.setTableName(entity.getTableName());
        existingEntity.setDescription(entity.getDescription());
        existingEntity.setStatus(entity.getStatus());

        // 删除现有的所有字段
        fieldRepository.deleteByEntityId(entity.getId());

        // 为了避免懒加载问题，先刷新实体
        entityRepository.flush();

        // 创建新的字段列表
        List<DynamicFieldMeta> updatedFields = new ArrayList<>();

        // 为每个传入的字段创建新的实体
        for (DynamicFieldMeta field : entity.getFields()) {
            DynamicFieldMeta newField = new DynamicFieldMeta();
            if (field.getId() == null || field.getId().isEmpty()) {
                newField.setId(UUID.randomUUID().toString());
            } else {
                newField.setId(field.getId());
            }
            newField.setFieldName(field.getFieldName());
            newField.setFieldLabel(field.getFieldLabel());
            newField.setFieldType(field.getFieldType());
            newField.setLength(field.getLength());
            newField.setNullable(field.getNullable());
            newField.setPrimaryKey(field.getPrimaryKey());
            newField.setInList(field.getInList());
            newField.setInForm(field.getInForm());
            newField.setInSearch(field.getInSearch());
            newField.setEntity(existingEntity);

            fieldRepository.save(newField);
            updatedFields.add(newField);
        }

        // 设置新的字段列表
        existingEntity.setFields(updatedFields);

        // 保存实体
        entityRepository.save(existingEntity);

        // 更新数据库表结构
        databaseService.updateTable(existingEntity);
    }


    public void deleteEntity(String id) {
        DynamicEntityMeta entity = entityRepository.findById(id).orElse(null);
        if (entity != null) {
            // 删除数据库表
            databaseService.dropTable(entity.getTableName());
            // 删除字段
            fieldRepository.deleteByEntityId(id);
            // 删除实体
            entityRepository.deleteById(id);
        }
    }
}