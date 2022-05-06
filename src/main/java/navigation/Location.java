package navigation;

import java.net.URI;

public class Location {
    public URI uri;
    public Range range;

    public Location() {
        uri = null;
        range = Range.NONE;
    }

    public Location(URI uri, Range range) {
        this.uri = uri;
        this.range = range;
    }

    public static final Location NONE = new Location();
}
