package com.cronapp.cronnewsparserapp.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UrlHasher {
    public static String hashUrl(String url) {
        try {
            // Create a MessageDigest instance for SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Generate the hash bytes
            byte[] hashBytes = digest.digest(url.getBytes(StandardCharsets.UTF_8));

            // Convert the hash bytes to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }
}