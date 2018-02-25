package model;

public class Point {

    private double x;
    private double y;
    private PointState pointState = PointState.BLANK;

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

}
