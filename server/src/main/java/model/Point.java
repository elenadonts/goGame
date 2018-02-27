package model;


public class Point {

    private double x;
    private double y;
    private PointState pointState;


    public PointState getPointState() {
        return pointState;
    }

    public void setPointState(PointState pointState) {
        this.pointState = pointState;
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Point clone () {
        Point result = new Point(this.getX(), this.getY());
        result.setPointState(this.getPointState());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        if (Double.compare(point.x, x) != 0) return false;
        if (Double.compare(point.y, y) != 0) return false;
        return pointState == point.pointState;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (pointState != null ? pointState.hashCode() : 0);
        return result;
    }

}
