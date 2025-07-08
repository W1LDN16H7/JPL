package com.kapil.jpl;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;

public class JPLContext {
    private final Map<String, Object> variables = new HashMap<>();

    public Object handleLet(JsonNode letNode) {
        Iterator<String> fields = letNode.fieldNames();
        while (fields.hasNext()) {
            String name = fields.next();
            Object value = resolve(letNode.get(name));
            variables.put(name, value);
        }
        return null;
    }

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
            if (node.has("add")) return resolveArithmetic(node.get("add"), "+");
            if (node.has("sub")) return resolveArithmetic(node.get("sub"), "-");
            if (node.has("mul")) return resolveArithmetic(node.get("mul"), "*");
            if (node.has("div")) return resolveArithmetic(node.get("div"), "/");
            if (node.has("eq")) return resolveLogic(node.get("eq"), "eq");
            if (node.has("lt")) return resolveLogic(node.get("lt"), "lt");
            if (node.has("gt")) return resolveLogic(node.get("gt"), "gt");
            if (node.has("and")) return resolveLogic(node.get("and"), "and");
            if (node.has("or")) return resolveLogic(node.get("or"), "or");
            if (node.has("not")) return resolveLogic(node.get("not"), "not");
            throw new JPLException("Unknown expression: " + node);
        }
        throw new JPLException("Unsupported node type: " + node);
    }

    private Number resolveArithmetic(JsonNode operands, String op) {
        if (!operands.isArray() || operands.size() != 2)
            throw new JPLException("Invalid operands for " + op);

        Number a = asNumber(resolve(operands.get(0)));
        Number b = asNumber(resolve(operands.get(1)));

        return switch (op) {
            case "+" -> a.doubleValue() + b.doubleValue();
            case "-" -> a.doubleValue() - b.doubleValue();
            case "*" -> a.doubleValue() * b.doubleValue();
            case "/" -> a.doubleValue() / b.doubleValue();
            default -> throw new JPLException("Unknown operator: " + op);
        };
    }

    private Object resolveLogic(JsonNode operands, String op) {
        switch (op) {
            case "eq" -> {
                Object a = resolve(operands.get(0));
                Object b = resolve(operands.get(1));
                return Objects.equals(a, b);
            }
            case "lt" -> {
                Number a = asNumber(resolve(operands.get(0)));
                Number b = asNumber(resolve(operands.get(1)));
                return a.doubleValue() < b.doubleValue();
            }
            case "gt" -> {
                Number a = asNumber(resolve(operands.get(0)));
                Number b = asNumber(resolve(operands.get(1)));
                return a.doubleValue() > b.doubleValue();
            }
            case "and" -> {
                return resolve(operands.get(0)).equals(true) && resolve(operands.get(1)).equals(true);
            }
            case "or" -> {
                return resolve(operands.get(0)).equals(true) || resolve(operands.get(1)).equals(true);
            }
            case "not" -> {
                return !resolve(operands.get(0)).equals(true);
            }
            default -> throw new JPLException("Unknown logical operator: " + op);
        }
    }

    private Number asNumber(Object value) {
        if (value instanceof Number) return (Number) value;
        throw new JPLException("Expected number, got: " + value);
    }

    public Map<String, Object> getVariables() {
        return variables;
    }
}