package com.commlink.citl_nid_sdk.parser.extractors;

import java.util.List;

import com.commlink.citl_nid_sdk.core.BangladeshNidParser;
import com.commlink.citl_nid_sdk.parser.core.NidExtractor;
import com.commlink.citl_nid_sdk.parser.models.NidData;

/**
 * Extractor for Smart NID Card (New).
 */
public class SmartNidExtractor implements NidExtractor {

    @Override
    public NidData extract(List<String> lines, String rawText) {
        NidData data = new NidData();
        data.setCardType("SMART");

        /*int engNameIndex = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (isEnglishNameAnchor(lines.get(i))) {
                data.setNameEn(lines.get(i));
                engNameIndex = i;
                break;
            }
        }

        // Backward for Name BN
        if (engNameIndex != -1) {
            for (int i = engNameIndex - 1; i >= 0; i--) {
                String line = lines.get(i);
                if (isMostlyBangla(line) && line.length() >= 5 && !CorrectionLayer.containsFuzzy(line, "নাম")) {
                    data.setNameBn(CorrectionLayer.cleanField(line));
                    break;
                }
            }
        }

        // Forward for Father/Mother
        int start = (engNameIndex != -1) ? engNameIndex + 1 : 0;
        int fatherIdx = -1;
        for (int i = start; i < lines.size(); i++) {
            String line = lines.get(i);
            if (CorrectionLayer.containsFuzzy(line, "পিতা")) {
                data.setFatherName(extractValue(lines, i, "father"));
                fatherIdx = i + (line.contains(":") ? 0 : 1);
                break;
            }
        }

        int mStart = (fatherIdx != -1) ? fatherIdx + 1 : start;
        for (int i = mStart; i < lines.size(); i++) {
            String line = lines.get(i);
            if (CorrectionLayer.containsFuzzy(line, "মাতা")) {
                data.setMotherName(extractValue(lines, i, "mother"));
                break;
            }
        }*/

        BangladeshNidParser.NidData nidData = BangladeshNidParser.parse(rawText);
        data.setNameEn(nidData.name);
        data.setNameBn(nidData.nameBangla);
        data.setDateOfBirth(nidData.dob);
        data.setFatherName(nidData.fatherNameBangla);
        data.setMotherName(nidData.motherNameBangla);
        data.setNidNumber(nidData.nidNumber);
        return data;
    }

    /*private String extractValue(List<String> lines, int index, String type) {
        String line = lines.get(index);
        if (line.contains(":") || line.contains(";") || line.contains("-")) {
            return CorrectionLayer.cleanField(line, type);
        } else if (index + 1 < lines.size()) {
            return CorrectionLayer.cleanField(lines.get(index + 1), type);
        }
        return "";
    }

    private boolean isEnglishNameAnchor(String line) {
        return line.matches("^[A-Z\\s]{5,}$") && !line.contains("NATIONAL") && !line.contains("SIGNATURE");
    }

    private boolean isMostlyBangla(String text) {
        int bangla = 0;
        for (char c : text.toCharArray()) if (c >= '\u0980' && c <= '\u09FF') bangla++;
        return bangla > (text.length() / 2);
    }*/
}
