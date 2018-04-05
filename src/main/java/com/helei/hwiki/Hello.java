package com.helei.hwiki;

import javax.servlet.http.HttpServletRequest;
import com.helei.hspace.reflect.*;

@Resource
public class Hello {

    @Route("/dajia/*")
    public String haha(HttpServletRequest req) {
        return "hello world from hwiki."; 
    }

}
