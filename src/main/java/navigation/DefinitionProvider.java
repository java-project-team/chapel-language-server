package navigation;

import parser.SimpleNode;
import java.nio.file.Path;
import java.util.List;

public class DefinitionProvider {
    private final SimpleNode root;
    private final Path file;
    private final Position position;

    public static final List<Location> NOT_SUPPORTED = List.of();

    public DefinitionProvider(Path file, SimpleNode root, int line, int column) {
        this(file, root, new Position(line, column));
    }

    public DefinitionProvider(Path file, SimpleNode root, Position position) {
        this.file = file;
        this.root = root;
        this.position = position;
    }

    public List<Location> find() {
        // TODO
        return NOT_SUPPORTED;
    }
}