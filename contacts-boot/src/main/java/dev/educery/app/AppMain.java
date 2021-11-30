package dev.educery.app;

import dev.educery.server.MainController;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Launches the web service.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@SpringBootApplication
public class AppMain {

    public static void main(String... args) { MainController.startApplication(args); }

} // AppMain
