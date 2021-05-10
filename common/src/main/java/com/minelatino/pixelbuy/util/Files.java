package com.minelatino.pixelbuy.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class Files {

    public static void writeStream(File file, InputStream in) {
        try (FileOutputStream fos = new FileOutputStream(file); BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                bos.write(buf, 0, len);
            }
            bos.flush();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
