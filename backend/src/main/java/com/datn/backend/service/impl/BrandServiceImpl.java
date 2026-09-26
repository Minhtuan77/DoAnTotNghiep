package com.datn.backend.service.impl;

import com.datn.backend.dto.request.BrandRequest;
import com.datn.backend.dto.response.BrandResponse;
import com.datn.backend.entity.Brand;
import com.datn.backend.exception.ResourceNotFoundException;
import com.datn.backend.repository.BrandRepository;
import com.datn.backend.repository.ProductRepository;
import com.datn.backend.exception.BusinessException;
import com.datn.backend.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    /**
     * Lấy tất cả thương hiệu
     */
    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getAllBrands() {

        return brandRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy thương hiệu theo ID
     */
    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrandById(Long id) {

        Integer brandId = convertToInteger(id);

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy thương hiệu với ID: " + id
                        )
                );

        return mapToResponse(brand);
    }

    /**
     * Tạo thương hiệu
     */
    @Override
    @Transactional
    public BrandResponse createBrand(BrandRequest request) {

        if (brandRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException(
                    "Tên thương hiệu đã tồn tại"
            );
        }

        Brand brand = Brand.builder()
                .name(request.getName())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .build();

        Brand savedBrand = brandRepository.save(brand);

        return mapToResponse(savedBrand);
    }

    /**
     * Cập nhật thương hiệu
     */
    @Override
    @Transactional
    public BrandResponse updateBrand(
            Long id,
            BrandRequest request
    ) {

        Integer brandId = convertToInteger(id);

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy thương hiệu với ID: " + id
                        )
                );

        // Kiểm tra tên có bị trùng với brand khác không
        if (brandRepository.existsByName(request.getName())
                && !brand.getName().equalsIgnoreCase(request.getName())) {

            throw new IllegalArgumentException(
                    "Tên thương hiệu đã tồn tại"
            );
        }

        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setLogoUrl(request.getLogoUrl());

        Brand updatedBrand = brandRepository.save(brand);

        return mapToResponse(updatedBrand);
    }

    /**
     * Xóa thương hiệu
     */
    @Override
    @Transactional
    public void deleteBrand(Long id) {

        Integer brandId = convertToInteger(id);

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy thương hiệu với ID: " + id
                        )
                );

        if (productRepository.existsByBrand_BrandId(brandId)) {
            throw new BusinessException(
                    "Không thể xóa thương hiệu đang được sản phẩm sử dụng. Hãy đổi thương hiệu của sản phẩm trước."
            );
        }
        brandRepository.delete(brand);
    }

    /**
     * Entity -> Response
     */
    private BrandResponse mapToResponse(Brand brand) {

        return BrandResponse.builder()
                .id(
                        brand.getBrandId() != null
                                ? brand.getBrandId().longValue()
                                : null
                )
                .name(brand.getName())
                .description(brand.getDescription())
                .logoUrl(brand.getLogoUrl())
                .build();
    }

    /**
     * Long -> Integer
     */
    private Integer convertToInteger(Long id) {

        if (id == null) {
            return null;
        }

        try {
            return Math.toIntExact(id);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "ID thương hiệu không hợp lệ: " + id
            );
        }
    }
}