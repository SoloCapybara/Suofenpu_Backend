package com.suofenpu.backend.dish;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DishCategoryRepository extends JpaRepository<DishCategory, Long> {
    List<DishCategory> findByEnabledTrueOrderBySortOrderAscIdAsc();

    List<DishCategory> findAllByOrderBySortOrderAscIdAsc();

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
