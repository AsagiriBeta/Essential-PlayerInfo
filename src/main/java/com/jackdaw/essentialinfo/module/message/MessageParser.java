package com.jackdaw.essentialinfo.module.message;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageParser extends Parser {

    private static final Pattern BROADCAST_PATTERN = Pattern.compile("^\\s*#\\s*(.+)$");

    @Override
    public HashMap<String, Object> parse(String message) {
        HashMap<String, Object> parsingResult = super.parse(message);
        parsingResult.put("broadcastTag", false);
        parsingResult.put("content", null);
        allowBroadcasting(message, parsingResult);
        return parsingResult;
    }

    private void allowBroadcasting(String message, HashMap<String, Object> parsingResult) {
        Matcher broadcastTagMatcher = BROADCAST_PATTERN.matcher(message);
        if (broadcastTagMatcher.matches()) {
            parsingResult.put("broadcastTag", true);
            parsingResult.put("content", broadcastTagMatcher.group(1));
        }
    }
}
