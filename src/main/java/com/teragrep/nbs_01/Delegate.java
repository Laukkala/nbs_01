package com.teragrep.nbs_01;

import com.teragrep.nbs_01.requests.Request;

public interface Delegate{
    Boolean resolve(Request request) throws Exception;
}
