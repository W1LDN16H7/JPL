package com.kapil.jpl;



import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java -jar jpl.jar <file.jpl>");
            return;
        }

        JPLInterpreter interpreter = new JPLInterpreter();
        interpreter.execute(new File(args[0]));
    }
}
