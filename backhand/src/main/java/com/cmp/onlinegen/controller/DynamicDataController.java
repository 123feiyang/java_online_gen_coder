package com.cmp.onlinegen.controller;


import com.cmp.common.core.domain.AjaxResult;
import com.cmp.onlinegen.service.DynamicDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.cmp.common.core.domain.AjaxResult.success;

@RestController
@RequestMapping("/api/dynamic")
public class DynamicDataController {
    
    @Autowired
    private DynamicDataService dataService;
    
    @PostMapping("/{entityId}/list")
    public AjaxResult list(
            @PathVariable String entityId,
            @RequestBody(required = false) Map<String, Object> params) {
        return success(dataService.queryList(entityId, params));
    }
    
    @PostMapping("/{entityId}")
    public AjaxResult save(
            @PathVariable String entityId,
            @RequestBody Map<String, Object> data) {
        dataService.save(entityId, data);
        return success("保存成功");
    }
    
    @PutMapping("/{entityId}/{id}")
    public AjaxResult update(
            @PathVariable String entityId,
            @PathVariable String id,
            @RequestBody Map<String, Object> data) {
        dataService.update(entityId, id, data);
        return success("更新成功");
    }
    
    @DeleteMapping("/{entityId}/{id}")
    public AjaxResult delete(
            @PathVariable String entityId,
            @PathVariable String id) {
        dataService.delete(entityId, id);
        return success("删除成功");
    }
    
    @GetMapping("/{entityId}/config")
    public AjaxResult getConfig(@PathVariable String entityId) {
        return success(dataService.getConfig(entityId));
    }
}