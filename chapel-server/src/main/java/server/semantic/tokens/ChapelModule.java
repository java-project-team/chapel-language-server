package server.semantic.tokens;

import parser.SimpleNode;

import java.util.HashMap;
import java.util.HashSet;

public class ChapelModule {
    private final String name;
    private final SimpleNode contentNode;
    private final HashMap<String, ChapelModule> modules = new HashMap<>();
    private final HashMap<String, ChapelProcedure> procedures = new HashMap<>();
    private final HashSet<String> variables = new HashSet<>();

    public ChapelModule(SimpleNode node, String name) {
        this.contentNode = node;
        this.name = name;
    }

    public SimpleNode getContentNode() {
        return contentNode;
    }

    @Override
    public String toString() {
        return String.join(
                "\n",
                contentNode.toString(),
                modules.toString(),
                procedures.toString(),
                variables.toString());
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

    public HashSet<String> getVariables() {
        return variables;
    }
}
