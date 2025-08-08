package org.acme;

import java.io.*;

public class FileHelper {

    public static String readFileContent(String fileName) throws IOException {
        return readFileContent(fileName, "CP1252");
    }

    public static String readFileContent(String fileName, String charsetname) throws IOException {
        InputStream inputStream = readFileStream(fileName);
        if (inputStream == null) {
            throw new IOException("Could not find file '" + fileName + "'");
        }
        return readFileContent(inputStream, charsetname);
    }

    public static String readFileContent(InputStream is, String charsetname) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (is == null) {
            return sb.toString();
        }
        try (is; BufferedReader bufferReader = new BufferedReader(new InputStreamReader(is, charsetname))) {
            int c;
            char[] cBuf = new char[1024];
            while ((c = bufferReader.read(cBuf)) != -1) {
                sb.append(cBuf, 0, c);
            }
        }
        return sb.toString();
    }

    public static InputStream readFileStream(String fileName) {
        File file = new File(fileName);
        InputStream inputStream = null;
        if (file.exists()) {
            try {
                inputStream = new FileInputStream(fileName);
            } catch (FileNotFoundException e) {
                // do nothing we first try with the classLoader
            }
        }
        if (inputStream == null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            inputStream = cl.getResourceAsStream(fileName);
        }
        return inputStream;
    }


}
