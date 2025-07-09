package com.kapil.jpl;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonLocation;
import com.kapil.jpl.exceptions.BreakException;
import com.kapil.jpl.exceptions.ContinueException;
import com.kapil.jpl.exceptions.JPLException;

import java.io.*;
import java.util.Iterator;

/**
 * JPLInterpreter is responsible for executing JPL (JSON Programming Language) scripts.
 * It loads, parses, and interprets .jpl files, handling variables, imports, conditionals, and output.
 * Supports comment removal, error reporting, and context management for script execution.
 */
public class JPLInterpreter {

    private File currentFile;

    private final JPLContext context = new JPLContext();

    /**
     * Executes a JPL program from the specified file.
     * Loads, parses, and evaluates the JSON instructions in the file.
     *
     * @param file The .jpl file to execute.
     * @throws IOException  If the file cannot be read or parsed.
     * @throws JPLException If the file is invalid or contains errors.
     */
    public void execute(File file) throws IOException {
        this.currentFile = file;
        if (file == null || !file.exists() || !file.isFile()) {
            throw new JPLException("Invalid file: " + (file != null ? file.getAbsolutePath() : "null"));
        }
        JsonNode root;
        try {
            root = loadCleanJson(file);
        } catch (JsonProcessingException e) {
            JsonLocation loc = e.getLocation();
            throw new JPLException("JSON parse error at line " + loc.getLineNr() + ", column " + loc.getColumnNr() + ": " + e.getOriginalMessage());
        }

        if (root.isArray()) {
            int index = 0;
            for (JsonNode node : root) {
                try {
                    eval(node);
                } catch (Exception e) {
                    throw new JPLException("Error in instruction at index " + index + ": " + node.toString() + "\nReason: " + e.getMessage());
                }
                index++;
            }
        } else {
            eval(root);
        }
    }

    /**
     * Evaluates a single JPL instruction node.
     * Handles let, import, if, print, and comment/ignore instructions.
     *
     * @param node The JSON node representing the instruction.
     * @return The result of the instruction, or null for statements.
     * @throws IOException  If an imported file cannot be read.
     * @throws JPLException If the instruction is unknown or invalid.
     */
    public Object eval(JsonNode node) throws IOException {




        if (node.has("let")) {
            return context.handleLet(node.get("let"));
        } else if (node.has("const")) {
            return context.handleConst(node.get("const"));
        } else if (node.has("import") || node.has("laao") || node.has("bring")) {
            String path = node.has("import") ? node.get("import").asText()
                    : node.has("laao") ? node.get("laao").asText()
                    : node.get("bring").asText();

            File imported = currentFile != null && currentFile.getParentFile() != null
                    ? new File(currentFile.getParentFile(), path)
                    : new File(path);

            if (!imported.exists()) {
                throw new JPLException("Import failed: file not found → " + imported.getAbsolutePath());
            }

            execute(imported);
            return null;
        } else if (node.has("if")) {
            return handleIf(node);
        } else if (node.has("comment") ||
                node.has("ignore") ||
                node.has("skip") ||
                node.has("noop")
                || node.has("cmt") ||
                node.has("ig")
        ) {
            // do nothing (ignore)
            return null;
        } else if (node.has("print")) {
            Object value = context.resolve(node.get("print"));
            System.out.println(value);
            return null;
        } else if (node.has("while") && node.has("do")) {
            while ((Boolean) context.resolve(node.get("while"))) {
                try {
                    for (JsonNode stmt : node.get("do")) {
                        eval(stmt);
                    }
                } catch (ContinueException ce) {
                    // skip to next iteration
                    continue;
                } catch (BreakException be) {
                    // exit loop
                    break;
                }
            }
            return null;

        } else if (node.has("do") && node.has("while")) {
            do {
                try {
                    for (JsonNode stmt : node.get("do")) {
                        eval(stmt);
                    }
                } catch (ContinueException ce) {
                    // skip to next iteration
                    // do nothing here, loop continues
                } catch (BreakException be) {
                    // exit loop
                    break;
                }
            } while ((Boolean) context.resolve(node.get("while")));
            return null;

        } else if (node.has("break")) {
            throw new BreakException();

        } else if (node.has("continue")) {
            throw new ContinueException();

        } else if (node.has("for")) {
            JsonNode forNode = node.get("for");

            String varName = forNode.get("var").asText();
            int from = forNode.get("from").asInt();
            int to = forNode.get("to").asInt();
            int step = forNode.has("step") ? forNode.get("step").asInt() : 1;

            for (int i = from; i <= to; i += step) {
                context.getVariables().put(varName, i);
                try {
                    for (JsonNode stmt : forNode.get("do")) {
                        eval(stmt);
                    }
                } catch (ContinueException ce) {
                    // skip to next iteration
                    continue;
                } catch (BreakException be) {
                    // exit loop
                    break;
                }
            }
            return null;

        } else {
            throw new JPLException("Unknown instruction: " + node);
        }
    }






    /**
     * Handles the 'if' instruction in JPL, evaluating conditionals and executing the appropriate branch.
     *
     * @param node The JSON node containing the 'if', 'then', and optional 'else' fields.
     * @return The result of the executed branch, or null if no branch is taken.
     * @throws IOException  If evaluation of a branch fails.
     * @throws JPLException If the condition does not evaluate to a boolean.
     */
    private Object handleIf(JsonNode node) throws IOException {
        JsonNode ifNode = node.get("if");
        if (ifNode == null)
            throw new JPLException("Missing 'if' field in if statement");

        // Support both "condition" key or direct condition inside "if"
        JsonNode conditionNode = ifNode.has("cond") ? ifNode.get("cond") : null;

        if (conditionNode == null) {
            // Try to get the first key that is not "then" or "else"
            Iterator<String> fields = ifNode.fieldNames();
            while (fields.hasNext()) {
                String key = fields.next();
                if (!key.equals("then") && !key.equals("else")) {
                    conditionNode = ifNode.get(key);
                    break;
                }
            }
        }

        if (conditionNode == null)
            throw new JPLException("Missing condition in if statement");

        Object condition = context.resolve(conditionNode);
        if (!(condition instanceof Boolean))
            throw new JPLException("\"if\" condition must evaluate to a boolean");

        if (Boolean.TRUE.equals(condition)) {
            return eval(ifNode.get("then"));
        } else if (ifNode.has("else")) {
            return eval(ifNode.get("else"));
        }
        return null;
    }


    /**
     * Loads a JSON file, removing both line (//) and block (/* ... *&#47;) comments before parsing.
     * This allows .jpl files to contain comments that are ignored during parsing.
     *
     * @param file The file to load and clean.
     * @return The parsed JsonNode from the cleaned file content.
     * @throws IOException If the file cannot be read or parsed.
     */
    private JsonNode loadCleanJson(File file) throws IOException {
        String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));

        // Remove // line comments
        content = content.replaceAll("(?m)//.*$", "");

        // Remove /* block */ comments
        content = content.replaceAll("(?s)/\\*.*?\\*/", "");

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(content);
    }


}