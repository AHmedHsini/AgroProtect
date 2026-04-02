package tn.esprit.spring1.Services;

import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;
import tn.esprit.spring1.entities.Quote;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;

import org.knowm.xchart.*;
import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Date;

@Service
public class PdfService {

    public byte[] generateQuotePdf(Quote quote,
                                   double score,
                                   double success,
                                   String reason,
                                   String baseUrl) throws Exception {

        score = Math.min(Math.max(score, 0), 1);
        success = Math.min(Math.max(success, 0), 1);

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        // 🎨 COLORS
        java.awt.Color blue = new java.awt.Color(41,128,185);
        java.awt.Color light = new java.awt.Color(245,247,250);

        java.awt.Color scoreColor = score > 0.7 ? new java.awt.Color(39,174,96)
                : score > 0.4 ? new java.awt.Color(243,156,18)
                : new java.awt.Color(192,57,43);

        // ================= HEADER =================
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);

        PdfPCell headerCell = new PdfPCell(new Phrase("AGROPROTECT - AI REPORT",
                new Font(Font.HELVETICA, 18, Font.BOLD, java.awt.Color.WHITE)));

        headerCell.setBackgroundColor(blue);
        headerCell.setPadding(20);
        headerCell.setBorder(Rectangle.NO_BORDER);

        header.addCell(headerCell);
        document.add(header);

        document.add(new Paragraph("Generated on: " + new Date()));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        // ================= SCORE =================
        PdfPTable scoreTable = new PdfPTable(1);
        scoreTable.setWidthPercentage(35);
        scoreTable.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell scoreCell = new PdfPCell();
        scoreCell.setPadding(25);
        scoreCell.setBackgroundColor(light);
        scoreCell.setBorder(Rectangle.NO_BORDER);

        Paragraph title = new Paragraph("FINAL SCORE",
                new Font(Font.HELVETICA, 11, Font.BOLD, java.awt.Color.GRAY));

        Paragraph value = new Paragraph(
                String.format("%.2f", score),
                new Font(Font.HELVETICA, 36, Font.BOLD, scoreColor)
        );
        value.setAlignment(Element.ALIGN_CENTER);

        scoreCell.addElement(title);
        scoreCell.addElement(value);

        scoreTable.addCell(scoreCell);
        document.add(scoreTable);

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        // ================= PROJECT =================
        document.add(new Paragraph("PROJECT INFORMATION",
                new Font(Font.HELVETICA, 12, Font.BOLD)));

        document.add(new Paragraph("Project: " + quote.getProject().getName()));
        document.add(new Paragraph("Partner: " + quote.getPartner().getName()));
        document.add(new Paragraph("Type: " + quote.getPartnerType()));

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        // ================= AI CARD =================
        PdfPTable card = new PdfPTable(1);
        card.setWidthPercentage(100);

        PdfPCell cardCell = new PdfPCell();
        cardCell.setPadding(20);
        cardCell.setBackgroundColor(light);
        cardCell.setBorder(Rectangle.NO_BORDER);

        cardCell.addElement(new Paragraph("AI INSIGHTS",
                new Font(Font.HELVETICA, 12, Font.BOLD)));

        cardCell.addElement(new Paragraph("Score: " + String.format("%.2f", score)));
        cardCell.addElement(new Paragraph("Success: " + (int)(success * 100) + "%"));
        cardCell.addElement(new Paragraph(reason));

        cardCell.addElement(new Paragraph("AI Prediction (ML): " + (int)(success * 100) + "%"));

        cardCell.addElement(new Paragraph("Decision: " +
                (success > 0.7 ? "Recommended for investment" : "Risky project")));

        cardCell.addElement(new Paragraph("Investor Selection: Based on capital, risk profile, and ML prediction"));

        card.addCell(cardCell);
        document.add(card);

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        // ================= CHARTS =================
        PdfPTable charts = new PdfPTable(2);
        charts.setWidthPercentage(100);
        charts.setSpacingBefore(15);

        Image pie = generateScoreChart(score, success);
        pie.scaleToFit(260,200);

        Image bar = generateBarChart(score, success);
        bar.scaleToFit(260,200);

        PdfPCell pieCell = new PdfPCell(pie);
        pieCell.setBorder(Rectangle.NO_BORDER);
        pieCell.setPadding(15);

        PdfPCell barCell = new PdfPCell(bar);
        barCell.setBorder(Rectangle.NO_BORDER);
        barCell.setPadding(15);

        charts.addCell(pieCell);
        charts.addCell(barCell);

        document.add(charts);

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        // ================= PROGRESS =================
        double progress = quote.getProject().getCollectedAmount() /
                quote.getProject().getFundingGoal();

        int percent = (int)(progress * 100);

        document.add(new Paragraph("Project Funding: " + percent + "%"));

        PdfPTable progressBar = new PdfPTable(1);
        progressBar.setWidthPercentage(percent);

        PdfPCell barProgress = new PdfPCell(new Phrase(""));
        barProgress.setFixedHeight(12);
        barProgress.setBackgroundColor(blue);
        barProgress.setBorder(Rectangle.NO_BORDER);

        progressBar.addCell(barProgress);
        document.add(progressBar);

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        // ================= QR =================
        String qrUrl = baseUrl + "/mfi/view/top3/" + quote.getProject().getIdProject();

        PdfPTable bottom = new PdfPTable(2);
        bottom.setWidthPercentage(100);

        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);

        left.addElement(new Paragraph("Access Full Dashboard:"));
        left.addElement(new Paragraph(qrUrl));

        Image qr = generateQRCode(qrUrl);
        qr.scaleToFit(100,100);

        PdfPCell right = new PdfPCell(qr);
        right.setBorder(Rectangle.NO_BORDER);
        right.setHorizontalAlignment(Element.ALIGN_CENTER);

        bottom.addCell(left);
        bottom.addCell(right);

        document.add(bottom);

        document.close();
        return out.toByteArray();
    }

    // ================= PIE =================
    private Image generateScoreChart(double score, double success) throws Exception {

        double risk = 1 - success;

        PieChart chart = new PieChartBuilder()
                .width(500)
                .height(350)
                .build();

        chart.addSeries("Score", score);
        chart.addSeries("Success", success);
        chart.addSeries("Risk", risk);

        chart.getStyler().setDonutThickness(0.6);
        chart.getStyler().setLegendVisible(true);
        chart.getStyler().setPlotBorderVisible(false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapEncoder.saveBitmap(chart, baos, BitmapEncoder.BitmapFormat.PNG);

        return Image.getInstance(baos.toByteArray());
    }

    // ================= BAR =================
    private Image generateBarChart(double score, double success) throws Exception {

        double risk = 1 - success;

        CategoryChart chart = new CategoryChartBuilder()
                .width(500)
                .height(300)
                .build();

        chart.addSeries("Metrics",
                java.util.Arrays.asList("Score","Success","Risk"),
                java.util.Arrays.asList(score, success, risk));

        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setPlotGridLinesVisible(false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapEncoder.saveBitmap(chart, baos, BitmapEncoder.BitmapFormat.PNG);

        return Image.getInstance(baos.toByteArray());
    }

    // ================= QR =================
    private Image generateQRCode(String url) throws Exception {

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, 150, 150);

        BufferedImage image = new BufferedImage(150,150,BufferedImage.TYPE_INT_RGB);

        for(int x=0;x<150;x++){
            for(int y=0;y<150;y++){
                image.setRGB(x,y, matrix.get(x,y) ? 0x000000 : 0xFFFFFF);
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image,"png",baos);

        return Image.getInstance(baos.toByteArray());
    }
}