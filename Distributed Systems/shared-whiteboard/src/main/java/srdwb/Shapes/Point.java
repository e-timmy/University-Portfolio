package srdwb.Shapes;

import java.util.Objects;

// Source: https://stackoverflow.com/questions/70371259/store-x-y-coordinates-in-an-arraylist
public class Point {
    public int x, y;

    public Point() {

    }
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Point [x=" + x + ", y=" + y + "]";
    }
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Point other = (Point) obj;
        return x == other.x && y == other.y;
    }
}