package com.helei.hspace.router;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Token {

    // four types of tokens
    public enum Type {
        METHOD,
        TEXT, 
        CAPTURE,
        WILDCARD
    }

    private final Type type;
    // for text : text = text
    // for capture : text = captureName
    // for wildcard : text = *
    // for method : text = ""
    private final String text;

    public Token(Type type, String text) {
        this.type = type;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public Type getType() {
        return type;
    }
    
    // regex based parser :)
    private static final Pattern methodPattern = 
        Pattern.compile("(?:\\[[ ]*([A-Z, ]+)[ ]*\\])?[ ]*((?:/[^?#]+)+)([?#].*)?");

    /**
     * parse url (pattern) into tokenlist
     */
    public static List<Token> tokenize(String pattern) { 

        Matcher matcher = methodPattern.matcher(pattern);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Illegal pattern format."); 
        }
        String methods = matcher.group(1);
        String uri = matcher.group(2);
        String[] uriParts = uri.split("/");
        ArrayList<Token> tokens = new ArrayList<>();
        if (methods != null && methods.length() > 0) {
            // remove additional whitespace
            tokens.add(new Token(Type.METHOD, methods.replace(" ", "")));
        }
        int partNum = uriParts.length;
        for (int i = 1; i < partNum; ++i) {
            String part = uriParts[i];
            if (i + 1 == partNum && part.isEmpty()) {
                // ignore last slash
                break;
            } else if (part.isEmpty()) {
                throw new IllegalArgumentException("Illegal pattern format."); 
            }
            if (part.startsWith(":")) {
                tokens.add(new Token(Type.CAPTURE, part.substring(1)));
            } else if (part.equals("*")) {
                tokens.add(new Token(Type.WILDCARD, part));
            } else {
                tokens.add(new Token(Type.TEXT, part));
            }
        }
        return tokens;
    }
}



