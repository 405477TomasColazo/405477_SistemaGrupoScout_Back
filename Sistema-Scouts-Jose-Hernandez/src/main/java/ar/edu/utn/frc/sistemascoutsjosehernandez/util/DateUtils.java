package ar.edu.utn.frc.sistemascoutsjosehernandez.util;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtils {
    public static String convertDate(String dateString){
        try {
            YearMonth yearMonth = YearMonth.parse(dateString);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES"));
            String result = yearMonth.format(formatter);
            return result.substring(0,1).toUpperCase() + result.substring(1);
        }catch(Exception e){
            throw new IllegalArgumentException("Formato de fecha inválido. Use YYYY-MM", e);
        }
    }
}
