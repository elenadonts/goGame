package model;

public enum PointState {
    STONE_WHITE(1),
    STONE_BLACK(2),
    TARGET(3),
    USED(4),
    BLOCKED(5),
    BLANK(0);

    private int value;

    /**
     * creates new PointState with given value
     * @param value
     */
    PointState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
