package com.helei.hspace.router;

import java.util.HashMap;
import java.util.Map;

public class MatchResult {
    private String tag = "";
    // capture_name -> [begin, end)
    private HashMap<String, String> captures = new HashMap<>();

    public MatchResult(String tag, HashMap<String, String> captures) {
        this.tag = tag;
        this.captures = captures;
    }
    
    public String getTag() {
        return tag;
    }
    
    public Map<String, String> getCaptures() {
        return captures;
    }
}
