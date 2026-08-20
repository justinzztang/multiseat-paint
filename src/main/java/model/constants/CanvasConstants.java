package model.constants;

public class CanvasConstants {
    public static final int MAX_WIDTH = 1000;
    public static final int MAX_HEIGHT = 1000;
    public static final int TILE_SIDE = 64;
    public static final int UNDO_LIMIT = 20;
    public static final int MAX_COMMANDS_PER_USER = UNDO_LIMIT*3/2;
    public static final int EMERGENCY_MAX_COMMANDS_PER_USER = 3;
    public static final int MAX_TIMELINE_SIZE = 100000;
}
