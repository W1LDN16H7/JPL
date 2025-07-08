package com.kapil.jpl;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonLocation;
import java.io.*;
import java.util.*;

public class JPLInterpreter {
    private final JPLContext context = new JPLContext();

    public void execute(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try {
            root = mapper.readTree(file);
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