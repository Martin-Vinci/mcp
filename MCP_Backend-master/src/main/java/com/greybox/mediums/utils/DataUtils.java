package com.greybox.mediums.utils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class DataUtils {

    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
    private static SimpleDateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat isoTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private SimpleDateFormat dateFormatterSlash = new SimpleDateFormat("dd/MM/yyyy");

    public static java.sql.Timestamp isoToSQLDateTime(String string) {
        Date parsedDate;
        try {
            if (string == null) {
                return null;
            } else if (string.equals("")) {
                return null;
            }
            if (string.length() == 10)
                parsedDate = isoFormatter.parse(string);
            else
                parsedDate = isoTimeFormatter.parse(string);
        } catch (ParseException e) {
            // LogUtils.getLogger().error("====================== convertToDate Date string
            // " + string);
            // LogUtils.getLogger().error(e);
            return null;
        }
        return new java.sql.Timestamp(parsedDate.getTime());
    }

    public static java.sql.Date isoToSQLDate(String string) {
        Date parsedDate;
        try {
            if (string == null) {
                return null;
            } else if (string.equals("")) {
                return null;
            }
            string = string.replaceAll("/", "-");
            parsedDate = isoFormatter.parse(string);
        } catch (ParseException e) {
            // LogUtils.getLogger().error("====================== convertToDate Date string
            // " + string);
            // LogUtils.getLogger().error(e);
            return null;
        }
        return new java.sql.Date(parsedDate.getTime());
    }

    public static Date convertToDate(String string) {
        Date parsedDate;
        try {
            parsedDate = dateFormatter.parse(string);
        } catch (ParseException e) {
//            LogUtils.getLogger().error("====================== convertToDate Date string " + string);
//            LogUtils.getLogger().error(e);
            parsedDate = new Date();
        }
        return parsedDate;
    }

    public static String toString(LocalDate date) {
        String parsedDate = null;
        try {
            parsedDate = isoFormatter.format(date);
        } catch (Exception e) {
            //LogUtils.getLogger().error(e);
        }

        System.out.println("=========================== Date to String is: " + parsedDate);

        return parsedDate;
    }

    public static String toTimeString(Date date) {
        String parsedDate = null;
        try {
            parsedDate = isoTimeFormatter.format(date);
        } catch (Exception e) {
            //LogUtils.getLogger().error(e);
        }
        System.out.println("=========================== Date Time to String is: " + parsedDate);
        return parsedDate;
    }

    public static XMLGregorianCalendar toGregorianDate(String dateString) throws ParseException, DatatypeConfigurationException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(dateString, formatter);
        XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(localDate.toString());
        return xmlGregorianCalendar;
    }

    public static Calendar convertToCalendar(String rawDateString) {
        Calendar calendar = Calendar.getInstance();
        try {
            Date sysDate = convertToDate(rawDateString);
            calendar.setTime(sysDate);
        } catch (Exception e) {
            //LogUtils.getLogger().error(e);
        }
        return calendar;
    }

    public static java.sql.Date getCurrentDate() {
        Date today = new Date();
        java.sql.Date date = new java.sql.Date(today.getTime());
        return date;
    }

    public static java.sql.Timestamp getCurrentTimeStamp() {
        Date today = new Date();
        java.sql.Timestamp date = new java.sql.Timestamp(today.getTime());
        return date;
    }

    public static BigDecimal getPrimitiveBigDecimal(BigDecimal value) {
        value = value == null ? BigDecimal.ZERO : value;
        return value;
    }

    public static LocalDate getLastDayOfMonth() {
        return LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
    }

    public static LocalDate getFirstDayOfMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }
}
