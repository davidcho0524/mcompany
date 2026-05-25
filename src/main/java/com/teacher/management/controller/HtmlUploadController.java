package com.teacher.management.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/html")
public class HtmlUploadController {

    private final String uploadDir = "uploads/html/";

    @PostMapping("/upload")
    public ResponseEntity<Object> uploadHtmlFile(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "filename", required = false) String inputFilename,
            jakarta.servlet.http.HttpServletRequest request) {

        System.out.println("=== Incoming Upload Request ===");
        System.out.println("Content-Type: " + request.getContentType());
        
        java.util.Map<String, String> debugInfo = new java.util.HashMap<>();
        debugInfo.put("contentType", request.getContentType());
        
        try {
            java.util.List<String> partNames = new java.util.ArrayList<>();
            if (request.getContentType() != null && request.getContentType().startsWith("multipart/form-data")) {
                for (jakarta.servlet.http.Part part : request.getParts()) {
                    partNames.add(part.getName() + " (size: " + part.getSize() + ")");
                    System.out.println("Found part: " + part.getName() + " (size: " + part.getSize() + ")");
                }
            }
            debugInfo.put("foundParts", partNames.toString());
        } catch (Exception e) {
            System.out.println("Error reading parts: " + e.getMessage());
            debugInfo.put("partsError", e.getMessage());
        }

        if (file == null || file.isEmpty()) {
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("error", "Please select a file to upload. Parameter 'file' is missing.");
            response.put("debug", debugInfo);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            // Create directories if they don't exist
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Generate file name
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            // Only allow html files
            if (!".html".equalsIgnoreCase(extension) && !".htm".equalsIgnoreCase(extension)) {
                java.util.Map<String, String> errorResponse = new java.util.HashMap<>();
                errorResponse.put("error", "Only HTML files are allowed.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            if (originalFilename == null || originalFilename.isEmpty()) {
                originalFilename = "uploaded.html";
            }

            // Create filename as yyyyMMddHHmmss_filename.html or yyyyMMddHHmmss_random.html
            String datePrefix = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String suffix;
            if (inputFilename != null && !inputFilename.trim().isEmpty()) {
                suffix = inputFilename;
                // Add extension if not provided in the input filename
                if (!suffix.toLowerCase().endsWith(".html") && !suffix.toLowerCase().endsWith(".htm")) {
                    suffix += extension;
                }
            } else {
                suffix = java.util.UUID.randomUUID().toString().substring(0, 8) + extension;
            }
            String newFilename = datePrefix + "_" + suffix;
            Path filePath = Paths.get(uploadDir, newFilename);

            // Save file
            Files.write(filePath, file.getBytes());

            // Build URL with requested domain
            String fileDownloadUri = "http://hubspoteapi.com:8080/html/" + newFilename;

            System.out.println("File successfully saved to absolute path: " + directory.getAbsolutePath());

            java.util.Map<String, String> response = new java.util.HashMap<>();
            response.put("message", "File uploaded successfully");
            response.put("fileUrl", fileDownloadUri);
            response.put("fileName", newFilename);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            java.util.Map<String, String> response = new java.util.HashMap<>();
            response.put("error", "Could not upload the file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
