package com.commlink.citl_nid_sdk.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateConverter {

    public static String convertDate(String inputDate) {

        try {
            SimpleDateFormat inputFormat =
                    new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);

            SimpleDateFormat outputFormat =
                    new SimpleDateFormat("dd-MM-yyyy");

            Date date = inputFormat.parse(inputDate);

            return outputFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
