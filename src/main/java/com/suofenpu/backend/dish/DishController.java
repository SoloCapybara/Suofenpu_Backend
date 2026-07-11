package com.suofenpu.backend.dish;

import static com.suofenpu.backend.dish.DishDtos.DishResponse;
import static com.suofenpu.backend.dish.DishDtos.DishCategoryResponse;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/menu")
@Tag(name = "客户端菜单")
public class DishController {
    private final DishRepository dishRepository;
    private final DishCategoryRepository dishCategoryRepository;

    public DishController(DishRepository dishRepository, DishCategoryRepository dishCategoryRepository) {
        this.dishRepository = dishRepository;
        this.dishCategoryRepository = dishCategoryRepository;
    }

    @GetMapping("/categories")
    @Operation(
        summary = "获取启用菜品分类",
        description = "小程序菜单页使用。只返回 enabled=true 的分类，按 sortOrder 升序排列。"
    )
    public List<DishCategoryResponse> listEnabledCategories() {
        return dishCategoryRepository.findByEnabledTrueOrderBySortOrderAscIdAsc().stream()
            .map(DishCategoryResponse::from)
            .toList();
    }

    @GetMapping("/dishes")
    @Operation(
        summary = "获取上架菜品",
        description = "小程序菜单页/首页推荐使用。只返回 available=true 的菜品；下单时使用返回的 id 作为 items[].dishId。"
    )
    public List<DishResponse> listAvailableDishes() {
        return dishRepository.findAvailableWithCategory().stream()
            .map(DishResponse::from)
            .toList();
    }
}
