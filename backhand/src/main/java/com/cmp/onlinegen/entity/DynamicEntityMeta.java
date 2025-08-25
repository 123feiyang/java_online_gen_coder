package com.cmp.onlinegen.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "sys_online_gen_entity")
@ToString(exclude = {"fields"})
public class DynamicEntityMeta {
    @Id
    private String id;
    private String entityName;
    private String tableName;
    private String description;
    private Integer status; // 0-草稿, 1-已发布
    
    @CreationTimestamp
    private Date createTime;
    
    @UpdateTimestamp
    private Date updateTime;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "entity")
    @JsonManagedReference
    private List<DynamicFieldMeta> fields;
}