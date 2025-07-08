package com.kapil.jpl;

import picocli.CommandLine;
import picocli.CommandLine.*;

import java.io.File;
import java.util.concurrent.Callable;

@Command(
        name = "jpl",
        mixinStandardHelpOptions = true,
        version = "JPL 1.0",
        description = "JSON Programming Language CLI",
        subcommands = {
                JPLCLI.RunCommand.class,
                JPLCLI.VersionCommand.class,
                JPLCLI.HelpCommand.class,
                JPLCLI.InitCommand.class,
                JPLCLI.ValidateCommand.class,
                JPLCLI.BuildCommand.class,
                JPLCLI.CompileCommand.class,
                JPLCLI.EvalCommand.class,
                JPLCLI.FormatCommand.class,
                JPLCLI.ExplainCommand.class
        }
)
public class JPLCLI implements Runnable {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new JPLCLI()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        System.out.println("Use `jpl help` to see available commands.");
    }

    @Command(name = "run", description = "Run a .jpl program file")
    static class RunCommand implements Runnable {
        @Parameters(index = "0", description = "Path to the .jpl file")
        private File file;

        @Override
        public void run() {
            try {
                JPLInterpreter interpreter = new JPLInterpreter();
                interpreter.execute(file);
            } catch (Exception e) {
                System.err.println("Error running file: " + e.getMessage());
            }
        }
    }

    @Command(name = "version", description = "Print JPL version info")
    static class VersionCommand implements Runnable {
        @Override
        public void run() {
            System.out.println("JPL version 1.0");
        }
    }

    @Command(name = "help", description = "Display help info")
    static class HelpCommand implements Runnable {
        @Override
        public void run() {
            new CommandLine(new JPLCLI()).usage(System.out);
        }
    }

    @Command(name = "init", description = "Create a new sample JPL program")
    static class InitCommand implements Runnable {
        @Override
        public void run() {
            try {
                String sample = "[\n  { \"let\": { \"x\": 5 } },\n  { \"print\": \"x\" }\n]";
                File f = new File("main.jpl");
                if (!f.exists()) {
                    java.nio.file.Files.writeString(f.toPath(), sample); //TODO make full project with src
                    System.out.println("Created main.jpl");
                } else {
                    System.out.println("main.jpl already exists.");
                }
            } catch (Exception e) {
                System.err.println("Failed to create file: " + e.getMessage());
            }
        }
    }

    @Command(name = "validate", description = "Validate syntax of a .jpl file")
    static class ValidateCommand implements Runnable {
        @Parameters(index = "0", description = "Path to the .jpl file")
        private File file;

        @Override
        public void run() {
            try {
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(file);
                System.out.println("File is valid JSON.");
            } catch (Exception e) {
                System.err.println("Invalid JSON: " + e.getMessage());
            }
        }
    }

    @Command(name = "build", description = "Compile .jpl files to bytecode (not yet implemented)")
    static class BuildCommand implements Runnable {
        @Override
        public void run() {
            System.out.println("Build feature coming soon...");
        }
    }

    @Command(name = "compile", description = "Compile and save output to file (not yet implemented)")
    static class CompileCommand implements Runnable {
        @Override
        public void run() {
            System.out.println("Compile feature coming soon...");
        }
    }

    @Command(name = "eval", description = "Evaluate a JPL expression directly")
    static class EvalCommand implements Runnable {
        @Parameters(index = "0", description = "Expression in JSON")
        private String expression;

        @Override
        public void run() {
            try {
                var node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(expression);
                var context = new JPLContext();
                var result = context.resolve(node);
                System.out.println("Result: " + result);
            } catch (Exception e) {
                System.err.println("Eval failed: " + e.getMessage());
            }
        }
    }

    @Command(name = "format", description = "Format a .jpl file (pretty print JSON)")
    static class FormatCommand implements Runnable {
        @Parameters(index = "0", description = "Path to the .jpl file")
        private File file;

        @Override
        public void run() {
            try {
                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                var tree = mapper.readTree(file);
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, tree);
                System.out.println("File formatted successfully.");
            } catch (Exception e) {
                System.err.println("Format error: " + e.getMessage());
            }
        }
    }

    @Command(name = "explain", description = "Explain what a .jpl file will do")
    static class ExplainCommand implements Runnable {
        @Parameters(index = "0", description = "Path to the .jpl file")
        private File file;

        @Override
        public void run() {
            System.out.println("(EXPLAIN) This feature will analyze and explain the logic of the JPL file.");
        }
    }
}