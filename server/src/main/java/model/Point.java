package model;


public class Point implements Cloneable {

    private double x;
    private double y;
    private PointState pointState;


    /**
     * gets state of current point
     * @return state
     */
    public PointState getPointState() {
        return pointState;
    }

    /**
     * sets new state for point
     * @param pointState state
     */
    public void setPointState(PointState pointState) {
        this.pointState = pointState;
    }

    /**
     * creates new Point instance
     * @param x x coordinate
     * @param y y coordinate
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * gets x coordinate
     * @return x
     */
    public double getX() {
        return x;
    }

    /**
     * gets y coordinate
     * @return y
     */
    public double getY() {
        return y;
    }

    /**
     * clones current point
     * @return new point instance
     */
    public Point clone() {
        Point result = new Point(this.getX(), this.getY());
        result.setPointState(this.getPointState());
        return result;
    }

    /**
     * compares two points
     * @param obj point to compare with
     * @return true if points are same
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Point point = (Point) obj;

        if (Double.compare(point.x, x) != 0) return false;
        if (Double.compare(point.y, y) != 0) return false;
        return pointState == point.pointState;
    }

    /**
     * gets hascode for current Point
     * @return hashcode value
     */
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
