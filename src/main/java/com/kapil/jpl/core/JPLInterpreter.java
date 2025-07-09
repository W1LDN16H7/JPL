package com.kapil.jpl.core;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonLocation;
import com.kapil.jpl.exceptions.BreakException;
import com.kapil.jpl.exceptions.ContinueException;
import com.kapil.jpl.exceptions.JPLException;
import com.kapil.jpl.exceptions.ReturnException;

import java.io.*;
import java.util.*;

/**
 * JPLInterpreter is responsible for executing JPL (JSON Programming Language) scripts.
 * It loads, parses, and interprets .jpl files, handling variables, imports, conditionals, and output.
 * Supports comment removal, error reporting, and context management for script execution.
 */
public class JPLInterpreter {
    private final File standardLibDir;


    private File currentFile;

    private final JPLContext context = new JPLContext();

    public JPLInterpreter() {
        this.standardLibDir = new File("lib"); // or wherever your lib folder is
    }

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
        if (node == null || node.isNull()) return null;


        if (node.isTextual()) return context.resolve(node);
        if (node.isNumber()) return node.numberValue();
        if (node.isBoolean()) return node.booleanValue();

        if (node.has("def")) return evalFunctionDefinition(node.get("def"));
        if (node.has("call")) return evalFunctionCall(node.get("call"));
        if (node.has("return")) {
            Object val = eval(node.get("return")); // recursive eval
            throw new ReturnException(val);
        }
        if (node.has("def")) {
            return evalFunctionDefinition(node.get("def"));
        }
        if (node.has("call")) {
            return evalFunctionCall(node.get("call"));
        }
        if (node.has("let")) {
            return context.handleLet(node.get("let"));
        } else if (node.has("const")) {
            return context.handleConst(node.get("const"));

            //import

        } else if (node.has("import") || node.has("laao") || node.has("bring")) {
            String path = node.has("import") ? node.get("import").asText()
                    : node.has("laao") ? node.get("laao").asText()
                    : node.get("bring").asText();

            File importedFile;

            // 1. Try relative to current file's directory
            if (currentFile != null && currentFile.getParentFile() != null) {
                importedFile = new File(currentFile.getParentFile(), path);
                if (!importedFile.exists()) {
                    // 2. fallback to standard lib directory
                    importedFile = new File(standardLibDir, path);
                }
            } else {
                // no current file context, check standard lib only
                importedFile = new File(standardLibDir, path);
            }

            if (!importedFile.exists()) {
                throw new JPLException("Import failed: file not found → " + importedFile.getAbsolutePath());
            }

            execute(importedFile);
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
            JsonNode printNode = node.get("print");
            if (node.isArray()) {
                Object lastValue = null;
                for (JsonNode element : node) {
                    lastValue = eval(element);
                }
                return lastValue;  // returns result of last evaluated statement
            }
            Object value;
            if (printNode.has("call")) {
                // Delegate to main eval() which handles 'call'
                value = eval(printNode);
            } else {
                value = context.resolve(printNode);
            }

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
            try {
                return context.resolve(node);
            } catch (Exception e) {
                throw new JPLException("Unknown instruction: " + node);
            }
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


    // fun support


    // Container for a function
    private static class FunctionDef {
        List<String> params;
        JsonNode body;

        FunctionDef(List<String> params, JsonNode body) {
            this.params = params;
            this.body = body;
        }
    }

    // Store functions here: function name -> definition
    private final Map<String, FunctionDef> functions = new HashMap<>();

    private Object evalFunctionDefinition(JsonNode defNode) {
        Iterator<String> names = defNode.fieldNames();
        while (names.hasNext()) {
            String fnName = names.next();
            JsonNode fnNode = defNode.get(fnName);
            List<String> params = new ArrayList<>();
            for (JsonNode p : fnNode.get("params")) {
                params.add(p.asText());
            }
            JsonNode body = fnNode.get("body");
            functions.put(fnName, new FunctionDef(params, body));
        }
        return null;
    }

    private Object evalFunctionCall(JsonNode callNode) throws IOException {
        Iterator<String> names = callNode.fieldNames();
        if (!names.hasNext()) {
            throw new JPLException("Function call missing function name");
        }

        String fnName = names.next();

        FunctionDef fn = functions.get(fnName);
        if (fn == null) {
            throw new JPLException("Function not defined: " + fnName);
        }

        JsonNode argsNode = callNode.get(fnName);
        if (!argsNode.isArray()) {
            throw new JPLException("Function call arguments must be an array");
        }

        if (argsNode.size() != fn.params.size()) {
            throw new JPLException("Function " + fnName + " expects " + fn.params.size() +
                    " arguments, got " + argsNode.size());
        }

        // Save current vars
        Map<String, Object> oldVars = new HashMap<>(context.getVariables());

        // Bind parameters with evaluated arguments
        for (int i = 0; i < fn.params.size(); i++) {
            Object argVal = eval(argsNode.get(i)); // ✅ Now supports expressions
            context.getVariables().put(fn.params.get(i), argVal);
        }

        Object retVal = null;
        try {
            for (JsonNode stmt : fn.body) {
                retVal = eval(stmt); // ✅ Will throw ReturnException if needed
            }
        } catch (ReturnException re) {
            retVal = re.getValue();
        } finally {
            context.getVariables().clear();
            context.getVariables().putAll(oldVars);
        }

        switch (fnName) {
            case "java_time_now":
                return java.time.LocalDateTime.now().toString();
            case "java_os_name":
                return System.getProperty("os.name");
            case "java_user_name":
                return System.getProperty("user.name");
            case "java_env":
                if (callNode.size() == 1) {
                    String key = callNode.get(0).asText();
                    return System.getenv(key);
                } else {
                    throw new JPLException("java_env expects 1 argument");
                }
            case null, default:
                // No special handling for other functions
                break;
        }

        return retVal;
    }


}