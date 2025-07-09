package com.kapil.jpl;



import com.fasterxml.jackson.databind.JsonNode;
import com.kapil.jpl.cli.REPL;
import com.kapil.jpl.core.JPLInterpreter;

import java.io.File;
import java.io.IOException;

public class Main {
    JPLInterpreter interpreter = new JPLInterpreter();
    public static void main(String[] args) throws IOException {



        JPLInterpreter interpreter = new JPLInterpreter();

        if (args.length == 0) {
            // No args → launch REPL
            System.out.println("No script file provided. Launching JPL REPL...");
            REPL repl = new REPL(interpreter);
            repl.start();
        } else {
            // Run script from file
            String filename = args[0];
            try {
                interpreter.execute(new File(filename));
            } catch (Exception e) {
                System.err.println("Error running file: " + e.getMessage());
            }
        }
    }
}
