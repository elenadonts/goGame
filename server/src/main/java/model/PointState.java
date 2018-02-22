package model;

public enum PointState {
    OCCUPIED_WHITE(1),
    OCCUPIED_BLACK(2),
    BLANK(0);

    private int value;

    PointState(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }


}
