package decimal.apigateway.service.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class BuiltInUtility {
    private BuiltInUtility() {
    }

    public static String getCurrentTimestamp() {
        return LocalDateTime.now(ZoneOffset.UTC).toString();
    }

    public static String getCurrentTime() {
        return LocalTime.now().toString();
    }

    public static String getCurrentDate() {
        return LocalDate.now().toString();
    }

    public static String getFormattedCurrentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.now().format(formatter);
    }

    public static String getRandomNumber(int digit) {
        String saltChars = "1234567890";
        return getRandom(saltChars, digit);
    }

    public static String getRandomString(int digit) {
        String saltChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        return getRandom(saltChars, digit);

    }

    /***
     * Common method to generate random String according to given chars of given digits
     *
     * @param digit - length of the string
     * @param saltChars - salt string containing supporting chars in random string
     *
     * @return return Random string
     */
    @SuppressWarnings("WeakerAccess")
    public static String getRandom(String saltChars, int digit) {
        StringBuilder salt = new StringBuilder();
        SecureRandom rnd = new SecureRandom();
        while (salt.length() < digit) {
            int index = (int) (rnd.nextFloat() * saltChars.length());
            salt.append(saltChars.charAt(index));
        }
        return salt.toString();

    }

    /**
     * @return Current timestamp object
     */
    public static Timestamp getCurrentTimeStampObject() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * Method to concat {@code String[]} with specified delimiter
     *
     * @param delimiter - what delimeter to be used to concat String
     * @param args      - {@code String...args} array of Strings which will be concatenated
     * @return Concatenated String
     */
    @SuppressWarnings("unused")
    public static String join(String delimiter, String... args) {
        return String.join(delimiter, args);
    }

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    public static List<URL> getUrls(String baseClassPath) throws IOException {
        List<URL> urls = new ArrayList<>();
        Files.walkFileTree(Paths.get(baseClassPath), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toFile().getName().endsWith(".jar")) {
                    urls.add(new URL("jar:file:/" + file.toFile().getAbsolutePath() + "!/"));
                }
                return super.visitFile(file, attrs);
            }
        });

        return urls;
    }


    public static List<String> getRegexPattern(String key) {
        List<String> regex = new ArrayList<>();
        regex.add("(" + "\"" + key + "\"" + ")(\\s*+:\\s*+)(\".+?\")");
        regex.add("(" + key + ")(\\s*=\\s*)(.+?)[\\s{ 0 , }?]");

        return regex;
    }

    public static String simpleDateFormat(){
        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String strDate = sdf.format(calendar.getTime());
        return strDate;
    }

    public static String stringFormat(String s){
       if(s!=null && !s.equals("")) {
           s = s.toLowerCase();
           s = s.substring(0, 1).toUpperCase() + s.substring(1);
       }
        return s;
    }
}
