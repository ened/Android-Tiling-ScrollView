package asia.ivity.android.tiledscrollview;

/** Simple tile coordinates (X, Y). */
class Tile {
    public Tile(int x_, int y_) {
        x = x_;
        y = y_;
    }

    int x;
    int y;

    @Override
    public String toString() {
        return "Tile{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tile tile = (Tile) o;

        if (x != tile.x) return false;
        if (y != tile.y) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }
}
