package com.greybox.mediums.utils;

import java.util.HashSet;


public class PinValidator {
    public static boolean isPinValid(String pin) {
        /*  9 */
        if (!pin.matches("\\d{4}")) {
            /* 10 */
            return false;
        }


        /* 14 */
        if (pin.startsWith("19") || pin.startsWith("20")) {
            /* 15 */
            return false;
        }


        /* 19 */
        HashSet<Character> seenDigits = new HashSet<>();
        /* 20 */
        char prevChar = pin.charAt(0);
        /* 21 */
        seenDigits.add(Character.valueOf(prevChar));
        /* 22 */
        boolean hasRepetitive = false;

        /* 24 */
        for (int i = 1; i < pin.length(); i++) {
            /* 25 */
            char currentChar = pin.charAt(i);
            /* 26 */
            if (currentChar == prevChar) {
                /* 27 */
                hasRepetitive = true;
            }
            /* 29 */
            seenDigits.add(Character.valueOf(currentChar));
            /* 30 */
            prevChar = currentChar;
        }

        /* 33 */
        if (hasRepetitive) {
            /* 34 */
            return false;
        }


        /* 38 */
        boolean isProgressive = true;
        /* 39 */
        for (int j = 1; j < pin.length(); j++) {
            /* 40 */
            int digit1 = Character.getNumericValue(pin.charAt(j - 1));
            /* 41 */
            int digit2 = Character.getNumericValue(pin.charAt(j));
            /* 42 */
            if (Math.abs(digit1 - digit2) != 1) {
                /* 43 */
                isProgressive = false;

                break;
            }
        }
        /* 48 */
        if (isProgressive) {
            /* 49 */
            return false;
        }

        /* 52 */
        return true;
    }

    public static void main(String[] args) {
        /* 56 */
        String pin = "1987";
        /* 57 */
        if (isPinValid(pin)) {
            /* 58 */
            System.out.println("Valid PIN");
        } else {
            /* 60 */
            System.out.println("Invalid PIN");
        }
    }
}


/* Location:              C:\Users\User\Downloads\mcp-patch-10082024\mcp-patch-10082024\!\com\greybox\medium\\utils\PinValidator.class
 * Java compiler version: 11 (55.0)
 * JD-Core Version:       1.1.3
 */