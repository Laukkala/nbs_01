package com.teragrep.nbs_01.requests;

import java.util.Map;

// Request object contains parameters that the user wants to send to NBS_01
public interface Request {
    String body();
    Map<String, String> parameters();
}
