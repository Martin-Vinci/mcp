package com.greybox.mediums.utils;

import com.greybox.mediums.models.ErrorData;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class StringUtil {
    public static String generateRandomString(int numberOfCharacters) {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789";
        StringBuilder sb = new StringBuilder(numberOfCharacters);
        for (int i = 0; i < numberOfCharacters; i++) {
            int index = (int) (AlphaNumericString.length() * Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }
        return sb.toString();
    }

    public static String generateRandomNumber(int numberOfCharacters) {
        String AlphaNumericString = "0123456789";
        StringBuilder sb = new StringBuilder(numberOfCharacters);
        for (int i = 0; i < numberOfCharacters; i++) {
            int index = (int) (AlphaNumericString.length() * Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }
        return sb.toString();
    }

    public static String toCurrencyFormat(BigDecimal value) {
        if (value == null)
            return null;
        if (value.compareTo(BigDecimal.ZERO) == 0)
            return "0.00";

        DecimalFormat df = new DecimalFormat("#,###.00");
        return df.format(value);
    }

    public static String toAmountDelimiter(BigDecimal value) {
        if (value == null)
            return null;
        if (value.compareTo(BigDecimal.ZERO) == 0)
            return "0";

        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(value);
    }

    public static String padLeftZeros(String inputString, int length) {
        if (inputString.length() >= length) {
            return inputString;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - inputString.length()) {
            sb.append('0');
        }
        sb.append(inputString);

        return sb.toString();
    }

    private static boolean isNullEmpty(String str) {
        // check if string is null
        if (str == null) {
            return true;
        }
        // check if string is empty
        else if (str.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public static String getTransferType(String drAcctNo, String crAcctNo) {
        if (drAcctNo.length() <= 12 && crAcctNo.length() <= 12)
            return "P2P";
        if (drAcctNo.length() > 12 && crAcctNo.length() > 12)
            return "GL2GL";
        if (drAcctNo.length() > 12 && crAcctNo.length() <= 12)
            return "GL2DP";
        if (drAcctNo.length() <= 12 && crAcctNo.length() > 12)
            return "DP2GL";
        else
            return null;
    }

    public static String formatPhoneNumber(String phoneNo) throws MediumException {
        String response;

        // Validate input: check for null or empty
        if (isNullEmpty(phoneNo))
            throw new MediumException(ErrorData.builder()
                    .code("-99")
                    .message("Invalid mobile phone number: " + phoneNo + ", format should be 256XXXXXXXXX").build());

        phoneNo = phoneNo.trim();

        // Check if the phone number starts with '07' or '7' for Ugandan numbers
        String phoneCode = phoneNo.substring(0, 1);  // Check for '7' instead of '07'

        if (phoneCode.equals("7")) {
            // If the phone number starts with '7' and has 9 digits, format as '256XXXXXX...'
            if (phoneNo.length() == 9) {
                response = "256" + phoneNo;  // Prepend '256' to the phone number
                return response;
            } else {
                throw new MediumException(ErrorData.builder()
                        .code("-99")
                        .message("Invalid mobile phone number: " + phoneNo + ", format should be 256XXXXXXXXX").build());
            }
        } else {
            // Continue with the existing checks for numbers starting with '07' or '25'
            if (phoneNo.startsWith("07")) {
                if (phoneNo.length() == 10) {
                    response = "256" + phoneNo.substring(1);
                    return response;
                } else {
                    throw new MediumException(ErrorData.builder()
                            .code("-99")
                            .message("Invalid mobile phone number: " + phoneNo + ", format should be 256XXXXXXXXX").build());
                }
            } else if (phoneNo.startsWith("25")) {
                if (phoneNo.length() == 12) {
                    return phoneNo;
                } else {
                    throw new MediumException(ErrorData.builder()
                            .code("-99")
                            .message("Invalid mobile phone number: " + phoneNo + ", format should be 256XXXXXXXXX").build());
                }
            } else {
                throw new MediumException(ErrorData.builder()
                        .code("-99")
                        .message("Invalid mobile phone number: " + phoneNo + ", format should be 256XXXXXXXXX").build());
            }
        }
    }


}
