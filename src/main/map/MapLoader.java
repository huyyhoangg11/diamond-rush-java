package main.map;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import main.core.GamePanel;
import main.util.AssetManager;

public class MapLoader {

    public static final int DIRT = 0;
    public static final int WALL = 1;
    public static final int DIAMOND = 2;
    public static final int ROCK = 3;
    public static final int DOOR = 4;

    private final GamePanel gp;
    private final int[][] mapData;

    public MapLoader(GamePanel gp) {
        this.gp = gp;
        this.mapData = createDefaultMap();
    }

    private int[][] createDefaultMap() {
        int rows = 12;
        int cols = 16;
        int[][] map = new int[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                map[r][c] = DIRT;
                if (r == 0 || c == 0 || r == rows - 1 || c == cols - 1) {
                    map[r][c] = WALL;
                }
            }
        }

        map[3][3] = WALL;
        map[3][4] = WALL;
        map[3][5] = WALL;
        map[6][8] = WALL;
        map[7][8] = WALL;
        map[8][8] = WALL;

        return map;
    }

    public int getTileAt(int row, int col) {
        if (row < 0 || col < 0 || row >= mapData.length || col >= mapData[0].length) {
            return WALL;
        }
        return mapData[row][col];
    }

    public void draw(Graphics2D g2) {
        for (int row = 0; row < mapData.length; row++) {
            for (int col = 0; col < mapData[row].length; col++) {
                BufferedImage tileImage = getTileImage(mapData[row][col]);
                if (tileImage != null) {
                    g2.drawImage(tileImage, col * gp.tileSize, row * gp.tileSize, gp.tileSize, gp.tileSize, null);
                }
            }
        }
    }

    private BufferedImage getTileImage(int tileType) {
        switch (tileType) {
            case WALL:
                return AssetManager.wall;
            case DIAMOND:
                return AssetManager.diamond;
            case ROCK:
                return AssetManager.rock;
            case DOOR:
                return AssetManager.door;
            case DIRT:
            default:
                return AssetManager.dirt;
        }
    }
}
