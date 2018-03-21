package com.helei.hspace.article;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ArticleService {
   
    public String hello(HttpServletRequest req, HttpServletResponse resp, int times) {
        String result = "kA";
        for (int i = 0; i < times; ++i) {
            result += "hello\n";
        }
        return result;
    } 

}