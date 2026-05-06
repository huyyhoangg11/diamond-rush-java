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

    public GamePanel() {
        AssetManager.loadAssets();

        this.setPreferredSize(new Dimension(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.addKeyListener(keyH);

        mapLoader = new MapLoader(this);
        player = new Player(this, keyH);
        ui = new UI(this);

        setupObjectsFromCsv();
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
        if (mapLoader.getTileAt(row, col) != MapLoader.DIRT) {
            return true;
        }

        return getObjectAt(row, col) != null;
    }

    public boolean isBlockedForEnemy(int row, int col) {
        if (mapLoader.getTileAt(row, col) != MapLoader.DIRT) {
            return true;
        }

        GameObject object = getObjectAt(row, col);
        return object != null && object.collision;
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

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        mapLoader.draw(g2);

        for (GameObject object : objects) {
            if (object != null && object.isActive()) {
                object.draw(g2, this);
            }
        }

        for (Enemy enemy : enemies) {
            enemy.draw(g2);
        }
        player.draw(g2);
        ui.draw(g2);

        g2.dispose();
    }
}
