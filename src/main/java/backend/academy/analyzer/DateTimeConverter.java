package backend.academy.analyzer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DateTimeConverter {

    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter ISO_LOCAL_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public static LocalDateTime convertLogDateTime(String dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTime, formatter);
        return zonedDateTime.toLocalDateTime();
    }

    public static LocalDateTime convertArgumentDateTime(String dateTime) {
        LocalDateTime result = tryParseDateTime(dateTime);
        if (result == null) {
            result = tryParseDate(dateTime);
        }

        if (result == null) {
            throw new DateTimeParseException("Invalid date format: " + dateTime, dateTime, 0);
        }
        return result;
    }

    private static LocalDateTime tryParseDateTime(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, ISO_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static LocalDateTime tryParseDate(String date) {
        try {
            LocalDate localDate = LocalDate.parse(date, ISO_LOCAL_DATE_FORMATTER);
            return localDate.atStartOfDay();
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
