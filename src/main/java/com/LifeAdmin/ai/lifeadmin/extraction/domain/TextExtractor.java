package com.LifeAdmin.ai.lifeadmin.extraction.domain;



import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * TextExtractor: extracts text from various document formats.
 * Requirement 12
 */
public class TextExtractor {

    public static String extractFromPdf(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    public static String extractFromPlainText(byte[] textBytes) {
        return new String(textBytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    public static String normalizeText(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }
        // Normalize: trim, collapse multiple whitespace
        return rawText
            .trim()
            .replaceAll("\\s+", " ");
    }
}
