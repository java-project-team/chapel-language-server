package navigation;

public class Position {
    int line, column;

    Position() {
        line = -1;
        column = -1;
    }

    Position(int line, int column) {
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return line + ", " + column;
    }

    public static final Position NONE = new Position();
}