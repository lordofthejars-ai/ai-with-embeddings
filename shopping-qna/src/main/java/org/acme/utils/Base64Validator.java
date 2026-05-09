package org.acme.utils;

import java.util.Base64;

public class Base64Validator {

    private static final String BASE64_REGEX =
        "^(?:[A-Za-z0-9+/]{4})*" +
            "(?:[A-Za-z0-9+/]{2}==|" +
            "[A-Za-z0-9+/]{3}=)?$";

    public static boolean isBase64(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return value.matches(BASE64_REGEX);
    }

}
