/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eca.data.file;

import eca.data.DataFileExtension;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.Assert;
import weka.core.Attribute;
import weka.core.Instances;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Implements saving {@link Instances} into XLS file.
 *
 * @author Roman Batygin
 */
public class XLSSaver {

    private static final short FONT_SIZE = 12;

    private File file;
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";

    /**
     * Sets file object.
     *
     * @param file {@link File} object
     */
    public void setFile(File file) {
        Assert.notNull(file, "File is not specified!");
        if (!file.getName().endsWith(DataFileExtension.XLS.getExtension()) &&
                !file.getName().endsWith(DataFileExtension.XLSX.getExtension())) {
            throw new IllegalArgumentException("Unexpected file extension!");
        }
        this.file = file;
    }

    /**
     * Returns date format.
     *
     * @return date format
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * Sets date format.
     *
     * @param dateFormat date format
     */
    public void setDateFormat(String dateFormat) {
        Assert.notNull(dateFormat, "Date format is not specified!");
        this.dateFormat = dateFormat;
    }

    /**
     * Writes data into xls file.
     *
     * @param data {@link Instances} object
     * @throws IOException
     */
    public void write(Instances data) throws IOException {
        Assert.notNull(data, "Data is not specified!");
        try (FileOutputStream stream = new FileOutputStream(file); Workbook book = createWorkbook(file)) {
            Font font = book.createFont();
            font.setBold(true);
            font.setFontHeightInPoints(FONT_SIZE);
            CellStyle style = book.createCellStyle();
            CellStyle dateStyle = book.createCellStyle();
            short date = book.createDataFormat().getFormat(dateFormat);
            dateStyle.setDataFormat(date);
            style.setFont(font);
            Sheet sheet = book.createSheet(data.relationName());
            Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());

            for (int i = 0; i < data.numAttributes(); i++) {
                Cell cell = row.createCell(i);
                cell.setCellStyle(style);
                cell.setCellValue(data.attribute(i).name());
            }

            for (int i = 0; i < data.numInstances(); i++) {
                row = sheet.createRow(sheet.getPhysicalNumberOfRows());
                for (int j = 0; j < data.numAttributes(); j++) {
                    Cell cell = row.createCell(j);
                    Attribute a = data.attribute(j);
                    if (!data.instance(i).isMissing(a)) {
                        if (a.isDate()) {
                            cell.setCellStyle(dateStyle);
                            cell.setCellValue(new Date((long) data.instance(i).value(a)));
                        } else if (a.isNumeric()) {
                            cell.setCellValue(data.instance(i).value(a));
                        } else {
                            cell.setCellValue(data.instance(i).stringValue(a));
                        }
                    }
                }
            }

            book.write(stream);
        }
    }

    private Workbook createWorkbook(File file) {
        return file.getName().endsWith(DataFileExtension.XLS.getExtension()) ? new HSSFWorkbook() : new XSSFWorkbook();
    }

}
