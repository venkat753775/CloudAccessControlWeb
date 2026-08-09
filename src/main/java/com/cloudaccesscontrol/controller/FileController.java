package com.cloudaccesscontrol.controller;

import com.cloudaccesscontrol.model.FileRecord;
import com.cloudaccesscontrol.repository.FileRepository;
import com.cloudaccesscontrol.service.FileEncryptionService;

import jakarta.servlet.http.HttpSession;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class FileController {

    private final FileEncryptionService encryptionService;
    private final FileRepository fileRepository;

    private final Path encryptedDirectory =
            Paths.get("encrypted");

    public FileController(
            FileEncryptionService encryptionService,
            FileRepository fileRepository) {

        this.encryptionService = encryptionService;
        this.fileRepository = fileRepository;

        try {
            Files.createDirectories(encryptedDirectory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/upload")
    public String uploadFile(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {

        String username =
                (String) session.getAttribute("username");

        if (username == null) {
            return "redirect:/login";
        }

        try {

            if (file.isEmpty()) {
                return "redirect:/dashboard?error=Please select a file";
            }

            // Generate AES key
            SecretKey key =
                    encryptionService.generateKey();

            // Create encrypted filename
            String encryptedFilename =
                    System.currentTimeMillis()
                    + "_"
                    + file.getOriginalFilename()
                    + ".enc";

            Path outputPath =
                    encryptedDirectory.resolve(
                            encryptedFilename
                    );

            // Encrypt file
            String keyString =
                    encryptionService.encryptFile(
                            file.getBytes(),
                            outputPath,
                            key
                    );

            // Save metadata to database
            FileRecord record =
                    new FileRecord(
                            username,
                            file.getOriginalFilename(),
                            encryptedFilename,
                            keyString
                    );

            fileRepository.save(record);

            return "redirect:/dashboard";

        } catch (Exception e) {

            e.printStackTrace();

            return "redirect:/dashboard?error=File upload failed";
        }
    }


    @GetMapping("/download/{id}")
    public ResponseEntity<ByteArrayResource> downloadFile(
            @PathVariable Long id,
            HttpSession session) {

        try {

            String username =
                    (String) session.getAttribute("username");

            String role =
                    (String) session.getAttribute("role");

            // User must be logged in
            if (username == null) {
                return ResponseEntity.status(401).build();
            }

            // Find file
            FileRecord record =
                    fileRepository.findById(id)
                            .orElseThrow();

            /*
             * ADMIN:
             * Can download any file.
             *
             * USER:
             * Can download only their own file.
             */
            if (!"ADMIN".equalsIgnoreCase(role)
                    && !record.getUsername().equals(username)) {

                return ResponseEntity.status(403).build();
            }

            // Encrypted file path
            Path encryptedPath =
                    encryptedDirectory.resolve(
                            record.getEncryptedFilename()
                    );

            // Check file exists
            if (!Files.exists(encryptedPath)) {
                return ResponseEntity.notFound().build();
            }

            // Recreate AES key
            SecretKey key =
                    encryptionService.keyFromString(
                            record.getSecretKey()
                    );

            // Decrypt file
            byte[] decryptedData =
                    encryptionService.decryptFile(
                            encryptedPath,
                            key
                    );

            ByteArrayResource resource =
                    new ByteArrayResource(decryptedData);

            // Download original file
            return ResponseEntity.ok()
                    .contentType(
                            MediaType.APPLICATION_OCTET_STREAM
                    )
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\""
                            + record.getOriginalFilename()
                            + "\""
                    )
                    .body(resource);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.notFound().build();
        }
    }
}