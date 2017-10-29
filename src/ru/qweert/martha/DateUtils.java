package ru.qweert.martha;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final SimpleDateFormat DEFAULT_DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);

    public static String formatDate(String date) {
        if (date == null || date.equals("")) {
            return "";
        }
        Date parsed = null;
        try {
            parsed = DEFAULT_DATE_FORMATTER.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        return new SimpleDateFormat("dd.MM.yyyy").format(parsed);
    }
}
