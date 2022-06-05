package server.semantic.tokens;

import parser.SimpleNode;

import java.util.HashMap;

public class ChapelModule {
    private final String name;
    private final SimpleNode contentNode;
    private final HashMap<String, ChapelModule> modules = new HashMap<>();
    private final HashMap<String, ChapelProcedure> procedures = new HashMap<>();

    public ChapelModule(SimpleNode node, String name) {
        this.contentNode = node;
        this.name = name;
    }

    public SimpleNode getContentNode() {
        return contentNode;
    }

    @Override
    public String toString() {
        return String.join("\n", contentNode.toString(), modules.toString(), procedures.toString());
    }

    public HashMap<String, ChapelModule> getModules() {
        return modules;
    }

    public HashMap<String, ChapelProcedure> getProcedures() {
        return procedures;
    }

    public String getName() {
        return name;
    }
}
