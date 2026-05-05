package piJava.api;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import piJava.entities.Classe;
import piJava.entities.Matiere;

import java.io.FileOutputStream;
import java.util.List;
import java.util.stream.Stream;

public class ExportApi {

    public static void exportClasseToPdf(Classe classe, List<Matiere> matieres, String filePath) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // Couleurs
        BaseColor mainBlue = new BaseColor(31, 41, 55); // #1f2937
        BaseColor secondaryBlue = new BaseColor(59, 130, 246); // #3b82f6
        BaseColor lightGray = new BaseColor(249, 250, 251); // #f9fafb

        // Polices
        com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, mainBlue);
        com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        com.itextpdf.text.Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11, mainBlue);
        com.itextpdf.text.Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, mainBlue);

        // Titre
        Paragraph title = new Paragraph("BULLETIN RÉCAPITULATIF", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Ligne de séparation
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell lineCell = new PdfPCell(new Phrase(" "));
        lineCell.setBorder(Rectangle.BOTTOM);
        lineCell.setBorderColor(secondaryBlue);
        lineCell.setBorderWidth(2f);
        line.addCell(lineCell);
        document.add(line);
        document.add(new Paragraph(" "));

        // Informations de la classe
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(20);

        addInfoCell(infoTable, "Classe :", classe.getNom(), labelFont, normalFont);
        addInfoCell(infoTable, "Niveau :", classe.getNiveau(), labelFont, normalFont);
        addInfoCell(infoTable, "Année Universitaire :", classe.getAnneeUniversitaire(), labelFont, normalFont);
        addInfoCell(infoTable, "Description :", (classe.getDescription() != null ? classe.getDescription() : "N/A"), labelFont, normalFont);

        document.add(infoTable);

        // Table des matières
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        
        // Headers
        String[] headers = {"Matière", "Coefficient", "Charge Horaire", "Complexité"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(mainBlue);
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorderColor(BaseColor.GRAY);
            table.addCell(cell);
        }

        // Données
        boolean alternate = false;
        for (Matiere m : matieres) {
            BaseColor bgColor = alternate ? lightGray : BaseColor.WHITE;
            
            table.addCell(createStyledCell(m.getNom(), normalFont, bgColor, Element.ALIGN_LEFT));
            table.addCell(createStyledCell(String.valueOf(m.getCoefficient()), normalFont, bgColor, Element.ALIGN_CENTER));
            table.addCell(createStyledCell(m.getChargehoraire() + " h", normalFont, bgColor, Element.ALIGN_CENTER));
            table.addCell(createStyledCell(m.getScorecomplexite() + "/10", normalFont, bgColor, Element.ALIGN_CENTER));
            
            alternate = !alternate;
        }

        document.add(table);

        // Footer avec date
        Paragraph footer = new Paragraph("\nDocument généré le : " + new java.util.Date().toString(), 
                FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY));
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);

        document.close();
    }

    private static void addInfoCell(PdfPTable table, String label, String value, com.itextpdf.text.Font labelFont, com.itextpdf.text.Font valueFont) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, labelFont));
        cellLabel.setBorder(Rectangle.NO_BORDER);
        cellLabel.setPadding(5);
        table.addCell(cellLabel);

        PdfPCell cellValue = new PdfPCell(new Phrase(value, valueFont));
        cellValue.setBorder(Rectangle.NO_BORDER);
        cellValue.setPadding(5);
        table.addCell(cellValue);
    }

    private static PdfPCell createStyledCell(String text, com.itextpdf.text.Font font, BaseColor bgColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        cell.setHorizontalAlignment(alignment);
        cell.setBorderColor(new BaseColor(229, 231, 235)); // Gray 200
        return cell;
    }

    public static void exportMatieresToExcel(List<Matiere> matieres, String filePath) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Liste des Matières");

        // Couleurs personnalisées (RGB)
        byte[] mainBlue = new byte[]{(byte) 31, (byte) 41, (byte) 55};     // #1f2937
        byte[] secondaryBlue = new byte[]{(byte) 59, (byte) 130, (byte) 246}; // #3b82f6
        byte[] lightGray = new byte[]{(byte) 249, (byte) 250, (byte) 251};  // #f9fafb
        byte[] white = new byte[]{(byte) 255, (byte) 255, (byte) 255};

        // --- STYLES ---

        // Style Titre
        XSSFCellStyle titleStyle = (XSSFCellStyle) workbook.createCellStyle();
        XSSFFont titleFont = (XSSFFont) workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 18);
        titleFont.setColor(new XSSFColor(white, null));
        titleStyle.setFont(titleFont);
        titleStyle.setFillForegroundColor(new XSSFColor(mainBlue, null));
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Style Header
        XSSFCellStyle headerStyle = (XSSFCellStyle) workbook.createCellStyle();
        XSSFFont headerFont = (XSSFFont) workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(new XSSFColor(white, null));
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(new XSSFColor(secondaryBlue, null));
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorders(headerStyle);

        // Styles de base pour les données
        XSSFCellStyle normalCenter = createBaseStyle(workbook, false, HorizontalAlignment.CENTER);
        XSSFCellStyle normalLeft = createBaseStyle(workbook, false, HorizontalAlignment.LEFT);
        XSSFCellStyle altCenter = createBaseStyle(workbook, true, HorizontalAlignment.CENTER);
        XSSFCellStyle altLeft = createBaseStyle(workbook, true, HorizontalAlignment.LEFT);

        // --- CONSTRUCTION ---

        // Ligne de Titre (Fusionnée)
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(45);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("RÉCAPITULATIF DES MATIÈRES - ESPRIT FLOW");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        // Ligne d'En-tête
        Row header = sheet.createRow(1);
        header.setHeightInPoints(30);
        String[] columns = {"Nom de la Matière", "Coefficient", "Charge Horaire", "Complexité", "Description"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Données
        int rowNum = 2;
        boolean isAlt = false;
        for (Matiere m : matieres) {
            Row row = sheet.createRow(rowNum++);
            row.setHeightInPoints(22);
            
            CellStyle centerStyle = isAlt ? altCenter : normalCenter;
            CellStyle leftStyle = isAlt ? altLeft : normalLeft;

            addCell(row, 0, m.getNom(), leftStyle);
            addCell(row, 1, String.format("%.1f", m.getCoefficient()), centerStyle);
            addCell(row, 2, m.getChargehoraire() + " h", centerStyle);
            addCell(row, 3, m.getScorecomplexite() + "/10", centerStyle);
            addCell(row, 4, (m.getDescription() != null ? m.getDescription() : "—"), leftStyle);

            isAlt = !isAlt;
        }

        // Auto-ajustement des colonnes
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1200);
        }

        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    private static XSSFCellStyle createBaseStyle(Workbook wb, boolean isAlt, HorizontalAlignment align) {
        XSSFCellStyle style = (XSSFCellStyle) wb.createCellStyle();
        style.setAlignment(align);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorders(style);
        
        if (isAlt) {
            byte[] lightGray = new byte[]{(byte) 249, (byte) 250, (byte) 251};
            style.setFillForegroundColor(new XSSFColor(lightGray, null));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        return style;
    }

    private static void applyBorders(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
    }

    private static void addCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
