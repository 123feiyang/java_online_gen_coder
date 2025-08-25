package com.cmp.onlinegen.repository;

import com.cmp.onlinegen.entity.DynamicEntityMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DynamicEntityRepository extends JpaRepository<DynamicEntityMeta, String> {
}
