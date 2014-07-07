package odin.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import odin.domain.Availability;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReader {
	public static void main(String[] args) {
		try {
			FileInputStream file = new FileInputStream(
					new File("Capacity.xlsx"));

			// Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(file);

			// Get first/desired sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(0);

			List<String> columns = new ArrayList<String>();
			int rowCount = 0;

			// Iterate through each rows one by one
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				String userName = "";
				Hashtable<String, Integer> kv = new Hashtable<String, Integer>();
				int cellNumber = 0;
				// For each row, iterate through all the columns
				Iterator<Cell> cellIterator = row.cellIterator();

				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					// Check the cell type and format accordingly
					switch (cell.getCellType()) {
					case Cell.CELL_TYPE_NUMERIC:
						kv.put(columns.get(cellNumber),
								new Integer((int)cell.getNumericCellValue()));
						System.out.print(cell.getNumericCellValue() + " nn  ");
						break;
					case Cell.CELL_TYPE_STRING:
						if (rowCount == 0) {
							columns.add(cellNumber, cell.getStringCellValue());
						} else {
							if (cellNumber == 0)
								userName = cell.getStringCellValue();
							
						}
						System.out.print(cell.getStringCellValue() + " | ");
						break;
					}
					cellNumber++;
				}
				if (rowCount != 0) {
					for (String key : kv.keySet()) {
						Availability.setAvailability(key, userName,
								kv.get(key).intValue());
					}
				}
				System.out.println("");
				rowCount++;
			}
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}