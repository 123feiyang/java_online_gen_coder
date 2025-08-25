package com.cmp.onlinegen.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Data
@Entity
@Table(name = "dynamic_config")
public class DynamicConfig {
    @Id
    private String id;
    private String entityId;
    private String formConfig;
    private String listConfig;
    private String searchConfig;
    private Integer status;
    
    @CreationTimestamp
    private Date createTime;
    
    @UpdateTimestamp
    private Date updateTime;
}