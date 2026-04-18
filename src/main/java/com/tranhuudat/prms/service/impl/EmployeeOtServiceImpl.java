package com.tranhuudat.prms.service.impl;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.employee_ot.EmployeeOtDto;
import com.tranhuudat.prms.dto.employee_ot.EmployeeOtMonthlyReportRequest;
import com.tranhuudat.prms.dto.employee_ot.EmployeeOtSearchRequest;
import com.tranhuudat.prms.entity.EmployeeOt;
import com.tranhuudat.prms.entity.Project;
import com.tranhuudat.prms.entity.User;
import com.tranhuudat.prms.enums.EmployeeOtStatusEnum;
import com.tranhuudat.prms.repository.EmployeeOtRepository;
import com.tranhuudat.prms.repository.ProjectRepository;
import com.tranhuudat.prms.repository.UserRepository;
import com.tranhuudat.prms.exception.AppException;
import com.tranhuudat.prms.service.BaseService;
import com.tranhuudat.prms.service.EmployeeOtService;
import com.tranhuudat.prms.util.DateUtil;
import com.tranhuudat.prms.util.SystemMessage;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeOtServiceImpl extends BaseService implements EmployeeOtService {
    private static final String FONT_TIMES_NEW_ROMAN = "Times New Roman";

    private final EmployeeOtRepository employeeOtRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public BaseResponse create(EmployeeOtDto request) {
        HashMap<String, String> errors = validation(request);
        normalizeCreateStatus(request);
        errors.putAll(validateBusiness(request));
        errors.putAll(validateCreateStatus(request));
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        EmployeeOt entity = new EmployeeOt();
        BeanUtils.copyProperties(request, entity, "user", "project", "approver");
        entity.setVoided(false);
        entity.setApprovedBy(null);
        entity.setApprovedDate(null);
        entity = employeeOtRepository.save(entity);
        return getResponse201(new EmployeeOtDto(entity), getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional
    public BaseResponse update(UUID id, EmployeeOtDto request) {
        HashMap<String, String> errors = validation(request);
        EmployeeOt entity = employeeOtRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, "employee_ot"));
        }
        errors.putAll(validateBusiness(request));
        errors.putAll(validateUpdateTransition(request, entity));
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        applyUpdate(request, entity);
        entity = employeeOtRepository.save(entity);
        return getResponse200(new EmployeeOtDto(entity));
    }

    @Override
    @Transactional
    public BaseResponse delete(UUID id) {
        EmployeeOt entity = employeeOtRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, "employee_ot"));
        }
        entity.setVoided(true);
        employeeOtRepository.save(entity);
        return getResponse200(new EmployeeOtDto(entity), getMessage(SystemMessage.SUCCESS));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getById(UUID id) {
        EmployeeOt entity = employeeOtRepository.findById(id).orElse(null);
        if (Objects.isNull(entity) || Boolean.TRUE.equals(entity.getVoided())) {
            return getResponse404(getMessage(SystemMessage.NOT_FOUND, "employee_ot"));
        }
        return getResponse200(new EmployeeOtDto(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getPage(EmployeeOtSearchRequest request) {
        return getResponse200(employeeOtRepository.getPages(entityManager, request, getPageable(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportMonthlyReport(EmployeeOtMonthlyReportRequest request) {
        HashMap<String, String> errors = validation(request);
        if (!CollectionUtils.isEmpty(errors)) {
            throw new AppException(getMessage(SystemMessage.BAD_REQUEST));
        }
        YearMonth ym = parseYearMonthOrThrow(request.getMonth());
        Date from = toDate(ym.atDay(1).atStartOfDay());
        Date to = DateUtil.getEndOfDay(toDate(ym.atEndOfMonth().atStartOfDay()));

        List<EmployeeOt> ots = employeeOtRepository.findForMonthlyReport(
                entityManager,
                request.getUserId(),
                request.getProjectId(),
                request.getStatus(),
                request.getOtType(),
                request.getKeyword(),
                from,
                to);

        BigDecimal total = BigDecimal.ZERO;
        for (EmployeeOt ot : ots) {
            if (ot != null && ot.getOtHours() != null) {
                total = total.add(ot.getOtHours());
            }
        }

        try (InputStream in = new ClassPathResource("templates/word/template_overtime.docx").getInputStream();
             XWPFDocument doc = new XWPFDocument(in);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            replaceAll(doc, "{{otReportTime}}", formatMonth(ym));
            replaceAll(doc, "{{otTotalHour}}", formatHours(total));

            if (!doc.getTables().isEmpty()) {
                fillOtTable(doc.getTables().get(0), ots);
            }

            doc.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new AppException(getMessage(SystemMessage.WRITE_FILE_ERROR));
        }
    }

    private void normalizeCreateStatus(EmployeeOtDto request) {
        if (request.getStatus() == null) {
            request.setStatus(EmployeeOtStatusEnum.DRAFT);
        }
    }

    private HashMap<String, String> validateCreateStatus(EmployeeOtDto request) {
        HashMap<String, String> errors = new HashMap<>();
        EmployeeOtStatusEnum s = request.getStatus();
        if (s != EmployeeOtStatusEnum.DRAFT && s != EmployeeOtStatusEnum.SUBMITTED) {
            errors.put("status", getMessage(SystemMessage.BAD_REQUEST));
        }
        return errors;
    }

    private HashMap<String, String> validateUpdateTransition(EmployeeOtDto request, EmployeeOt entity) {
        HashMap<String, String> errors = new HashMap<>();
        EmployeeOtStatusEnum cur = entity.getStatus();
        EmployeeOtStatusEnum next = request.getStatus() != null ? request.getStatus() : cur;
        if (cur == EmployeeOtStatusEnum.APPROVED) {
            errors.put("status", getMessage(SystemMessage.BAD_REQUEST));
            return errors;
        }
        if (cur == EmployeeOtStatusEnum.SUBMITTED) {
            if (next != EmployeeOtStatusEnum.APPROVED && next != EmployeeOtStatusEnum.REJECTED) {
                errors.put("status", getMessage(SystemMessage.BAD_REQUEST));
            }
            if (hasCoreFieldChanges(request, entity)) {
                errors.put("coreFields", getMessage(SystemMessage.BAD_REQUEST));
            }
            UUID approverId = resolveCurrentUserId();
            if (approverId == null) {
                errors.put("status", getMessage(SystemMessage.UNAUTHORIZED));
            }
            return errors;
        }
        if (!isAllowedDraftRejectedTransition(cur, next)) {
            errors.put("status", getMessage(SystemMessage.BAD_REQUEST));
        }
        return errors;
    }

    private boolean isAllowedDraftRejectedTransition(EmployeeOtStatusEnum cur, EmployeeOtStatusEnum next) {
        if (cur == EmployeeOtStatusEnum.DRAFT) {
            return next == EmployeeOtStatusEnum.DRAFT || next == EmployeeOtStatusEnum.SUBMITTED;
        }
        if (cur == EmployeeOtStatusEnum.REJECTED) {
            return next == EmployeeOtStatusEnum.REJECTED
                    || next == EmployeeOtStatusEnum.DRAFT
                    || next == EmployeeOtStatusEnum.SUBMITTED;
        }
        return true;
    }

    private void applyUpdate(EmployeeOtDto request, EmployeeOt entity) {
        EmployeeOtStatusEnum cur = entity.getStatus();
        if (cur == EmployeeOtStatusEnum.SUBMITTED) {
            entity.setStatus(request.getStatus());
            entity.setApprovedBy(resolveCurrentUserId());
            entity.setApprovedDate(new Date());
            return;
        }
        BeanUtils.copyProperties(
                request, entity, "user", "project", "approver", "approvedBy", "approvedDate","id");
        if (request.getStatus() == EmployeeOtStatusEnum.DRAFT
                || request.getStatus() == EmployeeOtStatusEnum.SUBMITTED) {
            entity.setApprovedBy(null);
            entity.setApprovedDate(null);
        }
    }

    private boolean hasCoreFieldChanges(EmployeeOtDto request, EmployeeOt entity) {
        if (!Objects.equals(request.getUserId(), entity.getUserId())) {
            return true;
        }
        if (!Objects.equals(request.getProjectId(), entity.getProjectId())) {
            return true;
        }
        if (!sameInstant(request.getOtDate(), entity.getOtDate())) {
            return true;
        }
        if (!sameInstant(request.getStartTime(), entity.getStartTime())) {
            return true;
        }
        if (!sameInstant(request.getEndTime(), entity.getEndTime())) {
            return true;
        }
        if (request.getOtHours() == null && entity.getOtHours() != null) {
            return true;
        }
        if (request.getOtHours() != null && entity.getOtHours() == null) {
            return true;
        }
        if (request.getOtHours() != null
                && entity.getOtHours() != null
                && request.getOtHours().compareTo(entity.getOtHours()) != 0) {
            return true;
        }
        if (request.getOtType() != entity.getOtType()) {
            return true;
        }
        return !Objects.equals(trimToNull(request.getReason()), trimToNull(entity.getReason()));
    }

    private static String trimToNull(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        return s.trim();
    }

    private static boolean sameInstant(Date a, Date b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.getTime() == b.getTime();
    }

    private UUID resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName()).map(User::getId).orElse(null);
    }

    private HashMap<String, String> validateBusiness(EmployeeOtDto request) {
        HashMap<String, String> errors = new HashMap<>();
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null || Boolean.TRUE.equals(user.getVoided())) {
                errors.put("userId", getMessage(SystemMessage.NOT_FOUND, "user"));
            }
        }
        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId()).orElse(null);
            if (project == null || Boolean.TRUE.equals(project.getVoided())) {
                errors.put("projectId", getMessage(SystemMessage.NOT_FOUND, "project"));
            }
        }
        if (request.getStartTime() != null
                && request.getEndTime() != null
                && request.getStartTime().after(request.getEndTime())) {
            errors.put("endTime", getMessage(SystemMessage.BAD_REQUEST));
        }
        if (request.getOtHours() != null && request.getOtHours().signum() < 0) {
            errors.put("otHours", getMessage(SystemMessage.BAD_REQUEST));
        }
        return errors;
    }

    private YearMonth parseYearMonthOrThrow(String raw) {
        if (!StringUtils.hasText(raw)) {
            throw new AppException(getMessage(SystemMessage.BAD_REQUEST));
        }
        try {
            return YearMonth.parse(raw.trim());
        } catch (Exception ex) {
            throw new AppException(getMessage(SystemMessage.BAD_REQUEST));
        }
    }

    private static Date toDate(java.time.LocalDateTime ldt) {
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    private static String formatMonth(YearMonth ym) {
        if (ym == null) {
            return "";
        }
        return String.format("%02d/%d", ym.getMonthValue(), ym.getYear());
    }

    private static String formatHours(BigDecimal hours) {
        BigDecimal v = hours != null ? hours : BigDecimal.ZERO;
        DecimalFormat df = new DecimalFormat("#,##0.##");
        return df.format(v);
    }

    private static void fillOtTable(XWPFTable table, List<EmployeeOt> ots) {
        if (table == null) {
            return;
        }
        // expected: header row (0), detail row template (1), total row (last)
        if (table.getNumberOfRows() < 2) {
            return;
        }
        int detailRowIndex = 1;
        XWPFTableRow template = table.getRow(detailRowIndex);
        int totalRowIndex = table.getNumberOfRows() - 1;
        XWPFTableRow totalRow = table.getRow(totalRowIndex);

        // clear existing extra detail rows between template and total row
        while (table.getNumberOfRows() > 2) {
            table.removeRow(2);
        }

        if (ots == null || ots.isEmpty()) {
            // keep template row empty
            writeDetailRow(template, 1, null);
            return;
        }

        for (int i = 0; i < ots.size(); i++) {
            EmployeeOt ot = ots.get(i);
            XWPFTableRow row = (i == 0) ? template : cloneRow(table, template, detailRowIndex + i);
            writeDetailRow(row, i + 1, ot);
        }

        // ensure total row stays at the end
        if (table.getRow(table.getNumberOfRows() - 1) != totalRow) {
            // best effort: leave as is if POI reorders unexpectedly
        }
    }

    private static XWPFTableRow cloneRow(XWPFTable table, XWPFTableRow source, int insertAt) {
        XWPFTableRow newRow = table.insertNewTableRow(insertAt);
        newRow.getCtRow().setTrPr(source.getCtRow().getTrPr());
        int cells = source.getTableCells().size();
        for (int i = 0; i < cells; i++) {
            XWPFTableCell srcCell = source.getCell(i);
            XWPFTableCell cell = newRow.addNewTableCell();
            cell.getCTTc().setTcPr(srcCell.getCTTc().getTcPr());
            // copy paragraph properties
            if (!srcCell.getParagraphs().isEmpty()) {
                XWPFParagraph p = cell.getParagraphs().get(0);
                p.getCTP().setPPr(srcCell.getParagraphs().get(0).getCTP().getPPr());
            }
        }
        return newRow;
    }

    private static void writeDetailRow(XWPFTableRow row, int stt, EmployeeOt ot) {
        if (row == null) {
            return;
        }
        List<XWPFTableCell> cells = row.getTableCells();
        if (cells.size() < 7) {
            return;
        }
        SimpleDateFormat dfDate = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat dfTime = new SimpleDateFormat("HH:mm");

        String user = ot != null && ot.getUser() != null
                ? (Objects.toString(ot.getUser().getUsername(), "") + " — " + Objects.toString(ot.getUser().getFullName(), ""))
                : "";
        String project = ot != null && ot.getProject() != null
                ? (Objects.toString(ot.getProject().getCode(), "") + " — " + Objects.toString(ot.getProject().getName(), ""))
                : "";
        String reason = ot != null ? Objects.toString(ot.getReason(), "") : "";
        String day = (ot != null && ot.getOtDate() != null) ? dfDate.format(ot.getOtDate()) : "";
        String time = "";
        if (ot != null && ot.getStartTime() != null && ot.getEndTime() != null) {
            time = dfTime.format(ot.getStartTime()) + " - " + dfTime.format(ot.getEndTime());
        } else if (ot != null && ot.getStartTime() != null) {
            time = dfTime.format(ot.getStartTime());
        }
        String hours = ot != null && ot.getOtHours() != null ? formatHours(ot.getOtHours()) : "";

        setCellText(cells.get(0), String.valueOf(stt));
        setCellText(cells.get(1), user);
        setCellText(cells.get(2), project);
        setCellText(cells.get(3), reason);
        setCellText(cells.get(4), day);
        setCellText(cells.get(5), time);
        setCellText(cells.get(6), hours);
    }

    private static void setCellText(XWPFTableCell cell, String text) {
        if (cell == null) {
            return;
        }
        cell.removeParagraph(0);
        XWPFParagraph p = cell.addParagraph();
        XWPFRun r = p.createRun();
        r.setFontFamily(FONT_TIMES_NEW_ROMAN);
        r.setText(text != null ? text : "");
    }

    private static void replaceAll(XWPFDocument doc, String from, String to) {
        if (doc == null || from == null || to == null) {
            return;
        }
        for (XWPFParagraph p : doc.getParagraphs()) {
            replaceInParagraph(p, from, to);
        }
        for (XWPFTable table : doc.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph p : cell.getParagraphs()) {
                        replaceInParagraph(p, from, to);
                    }
                }
            }
        }
    }

    private static void replaceInParagraph(XWPFParagraph paragraph, String from, String to) {
        if (paragraph == null) {
            return;
        }
        for (XWPFRun run : paragraph.getRuns()) {
            String text = run.getText(0);
            if (text == null || !text.contains(from)) {
                continue;
            }
            run.setFontFamily(FONT_TIMES_NEW_ROMAN);
            run.setText(text.replace(from, to), 0);
        }
    }
}
