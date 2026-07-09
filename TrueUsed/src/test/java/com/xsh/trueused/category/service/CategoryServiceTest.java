package com.xsh.trueused.category.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.xsh.trueused.category.dto.CategoryDTO;
import com.xsh.trueused.category.repository.CategoryRepository;
import com.xsh.trueused.entity.Category;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void listRootShouldMapRootCategories() {
        when(categoryRepository.findByParentIsNull()).thenReturn(List.of(category(1L, "电脑", null)));

        List<CategoryDTO> result = categoryService.listRoot();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("电脑", result.get(0).name());
        assertEquals("computers", result.get(0).slug());
    }

    @Test
    void listAllShouldExposeParentId() {
        Category parent = category(1L, "电脑", null);
        Category child = category(2L, "笔记本", parent);
        when(categoryRepository.findAll()).thenReturn(List.of(parent, child));

        List<CategoryDTO> result = categoryService.listAll();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(1).parentId());
    }

    private static Category category(Long id, String name, Category parent) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setSlug(id == 1L ? "computers" : "laptops");
        category.setPath(parent == null ? "/computers" : "/computers/laptops");
        category.setStatus("ACTIVE");
        category.setParent(parent);
        return category;
    }
}
