package com.cloudaccesscontrol.service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class FileEncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    private static final int KEY_SIZE = 128;
    private static final int IV_SIZE = 12;
    private static final int TAG_LENGTH = 128;

    // Generate AES key
    public SecretKey generateKey() throws Exception {

        KeyGenerator keyGenerator =
                KeyGenerator.getInstance(ALGORITHM);

        keyGenerator.init(KEY_SIZE);

        return keyGenerator.generateKey();
    }

    // Encrypt file
    public String encryptFile(
            byte[] fileData,
            Path outputPath,
            SecretKey key) throws Exception {

        // Generate random IV
        byte[] iv = new byte[IV_SIZE];

        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);

        // Create cipher
        Cipher cipher =
                Cipher.getInstance(TRANSFORMATION);

        GCMParameterSpec gcmSpec =
                new GCMParameterSpec(TAG_LENGTH, iv);

        cipher.init(
                Cipher.ENCRYPT_MODE,
                key,
                gcmSpec
        );

        // Encrypt data
        byte[] encryptedData =
                cipher.doFinal(fileData);

        // Store IV + encrypted data
        byte[] output =
                new byte[iv.length + encryptedData.length];

        System.arraycopy(
                iv,
                0,
                output,
                0,
                iv.length
        );

        System.arraycopy(
                encryptedData,
                0,
                output,
                iv.length,
                encryptedData.length
        );

        Files.write(outputPath, output);

        // Store AES key as Base64
        return Base64.getEncoder()
                .encodeToString(key.getEncoded());
    }

    // Decrypt file
    public byte[] decryptFile(
            Path encryptedFile,
            SecretKey key) throws Exception {

        byte[] encryptedFileData =
                Files.readAllBytes(encryptedFile);

        // Extract IV
        byte[] iv =
                new byte[IV_SIZE];

        System.arraycopy(
                encryptedFileData,
                0,
                iv,
                0,
                IV_SIZE
        );

        // Extract encrypted content
        byte[] encryptedData =
                new byte[
                        encryptedFileData.length - IV_SIZE
                ];

        System.arraycopy(
                encryptedFileData,
                IV_SIZE,
                encryptedData,
                0,
                encryptedData.length
        );

        // Create cipher
        Cipher cipher =
                Cipher.getInstance(TRANSFORMATION);

        GCMParameterSpec gcmSpec =
                new GCMParameterSpec(
                        TAG_LENGTH,
                        iv
                );

        cipher.init(
                Cipher.DECRYPT_MODE,
                key,
                gcmSpec
        );

        return cipher.doFinal(encryptedData);
    }

    // Convert Base64 key back to AES key
    public SecretKey keyFromString(
            String keyString) {

        byte[] decodedKey =
                Base64.getDecoder()
                        .decode(keyString);

        return new SecretKeySpec(
                decodedKey,
                ALGORITHM
        );
    }
}