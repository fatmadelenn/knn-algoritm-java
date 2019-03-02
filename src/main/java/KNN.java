import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class KNN {
	public static final String veri = "./veri1.xlsx";
	public static final String veriTest = "./veri2.xlsx";
	public static Row row;
	public static String[][] satirMatrisTest, satirMatrisGercek;
	public static Cell cell;
	public static Sheet sheet, sheetTest;
	public static int cells, cellsTest, rows, rowsTest;
	public static int kSayisi;

	public static void main(String[] args) throws IOException, InvalidFormatException {

		Workbook workbook = WorkbookFactory.create(new File(veri));
		sheet = workbook.getSheetAt(0);
		Workbook workbook2 = WorkbookFactory.create(new File(veriTest));
		sheetTest = workbook2.getSheetAt(0);
		System.out.println("-------------------Toplam veri seti-------------------");
		rows = sheet.getPhysicalNumberOfRows();
		cells = sheet.getRow(0).getPhysicalNumberOfCells();
		satirMatrisGercek = new String[rows][cells];
		for (int r = 0; r < rows; r++) {
			row = sheet.getRow(r);
			if (row != null) {
				for (int c = 0; c < cells; c++) {
					cell = row.getCell(c);
					if (cell != null) {
						switch (cell.getCellTypeEnum()) {
						case STRING:
							satirMatrisGercek[r][c] = "" + cell.getStringCellValue();
							break;
						case NUMERIC:
							satirMatrisGercek[r][c] = "" + cell.getNumericCellValue();
							break;
						case FORMULA:
							satirMatrisGercek[r][c] = cell.getCellFormula();
							break;
						case BLANK:
							satirMatrisGercek[r][c] = " ";
							break;
						default:
							satirMatrisGercek[r][c] = "";
						}
						System.out.print("\t");
					}
					System.out.print(satirMatrisGercek[r][c]);
				}
				System.out.print("\n");
			}
		}

		System.out.println("-------------------Test için kullanılacak veri seti-------------------");
		rowsTest = sheetTest.getPhysicalNumberOfRows();
		cellsTest = sheetTest.getRow(0).getPhysicalNumberOfCells();
		satirMatrisTest = new String[rowsTest][cellsTest];
		for (int r = 0; r < rowsTest; r++) {
			row = sheetTest.getRow(r);
			if (row != null) {
				for (int c = 0; c < cellsTest; c++) {
					cell = row.getCell(c);
					if (cell != null) {
						switch (cell.getCellTypeEnum()) {
						case STRING:
							satirMatrisTest[r][c] = "" + cell.getStringCellValue();
							break;
						case NUMERIC:
							satirMatrisTest[r][c] = "" + cell.getNumericCellValue();
							break;
						case FORMULA:
							satirMatrisTest[r][c] = cell.getCellFormula();
							break;
						case BLANK:
							satirMatrisTest[r][c] = " ";
							break;
						default:
							satirMatrisTest[r][c] = "";
						}
						System.out.print("\t");
					}
					System.out.print(satirMatrisTest[r][c]);
				}
				System.out.print("\n");
			}
		}
		double[][] oklitUzaklik = oklitHesapla(satirMatrisTest, satirMatrisGercek);
		System.out.println("-------------------------------------------------");
		System.out.print("k sayısını giriniz: ");
		Scanner scan = new Scanner(System.in);
		kSayisi = scan.nextInt();
		double[] tahmin = new double[satirMatrisTest.length];
		for (int i = 0; i < oklitUzaklik.length; i++) {
			int[] indisGetir = EnKucukIndisler(oklitUzaklik[i], kSayisi);
			int eksik = eksikGetir(i);
			double ortalama = 0.0;
			for (int j = 0; j < indisGetir.length; j++) {
				ortalama += Double.parseDouble(satirMatrisGercek[indisGetir[j]][eksik]);
			}
			tahmin[i] = ortalama / kSayisi;
			YerineKoy(i, tahmin);
		}
		System.out.println("----------------------SONUÇ---------------------------");
		for (int a = 0; a < satirMatrisTest.length; a++) {
			for (int b = 0; b < satirMatrisTest[a].length; b++) {
				System.out.print(satirMatrisTest[a][b] + " ");
			}
			System.out.println();
		}
		workbook2.close();
		workbook.close();
	}

	public static double[][] oklitHesapla(String[][] eksikVeriler, String[][] gercekMatris) {
		double uzakliklar[][] = new double[eksikVeriler.length][gercekMatris.length];
		for (int i = 0; i < eksikVeriler.length; i++) {
			for (int j = 0; j < gercekMatris.length; j++) {
				uzakliklar[i][j] = uzaklik(eksikVeriler[i], gercekMatris[j]);
			}
		}
		return uzakliklar;
	}

	public static double uzaklik(String[] testData, String[] gercekData) {
		double uzaklik = 0.0;
		for (int i = 0; i < testData.length; i++) {
			if (!testData[i].equals(" ")) {
				uzaklik += Math.pow((Double.parseDouble(testData[i]) - Double.parseDouble(gercekData[i])), 2);
			}
		}
		return Math.sqrt(uzaklik);
	}

	public static void YerineKoy(int i, double[] ortalamaDizi) {
		for (int a = 0; a < satirMatrisTest.length; a++) {
			for (int b = 0; b < satirMatrisTest[a].length; b++) {
				if (satirMatrisTest[a][b].equals(" ") && i == a) {
					satirMatrisTest[a][b] = Double.toString(ortalamaDizi[i]);
				}
			}
		}
	}

	public static int eksikGetir(int indis) {
		for (int b = 0; b < satirMatrisTest[indis].length; b++) {
			if (satirMatrisTest[indis][b].equals(" ")) {
				return b;
			}
		}
		return -1;
	}

	public static int[] EnKucukIndisler(double[] uzakliklar, int k) {
		int[] dizi = new int[k];
		for (int i = 0; i < k; i++) {
			int yeniIndis = getMinValue(uzakliklar);
			dizi[i] = yeniIndis;
			uzakliklar[yeniIndis] = uzakliklar[getMaxValue(uzakliklar)] + 1;
		}
		return dizi;
	}

	public static int getMaxValue(double[] array) {
		double maxValue = array[0];
		int indis = 0;
		for (int i = 1; i < array.length; i++) {
			if (array[i] > maxValue) {
				maxValue = array[i];
				indis = i;
			}
		}
		return indis;
	}

	public static int getMinValue(double[] array) {
		double minValue = array[0];
		int indis = 0;
		for (int i = 1; i < array.length; i++) {
			if (array[i] < minValue) {
				minValue = array[i];
				indis = i;
			}
		}
		return indis;
	}
}