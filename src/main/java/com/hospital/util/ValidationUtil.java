package com.hospital.util;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class ValidationUtil {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+()\\-\s]{7,20}$");

    public static boolean validateEmail(String email) {
        if (email == null || email.isBlank())
            return false;
        return EMAIL.matcher(email).matches();
    }

    public static boolean validatePhone(String phone) {
        if (phone == null || phone.isBlank())
            return false;
        return PHONE.matcher(phone).matches();
    }

    public static boolean validateRequired(String value) {
        return value != null && !value.isBlank();
    }

    public static boolean validateDateNotPast(LocalDate date) {
        if (date == null)
            return false;
        return !date.isBefore(LocalDate.now());
    }

    public static boolean validateDatePast(LocalDate date) {
        if (date == null)
            return false;
        return date.isBefore(LocalDate.now());
    }
}
