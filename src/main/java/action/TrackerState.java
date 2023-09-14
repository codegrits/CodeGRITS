package action;

enum TrackerState {
    STOPPED(0),
    STARTED(1),
    PAUSED(2);

    private final int value;

    TrackerState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
