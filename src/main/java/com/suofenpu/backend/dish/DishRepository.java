package com.suofenpu.backend.dish;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface DishRepository extends JpaRepository<Dish, Long> {
    @Query("select d from Dish d join fetch d.category where d.available = true and d.category.enabled = true order by d.category.sortOrder asc, d.id asc")
    List<Dish> findAvailableWithCategory();

    @Query("select d from Dish d join fetch d.category order by d.category.sortOrder asc, d.id asc")
    List<Dish> findAllWithCategory();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Dish d join fetch d.category where d.id = :id")
    Optional<Dish> findByIdForUpdate(@Param("id") Long id);

    long countByCategoryId(Long categoryId);
}
