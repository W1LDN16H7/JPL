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
        } else if (node.isObject()) {
            if (node.has("add")) return resolveArithmetic(node.get("add"), "+");
            if (node.has("sub")) return resolveArithmetic(node.get("sub"), "-");
            if (node.has("mul")) return resolveArithmetic(node.get("mul"), "*");
            if (node.has("div")) return resolveArithmetic(node.get("div"), "/");
            throw new JPLException("Unknown expression: " + node);
        }
        throw new JPLException("Unsupported node type: " + node);
    }

    private Number resolveArithmetic(JsonNode operands, String op) {
        if (!operands.isArray() || operands.size() != 2)
            throw new JPLException("Invalid operands for " + op);

        Number a = asNumber(resolve(operands.get(0)));
        Number b = asNumber(resolve(operands.get(1)));

        switch (op) {
            case "+": return a.doubleValue() + b.doubleValue();
            case "-": return a.doubleValue() - b.doubleValue();
            case "*": return a.doubleValue() * b.doubleValue();
            case "/": return a.doubleValue() / b.doubleValue();
        }
        throw new JPLException("Unknown operator: " + op);
    }

    private Number asNumber(Object value) {
        if (value instanceof Number) return (Number) value;
        throw new JPLException("Expected number, got: " + value);
    }
}
