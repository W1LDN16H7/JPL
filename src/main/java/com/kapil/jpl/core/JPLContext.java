package com.kapil.jpl.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.kapil.jpl.exceptions.JPLException;

import java.util.*;

/**
 * JPLContext manages the execution context for JPL scripts,
 * including variable storage and resolution of expressions.
 */
public class JPLContext {
    private final Map<String, Object> variables = new HashMap<>();
    private final Set<String> constants = new HashSet<>();
    private final Map<String, FunctionDef> functions = new HashMap<>();

    /**
     * Handles the 'let' operation in JPL, assigning variables in the context.
     *
     * @param letNode JSON node containing variable assignments.
     * @return Always returns null after assignment.
     */
    public Object handleLet(JsonNode letNode) {
        Iterator<String> fields = letNode.fieldNames();

        if (letNode.isEmpty()) {
            throw new JPLException("Empty 'let' operation");
        }
        while (fields.hasNext()) {
            String name = fields.next();
            Object value = resolve(letNode.get(name));

            if (constants.contains(name)) {
                throw new JPLException("Cannot assign to constant variable: " + name);
            }
            variables.put(name, value);
        }
        return null;
    }

    /**
     * Resolves a JSON node to its value in the current context.
     * Handles literals, variables, arithmetic, and logic expressions.
     *
     * @param node The JSON node to resolve.
     * @return The resolved value (may be a primitive, object, or result of an operation).
     */
    public Object resolve(JsonNode node) {
        if (node.isTextual()) {
            String text = node.asText();
            return variables.getOrDefault(text, text);
        } else if (node.isNumber()) {
            return node.numberValue();
        } else if (node.isBoolean()) {
            return node.booleanValue();

        } else if (node.isNull()) {
            return null;
        } else if (node.isObject()) {
            // Arithmetic (binary)
            if (node.has("add")) return resolveArithmetic(node.get("add"), "+");
            if (node.has("sub")) return resolveArithmetic(node.get("sub"), "-");
            if (node.has("mul")) return resolveArithmetic(node.get("mul"), "*");
            if (node.has("div")) return resolveArithmetic(node.get("div"), "/");
            if (node.has("mod")) return resolveArithmetic(node.get("mod"), "%");
            if (node.has("pow")) return resolveArithmetic(node.get("pow"), "^");

            // Bitwise
            if (node.has("&")) return resolveArithmetic(node.get("&"), "&");
            if (node.has("|")) return resolveArithmetic(node.get("|"), "|");
            if (node.has("^|")) return resolveArithmetic(node.get("^|"), "^|");
            if (node.has("<<")) return resolveArithmetic(node.get("<<"), "<<");
            if (node.has(">>")) return resolveArithmetic(node.get(">>"), ">>");
            if (node.has(">>>")) return resolveArithmetic(node.get(">>>"), ">>>");

            // Arithmetic (unary)
            if (node.has("sqrt")) return resolveUnaryArithmetic(node.get("sqrt"), "sqrt");
            if (node.has("abs")) return resolveUnaryArithmetic(node.get("abs"), "abs");
            if (node.has("neg")) return resolveUnaryArithmetic(node.get("neg"), "neg");
            if (node.has("~")) return resolveUnaryArithmetic(node.get("~"), "~");

            // Logical
            if (node.has("eq")) return resolveLogic(node.get("eq"), "eq");
            if (node.has("==")) return resolveLogic(node.get("=="), "==");
            if (node.has("lt")) return resolveLogic(node.get("lt"), "lt");
            if (node.has("<")) return resolveLogic(node.get("<"), "<");
            if (node.has("lte")) return resolveLogic(node.get("lte"), "lte");
            if (node.has("<=")) return resolveLogic(node.get("<="), "<=");
            if (node.has("gt")) return resolveLogic(node.get("gt"), "gt");
            if (node.has(">")) return resolveLogic(node.get(">"), ">");
            if (node.has("gte")) return resolveLogic(node.get("gte"), "gte");
            if (node.has(">=")) return resolveLogic(node.get(">="), ">=");
            if (node.has("and")) return resolveLogic(node.get("and"), "and");
            if (node.has("&&")) return resolveLogic(node.get("&&"), "&&");
            if (node.has("or")) return resolveLogic(node.get("or"), "or");
            if (node.has("||")) return resolveLogic(node.get("||"), "||");
            if (node.has("not")) return resolveLogic(node.get("not"), "not");
            if (node.has("!")) return resolveLogic(node.get("!"), "!");

            throw new JPLException("Unknown expression: " + node);
        }
        throw new JPLException("Unsupported node type: " + node);
    }

    /**
     * Resolves a binary arithmetic operation (add, sub, mul, div, mod, pow).
     *
     * @param operands JSON array of two operands.
     * @param op The arithmetic operator as a string (e.g., "+", "-", etc.).
     * @return The result of the arithmetic operation as a Number.
     */
    private Object resolveArithmetic(JsonNode operands, String op) {
        if (!operands.isArray() || operands.size() != 2)
            throw new JPLException("Invalid operands for " + op);

        Object a1 = resolve(operands.get(0));
        Object b1 = resolve(operands.get(1));

        if ((op.equals("+") || op.equals("add")) && (!(a1 instanceof Number) || !(b1 instanceof Number))) {
            // String concatenation if either operand is not number
            return String.valueOf(a1) + String.valueOf(b1);
        }

        Number a = asNumber(a1);
        Number b = asNumber(b1);

        return switch (op) {
            case "+", "add" -> a.doubleValue() + b.doubleValue();
            case "-", "sub" -> a.doubleValue() - b.doubleValue();
            case "*", "mul" -> a.doubleValue() * b.doubleValue();
            case "/", "div" -> a.doubleValue() / b.doubleValue();
            case "%", "mod" -> a.doubleValue() % b.doubleValue();
            case "^", "pow" -> Math.pow(a.doubleValue(), b.doubleValue());

            // Bitwise operators
            case "&" -> (long) a.doubleValue() & (long) b.doubleValue();
            case "|" -> (long) a.doubleValue() | (long) b.doubleValue();
            case "^|" -> (long) a.doubleValue() ^ (long) b.doubleValue();
            case "<<" -> (long) a.doubleValue() << b.intValue();
            case ">>" -> (long) a.doubleValue() >> b.intValue();
            case ">>>" -> (long) a.doubleValue() >>> b.intValue();

            default -> throw new JPLException("Unknown operator: " + op);
        };
    }

    /**
     * Resolves a unary arithmetic operation (sqrt, abs, neg).
     *
     * @param operand JSON node representing the operand.
     * @param op The unary operator as a string (e.g., "sqrt", "abs", "neg").
     * @return The result of the unary operation as a Number.
     */
    private Number resolveUnaryArithmetic(JsonNode operand, String op) {
        Object value = resolve(operand);
        Number num = asNumber(value);

        return switch (op) {
            case "sqrt" -> Math.sqrt(num.doubleValue());
            case "abs" -> Math.abs(num.doubleValue());
            case "neg" -> -num.doubleValue();
            case "~" -> ~(long) num.doubleValue();  // Add this line

            default -> throw new JPLException("Unknown unary arithmetic operator: " + op);
        };
    }

    /**
     * Resolves a logical operation (eq, lt, gt, and, or, not).
     *
     * @param operands JSON node(s) representing the operands.
     * @param op The logical operator as a string (e.g., "eq", "lt", etc.).
     * @return The result of the logical operation (Boolean).
     */
    private Object resolveLogic(JsonNode operands, String op) {
        switch (op) {
            case "eq", "==" -> {
                Object a = resolve(operands.get(0));
                Object b = resolve(operands.get(1));
                return Objects.equals(a, b);
            }
            case "lt", "<" -> {
                Number a = asNumber(resolve(operands.get(0)));
                Number b = asNumber(resolve(operands.get(1)));
                return a.doubleValue() < b.doubleValue();
            }
            case "lte", "<=" -> {
                Number a = asNumber(resolve(operands.get(0)));
                Number b = asNumber(resolve(operands.get(1)));
                return a.doubleValue() <= b.doubleValue();
            }
            case "gt", ">" -> {
                Number a = asNumber(resolve(operands.get(0)));
                Number b = asNumber(resolve(operands.get(1)));
                return a.doubleValue() > b.doubleValue();
            }
            case "gte", ">=" -> {
                Number a = asNumber(resolve(operands.get(0)));
                Number b = asNumber(resolve(operands.get(1)));
                return a.doubleValue() >= b.doubleValue();
            }
            case "and", "&&" -> {
                return resolve(operands.get(0)).equals(true) && resolve(operands.get(1)).equals(true);
            }
            case "or", "||" -> {
                return resolve(operands.get(0)).equals(true) || resolve(operands.get(1)).equals(true);
            }
            case "not", "!" -> {
                return !resolve(operands.get(0)).equals(true);
            }
            default -> throw new JPLException("Unknown logical operator: " + op);
        }
    }

    /**
     * Handles the 'const' operation in JPL, defining constants in the context.
     *
     * @param constNode JSON node containing constant definitions.
     * @return Always returns null after defining constants.
     */
    public Object handleConst(JsonNode constNode) {
        Iterator<String> fields = constNode.fieldNames();
        while (fields.hasNext()) {
            String name = fields.next();
            if (variables.containsKey(name)) {
                throw new JPLException("Variable already defined: " + name);
            }
            Object value = resolve(constNode.get(name));
            variables.put(name, value);
            constants.add(name);
        }
        return null;
    }

    /**
     * Converts an object to a Number if possible.
     *
     * @param value The object to convert.
     * @return The value as a Number.
     * @throws JPLException if the value is not a Number.
     */
    private Number asNumber(Object value) {
        if (value instanceof Number) return (Number) value;
        throw new JPLException("Expected number, got: " + value);
    }

    /**
     * Returns the current variable map for this context.
     *
     * @return Map of variable names to their values.
     */
    public Map<String, Object> getVariables() {
        return variables;
    }

    public static class FunctionDef {
        public List<String> params;
        public JsonNode body;

        public FunctionDef(List<String> params, JsonNode body) {
            this.params = params;
            this.body = body;
        }
    }



    /**
     * Returns the current constant set for this context.
     *
     * @return Set of constant variable names.
     */
    public void defineFunction(String name, List<String> params, JsonNode body) {
        functions.put(name, new FunctionDef(params, body));
    }


    /**
     * Retrieves a function definition by name.
     *
     * @param name The name of the function.
     * @return The FunctionDef object for the specified function.
     * @throws JPLException if the function is not defined.
     */
    public FunctionDef getFunction(String name) {
        if (!functions.containsKey(name)) {
            throw new JPLException("Function not defined: " + name);
        }
        return functions.get(name);
    }


}