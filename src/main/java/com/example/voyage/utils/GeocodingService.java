package com.example.voyage.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

public class GeocodingService {
    private static final String API_KEY = "516978462101196299454x78616";
    private static final String GEOCODE_URL = "https://geocode.xyz/";

    /**
     * Get latitude and longitude coordinates for a location name
     * 
     * @param locationName The name of the location to geocode
     * @return double array with [latitude, longitude] or null if geocoding failed
     */
    public double[] getCoordinates(String locationName) {
        try {
            // Debug print
            System.out.println("Geocoding location: " + locationName);

            String encodedLocation = URLEncoder.encode(locationName, StandardCharsets.UTF_8.toString());
            String urlStr = GEOCODE_URL + encodedLocation + "?json=1&auth=" + API_KEY;

            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10 second timeout

            int responseCode = connection.getResponseCode();
            System.out.println("Geocoding API response code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse the JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                System.out.println("Geocoding API response: " + jsonResponse.toString());

                if (jsonResponse.has("latt") && jsonResponse.has("longt")) {
                    double lat = Double.parseDouble(jsonResponse.getString("latt"));
                    double lon = Double.parseDouble(jsonResponse.getString("longt"));

                    // Check if the coordinates are valid (not 0,0 which often indicates failure)
                    if (Math.abs(lat) > 0.001 || Math.abs(lon) > 0.001) {
                        System.out.println("Found coordinates: " + lat + ", " + lon);
                        return new double[] { lat, lon };
                    } else {
                        System.out.println("Coordinates too close to 0,0, likely invalid");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Geocoding error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Geocoding failed, returning null");
        return null;
    }
}
