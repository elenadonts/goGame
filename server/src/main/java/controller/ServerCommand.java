package controller;

public enum ServerCommand {
    STOP("stop"),
    RESTART("restart"),
    UNKNOWN_COMMAND("");


    ServerCommand(String name) {
        this.name = name;
    }

    private String name;

    public String getName(){
        return name;
    }
}
