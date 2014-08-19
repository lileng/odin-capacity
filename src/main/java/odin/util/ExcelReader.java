/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package odin.util;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
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
		EnvironmentUtil.printClassPath();
		EnvironmentUtil.printEnvMap();
		try {
			URL url = ExcelReader.class.getResource("/Capacity.xlsx");
			FileInputStream file = new FileInputStream(
					new File(url.toURI()));
			

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
							if (cellNumber == 1)
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