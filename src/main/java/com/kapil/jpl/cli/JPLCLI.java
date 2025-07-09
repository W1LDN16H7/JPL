package com.kapil.jpl.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapil.jpl.core.JPLInterpreter;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;



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
                JPLCLI.InitProjectCommand.class,
                JPLCLI.ValidateCommand.class,
                JPLCLI.BuildCommand.class,
                JPLCLI.CompileCommand.class,
                JPLCLI.EvalCommand.class,
                JPLCLI.FormatCommand.class,
                JPLCLI.ExplainCommand.class
        }
)
public class JPLCLI implements Runnable {
    private static final String DEFAULT_FILE = "main.jpl";

    /**
     * Main entry point for the JPL CLI application.
     * Executes the command line interface with the provided arguments.
     *
     * @param args Command-line arguments passed to the CLI.
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new JPLCLI()).execute(args);

        System.exit(exitCode);
    }

    /**
     * Default run method for the root CLI command.
     * Prints a message guiding the user to available commands.
     */
    @Override
    public void run() {
        REPL repl = new REPL(new JPLInterpreter());
        repl.start();
    }

    /**
     * Command to run a .jpl program file.
     * Validates the file and executes it using the JPLInterpreter.
     *
     *
     */
    @Command(name = "run", description = "Run a .jpl program file")
    static class RunCommand implements Runnable {
        @Parameters(index = "0", description = "Path to the .jpl program")
        private File file;

        /**
         * Executes the run command, validating and running the specified .jpl file.
         */
        @Override
        public void run() {

            if (file == null) {
                file = new File(DEFAULT_FILE);
            }
            if (!file.isFile() || !file.getName().endsWith(".jpl")) {
                System.err.println("Invalid or missing JPL program: " + file.getAbsolutePath());
                return;
            }
            if (!file.canRead()) {
                System.err.println("Cannot read program: " + file.getAbsolutePath());
                return;
            }
            if (file.length() == 0L) {
                System.err.println("Program file is empty: " + file.getAbsolutePath());
                return;
            }
            try {
                JPLInterpreter interpreter = new JPLInterpreter();
                interpreter.execute(file);
            } catch (Exception e) {
                System.err.println("Error running file: " + e.getMessage());
            }
        }
    }

    /**
     * Command to print the JPL version information.
     */
    @Command(name = "version", description = "Print JPL version info")
    static class VersionCommand implements Runnable {
        /**
         * Prints the current version of JPL.
         */
        @Override
        public void run() {
            System.out.println("JPL version 1.0");
        }
    }

    @Command(name = "help", description = "Display help info or examples by topic")
    static class HelpCommand implements Runnable {

        @Parameters(index = "0", paramLabel = "TOPIC", description = "Optional topic like 'if', 'loop', 'math', etc.")
        String topic;

        @Override
        public void run() {
            if (topic == null) {
                // Show general usage if no topic given
                new CommandLine(new JPLCLI()).usage(System.out);
            } else {
                // Try loading example file from resources
                try {
                    printHelpExample(topic);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private void printHelpExample(String topic) throws IOException {
            Path path = Path.of("src/main/java/com/kapil/jpl/examples/"+topic+".jpl");
            String topicData;
            Class clazz = JPLCLI.class;


            InputStream inputStream = clazz.getResourceAsStream(path.toString());
            String is = readFromInputStream(inputStream);
            System.out.println("Searching for examples for topic: " + topic);
            System.out.println("InputStream content: " + is);

            if (is == null || is.isBlank()) {
                System.out.println("No examples found for topic: " + topic);
                return;
            }
            if (is.contains(topic)) {
                topicData = is.substring(is.indexOf(topic));
                topicData = topicData.substring(0, topicData.indexOf("\n\n"));
                System.out.println("Example for topic '" + topic + "':\n" + topicData);
            } else {
                System.out.println("No examples found for topic: " + topic);
            }



        }
    }

    private static String readFromInputStream(InputStream inputStream)
            throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    /**
     * Command to create a new sample JPL program file (main.jpl).
     * If the file already exists, notifies the user.
     */
    @Command(name = "init", description = "Create a new sample JPL program")
    static class InitCommand implements Runnable {
        /**
         * Creates a sample main.jpl file in the current directory.
         */
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

    /**
     * Command to create a new sample JPL project with a project structure.
     *
    */
    @Command(name = "create", description = "Create a new sample JPL project with project structure" )
    static class InitProjectCommand implements Runnable {
        @Parameters(index = "0", description = "Name of the project")
        private String projectName;
        @Parameters(index = "1", description = "Path to the project directory", defaultValue = ".")

        private String projectDir;

        /**
         * Creates a new JPL project directory with a src folder and a sample main.jpl file.
         */
        @Override
        public void run() {
            try {
                File projectDir = new File(this.projectDir, projectName);
                if (projectDir.exists()) {
                    System.out.println("Project directory already exists: " + projectName);
                    return;
                }
                if (!projectDir.mkdir()) {
                    System.err.println("Failed to create project directory: " + projectName);
                    return;
                }
                File srcDir = new File(projectDir, "src");
                if (srcDir.exists()) {
                    System.out.println("Source directory already exists in project: " + projectName);
                    return;
                }

                if (!srcDir.mkdir()) {
                    System.err.println("Failed to create src directory in project: " + projectName);
                    return;
                }
                System.out.println("Created project directory: " + projectName);

                File mainFile = new File(srcDir, "main.jpl");
                String sample = "[\n  { \"let\": { \"x\": 5 } },\n  { \"print\": \"x\" }\n]";
                java.nio.file.Files.writeString(mainFile.toPath(), sample);
                System.out.println("Created JPL project structure with main.jpl in src/");
            } catch (Exception e) {
                System.err.println("Failed to create project: " + e.getMessage());
            }
        }
    }

    /**
     * Command to validate the syntax of a .jpl file.
     *
     */
    @Command(name = "validate", description = "Validate syntax of a .jpl file")
    static class ValidateCommand implements Runnable {
        @Parameters(index = "0", description = "Path to the .jpl file")
        private File file;

        /**
         * Validates the syntax of the specified .jpl file using Jackson's ObjectMapper.
         */
        @Override
        public void run() {
            try {
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(file);
                System.out.println("File is valid JPL Program.");
            } catch (Exception e) {
                System.err.println("Invalid JPL Program: " + e.getMessage());
            }
        }
    }

    /**
     * Command to compile .jpl files to bytecode (feature not yet implemented).
     */
    @Command(name = "build", description = "Compile .jpl files to bytecode (not yet implemented)")
    static class BuildCommand implements Runnable {
        /**
         * Placeholder for build functionality. Prints a coming soon message.
         */
        @Override
        public void run() {
            System.out.println("Build feature coming soon...");
        }
    }

    /**
     * Command to compile and save output to a file (feature not yet implemented).
     */
    @Command(name = "compile", description = "Compile and save output to file (not yet implemented)")
    static class CompileCommand implements Runnable {
        @Parameters(index = "0", description = "Command to compile JPL program", arity = "1")
        private String file;
        /**
         * Placeholder for compile functionality. Prints a coming soon message.
         */
        @Override
        public void run() {
            System.out.println("Compile feature coming soon...,We need your help to implement this feature!");
        }
    }

    /**
     * Command to evaluate a single inline JSON instruction or JSON piped from stdin.
     *
     */
    @Command(name = "eval", description = "Evaluate a single inline JSON instruction or JSON piped from stdin")
    static class EvalCommand implements Runnable {

        @Parameters(index = "0", arity = "0..1", description = "JSON string to evaluate (optional if piping via stdin)", paramLabel = "JSON")
        private String json;

        /**
         * Evaluates the provided JSON string or reads JSON from stdin, then executes it using the JPLInterpreter.
         */
        @Override
        public void run() {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String inputJson = json;

                // If no argument, try reading from stdin
                if (inputJson == null || inputJson.isBlank()) {
                    System.out.println("Reading JSON from stdin (end input with Ctrl+D):");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    inputJson = sb.toString();
                }

                JsonNode node = mapper.readTree(inputJson);
                JPLInterpreter interpreter = new JPLInterpreter();
                Object result = interpreter.eval(node);
                if (result != null) {
                    System.out.println("Result: " + result);
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    /**
     * Command to format a .jpl file (pretty print JSON).
     *
     */
    @Command(name = "format", description = "Format a .jpl file (pretty print JSON)")
    static class FormatCommand implements Runnable {
        @Parameters(index = "0", description = "Path to the .jpl file")
        private File file;

        /**
         * Formats the specified .jpl file using Jackson's pretty printer.
         */
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

    /**
     * Command to explain what a .jpl file will do (feature not yet implemented).
     *
     */
    @Command(name = "explain", description = "Explain what a .jpl file will do")
    static class ExplainCommand implements Runnable {
        @Parameters(index = "0", description = "Path to the .jpl file")
        private File file;

        /**
         * Placeholder for explain functionality. Prints a message about future analysis feature.
         */
        @Override
        public void run() {
            System.out.println("(EXPLAIN) This feature will analyze and explain the logic of the JPL file.");
        }
    }


}