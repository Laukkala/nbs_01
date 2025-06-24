package com.teragrep.nbs_01;

import com.teragrep.nbs_01.requests.Request;

public class DoesKeyExistDelegate implements Delegate {
    private final String key;

    DoesKeyExistDelegate(String key){
        this.key = key;
    }
    public Boolean resolve(Request request) throws Exception {
        return request.parameters().containsKey(key);
    }
}
