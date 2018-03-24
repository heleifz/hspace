package com.helei.hspace.util;

public class StringUtil
{
    public static String toCamel(String s) {
        if (s.length() == 0) {
            return s; 
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

}