package main.map;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import main.core.GamePanel;
import main.util.AssetManager;

public class MapLoader {

    public static final int DIRT = 0;
    public static final int PLASTIC = 1;
    public static final int WALL = 2;
    public static final int BUSH = 3;
    public static final int SPAWN = 5;
    public static final int LOCK = 6;
    private static final int BREAKING_PLASTIC = -1;
    private static final int PLASTIC_BREAK_FRAMES = 18;
    private static final int PLASTIC_BREAK_PIECES = 4;

    private final GamePanel gp;
    private int[][] backgroundData;
    private final List<PlasticBreakAnimation> plasticBreakAnimations = new ArrayList<>();

    public MapLoader(GamePanel gp) {
        this(gp, "/maps/map01_background.csv");
    }

    public MapLoader(GamePanel gp, String backgroundPath) {
        this.gp = gp;
        this.backgroundData = loadCsvMap(backgroundPath);
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

    public boolean clearPlasticClusterAt(int row, int col) {
        if (getTileAt(row, col) != PLASTIC) {
            return false;
        }

        startPlasticBreakCluster(row, col);
        return true;
    }

    private void startPlasticBreakCluster(int row, int col) {
        if (getTileAt(row, col) != PLASTIC) {
            return;
        }

        backgroundData[row][col] = BREAKING_PLASTIC;
        plasticBreakAnimations.add(new PlasticBreakAnimation(row, col));
        startPlasticBreakCluster(row - 1, col);
        startPlasticBreakCluster(row + 1, col);
        startPlasticBreakCluster(row, col - 1);
        startPlasticBreakCluster(row, col + 1);
    }

    public int[][] copyBackgroundData() {
        return copyMap(backgroundData);
    }

    public void restoreBackgroundData(int[][] data) {
        backgroundData = copyMap(data);
        plasticBreakAnimations.clear();
        for (int row = 0; row < backgroundData.length; row++) {
            for (int col = 0; col < backgroundData[row].length; col++) {
                if (backgroundData[row][col] == BREAKING_PLASTIC) {
                    backgroundData[row][col] = DIRT;
                }
            }
        }
    }

    private static int[][] copyMap(int[][] source) {
        int[][] copy = new int[source.length][];
        for (int row = 0; row < source.length; row++) {
            copy[row] = source[row].clone();
        }
        return copy;
    }

    public boolean isWall(int row, int col) {
        int tile = getTileAt(row, col);
        return tile == WALL
                || tile == PLASTIC
                || tile == BREAKING_PLASTIC
                || tile == LOCK;
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
        drawPlasticBreakAnimations(g2);
    }

    private void drawPlasticBreakAnimations(Graphics2D g2) {
        Iterator<PlasticBreakAnimation> iterator = plasticBreakAnimations.iterator();
        while (iterator.hasNext()) {
            PlasticBreakAnimation animation = iterator.next();
            animation.frame++;
            if (animation.frame > PLASTIC_BREAK_FRAMES) {
                backgroundData[animation.row][animation.col] = DIRT;
                iterator.remove();
                continue;
            }
            drawPlasticBreakAnimation(g2, animation);
        }
    }

    private void drawPlasticBreakAnimation(Graphics2D g2, PlasticBreakAnimation animation) {
        int tileX = animation.col * gp.tileSize;
        int tileY = animation.row * gp.tileSize;
        int pieceSize = gp.tileSize / 2;
        int sourcePieceWidth = AssetManager.plastic.getWidth() / 2;
        int sourcePieceHeight = AssetManager.plastic.getHeight() / 2;
        float alpha = 1f - animation.frame / (float) PLASTIC_BREAK_FRAMES;

        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, alpha)));
        for (int i = 0; i < PLASTIC_BREAK_PIECES; i++) {
            int sourceX = (i % 2) * pieceSize;
            int sourceY = (i / 2) * pieceSize;
            int imageSourceX = (i % 2) * sourcePieceWidth;
            int imageSourceY = (i / 2) * sourcePieceHeight;
            int offsetX = ((i % 2 == 0) ? -1 : 1) * animation.frame;
            int offsetY = ((i / 2 == 0) ? -1 : 1) * animation.frame;
            g2.drawImage(AssetManager.plastic,
                    tileX + sourceX + offsetX,
                    tileY + sourceY + offsetY,
                    tileX + sourceX + offsetX + pieceSize,
                    tileY + sourceY + offsetY + pieceSize,
                    imageSourceX,
                    imageSourceY,
                    imageSourceX + sourcePieceWidth,
                    imageSourceY + sourcePieceHeight,
                    null);
        }
        g2.setComposite(oldComposite);
    }

    private BufferedImage getTileImage(int tileType) {
        switch (tileType) {
            case WALL:
                return AssetManager.wall;
            case PLASTIC:
                return AssetManager.plastic;
            case BREAKING_PLASTIC:
                return AssetManager.dirt;
            case BUSH:
                return AssetManager.bush;
            case SPAWN:
                return AssetManager.spawn;
            case LOCK:
                return AssetManager.tileLock;
            case DIRT:
            default:
                return AssetManager.dirt;
        }
    }

    private static final class PlasticBreakAnimation {
        private final int row;
        private final int col;
        private int frame;

        private PlasticBreakAnimation(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
}
