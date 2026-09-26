package com.datn.backend.service.impl;

import com.datn.backend.dto.request.VoucherRequest;
import com.datn.backend.dto.response.VoucherResponse;
import com.datn.backend.dto.response.VoucherValidationResponse;
import com.datn.backend.entity.Voucher;
import com.datn.backend.entity.enums.DiscountType;
import com.datn.backend.entity.enums.VoucherStatus;
import com.datn.backend.exception.BusinessException;
import com.datn.backend.exception.ResourceNotFoundException;
import com.datn.backend.repository.VoucherRepository;
import com.datn.backend.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoucherServiceImpl implements VoucherService {
    private final VoucherRepository voucherRepository;

    @Override
    @Transactional
    public VoucherResponse create(VoucherRequest request) {
        validateRequest(request);
        String code = normalizeCode(request.getCode());
        if (voucherRepository.existsByCodeIgnoreCase(code)) throw new BusinessException("Mã voucher đã tồn tại");
        Voucher voucher = new Voucher();
        apply(voucher, request, code);
        voucher.setUsedCount(0);
        return toResponse(voucherRepository.save(voucher));
    }

    @Override
    @Transactional
    public VoucherResponse update(Long id, VoucherRequest request) {
        validateRequest(request);
        Voucher voucher = getEntity(id);
        String code = normalizeCode(request.getCode());
        voucherRepository.findByCodeIgnoreCase(code).ifPresent(existing -> {
            if (!existing.getVoucherId().equals(id)) throw new BusinessException("Mã voucher đã tồn tại");
        });
        apply(voucher, request, code);
        return toResponse(voucherRepository.save(voucher));
    }

    @Override
    @Transactional
    public VoucherResponse disable(Long id) {
        Voucher voucher = getEntity(id);
        voucher.setStatus(VoucherStatus.DISABLED);
        return toResponse(voucherRepository.save(voucher));
    }

    @Override
    public VoucherResponse getById(Long id) { return toResponse(getEntity(id)); }

    @Override
    public Page<VoucherResponse> getAll(Pageable pageable) { return voucherRepository.findAll(pageable).map(this::toResponse); }

    @Override
    public VoucherValidationResponse validate(String rawCode, BigDecimal orderAmount, Long userId) {
        String code = normalizeCode(rawCode);
        Voucher voucher = voucherRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new BusinessException("Mã voucher không tồn tại"));
        LocalDateTime now = LocalDateTime.now();
        if (voucher.getStatus() != VoucherStatus.ACTIVE) throw new BusinessException("Voucher không còn hoạt động");
        if (now.isBefore(voucher.getStartDate())) throw new BusinessException("Voucher chưa đến thời gian sử dụng");
        if (now.isAfter(voucher.getEndDate())) throw new BusinessException("Voucher đã hết hạn");
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit())
            throw new BusinessException("Voucher đã đạt giới hạn sử dụng");
        if (orderAmount.compareTo(voucher.getMinOrderValue()) < 0)
            throw new BusinessException("Đơn hàng chưa đạt giá trị tối thiểu " + voucher.getMinOrderValue());

        BigDecimal discount;
        if (voucher.getDiscountType() == DiscountType.PERCENT) {
            discount = orderAmount.multiply(voucher.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (voucher.getMaxDiscountValue() != null && discount.compareTo(voucher.getMaxDiscountValue()) > 0)
                discount = voucher.getMaxDiscountValue();
        } else {
            discount = voucher.getDiscountValue();
        }
        if (discount.compareTo(orderAmount) > 0) discount = orderAmount;
        return VoucherValidationResponse.builder().voucherId(voucher.getVoucherId()).code(voucher.getCode())
                .orderAmount(orderAmount).discountAmount(discount).finalAmount(orderAmount.subtract(discount))
                .message("Voucher hợp lệ").build();
    }

    private void validateRequest(VoucherRequest r) {
        if (!r.getEndDate().isAfter(r.getStartDate())) throw new BusinessException("Thời gian kết thúc phải sau thời gian bắt đầu");
        if (r.getDiscountType() == DiscountType.PERCENT && r.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0)
            throw new BusinessException("Voucher phần trăm không được giảm quá 100%");
    }

    private void apply(Voucher v, VoucherRequest r, String code) {
        v.setCode(code); v.setDescription(r.getDescription()); v.setDiscountType(r.getDiscountType());
        v.setDiscountValue(r.getDiscountValue()); v.setMinOrderValue(r.getMinOrderValue() == null ? BigDecimal.ZERO : r.getMinOrderValue());
        v.setMaxDiscountValue(r.getMaxDiscountValue()); v.setUsageLimit(r.getUsageLimit());
        v.setStartDate(r.getStartDate()); v.setEndDate(r.getEndDate());
        v.setStatus(r.getStatus() == null ? VoucherStatus.ACTIVE : r.getStatus());
    }

    private String normalizeCode(String code) { return code.trim().toUpperCase(Locale.ROOT); }
    private Voucher getEntity(Long id) { return voucherRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher")); }
    private VoucherResponse toResponse(Voucher v) {
        Integer remaining = v.getUsageLimit() == null ? null : Math.max(0, v.getUsageLimit() - v.getUsedCount());
        return VoucherResponse.builder().voucherId(v.getVoucherId()).code(v.getCode()).description(v.getDescription())
                .discountType(v.getDiscountType()).discountValue(v.getDiscountValue()).minOrderValue(v.getMinOrderValue())
                .maxDiscountValue(v.getMaxDiscountValue()).usageLimit(v.getUsageLimit()).usedCount(v.getUsedCount())
                .remainingUses(remaining).startDate(v.getStartDate()).endDate(v.getEndDate()).status(v.getStatus())
                .createdAt(v.getCreatedAt()).build();
    }
}
