package com.cmp.onlinegen.repository;

import com.cmp.onlinegen.entity.DynamicFieldMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DynamicFieldRepository extends JpaRepository<DynamicFieldMeta, String> {
    // 根据实体ID查找所有字段
    List<DynamicFieldMeta> findByEntityId(String entityId);

    // 根据实体ID删除字段
    // 根据实体ID删除字段 - 直接执行DELETE语句
    @Modifying
    @Query("DELETE FROM DynamicFieldMeta f WHERE f.entity.id = :entityId")
    int deleteByEntityId(@Param("entityId") String entityId);

    // 根据实体ID和字段名查找字段
    DynamicFieldMeta findByEntityIdAndFieldName(String entityId, String fieldName);
}
