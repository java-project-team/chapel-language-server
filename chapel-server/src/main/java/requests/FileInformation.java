package requests;

import parser.Parser;
import parser.SimpleNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileInformation {
    private final String path;
    private boolean isChanged;
    private List<DefinitionVariable> variables;
    private List<DefinitionFunction> functions;
    private SimpleNode root;

    public FileInformation(String path) {
        this.path = path;
        isChanged = true;
        variables = new ArrayList<>();
        functions = new ArrayList<>();
        root = null;
        update();
    }

    public List<DefinitionVariable> getVariables() {
        update();
        return variables;
    }

    public List<DefinitionFunction> getFunctions() {
        update();
        return functions;
    }

    public SimpleNode getRoot() {
        update();
        return root;
    }

    public FileInformation update() {
        if (!isChanged) {
            return this;
        }
        variables.clear();
        functions.clear();
        //isChanged = false;

        var root = Parser.parse(path);
        this.root = root;
        if (root != null) {
            //root = (SimpleNode) root.jjtGetChild(0);

            for (int i = 0; i < root.jjtGetNumChildren(); i++) {
                if (Objects.equals(root.jjtGetChild(i).toString(), "Statement")) {
                    if (Objects.equals(root.jjtGetChild(i).jjtGetChild(0).toString(), "VariableDeclarationStatement")) {
                        SimpleNode statement = (SimpleNode) root.jjtGetChild(i).jjtGetChild(0);
                        for (int j = 0; j < statement.jjtGetNumChildren(); j++) {
                            if (Objects.equals(statement.jjtGetChild(j).toString(), "VariableDeclaration")) {
                                variables.add(new DefinitionVariable((SimpleNode) statement.jjtGetChild(j)));
                            }
                        }
                    } else if (Objects.equals(root.jjtGetChild(i).jjtGetChild(0).toString(), "ProcedureDeclarationStatement")) {
                        functions.add(new DefinitionFunction((SimpleNode) root.jjtGetChild(i).jjtGetChild(0)));
                    }
                }
            }
        }
        return this;
    }
}