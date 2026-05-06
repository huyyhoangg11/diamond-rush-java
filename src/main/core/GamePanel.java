package main.core;

import main.config.GameConfig;
import main.entity.Enemy;
import main.entity.Player;
import main.input.KeyHandler;
import main.map.MapLoader;
import main.object.Diamond;
import main.object.Door;
import main.object.GameObject;
import main.object.Rock;
import main.ui.UI;
import main.util.AssetManager;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {

    private static final int OBJ_EMPTY = 0;
    private static final int OBJ_ROCK = 1;
    private static final int OBJ_DIAMOND = 2;
    private static final int OBJ_DOOR = 3;
    private static final int OBJ_SNAKE = 4;
    private static final int OBJ_SPAWN = 5;

    public final int tileSize = GameConfig.TILE_SIZE;

    KeyHandler keyH = new KeyHandler();
    public Player player;
    public MapLoader mapLoader;
    public final List<GameObject> objects = new ArrayList<>();
    public final List<Enemy> enemies = new ArrayList<>();
    public UI ui;

    public int totalDiamonds;
    public boolean levelComplete;
    public boolean gameOver;
    public int spawnRow = 1;
    public int spawnCol = 1;
    public int cameraX;
    public int cameraY;

    public GamePanel() {
        AssetManager.loadAssets();

        this.setPreferredSize(new Dimension(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.addKeyListener(keyH);

        mapLoader = new MapLoader(this);
        setupSpawnFromBackground();
        setupObjectsFromCsv();
        player = new Player(this, keyH, spawnRow, spawnCol);
        ui = new UI(this);
        updateCamera();
    }

    private void setupSpawnFromBackground() {
        if (mapLoader.getSpawnRow() >= 0 && mapLoader.getSpawnCol() >= 0) {
            spawnRow = mapLoader.getSpawnRow();
            spawnCol = mapLoader.getSpawnCol();
        }
    }

    private void setupObjectsFromCsv() {
        int[][] objectData = MapLoader.loadCsvMap("/maps/map01_objects.csv");

        for (int row = 0; row < objectData.length; row++) {
            for (int col = 0; col < objectData[row].length; col++) {
                int x = col * tileSize;
                int y = row * tileSize;
                switch (objectData[row][col]) {
                    case OBJ_ROCK:
                        objects.add(new Rock(x, y));
                        break;
                    case OBJ_DIAMOND:
                        objects.add(new Diamond(x, y));
                        totalDiamonds++;
                        break;
                    case OBJ_DOOR:
                        objects.add(new Door(x, y));
                        break;
                    case OBJ_SNAKE:
                        enemies.add(new Enemy(this, x, y, row % 2 == 0));
                        break;
                    case OBJ_SPAWN:
                        spawnRow = row;
                        spawnCol = col;
                        break;
                    case OBJ_EMPTY:
                    default:
                        break;
                }
            }
        }
    }

    public void update() {
        if (gameOver || levelComplete) {
            return;
        }

        player.update();
        updateRocks();
        for (Enemy enemy : enemies) {
            enemy.update();
        }
        checkEnemyContact();
        updateCamera();
    }

    public GameObject getObjectAt(int row, int col) {
        for (GameObject object : objects) {
            if (object.isActive() && object.getRow(this) == row && object.getCol(this) == col) {
                return object;
            }
        }
        return null;
    }

    public boolean isBlockedForRock(int row, int col) {
        if (!mapLoader.isGround(row, col)) {
            return true;
        }

        return getObjectAt(row, col) != null;
    }

    public boolean isBlockedForEnemy(int row, int col) {
        if (!mapLoader.isGround(row, col)) {
            return true;
        }

        return getObjectAt(row, col) != null;
    }

    public boolean hasPlayerAt(int row, int col) {
        return player.getRow() == row && player.getCol() == col;
    }

    private void updateRocks() {
        for (GameObject object : objects) {
            if (!(object instanceof Rock) || !object.isActive()) {
                continue;
            }

            int row = object.getRow(this);
            int col = object.getCol(this);
            int belowRow = row + 1;

            if (!isBlockedForRock(belowRow, col) && !hasPlayerAt(belowRow, col)) {
                object.setGridPosition(this, belowRow, col);
            }
        }
    }

    private void checkEnemyContact() {
        for (Enemy enemy : enemies) {
            if (enemy.getRow() == player.getRow() && enemy.getCol() == player.getCol()) {
                player.loseLife();
                if (gameOver) {
                    return;
                }
            }
        }
    }

    public void updateCamera() {
        int targetCameraX = player.x + tileSize / 2 - GameConfig.SCREEN_WIDTH / 2;
        int targetCameraY = player.y + tileSize / 2 - GameConfig.SCREEN_HEIGHT / 2;
        int maxCameraX = Math.max(0, mapLoader.getCols() * tileSize - GameConfig.SCREEN_WIDTH);
        int maxCameraY = Math.max(0, mapLoader.getRows() * tileSize - GameConfig.SCREEN_HEIGHT);

        cameraX = clamp(targetCameraX, 0, maxCameraX);
        cameraY = clamp(targetCameraY, 0, maxCameraY);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        updateCamera();
        g2.translate(-cameraX, -cameraY);
        mapLoader.draw(g2);
        if (mapLoader.getTileAt(spawnRow, spawnCol) != MapLoader.SPAWN) {
            g2.drawImage(AssetManager.spawn, spawnCol * tileSize, spawnRow * tileSize, tileSize, tileSize, null);
        }

        for (GameObject object : objects) {
            if (object != null && object.isActive()) {
                object.draw(g2, this);
            }
        }

        for (Enemy enemy : enemies) {
            enemy.draw(g2);
        }
        player.draw(g2);
        g2.translate(cameraX, cameraY);
        ui.draw(g2);

        g2.dispose();
    }
}
