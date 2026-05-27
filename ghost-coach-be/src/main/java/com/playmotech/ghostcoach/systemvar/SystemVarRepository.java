package com.playmotech.ghostcoach.systemvar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemVarRepository extends JpaRepository<SystemVar, Long> {

    List<SystemVar> findByGroupCodeAndActiveTrueOrderBySortOrderAscIdAsc(String groupCode);
}
