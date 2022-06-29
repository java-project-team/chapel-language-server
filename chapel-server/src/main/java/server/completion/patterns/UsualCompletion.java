package server.completion.patterns;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.InsertTextMode;

public class UsualCompletion extends CompletionItem {
    public UsualCompletion(String label, int kind){
        this.setLabel(label);//just a label
        this.setInsertText(label);//Text that will be inserted, when you press enter
        this.setPreselect(false);//???
        this.setKind(CompletionItemKind.forValue(kind));//???
        this.setSortText(label);//Text, that will be compared during sort
        this.setFilterText(label);//
        this.setInsertTextFormat(InsertTextFormat.PlainText);//???
        this.setInsertTextMode(InsertTextMode.AsIs);//???
    }
}
