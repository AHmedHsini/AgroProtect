package tn.esprit.spring1.Services;

import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.knowm.xchart.*;
import org.springframework.stereotype.Service;
import tn.esprit.spring1.entities.Investment;
import tn.esprit.spring1.entities.Project;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.List;

@Service
public class InvestorPdfService {

    public byte[] generateInvestorReport(Project project,
                                         List<Investment> investments,
                                         double success,
                                         String explanation,
                                         String baseUrl) throws Exception {

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        // 🎨 COLORS
        java.awt.Color blue = new java.awt.Color(52,152,219);
        java.awt.Color light = new java.awt.Color(245,247,250);

        // ================= HEADER =================
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);

        PdfPCell headerCell = new PdfPCell(new Phrase(
                "AI INVESTMENT REPORT",
                new Font(Font.HELVETICA, 18, Font.BOLD, java.awt.Color.WHITE)
        ));

        headerCell.setBackgroundColor(blue);
        headerCell.setPadding(20);
        headerCell.setBorder(Rectangle.NO_BORDER);

        header.addCell(headerCell);
        document.add(header);

        document.add(new Paragraph("Generated on: " + new Date()));
        document.add(new Paragraph(" "));

        // ================= PROJECT =================
        document.add(new Paragraph("PROJECT INFORMATION",
                new Font(Font.HELVETICA, 12, Font.BOLD)));

        document.add(new Paragraph("Project: " + project.getName()));
        document.add(new Paragraph("Funding Goal: " + project.getFundingGoal()));
        document.add(new Paragraph("Collected: " + project.getCollectedAmount()));

        document.add(new Paragraph(" "));

        // ================= AI INSIGHTS =================
        PdfPTable card = new PdfPTable(1);
        card.setWidthPercentage(100);

        PdfPCell cardCell = new PdfPCell();
        cardCell.setPadding(20);
        cardCell.setBackgroundColor(light);
        cardCell.setBorder(Rectangle.NO_BORDER);

        cardCell.addElement(new Paragraph("AI ANALYSIS",
                new Font(Font.HELVETICA, 12, Font.BOLD)));

        cardCell.addElement(new Paragraph("Success Prediction: " + (int)(success * 100) + "%"));

        cardCell.addElement(new Paragraph("Decision: " +
                (success > 0.7 ? "INVEST" : "RISKY")));

        cardCell.addElement(new Paragraph("Explanation: " + explanation));

        card.addCell(cardCell);
        document.add(card);

        document.add(new Paragraph(" "));

        if (investments == null || investments.isEmpty()) {
            investments = new ArrayList<>();
        }

        // ================= INVESTOR CHART =================
        Image investorChart = generateInvestorChart(investments);
        investorChart.scaleToFit(500,300);
        document.add(investorChart);

        document.add(new Paragraph(" "));

        // ================= TABLE =================
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);

        table.addCell("Investor");
        table.addCell("Amount");
        table.addCell("Share");

        double total = investments.stream().mapToDouble(Investment::getAmount).sum();

        for (Investment i : investments) {
            table.addCell(i.getInvestor().getName());
            table.addCell(String.valueOf(i.getAmount()));
            table.addCell(String.format("%.2f %%", (i.getAmount()/total)*100));
        }

        document.add(table);

        document.add(new Paragraph(" "));

        // ================= QR =================
        String qrUrl = baseUrl + "/ai/dashboard/" + project.getIdProject();

        Image qr = generateQRCode(qrUrl);
        qr.scaleToFit(120,120);

        document.add(new Paragraph("Scan for full AI dashboard"));
        document.add(qr);

        document.close();
        return out.toByteArray();
    }

    // ================= CHART =================
    private Image generateInvestorChart(List<Investment> investments) throws Exception {

        CategoryChart chart = new CategoryChartBuilder()
                .width(500)
                .height(300)
                .title("Investor Contributions")
                .build();

        List<String> names = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        for (Investment i : investments) {
            names.add(i.getInvestor().getName());
            values.add(i.getAmount());
        }

        chart.addSeries("Investment", names, values);

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