package com.cmp.onlinegen.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "sys_online_gen_field")
@ToString(exclude = {"entity"})
public class DynamicFieldMeta {
    @Id
    private String id;
    private String fieldName;
    private String fieldLabel;
    private String fieldType;
    private Integer length;
    private Boolean nullable;
    private Boolean primaryKey;
    private Boolean inList;
    private Boolean inForm;
    private Boolean inSearch;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id")
    @JsonBackReference
    private DynamicEntityMeta entity;
}