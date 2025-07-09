package com.kapil.jpl.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapil.jpl.core.JPLContext;
import com.kapil.jpl.core.JPLInterpreter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class REPL {
    private final JPLInterpreter interpreter;
    private final ObjectMapper mapper;
    private final JPLContext context;

    public REPL(JPLInterpreter interpreter) {
        this.interpreter = interpreter;
        this.context = new JPLContext();
        this.mapper = new ObjectMapper();
    }

    public void start() {
        System.out.println("🔁 Welcome to JPL REPL v1.0");
        System.out.println("Type JPL JSON code or type `exit` to quit.");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder buffer = new StringBuilder();

        while (true) {
            try {
                System.out.print("jpl> ");
                String line = reader.readLine();

                if (line == null || line.trim().equalsIgnoreCase("exit")) break;
                if (line.trim().equalsIgnoreCase("clear")) {
                    buffer.setLength(0);
                    System.out.println("[ Cleared ]");
                    continue;
                }
                if (line.trim().equalsIgnoreCase("help")) {
                    System.out.println("💡 Write valid JPL JSON code and press Enter.");
                    System.out.println("Commands: exit, clear, help");
                    continue;
                }

                buffer.append(line.trim());

                // Try parsing JSON
                try {
                    JsonNode node = mapper.readTree(buffer.toString());
                    if (node.isNull() || !node.isObject()) {
                        System.err.println("❌ Invalid JPL JSON. Please enter a valid object.");
                        buffer.setLength(0); // clear buffer on error
                        continue;
                    }
                    // If valid JSON, evaluate it
                    Object result = interpreter.eval(node);
                    if (result==null){
                        Object newresult = context.resolve(node);
                        if (newresult != null) {
                            result = newresult;
                        } else {
                            System.out.println("No result returned.");
                        }

                    }
                    if (result != null) {
                        System.out.println("=> " + result);
                    }
                    buffer.setLength(0); // clear for next input
                } catch (com.fasterxml.jackson.core.JsonParseException ex) {
                    // not yet complete JSON – keep reading
                    buffer.append(" ");
                    System.out.println(ex);
                } catch (IOException e) {
                    System.err.println("❌ Error parsing JSON: " + e.getMessage());
                    buffer.setLength(0); // clear buffer on error
                } catch (Exception e) {

                    buffer.setLength(0); // clear buffer on error

                }
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                buffer.setLength(0);
            }
        }

        System.out.println("👋 Exiting JPL REPL.");
    }

}
