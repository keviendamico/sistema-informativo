package com.rextart.sys.sistemainformativo.service;

import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;

import java.awt.*;

public abstract class PdfService {

    protected static final Color BG_HEADER = new Color(210, 210, 210);
    protected static final Color BLACK     = Color.BLACK;

    protected com.lowagie.text.Font font(float size, int style) {
        return new com.lowagie.text.Font(Font.HELVETICA, size, style);
    }

    protected PdfPCell brd(String text, Font font, int align, Color bg) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "", font));
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(2f);
        c.setBorderColor(BLACK);
        if (bg != null) c.setBackgroundColor(bg);
        return c;
    }

    protected PdfPCell noB(String text, Font font, int align) {
        PdfPCell c = brd(text, font, align, null);
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

}
