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
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GamePanel extends JPanel {

    private static final int OBJ_EMPTY = 0;
    private static final int OBJ_ROCK = 1;
    private static final int OBJ_DIAMOND = 2;
    private static final int OBJ_DOOR = 3;
    private static final int OBJ_SNAKE = 4;
    private static final int OBJ_PRIMARY_SPAWN = 5;
    private static final int OBJ_SECONDARY_SPAWN = 6;
    private static final int MAX_AUTO_LEVELS = 20;
    private static final int ROCK_PRESSURE_DAMAGE_FRAMES = 60;

    public final int tileSize = GameConfig.TILE_SIZE;

    KeyHandler keyH = new KeyHandler();

    public Player player;
    public MapLoader mapLoader;
    public final List<GameObject> objects = new ArrayList<>();
    public final List<Enemy> enemies = new ArrayList<>();

    public UI ui;
    public GameStateManager gsm;
    public SoundManager soundManager;

    public int totalDiamonds;
    public boolean levelComplete;
    public boolean gameOver;
    public int spawnRow = 1;
    public int spawnCol = 1;
    public int cameraX;
    public int cameraY;

    private final List<LevelDefinition> levels = new ArrayList<>();
    private final List<Point> spawnPoints = new ArrayList<>();
    private final List<Set<String>> collectedDiamondKeysByLevel = new ArrayList<>();
    private LevelSnapshot currentCheckpointSnapshot;
    private boolean[] completedLevels;
    private boolean resetKeyWasDown;
    private boolean levelCompletionRecorded;
    private int rockPressureFrames;
    private int currentLevelIndex;

    public int selectedLevelIndex;

    public GamePanel() {
        AssetManager.loadAssets();
        initializeLevels();

        this.setPreferredSize(new Dimension(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.addKeyListener(keyH);

        loadLevel(0);

        ui = new UI(this);
        gsm = new GameStateManager(this);
        ui.gsm = gsm;

        soundManager = new SoundManager();
        soundManager.loadSounds();

        updateCamera();
    }

    private void initializeLevels() {
        for (int i = 1; i <= MAX_AUTO_LEVELS; i++) {
            String levelId = String.format("%02d", i);
            String backgroundPath = "/maps/map" + levelId + "_background.csv";
            String objectPath = "/maps/map" + levelId + "_objects.csv";
            if (resourceExists(backgroundPath) && resourceExists(objectPath)) {
                levels.add(new LevelDefinition("Map " + i, backgroundPath, objectPath));
            }
        }
        if (levels.isEmpty()) {
            levels.add(new LevelDefinition("Map 1", "/maps/map01_background.csv", "/maps/map01_objects.csv"));
        }
        completedLevels = new boolean[levels.size()];
        for (int i = 0; i < levels.size(); i++) {
            collectedDiamondKeysByLevel.add(new HashSet<>());
        }
    }

    private boolean resourceExists(String resourcePath) {
        return GamePanel.class.getResource(resourcePath) != null;
    }

    public int getLevelCount() {
        return levels.size();
    }

    public String getLevelName(int index) {
        return levels.get(index).name;
    }

    public boolean isLevelUnlocked(int index) {
        return index == 0 || completedLevels[index - 1];
    }

    public boolean isLevelCompleted(int index) {
        return completedLevels[index];
    }

    public void selectPreviousLevel() {
        selectedLevelIndex = (selectedLevelIndex - 1 + levels.size()) % levels.size();
    }

    public void selectNextLevel() {
        selectedLevelIndex = (selectedLevelIndex + 1) % levels.size();
    }

    public boolean startSelectedLevel() {
        if (!isLevelUnlocked(selectedLevelIndex)) {
            return false;
        }
        loadLevel(selectedLevelIndex);
        gsm.setState(GameState.PLAYING);
        keyH.clearActionKeys();
        keyH.clearMovementKeys();
        return true;
    }

    public void resetGame() {
        loadLevel(selectedLevelIndex);
    }

    public void quitToMenuFromPause() {
        rememberCollectedDiamondsForCurrentLevel();
        selectedLevelIndex = currentLevelIndex;
        gsm.setState(GameState.MENU);
        keyH.clearActionKeys();
        keyH.clearMovementKeys();
    }

    public void resetToCheckpoint() {
        int currentLives = player != null ? player.lives : 3;
        if (currentCheckpointSnapshot != null) {
            restoreSnapshot(currentCheckpointSnapshot, currentLives);
        } else {
            loadLevel(currentLevelIndex);
            player.lives = currentLives;
        }
        rockPressureFrames = 0;
        gsm.setState(GameState.PLAYING);
        keyH.clearActionKeys();
        keyH.clearMovementKeys();
        updateCamera();
    }

    private void loadLevel(int index) {
        currentLevelIndex = index;
        LevelDefinition level = levels.get(index);

        gameOver = false;
        levelComplete = false;
        levelCompletionRecorded = false;
        totalDiamonds = 0;
        rockPressureFrames = 0;
        objects.clear();
        enemies.clear();
        spawnPoints.clear();
        currentCheckpointSnapshot = null;

        mapLoader = new MapLoader(this, level.backgroundPath);
        setupSpawnFromBackground();
        setupObjectsFromCsv(level.objectPath);
        player = new Player(this, keyH, spawnRow, spawnCol);
        applyRememberedCollectedDiamonds(index);
        rememberCheckpointAtCurrentSpawn();

        updateCamera();
    }

    private void setupSpawnFromBackground() {
        int row = mapLoader.getSpawnRow();
        int col = mapLoader.getSpawnCol();
        if (row >= 0 && col >= 0) {
            addSpawnPoint(row, col);
            spawnRow = row;
            spawnCol = col;
        }
    }

    private void setupObjectsFromCsv(String objectPath) {
        int[][] objectData = MapLoader.loadCsvMap(objectPath);

        for (int row = 0; row < objectData.length; row++) {
            for (int col = 0; col < objectData[row].length; col++) {
                int x = col * tileSize;
                int y = row * tileSize;
                switch (objectData[row][col]) {
                    case OBJ_ROCK -> objects.add(new Rock(x, y));
                    case OBJ_DIAMOND -> {
                        objects.add(new Diamond(x, y));
                        totalDiamonds++;
                    }
                    case OBJ_DOOR -> objects.add(new Door(x, y));
                    case OBJ_SNAKE -> enemies.add(new Enemy(this, x, y, row % 2 == 0));
                    case OBJ_PRIMARY_SPAWN -> {
                        addSpawnPoint(row, col);
                        spawnRow = row;
                        spawnCol = col;
                    }
                    case OBJ_SECONDARY_SPAWN -> {
                        addSpawnPoint(row, col);
                    }
                    case OBJ_EMPTY -> {
                    }
                    default -> {
                    }
                }
            }
        }
    }

    private void addSpawnPoint(int row, int col) {
        for (Point point : spawnPoints) {
            if (point.x == col && point.y == row) {
                return;
            }
        }
        spawnPoints.add(new Point(col, row));
    }

    private void rememberCollectedDiamondsForCurrentLevel() {
        Set<String> collectedDiamondKeys = collectedDiamondKeysByLevel.get(currentLevelIndex);
        for (GameObject object : objects) {
            if (object instanceof Diamond && !object.isActive()) {
                collectedDiamondKeys.add(objectKey(object.getRow(this), object.getCol(this)));
            }
        }
    }

    private void applyRememberedCollectedDiamonds(int levelIndex) {
        Set<String> collectedDiamondKeys = collectedDiamondKeysByLevel.get(levelIndex);
        if (collectedDiamondKeys.isEmpty()) {
            return;
        }

        int collected = 0;
        for (GameObject object : objects) {
            if (object instanceof Diamond
                    && collectedDiamondKeys.contains(objectKey(object.getRow(this), object.getCol(this)))) {
                object.setActive(false);
                collected++;
            }
        }
        player.score = collected;
    }

    private String objectKey(int row, int col) {
        return row + ":" + col;
    }

    public void update() {
        gsm.update(keyH);
        handleResetShortcut();

        if (gsm.getState() != GameState.PLAYING || gameOver || levelComplete) {
            return;
        }

        player.update();
        rememberCheckpointAtCurrentSpawn();

        if (levelComplete) {
            completeCurrentLevel();
            updateCamera();
            return;
        }

        updateRocks();
        checkRockPressure();

        for (Enemy enemy : enemies) {
            enemy.update();
        }
        checkEnemyContact();

        if (levelComplete) {
            completeCurrentLevel();
        }

        updateCamera();
    }

    private void handleResetShortcut() {
        boolean resetKeyDown = keyH.rPressed;
        if (resetKeyDown && !resetKeyWasDown
                && (gsm.getState() == GameState.PLAYING
                || gsm.getState() == GameState.PAUSED
                || gsm.getState() == GameState.GAME_OVER)) {
            resetToCheckpoint();
        }
        resetKeyWasDown = resetKeyDown;
    }

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
        return !mapLoader.isGround(row, col) || getObjectAt(row, col) != null;
    }

    public boolean isBlockedForEnemy(int row, int col) {
        return !mapLoader.isGround(row, col) || getObjectAt(row, col) != null;
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

    private void checkRockPressure() {
        int stackedRocksAbovePlayer = 0;
        int row = player.getRow() - 1;
        int col = player.getCol();

        while (getObjectAt(row, col) instanceof Rock) {
            stackedRocksAbovePlayer++;
            row--;
        }

        if (stackedRocksAbovePlayer >= 2) {
            rockPressureFrames++;
            if (rockPressureFrames >= ROCK_PRESSURE_DAMAGE_FRAMES) {
                player.loseLife();
                rockPressureFrames = 0;
            }
        } else {
            rockPressureFrames = 0;
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

    private void rememberCheckpointAtCurrentSpawn() {
        if (!isSpawnPoint(player.getRow(), player.getCol())) {
            return;
        }

        LevelSnapshot snapshot = captureSnapshot();
        currentCheckpointSnapshot = snapshot;
    }

    private boolean isSpawnPoint(int row, int col) {
        for (Point point : spawnPoints) {
            if (point.x == col && point.y == row) {
                return true;
            }
        }
        return false;
    }

    private void completeCurrentLevel() {
        if (levelCompletionRecorded) {
            return;
        }
        levelCompletionRecorded = true;
        completedLevels[currentLevelIndex] = true;
    }

    private LevelSnapshot captureSnapshot() {
        List<ObjectSnapshot> objectSnapshots = new ArrayList<>();
        for (GameObject object : objects) {
            objectSnapshots.add(new ObjectSnapshot(
                    objectType(object),
                    object.getRow(this),
                    object.getCol(this),
                    object.isActive()));
        }

        List<EnemySnapshot> enemySnapshots = new ArrayList<>();
        for (Enemy enemy : enemies) {
            enemySnapshots.add(new EnemySnapshot(
                    enemy.x,
                    enemy.y,
                    enemy.isHorizontalMovement(),
                    enemy.getMoveDirection(),
                    enemy.getMoveCounter()));
        }

        return new LevelSnapshot(
                mapLoader.copyBackgroundData(),
                objectSnapshots,
                enemySnapshots,
                player.getRow(),
                player.getCol(),
                player.score,
                player.lives,
                totalDiamonds);
    }

    private String objectType(GameObject object) {
        if (object instanceof Rock) {
            return "rock";
        }
        if (object instanceof Diamond) {
            return "diamond";
        }
        if (object instanceof Door) {
            return "door";
        }
        throw new IllegalArgumentException("Unknown object type: " + object.getClass().getName());
    }

    private void restoreSnapshot(LevelSnapshot snapshot) {
        restoreSnapshot(snapshot, snapshot.playerLives);
    }

    private void restoreSnapshot(LevelSnapshot snapshot, int playerLives) {
        mapLoader.restoreBackgroundData(snapshot.backgroundData);
        objects.clear();
        for (ObjectSnapshot objectSnapshot : snapshot.objects) {
            GameObject object = createObject(objectSnapshot);
            if (object != null) {
                object.setActive(objectSnapshot.active);
                objects.add(object);
            }
        }

        enemies.clear();
        for (EnemySnapshot enemySnapshot : snapshot.enemies) {
            Enemy enemy = new Enemy(this, enemySnapshot.x, enemySnapshot.y, enemySnapshot.horizontalMovement);
            enemy.restoreState(enemySnapshot.x, enemySnapshot.y, enemySnapshot.moveDirection, enemySnapshot.moveCounter);
            enemies.add(enemy);
        }

        player.restoreState(snapshot.playerRow, snapshot.playerCol, snapshot.playerScore, playerLives);
        totalDiamonds = snapshot.totalDiamonds;
        gameOver = false;
        levelComplete = false;
        levelCompletionRecorded = false;
        rockPressureFrames = 0;
    }

    private GameObject createObject(ObjectSnapshot objectSnapshot) {
        int x = objectSnapshot.col * tileSize;
        int y = objectSnapshot.row * tileSize;
        return switch (objectSnapshot.type) {
            case "rock" -> new Rock(x, y);
            case "diamond" -> new Diamond(x, y);
            case "door" -> new Door(x, y);
            default -> null;
        };
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

        GameState state = gsm.getState();
        if (state == GameState.PLAYING || state == GameState.PAUSED) {
            updateCamera();
            g2.translate(-cameraX, -cameraY);

            mapLoader.draw(g2);
            drawSpawnMarkers(g2);

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
        }

        ui.draw(g2);
        g2.dispose();
    }

    private void drawSpawnMarkers(Graphics2D g2) {
        for (Point point : spawnPoints) {
            int row = point.y;
            int col = point.x;
            if (mapLoader.getTileAt(row, col) != MapLoader.SPAWN) {
                g2.drawImage(AssetManager.spawn, col * tileSize, row * tileSize, tileSize, tileSize, null);
            }
        }
    }

    private static final class LevelDefinition {
        private final String name;
        private final String backgroundPath;
        private final String objectPath;

        private LevelDefinition(String name, String backgroundPath, String objectPath) {
            this.name = name;
            this.backgroundPath = backgroundPath;
            this.objectPath = objectPath;
        }
    }

    private static final class LevelSnapshot {
        private final int[][] backgroundData;
        private final List<ObjectSnapshot> objects;
        private final List<EnemySnapshot> enemies;
        private final int playerRow;
        private final int playerCol;
        private final int playerScore;
        private final int playerLives;
        private final int totalDiamonds;

        private LevelSnapshot(int[][] backgroundData,
                              List<ObjectSnapshot> objects,
                              List<EnemySnapshot> enemies,
                              int playerRow,
                              int playerCol,
                              int playerScore,
                              int playerLives,
                              int totalDiamonds) {
            this.backgroundData = backgroundData;
            this.objects = objects;
            this.enemies = enemies;
            this.playerRow = playerRow;
            this.playerCol = playerCol;
            this.playerScore = playerScore;
            this.playerLives = playerLives;
            this.totalDiamonds = totalDiamonds;
        }
    }

    private static final class ObjectSnapshot {
        private final String type;
        private final int row;
        private final int col;
        private final boolean active;

        private ObjectSnapshot(String type, int row, int col, boolean active) {
            this.type = type;
            this.row = row;
            this.col = col;
            this.active = active;
        }
    }

    private static final class EnemySnapshot {
        private final int x;
        private final int y;
        private final boolean horizontalMovement;
        private final int moveDirection;
        private final int moveCounter;

        private EnemySnapshot(int x, int y, boolean horizontalMovement, int moveDirection, int moveCounter) {
            this.x = x;
            this.y = y;
            this.horizontalMovement = horizontalMovement;
            this.moveDirection = moveDirection;
            this.moveCounter = moveCounter;
        }
    }
}
