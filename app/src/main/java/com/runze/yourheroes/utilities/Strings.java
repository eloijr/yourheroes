package com.runze.yourheroes.utilities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.regex.Pattern;

/**
 * Created by Eloi Jr on 06/01/2015.
 */
public class Strings {
    public static final double KIB = 2L<<9;
    public static final double MIB = 2L<<19;
    public static final double GIB = 2L<<29;

    public static final double K = 1000;
    public static final double M = 1000000;
    public static final double B = 1000000000;

    private static char[] HEXCHARS = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};

    private static final String base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int[] base32Lookup = {
            0xFF, 0xFF, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F,
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
            0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E,
            0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16,
            0x17, 0x18, 0x19, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
            0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E,
            0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16,
            0x17, 0x18, 0x19, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
    };

    public static final Pattern pattern = Pattern.compile("(http[s]?|file|ftp)://[\\w\\.\\-#~%\\?=\\&&&[^,\\s]]*[/]*|\\bwww\\.[\\w\\.\\-#~%\\?=\\&&&[^,\\s]]*[/]*",Pattern.CASE_INSENSITIVE);



    public static String fixQuotes(String in) {
        if ( in == null ) {
            return in;
        } else {
            StringBuffer buffer = new StringBuffer(in);
            for ( int i = 0; i < in.length(); i ++) {
                if ( in.charAt(i) == 8217 || in.charAt(i) == 8216) {
                    buffer.setCharAt(i,'\'');
                } else if ( in.charAt(i) == 8220 || in.charAt(i) == 8221 ) {
                    buffer.setCharAt(i,'\"');
                }
            }

            return buffer.toString();
        }
    }


    public static String getFileName(String url, String defExt) {
        return  getFileName(url, false, defExt);
    }

    public static String getFileName(String url, boolean hashPrefix, String defExt) {
        String fileName = url.replaceFirst(pattern.pattern(),"");

        int index = fileName.indexOf('?');
        if ( index != -1 ) {
            fileName = fileName.substring(0,index);
        }

        index = fileName.lastIndexOf('/');
        if ( index != -1 ) {
            fileName = fileName.substring(index+1);
        }

        if ( fileName.trim().length() == 0 ) {
            fileName = Integer.toHexString(url.hashCode()).toLowerCase();
        }
        else if ( hashPrefix ) {
            fileName = Integer.toHexString(url.hashCode()).toLowerCase() + "_" + fileName.trim();
        }

        if ( defExt != null ) {
            int dotIndex = fileName.lastIndexOf('.');
            if ( dotIndex == -1 ) {
                fileName += defExt;
            }
            else if ( dotIndex == (fileName.length()-1) ) {
                fileName += defExt.substring(1);
            }
        }

        return safeName(fileName);
    }


    public static String safeName(String name) {
        return name.trim().replaceAll("[\\+%#\\?\\&\\>\\<\\*\\: \\/|]","_").replace('\'','_').replace('\"', '_');
    }

    public static String safeName(String name, boolean spaces) {
        return name.trim().replaceAll("[^a-zA-Z0-9\\., ]","").trim();
    }

    public static String hexEncode(byte[] bytes) {
        char[] result = new char[bytes.length*2];
        int b;
        for (int i = 0, j = 0; i < bytes.length; i++) {
            b = bytes[i] & 0xff;
            result[j++] = HEXCHARS[b >> 4];
            result[j++] = HEXCHARS[b & 0xf];
        }
        return new String(result);
    }

    public static int getSoundexCode(char c) {
        switch ((int) c) {
            case (int) 'A' :
                return -1;
            case (int) 'B' :
                return 1;
            case (int) 'C' :
                return 2;
            case (int) 'D' :
                return 3;
            case (int) 'E' :
                return -1;
            case (int) 'F' :
                return 1;
            case (int) 'G' :
                return 2;
            case (int) 'H' :
                return -1;
            case (int) 'I' :
                return -1;
            case (int) 'J' :
                return 2;
            case (int) 'K' :
                return 2;
            case (int) 'L' :
                return 4;
            case (int) 'M' :
                return 5;
            case (int) 'N' :
                return 5;
            case (int) 'O' :
                return -1;
            case (int) 'P' :
                return 1;
            case (int) 'Q' :
                return 2;
            case (int) 'R' :
                return 6;
            case (int) 'S' :
                return 2;
            case (int) 'T' :
                return 3;
            case (int) 'U' :
                return -1;
            case (int) 'V' :
                return 1;
            case (int) 'W' :
                return -1;
            case (int) 'X' :
                return 2;
            case (int) 'Y' :
                return -1;
            case (int) 'Z' :
                return 2;
            default :
                return -1;
        }
    }

    public static String getSoundexCode(String s) {
        s = s.toUpperCase();
        StringBuffer soundex = new StringBuffer(4);
        soundex.append(s.charAt(0));

        int code, c = 1;
        for (int i = 1, len = s.length(); i < len && c < 4; i++) {
            if (s.charAt(i) != s.charAt(i - 1)) {
                if ((code = getSoundexCode(s.charAt(i))) != -1) {
                    soundex.append(code);
                    c++;
                }
            }
        }

        for (; c < 4; c++) {
            soundex.append("0");
        }

        return soundex.toString();
    }

    public static String join(String[] sa, int start, int stop, String delim)
    {
        if (sa != null) {
            if ( stop > sa.length-1 ) {
                stop = sa.length-1;
            }

            if ( start <= stop ) {
                StringBuffer buffer = new StringBuffer(sa[start].toString());

                start++;
                while (start <= stop) {
                    buffer.append(delim);
                    buffer.append(sa[start++]);
                }

                return buffer.toString();
            }
        }

        return "";
    }

    public static String[] split(String s, int i) {
        int len = s.length();
        String[] sa = new String[len / i + (((len % i) > 0) ? 1 : 0)];
        for (int j = 0; j < (sa.length - 1) || ((sa[j] = s.substring(j * i)) == null); sa[j] = s.substring(j * i, j++ * i + i));
        return sa;
    }

    public static boolean isNull(String s) {
        return (s == null || s.trim().length()== 0 );
    }

    public static String ifNull(String s, String def) {
        return (Strings.isNull(s))? def : s;
    }

    public static String ifNull(Object s, String def) {
        return (Strings.isNull(String.valueOf(s)))? def : s.toString();
    }

    public static String getSizeString(long number) {
        String suffix;
        double sz;

        if ( number > GIB ) {
            //gb
            sz = ( number / GIB );
            suffix = "GB";
        } else if ( number > MIB ) {
            //mb
            sz = ( number / MIB );
            suffix = "MB";
        } else if ( number > KIB ) {
            //kb
            sz = number / KIB;
            suffix = "KB";
        } else {
            sz = number;
            suffix = "b";
        }

        return (Math.ceil(sz*100.00)/100.0) + suffix;
    }


    public static String getMagnitude(long number) {
        String suffix;
        double sz;

        if ( number > B ) {
            //gb
            sz = ( number / B );
            suffix = "B";
        } else if ( number > M ) {
            //mb
            sz = ( number / M );
            suffix = "M";
        } else if ( number > K ) {
            //kb
            sz = number / K;
            suffix = "K";
        } else {
            sz = number;
            suffix = "";
        }

        return NumberFormat.getNumberInstance().format((Math.ceil(sz*100.00)/100.0)) + suffix;
    }

    public static String padNumber(int number, int len) {
        String s = String.valueOf(number);
        if ( s.length() < len ) {
            for ( int i = 0, stop = len - s.length(); i < stop; i++ ) {
                s = "0" + s;
            }
        }


        return s;
    }


    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}
