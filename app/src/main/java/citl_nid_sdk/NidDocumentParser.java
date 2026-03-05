package citl_nid_sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import citl_nid_sdk.parser.models.NidCardType;

/**
 * Production-grade KYC document parser specialized for Bangladesh National ID cards.
 * Handles both Smart (New) and Legacy (Old) formats with robust OCR noise tolerance.
 */
public class NidDocumentParser {

    public static NidCardType detectType(String text) {
        if (text.contains("Smart") || text.contains("National ID Card")) {
            return NidCardType.SMART;
        } else if (text.contains("NATIONAL ID CARD") || text.contains("জাতীয় পরিচয় পত্র")) {
            return NidCardType.OLD;
        }
        // Heuristic fallback
        return text.length() < 500 ? NidCardType.SMART : NidCardType.OLD;
    }

    public static class NidData {
        public String name_bn = "";
        public String name_en = "";
        public String father_name = "";
        public String mother_name = "";
        public String date_of_birth = "";
        public String nid_number = "";

        @Override
        public String toString() {
            return "NidData{" +
                    "name_bn='" + name_bn + '\'' +
                    ", name_en='" + name_en + '\'' +
                    ", father_name='" + father_name + '\'' +
                    ", mother_name='" + mother_name + '\'' +
                    ", date_of_birth='" + date_of_birth + '\'' +
                    ", nid_number='" + nid_number + '\'' +
                    '}';
        }
    }

    /**
     * Parse raw OCR text into structured NidData.
     */
    public static NidData parse(String rawText) {
        if (rawText == null || rawText.isEmpty()) return new NidData();

        String normalizedText = preprocess(rawText);
        String[] lines = normalizedText.split("\\n");
        List<String> cleanedLines = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !isHeaderOrNoise(trimmed)) {
                cleanedLines.add(trimmed);
            }
        }

        NidData data = new NidData();
        data.nid_number = extractNidNumber(normalizedText);
        data.date_of_birth = extractDateOfBirth(normalizedText);

        // Core extraction logic using layout order and language dominance
        extractNamesAndParents(cleanedLines, data);

        return data;
    }

    private static String preprocess(String text) {
        // Standardize common OCR errors but keep structure
        return text.replace("\r", "")
                .replaceAll("[|]", " ")
                // Don't normalize labels to English here to avoid losing layout clues,
                // but we handle them during extraction.
                .trim();
    }

    private static boolean isHeaderOrNoise(String line) {
        String lower = line.toLowerCase();
        return lower.contains("গণপ্রজাতন্ত্রী বাংলাদেশ সরকার") ||
               lower.contains("government of the people") ||
               lower.contains("national id card") ||
               lower.contains("জাতীয় পরিচয়পত্র");
    }

    private static String extractNidNumber(String text) {
        // Handle spaces and look for 10, 13, 17 digit sequences
        Pattern pattern = Pattern.compile("(\\d[\\s\\d]{9,18}\\d)");
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            String match = matcher.group().replaceAll("\\s", "");
            if (match.length() == 10 || match.length() == 13 || match.length() == 17) {
                return match;
            }
        }
        
        // Fallback: longest digit string
        String digitsOnly = text.replaceAll("[^0-9]", "");
        if (digitsOnly.length() >= 10) return digitsOnly;

        return "";
    }

    private static String extractDateOfBirth(String text) {
        // Pattern: DD Mon YYYY (handling colon and spaces)
        Pattern pattern = Pattern.compile("(\\d{1,2}\\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+\\d{4})", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) return matcher.group();
        return "";
    }

    private static void extractNamesAndParents(List<String> lines, NidData data) {
        int engNameIndex = -1;

        // 1. Find English Name Anchor (ALL CAPS, 5+ characters, excluding common noise)
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (isEnglishName(line)) {
                Pattern pattern = Pattern.compile("^Name\\s*:?\\s*([A-Z]{2,}(?:\\s[A-Z]{2,})+)$");
                Matcher matcher = pattern.matcher(line.trim());

                if (matcher.find()) {
                    String name = matcher.group(1);
                    data.name_en = name;
                }
                //data.name_en = line;
                engNameIndex = i;
                break;
            }
        }

        // 2. Bangla Name (Backward scan from English anchor)
        if (engNameIndex != -1) {
            for (int i = engNameIndex - 1; i >= 0; i--) {
                String line = lines.get(i);
                if (isBangla(line) && line.length() >= 5) {
                    if (isNameLabel(line)) {
                        String val = extractValueAfterLabel(line);
                        if (!val.isEmpty()) {
                            data.name_bn = cleanBanglaField(val, "name");
                            break;
                        }
                    } else {
                        data.name_bn = cleanBanglaField(line, "name");
                        break;
                    }
                }
            }
        }

        // 3. Father & Mother Name (Forward scan from English anchor)
        int startSearch = (engNameIndex != -1) ? engNameIndex + 1 : 0;
        int foundFatherIndex = -1;

        for (int i = startSearch; i < lines.size(); i++) {
            String line = lines.get(i);
            if (isBangla(line) && (line.contains("পিতা") || isFatherLabel(line))) {
                // If the same line has value after label
                String val = extractValueAfterLabel(line);
                if (!val.isEmpty()) {
                    data.father_name = cleanBanglaField(val, "father");
                } else if (i + 1 < lines.size()) {
                    // Check next line
                    String next = lines.get(i + 1);
                    if (isBangla(next) && !next.contains("মাতা")) {
                        data.father_name = cleanBanglaField(next, "father");
                        foundFatherIndex = i + 1;
                    }
                }
                if (!data.father_name.isEmpty()) {
                    if (foundFatherIndex == -1) foundFatherIndex = i;
                    break;
                }
            }
        }

        int motherSearchStart = (foundFatherIndex != -1) ? foundFatherIndex + 1 : startSearch;
        for (int i = motherSearchStart; i < lines.size(); i++) {
            String line = lines.get(i);
            if (isBangla(line) && (line.contains("মাতা") || isMotherLabel(line))) {
                String val = extractValueAfterLabel(line);
                if (!val.isEmpty()) {
                    data.mother_name = cleanBanglaField(val, "mother");
                } else if (i + 1 < lines.size()) {
                    String next = lines.get(i + 1);
                    if (isBangla(next) && !isDobLine(next)) {
                        data.mother_name = cleanBanglaField(next, "mother");
                    }
                }
                if (!data.mother_name.isEmpty()) break;
            }
        }
    }

    private static boolean isFatherLabel(String line) {
        return line.contains("পিতা") || line.contains("পিক");
    }

    private static boolean isMotherLabel(String line) {
        return line.contains("মাতা") || line.contains("মাতত");
    }

    private static String extractValueAfterLabel(String line) {
        String[] parts = line.split("[:：;\\-]");
        if (parts.length > 1) {
            return parts[1].trim();
        }
        return "";
    }

    private static boolean isDobLine(String line) {
        return line.toLowerCase().contains("birth") || line.toLowerCase().contains("date");
    }

    private static boolean isNameLabel(String line) {
        String l = line.toLowerCase();
        return l.contains("নাম") || l.contains("name") || l.contains("ন্যম") || l.equals("অম");
    }


    private static boolean isEnglishName(String line) {
        //("^Name\\s*:\\s*([A-Z]{2,}(?:\\s[A-Z]{2,})+)$")
        //line.matches("^[A-Z\\s]{5,}$")
        return line.matches("^Name\\s*:?\\s*([A-Z]{2,}(?:\\s[A-Z]{2,})+)$") &&
               !line.contains("DATE") && 
               !line.contains("ID") && 
               !line.contains("BLOOD") &&
               !line.contains("PLACE") &&
               !line.contains("REPUBLIC") &&
               !line.contains("BANGLADESH") &&
               !line.contains("SIGNATURE");
    }

    private static boolean isBangla(String text) {
        int banglaChars = 0;
        for (char c : text.toCharArray()) {
            if (c >= '\u0980' && c <= '\u09FF') banglaChars++;
        }
        return banglaChars > (text.length() / 2); // Mostly Bangla
    }

    private static String cleanBanglaField(String text, String type) {
        // Remove distorted keywords
        String cleaned = text;
        String[] keywords;
        
        switch (type) {
            case "name":
                keywords = new String[]{"নাম", "ন্যম", "নযম", "সাম", "না", "অম"};
                break;
            case "father":
                keywords = new String[]{"পিতা", "পিত", "পিত্ত", "পিকা", "পিতৃ", "মৃত", "মুত"};
                break;
            case "mother":
                keywords = new String[]{"মাতা", "মাত", "মাতত", "আতা", "মাকা", "থাত", "সাতত", "মৃত", "মুত"};
                break;
            default:
                keywords = new String[]{};
        }

        for (String kw : keywords) {
            cleaned = cleaned.replace(kw, "");
        }
        
        return cleaned.replaceAll("[:：;\\-]", "").trim();
    }
}
