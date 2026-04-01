package AgroProtect.services;

import AgroProtect.entities.AgriculturalCredit;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    public ByteArrayOutputStream generateCreditPdf(AgriculturalCredit credit) throws DocumentException {

        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);

        document.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Agricultural Credit Summary", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Table with details
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        // Helper method to add a row
        addCell(table, "Credit ID:");
        addCell(table, credit.getIdAgriculturalCredit().toString());

        addCell(table, "Approved Amount:");
        addCell(table, credit.getApprovedAmount() + " TND");

        addCell(table, "Duration (Months):");
        addCell(table, String.valueOf(credit.getDurationMonths()));

        addCell(table, "Interest Rate:");
        addCell(table, credit.getInterestRate() * 100 + "%");

        addCell(table, "Total Repayment:");
        addCell(table, credit.getTotalRepaymentAmount() + " TND");

        addCell(table, "Disbursement Date:");
        addCell(table, credit.getDisbursementDate().toString());

        addCell(table, "Maturity Date:");
        addCell(table, credit.getMaturityDate().toString());

        addCell(table, "Status:");
        addCell(table, credit.getStatus().name());

        document.add(table);

        document.close();
        return baos;
    }

    private void addCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setPadding(5);
        table.addCell(cell);
    }
}