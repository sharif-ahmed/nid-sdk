package com.commlink.citl_nid_sdk.core;


import java.util.regex.*;

public class BangladeshNidParser {

    public static class NidData {

        public String name = "";
        public String nameBangla = "";
        public String dob = "";
        public String fatherName = "";
        public String fatherNameBangla = "";
        public String motherName = "";
        public String motherNameBangla = "";
        public String addressBangla = "";
        public String nidNumber = "";
        public boolean isBangladeshNid = false;
    }

    public static NidData parse(String rawText) {

        NidData data = new NidData();

        String text = normalize(rawText);

        data.isBangladeshNid = isValidNidCard(text);

        data.nidNumber = extractNid(text);

        data.dob = extractDOB(text);
        data.name = extractName(text);
        data.nameBangla = extractBanglaName(text);
        data.fatherName = extractFatherName(text);
        data.fatherNameBangla = extractBanglaFatherName(text);
        data.motherName = extractMotherName(text);
        data.motherNameBangla = extractBanglaMotherName(text);
        data.addressBangla = extractBanglaAddress(rawText);
        return data;
    }


    // Normalize OCR text
    static String normalize(String text) {
        return text
                .replace("Namie", "Name")
                .replace("Narne", "Name")
                .replace("Nane", "Name")
                .replace("Govemment", "Government")
                .replace("Govermment", "Government")
                .replace("Peoples", "People's")
                .replace("সাম", "নাম") // Common OCR misspelling
                .replace("ন্যম", "নাম") // Common OCR misspelling
                .replace("ना", "নাম") // Common OCR misspelling
                .replace("সাম:", "Name")
                .replace("নাম:", "Name")
                .replace("ঠিকানা:", "Address")
                .replace("পিত্", "পিতা") // Common OCR misspelling
                .replace("পিত্ত", "পিতা") // Common OCR misspelling
                .replace("পিকা", "পিতা") // Common OCR misspelling
                .replace("পিত", "পিতা") // Common OCR misspelling
                .replace("মাতত", "মাতা") // Common OCR misspelling
                .replace("আতা", "মাতা") // Common OCR misspelling
                .replace("মাকা", "মাতা") // Common OCR misspelling
                .replace("থাত", "মাতা") // Common OCR misspelling
                .replace("মাত", "মাতা") // Common OCR misspelling
                .replace("থানা", "মাতা") // Common OCR misspelling
                .replace("সাতত", "মাতা") // Common OCR misspelling
                .replace("জাতীয় পরিচয়পত্র", "National ID")
                .replace("National D Card", "National ID Card")
                .replace("জন্ম তারিখ:", "DOB")
                .replace("এনআইডি নম্বর:", "NID No")
                .replaceAll("[|]", " ")
                .trim();
    }


    // Validate Bangladesh NID
    /*static boolean isValidNidCard(String text){

        return text.contains("Government of the People's Republic of Bangladesh")
                || text.contains("National ID Card");
    }*/

    static boolean isValidNidCard(String text) {
        if (text == null || text.isEmpty()) return false;
        text = text.toLowerCase();
        String[] keywords = {
                "government",
                "people's republic",
                "republic of bangladesh",
                "bangladesh",
                "national id",
                "national id card",
                "nid",
                "জাতীয় পরিচয়পত্র"
        };
        int matchCount = 0;
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                matchCount++;
            }
        }
        // if at least 2 keywords match → valid NID
        return matchCount >= 2;
    }

    // Extract NID Number
    static String extractNid(String text) {
        Pattern pattern = Pattern.compile("\\b\\d{3}\\s?\\d{3}\\s?\\d{4}\\b");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) return BangladeshNidParser.cleanNid(matcher.group());
        return "";
    }

    // Extract DOB
    static String extractDOB(String text) {
        Pattern pattern = Pattern.compile(
                "\\b\\d{1,2}\\s(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s\\d{4}\\b"
        );
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) return matcher.group();
        return "";
    }

    // Extract Name (English)
    static String extractName(String text) {
        String[] lines = text.split("\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.equalsIgnoreCase("Name") || line.contains("Name") || line.contains("নাম")) {
                if (i + 1 < lines.length) {
                    String nameLine = lines[i + 1].trim();
                    if (nameLine.matches("[A-Z ]{3,}")) return nameLine;
                }
            }
        }
        for (String line : lines) {
            line = line.trim();
            if (line.matches("[A-Z ]{5,}")
                    && !line.contains("GOVERNMENT")
                    && !line.contains("NATIONAL")
                    && !line.contains("REPUBLIC")) {
                return line;
            }
        }
        return "";
    }

    // Extract Bangla Name
    static String extractBanglaName(String text) {
        String[] lines = text.split("\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.contains("নাম") || line.contains("Name") || line.equals("নাম")) {
                // In some formats, Bangla name is BEFORE "Name" label or AFTER "নাম"
                if (i > 0) {
                    String prevLine = lines[i - 1].trim();
                    if (isBangla(prevLine) && prevLine.length() > 2) return prevLine;
                }
                if (i + 1 < lines.length) {
                    String nextLine = lines[i + 1].trim();
                    if (isBangla(nextLine) && nextLine.length() > 2) return nextLine;
                }
            }
        }
        for (String line : lines) {
            line = line.trim();
            if (!line.matches("[A-Z ]{5,}")
                    && !line.contains("GOVERNMENT")
                    && !line.contains("NATIONAL")
                    && !line.contains("REPUBLIC")
            ) {
                return line;
            }
        }
        return "";
    }

    // Extract Father Name (English)
    static String extractFatherName(String text) {
        String[] lines = text.split("\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.equalsIgnoreCase("Father") || line.contains("Father's Name") || line.contains("পিতা")) {
                // Search subsequent lines for English characters
                for (int j = i + 1; j < Math.min(i + 4, lines.length); j++) {
                    String val = lines[j].trim();
                    if (val.matches("[A-Z .]{3,}")) return val;
                }
            }
        }
        return "";
    }

    // Extract Mother Name (English)
    static String extractMotherName(String text) {
        String[] lines = text.split("\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.equalsIgnoreCase("Mother") || line.contains("Mother's Name") || line.contains("মাতা")) {
                for (int j = i + 1; j < Math.min(i + 4, lines.length); j++) {
                    String val = lines[j].trim();
                    if (val.matches("[A-Z .]{3,}")) return val;
                }
            }
        }
        return "";
    }


    // Extract Father Name (Bangla)
    static String extractBanglaFatherName(String text) {
        String[] lines = text.split("\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.contains("পিতা") || line.contains("Father")) {
                if (i + 1 < lines.length) {
                    String nextLine = lines[i + 1].trim();
                    if (isBangla(nextLine)) return nextLine;
                }
            }
        }
        return "";
    }

    static String extractFatherNameFromEnglishLine(String ocrText) {
        if (ocrText == null || ocrText.isEmpty()) return "";

        String[] lines = ocrText.split("\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            // Detect English Name lines
            if (line.equalsIgnoreCase("Name") || line.equalsIgnoreCase("\\Name")) {
                // Skip next line (English name itself)
                int nextLine = i + 2;
                if (nextLine < lines.length) {
                    String fatherCandidate = lines[nextLine].trim();
                    if (isBangla(fatherCandidate)) {
                        return fatherCandidate; // This is Father Bangla Name
                    }
                }
            }
        }

        return "";
    }

    // Extract Mother Name (Bangla)
    /*static String extractBanglaMotherName(String text) {
        String[] lines = text.split("\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            // Mother's name often follows Father's name without a clear label in some OCRs
            if (line.contains("পিতা") || line.contains("Father")) {
                // Look for the second Bangla line after Father label
                int banglaCount = 0;
                for (int j = i + 1; j < Math.min(i + 6, lines.length); j++) {
                    String check = lines[j].trim();
                    *//*if (isBangla(check)) {
                        banglaCount++;
                        if (banglaCount == 2) return check; // Highly likely the mother's name
                    }*//*
                    if (check.contains("মাতা") || check.contains("Mother")) {
                         if (j + 1 < lines.length && isBangla(lines[j+1])) return lines[j+1].trim();
                    }
                }
            }
        }
        return "";
    }*/

    static String extractBanglaMotherName(String text) {
        if (text == null || text.isEmpty()) return "";

        String[] lines = text.split("\\n");
        String motherName = "";

        // Step 1: Look for explicit labels
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.contains("মাতা") || line.toLowerCase().contains("mother")) {
                // Check current line after label
                String[] parts = line.split("[:：]");
                if (parts.length > 1 && isBangla(parts[1].trim())) {
                    return parts[1].trim();
                }
                // Check next line
                if (i + 1 < lines.length) {
                    String nextLine = lines[i + 1].trim();
                    if (isBangla(nextLine)) return nextLine;
                }
            }
        }

        // Step 2: Fallback -> take Bangla text JUST before DOB
        // We look for both "Date of Birth", "DOB" (as normalization might have changed it), and "জন্ম তারিখ"
        String[] dobKeywords = {"Date of Birth", "DOB", "জন্ম তারিখ", "জন্ম"};
        int dobIndex = -1;
        String foundKeyword = "";

        for (String key : dobKeywords) {
            dobIndex = text.indexOf(key);
            if (dobIndex != -1) {
                foundKeyword = key;
                break;
            }
        }

        if (dobIndex > 0) {
            String beforeDob = text.substring(0, dobIndex).trim();
            String[] tokens = beforeDob.split("\\n");
            // Take the last Bangla line before the DOB label
            for (int i = tokens.length - 1; i >= 0; i--) {
                String candidate = tokens[i].trim();
                if (isBangla(candidate) && candidate.length() > 2) {
                    // Ensure it's not just the Father's name (which is further up)
                    // If we found a father name earlier, we should ensure this is different
                    return candidate;
                }
            }
        }

        return motherName;
    }

    public static String extractFatherBanglaName(String ocrText) {

        String[] lines = ocrText.split("\\n");

        // ===== STEP 1: Try 'পিতা' keyword =====
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.contains("পিতা") || line.contains("পিত") || line.toLowerCase().contains("father")) {
                // next 1-2 lines check
                for (int j = i + 1; j <= i + 2 && j < lines.length; j++) {
                    String possible = lines[j].trim();
                    if (isBangla(possible)) {
                        return possible;
                    }
                }
            }
        }

        // ===== STEP 2: Fallback: English Name + skip line + next line =====
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (isEnglishName(line)) {
                int targetIndex = i + 2; // skip 1 line
                if (targetIndex < lines.length) {
                    String fallback = lines[targetIndex].trim();
                    if (isBangla(fallback)) {
                        return fallback;
                    }
                }
            }
        }

        // ===== STEP 3: Forward scan next 4 lines after English Name =====
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (isEnglishName(line)) {
                for (int j = i + 1; j <= i + 4 && j < lines.length; j++) {
                    String next = lines[j].trim();
                    if (isBangla(next)) {
                        return next;
                    }
                }
            }
        }

        // ===== STEP 4: Fallback: 9th line (index 8) =====
        if (lines.length >= 9) {
            String line9 = lines[8].trim();
            if (isBangla(line9)) {
                return line9;
            }
        }

        return "";
    }

    // Mother Name extractor
    public static String extractMotherBanglaName(String ocrText) {

        String[] lines = ocrText.split("\\n");

        // ===== STEP 1: Try 'মাতা' keyword =====
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.contains("মাতা") || line.contains("মাত") || line.toLowerCase().contains("mother")) {
                // next 1-2 lines check
                for (int j = i + 1; j <= i + 2 && j < lines.length; j++) {
                    String possible = lines[j].trim();
                    if (isBangla(possible)) {
                        return possible;
                    }
                }
            }
        }

        // ===== STEP 2: Fallback: Date of Birth line এর আগের Bangla line =====
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim().toLowerCase();
            if (line.contains("date of birth") || line.contains("dob")) {
                // scan previous 1-3 lines
                for (int j = i - 1; j >= 0 && j >= i - 3; j--) {
                    String prev = lines[j].trim();
                    if (isBangla(prev)) {
                        return prev;
                    }
                }
            }
        }

        // ===== STEP 3: Fallback: 11th line (index 10) =====
        if (lines.length >= 11) {
            String line11 = lines[10].trim();
            if (isBangla(line11)) {
                return line11;
            }
        }

        // ===== STEP 4: Last resort: forward scan top 10 lines for Bangla name =====
        for (int i = 0; i < Math.min(10, lines.length); i++) {
            String text = lines[i].trim();
            if (isBangla(text)) {
                return text;
            }
        }

        return "";
    }

    // Simple check for Bangla characters
    /*static boolean isBangla(String input) {
        for (char c : input.toCharArray()) {
            if (c >= '\u0980' && c <= '\u09FF') return true;
        }
        return false;
    }*/

    private static boolean isBangla(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '\u0980' && c <= '\u09FF') return true;
        }
        return false;
    }

    // English Name detect
    private static boolean isEnglishName(String text) {
        return text.matches("^[A-Z ]{3,}$");
    }

    // Extract Bangla Address
    /*static String extractBanglaAddress(String text) {
        String[] lines = text.split("\\n");
        StringBuilder address = new StringBuilder();
        boolean foundAddress = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            // Address usually starts after "ঠিকানা:" label on the back side
            if (line.contains("ঠিকানা") || line.contains("Address")) {
                foundAddress = true;
                // Check if the same line has some content after the label
                String[] parts = line.split("[:：]");
                if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                    address.append(parts[1].trim()).append(" ");
                }
                continue;
            }

            if (foundAddress) {
                // If we hit another major label or empty line, stop
                if (line.isEmpty() || line.contains("রক্তের") || line.contains("জন্ম") || line.contains("Blood") || line.contains("Place")) {
                    break;
                }
                // Append multi-line address
                address.append(line).append(" ");

                // Usually address is 2-4 lines long
                if (address.toString().split(" ").length > 20) break;
            }
        }

        // Fallback: If no label found, look for typical address keywords like "বাসা/হোল্ডিং"
        if (address.length() == 0) {
            for (String line : lines) {
                String l = line.trim();
                if (l.contains("বাসা") || l.contains("গ্রাম/রাস্তা") || l.contains("ডাকঘর")) {
                    address.append(l).append(" ");
                    foundAddress = true;
                } else if (foundAddress) {
                    if (l.isEmpty() || l.contains("রক্তের")) break;
                    address.append(l).append(" ");
                }
            }
        }

        return address.toString().trim();
    }*/

    /*
    * escape english in address bangla ocr
    * */
    // Extract Bangla Address
    static String extractBanglaAddress(String text) {
        String[] lines = text.split("\\n");
        StringBuilder address = new StringBuilder();
        boolean foundAddress = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            // Address usually starts after "ঠিকানা:" label on the back side
            if (line.contains("ঠিকানা") || line.contains("Address")) {
                foundAddress = true;
                // Check if the same line has some content after the label
                String[] parts = line.split("[:：]");
                if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                    address.append(parts[1].trim()).append(" ");
                }
                continue;
            }

            if (foundAddress) {
                // If we hit another major label or empty line, stop
                if (line.isEmpty() || line.contains("রক্তের") || line.contains("জন্ম") || line.contains("Blood")
                        || line.contains("Place")) {
                    break;
                }
                // Append multi-line address
                address.append(line).append(" ");

                // Usually address is 2-4 lines long
                if (address.toString().split(" ").length > 20)
                    break;
            }
        }

        // Fallback: If no label found, look for typical address keywords like
        // "বাসা/হোল্ডিং"
        if (address.length() == 0) {
            for (String line : lines) {
                String l = line.trim();
                if (l.contains("বাসা") || l.contains("গ্রাম/রাস্তা") || l.contains("ডাকঘর")) {
                    address.append(l).append(" ");
                    foundAddress = true;
                } else if (foundAddress) {
                    if (l.isEmpty() || l.contains("রক্তের"))
                        break;
                    address.append(l).append(" ");
                }
            }
        }

        String result = address.toString().trim();
        return stripEnglish(result);
    }

    private static String stripEnglish(String input) {
        if (input == null)
            return "";
        // Remove English alphabets (A-Z, a-z)
        // Keep Bangla, numbers, and common punctuation (, - / .)
        return input.replaceAll("[A-Za-z]", "").replaceAll("\\s+", " ").trim();
    }

    public static String cleanNid(String input) {
        if (input == null) return "";
        return input.replaceAll("[^0-9]", "");
    }
}
