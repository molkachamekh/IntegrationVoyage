package com.example.voyage.utils;

import javafx.scene.image.Image;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class ImageUtils {

    /**
     * Convert an image file to Base64 string
     * 
     * @param imageFile The image file to convert
     * @return Base64 encoded string
     * @throws IOException If reading the file fails
     */
    public static String imageToBase64(File imageFile) throws IOException {
        byte[] fileContent = Files.readAllBytes(imageFile.toPath());
        return Base64.getEncoder().encodeToString(fileContent);
    }

    /**
     * Convert Base64 string to JavaFX Image
     * 
     * @param base64String Base64 encoded string
     * @return JavaFX Image object
     */
    public static Image base64ToImage(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }

        try {
            byte[] imageData = Base64.getDecoder().decode(base64String);
            ByteArrayInputStream stream = new ByteArrayInputStream(imageData);
            return new Image(stream);
        } catch (IllegalArgumentException e) {
            System.err.println("Error decoding Base64 string: " + e.getMessage());
            return null;
        }
    }

    /**
     * Convert Base64 string to byte array
     * 
     * @param base64String Base64 encoded string
     * @return Byte array
     */
    public static byte[] base64ToByteArray(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }

        try {
            return Base64.getDecoder().decode(base64String);
        } catch (IllegalArgumentException e) {
            System.err.println("Error decoding Base64 string: " + e.getMessage());
            return null;
        }
    }

    /**
     * Convert byte array to Base64 string
     * 
     * @param imageData Byte array of image data
     * @return Base64 encoded string
     */
    public static String byteArrayToBase64(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            return null;
        }

        return Base64.getEncoder().encodeToString(imageData);
    }
}
