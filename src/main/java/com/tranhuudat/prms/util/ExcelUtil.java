package com.tranhuudat.prms.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class ExcelUtil {

    /**
     * Đọc workbook từ stream (xls/xlsx). Caller nên đóng workbook sau khi dùng (try-with-resources).
     */
    public static Workbook loadWorkbook(InputStream inputStream) throws IOException {
        return WorkbookFactory.create(inputStream);
    }

    /**
     * Ghi workbook ra mảng byte (dùng cho tải file / HTTP response).
     */
    public static byte[] workbookToBytes(Workbook workbook) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    public static Cell getOrCreateCell(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            cell = row.createCell(columnIndex);
        }
        return cell;
    }

    public static void setCellText(Row row, int columnIndex, String text) {
        Cell cell = getOrCreateCell(row, columnIndex);
        cell.setCellValue(text != null ? text : "");
    }

    /**
     * Sao chép style từ một hàng mẫu (ví dụ hàng header) sang ô tương ứng trên hàng đích.
     */
    public static void copyCellStyleFromTemplateRow(Row templateRow, Row targetRow, int columnIndex) {
        if (templateRow == null || targetRow == null) {
            return;
        }
        Cell src = templateRow.getCell(columnIndex);
        Cell dst = getOrCreateCell(targetRow, columnIndex);
        if (src != null && src.getCellStyle() != null) {
            dst.setCellStyle(src.getCellStyle());
        }
    }

    /**
     * Style cho dòng dữ liệu: giữ font, căn lề, viền từ ô header mẫu nhưng không tô nền (chỉ border).
     * Nên tạo một lần theo từng cột và tái sử dụng cho mọi dòng để tránh vượt giới hạn style của Excel.
     */
    public static CellStyle createBodyStyleWithoutFill(Workbook workbook, Cell templateHeaderCell) {
        CellStyle style = workbook.createCellStyle();
        if (templateHeaderCell != null && templateHeaderCell.getCellStyle() != null) {
            style.cloneStyleFrom(templateHeaderCell.getCellStyle());
        } else {
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
        }
        style.setFillPattern(FillPatternType.NO_FILL);
        return style;
    }

    public static void autoSizeColumns(Sheet sheet, int firstCol, int lastColInclusive) {
        if (sheet == null) {
            return;
        }
        for (int i = firstCol; i <= lastColInclusive; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    public static boolean isRowEmpty(Row row, int startCol, int endCol){
        if(row == null){
            return true;
        }
        DataFormatter dataFormatter = new DataFormatter();
        for(int i = startCol; i <= endCol; i++){
            Cell cell = row.getCell(i);
            if(cell !=null && org.springframework.util.StringUtils.hasText(dataFormatter.formatCellValue(cell))){
                return false;
            }
        }
        return true;
    }

    public static String getCellValue(Row row, int columnIndex){
        if(row == null){return "";}
        DataFormatter dataFormatter = new DataFormatter();
        return dataFormatter.formatCellValue(row.getCell(columnIndex)).trim();
    }

    public static Integer getCellValueInteger(Row row, int columnIndex){
        if(row == null){return null;}
        Cell cell = row.getCell(columnIndex);
        DataFormatter dataFormatter = new DataFormatter();
        CellType cellType = cell.getCellType();
        try {
            if (cellType == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else {
                String raw = dataFormatter.formatCellValue(cell).trim();
                String normalized = raw.replace(",", "");
                return Integer.parseInt(normalized);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static Long getCellValueLong(Row row, int columnIndex){
        if(row == null){return null;}
        Cell cell = row.getCell(columnIndex);
        DataFormatter dataFormatter = new DataFormatter();
        CellType cellType = cell.getCellType();
        try {
            if (cellType == CellType.NUMERIC) {
                return (long) cell.getNumericCellValue();
            } else {
                String raw = dataFormatter.formatCellValue(cell).trim();
                String normalized = raw.replace(",", "");
                return Long.parseLong(normalized);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static BigDecimal getCellValueBigDecimal(Row row, int columnIndex){
        if(row == null){return null;}
        Cell cell = row.getCell(columnIndex);
        DataFormatter dataFormatter = new DataFormatter();
        CellType cellType = cell.getCellType();
        try {
            if (cellType == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue()) ;
            } else {
                String raw = dataFormatter.formatCellValue(cell).trim();
                String normalized = raw.replace(",", "");
                return BigDecimal.valueOf(Double.parseDouble(normalized));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static Date getCellValueDate(Row row, int columnIndex, String format){
        if(row == null){return null;}
        Cell cell = row.getCell(columnIndex);
        DataFormatter dataFormatter = new DataFormatter();
        String value = dataFormatter.formatCellValue(cell).trim();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        if(!org.springframework.util.StringUtils.hasText(value)){
            return null;
        }else{
            try{
                return sdf.parse(value);
            }catch (Exception e){
                return null;
            }
        }
    }

    public static String getExcelColumnName(int columnIndex) {
        StringBuilder columnName = new StringBuilder();
        while (columnIndex >= 0) {
            int remainder = columnIndex % 26;
            columnName.insert(0, (char)(remainder + 'A'));
            columnIndex = (columnIndex / 26) - 1;
        }
        return columnName.toString();
    }

    public static CellStyle createBaseStyle(Workbook workbook, HorizontalAlignment alignment) {
        CellStyle style = workbook.createCellStyle();
        style.setFont(createBaseFont(workbook));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(alignment);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    public static CellStyle createBaseStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFont(createBaseFont(workbook));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    public static Font createBaseFont(Workbook workbook){
        return createBaseFont(workbook,14);
    }

    public static Font createBaseFont(Workbook workbook, int size){
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) size);
        font.setFontName("Times New Roman");
        return font;
    }
}
