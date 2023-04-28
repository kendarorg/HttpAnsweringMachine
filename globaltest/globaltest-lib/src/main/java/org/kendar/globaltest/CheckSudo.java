package org.kendar.globaltest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CheckSudo {
    private static final String CRLF = "\r\n";


    public static boolean isSudo() {
        return executeCommand("sudo", "-n", "true").length() == 0;
    }

    public static String executeCommand(String... command) {
        StringBuilder commandOutput = new StringBuilder();

        try {
            Process process = Runtime.getRuntime().exec(command);
            try {
                process.waitFor();
            } catch (InterruptedException ex) {

            }

            BufferedReader processOutput;

            if (process.exitValue() == 0) {
                processOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            } else {
                processOutput = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            }

            String line;
            while ((line = processOutput.readLine()) != null) {
                if (!line.isEmpty()) {
                    commandOutput.append(line).append(CRLF);
                }
            }
        } catch (IOException ex) {

        }

        return commandOutput.toString();
    }
}
