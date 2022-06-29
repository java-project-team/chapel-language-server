package requests;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import server.completion.patterns.UsualCompletion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CompletionProvider {
    List<CompletionItem> listOfItems;

    public CompletionProvider() {
        listOfItems = new ArrayList<>();
        listOfItems.add(new UsualCompletion("module ", 9));
        listOfItems.add(new UsualCompletion("proc ", 2));
        listOfItems.add(new UsualCompletion("var ", 6));
    }

    public Either<List<CompletionItem>, CompletionList> getCompletion() {
        return Either.forLeft(listOfItems);
    }

    public void addUsualCompletion(String value, int kind) {
        listOfItems.add(new UsualCompletion(value, kind));
    }

    public void addFieldOrMethod(String name, String fieldOrMethod, int kind) {
        String fullLabel = name + "." + fieldOrMethod;
        var fitComplition = new UsualCompletion(fullLabel, kind);
        fitComplition.setSortText(fieldOrMethod);
        fitComplition.setFilterText(fieldOrMethod);
        fitComplition.setInsertText(fieldOrMethod);

        if(kind ==  2){
            fitComplition.setInsertText(fieldOrMethod + "()");
        }

        listOfItems.add(fitComplition);
    }

    public void addModule(String name) {
        addUsualCompletion(name, 9);
    }
}
