package com.Admin.insurance.GUI;

import com.Admin.insurance.BUS.BUS_Warranty;
import com.Admin.insurance.DTO.DTO_Insurance;
import com.Admin.insurance.DTO.DTO_InsuranceDetails;
import com.ComponentandDatabase.Components.CustomDialog;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InsurancePDFExporter {
    private BUS_Warranty busWarranty;
    
    public InsurancePDFExporter() {
        busWarranty = new BUS_Warranty();
    }
    
    public void exportInsurancePDF(String insuranceNo) {
        try {
            // Get insurance details
            DTO_Insurance insurance = busWarranty.getInsuranceByNo(insuranceNo);
            if (insurance == null) {
                CustomDialog.showError("Insurance not found: " + insuranceNo);
                return;
            }
            
            // Get insurance details (products)
            List<DTO_InsuranceDetails> insuranceDetails = busWarranty.getInsuranceDetailsByNo(insuranceNo);
            
            // Show file chooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Insurance PDF");
            fileChooser.setSelectedFile(new java.io.File("Insurance_" + insuranceNo + ".pdf"));
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));
            
            int userSelection = fileChooser.showSaveDialog(null);
            
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToSave = fileChooser.getSelectedFile();
                String filePath = fileToSave.getAbsolutePath();
                
                if (!filePath.toLowerCase().endsWith(".pdf")) {
                    filePath += ".pdf";
                }
                
                // Create PDF
                createInsurancePDF(insurance, insuranceDetails, filePath);
                CustomDialog.showSuccess("Insurance PDF exported successfully!");
            }
            
        } catch (Exception e) {
            CustomDialog.showError("Failed to export PDF: " + e.getMessage());
        }
    }
    
    private void createInsurancePDF(DTO_Insurance insurance, List<DTO_InsuranceDetails> insuranceDetails, String filePath) {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            
            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Paragraph title = new Paragraph("INSURANCE CERTIFICATE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Insurance Information
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            
            // Insurance Details Table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(20);
            
            // Insurance No
            addInfoRow(infoTable, "Insurance No:", insurance.getInsuranceNo(), headerFont, contentFont);
            addInfoRow(infoTable, "Invoice No:", insurance.getInvoiceNo(), headerFont, contentFont);
            addInfoRow(infoTable, "Customer ID:", insurance.getCustomerId(), headerFont, contentFont);
            addInfoRow(infoTable, "Admin ID:", insurance.getAdminId(), headerFont, contentFont);
            addInfoRow(infoTable, "Start Date:", insurance.getStartDateInsurance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), headerFont, contentFont);
            addInfoRow(infoTable, "End Date:", insurance.getEndDateInsurance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), headerFont, contentFont);
            addInfoRow(infoTable, "Description:", insurance.getDescribleCustomer(), headerFont, contentFont);
            
            document.add(infoTable);
            
            // Products Table
            if (insuranceDetails != null && !insuranceDetails.isEmpty()) {
                Paragraph productsTitle = new Paragraph("INSURED PRODUCTS", headerFont);
                productsTitle.setSpacingBefore(20);
                productsTitle.setSpacingAfter(10);
                document.add(productsTitle);
                
                PdfPTable productsTable = new PdfPTable(4);
                productsTable.setWidthPercentage(100);
                productsTable.setSpacingAfter(20);
                
                // Headers
                String[] headers = {"Product ID", "Description", "Insurance Date", "Status"};
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    productsTable.addCell(cell);
                }
                
                // Data rows
                for (DTO_InsuranceDetails detail : insuranceDetails) {
                    productsTable.addCell(new PdfPCell(new Phrase(detail.getProductId(), contentFont)));
                    productsTable.addCell(new PdfPCell(new Phrase(detail.getDescription(), contentFont)));
                    productsTable.addCell(new PdfPCell(new Phrase(detail.getDateInsurance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), contentFont)));
                    productsTable.addCell(new PdfPCell(new Phrase("Active", contentFont)));
                }
                
                document.add(productsTable);
            }
            
            // Terms and Conditions
            Paragraph termsTitle = new Paragraph("TERMS AND CONDITIONS", headerFont);
            termsTitle.setSpacingBefore(20);
            termsTitle.setSpacingAfter(10);
            document.add(termsTitle);
            
            String termsText = "1. This insurance certificate covers manufacturing defects only.\n" +
                             "2. Physical damage or liquid damage is not covered.\n" +
                             "3. Valid ID must be presented for warranty claims.\n" +
                             "4. This insurance is non-transferable.\n" +
                             "5. Warranty period starts from the date of purchase.\n" +
                             "6. Any modifications to the product will void this insurance.\n" +
                             "7. This certificate must be presented for any warranty service.";
            
            Paragraph terms = new Paragraph(termsText, contentFont);
            terms.setSpacingAfter(20);
            document.add(terms);
            
            // Footer
            Paragraph footer = new Paragraph("Generated on: " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), contentFont);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);
            
            document.close();
            
        } catch (Exception e) {
            throw new RuntimeException("Error creating PDF: " + e.getMessage(), e);
        }
    }
    
    private void addInfoRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        labelCell.setPadding(5);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "N/A", valueFont));
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }
}
