package com.datn.backend.service.impl;

import com.datn.backend.dto.request.CategoryRequest;
import com.datn.backend.dto.response.CategoryResponse;
import com.datn.backend.entity.Category;
import com.datn.backend.exception.ResourceNotFoundException;
import com.datn.backend.repository.CategoryRepository;
import com.datn.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Lấy tất cả danh mục
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh mục theo ID
     */
    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {

        Integer categoryId = convertToInteger(id);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy danh mục với ID: " + id
                        )
                );

        return mapToResponse(category);
    }

    /**
     * Tạo danh mục
     */
    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {

        // Kiểm tra tên danh mục đã tồn tại
        if (categoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException(
                    "Tên danh mục đã tồn tại"
            );
        }

        // Tìm danh mục cha nếu có
        Category parent = null;

        if (request.getParentId() != null) {

            Integer parentId = convertToInteger(request.getParentId());

            parent = categoryRepository.findById(parentId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Danh mục cha không tồn tại"
                            )
                    );
        }

        // Tạo category
        Category category = Category.builder()
                .name(request.getName())
                .slug(toSlug(request.getName()))
                .description(request.getDescription())
                .parent(parent)
                .build();

        Category savedCategory = categoryRepository.save(category);

        return mapToResponse(savedCategory);
    }

    /**
     * Cập nhật danh mục
     */
    @Override
    @Transactional
    public CategoryResponse updateCategory(
            Long id,
            CategoryRequest request
    ) {

        Integer categoryId = convertToInteger(id);

        // Tìm danh mục cần cập nhật
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy danh mục với ID: " + id
                        )
                );

        // Kiểm tra tên danh mục
        if (categoryRepository.existsByName(request.getName())
                && !category.getName().equalsIgnoreCase(request.getName())) {

            throw new IllegalArgumentException(
                    "Tên danh mục đã tồn tại"
            );
        }

        // Tìm danh mục cha
        Category parent = null;

        if (request.getParentId() != null) {

            Integer parentId = convertToInteger(request.getParentId());

            // Không cho danh mục làm cha của chính nó
            if (parentId.equals(categoryId)) {
                throw new IllegalArgumentException(
                        "Danh mục cha không thể là chính nó"
                );
            }

            parent = categoryRepository.findById(parentId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Danh mục cha không tồn tại"
                            )
                    );

            /*
             * Không cho phép chọn một danh mục con làm danh mục cha
             * của chính danh mục hiện tại.
             *
             * Ví dụ:
             * Điện tử
             *   └── Điện thoại
             *
             * Không thể đặt Điện tử có parent là Điện thoại.
             */
            if (isDescendant(parent, category)) {
                throw new IllegalArgumentException(
                        "Không thể chọn danh mục con làm danh mục cha"
                );
            }
        }

        // Cập nhật thông tin
        category.setName(request.getName());
        category.setSlug(toSlug(request.getName()));
        category.setDescription(request.getDescription());
        category.setParent(parent);

        Category updatedCategory = categoryRepository.save(category);

        return mapToResponse(updatedCategory);
    }

    /**
     * Xóa danh mục
     */
    @Override
    @Transactional
    public void deleteCategory(Long id) {

        Integer categoryId = convertToInteger(id);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy danh mục với ID: " + id
                        )
                );

        categoryRepository.delete(category);
    }

    /**
     * Chuyển Category Entity -> CategoryResponse
     *
     * Entity:
     *     categoryId = Integer
     *
     * Response:
     *     id = Long
     *
     * Vì vậy cần convert Integer -> Long.
     */
    private CategoryResponse mapToResponse(Category category) {

        return CategoryResponse.builder()
                .id(
                        category.getCategoryId() != null
                                ? category.getCategoryId().longValue()
                                : null
                )
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(
                        category.getParent() != null
                                && category.getParent().getCategoryId() != null
                                ? category.getParent()
                                        .getCategoryId()
                                        .longValue()
                                : null
                )
                .createdAt(category.getCreatedAt())
                .build();
    }

    /**
     * Kiểm tra parent có phải là hậu duệ của category hay không.
     *
     * Ví dụ:
     *
     * Điện tử
     *   └── Điện thoại
     *         └── Smartphone
     *
     * Nếu đang sửa "Điện tử" mà chọn "Smartphone"
     * làm parent thì không hợp lệ.
     */
    private boolean isDescendant(
            Category potentialParent,
            Category category
    ) {

        Category current = potentialParent;

        while (current != null) {

            if (current.getCategoryId()
                    .equals(category.getCategoryId())) {
                return true;
            }

            current = current.getParent();
        }

        return false;
    }

    /**
     * Chuyển Long -> Integer
     *
     * Vì Category Entity sử dụng Integer categoryId.
     */
    private Integer convertToInteger(Long id) {

        if (id == null) {
            return null;
        }

        try {
            return Math.toIntExact(id);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "ID danh mục không hợp lệ: " + id
            );
        }
    }

    /**
     * Regex dùng để tạo slug.
     */
    private static final Pattern NONLATIN =
            Pattern.compile("[^\\w-]");

    private static final Pattern WHITESPACE =
            Pattern.compile("[\\s]+");

    /**
     * Chuyển tên danh mục thành slug.
     *
     * Ví dụ:
     *
     * "Đồ Gia Dụng"
     *      ↓
     * "do-gia-dung"
     */
    private String toSlug(String input) {

        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        // Loại bỏ khoảng trắng đầu/cuối
        String trimmed = input.trim();

        // Thay khoảng trắng bằng dấu "-"
        String noWhitespace =
                WHITESPACE
                        .matcher(trimmed)
                        .replaceAll("-");

        // Chuẩn hóa Unicode
        String normalized =
                Normalizer.normalize(
                        noWhitespace,
                        Normalizer.Form.NFD
                );

        // Loại bỏ dấu tiếng Việt và ký tự không hợp lệ
        String slug =
                NONLATIN
                        .matcher(normalized)
                        .replaceAll("");

        // Chuyển thành chữ thường
        return slug.toLowerCase(Locale.ENGLISH);
    }
}