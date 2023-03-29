package de.maxhenkel.resourcepackchecker;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;

public class ShaUtils {

    public static String getSha1(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            String sha1 = DigestUtils.sha1Hex(fis);
            fis.close();
            return sha1;
        } catch (Exception e) {
            return "";
        }
    }

}
