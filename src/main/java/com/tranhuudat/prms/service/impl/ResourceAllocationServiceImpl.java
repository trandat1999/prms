package com.tranhuudat.prms.service.impl;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.resource_allocation.ResourceAllocationDto;
import com.tranhuudat.prms.dto.resource_allocation.ResourceAllocationSearchRequest;
import com.tranhuudat.prms.entity.ResourceAllocation;
import com.tranhuudat.prms.entity.User;
import com.tranhuudat.prms.repository.AppParamRepository;
import com.tranhuudat.prms.repository.ResourceAllocationRepository;
import com.tranhuudat.prms.repository.UserRepository;
import com.tranhuudat.prms.service.BaseService;
import com.tranhuudat.prms.service.ResourceAllocationService;
import com.tranhuudat.prms.util.ConstUtil;
import com.tranhuudat.prms.util.DateUtil;
import com.tranhuudat.prms.util.ExcelUtil;
import com.tranhuudat.prms.util.SystemMessage;
import com.tranhuudat.prms.util.SystemVariable;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceAllocationServiceImpl extends BaseService implements ResourceAllocationService {

    private static final String PARAM_GROUP_MODULE_RESOURCE_ALLOCATION = "MODULE_RESOURCE_ALLOCATION";
    private static final String PARAM_TYPE_RESOURCE_ALLOCATION = "RESOURCE_ALLOCATION";

    private final ResourceAllocationRepository resourceAllocationRepository;
    private final UserRepository userRepository;
    private final AppParamRepository appParamRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public BaseResponse create(ResourceAllocationDto request) {
        HashMap<String, String> errors = validation(request);
        errors.putAll(validateBusiness(request));
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        ResourceAllocation entity = new ResourceAllocation();
        BeanUtils.copyProperties(request, entity, ConstUtil.NON_UPDATABLE_FIELDS);
        entity.setResourceMonth(request.getMonth());
        entity.setVoided(false);
        entity = resourceAllocationRepository.save(entity);
        return getResponse201(new ResourceAllocationDto(entity), getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional
    public BaseResponse update(UUID id, ResourceAllocationDto request) {
        HashMap<String, String> errors = validation(request);
        ResourceAllocation entity = resourceAllocationRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.RESOURCE_ALLOCATION)));
        }
        errors.putAll(validateBusiness(request));
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        BeanUtils.copyProperties(request, entity, ConstUtil.NON_UPDATABLE_FIELDS);
        entity.setResourceMonth(request.getMonth());
        entity = resourceAllocationRepository.save(entity);
        return getResponse200(new ResourceAllocationDto(entity));
    }

    @Override
    @Transactional
    public BaseResponse delete(UUID id) {
        ResourceAllocation entity = resourceAllocationRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.RESOURCE_ALLOCATION)));
        }
        entity.setVoided(true);
        resourceAllocationRepository.save(entity);
        return getResponse200(new ResourceAllocationDto(entity), getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getById(UUID id) {
        ResourceAllocation entity = resourceAllocationRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.RESOURCE_ALLOCATION)));
        }
        return getResponse200(new ResourceAllocationDto(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getPage(ResourceAllocationSearchRequest request) {
        if(Objects.nonNull(request.getMonth())){
            request.setYear(DateUtil.getYear(request.getMonth()));
            request.setMonthYear(DateUtil.getMonth(request.getMonth()));
        }
        return getResponse200(resourceAllocationRepository.getPages(entityManager, request, getPageable(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportResourceEmployeeExcel(ResourceAllocationSearchRequest request) {
        if (request == null || request.getMonth() == null) {
            throw new IllegalArgumentException(getMessage(SystemMessage.RESOURCE_ALLOCATION_EXPORT_MONTH_REQUIRED));
        }
        Date month = request.getMonth();
        int year = Objects.requireNonNull(DateUtil.getYear(month));
        int monthValue = Objects.requireNonNull(DateUtil.getMonth(month));
        List<ResourceAllocationDto> rows =
                resourceAllocationRepository.listAllByResourceMonth(entityManager, year, monthValue);

        ClassPathResource template = new ClassPathResource("templates/excel/template_resource_employee.xlsx");
        try (InputStream in = template.getInputStream();
                Workbook wb = ExcelUtil.loadWorkbook(in)) {
            Sheet sheet = wb.getSheetAt(0);
            String monthLabel = DateUtil.toLocalDate(month).format(DateTimeFormatter.ofPattern("MM/yyyy"));
            Row titleRow = sheet.getRow(0);
            if (titleRow != null) {
                ExcelUtil.setCellText(titleRow, 0, "Nguồn lực tháng: " + monthLabel);
            }
            Row headerRow = sheet.getRow(1);
            CellStyle[] bodyStyles = new CellStyle[5];
            for (int c = 0; c < 5; c++) {
                Cell sample = headerRow != null ? headerRow.getCell(c) : null;
                bodyStyles[c] = ExcelUtil.createBodyStyleWithoutFill(wb, sample);
            }
            DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            int dataRowIndex = 2;
            int stt = 1;
            for (ResourceAllocationDto dto : rows) {
                Row row = sheet.createRow(dataRowIndex++);
                for (int c = 0; c < 5; c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellStyle(bodyStyles[c]);
                }
                ExcelUtil.setCellText(row, 0, String.valueOf(stt++));
                ExcelUtil.setCellText(row, 1, buildPersonLabel(dto));
                ExcelUtil.setCellText(row, 2, formatDateForExport(dto.getStartDate(), dayFmt));
                ExcelUtil.setCellText(row, 3, formatDateForExport(dto.getEndDate(), dayFmt));
                ExcelUtil.setCellText(row, 4, formatPercentForExport(dto.getAllocationPercent()));
            }
            ExcelUtil.autoSizeColumns(sheet, 0, 4);
            return ExcelUtil.workbookToBytes(wb);
        } catch (IOException ex) {
            log.error("exportResourceEmployeeExcel failed", ex);
            throw new IllegalStateException(getMessage(SystemMessage.WRITE_FILE_ERROR));
        }
    }

    private static String buildPersonLabel(ResourceAllocationDto dto) {
        String base = dto.getUserDisplay() != null ? dto.getUserDisplay().trim() : "";
        String role = dto.getRole() != null ? dto.getRole().trim() : "";
        if (StringUtils.hasText(role)) {
            return base + " — " + role;
        }
        return base;
    }

    private static String formatDateForExport(Date d, DateTimeFormatter fmt) {
        if (d == null) {
            return "";
        }
        return DateUtil.toLocalDate(d).format(fmt);
    }

    private static String formatPercentForExport(BigDecimal p) {
        if (p == null) {
            return "";
        }
        return p.stripTrailingZeros().toPlainString() + "%";
    }

    private HashMap<String, String> validateBusiness(ResourceAllocationDto request) {
        HashMap<String, String> errors = new HashMap<>();
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null || Boolean.TRUE.equals(user.getVoided())) {
                errors.put(SystemVariable.USER_ID, getMessage(SystemMessage.NOT_FOUND, getMessage(SystemVariable.USER)));
            }
        }
        if (StringUtils.hasText(request.getRole())
                && !appParamRepository.existsActiveParamValue(
                PARAM_GROUP_MODULE_RESOURCE_ALLOCATION,
                PARAM_TYPE_RESOURCE_ALLOCATION,
                request.getRole().trim())) {
            errors.put(SystemVariable.ROLE, getMessage(SystemMessage.BAD_REQUEST));
        }
        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getStartDate().after(request.getEndDate())) {
            errors.put(SystemVariable.END_DATE, getMessage(SystemMessage.BAD_REQUEST));
        }
        return errors;
    }
}
