package tn.esprit.spring1.Services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;
import tn.esprit.spring1.entities.PartnerType;
import tn.esprit.spring1.entities.Quote;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.Date;

@Service
public class PdfService {

    public byte[] generateQuotePdf(Quote quote, double score, double success, String reason) {

        try {

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            // 🔹 Polices
            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(34, 139, 34));
            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
            Font normalFont = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.BLACK);
            Font bestFont = new Font(Font.HELVETICA, 14, Font.BOLD, Color.RED);
            Font aiTitleFont = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(0, 100, 0));

            // 🔹 Titre
            Paragraph title = new Paragraph("AGRICULTURAL PROJECT QUOTE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            Paragraph date = new Paragraph("Generated on: " + new Date(), normalFont);
            date.setAlignment(Element.ALIGN_RIGHT);
            date.setSpacingAfter(20);
            document.add(date);

            // 🔹 Tableau principal
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setWidths(new int[]{2, 3});

            addRow(table, "Project", quote.getProject().getName(), normalFont);
            addRow(table, "Partner", quote.getPartner().getName(), normalFont);
            addRow(table, "Partner Type", quote.getPartnerType().toString(), normalFont);
            addRow(table, "Proposed Price", quote.getProposedPrice() + " DT", normalFont);

            document.add(table);

            document.add(new Paragraph(" "));

            // 🔥 AI SECTION
            Paragraph aiTitle = new Paragraph("AI DECISION ANALYSIS", aiTitleFont);
            aiTitle.setSpacingBefore(15);
            aiTitle.setSpacingAfter(10);
            document.add(aiTitle);

            document.add(new Paragraph("Score: " + String.format("%.2f", score), normalFont));
            document.add(new Paragraph("Success Probability: " + String.format("%.2f", success * 100) + " %", normalFont));
            document.add(new Paragraph("Decision: " + reason, normalFont));

            document.add(new Paragraph(" "));
            document.add(new Paragraph(
                    "This decision is based on a multi-criteria scoring model combined with risk simulation.",
                    normalFont
            ));

            document.add(new Paragraph(" "));

            // 🔹 Section dynamique
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setSpacingBefore(10);
            detailsTable.setWidths(new int[]{2, 3});

            if (quote.getPartnerType() == PartnerType.EQUIPMENT_PROVIDER) {
                addRow(detailsTable, "Equipment Name", quote.getEquipmentName(), normalFont);
                addRow(detailsTable, "Quantity", String.valueOf(quote.getQuantity()), normalFont);
                addRow(detailsTable, "Delivery Days", String.valueOf(quote.getDeliveryTimeDays()), normalFont);
            }

            if (quote.getPartnerType() == PartnerType.INVESTOR) {
                addRow(detailsTable, "Investment Amount", String.valueOf(quote.getProposedInvestmentAmount()), normalFont);
                addRow(detailsTable, "Expected Return (%)", String.valueOf(quote.getExpectedReturnRate()), normalFont);
                addRow(detailsTable, "Duration (months)", String.valueOf(quote.getDurationMonths()), normalFont);
            }

            if (quote.getPartnerType() == PartnerType.OLIVE_MILL) {
                addRow(detailsTable, "Price Per Ton", String.valueOf(quote.getPricePerTon()), normalFont);
                addRow(detailsTable, "Processing Capacity", String.valueOf(quote.getProcessingCapacity()), normalFont);
                addRow(detailsTable, "Transport Included", String.valueOf(quote.getTransportIncluded()), normalFont);
            }

            document.add(detailsTable);

            document.add(new Paragraph(" "));

            // 🔹 Badge meilleure offre
            if (Boolean.TRUE.equals(quote.getIsBest())) {
                Paragraph best = new Paragraph("⭐ BEST OFFER SELECTED ⭐", bestFont);
                best.setAlignment(Element.ALIGN_CENTER);
                best.setSpacingBefore(20);
                document.add(best);
            }

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    // 🔹 Méthode utilitaire
    private void addRow(PdfPTable table, String key, String value, Font font) {

        PdfPCell cell1 = new PdfPCell(new Phrase(key, font));
        cell1.setBackgroundColor(new Color(230, 255, 230));
        cell1.setPadding(8);
        table.addCell(cell1);

        PdfPCell cell2 = new PdfPCell(new Phrase(value != null ? value : "-", font));
        cell2.setPadding(8);
        table.addCell(cell2);
    }
}