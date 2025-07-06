package com.example.birdquest.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.util.Log;


public class BirdDataExtractor {

    private static final String TAG = "DataExtractor";

    // Call this method from a background thread (e.g., using AsyncTask, Coroutines, ExecutorService)
    public static String extractMatchingDataUrl(String speciesUrl, String latinName) {
        if (speciesUrl == null || speciesUrl.isEmpty() || latinName == null || latinName.isEmpty()) {
            Log.e(TAG, "Species URL or Latin name is null or empty.");
            return null;
        }

        String[] latinParts = latinName.toLowerCase(Locale.ROOT).split("\\s+");
        if (latinParts.length < 2) {
            Log.e(TAG, "Latin name does not have at least two parts: " + latinName);
            return null; // Expecting at least two parts like "Genus species"
        }

        // Construct the search pattern based on the first 3 letters of the first two parts
        // e.g., "Microcarbo pygmaeus" -> "mic" and "pyg"
        String part1Prefix = latinParts[0].length() >= 3 ? latinParts[0].substring(0, 3) : latinParts[0];
        String part2Prefix = latinParts[1].length() >= 3 ? latinParts[1].substring(0, 3) : latinParts[1];

        // This pattern looks for the prefixes in the image URL path.
        // It's a bit loose to account for variations like hyphens or underscores.
        // Example: /mic.*pyg.*\.jpg (or .png, .jpeg etc.)
        // We will refine this by checking the domain as well.
        String searchPatternInPath = ".*" + Pattern.quote(part1Prefix) + ".*" + Pattern.quote(part2Prefix) + ".*\\.(jpg|jpeg|png|gif)";
        Pattern pattern = Pattern.compile(searchPatternInPath, Pattern.CASE_INSENSITIVE);

        Log.d(TAG, "Attempting to fetch URL: " + speciesUrl);
        Log.d(TAG, "Latin Name: " + latinName + ", Part1 Prefix: " + part1Prefix + ", Part2 Prefix: " + part2Prefix);
        Log.d(TAG, "Search pattern for path: " + searchPatternInPath);


        try {
            // 1. Fetch the HTML content
            // Adding a user agent can sometimes help avoid being blocked
            Document doc = Jsoup.connect(speciesUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(10000) // 10 seconds timeout
                    .get();

            // 2. Parse the HTML to find <img> tags
            Elements imgTags = doc.select("img[src]"); // Select all img tags with an src attribute

            Log.d(TAG, "Found " + imgTags.size() + " img tags.");

            for (Element imgTag : imgTags) {
                String imgSrc = imgTag.attr("abs:src"); // Get absolute URL for the image source
                // "abs:src" resolves relative URLs against the base URI of the document.

                if (imgSrc == null || imgSrc.isEmpty()) {
                    continue;
                }

                Log.d(TAG, "Checking img src: " + imgSrc);


                if (!imgSrc.toLowerCase(Locale.ROOT).startsWith("https://pasaridinromania.sor.ro/")) {
                    Log.d(TAG, "Skipping image from different domain: " + imgSrc);
                    continue;
                }

                // Check if the path part of the URL matches our pattern
                Matcher matcher = pattern.matcher(imgSrc);
                if (matcher.find()) {
                    Log.i(TAG, "MATCH FOUND: " + imgSrc);
                    return imgSrc; // Return the first match
                }
            }

            Log.w(TAG, "No matching image found for pattern on URL: " + speciesUrl);
            return null; // No matching image found

        } catch (IOException e) {
            Log.e(TAG, "Error fetching or parsing URL " + speciesUrl + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            Log.e(TAG, "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}