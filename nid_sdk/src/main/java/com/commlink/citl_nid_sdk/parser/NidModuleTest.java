package com.commlink.citl_nid_sdk.parser;

import com.commlink.citl_nid_sdk.parser.models.NidData;

public class NidModuleTest {
    public static void main(String[] args) {
        String smartOcr = "গণপ্রজাতন্ত্রী বাংলাদেশ সরকার\n" +
                "Govemment of the People's Republic of Bangladesh\n" +
                "জাতীয় পরিচয়পত্র/ National ID Card\n" +
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

        String oldOcr = "গণপ্রজাতন্ত্রী বাংলাদেশ সরকার\n" +
                "Government of the Peoples Republic of Bangladesh\n" +
                "NATIONAL ID CARD / জাতীয় পরিচয় পত্র\n" +
                "অম\n" +
                "নাম: শরীফ আহমেদ\n" +
                "Name: SHARIF AHMED\n" +
                "পিতা; সৈয়দ আলম\n" +
                "মাতা; মুত আলেয়া বেগম\n" +
                "Date of Birth: 31 Dec 1993\n" +
                "ID NO: 19937518780000260";

        System.out.println("=== TESTING MODULAR PARSER ENGINE ===");
        
        System.out.println("\n[1] Testing Smart NID...");
        NidData smartResult = NidParserEngine.parse(smartOcr);
        System.out.println(smartResult);

        System.out.println("\n[2] Testing Old NID...");
        NidData oldResult = NidParserEngine.parse(oldOcr);
        System.out.println(oldResult);
        
        System.out.println("\n=== VERIFICATION COMPLETE ===");
    }
}
