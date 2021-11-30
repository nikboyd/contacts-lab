package dev.educery.app;

import java.io.*;
import java.net.Socket;
import dev.educery.utils.Logging;
import static dev.educery.utils.Logging.StaticLogger;

/**
 * Starts and stops the web service as needed.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Servant implements Logging {

    static Process RemoteProcess = null;
    static BufferedReader reader() {
        return new BufferedReader(new InputStreamReader(RemoteProcess.getInputStream())); }

    static void pipeShellOutput() {
        BufferedReader br = reader();
        String line;
        try {
            while((line=br.readLine()) != null){
               System.out.println(line);
            }
        }
        catch (IOException x) {
            System.out.println(x);
        }
    }

    static final String ServiceCommand = "java -jar contacts-boot*.jar";
    public static void startServer() {
        try {
            String baseFolder = System.getProperty("user.dir");
            StaticLogger.report(baseFolder);
            String bootFolder = "contacts-boot/target";
            if (!baseFolder.endsWith("contacts-lab")) bootFolder = "../" + bootFolder;
            String launchCommand = "cd " + bootFolder + "; " + ServiceCommand;
            ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", launchCommand);
            RemoteProcess = pb.start();
            new Thread(() -> { pipeShellOutput(); }).start();
        }
        catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public static void stopServer() {
        if (RemoteProcess == null) return;
        long pid = RemoteProcess.pid() + 1;
        String killCommand = "kill -kill " + pid;
        StaticLogger.report("killed pid = " + pid);
        try {
            ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", killCommand);
            RemoteProcess = pb.start();
        }
        catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public static boolean checkServer() {
        Socket s = null;
        try {
            s = new Socket("localhost", 9001);
            return true;
        }
        catch (Exception ex) {
            return false;
        }
        finally {
            if (s != null) try {s.close();} catch(Exception e){ }
        }
    }

    public static void waitForServer() {
        waitAwhile(15000);
        StaticLogger.report("checking service ...");
        while (!checkServer()) {
            StaticLogger.report("waiting for service ...");
            waitAwhile(2000);
        }
    }

    public static void waitAwhile(long msecs) {
        try {
            Thread.sleep(msecs);
        }
        catch (Exception ex) {
            // ignore ex
        }
    }

} // Servant
