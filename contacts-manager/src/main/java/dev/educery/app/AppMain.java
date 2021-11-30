package dev.educery.app;

import dev.educery.manager.Main;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Launches this application.
 * 
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@SpringBootApplication
public class AppMain {

    public static void main(String... args) { Application.launch(Main.class, args); }

} // AppMain
