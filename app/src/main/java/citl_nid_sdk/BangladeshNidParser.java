package citl_nid_sdk;

import java.util.regex.*;

public class BangladeshNidParser {

    public static class NidData {

        public String name = "";

        public String dob = "";

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

        return data;
    }


    // Normalize OCR text
    static String normalize(String text){

        return text
                .replace("Namie","Name")
                .replace("Narne","Name")
                .replace("Nane","Name")
                .replace("Govermment","Government")
                .replace("Peoples","People's")
                .replaceAll("[|]"," ")
                .trim();
    }



    // Validate Bangladesh NID
    static boolean isValidNidCard(String text){

        return text.contains("Government of the People's Republic of Bangladesh")
                || text.contains("National ID Card");
    }



    // Extract NID Number
    static String extractNid(String text){

        Pattern pattern = Pattern.compile("\\b\\d{3}\\s?\\d{3}\\s?\\d{4}\\b");

        Matcher matcher = pattern.matcher(text);

        if(matcher.find())

            return matcher.group();

        return "";
    }



    // Extract DOB
    static String extractDOB(String text){

        Pattern pattern = Pattern.compile(
                "\\b\\d{1,2}\\s(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s\\d{4}\\b"
        );

        Matcher matcher = pattern.matcher(text);

        if(matcher.find())

            return matcher.group();

        return "";
    }



    // Extract Name (Most Accurate Method)
    static String extractName(String text){

        String[] lines = text.split("\\n");

        for(int i=0;i<lines.length;i++){

            String line = lines[i].trim();

            if(line.equalsIgnoreCase("Name")){

                if(i+1 < lines.length){

                    String nameLine = lines[i+1].trim();

                    if(nameLine.matches("[A-Z ]{3,}"))

                        return nameLine;

                }

            }

        }


        // fallback method

        for(String line : lines){

            line = line.trim();

            if(line.matches("[A-Z ]{5,}")
                    && !line.contains("GOVERNMENT")
                    && !line.contains("NATIONAL")
                    && !line.contains("REPUBLIC")){

                return line;

            }

        }

        return "";
    }

}
