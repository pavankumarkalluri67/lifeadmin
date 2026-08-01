package com.LifeAdmin.ai.lifeadmin.document;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DocumentUploadIntegrationTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ProcessingJobRepository processingJobRepository;

    @Test
    public void testUploadDocument_CreatesDocumentAndJob() throws Exception {
        // Arrange
        byte[] pdfContent = createSimplePdf();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            pdfContent
        );

        // Act
        DocumentResponse response = documentService.upload(file);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("test.pdf", response.getOriginalFileName());
        assertEquals("UPLOADED", response.getProcessingStatus());

        // Verify document was saved
        Optional<Document> savedDoc = documentRepository.findByIdAndUserId(
            UUID.fromString(response.getId()),
            UUID.randomUUID()  // Mocked userId
        );
        assertTrue(savedDoc.isPresent());

        // Verify processing job was created
        Optional<ProcessingJob> job = processingJobRepository.findByDocumentId(UUID.fromString(response.getId()));
        assertTrue(job.isPresent());
        assertEquals("PENDING", job.get().getStatus().toString());
    }

    @Test
    public void testUploadDuplicateDocument_ReturnsDuplicateError() throws Exception {
        // First upload
        byte[] pdfContent = createSimplePdf();
        MockMultipartFile file1 = new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            pdfContent
        );
        documentService.upload(file1);

        // Second upload (same content)
        MockMultipartFile file2 = new MockMultipartFile(
            "file",
            "test2.pdf",
            "application/pdf",
            pdfContent
        );

        // Should throw exception for duplicate
        assertThrows(Exception.class, () -> documentService.upload(file2));
    }

    private byte[] createSimplePdf() {
        // Return minimal PDF bytes for testing
        return "%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj 2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj 3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]/Contents 4 0 R>>endobj 4 0 obj<</Length 44>>stream\nBT /F1 12 Tf 100 700 Td (Hello, World!) Tj ET\nendstream endobj xref\n0 5\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\n0000000214 00000 n\ntrailer<</Size 5/Root 1 0 R>>\nstartxref\n308\n%%EOF".getBytes();
    }
}
