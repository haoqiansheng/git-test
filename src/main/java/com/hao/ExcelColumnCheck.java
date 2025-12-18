package com.hao;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 读取a.xlsx的D列值，校验是否存在于b.xlsx的D列中；
 * 若不存在，将a.xlsx对应行的N列赋值为“不存在”并保存
 */
public class ExcelColumnCheck {

    // 列索引常量（便于维护）
    private static final int CHECK_COLUMN = 3;    // D列（0开始：A=0,B=1,C=2,D=3）
    private static final int RESULT_COLUMN = 13;  // N列（0开始：N=13）
    private static final String NOT_EXIST_VALUE = "不存在"; // N列赋值内容

    // 读取指定Excel的所有Sheet的D列值，存入Set（用于快速查询）
    private static Set<String> readColumnDValues(String excelPath) throws IOException {
        Set<String> columnDValues = new HashSet<>();
        try (FileInputStream fis = new FileInputStream(excelPath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // 遍历所有Sheet（如需指定Sheet，可改为workbook.getSheetAt(0)或getSheet("Sheet1")）
            for (Sheet sheet : workbook) {
                Iterator<Row> rowIterator = sheet.iterator();
                // 跳过表头（如果D列第一行是表头，取消下面注释）
                // if (rowIterator.hasNext()) rowIterator.next();

                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    // 获取D列，空单元格转为空白
                    Cell cell = row.getCell(CHECK_COLUMN, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    // 统一转为字符串，避免数字/文本格式问题
                    String cellValue = getCellStringValue(cell);
                    if (!cellValue.isBlank()) { // 跳过空值
                        columnDValues.add(cellValue.trim());
                    }
                }
            }
        }
        return columnDValues;
    }

    // 统一处理单元格值（兼容不同类型：字符串、数字、布尔等）
    private static String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // 处理数字（避免小数位，如123.0转为123）
                if (Math.floor(cell.getNumericCellValue()) == cell.getNumericCellValue()) {
                    return String.valueOf((long) cell.getNumericCellValue());
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return "";
            default:
                return cell.toString().trim();
        }
    }

    // 核心逻辑：校验a.xlsx的D列值，不存在则给N列赋值并保存
    public static void main(String[] args) {
        // 替换为实际的文件路径（绝对路径/相对路径均可）
        String aExcelPath = "C:\\Users\\jrjc\\Desktop\\备品备件\\无规则备件统计2025.12.17.xlsx";
        String bExcelPath = "C:\\Users\\jrjc\\Desktop\\备品备件\\未录入备件统计.xlsx";

        try {
            // 1. 读取b.xlsx的所有D列值，存入Set（查询效率O(1)）
            Set<String> bColumnDValues = readColumnDValues(bExcelPath);
            System.out.println("成功读取b.xlsx的D列值数量：" + bColumnDValues.size());

            // 2. 读取a.xlsx并处理
            FileInputStream fis = new FileInputStream(aExcelPath);
            Workbook workbook = new XSSFWorkbook(fis);

            for (Sheet sheet : workbook) {
                Iterator<Row> rowIterator = sheet.iterator();
                // 跳过表头（如果需要，取消注释）
                // if (rowIterator.hasNext()) rowIterator.next();

                int rowNum = 0;
                while (rowIterator.hasNext()) {
                    rowNum++;
                    Row row = rowIterator.next();
                    // 获取当前行D列值
                    Cell dCell = row.getCell(CHECK_COLUMN, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String aValue = getCellStringValue(dCell).trim();

                    // 跳过空值
                    if (aValue.isBlank()) {
                        continue;
                    }

                    // 3. 校验：不存在则给N列赋值“不存在”
                    if (!bColumnDValues.contains(aValue)) {
                        System.out.printf("a.xlsx第%d行D列值【%s】不存在于b.xlsx中，已为N列赋值“不存在”%n", rowNum, aValue);
                        // 获取/创建N列单元格
                        Cell nCell = row.getCell(RESULT_COLUMN, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        // 设置单元格值为“不存在”（强制字符串格式）
                        nCell.setCellType(CellType.STRING);
                        nCell.setCellValue(NOT_EXIST_VALUE);
                    }
                }
            }

            // 4. 保存修改后的a.xlsx（覆盖原文件，如需备份可修改输出路径）
            try (FileOutputStream fos = new FileOutputStream(aExcelPath)) {
                workbook.write(fos);
                System.out.println("已成功保存修改后的a.xlsx文件");
            }

            // 关闭资源
            workbook.close();
            fis.close();

        } catch (IOException e) {
            System.err.println("文件操作异常：" + e.getMessage());
            e.printStackTrace();
        }
    }
}