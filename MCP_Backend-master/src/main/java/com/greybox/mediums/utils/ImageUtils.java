package com.greybox.mediums.utils;

import org.springframework.beans.factory.annotation.Autowired;
import ug.ac.mak.java.logger.Log;

import java.io.*;
import java.util.Base64;

public class ImageUtils {


    private static Log logHandler;

    @Autowired
    public void setLogHandler(Log logHandler) {
        ImageUtils.logHandler = logHandler;
    }

    public static byte[] getImage(String base64String) {
        if (base64String == null)
            return null;
        try {
            return Base64.getMimeDecoder().decode(base64String.trim()
                    .replaceFirst("^data:image/[^;]*;base64,?", "").getBytes());
        } catch (Exception e) {
            logHandler.error(e);
            return null;
        }
    }

    public static String getImageToString(byte[] image) {
        /* 32 */     if (image == null)
            /* 33 */       return null;
        /* 34 */     return
                /* 35 */       Base64.getMimeEncoder().encodeToString(image);
        /*    */   }

    public static String getExtension(String base64) {
        String[] strings = base64.split(",");
        String extension;
        switch (strings[0]) {
            case "data:image/jpeg;base64":
                extension = "jpeg";
                break;
            case "data:image/png;base64":
                extension = "png";
                break;
            default:// should write cases for more images types
                extension = "jpg";
                break;
        }
        return extension.toUpperCase();
    }

    public static void saveImage(String base64, String folder, String fileName) {

        File xFolder = new File(folder);
        if (!xFolder.exists())
            xFolder.mkdirs();

        File targetFile = new File(xFolder, fileName + getExtension(base64));
        try (InputStream stream = new ByteArrayInputStream(Base64.getMimeDecoder()
                .decode(getImage(base64)));
             OutputStream outStream = new FileOutputStream(targetFile);) {

            byte[] buffer = new byte[stream.available()];
            stream.read(buffer);
            outStream.write(buffer);

        } catch (IOException e) {
            logHandler.error(e);
        }
    }

}
