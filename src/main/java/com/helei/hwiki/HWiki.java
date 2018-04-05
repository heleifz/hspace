package com.helei.hwiki;

import com.helei.hspace.*;

class HWiki extends Application
{
    public static void main(String[] arg) {
        HWiki app = new HWiki();
        try {
            app.run("8080", 3);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } 
    }
}