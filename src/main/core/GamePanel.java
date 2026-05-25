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
import main.ui.GameState;
import main.ui.GameStateManager;
import main.ui.SoundManager;
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

    // =========================================================
    // Hằng số định danh object trong CSV
    // =========================================================
    private static final int OBJ_EMPTY   = 0;
    private static final int OBJ_ROCK    = 1;
    private static final int OBJ_DIAMOND = 2;
    private static final int OBJ_DOOR    = 3;
    private static final int OBJ_SNAKE   = 4;
    private static final int OBJ_SPAWN   = 5;

    // =========================================================
    // Cấu hình tile
    // =========================================================
    public final int tileSize = GameConfig.TILE_SIZE;

    // =========================================================
    // Input
    // =========================================================
    KeyHandler keyH = new KeyHandler();

    // =========================================================
    // Các thành phần game
    // =========================================================
    public Player   player;
    public MapLoader mapLoader;
    public final List<GameObject> objects = new ArrayList<>();
    public final List<Enemy>      enemies = new ArrayList<>();

    // =========================================================
    // UI & State (Phần 5 — thành viên 5)
    // =========================================================
    public UI               ui;
    public GameStateManager gsm;
    public SoundManager     soundManager;

    // =========================================================
    // Trạng thái gameplay
    // =========================================================
    public int     totalDiamonds;
    public boolean levelComplete;
    public boolean gameOver;
    public int     spawnRow = 1;
    public int     spawnCol = 1;

    // =========================================================
    // Camera
    // =========================================================
    public int cameraX;
    public int cameraY;

    // =========================================================
    // Constructor
    // =========================================================
    public GamePanel() {
        AssetManager.loadAssets();

        this.setPreferredSize(new Dimension(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.addKeyListener(keyH);

        // Khởi tạo map trước để biết spawn point
        mapLoader = new MapLoader(this);
        setupSpawnFromBackground();
        setupObjectsFromCsv();

        // Khởi tạo player tại spawn point
        player = new Player(this, keyH, spawnRow, spawnCol);

        // Khởi tạo UI và GameStateManager (Phần 5)
        ui           = new UI(this);
        gsm          = new GameStateManager(this);
        ui.gsm       = gsm;   // UI cần GSM để vẽ overlay các màn hình

        // Khởi tạo âm thanh
        soundManager = new SoundManager();
        soundManager.loadSounds();
        // soundManager.playBGM(); // Bỏ comment khi đã có file bgm.wav

        updateCamera();
    }

    // =========================================================
    // Setup ban đầu
    // =========================================================

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

    // =========================================================
    // Reset game — được MenuScreen gọi khi chọn "Bắt đầu"
    // =========================================================
    public void resetGame() {
        gameOver      = false;
        levelComplete = false;
        totalDiamonds = 0;
        objects.clear();
        enemies.clear();

        // Nạp lại map và spawn
        setupSpawnFromBackground();
        setupObjectsFromCsv();

        // Reset player về vị trí spawn, xóa score và lives
        player.setDefaultValues();

        // Khởi động lại nhạc nền
        soundManager.stopBGM();
        // soundManager.playBGM(); // Bỏ comment khi đã có file bgm.wav

        updateCamera();
    }

    // =========================================================
    // Vòng lặp cập nhật — gọi mỗi frame từ GameLoop
    // =========================================================
    public void update() {
        // Phải gọi GSM TRƯỚC guard để các màn hình Game Over / Win
        // vẫn nhận được input (Enter để về Menu) khi gameplay đã dừng.
        gsm.update(keyH);

        // Khi đang PLAYING nhưng gameplay chưa nhận trạng thái mới:
        // guard này ngăn gameplay chạy tiếp sau khi thua/thắng.
        if (gsm.getState() != GameState.PLAYING) {
            return;
        }

        // Gameplay chỉ chạy khi đang ở trạng thái PLAYING
        player.update();
        updateRocks();
        for (Enemy enemy : enemies) {
            enemy.update();
        }
        checkEnemyContact();
        updateCamera();
    }

    // =========================================================
    // Logic gameplay nội bộ
    // =========================================================

    public GameObject getObjectAt(int row, int col) {
        for (GameObject object : objects) {
            if (object.isActive()
                    && object.getRow(this) == row
                    && object.getCol(this) == col) {
                return object;
            }
        }
        return null;
    }

    public boolean isBlockedForRock(int row, int col) {
        if (!mapLoader.isGround(row, col)) return true;
        return getObjectAt(row, col) != null;
    }

    public boolean isBlockedForEnemy(int row, int col) {
        if (!mapLoader.isGround(row, col)) return true;
        return getObjectAt(row, col) != null;
    }

    public boolean hasPlayerAt(int row, int col) {
        return player.getRow() == row && player.getCol() == col;
    }

    private void updateRocks() {
        for (GameObject object : objects) {
            if (!(object instanceof Rock) || !object.isActive()) continue;

            int row      = object.getRow(this);
            int col      = object.getCol(this);
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
                if (gameOver) return;
            }
        }
    }

    // =========================================================
    // Camera
    // =========================================================
    public void updateCamera() {
        int targetCameraX = player.x + tileSize / 2 - GameConfig.SCREEN_WIDTH / 2;
        int targetCameraY = player.y + tileSize / 2 - GameConfig.SCREEN_HEIGHT / 2;
        int maxCameraX    = Math.max(0, mapLoader.getCols() * tileSize - GameConfig.SCREEN_WIDTH);
        int maxCameraY    = Math.max(0, mapLoader.getRows() * tileSize - GameConfig.SCREEN_HEIGHT);

        cameraX = clamp(targetCameraX, 0, maxCameraX);
        cameraY = clamp(targetCameraY, 0, maxCameraY);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    // =========================================================
    // Vẽ — gọi mỗi frame từ GameLoop (qua repaint)
    // =========================================================
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        GameState state = gsm.getState();

        // --- Vẽ gameplay (map, object, enemy, player) ---
        // Chỉ vẽ khi đang chơi hoặc đang pause (vẫn thấy map phía sau)
        if (state == GameState.PLAYING || state == GameState.PAUSED) {
            updateCamera();

            // Dịch chuyển theo camera để vẽ thế giới
            g2.translate(-cameraX, -cameraY);

            // Vẽ nền map
            mapLoader.draw(g2);

            // Vẽ spawn marker nếu ô spawn đã bị đè bởi tile khác
            if (mapLoader.getTileAt(spawnRow, spawnCol) != MapLoader.SPAWN) {
                g2.drawImage(AssetManager.spawn,
                        spawnCol * tileSize, spawnRow * tileSize,
                        tileSize, tileSize, null);
            }

            // Vẽ tất cả game object (đá, kim cương, cửa...)
            for (GameObject object : objects) {
                if (object != null && object.isActive()) {
                    object.draw(g2, this);
                }
            }

            // Vẽ kẻ địch
            for (Enemy enemy : enemies) {
                enemy.draw(g2);
            }

            // Vẽ nhân vật chính
            player.draw(g2);

            // Dịch ngược camera về tọa độ màn hình
            g2.translate(cameraX, cameraY);
        }

        // --- Vẽ UI ---
        // UI.draw() sẽ:
        //   - Vẽ HUD (kim cương, mạng) nếu đang PLAYING hoặc PAUSED
        //   - Gọi gsm.draw() để vẽ màn hình tương ứng (Menu, Pause overlay, GameOver, Win)
        ui.draw(g2);

        g2.dispose();
    }
}