
package ru.hse.fmcs;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.jupiter.api.Test;
import parser.Parser;
import parser.SimpleNode;
import requests.BasicProcessing;
import requests.DefinitionProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestsDefinitionProvider {
    private static final SimpleNode root = Parser.parse("./src/test/resources/a.txt");
    private static final DefinitionProvider definitionProvider = new DefinitionProvider(new BasicProcessing(List.of()));

    @Test
    public void test1() {
        Location location = new Location("./src/test/resources/a.txt", new Range(new Position(2, 1), new Position(2, 1)));

        var res = definitionProvider.findDumb(location);
        /*assertNotNull(res);
        assertEquals(1, res.size());
        SimpleNode ans = res.get(0).getKey();

        assertNotNull(ans);
        assertEquals(1, ans.jjtGetFirstToken().beginLine);
        assertEquals(1, ans.jjtGetFirstToken().beginColumn);
        assertEquals(1, ans.jjtGetLastToken().endLine);
        assertEquals(11, ans.jjtGetLastToken().endColumn);*/
    }
/*
    @Test
    public void test2() {
        Location location = new Location("./src/test/resources/a.txt", new Range(new Position(7, 5), new Position(7, 5)));

        var res = definitionProvider.find(location, root);
        assertNotNull(res);
        assertEquals(1, res.size());
        SimpleNode ans = res.get(0).getKey();

        assertNotNull(ans);
        assertEquals(6, ans.jjtGetFirstToken().beginLine);
        assertEquals(5, ans.jjtGetFirstToken().beginColumn);
        assertEquals(6, ans.jjtGetLastToken().endLine);
        assertEquals(15, ans.jjtGetLastToken().endColumn);
    }

    @Test
    public void test3() {
        Location location = new Location("./src/test/resources/a.txt", new Range(new Position(10, 1), new Position(10, 1)));

        var res = definitionProvider.find(location, root);
        assertEquals(0, res.size());
    }

    @Test
    public void test4() {
        Location location = new Location("./src/test/resources/a.txt", new Range(new Position(13, 1), new Position(13, 1)));

        var res = definitionProvider.find(location, root);
        assertNotNull(res);
        assertEquals(1, res.size());
        SimpleNode ans = res.get(0).getKey();

        assertNotNull(ans);
        assertEquals(12, ans.jjtGetFirstToken().beginLine);
        assertEquals(1, ans.jjtGetFirstToken().beginColumn);
        assertEquals(12, ans.jjtGetLastToken().endLine);
        assertEquals(10, ans.jjtGetLastToken().endColumn);
    }

    @Test
    public void test5() {
        Location location = new Location("./src/test/resources/a.txt", new Range(new Position(20, 1), new Position(20, 1)));

        var res = definitionProvider.find(location, root);
        assertNotNull(res);
        assertEquals(1, res.size());
        SimpleNode ans = res.get(0).getKey();

        assertNotNull(ans);
        assertEquals(19, ans.jjtGetFirstToken().beginLine);
        assertEquals(1, ans.jjtGetFirstToken().beginColumn);
        assertEquals(19, ans.jjtGetLastToken().endLine);
        assertEquals(36, ans.jjtGetLastToken().endColumn);
    }

    @Test
    public void test6() {
        Location location = new Location("./src/test/resources/a.txt", new Range(new Position(22, 1), new Position(22, 1)));

        var res = definitionProvider.find(location, root);
        assertNotNull(res);
        assertEquals(1, res.size());
        SimpleNode ans = res.get(0).getKey();

        assertNotNull(ans);
        assertEquals(16, ans.jjtGetFirstToken().beginLine);
        assertEquals(1, ans.jjtGetFirstToken().beginColumn);
        assertEquals(17, ans.jjtGetLastToken().endLine);
        assertEquals(1, ans.jjtGetLastToken().endColumn);
    } // он возвращает вместе с телом определения
 */
}
