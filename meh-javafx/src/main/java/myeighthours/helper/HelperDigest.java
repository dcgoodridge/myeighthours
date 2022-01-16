package myeighthours.helper;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HelperDigest {

    private static final Logger LOG = LoggerFactory.getLogger(HelperDigest.class);

    public static String SHA256String(String text) {
        String output = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(text.getBytes());
            byte[] digest = md.digest();
            byte[] encodedBytes = Base64.encodeBase64(digest);
            output =  new String(encodedBytes);
        }catch (NoSuchAlgorithmException nsae){
            LOG.error("Error generando SHA256", nsae);
        }
        return output;
    }

    public static String SHA1String(String text) {
        return DigestUtils.sha1Hex(text);
    }

}
