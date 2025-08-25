package com.cmp.onlinegen.controller;

import com.cmp.common.core.domain.AjaxResult;
import com.cmp.onlinegen.entity.DynamicEntityMeta;
import com.cmp.onlinegen.service.DynamicEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.cmp.common.core.domain.AjaxResult.success;


@RestController
@RequestMapping("/api/entity")
public class DynamicEntityController {
    
    @Autowired
    private DynamicEntityService entityService;
    
    @GetMapping
    public AjaxResult list() {
        return success(entityService.listAll());
    }
    
    @GetMapping("/{id}")
    public AjaxResult getById(@PathVariable String id) {
        return success(entityService.getById(id));
    }
    
    @PostMapping
    public AjaxResult create(@RequestBody DynamicEntityMeta entity) {
        entityService.createEntity(entity);
        return success("创建成功");
    }
    
    @PutMapping("/{id}")
    public AjaxResult update(@PathVariable String id, @RequestBody DynamicEntityMeta entity) {
        entity.setId(id);
        entityService.updateEntity(entity);
        return success("更新成功");
    }
    
    @DeleteMapping("/{id}")
    public AjaxResult delete(@PathVariable String id) {
        entityService.deleteEntity(id);
        return success("删除成功");
    }
}