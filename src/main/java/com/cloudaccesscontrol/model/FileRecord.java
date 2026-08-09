package com.cloudaccesscontrol.model;

import jakarta.persistence.*;

@Entity
@Table(name = "files")
public class FileRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "encrypted_filename")
    private String encryptedFilename;

    @Column(name = "secret_key")
    private String secretKey;

    @Column(name = "upload_time")
    private java.time.LocalDateTime uploadTime;

    public FileRecord() {
    }

    public FileRecord(
            String username,
            String originalFilename,
            String encryptedFilename,
            String secretKey) {

        this.username = username;
        this.originalFilename = originalFilename;
        this.encryptedFilename = encryptedFilename;
        this.secretKey = secretKey;
        this.uploadTime = java.time.LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getEncryptedFilename() {
        return encryptedFilename;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public java.time.LocalDateTime getUploadTime() {
        return uploadTime;
    }
}