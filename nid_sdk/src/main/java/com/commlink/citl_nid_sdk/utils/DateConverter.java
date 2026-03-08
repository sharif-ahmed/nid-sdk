package com.commlink.citl_nid_sdk.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateConverter {

    public static String convertDate(String inputDate) {
        if (inputDate == null || inputDate.isEmpty())
            return null;

        String[] formats = { "dd MMM yyyy", "dd/MM/yyyy", "dd-MM-yyyy" };
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);

        for (String format : formats) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(format, Locale.ENGLISH);
                Date date = inputFormat.parse(inputDate);
                if (date != null) {
                    return outputFormat.format(date);
                }
            } catch (ParseException ignored) {
            }
        }
        return null; // Return null if all formats fail
    }
}
