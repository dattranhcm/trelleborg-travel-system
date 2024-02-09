package com.travel.system.trelleborg.common;

import org.apache.commons.codec.digest.DigestUtils;

public class HashUtils {
    public static String hashMD5(String input) {
        return DigestUtils.md5Hex(input);
    }
}
