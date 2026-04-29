package com.example.androidfinalproject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReceiptParser {

    public static String extractTotal(String text) {
        // Look for "total $amount" first
        Pattern pattern = Pattern.compile("(?i)total\\s*[:\\-]?\\s*\\$?([0-9]+\\.[0-9]{2})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // If that fails, look for any dollar amount (largest number)
        pattern = Pattern.compile("\\$\\s*([0-9]+\\.[0-9]{2})");
        matcher = pattern.matcher(text);
        double max = 0.0;
        String maxStr = null;
        while (matcher.find()) {
            try {
                double amount = Double.parseDouble(matcher.group(1));
                if (amount > max) {
                    max = amount;
                    maxStr = matcher.group(1);
                }
            } catch (NumberFormatException e) {
                // skip invalid numbers
            }
        }
        return maxStr;
    }


    public static String extractDate(String text) {
        // Matches formats like 2025-07-29, 07/29/2025, Mar 08. 2025, etc.
        Pattern pattern = Pattern.compile(
                "\\b(\\d{4}[-/]\\d{2}[-/]\\d{2}|\\d{2}[-/]\\d{2}[-/]\\d{4}|[A-Za-z]{3,9}\\s+\\d{2}[.,]?\\s+\\d{4})\\b"
        );
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static String extractVendor(String text) {
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            // Skip short or empty lines
            if (line.length() < 3) continue;
            // Skip lines with lots of numbers or lowercase
            if (line.matches(".*\\d+.*")) continue;
            if (line.equals(line.toUpperCase()) && !line.toLowerCase().contains("transaction")) {
                return line;
            }
        }
        // Fallback to first non-empty line
        for (String line : lines) {
            if (line.trim().length() >= 3) {
                return line.trim();
            }
        }
        return null;
    }

}
