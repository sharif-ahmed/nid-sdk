package citl_nid_sdk.parser.core;

import java.util.List;
import citl_nid_sdk.parser.models.NidData;

/**
 * Base interface for NID format extractors.
 */
public interface NidExtractor {
    NidData extract(List<String> lines, String rawText);
}
