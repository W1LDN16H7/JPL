package com.kapil.jpl;

import com.fasterxml.jackson.databind.*;
import java.io.*;
import java.util.*;

public class JPLInterpreter {
    private final JPLContext context = new JPLContext();

    public void execute(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(file);

        if (root.isArray()) {
            for (JsonNode node : root) {
                eval(node);
            }
        } else {
            eval(root);
        }
    }

    private Object eval(JsonNode node) {
        if (node.has("let")) {
            return context.handleLet(node.get("let"));
        } else if (node.has("print")) {
            Object value = context.resolve(node.get("print"));
            System.out.println(value);
            return null;
        } else {
            throw new JPLException("Unknown instruction: " + node);
        }
    }
}