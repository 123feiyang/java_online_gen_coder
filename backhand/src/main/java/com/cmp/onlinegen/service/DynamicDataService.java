package com.cmp.onlinegen.service;

import com.cmp.onlinegen.entity.DynamicEntityMeta;
import com.cmp.onlinegen.repository.DynamicEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DynamicDataService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private DynamicEntityRepository entityRepository;
    
    public Map<String, Object> queryList(String entityId, Map<String, Object> params) {
        DynamicEntityMeta entity = entityRepository.findById(entityId).orElse(null);
        if (entity == null) {
            throw new RuntimeException("实体不存在");
        }
        
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(entity.getTableName());
        
        List<Object> paramValues = new ArrayList<>();
        StringBuilder whereClause = new StringBuilder();
        
        if (params != null) {
            Map<String, Object> searchParams = (Map<String, Object>) params.get("search");
            if (searchParams != null) {
                for (Map.Entry<String, Object> entry : searchParams.entrySet()) {
                    if (entry.getValue() != null && !entry.getValue().toString().isEmpty()) {
                        if (whereClause.length() > 0) {
                            whereClause.append(" AND ");
                        }
                        whereClause.append(entry.getKey()).append(" LIKE ?");
                        paramValues.add("%" + entry.getValue() + "%");
                    }
                }
            }
        }
        
        if (whereClause.length() > 0) {
            sql.append(" WHERE ").append(whereClause);
        }
        
        // 分页
        int page = params != null && params.containsKey("page") ? (Integer) params.get("page") : 1;
        int pageSize = params != null && params.containsKey("pageSize") ? (Integer) params.get("pageSize") : 10;
        int offset = (page - 1) * pageSize;
        
        sql.append(" LIMIT ? OFFSET ?");
        paramValues.add(pageSize);
        paramValues.add(offset);
        
        List<Map<String, Object>> records = jdbcTemplate.query(sql.toString(), paramValues.toArray(), new ColumnMapRowMapper());
        
        // 总数查询
        String countSql = "SELECT COUNT(*) FROM " + entity.getTableName();
        if (whereClause.length() > 0) {
            countSql += " WHERE " + whereClause;
        }
        Long total = jdbcTemplate.queryForObject(countSql, paramValues.subList(0, paramValues.size() - 2).toArray(), Long.class);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("size", pageSize);
        result.put("current", page);
        
        return result;
    }
    
    public void save(String entityId, Map<String, Object> data) {
        DynamicEntityMeta entity = entityRepository.findById(entityId).orElse(null);
        if (entity == null) {
            throw new RuntimeException("实体不存在");
        }
        
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(entity.getTableName()).append(" (");
        
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        sql.append("id, ");
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            columns.add(entry.getKey());
            values.add(entry.getValue());
        }
        
        sql.append(String.join(", ", columns)).append(") VALUES (");
        sql.append("?, ");
        sql.append(columns.stream().map(c -> "?").collect(Collectors.joining(", ")));
        sql.append(")");
        
        jdbcTemplate.update(sql.toString(), UUID.randomUUID().toString(),values.toArray());
    }
    
    public void update(String entityId, String id, Map<String, Object> data) {
        DynamicEntityMeta entity = entityRepository.findById(entityId).orElse(null);
        if (entity == null) {
            throw new RuntimeException("实体不存在");
        }
        
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(entity.getTableName()).append(" SET ");
        
        List<String> setClauses = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            setClauses.add(entry.getKey() + " = ?");
            values.add(entry.getValue());
        }
        
        sql.append(String.join(", ", setClauses));
        sql.append(" WHERE id = ?");
        values.add(id);
        
        jdbcTemplate.update(sql.toString(), values.toArray());
    }
    
    public void delete(String entityId, String id) {
        DynamicEntityMeta entity = entityRepository.findById(entityId).orElse(null);
        if (entity == null) {
            throw new RuntimeException("实体不存在");
        }
        
        String sql = "DELETE FROM " + entity.getTableName() + " WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
    
    public Map<String, Object> getConfig(String entityId) {
        DynamicEntityMeta entity = entityRepository.findById(entityId).orElse(null);
        if (entity == null) {
            throw new RuntimeException("实体不存在");
        }
        
        Map<String, Object> config = new HashMap<>();
        config.put("entityName", entity.getEntityName());
        config.put("tableName", entity.getTableName());
        
        // 字段配置
        List<Map<String, Object>> fields = entity.getFields().stream().map(field -> {
            Map<String, Object> fieldConfig = new HashMap<>();
            fieldConfig.put("fieldName", field.getFieldName());
            fieldConfig.put("fieldLabel", field.getFieldLabel());
            fieldConfig.put("fieldType", field.getFieldType());
            fieldConfig.put("inList", field.getInList());
            fieldConfig.put("inForm", field.getInForm());
            fieldConfig.put("inSearch", field.getInSearch());
            return fieldConfig;
        }).collect(Collectors.toList());
        
        config.put("fields", fields);
        return config;
    }
    
    private static class ColumnMapRowMapper implements RowMapper<Map<String, Object>> {
        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> result = new HashMap<>();
            java.sql.ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            for (int i = 1; i <= columnCount; i++) {
                result.put(metaData.getColumnName(i), rs.getObject(i));
            }
            
            return result;
        }
    }
}