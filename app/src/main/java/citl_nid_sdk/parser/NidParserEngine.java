package citl_nid_sdk.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import citl_nid_sdk.parser.core.CorrectionLayer;
import citl_nid_sdk.parser.core.NidExtractor;
import citl_nid_sdk.parser.core.NidValidator;
import citl_nid_sdk.parser.extractors.OldNidExtractor;
import citl_nid_sdk.parser.extractors.SmartNidExtractor;
import citl_nid_sdk.parser.models.NidCardType;
import citl_nid_sdk.parser.models.NidData;

/**
 * Facade engine for Bangladesh NID parsing.
 */
public class NidParserEngine {

    public static NidData parse(String rawText) {
        String normalized = CorrectionLayer.normalize(rawText);
        List<String> lines = toCleanLines(normalized);
        
        NidCardType type = detectType(normalized);
        NidExtractor extractor = getExtractor(type);
        
        //NidData data = extractor.extract(lines, normalized);
        NidData data = extractor.extract(lines, rawText);

        // Always extract these globally as they are pattern-based
        //data.setNidNumber(extractNidNumber(normalized));
        //data.setDateOfBirth(extractDob(normalized));
        
        //NidValidator.validate(data);
        return data;
    }

    private static NidCardType detectType(String text) {
        if (text.contains("Smart") || text.contains("National ID Card")) {
            return NidCardType.SMART;
        } else if (text.contains("NATIONAL ID CARD") || text.contains("জাতীয় পরিচয় পত্র")) {
            return NidCardType.OLD;
        }
        // Heuristic fallback
        return text.length() < 500 ? NidCardType.SMART : NidCardType.OLD;
    }

    private static NidExtractor getExtractor(NidCardType type) {
        if (type == NidCardType.OLD) return new OldNidExtractor();
        return new SmartNidExtractor();
    }

    private static List<String> toCleanLines(String text) {
        List<String> list = new ArrayList<>();
        for (String s : text.split("\\n")) {
            String t = s.trim();
            if (!t.isEmpty()) list.add(t);
        }
        return list;
    }

    private static String extractNidNumber(String text) {
        Pattern p = Pattern.compile("(\\d[\\s\\d]{9,18}\\d)");
        Matcher m = p.matcher(text);
        while (m.find()) {
            String match = m.group().replaceAll("\\s", "");
            if (match.length() == 10 || match.length() == 13 || match.length() == 17) return match;
        }
        return "";
    }

    private static String extractDob(String text) {
        Pattern p = Pattern.compile("(\\d{1,2}\\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+\\d{4})", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        return m.find() ? m.group() : "";
    }
}
