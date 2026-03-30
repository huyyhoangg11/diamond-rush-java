package main.config;

public class GameConfig {
    // Kích thước chuẩn của 1 ô lưới (grid)
    public static final int ORIGINAL_TILE_SIZE = 16;
    public static final int SCALE = 3;
    public static final int TILE_SIZE = ORIGINAL_TILE_SIZE * SCALE; // 48x48 pixels

    // Kích thước màn hình (Ví dụ: 16 ô chiều ngang, 12 ô chiều dọc)
    public static final int MAX_SCREEN_COL = 16;
    public static final int MAX_SCREEN_ROW = 12;
    public static final int SCREEN_WIDTH = TILE_SIZE * MAX_SCREEN_COL;  // 768 pixels
    public static final int SCREEN_HEIGHT = TILE_SIZE * MAX_SCREEN_ROW; // 576 pixels

    // Tốc độ khung hình
    public static final int FPS = 60;
}