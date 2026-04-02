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
                                   String baseUrl,
                                   Quote bestQuote) throws Exception {

        score = Math.min(Math.max(score, 0), 1);
        success = Math.min(Math.max(success, 0), 1);

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        // 🎨 COLORS
        java.awt.Color blue1 = new java.awt.Color(41,128,185);
        java.awt.Color blue2 = new java.awt.Color(52,152,219);
        java.awt.Color light = new java.awt.Color(245,247,250);

        java.awt.Color scoreColor = score > 0.7 ? new java.awt.Color(39,174,96)
                : score > 0.4 ? new java.awt.Color(243,156,18)
                : new java.awt.Color(231,76,60);

        // ================= HEADER GRADIENT =================
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);

        PdfPCell headerCell = new PdfPCell();
        headerCell.setPadding(30);
        headerCell.setBorder(Rectangle.NO_BORDER);

        // 🔥 fake gradient (2 blocs)
        headerCell.setBackgroundColor(blue1);

        Paragraph title = new Paragraph("🚀 AGROPROTECT AI",
                new Font(Font.HELVETICA, 22, Font.BOLD, java.awt.Color.WHITE));
        title.setAlignment(Element.ALIGN_CENTER);

        Paragraph sub = new Paragraph("Smart Investment Intelligence Report",
                new Font(Font.HELVETICA, 11, Font.NORMAL, java.awt.Color.WHITE));
        sub.setAlignment(Element.ALIGN_CENTER);

        headerCell.addElement(title);
        headerCell.addElement(sub);

        header.addCell(headerCell);
        document.add(header);

        Paragraph date = new Paragraph("Generated on: " + new Date(),
                new Font(Font.HELVETICA, 9, Font.NORMAL, java.awt.Color.GRAY));
        date.setAlignment(Element.ALIGN_CENTER);

        document.add(date);
        document.add(new Paragraph(" "));

        // ================= SCORE CARD =================
        PdfPTable scoreTable = new PdfPTable(1);
        scoreTable.setWidthPercentage(45);
        scoreTable.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell scoreCell = new PdfPCell();
        scoreCell.setPadding(35);
        scoreCell.setBackgroundColor(light);
        scoreCell.setBorder(Rectangle.NO_BORDER);

        Paragraph scoreTitle = new Paragraph("AI SCORE",
                new Font(Font.HELVETICA, 12, Font.BOLD, java.awt.Color.GRAY));
        scoreTitle.setAlignment(Element.ALIGN_CENTER);

        Paragraph value = new Paragraph(
                (int)(score * 100) + "%",
                new Font(Font.HELVETICA, 44, Font.BOLD, scoreColor)
        );
        value.setAlignment(Element.ALIGN_CENTER);

        scoreCell.addElement(scoreTitle);
        scoreCell.addElement(value);

        scoreTable.addCell(scoreCell);
        document.add(scoreTable);

        document.add(new Paragraph(" "));

        // ================= PROJECT =================
        Paragraph section = new Paragraph("PROJECT OVERVIEW",
                new Font(Font.HELVETICA, 14, Font.BOLD, blue1));
        section.setAlignment(Element.ALIGN_CENTER);
        section.setSpacingAfter(10);

        document.add(section);

        document.add(new Paragraph("📌 Project: " + quote.getProject().getName()));
        document.add(new Paragraph("💰 Goal: " + quote.getProject().getFundingGoal() + " TND"));
        document.add(new Paragraph("📊 Collected: " + quote.getProject().getCollectedAmount() + " TND"));

        document.add(new Paragraph(" "));

        // ================= AI PREMIUM =================
        PdfPTable aiCard = new PdfPTable(1);
        aiCard.setWidthPercentage(100);

        PdfPCell aiCell = new PdfPCell();
        aiCell.setPadding(25);
        aiCell.setBackgroundColor(light);
        aiCell.setBorder(Rectangle.NO_BORDER);

        Paragraph aiTitle = new Paragraph("🧠 AI ANALYSIS",
                new Font(Font.HELVETICA, 14, Font.BOLD, blue1));

        aiCell.addElement(aiTitle);
        aiCell.addElement(new Paragraph(" "));

        aiCell.addElement(new Paragraph("🚀 Success Probability: " + (int)(success * 100) + "%"));
        aiCell.addElement(new Paragraph("📊 Risk Level: " + (success > 0.7 ? "Low" : success > 0.4 ? "Medium" : "High")));

        aiCell.addElement(new Paragraph(" "));

        // 🔥 AI explanation stylée
        aiCell.addElement(new Paragraph("💡 AI Insight:",
                new Font(Font.HELVETICA, 11, Font.BOLD)));

        aiCell.addElement(new Paragraph(reason));

        aiCell.addElement(new Paragraph(" "));

        aiCell.addElement(new Paragraph("📌 Recommendation:",
                new Font(Font.HELVETICA, 11, Font.BOLD)));

        aiCell.addElement(new Paragraph(
                success > 0.7 ?
                        "This project shows strong potential with balanced financial indicators."
                        :
                        "Caution advised: risk factors detected in financial structure."
        ));

        aiCard.addCell(aiCell);
        document.add(aiCard);

        document.add(new Paragraph(" "));

        // ================= BEST QUOTE =================
        PdfPTable bestCard = new PdfPTable(1);
        bestCard.setWidthPercentage(100);

        PdfPCell bestCell = new PdfPCell();
        bestCell.setPadding(25);
        bestCell.setBackgroundColor(new java.awt.Color(232, 246, 243));
        bestCell.setBorder(Rectangle.NO_BORDER);

        Paragraph bestTitle = new Paragraph("🏆 BEST OFFER (AI SELECTED)",
                new Font(Font.HELVETICA, 13, Font.BOLD, new java.awt.Color(39,174,96)));

        bestCell.addElement(bestTitle);
        bestCell.addElement(new Paragraph(" "));

        bestCell.addElement(new Paragraph("📌 Provider: " + bestQuote.getPartner().getName()));
        bestCell.addElement(new Paragraph("💰 Price: " + bestQuote.getProposedPrice() + " TND"));
        bestCell.addElement(new Paragraph("📦 Equipment: " + bestQuote.getEquipmentName()));
        bestCell.addElement(new Paragraph("⏱ Delivery: " + bestQuote.getDeliveryTimeDays() + " days"));

        double diff = quote.getProposedPrice() - bestQuote.getProposedPrice();
        bestCell.addElement(new Paragraph("📊 Price Difference: " + diff + " TND"));

        if(bestQuote.getId().equals(quote.getId())) {
            bestCell.addElement(new Paragraph("✅ THIS IS THE BEST OPTION",
                    new Font(Font.HELVETICA, 11, Font.BOLD, new java.awt.Color(39,174,96))));
        }

        bestCard.addCell(bestCell);
        document.add(bestCard);

        document.add(new Paragraph(" "));

        // ================= PIE CHART CENTER =================
        PdfPTable charts = new PdfPTable(1);
        charts.setWidthPercentage(100);

        Image pie = generateScoreChart(score, success);
        pie.scaleToFit(350, 250);
        pie.setAlignment(Element.ALIGN_CENTER);

        PdfPCell pieCell = new PdfPCell();
        pieCell.setBorder(Rectangle.NO_BORDER);
        pieCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pieCell.setPadding(20);
        pieCell.addElement(pie);

        charts.addCell(pieCell);

        document.add(charts);

        Paragraph legend = new Paragraph("Score vs Success vs Risk Distribution",
                new Font(Font.HELVETICA, 10, Font.ITALIC, java.awt.Color.GRAY));
        legend.setAlignment(Element.ALIGN_CENTER);

        document.add(legend);
        document.add(new Paragraph(" "));

        // ================= PROGRESS =================
        double progress = quote.getProject().getCollectedAmount() /
                quote.getProject().getFundingGoal();

        int percent = (int)(progress * 100);

        Paragraph progText = new Paragraph("📈 Funding Progress: " + percent + "%",
                new Font(Font.HELVETICA, 11, Font.BOLD));
        progText.setAlignment(Element.ALIGN_CENTER);

        document.add(progText);

        PdfPTable progressBar = new PdfPTable(1);
        progressBar.setWidthPercentage(60);
        progressBar.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell barProgress = new PdfPCell(new Phrase(""));
        barProgress.setFixedHeight(14);
        barProgress.setBackgroundColor(blue2);
        barProgress.setBorder(Rectangle.NO_BORDER);

        progressBar.addCell(barProgress);
        document.add(progressBar);

        document.add(new Paragraph(" "));

        // ================= QR CENTER =================
        String qrUrl = baseUrl + "/mfi/view/top3/" + quote.getProject().getIdProject();

        Paragraph qrText = new Paragraph("📱 Scan to open dashboard",
                new Font(Font.HELVETICA, 10, Font.NORMAL, java.awt.Color.GRAY));
        qrText.setAlignment(Element.ALIGN_CENTER);

        document.add(qrText);

        Image qr = generateQRCode(qrUrl);
        qr.scaleToFit(120,120);
        qr.setAlignment(Element.ALIGN_CENTER);

        document.add(qr);

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