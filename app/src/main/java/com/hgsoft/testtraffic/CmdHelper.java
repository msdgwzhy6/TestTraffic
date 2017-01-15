package com.hgsoft.testtraffic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by YUDAPEI on 16/11/30.
 */

public class CmdHelper {
    private Process mProcess;
    private BufferedReader mReader;

    public String run(final String cmd) {
        InputStream inputStream = null;
        try {
            mProcess = Runtime.getRuntime().exec(cmd);
            inputStream = mProcess.getInputStream();
            mReader = new BufferedReader(new InputStreamReader(inputStream));
            String s = "";
            StringBuffer sb = new StringBuffer();
            boolean isFirst = true;
            while ((s = mReader.readLine()) != null) {
                if (!isFirst) {
                    sb.append("\n");
                }
                sb.append(s);
                parseResult(s);
                isFirst = false;
            }
            int status = mProcess.waitFor();
            if (status != 0) {
                inputStream = mProcess.getErrorStream();
                byte[] bytes = new byte[1024];
                inputStream.read(bytes, 0, inputStream.available());
                throw new Exception(new String(bytes));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }


    private void parseResult(String s) {
        System.out.println(s);
    }
}
