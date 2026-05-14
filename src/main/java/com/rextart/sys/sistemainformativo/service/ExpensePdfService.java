package com.rextart.sys.sistemainformativo.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.rextart.sys.sistemainformativo.model.ExpenseReport;
import com.rextart.sys.sistemainformativo.model.ExpenseRow;
import com.rextart.sys.sistemainformativo.repository.ExpenseRowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpensePdfService extends PdfService {

    private final ExpenseRowRepository expenseRowRepository;

    public byte[] generatePdf(ExpenseReport report) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 15f, 15f, 28f, 15f);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        YearMonth ym = YearMonth.of(report.getYear(), report.getMonth());
        List<ExpenseRow> rows = expenseRowRepository.findByExpenseReportId(report.getId());

        addPageHeader(doc, report);
        addGrid(doc, rows);
        addSignatures(doc, report, ym);

        doc.close();
        log.info("PDF generated for expense report {}", report.getId());
        return baos.toByteArray();
    }

    private void addPageHeader(Document doc, ExpenseReport report) {
        Font fSmall = font(6.5f, Font.NORMAL);
        Font fBold  = font(8f, Font.BOLD);

        PdfPTable top = new PdfPTable(new float[]{30f, 40f, 30f});
        top.setWidthPercentage(100f);
        top.setSpacingAfter(1f);

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy, HH:mm"));
        top.addCell(noB(now, fSmall, Element.ALIGN_LEFT));
        top.addCell(noB("Sistema Informativo Rextart S.r.l.", fSmall, Element.ALIGN_CENTER));
        top.addCell(noB("", fSmall, Element.ALIGN_RIGHT));
        top.addCell(noB("Rextart S.r.l. - Nota Spese", fBold, Element.ALIGN_LEFT));
        top.addCell(noB("", fSmall, Element.ALIGN_CENTER));
        top.addCell(noB("", fSmall, Element.ALIGN_RIGHT));
        doc.add(top);

        Paragraph title = new Paragraph("NOTA SPESE", font(13f, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(3f);
        title.setSpacingAfter(3f);
        doc.add(title);

        String fullName = report.getUser().getFirstName() + " " + report.getUser().getLastName();
        String period   = String.format("%d/%02d", report.getYear(), report.getMonth());
        String project  = report.getProject() != null ? report.getProject().getCode() : "—";
        String vehicle  = buildVehicleString(report);

        PdfPTable info = new PdfPTable(new float[]{25f, 25f, 25f, 25f});
        info.setWidthPercentage(100f);
        info.setSpacingAfter(4f);
        info.addCell(brd("Dipendente: " + fullName, fBold, Element.ALIGN_LEFT, null));
        info.addCell(brd("Periodo: " + period,      fBold, Element.ALIGN_LEFT, null));
        info.addCell(brd("Commessa: " + project,    fBold, Element.ALIGN_LEFT, null));
        info.addCell(brd("Veicolo: " + vehicle,     fBold, Element.ALIGN_LEFT, null));

        String plate = report.getPlate() != null ? report.getPlate() : "—";
        String attachments = String.valueOf(report.getAttachmentCount());
        info.addCell(brd("Targa: " + plate,               font(7f, Font.NORMAL), Element.ALIGN_LEFT, null));
        info.addCell(brd("N° allegati: " + attachments,   font(7f, Font.NORMAL), Element.ALIGN_LEFT, null));
        info.addCell(noB("", font(7f, Font.NORMAL), Element.ALIGN_LEFT));
        info.addCell(noB("", font(7f, Font.NORMAL), Element.ALIGN_LEFT));
        doc.add(info);
    }

    private void addGrid(Document doc, List<ExpenseRow> rows) {
        Font fHdr  = font(6f, Font.BOLD);
        Font fCell = font(6f, Font.NORMAL);
        Font fTot  = font(6.5f, Font.BOLD);

        // GG | Km | Tragitto | A/R | Mezzo | Pasti | Hotel | Altro | Causale | Pagamento | Totale
        float[] widths = {8f, 10f, 40f, 8f, 12f, 14f, 14f, 14f, 40f, 18f, 14f};
        PdfPTable table = new PdfPTable(widths.length);
        table.setWidthPercentage(100f);
        table.setWidths(widths);
        table.setHeaderRows(1);

        for (String hdr : new String[]{"GG", "Km", "Tragitto", "A/R", "Mezzo",
                "Pasti (€)", "Hotel (€)", "Altro (€)", "Causale", "Pagamento", "Totale (€)"}) {
            table.addCell(brd(hdr, fHdr, Element.ALIGN_CENTER, BG_HEADER));
        }

        BigDecimal totMeal = BigDecimal.ZERO;
        BigDecimal totAccom = BigDecimal.ZERO;
        BigDecimal totOther = BigDecimal.ZERO;
        BigDecimal totAll = BigDecimal.ZERO;

        for (ExpenseRow row : rows) {
            table.addCell(brd(String.valueOf(row.getDay()), fCell, Element.ALIGN_CENTER, null));
            table.addCell(brd(row.getKm() != null ? row.getKm().toPlainString() : "", fCell, Element.ALIGN_CENTER, null));
            table.addCell(brd(nvl(row.getRoute()), fCell, Element.ALIGN_LEFT, null));
            table.addCell(brd(row.isRoundTrip() ? "Sì" : "No", fCell, Element.ALIGN_CENTER, null));
            table.addCell(brd(row.getVehicleType() != null ? row.getVehicleType().getLabel() : "", fCell, Element.ALIGN_CENTER, null));

            BigDecimal meal  = nvlDecimal(row.getMealAmount());
            BigDecimal accom = nvlDecimal(row.getAccommodationAmount());
            BigDecimal other = nvlDecimal(row.getOtherAmount());
            BigDecimal rowTotal = meal.add(accom).add(other);

            table.addCell(brd(formatDecimal(row.getMealAmount()),          fCell, Element.ALIGN_RIGHT, null));
            table.addCell(brd(formatDecimal(row.getAccommodationAmount()), fCell, Element.ALIGN_RIGHT, null));
            table.addCell(brd(formatDecimal(row.getOtherAmount()),         fCell, Element.ALIGN_RIGHT, null));
            table.addCell(brd(nvl(row.getDescription()), fCell, Element.ALIGN_LEFT, null));
            table.addCell(brd(row.getPaymentMethod() != null ? row.getPaymentMethod().getLabel() : "", fCell, Element.ALIGN_CENTER, null));
            table.addCell(brd(rowTotal.compareTo(BigDecimal.ZERO) > 0 ? rowTotal.toPlainString() : "", fCell, Element.ALIGN_RIGHT, null));

            totMeal  = totMeal.add(meal);
            totAccom = totAccom.add(accom);
            totOther = totOther.add(other);
            totAll   = totAll.add(rowTotal);
        }

        // Footer totals
        table.addCell(brd("Totale", fTot, Element.ALIGN_CENTER, BG_HEADER));
        table.addCell(brd("", fTot, Element.ALIGN_CENTER, BG_HEADER));
        table.addCell(brd("", fTot, Element.ALIGN_CENTER, BG_HEADER));
        table.addCell(brd("", fTot, Element.ALIGN_CENTER, BG_HEADER));
        table.addCell(brd("", fTot, Element.ALIGN_CENTER, BG_HEADER));
        table.addCell(brd(totMeal.toPlainString(),  fTot, Element.ALIGN_RIGHT, BG_HEADER));
        table.addCell(brd(totAccom.toPlainString(), fTot, Element.ALIGN_RIGHT, BG_HEADER));
        table.addCell(brd(totOther.toPlainString(), fTot, Element.ALIGN_RIGHT, BG_HEADER));
        table.addCell(brd("", fTot, Element.ALIGN_CENTER, BG_HEADER));
        table.addCell(brd("", fTot, Element.ALIGN_CENTER, BG_HEADER));
        table.addCell(brd(totAll.toPlainString(), fTot, Element.ALIGN_RIGHT, BG_HEADER));

        doc.add(table);
    }

    private void addSignatures(Document doc, ExpenseReport report, YearMonth ym) {
        Font fHdr  = font(7f, Font.BOLD);
        Font fName = font(9f, Font.BOLD);

        PdfPTable t = new PdfPTable(3);
        t.setWidthPercentage(100f);
        t.setSpacingBefore(6f);

        t.addCell(brd("Data di compilazione", fHdr, Element.ALIGN_CENTER, BG_HEADER));
        t.addCell(brd("Validato da",           fHdr, Element.ALIGN_CENTER, BG_HEADER));
        t.addCell(brd("Compilato da",          fHdr, Element.ALIGN_CENTER, BG_HEADER));

        String date = report.getSubmittedAt() != null
                ? report.getSubmittedAt().format(DateTimeFormatter.ofPattern("d/M/yyyy"))
                : ym.atEndOfMonth().format(DateTimeFormatter.ofPattern("d/M/yyyy"));
        String compiledBy  = report.getUser().getFirstName() + " " + report.getUser().getLastName();
        String validatedBy = report.getValidatedBy() != null
                ? report.getValidatedBy().getFirstName() + " " + report.getValidatedBy().getLastName()
                : "";

        PdfPCell dateCell = brd(date, fName, Element.ALIGN_CENTER, null);
        dateCell.setMinimumHeight(22f);
        t.addCell(dateCell);
        PdfPCell validCell = brd(validatedBy, fName, Element.ALIGN_CENTER, null);
        validCell.setMinimumHeight(22f);
        t.addCell(validCell);
        PdfPCell compiledCell = brd(compiledBy, fName, Element.ALIGN_CENTER, null);
        compiledCell.setMinimumHeight(22f);
        t.addCell(compiledCell);

        doc.add(t);

        if (report.getNotes() != null && !report.getNotes().isBlank()) {
            PdfPTable notes = new PdfPTable(new float[]{15f, 85f});
            notes.setWidthPercentage(100f);
            notes.setSpacingBefore(4f);
            notes.addCell(brd("Note:", font(7f, Font.BOLD), Element.ALIGN_LEFT, null));
            notes.addCell(brd(report.getNotes(), font(7f, Font.NORMAL), Element.ALIGN_LEFT, null));
            doc.add(notes);
        }
    }

    private String buildVehicleString(ExpenseReport report) {
        StringBuilder sb = new StringBuilder();
        if (report.getVehicle() != null) sb.append(report.getVehicle());
        if (report.getEngineCc() != null) sb.append(" (").append(report.getEngineCc()).append(" cc)");
        return !sb.isEmpty() ? sb.toString() : "—";
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }

    private BigDecimal nvlDecimal(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private String formatDecimal(BigDecimal v) {
        return v != null && v.compareTo(BigDecimal.ZERO) > 0 ? v.toPlainString() : "";
    }
}
