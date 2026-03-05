package citl_nid_sdk.parser.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles OCR noise reduction and fuzzy matching of Bangla keywords.
 */
public class CorrectionLayer {

    private static final Map<String, String[]> FUZZY_LABELS = new HashMap<>();

    static {
        FUZZY_LABELS.put("নাম", new String[]{"ন্যম", "নযম", "সাম", "না", "অম"});
        FUZZY_LABELS.put("পিতা", new String[]{"পিত", "পিত্ত", "পিকা", "পিতৃ", "পিক"});
        FUZZY_LABELS.put("মাতা", new String[]{"মাত", "মাতত", "আতা", "মাকা", "থাত", "সাতত"});
        FUZZY_LABELS.put("মৃত", new String[]{"মুত", "মৃত্ত"});
    }

    public static String normalize(String text) {
        if (text == null) return "";
        return text.replace("\r", "")
                .replaceAll("[|]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static String cleanField(String text, String... keywordsToStrip) {
        if (text == null) return "";
        String cleaned = text;
        
        // Strip common label distortions first
        for (Map.Entry<String, String[]> entry : FUZZY_LABELS.entrySet()) {
            for (String distortion : entry.getValue()) {
                cleaned = cleaned.replace(distortion, "");
            }
            cleaned = cleaned.replace(entry.getKey(), "");
        }

        // Strip custom keywords provided
        for (String kw : keywordsToStrip) {
            cleaned = cleaned.replace(kw, "");
        }

        return cleaned.replaceAll("[:：;\\-]", "").trim();
    }

    public static boolean containsFuzzy(String line, String baseLabel) {
        if (line.contains(baseLabel)) return true;
        String[] variations = FUZZY_LABELS.get(baseLabel);
        if (variations != null) {
            for (String v : variations) {
                if (line.contains(v)) return true;
            }
        }
        return false;
    }
}
