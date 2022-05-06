package navigation;

public class Range {
    public Position begin, end;

    public Range() {}

    public Range(Position start, Position end) {
        this.begin = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return begin + " -- " + end;
    }

    public static final Range NONE = new Range(Position.NONE, Position.NONE);
}