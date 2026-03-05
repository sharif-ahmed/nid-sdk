package citl_nid_sdk.parser.core;

import citl_nid_sdk.parser.models.NidData;
import java.util.regex.Pattern;

/**
 * Validates extracted NID data for consistency and completeness.
 */
public class NidValidator {

    public static void validate(NidData data) {
        boolean valid = true;

        if (data.getNameBn().isEmpty() || data.getNameBn().length() < 5) valid = false;
        if (data.getNameEn().isEmpty() || data.getNameEn().length() < 5) valid = false;
        if (data.getNidNumber().isEmpty() || !isValidNid(data.getNidNumber())) valid = false;
        if (data.getDateOfBirth().isEmpty()) valid = false;

        data.setValid(valid);
    }

    private static boolean isValidNid(String nid) {
        int len = nid.length();
        return len == 10 || len == 13 || len == 17;
    }
}
