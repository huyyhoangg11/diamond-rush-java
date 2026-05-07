package main.config;

public class GameConfig {
    // Kích thước chuẩn của 1 ô lưới (grid)
    public static final int ORIGINAL_TILE_SIZE = 16;
    public static final int SCALE = 3;
    public static final int TILE_SIZE = ORIGINAL_TILE_SIZE * SCALE; // 48x48 pixels

    // Kích thước màn hình theo map 20x20
    public static final int MAX_SCREEN_COL = 25;
    public static final int MAX_SCREEN_ROW = 20;
    public static final int SCREEN_WIDTH = TILE_SIZE * MAX_SCREEN_COL;  // 960 pixels
    public static final int SCREEN_HEIGHT = TILE_SIZE * MAX_SCREEN_ROW; // 960 pixels

    // Tốc độ khung hình
    public static final int FPS = 60;
}