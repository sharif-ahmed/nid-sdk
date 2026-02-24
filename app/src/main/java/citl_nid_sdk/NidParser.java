package citl_nid_sdk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NidParser {

    public static String getName(String text) {

        Pattern pattern = Pattern.compile("Name\\s*\\n?\\s*([A-Z ]+)");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return "";
    }


    public static String getDOB(String text) {

        Pattern pattern = Pattern.compile("Date of Birth\\s*([0-9]{1,2}\\s\\w{3}\\s[0-9]{4})");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }


    public static String getNidNumber(String text) {

        Pattern pattern = Pattern.compile("\\d{3}\\s\\d{3}\\s\\d{4}");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group();
        }

        return "";
    }


    public static boolean containsGovText(String text) {

        return text.contains("Government of the People's Republic of Bangladesh");
    }


    public static boolean containsNationalId(String text) {

        return text.contains("National ID Card");
    }

}
