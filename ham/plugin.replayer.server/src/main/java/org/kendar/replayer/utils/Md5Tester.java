package org.kendar.replayer.utils;

import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class Md5Tester {
    public String calculateMd5(Object data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        if(data==null){
            return "0";
        }
        if(data instanceof String){
            if(((String)data).length()==0) return "0";
            md.update(((String)data).getBytes(StandardCharsets.UTF_8));
        }else{
            if(((byte[])data).length==0) return "0";
            md.update((byte[])data);
        }
        byte[] digest = md.digest();
        BigInteger bigInt = new BigInteger(1,digest);
        return bigInt.toString(16);
    }
}
