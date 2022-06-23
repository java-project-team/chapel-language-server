package requests;

import parser.Parser;
import parser.SimpleNode;
import server.semantic.tokens.ChapelUseStatement;

import java.util.*;

public class FileInformation {
    private final String path;
    private String nameModule;
    private boolean isChanged;
    private FileInformation parentModule;
    private Map<String, FileInformation> useModules;
    private ChapelUseStatement useStatement;
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
        useModules = new HashMap<>();
        root = null;
        useStatement = null;
        update(false);
    }

    public FileInformation(String path, FileInformation parentModule, SimpleNode root) {
        nameModule = null;
        this.path = path;
        isChanged = true;
        variables = new ArrayList<>();
        functions = new ArrayList<>();
        this.parentModule = parentModule;
        useModules = new HashMap<>();
        this.root = root;
        useStatement = null;
        update(true);
    }

    public List<DefinitionVariable> getVariables() {
        return variables;
    }

    public List<DefinitionFunction> getFunctions() {
        return functions;
    }

    public String getNameModule() {
        return nameModule;
    }

    public Map<String, FileInformation> getUseModules() {
        return useModules;
    }

    public SimpleNode getRoot() {
        return root;
    }

    public FileInformation update() {
        return update(false);
    }

    public FileInformation getParentModule() { return parentModule;}

    private FileInformation update(boolean isParse) {
        if (!isChanged) {
            return this;
        }
        variables.clear();
        functions.clear();
        useModules.clear();
        //isChanged = false; // TODO как-то обновлять надо

        if (!isParse) {
            root = Parser.parse(path);
        }
        if (root != null) {
            useStatement = new ChapelUseStatement(root);
        }
        if (root != null && Objects.equals(root.toString(), "ModuleDeclarationStatement")) {
            nameModule = root.jjtGetFirstToken().next.image;
        }
        if (root != null) {
            for (int i = 0; i < root.jjtGetNumChildren(); i++) {
                if (Objects.equals(root.jjtGetChild(i).toString(), "Statement")) {
                    if (Objects.equals(root.jjtGetChild(i).jjtGetChild(0).toString(), "VariableDeclarationStatement")) {
                        SimpleNode statement = (SimpleNode) root.jjtGetChild(i).jjtGetChild(0);
                        for (int j = 0; j < statement.jjtGetNumChildren(); j++) {
                            if (Objects.equals(statement.jjtGetChild(j).toString(), "VariableDeclarationList")) {
                                for (int k = 0; k < statement.jjtGetChild(j).jjtGetNumChildren(); k++) {
                                    if (Objects.equals(statement.jjtGetChild(j).jjtGetChild(k).toString(), "VariableDeclaration")) {
                                        variables.add(new DefinitionVariable((SimpleNode) statement.jjtGetChild(j).jjtGetChild(k)));
                                    }
                                }
                            }
                        }
                    } else if (Objects.equals(root.jjtGetChild(i).jjtGetChild(0).toString(), "ProcedureDeclarationStatement") || Objects.equals(root.jjtGetChild(i).jjtGetChild(0).toString(), "MethodDeclarationStatement")) {
                        functions.add(new DefinitionFunction((SimpleNode) root.jjtGetChild(i).jjtGetChild(0)));
                    } else if (Objects.equals(root.jjtGetChild(i).jjtGetChild(0).toString(), "ModuleDeclarationStatement")) {
                        useModules.put(((SimpleNode) root.jjtGetChild(i).jjtGetChild(0)).jjtGetFirstToken().next.image, new FileInformation(path, this, (SimpleNode) root.jjtGetChild(i).jjtGetChild(0)));
                    } else if (Objects.equals(root.jjtGetChild(i).jjtGetChild(0).toString(), "BlockStatement")) {
                        SimpleNode block = (SimpleNode) root.jjtGetChild(i).jjtGetChild(0);
                        for (int j = 0; j < block.jjtGetNumChildren(); j++) {
                            if (Objects.equals(block.jjtGetChild(j).toString(), "Statement")) {
                                if (Objects.equals(block.jjtGetChild(j).jjtGetChild(0).toString(), "VariableDeclarationStatement")) {
                                    SimpleNode statement = (SimpleNode) block.jjtGetChild(j).jjtGetChild(0);
                                    for (int k = 0; k < statement.jjtGetNumChildren(); k++) {
                                        if (Objects.equals(statement.jjtGetChild(k).toString(), "VariableDeclarationList")) {
                                            for (int l = 0; l < statement.jjtGetChild(k).jjtGetNumChildren(); l++) {
                                                if (Objects.equals(statement.jjtGetChild(k).jjtGetChild(l).toString(), "VariableDeclaration")) {
                                                    variables.add(new DefinitionVariable((SimpleNode) statement.jjtGetChild(k).jjtGetChild(l)));
                                                }
                                            }
                                        }
                                    }
                                } else if (Objects.equals(block.jjtGetChild(j).jjtGetChild(0).toString(), "ProcedureDeclarationStatement") || Objects.equals(root.jjtGetChild(i).jjtGetChild(0).toString(), "MethodDeclarationStatement")) {
                                    functions.add(new DefinitionFunction((SimpleNode) block.jjtGetChild(j).jjtGetChild(0)));
                                } else if (Objects.equals(block.jjtGetChild(j).jjtGetChild(0).toString(), "ModuleDeclarationStatement")) {
                                    useModules.put(((SimpleNode) block.jjtGetChild(j).jjtGetChild(0)).jjtGetFirstToken().next.image, new FileInformation(path, this, (SimpleNode) block.jjtGetChild(j).jjtGetChild(0)));
                                }
                            }
                        }
                    }


                }
            }
        }
        return this;
    }
}