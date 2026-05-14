package com.rextart.sys.sistemainformativo.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.rextart.sys.sistemainformativo.model.Project;
import com.rextart.sys.sistemainformativo.model.Timesheet;
import com.rextart.sys.sistemainformativo.model.TimesheetAbsenceRow;
import com.rextart.sys.sistemainformativo.model.TimesheetRow;
import com.rextart.sys.sistemainformativo.repository.TimesheetAbsenceRowRepository;
import com.rextart.sys.sistemainformativo.repository.TimesheetRowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimesheetPdfService {

    private final TimesheetRowRepository timesheetRowRepository;
    private final TimesheetAbsenceRowRepository timesheetAbsenceRowRepository;

    private static final Color BG_HEADER = new Color(210, 210, 210);
    private static final Color BLACK     = Color.BLACK;

    public byte[] generatePdf(Timesheet ts) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 20f, 20f, 28f, 20f);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        YearMonth ym = YearMonth.of(ts.getYear(), ts.getMonth());
        int days = ym.lengthOfMonth();

        List<TimesheetRow> projectRows = timesheetRowRepository.findByTimesheetId(ts.getId());
        List<TimesheetAbsenceRow> absRows  = timesheetAbsenceRowRepository.findByTimesheetId(ts.getId());

        // Active projects (insertion-ordered)
        Map<Long, Project> projectMap = new LinkedHashMap<>();
        for (TimesheetRow r : projectRows) projectMap.put(r.getProject().getId(), r.getProject());

        // hours[pid][day]
        Map<Long, Map<Integer, Integer>> hoursByProject = new LinkedHashMap<>();
        for (Long pid : projectMap.keySet()) hoursByProject.put(pid, new HashMap<>());
        for (TimesheetRow r : projectRows)
            hoursByProject.get(r.getProject().getId()).put(r.getDay(), r.getHours());

        // absence hours[day]
        Map<Integer, Integer> absHours = new HashMap<>();
        for (TimesheetAbsenceRow ar : absRows)
            if (ar.getHours() != null && ar.getHours() > 0) absHours.put(ar.getDay(), ar.getHours());

        List<Project> projects = new ArrayList<>(projectMap.values());

        addPageHeader(doc, ts);
        addGrid(doc, days, projects, hoursByProject, absHours);
        addSummary(doc, hoursByProject);
        addActivitiesNotes(doc, ts);
        addSignatures(doc, ts, ym);

        doc.close();
        log.info("PDF generated for timesheet {}", ts.getId());
        return baos.toByteArray();
    }

    private void addPageHeader(Document doc, Timesheet ts) throws Exception {
        Font fSmall = font(6.5f, Font.NORMAL);
        Font fBold  = font(8f, Font.BOLD);

        PdfPTable top = new PdfPTable(new float[]{30f, 40f, 30f});
        top.setWidthPercentage(100f);
        top.setSpacingAfter(1f);

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy, HH:mm"));
        top.addCell(noB(now, fSmall, Element.ALIGN_LEFT));
        top.addCell(noB("Sistema Informativo Rextart S.r.l.", fSmall, Element.ALIGN_CENTER));
        top.addCell(noB("", fSmall, Element.ALIGN_RIGHT));
        top.addCell(noB("Rextart S.r.l. - Timesheet", fBold, Element.ALIGN_LEFT));
        top.addCell(noB("", fSmall, Element.ALIGN_CENTER));
        top.addCell(noB("", fSmall, Element.ALIGN_RIGHT));
        doc.add(top);

        Paragraph title = new Paragraph("DIPENDENTE", font(13f, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(3f);
        title.setSpacingAfter(3f);
        doc.add(title);

        String fullName = ts.getUser().getFirstName() + " " + ts.getUser().getLastName();
        String period   = String.format("%d/%02d", ts.getYear(), ts.getMonth());

        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100f);
        info.setSpacingAfter(4f);
        info.addCell(brd("Dipendente: " + fullName, font(8f, Font.BOLD), Element.ALIGN_LEFT, null));
        info.addCell(brd("anno e mese di riferimento:   " + period, font(8f, Font.BOLD), Element.ALIGN_RIGHT, null));
        doc.add(info);
    }

    private void addGrid(Document doc, int days,
                         List<Project> projects,
                         Map<Long, Map<Integer, Integer>> hoursByProject,
                         Map<Integer, Integer> absHours) throws Exception {
        Font fHdr  = font(6.5f, Font.BOLD);
        Font fCell = font(6.5f, Font.NORMAL);
        Font fTot  = font(6.5f, Font.BOLD);

        final int N = 10; // colonne commessa sempre fisse a 10
        // pad con null per gli slot vuoti
        List<Project> cols = new ArrayList<>(projects);
        while (cols.size() < N) cols.add(null);

        int totalCols = 1 + N + 3; // G | 10 proj | ASS | TP | T
        float gW   = 14f;
        float assW = 20f;
        float tpW  = 20f;
        float tW   = 20f;
        float pW   = (680f - gW - assW - tpW - tW) / N;

        float[] widths = new float[totalCols];
        widths[0] = gW;
        for (int i = 0; i < N; i++) widths[1 + i] = pW;
        widths[1 + N]     = assW;
        widths[1 + N + 1] = tpW;
        widths[1 + N + 2] = tW;

        PdfPTable table = new PdfPTable(totalCols);
        table.setWidthPercentage(100f);
        table.setWidths(widths);
        table.setHeaderRows(2);

        // -- header row 1 --
        PdfPCell gH = brd("G", fHdr, Element.ALIGN_CENTER, BG_HEADER);
        gH.setRowspan(2);
        table.addCell(gH);

        PdfPCell projH = brd(
            "Codici commessa su cui si è lavorato\n(ore di presenza/ore di reperibilità'/ore per interventi)",
            fHdr, Element.ALIGN_CENTER, BG_HEADER);
        projH.setColspan(N);
        table.addCell(projH);

        for (String lbl : new String[]{"ASS\n(1)", "TP", "T"}) {
            PdfPCell h = brd(lbl, fHdr, Element.ALIGN_CENTER, BG_HEADER);
            h.setRowspan(2);
            table.addCell(h);
        }

        // -- header row 2: codici commessa (vuoto se slot libero) --
        for (Project p : cols)
            table.addCell(brd(p != null ? p.getCode() : "", fHdr, Element.ALIGN_CENTER, BG_HEADER));

        // -- righe giorno --
        int[] colTotals = new int[N];
        int absTotal = 0, presTotal = 0;

        for (int day = 1; day <= days; day++) {
            table.addCell(brd(String.valueOf(day), fTot, Element.ALIGN_CENTER, null));

            int pres = 0;
            for (int ci = 0; ci < N; ci++) {
                Project proj = cols.get(ci);
                String val = "";
                if (proj != null) {
                    Integer h = hoursByProject.get(proj.getId()).get(day);
                    if (h != null && h > 0) { val = String.valueOf(h); colTotals[ci] += h; pres += h; }
                }
                table.addCell(brd(val, fCell, Element.ALIGN_CENTER, null));
            }

            Integer absH = absHours.get(day);
            table.addCell(brd(absH != null ? String.valueOf(absH) : "", fCell, Element.ALIGN_CENTER, null));
            if (absH != null) absTotal += absH;

            table.addCell(brd(pres > 0 ? String.valueOf(pres) : "", fCell, Element.ALIGN_CENTER, null));
            presTotal += pres;

            int tot = pres + (absH != null ? absH : 0);
            table.addCell(brd(String.valueOf(tot), fCell, Element.ALIGN_CENTER, null));
        }

        // -- riga totale footer --
        table.addCell(brd("T", fTot, Element.ALIGN_CENTER, BG_HEADER));
        for (int ci = 0; ci < N; ci++)
            table.addCell(brd(colTotals[ci] > 0 ? String.valueOf(colTotals[ci]) : "", fTot, Element.ALIGN_CENTER, BG_HEADER));
        table.addCell(brd(absTotal > 0 ? String.valueOf(absTotal) : "", fTot, Element.ALIGN_CENTER, BG_HEADER));
        table.addCell(brd(String.valueOf(presTotal), fTot, Element.ALIGN_CENTER, BG_HEADER));
        table.addCell(brd(String.valueOf(presTotal + absTotal), fTot, Element.ALIGN_CENTER, BG_HEADER));

        doc.add(table);
    }

    private void addSummary(Document doc, Map<Long, Map<Integer, Integer>> hoursByProject) throws Exception {
        Font fHdr = font(7f, Font.BOLD);

        int presTotal = hoursByProject.values().stream()
                .flatMap(m -> m.values().stream()).mapToInt(Integer::intValue).sum();

        PdfPTable t = new PdfPTable(new float[]{40f, 10f});
        t.setWidthPercentage(45f);
        t.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.setSpacingBefore(2f);

        t.addCell(brd("Totale ore di presenza",    fHdr, Element.ALIGN_LEFT,   null));
        t.addCell(brd(String.valueOf(presTotal),   fHdr, Element.ALIGN_CENTER, null));
        t.addCell(brd("Totale ore di reperibilità", fHdr, Element.ALIGN_LEFT,  null));
        t.addCell(brd("0",                         fHdr, Element.ALIGN_CENTER, null));
        t.addCell(brd("Totale ore di interventi",  fHdr, Element.ALIGN_LEFT,   null));
        t.addCell(brd("0",                         fHdr, Element.ALIGN_CENTER, null));

        doc.add(t);
    }

    private void addActivitiesNotes(Document doc, Timesheet ts) throws Exception {
        Font fLabel = font(7f, Font.BOLD);
        Font fText  = font(7f, Font.NORMAL);

        PdfPTable t = new PdfPTable(new float[]{18f, 82f});
        t.setWidthPercentage(100f);
        t.setSpacingBefore(4f);

        PdfPCell actLabel = brd("Attività'\nprogettuali del\nmese:", fLabel, Element.ALIGN_LEFT, null);
        actLabel.setMinimumHeight(28f);
        t.addCell(actLabel);

        PdfPCell actVal = brd(ts.getActivities() != null ? ts.getActivities() : "", fText, Element.ALIGN_LEFT, null);
        actVal.setMinimumHeight(28f);
        t.addCell(actVal);

        t.addCell(brd("Note:", fLabel, Element.ALIGN_LEFT, null));
        t.addCell(brd(ts.getNotes() != null ? ts.getNotes() : "", fText, Element.ALIGN_LEFT, null));

        doc.add(t);
    }

    private void addSignatures(Document doc, Timesheet ts, YearMonth ym) throws Exception {
        Font fHdr  = font(7f, Font.BOLD);
        Font fName = font(9f, Font.BOLD);

        PdfPTable t = new PdfPTable(3);
        t.setWidthPercentage(100f);
        t.setSpacingBefore(4f);

        t.addCell(brd("Data di compilazione", fHdr, Element.ALIGN_CENTER, BG_HEADER));
        t.addCell(brd("Validato da",           fHdr, Element.ALIGN_CENTER, BG_HEADER));
        t.addCell(brd("Compilato da",          fHdr, Element.ALIGN_CENTER, BG_HEADER));

        String date = ts.getSubmittedAt() != null
                ? ts.getSubmittedAt().format(DateTimeFormatter.ofPattern("d/M/yyyy"))
                : ym.atEndOfMonth().format(DateTimeFormatter.ofPattern("d/M/yyyy"));
        String compiledBy  = ts.getUser().getFirstName() + " " + ts.getUser().getLastName();
        String validatedBy = ts.getValidatedBy() != null
                ? ts.getValidatedBy().getFirstName() + " " + ts.getValidatedBy().getLastName()
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
    }

    private Font font(float size, int style) {
        return new Font(Font.HELVETICA, size, style);
    }

    private PdfPCell brd(String text, Font font, int align, Color bg) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "", font));
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(2f);
        c.setBorderColor(BLACK);
        if (bg != null) c.setBackgroundColor(bg);
        return c;
    }

    private PdfPCell noB(String text, Font font, int align) {
        PdfPCell c = brd(text, font, align, null);
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }
}
