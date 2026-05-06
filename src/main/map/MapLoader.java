package main.map;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import main.core.GamePanel;
import main.util.AssetManager;

public class MapLoader {

    public static final int DIRT = 0;
    public static final int WALL = 2;
    public static final int BUSH = 3;
    public static final int SPAWN = 5;

    private final GamePanel gp;
    private final int[][] backgroundData;

    public MapLoader(GamePanel gp) {
        this.gp = gp;
        this.backgroundData = loadCsvMap("/maps/map01_background.csv");
    }

    public int getRows() {
        return backgroundData.length;
    }

    public int getCols() {
        return backgroundData[0].length;
    }

    public int getTileAt(int row, int col) {
        if (row < 0 || col < 0 || row >= backgroundData.length || col >= backgroundData[0].length) {
            return WALL;
        }
        return backgroundData[row][col];
    }

    public void clearBushAt(int row, int col) {
        if (getTileAt(row, col) == BUSH) {
            backgroundData[row][col] = DIRT;
        }
    }

    public boolean isWall(int row, int col) {
        return getTileAt(row, col) == WALL;
    }

    public boolean isGround(int row, int col) {
        int tile = getTileAt(row, col);
        return tile == DIRT || tile == SPAWN;
    }

    public int getSpawnRow() {
        for (int row = 0; row < backgroundData.length; row++) {
            for (int col = 0; col < backgroundData[row].length; col++) {
                if (backgroundData[row][col] == SPAWN) {
                    return row;
                }
            }
        }
        return -1;
    }

    public int getSpawnCol() {
        for (int row = 0; row < backgroundData.length; row++) {
            for (int col = 0; col < backgroundData[row].length; col++) {
                if (backgroundData[row][col] == SPAWN) {
                    return col;
                }
            }
        }
        return -1;
    }

    public static int[][] loadCsvMap(String resourcePath) {
        try (InputStream inputStream = MapLoader.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalStateException("Map not found: " + resourcePath);
            }
            return parseCsv(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load map: " + resourcePath, e);
        }
    }

    private static int[][] parseCsv(InputStream inputStream) throws IOException {
        List<int[]> rows = new ArrayList<>();
        int expectedCols = -1;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] values = line.split(",");
                if (expectedCols == -1) {
                    expectedCols = values.length;
                } else if (values.length != expectedCols) {
                    throw new IllegalStateException("CSV map has rows with different column counts");
                }

                int[] row = new int[values.length];
                for (int i = 0; i < values.length; i++) {
                    row[i] = Integer.parseInt(values[i].trim());
                }
                rows.add(row);
            }
        }

        if (rows.isEmpty()) {
            throw new IllegalStateException("CSV map is empty");
        }

        return rows.toArray(new int[0][]);
    }

    public void draw(Graphics2D g2) {
        for (int row = 0; row < backgroundData.length; row++) {
            for (int col = 0; col < backgroundData[row].length; col++) {
                BufferedImage tileImage = getTileImage(backgroundData[row][col]);
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
            case BUSH:
                return AssetManager.bush;
            case SPAWN:
                return AssetManager.spawn;
            case DIRT:
            default:
                return AssetManager.dirt;
        }
    }
}
