package requests;

import org.checkerframework.checker.units.qual.A;
import parser.Parser;
import parser.SimpleNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileInformation {
    private final String path;
    private String nameModule;
    private boolean isChanged;
    private FileInformation parentModule;
    private List<FileInformation> modules;
    private List<DefinitionVariable> variables;
    private List<DefinitionFunction> functions;
    private SimpleNode root;

    public FileInformation(String path) {
        nameModule = null;
        this.path = path;
        isChanged = true;
        variables = new ArrayList<>();
        functions = new ArrayList<>();
        parentModule = null;
        modules = new ArrayList<>();
        root = null;
        update(false);
    }
    public FileInformation(String path, FileInformation parentModule, SimpleNode root) {
        nameModule = null;
        this.path = path;
        isChanged = true;
        variables = new ArrayList<>();
        functions = new ArrayList<>();
        this.parentModule = parentModule;
        modules = new ArrayList<>();
        this.root = root;
        update(true);
    }

    public List<DefinitionVariable> getVariables() {
        return variables;
    }

    public List<DefinitionFunction> getFunctions() {
        return functions;
    }

    public SimpleNode getRoot() {
        return root;
    }

    public FileInformation update() {
        return update(false);
    }

    private FileInformation update(boolean isParse) {
        if (!isChanged) {
            return this;
        }
        variables.clear();
        functions.clear();
        modules.clear();
        //isChanged = false; // TODO как-то обновлять надо

        if (!isParse) {
            root = Parser.parse(path);
        }
        if (root.toString() == "ModuleDeclarationStatement") {
            nameModule = root.jjtGetFirstToken().image;
        }
        if (root != null) {
            for (int i = 0; i < root.jjtGetNumChildren(); i++) {
                if (Objects.equals(root.jjtGetChild(i).toString(), "Statement")) {
                    if (Objects.equals(root.jjtGetChild(i).jjtGetChild(0).toString(), "VariableDeclarationStatement")) {
                        SimpleNode statement = (SimpleNode) root.jjtGetChild(i).jjtGetChild(0);
                        for (int j = 0; j < statement.jjtGetNumChildren(); j++) {
                            if (Objects.equals(statement.jjtGetChild(j).toString(), "VariableDeclaration")) {
                                variables.add(new DefinitionVariable((SimpleNode) statement.jjtGetChild(j)));
                            }
                        }
                    } else if (Objects.equals(root.jjtGetChild(i).jjtGetChild(0).toString(), "ProcedureDeclarationStatement") || Objects.equals(root.jjtGetChild(i).jjtGetChild(0).toString(), "MethodDeclarationStatement")) {
                        functions.add(new DefinitionFunction((SimpleNode) root.jjtGetChild(i).jjtGetChild(0)));
                    } else if (Objects.equals(root.jjtGetChild(i).jjtGetChild(0).toString(), "ModuleDeclarationStatement")) {
                        modules.add(new FileInformation(path, this, (SimpleNode)root.jjtGetChild(i).jjtGetChild(0)));
                    }
                }
            }
        }
        return this;
    }
}