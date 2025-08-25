package com.cmp.onlinegen.service;


import cn.hutool.core.convert.Convert;
import com.cmp.onlinegen.entity.DynamicEntityMeta;
import com.cmp.onlinegen.entity.DynamicFieldMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DatabaseService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public void createTable(DynamicEntityMeta entity) {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sql.append(entity.getTableName()).append(" (");
        
        // 添加ID字段
        sql.append("id VARCHAR(36) PRIMARY KEY, ");
        
        // 添加其他字段
        List<String> fieldDefinitions = entity.getFields().stream().map(this::generateFieldDefinition).collect(Collectors.toList());
        sql.append(String.join(", ", fieldDefinitions));
        
        sql.append(")");
        
        jdbcTemplate.execute(sql.toString());
    }
    
    public void updateTable(DynamicEntityMeta entity) {
        // 检查表是否存在
        String checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, entity.getTableName());

        if (count == 0) {
            createTable(entity);
            return;
        }

        // 获取现有字段信息
        Map<String, ColumnInfo> existingColumns = getTableColumnDetails(entity.getTableName());

        // 处理字段变更
        List<String> newFieldNames = entity.getFields().stream()
                .map(DynamicFieldMeta::getFieldName)
                .collect(Collectors.toList());

        // 删除不再需要的字段
        for (String columnName : existingColumns.keySet()) {
            // 跳过ID字段，因为它是主键不应该被删除
            if ("id".equals(columnName)) {
                continue;
            }

            if (!newFieldNames.contains(columnName)) {
                // 删除字段前需要考虑数据安全
                handleFieldDeletion(entity.getTableName(), columnName, existingColumns.get(columnName));
            }
        }

        // 处理新增和修改的字段
        for (DynamicFieldMeta field : entity.getFields()) {
            if (!existingColumns.containsKey(field.getFieldName())) {
                // 新增字段
                addColumn(entity, field);
            } else {
                // 检查字段是否需要修改
                ColumnInfo existingColumn = existingColumns.get(field.getFieldName());
                if (needsModification(field, existingColumn)) {
                    modifyColumn(entity, field, existingColumn);
                }
            }
        }
    }

    // 字段信息类
    private static class ColumnInfo {
        String columnName;
        String dataType;
        String isNullable;
        Integer characterMaximumLength;
        Integer numericPrecision;
        Integer numericScale;

        // 构造函数和getter/setter
        public ColumnInfo(String columnName, String dataType, String isNullable,
                          Integer characterMaximumLength, Integer numericPrecision, Integer numericScale) {
            this.columnName = columnName;
            this.dataType = dataType;
            this.isNullable = isNullable;
            this.characterMaximumLength = characterMaximumLength;
            this.numericPrecision = numericPrecision;
            this.numericScale = numericScale;
        }
    }

    // 获取表的详细字段信息
    private Map<String, ColumnInfo> getTableColumnDetails(String tableName) {
        String sql = "SELECT column_name, data_type, is_nullable, character_maximum_length, " +
                "numeric_precision, numeric_scale FROM information_schema.columns WHERE table_name = ?";

        Map<String, ColumnInfo> columns = new HashMap<>();

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, tableName);
        for (Map<String, Object> row : rows) {
            String columnName = (String) row.get("column_name");
            columns.put(columnName, new ColumnInfo(
                    columnName,
                    (String) row.get("data_type"),
                    (String) row.get("is_nullable"),
                    Convert.toInt(row.get("character_maximum_length"),0) ,
                    Convert.toInt(row.get("numeric_precision"),0) ,
                    Convert.toInt(row.get("numeric_scale"),0)
            ));
        }

        return columns;
    }

    // 处理字段删除
    private void handleFieldDeletion(String tableName, String columnName, ColumnInfo columnInfo) {
        // 记录警告信息，让用户确认是否删除字段
        log.warn("警告: 表 {} 中的字段 {} 将被删除，数据可能会丢失。", tableName, columnName);

        // 实际删除字段（在生产环境中可能需要更复杂的确认机制）
        String dropColumnSql = String.format("ALTER TABLE %s DROP COLUMN %s", tableName, columnName);
        try {
            jdbcTemplate.execute(dropColumnSql);
            log.info("字段 {} 已成功删除。", columnName);
        } catch (Exception e) {
            log.error("删除字段 {} 失败: {}", columnName, e.getMessage());
            throw e;
        }
    }

    // 添加新字段
    private void addColumn(DynamicEntityMeta entity, DynamicFieldMeta field) {
        String addColumnSql = String.format("ALTER TABLE %s ADD COLUMN %s %s%s",
                entity.getTableName(),
                field.getFieldName(),
                mapFieldType(field),
                field.getNullable() ? "" : " NOT NULL");

        try {
            jdbcTemplate.execute(addColumnSql);
            log.info("字段 {} 已成功添加。", field.getFieldName());
        } catch (Exception e) {
            log.error("添加字段 {} 失败: {}", field.getFieldName(), e.getMessage());
            throw e;
        }
    }

    // 修改现有字段
    private void modifyColumn(DynamicEntityMeta entity, DynamicFieldMeta field, ColumnInfo existingColumn) {
        // 对于某些类型的修改，需要特别小心以避免数据丢失

        String newType = mapFieldType(field);
        String existingType = existingColumn.dataType.toUpperCase();

        // 检查是否可以安全修改
        if (isSafeModification(existingColumn, field)) {
            StringBuilder modifySql = new StringBuilder();
            modifySql.append("ALTER TABLE ").append(entity.getTableName())
                    .append(" MODIFY COLUMN ").append(field.getFieldName())
                    .append(" ").append(newType);

            if (!field.getNullable()) {
                modifySql.append(" NOT NULL");
            }

            try {
                jdbcTemplate.execute(modifySql.toString());
                log.info("字段 {} 已成功修改。", field.getFieldName());
            } catch (Exception e) {
                log.error("修改字段 {} 失败: {}", field.getFieldName(), e.getMessage());
                throw new RuntimeException("修改字段 " + field.getFieldName() + " 失败: " + e.getMessage());
            }
        } else {
            // 对于不安全的修改，记录警告信息
            System.out.println("警告: 字段 " + field.getFieldName() + " 的修改可能不安全，需要手动处理。");
            System.out.println("现有类型: " + existingType + ", 新类型: " + newType);
        }
    }

    // 判断字段修改是否安全
    private boolean isSafeModification(ColumnInfo existingColumn, DynamicFieldMeta newField) {
        String existingType = existingColumn.dataType.toUpperCase();
        String newType = mapFieldType1(newField).toUpperCase();

        // 相同类型总是安全的
        if (existingType.equals(newType)) {
            return true;
        }

        // 一些安全的类型转换示例：
        // VARCHAR 增加长度是安全的
        if (existingType.contains("VARCHAR") && newType.contains("VARCHAR")) {
            if (newField.getLength() >= existingColumn.characterMaximumLength) {
                return true;
            }
        }

        // 从较小的整数类型到较大的整数类型是安全的
        if (("TINYINT".equals(existingType) || "SMALLINT".equals(existingType) || "INT".equals(existingType))
                && "BIGINT".equals(newType)) {
            return true;
        }

        // 其他情况默认为不安全
        return false;
    }

    // 检查字段是否需要修改
    private boolean needsModification(DynamicFieldMeta field, ColumnInfo existingColumn) {
        String newType = mapFieldType1( field).toUpperCase();
        String existingType = existingColumn.dataType.toUpperCase();

        // 检查类型是否不同
        if (!newType.equals(existingType)) {
            return true;
        }

        // 检查可空性是否不同
        boolean existingNullable = "YES".equals(existingColumn.isNullable);
        if (existingNullable != field.getNullable()) {
            return true;
        }
        // 检查字符串长度是否不同（仅对VARCHAR类型）
        if (newType.contains("VARCHAR") && existingType.contains("VARCHAR")) {
            int existingLength = existingColumn.characterMaximumLength;
            int newLength = field.getLength();
            if (newLength != existingLength) {
                return true;
            }
        }

        return false;
    }
    
    public void dropTable(String tableName) {
        String sql = "DROP TABLE IF EXISTS " + tableName;
        jdbcTemplate.execute(sql);
    }
    
    private String generateFieldDefinition(DynamicFieldMeta field) {
        StringBuilder definition = new StringBuilder();
        definition.append(field.getFieldName()).append(" ").append(mapFieldType(field));
        
        if (!field.getNullable()) {
            definition.append(" NOT NULL");
        }
        
        return definition.toString();
    }
    
    private String mapFieldType(DynamicFieldMeta field) {
        switch (field.getFieldType()) {
            case "string":
                return "VARCHAR(" + (field.getLength() != null ? field.getLength() : 255) + ")";
            case "int":
                return "INTEGER";
            case "long":
                return "BIGINT";
            case "double":
                return "DOUBLE";
            case "decimal":
                return "DECIMAL(19,2)";
            case "date":
                return "DATE";
            case "datetime":
                return "TIMESTAMP";
            case "boolean":
                return "TINYINT";
            case "text":
                return "TEXT";
            default:
                return "VARCHAR(255)";
        }
    }
    private String mapFieldType1(DynamicFieldMeta field) {
        switch (field.getFieldType()) {
            case "string":
                return "VARCHAR";
            case "int":
                return "INTEGER";
            case "long":
                return "BIGINT";
            case "double":
                return "DOUBLE";
            case "decimal":
                return "DECIMAL";
            case "date":
                return "DATE";
            case "datetime":
                return "TIMESTAMP";
            case "boolean":
                return "TINYINT";
            case "text":
                return "TEXT";
            default:
                return "VARCHAR";
        }
    }
    
    private List<String> getTableColumns(String tableName) {
        String sql = "SELECT column_name FROM information_schema.columns WHERE table_name = ?";
        return jdbcTemplate.queryForList(sql, String.class, tableName);
    }
}