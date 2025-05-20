package com.example.scanimin.function;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.widget.Toast;
import com.example.scanimin.data.Object.Customer;
import com.example.scanimin.data.Object.Data;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import java.io.OutputStream;
import java.util.List;

public class ExcelExporter {

    public static void saveWorkbookToUri(Context context, HSSFWorkbook workbook, Uri uri) {
        try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
            if (outputStream != null) {
                workbook.write(outputStream);
                Toast.makeText(context, "Đã lưu Excel thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Không thể mở file để ghi", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi khi lưu Excel: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            try {
                workbook.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static void exportCustomersToExcel(Context context, List<Customer> customers, Uri uri) {
        try {
            HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
            HSSFSheet sheet = hssfWorkbook.createSheet("Customers");

            // Header
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Code", "Name", "Company", "Position", "Role", "Status", "Timestamp", "URL"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Data rows
            int rowIndex = 1;
            for (Customer customer : customers) {
                Row row = sheet.createRow(rowIndex++);

                Data data = customer.getData();
                row.createCell(0).setCellValue(customer.getQrcode());
                row.createCell(1).setCellValue(data.getName());
                row.createCell(2).setCellValue(data.getCompany());
                row.createCell(3).setCellValue(data.getPosition());
                row.createCell(4).setCellValue(data.getRole());
                row.createCell(5).setCellValue(customer.getStatus() ? "Đã quét" : "Chưa quét");
                row.createCell(6).setCellValue(customer.getTimestamp());
                row.createCell(7).setCellValue(customer.getUrl());
            }

            saveWorkbookToUri(context, hssfWorkbook, uri);


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi xuất Excel: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    @SuppressLint("Range")
    public static String getFileNameFromUri(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

}
