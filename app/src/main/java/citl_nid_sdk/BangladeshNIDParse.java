package citl_nid_sdk;

public class BangladeshNIDParse {

    public static class NidData {
        public String banglaName;
        public String englishName;
        public String fatherName;
        public String motherName;
        public String dob;
        public String nidNumber;

        @Override
        public String toString() {
            return "Bangla Name: " + banglaName + "\n" +
                    "English Name: " + englishName + "\n" +
                    "Father Name: " + fatherName + "\n" +
                    "Mother Name: " + motherName + "\n" +
                    "Date of Birth: " + dob + "\n" +
                    "NID Number: " + nidNumber;
        }
    }

    public static NidData parse(String ocrText) {
        NidData data = new NidData();

        if (ocrText == null || ocrText.isEmpty()) return data;

        String[] lines = ocrText.split("\\n");

        // Step 1: Find Bangla and English Names
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            // Bangla Name: Usually first line after ন্যম
            if (line.equals("ন্যম") || line.equals("নাম")) {
                if (i + 1 < lines.length) data.banglaName = lines[i + 1].trim();
            }

            // English Name: after \Name or Name keyword
            if (line.equalsIgnoreCase("Name") || line.equalsIgnoreCase("\\Name")) {
                if (i + 1 < lines.length) data.englishName = lines[i + 1].trim();
            }
        }

        // Step 2: Extract Father Name
        data.fatherName = extractFatherName(lines);

        // Step 3: Extract Mother Name
        data.motherName = extractMotherName(lines);

        // Step 4: Extract DOB
        data.dob = extractDOB(lines);

        // Step 5: Extract NID Number (last numeric line)
        data.nidNumber = extractNidNumber(lines);

        return data;
    }

    // ------------------- Helper Methods -------------------

    static String extractFatherName(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.equalsIgnoreCase("Name") || line.equalsIgnoreCase("\\Name")) {
                // skip English Name line
                int nextLine = i + 2;
                if (nextLine < lines.length) {
                    String candidate = lines[nextLine].trim();
                    if (isBangla(candidate)) return candidate;
                }
            }
        }
        return "";
    }

    static String extractFatherName(String ocrText) {
        String[] lines = ocrText.split("\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.equalsIgnoreCase("Name") || line.equalsIgnoreCase("\\Name")) {
                // skip English Name line
                int nextLine = i + 2;
                if (nextLine < lines.length) {
                    String candidate = lines[nextLine].trim();
                    if (isBangla(candidate)) return candidate;
                }
            }
        }
        return "";
    }

    static String extractMotherName(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.toLowerCase().contains("date of birth") || line.contains("জন্ম তারিখ")) {
                // take previous Bangla line
                for (int j = i - 1; j >= 0; j--) {
                    String candidate = lines[j].trim();
                    if (isBangla(candidate)) return candidate;
                }
            }
        }
        return "";
    }

    static String extractMotherName(String ocrText) {
        String[] lines = ocrText.split("\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.toLowerCase().contains("date of birth") || line.contains("জন্ম তারিখ")) {
                // take previous Bangla line
                for (int j = i - 1; j >= 0; j--) {
                    String candidate = lines[j].trim();
                    if (isBangla(candidate)) return candidate;
                }
            }
        }
        return "";
    }

    private static String extractDOB(String[] lines) {
        for (String line : lines) {
            if (line.toLowerCase().contains("date of birth") || line.contains("জন্ম তারিখ")) {
                return line.replace("Date of Birth", "").replace("জন্ম তারিখ", "").trim();
            }
        }
        return "";
    }

    private static String extractNidNumber(String[] lines) {
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (line.matches(".*\\d{6,}.*")) { // contains at least 6 digits
                return line.replaceAll("[^0-9]", "");
            }
        }
        return "";
    }

    // Bangla character check
    private static boolean isBangla(String input) {
        for (char c : input.toCharArray()) {
            if (c >= '\u0980' && c <= '\u09FF') return true;
        }
        return false;
    }

    // ------------------- Test -------------------
    public static void main(String[] args) {
        String ocrText = "গণপ্রজাতন্ত্রী বাংলাদেশ সরকার\n" +
                "Govemment of the People's Republic of Bangladesh\n" +
                "জাতীয় পরিচয়পত্র/ National IDCard\n" +
                "ন্যম\n" +
                "শরীফ আহমেদ\n" +
                "\\Name\n" +
                "SHARIF AHMED\n" +
                "পিতা\n" +
                "সৈয়দ আলম\n" +
                "মাতত\n" +
                "আলেয়া বেগম\n" +
                "Date of Birth 31 Dec 1993\n" +
                "415 264 7311\n" +
                "শারীফ হস NID NO";

        NidData data = parse(ocrText);
        System.out.println(data);
    }
}
