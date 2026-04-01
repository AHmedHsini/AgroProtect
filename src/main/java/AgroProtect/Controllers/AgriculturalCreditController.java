package AgroProtect.Controllers;
import AgroProtect.DTOs.CreditCreateDTO;
import AgroProtect.entities.AgriculturalCredit;
import AgroProtect.entities.CreditStatus;
import AgroProtect.services.IAgriculturalCreditService;
import AgroProtect.services.PdfService;
import com.itextpdf.text.DocumentException;
import lombok.AllArgsConstructor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/agricultural-credits")
@AllArgsConstructor
public class AgriculturalCreditController {

    private IAgriculturalCreditService ACser;
    private PdfService pdfService;



    @GetMapping
    public List<AgriculturalCredit> getAll() {
        return ACser.getAllAgriculturalCredit();
    }

    @GetMapping("/{id}")
    public AgriculturalCredit getById(@PathVariable Long id) {
        return ACser.getAgriculturalCreditById(id);
    }

    @PutMapping("/{id}/status")
    public AgriculturalCredit updateStatus(
            @PathVariable Long id,
            @RequestParam CreditStatus status) {
        return ACser.updateCreditStatus(id, status);
    }

    @GetMapping("/triage")
    public List<AgriculturalCredit> getTriageCredits() {
        return ACser.getAllAgriculturalCredit()
                .stream()
                .sorted(Comparator
                        .comparing(AgriculturalCredit::getDisbursementDate) // oldest first
                        .thenComparing(AgriculturalCredit::getApprovedAmount).reversed()
                )
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generateCreditPdf(@PathVariable Long id) throws DocumentException {
        AgriculturalCredit credit = ACser.getAgriculturalCreditById(id);
        ByteArrayOutputStream baos = pdfService.generateCreditPdf(credit);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=credit_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }
   
    @GetMapping("/summary")
    public Map<String, Object> getCreditStats() {
        List<AgriculturalCredit> credits = ACser.getAllAgriculturalCredit();

        long totalCredits = credits.size();
        double totalApproved = credits.stream().mapToDouble(AgriculturalCredit::getApprovedAmount).sum();
        double totalPaid = credits.stream().mapToDouble(AgriculturalCredit::getPaidAmount).sum();

        Map<CreditStatus, Long> byStatus = credits.stream()
                .collect(Collectors.groupingBy(AgriculturalCredit::getStatus, Collectors.counting()));

        double averageInterest = credits.stream()
                .mapToDouble(AgriculturalCredit::getInterestRate)
                .average()
                .orElse(0);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCredits", totalCredits);
        stats.put("totalApprovedAmount", totalApproved);
        stats.put("totalPaidAmount", totalPaid);
        stats.put("averageInterestRate", averageInterest);
        stats.put("creditsByStatus", byStatus);

        return stats;
    }
    @GetMapping("/graph/status")
    public ResponseEntity<byte[]> getCreditStatusGraph() throws IOException {
        List<AgriculturalCredit> credits = ACser.getAllAgriculturalCredit();

        Map<CreditStatus, Long> statusCount = credits.stream()
                .collect(Collectors.groupingBy(AgriculturalCredit::getStatus, Collectors.counting()));

        DefaultPieDataset dataset = new DefaultPieDataset();
        statusCount.forEach((status, count) -> dataset.setValue(status.name(), count));

        JFreeChart chart = ChartFactory.createPieChart(
                "Credits by Status", dataset, true, true, false);

        BufferedImage image = chart.createBufferedImage(600, 400);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(baos.toByteArray());
    }


}