package server.semantic.tokens;

import parser.SimpleNode;
import parser.*;

import java.util.ArrayList;

public class ChapelUseStatement extends ChapelUnnamedStatement {
    public static class UseModuleDeclaration {
        public ArrayList<String> modules = new ArrayList<>();
        public String name = "_";

        @Override
        public String toString() {
            return "modules: " + modules + "," + "name: "  + name;
        }
    }

    public ArrayList<UseModuleDeclaration> useModules = new ArrayList<>();
    public boolean isPublic = false;
    @Override
    public String toString() {
        return useModules.toString() + " public?: " + isPublic;
    }

    public ChapelUseStatement(SimpleNode newRootNode) {
        super(newRootNode);

        if (newRootNode.jjtGetFirstToken().kind == ParserConstants.PUBLIC) {
            isPublic = true;
        }
        for (int i = 0; i < newRootNode.jjtGetNumChildren(); i++) {
            var child = (SimpleNode) newRootNode.jjtGetChild(i);
            if (child.getId() == ParserTreeConstants.JJTMODULEORENUMNAME) {
                UseModuleDeclaration useDec = parseModuleOrEnumName(child);
                if (useDec == null) {
                    continue;
                }
                useModules.add(useDec);
            }
        }
    }

    private UseModuleDeclaration parseModuleOrEnumName(SimpleNode node) {
        var token = node.jjtGetFirstToken();

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            var child = (SimpleNode)node.jjtGetChild(i);
            if (child.getId() == ParserTreeConstants.JJTOPERATOR) {
                return null;
            }
        }
        var ans = new UseModuleDeclaration();
        for (;token.kind != ParserConstants.SEMICOLON; token = token.next) {
            if (token.kind == ParserConstants.ID) {
                ans.modules.add(token.image);
                if (token.next.kind != ParserConstants.ID) {
                    ans.name = token.image;
                }
            }
            if (token.kind == ParserConstants.AS) {
                ans.name = token.next.image;
                break;
            }
        }
        return ans;
    }

}
