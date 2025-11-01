package com.hub.app.consultation.service;

import com.hub.app.consultation.dto.PaymentInstallment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractTemplateService {

    private static final String TEMPLATE_PATH = "templates/contract-template.docx";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    /**
     * Generate contract document from template with provided data
     * Returns the document as Word (.docx) bytes for consultant review
     */
    public byte[] generateContractDocument(
            String homeOwnerName,
            String consultantName,
            BigDecimal contractValue,
            String projectLocation,
            String homeOwnerAddress,
            String consultantAddress,
            List<PaymentInstallment> installments) throws IOException {

        try (InputStream templateStream = new ClassPathResource(TEMPLATE_PATH).getInputStream()) {
            XWPFDocument document = new XWPFDocument(templateStream);

            // Prepare replacement data
            Map<String, String> replacements = createReplacementMap(
                homeOwnerName, consultantName, contractValue,
                projectLocation, homeOwnerAddress, consultantAddress, installments
            );

            // Replace placeholders in document
            replacePlaceholders(document, replacements);

            // Return as Word document bytes (consultant will review and convert to PDF)
            return convertToWordBytes(document);
        } catch (Exception e) {
            log.error("Error generating contract document", e);
            throw new IOException("Failed to generate contract document", e);
        }
    }

    private Map<String, String> createReplacementMap(
            String homeOwnerName,
            String consultantName,
            BigDecimal contractValue,
            String projectLocation,
            String homeOwnerAddress,
            String consultantAddress,
            List<PaymentInstallment> installments) {

        Map<String, String> replacements = new HashMap<>();

        // Basic information
        replacements.put("${homeOwnerName}", homeOwnerName != null ? homeOwnerName : "");
        replacements.put("${consultantName}", consultantName != null ? consultantName : "");
        replacements.put("${contractValue}", formatCurrency(contractValue));
        replacements.put("${contractValueInWords}", convertToWords(contractValue));
        replacements.put("${projectLocation}", projectLocation != null ? projectLocation : "");
        replacements.put("${homeOwnerAddress}", homeOwnerAddress != null ? homeOwnerAddress : "");
        replacements.put("${consultantAddress}", consultantAddress != null ? consultantAddress : "");

        // Contract metadata
        LocalDate now = LocalDate.now();
        replacements.put("${contractNumber}", "KON-" + now.getYear() + "-" + String.format("%04d", now.getDayOfYear()));
        replacements.put("${currentDate}", now.format(DATE_FORMATTER));
        replacements.put("${currentDay}", getDayName(now.getDayOfWeek()));

        // Placeholder values for additional fields (can be enhanced later)
        replacements.put("${homeOwnerNik}", "NIK akan diisi saat tanda tangan");
        replacements.put("${consultantRegistration}", "Nomor Registrasi Konsultan");

        // Add installment information
        for (int i = 0; i < installments.size(); i++) {
            PaymentInstallment installment = installments.get(i);
            int installmentNum = i + 1;
            replacements.put("${installment" + installmentNum + "Amount}",
                           formatCurrency(installment.getAmount()));
            replacements.put("${installment" + installmentNum + "DueDate}",
                           installment.getDueDate().format(DATE_FORMATTER));
        }

        // Handle third installment clause
        if (installments.size() == 3) {
            PaymentInstallment thirdInstallment = installments.get(2);
            replacements.put("${installment3Clause}",
                           "**Termin 3:** Rp " + formatCurrency(thirdInstallment.getAmount()) +
                           " pada tanggal " + thirdInstallment.getDueDate().format(DATE_FORMATTER));
        } else {
            replacements.put("${installment3Clause}", "");
        }

        // Clear unused installment placeholders
        for (int i = installments.size() + 1; i <= 3; i++) {
            replacements.put("${installment" + i + "Amount}", "");
            replacements.put("${installment" + i + "DueDate}", "");
        }

        return replacements;
    }

    private void replacePlaceholders(XWPFDocument document, Map<String, String> replacements) {
        // Replace in paragraphs
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            replaceInParagraph(paragraph, replacements);
        }

        // Replace in tables
        for (XWPFTable table : document.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        replaceInParagraph(paragraph, replacements);
                    }
                }
            }
        }
    }

    private void replaceInParagraph(XWPFParagraph paragraph, Map<String, String> replacements) {
        String text = paragraph.getText();
        if (text != null && !text.isEmpty()) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                if (text.contains(entry.getKey())) {
                    text = text.replace(entry.getKey(), entry.getValue());
                }
            }

            // Clear and rebuild paragraph with replaced text
            List<XWPFRun> runs = paragraph.getRuns();
            if (!runs.isEmpty()) {
                // Remove all existing runs
                for (int i = runs.size() - 1; i > 0; i--) {
                    paragraph.removeRun(i);
                }
                // Replace text in first run
                XWPFRun firstRun = runs.get(0);
                firstRun.setText(text, 0);
            }
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "Rp 0";
        return "Rp " + String.format("%,.0f", amount);
    }

    private String convertToWords(BigDecimal amount) {
        if (amount == null) return "nol rupiah";

        // Simple conversion for Indonesian Rupiah
        long rupiah = amount.longValue();
        if (rupiah == 0) return "nol rupiah";

        String[] units = {"", "ribu", "juta", "miliar", "triliun"};
        String[] numbers = {"", "satu", "dua", "tiga", "empat", "lima", "enam", "tujuh", "delapan", "sembilan"};

        String result = "";
        int unitIndex = 0;

        while (rupiah > 0) {
            int chunk = (int)(rupiah % 1000);
            if (chunk > 0) {
                String chunkText = convertChunkToWords(chunk, numbers);
                result = chunkText + " " + units[unitIndex] + " " + result;
            }
            rupiah /= 1000;
            unitIndex++;
        }

        return result.trim() + " rupiah";
    }

    private String convertChunkToWords(int number, String[] numbers) {
        if (number == 0) return "";

        String result = "";
        int hundreds = number / 100;
        int tens = (number % 100) / 10;
        int ones = number % 10;

        if (hundreds > 0) {
            result += numbers[hundreds] + " ratus ";
        }

        if (tens == 1) {
            String[] teens = {"sepuluh", "sebelas", "dua belas", "tiga belas", "empat belas",
                            "lima belas", "enam belas", "tujuh belas", "delapan belas", "sembilan belas"};
            result += teens[ones];
            return result;
        } else if (tens > 0) {
            String[] tenUnits = {"", "", "dua puluh", "tiga puluh", "empat puluh", "lima puluh",
                               "enam puluh", "tujuh puluh", "delapan puluh", "sembilan puluh"};
            result += tenUnits[tens] + " ";
        }

        if (ones > 0) {
            result += numbers[ones];
        }

        return result.trim();
    }

    private String getDayName(java.time.DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY: return "Senin";
            case TUESDAY: return "Selasa";
            case WEDNESDAY: return "Rabu";
            case THURSDAY: return "Kamis";
            case FRIDAY: return "Jumat";
            case SATURDAY: return "Sabtu";
            case SUNDAY: return "Minggu";
            default: return "";
        }
    }

    private byte[] convertToWordBytes(XWPFDocument document) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            document.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}