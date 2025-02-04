package com.greybox.mediums.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class NumberToWord {

    private static final String[] units = { "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve",
            "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen" };

    private static final String[] tens = {
            "",        // 0
            "",        // 1
            "Twenty",  // 2
            "Thirty",  // 3
            "Forty",   // 4
            "Fifty",   // 5
            "Sixty",   // 6
            "Seventy", // 7
            "Eighty",  // 8
            "Ninety"   // 9
    };

    public static String convert(final long n) {
        if (n < 0) {
            return "Minus " + convert(-n);
        }

        if (n < 20) {
            return units[(int) n];
        }

        if (n < 100) {
            return tens[(int) n / 10] + ((n % 10 != 0) ? " " : "") + units[(int) n % 10];
        }

        if (n < 1000) {
            return units[(int) n / 100] + " Hundred" + ((n % 100 != 0) ? " " : "") + convert(n % 100);
        }

        if (n < 1000000) {
            return convert(n / 1000) + " Thousand" + ((n % 1000 != 0) ? " " : "") + convert(n % 1000);
        }

        if (n < 1000000000) {
            return convert(n / 1000000) + " Million" + ((n % 1000000 != 0) ? " " : "") + convert(n % 1000000);
        }

        if (n < 1000000000000L) {
            return convert(n / 1000000000) + " Billion" + ((n % 1000000000 != 0) ? " " : "") + convert(n % 1000000000);
        }

        return "";
    }

    public static String convertToWords(BigDecimal n) {
        n = n.setScale(2, RoundingMode.HALF_EVEN);
        String[] parts = String.valueOf(n).split("\\.");
        String words = convert(Long.parseLong(parts[0])) + " Shillings";
        if (parts.length > 1) {
            String cents = convert(Long.parseLong(parts[1]));
            if (!cents.isEmpty()) {
                words += " and " + cents + " Cents";
            }
        }
        return words + " only";
    }

    public static void main(String[] args) {
        BigDecimal amount = new BigDecimal(200000000000.234);
        System.out.println("Amount in words: " + convertToWords(amount));
    }
}