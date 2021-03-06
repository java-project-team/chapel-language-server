package requests;

import org.eclipse.xtext.xbase.lib.Pair;
import parser.Parser;
import parser.SimpleNode;
import server.semantic.tokens.ChapelUseStatement;

import java.util.*;

public class FileInformation {
    private final String path;
    private String nameModule;
    boolean isPublic;
    private boolean isChanged;
    private FileInformation parentModule;
    private Map<String, FileInformation> inModules;
    private ChapelUseStatement useStatement;
    private List<Pair<List<String>, Boolean>> useModules;
    private List<DefinitionVariable> variables;
    private List<DefinitionFunction> functions;
    private SimpleNode root;

    public FileInformation(String path) {
        nameModule = null;
        this.path = path;
        isPublic = true;
        isChanged = true;
        variables = new ArrayList<>();
        functions = new ArrayList<>();
        parentModule = null;
        inModules = new HashMap<>();
        useModules = new ArrayList<>();
        root = null;
        useStatement = null;
        update(false);
    }

    public FileInformation(String path, FileInformation parentModule, SimpleNode root) {
        nameModule = null;
        this.path = path;
        isChanged = true;
        isPublic = true;
        variables = new ArrayList<>();
        functions = new ArrayList<>();
        this.parentModule = parentModule;
        inModules = new HashMap<>();
        useModules = new ArrayList<>();
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

    public Map<String, FileInformation> getInModules() {
        return inModules;
    }

    public List<Pair<List<String>, Boolean>> getUseModules() {
        return useModules;
    }

    public ChapelUseStatement getUseStatement() {
        return useStatement;
    }

    public SimpleNode getRoot() {
        return root;
    }

    public FileInformation update() {
        return update(false);
    }

    public FileInformation getParentModule() {
        return parentModule;
    }

    private FileInformation update(boolean isParse) {
        if (!isChanged) {
            return this;
        }
        variables.clear();
        functions.clear();
        inModules.clear();
        //isChanged = false;

        if (!isParse) {
            root = Parser.parse(path);
        }
        if (root != null && Objects.equals(root.toString(), "ModuleDeclarationStatement")) {
            if (Objects.equals(root.jjtGetChild(0).toString(), "PrivacySpecifier")
                    && !Objects.equals(((SimpleNode) root.jjtGetChild(0)).jjtGetFirstToken().image, "public")) {
                isPublic = false;
            }
            nameModule = root.jjtGetFirstToken().next.image;
            if (Objects.equals(nameModule, "module")) {
                nameModule = root.jjtGetFirstToken().next.next.image;
            }
        }
        if (root != null) {
            useStatement = new ChapelUseStatement(root);
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
                        String name = ((SimpleNode) root.jjtGetChild(i).jjtGetChild(0)).jjtGetFirstToken().next.image;
                        if (Objects.equals(name, "module")) {
                            name = ((SimpleNode) root.jjtGetChild(i).jjtGetChild(0)).jjtGetFirstToken().next.next.image;
                        }
                        inModules.put(name, new FileInformation(path, this, (SimpleNode) root.jjtGetChild(i).jjtGetChild(0)));
                    } else if (Objects.equals(root.jjtGetChild(i).jjtGetChild(0).toString(), "UseStatement")) {
                        SimpleNode useStatement = (SimpleNode) root.jjtGetChild(i).jjtGetChild(0);
                        Pair<List<String>, Boolean> p;
                        if (Objects.equals(useStatement.jjtGetChild(0).toString(), "PrivacySpecifier") && Objects.equals(((SimpleNode) useStatement.jjtGetChild(0)).jjtGetFirstToken().image, "public")) {
                            p = new Pair<>(new ArrayList<>(), true);
                        } else {
                            p = new Pair<>(new ArrayList<>(), false);
                        }
                        for (int j = 0; j < useStatement.jjtGetNumChildren(); j++) {
                            if (Objects.equals(useStatement.jjtGetChild(j).toString(), "ModuleOrEnumName")) {
                                for (int k = 0; k < useStatement.jjtGetChild(j).jjtGetNumChildren(); k++) {
                                    if (Objects.equals(useStatement.jjtGetChild(j).jjtGetChild(k).toString(), "Identifier")) {
                                        p.getKey().add(((SimpleNode) useStatement.jjtGetChild(j).jjtGetChild(k)).jjtGetFirstToken().image);
                                    }
                                }
                            }
                        }
                        var localPath = Vertex.findModule((SimpleNode) root.jjtGetParent()).getValue();
                        if (p.getKey().isEmpty() || (!localPath.isEmpty() && !Objects.equals(p.getKey().get(0), localPath.get(0)))) {
                            localPath.addAll(p.getKey());
                            p.getKey().clear();
                            p.getKey().addAll(localPath);
                        }
                        useModules.add(p);
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
                                    String name = ((SimpleNode) block.jjtGetChild(j).jjtGetChild(0)).jjtGetFirstToken().next.image;
                                    if (Objects.equals(name, "module")) {
                                        name = ((SimpleNode) block.jjtGetChild(j).jjtGetChild(0)).jjtGetFirstToken().next.next.image;
                                    }
                                    inModules.put(name, new FileInformation(path, this, (SimpleNode) block.jjtGetChild(j).jjtGetChild(0)));
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