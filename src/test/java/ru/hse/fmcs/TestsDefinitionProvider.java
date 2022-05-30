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

public class TestsDefinitionProvider {
    private static final SimpleNode root = Parser.parse("./src/test/resources/a.txt");
    private static final DefinitionProvider definitionProvider = new DefinitionProvider(new BasicProcessing(List.of()));

    @Test
    public void test1() {
        Location location = new Location("./src/test/resources/a.txt", new Range(new Position(2, 1), new Position(2, 1)));
        SimpleNode res = definitionProvider.find(location, root);

        assertNotNull(res);
        assertEquals(1, res.jjtGetFirstToken().beginLine);
        assertEquals(1, res.jjtGetFirstToken().beginColumn);
        assertEquals(1, res.jjtGetLastToken().endLine);
        assertEquals(11, res.jjtGetLastToken().endColumn);
    }

    @Test
    public void test2() {
        Location location = new Location("./src/test/resources/a.txt", new Range(new Position(7, 5), new Position(7, 5)));
        SimpleNode res = definitionProvider.find(location, root);

        assertNotNull(res);
        assertEquals(6, res.jjtGetFirstToken().beginLine);
        assertEquals(5, res.jjtGetFirstToken().beginColumn);
        assertEquals(6, res.jjtGetLastToken().endLine);
        assertEquals(15, res.jjtGetLastToken().endColumn);
    }

    @Test
    public void test3() {
        Location location = new Location("./src/test/resources/a.txt", new Range(new Position(10, 1), new Position(10, 1)));
        SimpleNode res = definitionProvider.find(location, root);

        assertNull(res);
    }

    @Test
    public void test4() {
        Location location = new Location("./src/test/resources/a.txt", new Range(new Position(13, 1), new Position(13, 1)));
        SimpleNode res = definitionProvider.find(location, root);

        assertNotNull(res);
        assertEquals(12, res.jjtGetFirstToken().beginLine);
        assertEquals(1, res.jjtGetFirstToken().beginColumn);
        assertEquals(12, res.jjtGetLastToken().endLine);
        assertEquals(10, res.jjtGetLastToken().endColumn);
    }

    @Test
    public void test5() {
        Location location = new Location("./src/test/resources/a.txt", new Range(new Position(20, 1), new Position(20, 1)));
        SimpleNode res = definitionProvider.find(location, root);

        assertNotNull(res);
        assertEquals(19, res.jjtGetFirstToken().beginLine);
        assertEquals(1, res.jjtGetFirstToken().beginColumn);
        assertEquals(19, res.jjtGetLastToken().endLine);
        assertEquals(36, res.jjtGetLastToken().endColumn);
    }

    @Test
    public void test6() {
        Location location = new Location("./src/test/resources/a.txt", new Range(new Position(22, 1), new Position(22, 1)));
        SimpleNode res = definitionProvider.find(location, root);

        assertNotNull(res);
        assertEquals(16, res.jjtGetFirstToken().beginLine);
        assertEquals(1, res.jjtGetFirstToken().beginColumn);
        assertEquals(17, res.jjtGetLastToken().endLine);
        assertEquals(1, res.jjtGetLastToken().endColumn);
    } // он возвращает вместе с телом определения
}