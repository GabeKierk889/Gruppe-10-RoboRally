package dk.dtu.compute.se.pisd.roborally.model;

public class CheckPoint {
    private int CHECKPOINTNUMBER;
    public static int highestCheckPointNumber = 0;
    private final int x;
    private final int y;

    public CheckPoint(int x, int y) {
        this.x = x;
        this.y = y;
        highestCheckPointNumber++;
        CHECKPOINTNUMBER = highestCheckPointNumber;
    }

    public int getCheckpointNumber() {
        return CHECKPOINTNUMBER;
    }

    public void setCheckpointNumber(int number) {
        CHECKPOINTNUMBER = number;
        if (number > highestCheckPointNumber)
            highestCheckPointNumber = number;
    }

    public int getXcoordinate() {
        return x;
    }

    public int getYcoordinate() {
        return y;
    }
}
