package main.map;

import java.awt.Color;
import java.awt.Graphics2D;

import main.core.GamePanel;

public class MapLoader {

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
                if (r == 0 || c == 0 || r == rows - 1 || c == cols - 1) {
                    map[r][c] = 1;
                }
            }
        }

        map[3][3] = 1;
        map[3][4] = 1;
        map[3][5] = 1;
        map[6][8] = 1;
        map[7][8] = 1;
        map[8][8] = 1;

        return map;
    }

    public int getTileAt(int row, int col) {
        if (row < 0 || col < 0 || row >= mapData.length || col >= mapData[0].length) {
            return 1;
        }
        return mapData[row][col];
    }

    public void draw(Graphics2D g2) {
        for (int row = 0; row < mapData.length; row++) {
            for (int col = 0; col < mapData[row].length; col++) {
                int tile = mapData[row][col];
                g2.setColor(tile == 1 ? Color.GRAY : Color.BLACK);
                g2.fillRect(col * gp.tileSize, row * gp.tileSize, gp.tileSize, gp.tileSize);
            }
        }
    }
}
