package main.core;

import main.config.GameConfig;
import main.entity.Enemy;
import main.entity.Player;
import main.input.KeyHandler;
import main.map.MapLoader;
import main.object.Diamond;
import main.object.DiamondPre;
import main.object.Door;
import main.object.FinalDiamondPre;
import main.object.GameObject;
import main.object.Hammer;
import main.object.Key;
import main.object.Knob;
import main.object.LockBolt;
import main.object.PhatTile;
import main.object.Rock;
import main.object.SpecialLock;
import main.object.Statue;
import main.object.WallSnake;
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
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

public class GamePanel extends JPanel {

    private static final int OBJ_EMPTY = 0;
    private static final int OBJ_ROCK = 1;
    private static final int OBJ_DIAMOND = 2;
    private static final int OBJ_DOOR = 3;
    private static final int OBJ_SNAKE = 4;
    private static final int OBJ_PRIMARY_SPAWN = 5;
    private static final int OBJ_SECONDARY_SPAWN = 6;
    private static final int OBJ_HAMMER = 7;
    private static final int OBJ_STATUE = 8;
    private static final int OBJ_LOCK_BOLT = 9;
    private static final int OBJ_KNOB = 10;
    private static final int OBJ_SPECIAL_LOCK = 11;
    private static final int OBJ_KEY = 12;
    private static final int OBJ_FINAL_DIAMOND_PRE = 13;
    private static final int OBJ_DIAMOND_PRE = 14;
    private static final int OBJ_PHAT_0 = 40;
    private static final int OBJ_PHAT_1 = 41;
    private static final int OBJ_PHAT_2 = 42;
    private static final int OBJ_PHAT_3 = 43;
    private static final int OBJ_SNAKE_BOSS_SPAWN = 45;
    private static final int OBJ_WALL_SNAKE = 50;
    private static final int MAX_AUTO_LEVELS = 20;
    private static final int ROCK_PRESSURE_DAMAGE_FRAMES = 60;
    private static final int ROCK_MOVE_INTERVAL_FRAMES = 10;
    private static final int FINAL_DEATH_ZOOM_FRAMES = 48;
    private static final double FINAL_DEATH_ZOOM_SCALE = 2.55;
    private static final Path SAVE_PATH = Path.of(System.getProperty("user.home"), ".diamondrush_save.properties");

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
    private boolean hasHammer;
    private boolean hasKey;

    private final List<LevelDefinition> levels = new ArrayList<>();
    private final List<Point> spawnPoints = new ArrayList<>();
    private final List<Set<String>> collectedDiamondKeysByLevel = new ArrayList<>();
    private final Random random = new Random();
    private LevelSnapshot currentCheckpointSnapshot;
    private boolean[] completedLevels;
    private boolean resetKeyWasDown;
    private boolean levelCompletionRecorded;
    private int rockPressureFrames;
    private int rockMoveCounter;
    private int currentLevelIndex;
    private int snakeVariantOffset;
    private int snakeCountInLevel;
    private boolean finalDeathZoomActive;
    private int finalDeathZoomFrame;
    private SnakeBossController snakeBossController;

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
        soundManager.playMenuBGM();

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

    public int getCurrentLevelIndex() {
        return currentLevelIndex;
    }

    public boolean isFinalLevel() {
        return currentLevelIndex == levels.size() - 1;
    }

    public int getJourneyDiamondCount() {
        int collected = player != null ? player.score : 0;
        for (int i = 0; i < collectedDiamondKeysByLevel.size(); i++) {
            if (i != currentLevelIndex) {
                collected += collectedDiamondKeysByLevel.get(i).size();
            }
        }
        return collected;
    }

    public boolean isLevelUnlocked(int index) {
        if (index < 0 || index >= levels.size()) {
            return false;
        }
        return index == 0 || completedLevels[index] || completedLevels[index - 1];
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

    public void clearInputKeys() {
        keyH.clearActionKeys();
        keyH.clearMovementKeys();
    }

    public boolean startSelectedLevel() {
        if (!isLevelUnlocked(selectedLevelIndex)) {
            return false;
        }
        rememberCollectedDiamondsForCurrentLevel();
        loadLevel(selectedLevelIndex);
        gsm.setState(GameState.PLAYING);
        saveGame();
        clearInputKeys();
        return true;
    }

    public void startNewGame() {
        resetProgress();
        selectedLevelIndex = 0;
        hasHammer = false;
        hasKey = false;
        loadLevel(0);
        saveGame();
        gsm.setState(GameState.PLAYING);
        clearInputKeys();
    }

    public boolean hasSaveFile() {
        return Files.isRegularFile(SAVE_PATH);
    }

    public boolean loadSavedGame() {
        if (!hasSaveFile()) {
            return false;
        }

        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(SAVE_PATH)) {
            properties.load(in);
        } catch (IOException e) {
            return false;
        }

        resetProgress();
        hasHammer = Boolean.parseBoolean(properties.getProperty("hasHammer", "false"));
        hasKey = Boolean.parseBoolean(properties.getProperty("hasKey", "false"));
        selectedLevelIndex = clamp(parseInt(properties.getProperty("selectedLevelIndex"), 0), 0, levels.size() - 1);
        currentLevelIndex = clamp(parseInt(properties.getProperty("currentLevelIndex"), selectedLevelIndex), 0, levels.size() - 1);
        loadCompletedLevels(properties.getProperty("completed", ""));
        loadCollectedDiamonds(properties);
        clearInputKeys();
        return true;
    }

    public void saveGame() {
        rememberCollectedDiamondsForCurrentLevel();

        Properties properties = new Properties();
        properties.setProperty("currentLevelIndex", Integer.toString(currentLevelIndex));
        properties.setProperty("selectedLevelIndex", Integer.toString(selectedLevelIndex));
        properties.setProperty("hasHammer", Boolean.toString(hasHammer));
        properties.setProperty("hasKey", Boolean.toString(hasKey));
        properties.setProperty("completed", encodeCompletedLevels());
        for (int i = 0; i < collectedDiamondKeysByLevel.size(); i++) {
            properties.setProperty("diamonds." + i, String.join(";", collectedDiamondKeysByLevel.get(i)));
        }

        try (OutputStream out = Files.newOutputStream(SAVE_PATH)) {
            properties.store(out, "Diamond Rush save data");
        } catch (IOException e) {
            System.err.println("Could not save game: " + e.getMessage());
        }
    }

    public void resetGame() {
        loadLevel(selectedLevelIndex);
    }

    public void quitToMenuFromPause() {
        rememberCollectedDiamondsForCurrentLevel();
        selectedLevelIndex = currentLevelIndex;
        saveGame();
        gsm.setState(GameState.MENU);
        clearInputKeys();
    }

    private void resetProgress() {
        for (int i = 0; i < completedLevels.length; i++) {
            completedLevels[i] = false;
        }
        for (Set<String> collectedDiamondKeys : collectedDiamondKeysByLevel) {
            collectedDiamondKeys.clear();
        }
    }

    private String encodeCompletedLevels() {
        StringBuilder builder = new StringBuilder(completedLevels.length);
        for (boolean completedLevel : completedLevels) {
            builder.append(completedLevel ? '1' : '0');
        }
        return builder.toString();
    }

    private void loadCompletedLevels(String encoded) {
        for (int i = 0; i < completedLevels.length && i < encoded.length(); i++) {
            completedLevels[i] = encoded.charAt(i) == '1';
        }
    }

    private void loadCollectedDiamonds(Properties properties) {
        for (int i = 0; i < collectedDiamondKeysByLevel.size(); i++) {
            String encoded = properties.getProperty("diamonds." + i, "");
            if (encoded.isBlank()) {
                continue;
            }
            String[] keys = encoded.split(";");
            for (String key : keys) {
                if (!key.isBlank()) {
                    collectedDiamondKeysByLevel.get(i).add(key);
                }
            }
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public void resetToCheckpoint() {
        int currentLives = player != null ? player.lives : 3;
        resetFinalDeathZoom();
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

    public void startFinalDeathZoom() {
        finalDeathZoomActive = true;
        finalDeathZoomFrame = 0;
    }

    public boolean advanceFinalDeathZoom() {
        if (!finalDeathZoomActive) {
            return true;
        }
        if (finalDeathZoomFrame < FINAL_DEATH_ZOOM_FRAMES) {
            finalDeathZoomFrame++;
            return false;
        }
        return true;
    }

    private void resetFinalDeathZoom() {
        finalDeathZoomActive = false;
        finalDeathZoomFrame = 0;
    }

    private double getFinalDeathZoomScale() {
        if (!finalDeathZoomActive) {
            return 1.0;
        }
        double progress = Math.min(1.0, finalDeathZoomFrame / (double) FINAL_DEATH_ZOOM_FRAMES);
        double eased = 1.0 - Math.pow(1.0 - progress, 3);
        return 1.0 + (FINAL_DEATH_ZOOM_SCALE - 1.0) * eased;
    }

    private void loadLevel(int index) {
        currentLevelIndex = index;
        LevelDefinition level = levels.get(index);

        gameOver = false;
        levelComplete = false;
        levelCompletionRecorded = false;
        totalDiamonds = 0;
        rockPressureFrames = 0;
        rockMoveCounter = 0;
        snakeVariantOffset = random.nextInt(2);
        snakeCountInLevel = 0;
        resetFinalDeathZoom();
        objects.clear();
        enemies.clear();
        spawnPoints.clear();
        snakeBossController = null;
        currentCheckpointSnapshot = null;

        mapLoader = new MapLoader(this, level.backgroundPath);
        setupSpawnFromBackground();
        setupObjectsFromCsv(level.objectPath);
        updateHammerPickupsForOwnership();
        updateKeyPickupsForOwnership();
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
        List<Point> snakeBossSpawnMarkers = new ArrayList<>();

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
                    case OBJ_DIAMOND_PRE -> {
                        objects.add(new DiamondPre(x, y));
                        totalDiamonds++;
                    }
                    case OBJ_FINAL_DIAMOND_PRE -> {
                        objects.add(new FinalDiamondPre(x, y));
                        totalDiamonds++;
                    }
                    case OBJ_DOOR -> objects.add(new Door(x, y));
                    case OBJ_SNAKE -> enemies.add(new Enemy(this, x, y, (row + col) % 2 == 0, nextSnakeVariant()));
                    case OBJ_HAMMER -> objects.add(new Hammer(x, y));
                    case OBJ_STATUE -> objects.add(new Statue(x, y));
                    case OBJ_LOCK_BOLT -> objects.add(new LockBolt(x, y));
                    case OBJ_KNOB -> objects.add(new Knob(x, y));
                    case OBJ_SPECIAL_LOCK -> objects.add(new SpecialLock(x, y));
                    case OBJ_KEY -> objects.add(new Key(x, y));
                    case OBJ_PHAT_0 -> objects.add(new PhatTile(x, y, 0));
                    case OBJ_PHAT_1 -> objects.add(new PhatTile(x, y, 1));
                    case OBJ_PHAT_2 -> objects.add(new PhatTile(x, y, 2));
                    case OBJ_PHAT_3 -> objects.add(new PhatTile(x, y, 3));
                    case OBJ_SNAKE_BOSS_SPAWN -> snakeBossSpawnMarkers.add(new Point(col, row));
                    case OBJ_WALL_SNAKE -> objects.add(new WallSnake(x, y));
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

        if (!snakeBossSpawnMarkers.isEmpty()) {
            snakeBossController = new SnakeBossController(this, snakeBossSpawnMarkers);
        }
    }

    private int nextSnakeVariant() {
        return (snakeVariantOffset + snakeCountInLevel++) % 2;
    }

    public boolean hasHammer() {
        return hasHammer;
    }

    public void acquireHammer() {
        hasHammer = true;
        updateHammerPickupsForOwnership();
    }

    public boolean hasKey() {
        return hasKey;
    }

    public void acquireKey() {
        hasKey = true;
        updateKeyPickupsForOwnership();
    }

    public void playSfx(String name) {
        if (soundManager != null) {
            soundManager.playSFX(name);
        }
    }

    public void updateMusicForState(GameState state) {
        if (soundManager == null) {
            return;
        }

        switch (state) {
            case MENU, WORLD_MAP, HOW_TO_PLAY, STORY -> soundManager.playMenuBGM();
            case PLAYING, PAUSED -> soundManager.playStageBGM();
            case GAME_OVER, WIN -> soundManager.stopBGM();
        }
    }

    public void stopBgm() {
        if (soundManager != null) {
            soundManager.stopBGM();
        }
    }

    private void updateHammerPickupsForOwnership() {
        if (!hasHammer) {
            return;
        }

        for (GameObject object : objects) {
            if (object instanceof Hammer) {
                object.setActive(false);
            }
        }
    }

    private void updateKeyPickupsForOwnership() {
        if (!hasKey) {
            return;
        }

        for (GameObject object : objects) {
            if (object instanceof Key) {
                object.setActive(false);
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
        if (player.isDeathSequenceActive()) {
            updateCamera();
            return;
        }
        updateLocks();
        updateSnakeBoss();
        updateStatues();
        checkFireContact();
        rememberCheckpointAtCurrentSpawn();

        if (levelComplete) {
            completeCurrentLevel();
            updateCamera();
            return;
        }

        updateFallingObjects();
        checkRockPressure();

        for (Enemy enemy : enemies) {
            enemy.update();
        }
        enemies.removeIf(enemy -> !enemy.isActive());
        checkEnemyContact();

        if (levelComplete) {
            completeCurrentLevel();
        }

        updateCamera();
    }

    private void updateSnakeBoss() {
        if (snakeBossController == null) {
            return;
        }
        snakeBossController.update();
        if (snakeBossController.isDangerAt(player.getRow(), player.getCol())) {
            player.loseLife();
        }
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
                    && object.collision
                    && object.getRow(this) == row
                    && object.getCol(this) == col) {
                return object;
            }
        }
        for (GameObject object : objects) {
            if (object.isActive()
                    && object.getRow(this) == row
                    && object.getCol(this) == col) {
                return object;
            }
        }
        return null;
    }

    public boolean hasKnobAt(int row, int col) {
        for (GameObject object : objects) {
            if (object instanceof Knob
                    && object.isActive()
                    && object.getRow(this) == row
                    && object.getCol(this) == col) {
                return true;
            }
        }
        return false;
    }

    public boolean isBlockedForRock(int row, int col) {
        return !mapLoader.isGround(row, col) || isObjectBlockingMovable(row, col);
    }

    public boolean isObjectBlockingMovable(int row, int col) {
        for (GameObject object : objects) {
            if (!object.isActive()
                    || object.getRow(this) != row
                    || object.getCol(this) != col) {
                continue;
            }
            if (object instanceof Knob) {
                continue;
            }
            if (object instanceof LockBolt && !object.collision) {
                continue;
            }
            return true;
        }
        return false;
    }

    private boolean canRockOccupy(int row, int col) {
        return !isBlockedForRock(row, col) && !hasPlayerAt(row, col);
    }

    public boolean isBlockedForEnemy(int row, int col) {
        return !mapLoader.isGround(row, col)
                || mapLoader.getTileAt(row, col) == MapLoader.SPAWN
                || getObjectAt(row, col) != null;
    }

    public boolean hasPlayerAt(int row, int col) {
        return player.getRow() == row && player.getCol() == col;
    }

    public Enemy getEnemyAt(int row, int col) {
        for (Enemy enemy : enemies) {
            if (enemy.isActive()
                    && !enemy.isDying()
                    && enemy.getRow() == row
                    && enemy.getCol() == col) {
                return enemy;
            }
        }
        return null;
    }

    public boolean hitBossWithHammerAt(int row, int col) {
        return snakeBossController != null && snakeBossController.hitWithHammer(row, col);
    }

    public boolean hitBossWithRockAt(int row, int col, GameObject rock) {
        return snakeBossController != null && snakeBossController.hitWithRock(row, col, rock);
    }

    public void trackBossRockMove(GameObject rock, int row, int col) {
        if (snakeBossController != null) {
            snakeBossController.trackPushedRock(rock, row, col);
        }
    }

    private void updateFallingObjects() {
        rockMoveCounter++;
        if (rockMoveCounter < ROCK_MOVE_INTERVAL_FRAMES) {
            return;
        }
        rockMoveCounter = 0;

        for (GameObject object : objects) {
            if (!(object instanceof Rock || object instanceof Diamond) || !object.isActive()) {
                continue;
            }

            int row = object.getRow(this);
            int col = object.getCol(this);
            int belowRow = row + 1;

            if (canRockOccupy(belowRow, col)) {
                moveRockTo(object, belowRow, col);
            } else {
                int slideDirection = getRockSlideDirection(row, col);
                if (slideDirection != 0) {
                    moveRockTo(object, belowRow, col + slideDirection);
                }
            }
        }
    }

    private void updateStatues() {
        for (GameObject object : objects) {
            if (object instanceof Statue statue && statue.isActive()) {
                statue.update();
            }
        }
    }

    private void checkFireContact() {
        int playerRow = player.getRow();
        int playerCol = player.getCol();
        for (GameObject object : objects) {
            if (object instanceof Statue statue && statue.hasFireAt(this, playerRow, playerCol)) {
                player.loseLife();
                return;
            }
        }
    }

    private void updateLocks() {
        for (GameObject object : objects) {
            if (object instanceof SpecialLock specialLock && specialLock.isActive()) {
                boolean shouldOpen = hasKey && isPlayerAdjacentTo(object.getRow(this), object.getCol(this));
                specialLock.updateWithKey(shouldOpen);
            }
        }

        for (GameObject object : objects) {
            if (object instanceof LockBolt lockBolt
                    && !(object instanceof SpecialLock)
                    && lockBolt.isActive()) {
                if (snakeBossController != null && snakeBossController.controlsLock(lockBolt)) {
                    continue;
                }
                Knob nearestKnob = findNearestKnob(lockBolt);
                boolean shouldRetract = nearestKnob != null && isKnobPressed(nearestKnob);
                lockBolt.update(shouldRetract);
            }
        }
    }

    private boolean isPlayerAdjacentTo(int row, int col) {
        return Math.abs(player.getRow() - row) + Math.abs(player.getCol() - col) == 1;
    }

    private Knob findNearestKnob(GameObject lock) {
        Knob nearestKnob = null;
        int nearestDistance = Integer.MAX_VALUE;
        int lockRow = lock.getRow(this);
        int lockCol = lock.getCol(this);

        for (GameObject object : objects) {
            if (!(object instanceof Knob knob) || !knob.isActive()) {
                continue;
            }

            int distance = Math.abs(knob.getRow(this) - lockRow) + Math.abs(knob.getCol(this) - lockCol);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestKnob = knob;
            }
        }
        return nearestKnob;
    }

    private boolean isKnobPressed(Knob knob) {
        int knobRow = knob.getRow(this);
        int knobCol = knob.getCol(this);
        if (hasPlayerAt(knobRow, knobCol)) {
            return true;
        }

        for (GameObject object : objects) {
            if (object instanceof Rock
                    && object.isActive()
                    && object.getRow(this) == knobRow
                    && object.getCol(this) == knobCol) {
                return true;
            }
        }
        return false;
    }

    private void moveRockTo(GameObject rock, int row, int col) {
        if (snakeBossController != null && snakeBossController.hitWithRock(row, col, rock)) {
            return;
        }
        if (snakeBossController != null) {
            snakeBossController.trackPushedRock(rock, row, col);
        }
        Enemy enemy = getEnemyAt(row, col);
        if (enemy != null) {
            enemy.crushByRock();
        }
        rock.setGridPosition(this, row, col);
    }

    private int getRockSlideDirection(int row, int col) {
        GameObject support = getFallingBodyAt(row + 1, col);
        if (!(support instanceof Rock) && !(support instanceof Diamond)) {
            return 0;
        }

        boolean canSlideLeft = canRockSlideTo(row, col, -1);
        boolean canSlideRight = canRockSlideTo(row, col, 1);

        if (canSlideLeft && canSlideRight) {
            return (row + col) % 2 == 0 ? -1 : 1;
        }
        if (canSlideLeft) {
            return -1;
        }
        if (canSlideRight) {
            return 1;
        }
        return 0;
    }

    private boolean canRockSlideTo(int row, int col, int direction) {
        return canRockOccupy(row, col + direction)
                && canRockOccupy(row + 1, col + direction);
    }

    private GameObject getFallingBodyAt(int row, int col) {
        for (GameObject object : objects) {
            if ((object instanceof Rock || object instanceof Diamond)
                    && object.isActive()
                    && object.getRow(this) == row
                    && object.getCol(this) == col) {
                return object;
            }
        }
        return null;
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
            if (enemy.isActive()
                    && !enemy.isDying()
                    && enemy.getRow() == player.getRow()
                    && enemy.getCol() == player.getCol()) {
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
        selectedLevelIndex = currentLevelIndex;
        playSfx(SoundManager.SFX_LEVEL_CLEAR);
        saveGame();
    }

    private LevelSnapshot captureSnapshot() {
        List<ObjectSnapshot> objectSnapshots = new ArrayList<>();
        for (GameObject object : objects) {
            objectSnapshots.add(createObjectSnapshot(object));
        }

        List<EnemySnapshot> enemySnapshots = new ArrayList<>();
        for (Enemy enemy : enemies) {
            enemySnapshots.add(new EnemySnapshot(
                    enemy.x,
                    enemy.y,
                    enemy.isHorizontalMovement(),
                    enemy.getSnakeVariant(),
                    enemy.getMoveDirection(),
                    enemy.getMoveCounter(),
                    enemy.getStunFrames(),
                    enemy.getDeathFrames()));
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

    private ObjectSnapshot createObjectSnapshot(GameObject object) {
        int state1 = 0;
        int state2 = 0;
        if (object instanceof Statue statue) {
            state1 = statue.getFireLength();
            state2 = statue.getFireTimer();
        } else if (object instanceof LockBolt lockBolt) {
            state1 = lockBolt.getFrame();
            state2 = lockBolt.getAnimationCounter();
        }

        return new ObjectSnapshot(
                objectType(object),
                object.getRow(this),
                object.getCol(this),
                object.isActive(),
                state1,
                state2);
    }

    private String objectType(GameObject object) {
        if (object instanceof Rock) {
            return "rock";
        }
        if (object instanceof FinalDiamondPre) {
            return "final_diamond_pre";
        }
        if (object instanceof DiamondPre) {
            return "diamond_pre";
        }
        if (object instanceof Diamond) {
            return "diamond";
        }
        if (object instanceof Door) {
            return "door";
        }
        if (object instanceof Hammer) {
            return "hammer";
        }
        if (object instanceof Key) {
            return "key";
        }
        if (object instanceof Knob) {
            return "knob";
        }
        if (object instanceof SpecialLock) {
            return "special_lock";
        }
        if (object instanceof LockBolt) {
            return "lock_bolt";
        }
        if (object instanceof Statue) {
            return "statue";
        }
        if (object instanceof PhatTile phatTile) {
            return "phat_" + phatTile.getVariant();
        }
        if (object instanceof WallSnake) {
            return "wall_snake";
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
                if (hasHammer && object instanceof Hammer) {
                    object.setActive(false);
                }
                if (hasKey && object instanceof Key) {
                    object.setActive(false);
                }
                objects.add(object);
            }
        }

        enemies.clear();
        for (EnemySnapshot enemySnapshot : snapshot.enemies) {
            Enemy enemy = new Enemy(this, enemySnapshot.x, enemySnapshot.y,
                    enemySnapshot.horizontalMovement, enemySnapshot.snakeVariant);
            enemy.restoreState(enemySnapshot.x, enemySnapshot.y, enemySnapshot.moveDirection,
                    enemySnapshot.moveCounter, enemySnapshot.stunFrames, enemySnapshot.deathFrames);
            enemies.add(enemy);
        }

        if (snakeBossController != null) {
            snakeBossController.resetAfterRestore();
        }

        player.restoreState(snapshot.playerRow, snapshot.playerCol, snapshot.playerScore, playerLives);
        totalDiamonds = snapshot.totalDiamonds;
        gameOver = false;
        levelComplete = false;
        levelCompletionRecorded = false;
        rockPressureFrames = 0;
        rockMoveCounter = 0;
    }

    private GameObject createObject(ObjectSnapshot objectSnapshot) {
        int x = objectSnapshot.col * tileSize;
        int y = objectSnapshot.row * tileSize;
        return switch (objectSnapshot.type) {
            case "rock" -> new Rock(x, y);
            case "final_diamond_pre" -> new FinalDiamondPre(x, y);
            case "diamond" -> new Diamond(x, y);
            case "diamond_pre" -> new DiamondPre(x, y);
            case "door" -> new Door(x, y);
            case "hammer" -> new Hammer(x, y);
            case "key" -> new Key(x, y);
            case "knob" -> new Knob(x, y);
            case "lock_bolt" -> {
                LockBolt lockBolt = new LockBolt(x, y);
                lockBolt.restoreState(objectSnapshot.state1, objectSnapshot.state2);
                yield lockBolt;
            }
            case "special_lock" -> {
                SpecialLock specialLock = new SpecialLock(x, y);
                specialLock.restoreState(objectSnapshot.state1, objectSnapshot.state2);
                yield specialLock;
            }
            case "statue" -> {
                Statue statue = new Statue(x, y);
                statue.restoreState(objectSnapshot.state1, objectSnapshot.state2);
                yield statue;
            }
            case "phat_0" -> new PhatTile(x, y, 0);
            case "phat_1" -> new PhatTile(x, y, 1);
            case "phat_2" -> new PhatTile(x, y, 2);
            case "phat_3" -> new PhatTile(x, y, 3);
            case "wall_snake" -> new WallSnake(x, y);
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
            AffineTransform gameplayTransform = g2.getTransform();
            applyGameplayTransform(g2);

            mapLoader.draw(g2);
            drawSpawnMarkers(g2);

            for (GameObject object : objects) {
                if (object instanceof Knob && object.isActive()) {
                    object.draw(g2, this);
                }
            }

            for (GameObject object : objects) {
                if (object != null
                        && !(object instanceof Knob)
                        && !(object instanceof WallSnake)
                        && object.isActive()) {
                    object.draw(g2, this);
                }
            }

            if (snakeBossController != null) {
                snakeBossController.draw(g2);
            }

            for (GameObject object : objects) {
                if (object instanceof WallSnake && object.isActive()) {
                    object.draw(g2, this);
                }
            }

            for (Enemy enemy : enemies) {
                enemy.draw(g2);
            }

            player.draw(g2);
            g2.setTransform(gameplayTransform);
        }

        ui.draw(g2);
        if ((state == GameState.PLAYING || state == GameState.PAUSED) && snakeBossController != null) {
            snakeBossController.drawHealthBar(g2);
        }
        g2.dispose();
    }

    private void applyGameplayTransform(Graphics2D g2) {
        double zoom = getFinalDeathZoomScale();
        if (zoom > 1.001 && player != null) {
            int focusScreenX = player.x + tileSize / 2 - cameraX;
            int focusScreenY = player.y + tileSize / 2 - cameraY;
            g2.translate(focusScreenX, focusScreenY);
            g2.scale(zoom, zoom);
            g2.translate(-focusScreenX, -focusScreenY);
        }
        g2.translate(-cameraX, -cameraY);
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

    private static final class SnakeBossController {
        private static final int MAX_HEALTH = 10;
        private static final int EMERGE_FRAMES = 36;
        private static final int WAIT_HEAD_FRAMES = 60;
        private static final int CHARGE_FRAMES = 18;
        private static final int STUN_FIRE_FRAMES = 120;
        private static final int RETREAT_FRAMES = 50;
        private static final int SUBMERGE_FRAMES = 36;
        private static final int HURT_FLASH_FRAMES = 30;
        private static final int FIRE_RANGE = 3;

        private final GamePanel gp;
        private final List<Point> spawnPoints = new ArrayList<>();
        private final List<Point> rockRespawnPoints = new ArrayList<>();
        private final List<Point> pendingRockRespawns = new ArrayList<>();
        private final List<GameObject> pendingRespawnRocks = new ArrayList<>();
        private int fallbackLeftCol;
        private int fallbackTopRow;
        private List<LockBolt> controlledLocks = new ArrayList<>();
        private BossPhase phase = BossPhase.IDLE;
        private int health = MAX_HEALTH;
        private int phaseTimer;
        private int hurtFlashFrames;
        private int headLeftCol;
        private double headTopRow;
        private boolean battleStarted;

        private SnakeBossController(GamePanel gp, List<Point> spawnMarkers) {
            this.gp = gp;
            findSpawnPoints(spawnMarkers);
            this.fallbackLeftCol = spawnPoints.get(0).x;
            this.fallbackTopRow = spawnPoints.get(0).y;
            this.headLeftCol = fallbackLeftCol;
            this.headTopRow = fallbackTopRow;
            findRockRespawnPoints();
            rebindLocks();
            setLocksOpenInstantly();
        }

        private void resetAfterRestore() {
            phase = BossPhase.IDLE;
            health = MAX_HEALTH;
            phaseTimer = 0;
            hurtFlashFrames = 0;
            battleStarted = false;
            headLeftCol = fallbackLeftCol;
            headTopRow = fallbackTopRow;
            pendingRockRespawns.clear();
            pendingRespawnRocks.clear();
            rebindLocks();
            setLocksOpenInstantly();
        }

        private void update() {
            if (phase == BossPhase.DEAD) {
                updateLocks(true);
                return;
            }

            if (!battleStarted && gp.player.getCol() > getFirstLockCol()) {
                startBattle();
            }

            updateLocks(!battleStarted);
            if (!battleStarted) {
                return;
            }

            if (hurtFlashFrames > 0) {
                hurtFlashFrames--;
            }

            phaseTimer++;
            switch (phase) {
                case EMERGE -> {
                    double progress = Math.min(1.0, phaseTimer / (double) EMERGE_FRAMES);
                    headTopRow = getHiddenHeadTopRow() + (getHoleHeadTopRow() - getHiddenHeadTopRow()) * progress;
                    if (phaseTimer >= EMERGE_FRAMES) {
                        startPhase(BossPhase.WAIT_HEAD);
                    }
                }
                case WAIT_HEAD -> {
                    if (phaseTimer >= WAIT_HEAD_FRAMES) {
                        startPhase(BossPhase.CHARGE);
                    }
                }
                case CHARGE -> {
                    double progress = Math.min(1.0, phaseTimer / (double) CHARGE_FRAMES);
                    headTopRow = getHoleHeadTopRow() + (getCrashTopRow() - getHoleHeadTopRow()) * progress;
                    if (phaseTimer >= CHARGE_FRAMES) {
                        respawnPendingRocks();
                        startPhase(BossPhase.STUN_FIRE);
                    }
                }
                case STUN_FIRE -> {
                    if (phaseTimer >= STUN_FIRE_FRAMES) {
                        startPhase(BossPhase.RETREAT);
                    }
                }
                case RETREAT -> {
                    double progress = Math.min(1.0, phaseTimer / (double) RETREAT_FRAMES);
                    headTopRow = getCrashTopRow() + (getHoleHeadTopRow() - getCrashTopRow()) * progress;
                    if (phaseTimer >= RETREAT_FRAMES) {
                        startPhase(BossPhase.SUBMERGE);
                    }
                }
                case SUBMERGE -> {
                    double progress = Math.min(1.0, phaseTimer / (double) SUBMERGE_FRAMES);
                    headTopRow = getHoleHeadTopRow() + (getHiddenHeadTopRow() - getHoleHeadTopRow()) * progress;
                    if (phaseTimer >= SUBMERGE_FRAMES) {
                        chooseNextSpawn();
                        startPhase(BossPhase.EMERGE);
                    }
                }
                default -> {
                }
            }
        }

        private void startBattle() {
            battleStarted = true;
            chooseNextSpawn();
            startPhase(BossPhase.EMERGE);
        }

        private void startPhase(BossPhase nextPhase) {
            phase = nextPhase;
            phaseTimer = 0;
            if (nextPhase == BossPhase.EMERGE) {
                headTopRow = getHiddenHeadTopRow();
            } else if (nextPhase == BossPhase.WAIT_HEAD) {
                headTopRow = getHoleHeadTopRow();
            } else if (nextPhase == BossPhase.CHARGE) {
                headTopRow = getHoleHeadTopRow();
            } else if (nextPhase == BossPhase.STUN_FIRE) {
                headTopRow = getCrashTopRow();
            } else if (nextPhase == BossPhase.SUBMERGE) {
                headTopRow = getHoleHeadTopRow();
            }
        }

        private void chooseNextSpawn() {
            Point spawn = spawnPoints.isEmpty()
                    ? new Point(fallbackLeftCol, fallbackTopRow)
                    : spawnPoints.get(gp.random.nextInt(spawnPoints.size()));
            headLeftCol = spawn.x;
            headTopRow = spawn.y;
        }

        private void findSpawnPoints(List<Point> spawnMarkers) {
            boolean[] used = new boolean[spawnMarkers.size()];
            for (int i = 0; i < spawnMarkers.size(); i++) {
                if (used[i]) {
                    continue;
                }
                Point first = spawnMarkers.get(i);
                Point second = null;
                for (int j = i + 1; j < spawnMarkers.size(); j++) {
                    Point candidate = spawnMarkers.get(j);
                    if (!used[j] && candidate.x == first.x && Math.abs(candidate.y - first.y) == 1) {
                        second = candidate;
                        used[j] = true;
                        break;
                    }
                }
                used[i] = true;
                int leftCol = first.x - 1;
                int topRow = second == null ? first.y : Math.min(first.y, second.y);
                spawnPoints.add(new Point(leftCol, topRow));
            }
            if (spawnPoints.isEmpty()) {
                spawnPoints.add(new Point(1, 1));
            }
        }

        private void findRockRespawnPoints() {
            int minCol = spawnPoints.stream().mapToInt(point -> point.x).min().orElse(fallbackLeftCol) - 3;
            int maxCol = spawnPoints.stream().mapToInt(point -> point.x).max().orElse(fallbackLeftCol) + 4;
            int topRow = spawnPoints.stream().mapToInt(point -> point.y).min().orElse(fallbackTopRow);
            for (GameObject object : gp.objects) {
                if (object instanceof Rock && object.isActive()) {
                    int row = object.getRow(gp);
                    int col = object.getCol(gp);
                    if (row <= topRow && col >= minCol && col <= maxCol) {
                        rockRespawnPoints.add(new Point(col, row));
                    }
                }
            }
        }

        private void rebindLocks() {
            controlledLocks = new ArrayList<>();
            for (GameObject object : gp.objects) {
                if (object instanceof LockBolt lockBolt && !(object instanceof SpecialLock)) {
                    controlledLocks.add(lockBolt);
                }
            }
        }

        private void setLocksOpenInstantly() {
            for (LockBolt lock : controlledLocks) {
                lock.restoreState(2, 0);
            }
        }

        private void updateLocks(boolean shouldOpen) {
            for (LockBolt lock : controlledLocks) {
                lock.update(shouldOpen);
            }
        }

        private int getFirstLockCol() {
            return controlledLocks.stream()
                    .mapToInt(lock -> lock.getCol(gp))
                    .min()
                    .orElse(fallbackLeftCol - 1);
        }

        private boolean controlsLock(LockBolt lockBolt) {
            return controlledLocks.contains(lockBolt);
        }

        private boolean hitWithHammer(int row, int col) {
            if (!battleStarted || phase == BossPhase.DEAD || !occupies(row, col)) {
                return false;
            }
            if (hurtFlashFrames > 0) {
                return true;
            }
            damage();
            return true;
        }

        private boolean hitWithRock(int row, int col, GameObject rock) {
            if (!battleStarted || !canRockDamageBoss() || !isRockHittingHead(row, col)) {
                return false;
            }
            rock.setActive(false);
            rememberRockRespawn(rock.getCol(gp));
            damage();
            return true;
        }

        private boolean canRockDamageBoss() {
            return phase == BossPhase.EMERGE || phase == BossPhase.WAIT_HEAD || phase == BossPhase.CHARGE;
        }

        private boolean isRockHittingHead(int row, int col) {
            if (col < headLeftCol || col > headLeftCol + 1) {
                return false;
            }
            int top = getHeadTopRow();
            int bottom = top + 1;
            int holeTop = getHoleHeadTopRow();
            int holeBottom = holeTop + 2;
            return (row >= top && row <= bottom) || (row >= holeTop - 1 && row <= holeBottom);
        }

        private void trackPushedRock(GameObject rock, int targetRow, int targetCol) {
            if (!battleStarted || phase == BossPhase.DEAD || !(rock instanceof Rock) || !rock.isActive()) {
                return;
            }

            Point respawnPoint = getNearestRockRespawnPoint(targetCol);
            if (respawnPoint == null || targetRow <= respawnPoint.y) {
                return;
            }

            if (!pendingRespawnRocks.contains(rock)) {
                pendingRespawnRocks.add(rock);
            }
            addPendingRespawn(respawnPoint);
        }

        private void rememberRockRespawn(int rockCol) {
            if (rockRespawnPoints.isEmpty()) {
                return;
            }
            Point nearest = getNearestRockRespawnPoint(rockCol);
            if (nearest == null) {
                return;
            }
            addPendingRespawn(nearest);
        }

        private Point getNearestRockRespawnPoint(int rockCol) {
            if (rockRespawnPoints.isEmpty()) {
                return null;
            }
            Point nearest = rockRespawnPoints.get(0);
            int nearestDistance = Math.abs(nearest.x - rockCol);
            for (Point point : rockRespawnPoints) {
                int distance = Math.abs(point.x - rockCol);
                if (distance < nearestDistance) {
                    nearest = point;
                    nearestDistance = distance;
                }
            }
            return nearestDistance <= 2 ? nearest : null;
        }

        private void addPendingRespawn(Point point) {
            for (Point pending : pendingRockRespawns) {
                if (pending.x == point.x && pending.y == point.y) {
                    return;
                }
            }
            pendingRockRespawns.add(new Point(point));
        }

        private void respawnPendingRocks() {
            for (GameObject rock : pendingRespawnRocks) {
                rock.setActive(false);
            }
            pendingRespawnRocks.clear();

            for (Point point : pendingRockRespawns) {
                if (hasActiveRockNear(point)) {
                    continue;
                }
                int spawnRow = Math.max(1, point.y - 2);
                while (spawnRow < point.y && !gp.mapLoader.isGround(spawnRow, point.x)) {
                    spawnRow++;
                }
                gp.objects.add(new Rock(point.x * gp.tileSize, spawnRow * gp.tileSize));
            }
            pendingRockRespawns.clear();
        }

        private boolean hasActiveRockNear(Point point) {
            for (GameObject object : gp.objects) {
                if (object instanceof Rock
                        && object.isActive()
                        && object.getCol(gp) == point.x
                        && object.getRow(gp) <= point.y) {
                    return true;
                }
            }
            return false;
        }

        private void damage() {
            if (phase == BossPhase.DEAD || health <= 0) {
                return;
            }
            health--;
            hurtFlashFrames = HURT_FLASH_FRAMES;
            if (health <= 0) {
                phase = BossPhase.DEAD;
                battleStarted = false;
                pendingRockRespawns.clear();
                pendingRespawnRocks.clear();
                updateLocks(true);
            }
        }

        private boolean isDangerAt(int row, int col) {
            if (!battleStarted || phase == BossPhase.DEAD) {
                return false;
            }
            if (phase == BossPhase.STUN_FIRE && hasFireAt(row, col)) {
                return true;
            }
            return (phase == BossPhase.CHARGE || phase == BossPhase.EMERGE || phase == BossPhase.WAIT_HEAD)
                    && occupies(row, col);
        }

        private boolean hasFireAt(int row, int col) {
            int fireRow = getHeadTopRow() + 1;
            if (row != fireRow) {
                return false;
            }
            int leftOffset = headLeftCol - col;
            int rightOffset = col - (headLeftCol + 1);
            return (leftOffset >= 1 && leftOffset <= FIRE_RANGE)
                    || (rightOffset >= 1 && rightOffset <= FIRE_RANGE);
        }

        private boolean occupies(int row, int col) {
            return occupiesHead(row, col) || occupiesBody(row, col);
        }

        private boolean occupiesHead(int row, int col) {
            int top = getHeadTopRow();
            return row >= top && row <= top + 1 && col >= headLeftCol && col <= headLeftCol + 1;
        }

        private boolean occupiesBody(int row, int col) {
            if (phase == BossPhase.EMERGE || phase == BossPhase.WAIT_HEAD || phase == BossPhase.SUBMERGE) {
                return false;
            }
            int top = getHeadTopRow() + 2;
            return row >= top && row <= top + 3 && col >= headLeftCol && col <= headLeftCol + 1;
        }

        private int getHeadTopRow() {
            return (int) Math.round(headTopRow);
        }

        private int getHoleHeadTopRow() {
            return spawnPoints.stream()
                    .filter(point -> point.x == headLeftCol)
                    .findFirst()
                    .map(point -> point.y)
                    .orElse(fallbackTopRow);
        }

        private int getHiddenHeadTopRow() {
            return getHoleHeadTopRow() + 6;
        }

        private int getCrashTopRow() {
            return Math.max(1, getHoleHeadTopRow() - 4);
        }

        private int getClipTopY() {
            if (phase == BossPhase.EMERGE || phase == BossPhase.SUBMERGE) {
                return getHoleHeadTopRow() * gp.tileSize;
            }
            return 0;
        }

        private int getClipHeight() {
            if (phase == BossPhase.EMERGE || phase == BossPhase.SUBMERGE) {
                return gp.tileSize * 2;
            }
            return getHoleHeadTopRow() * gp.tileSize + gp.tileSize * 6;
        }

        private void draw(Graphics2D g2) {
            if (phase == BossPhase.DEAD) {
                return;
            }
            if (hurtFlashFrames > 0 && (hurtFlashFrames / 4) % 2 == 0) {
                return;
            }
            if (!battleStarted) {
                return;
            }
            if (phase == BossPhase.SUBMERGE && headTopRow > getHoleHeadTopRow() + 2) {
                return;
            }

            int top = getHeadTopRow();
            java.awt.Shape oldClip = g2.getClip();
            g2.clipRect(
                    headLeftCol * gp.tileSize,
                    getClipTopY(),
                    gp.tileSize * 2,
                    getClipHeight());
            drawHead(g2, headLeftCol, top);
            drawBodyIfVisible(g2, headLeftCol, top + 2, 0);
            drawBodyIfVisible(g2, headLeftCol, top + 4, 1);
            g2.setClip(oldClip);
            if (phase == BossPhase.STUN_FIRE) {
                drawFire(g2);
            }
        }

        private void drawHead(Graphics2D g2, int col, int row) {
            int x = col * gp.tileSize;
            int y = row * gp.tileSize;
            int width = gp.tileSize;
            int height = gp.tileSize * 2;
            g2.drawImage(AssetManager.snakePre, x, y, width, height, null);
            g2.drawImage(AssetManager.snakePre, x + width * 2, y, -width, height, null);
        }

        private void drawBodyIfVisible(Graphics2D g2, int col, int row, int bodyIndex) {
            if (phase == BossPhase.EMERGE || phase == BossPhase.WAIT_HEAD || phase == BossPhase.SUBMERGE) {
                return;
            }
            if (!shouldDrawBodyPart(bodyIndex)) {
                return;
            }
            g2.drawImage(AssetManager.bodySnakePreFrames[bodyIndex],
                    col * gp.tileSize,
                    row * gp.tileSize,
                    gp.tileSize * 2,
                    gp.tileSize * 2,
                    null);
        }

        private boolean shouldDrawBodyPart(int bodyIndex) {
            if (phase != BossPhase.CHARGE) {
                return true;
            }
            double progress = Math.min(1.0, phaseTimer / (double) CHARGE_FRAMES);
            return bodyIndex == 0 ? progress >= 0.28 : progress >= 0.58;
        }

        private void drawFire(Graphics2D g2) {
            int row = getHeadTopRow() + 1;
            for (int i = 1; i <= FIRE_RANGE; i++) {
                g2.drawImage(getFireImage(i),
                        (headLeftCol - i) * gp.tileSize,
                        row * gp.tileSize,
                        gp.tileSize,
                        gp.tileSize,
                        null);
                g2.drawImage(getFireImage(i),
                        (headLeftCol + 1 + i) * gp.tileSize,
                        row * gp.tileSize,
                        gp.tileSize,
                        gp.tileSize,
                        null);
            }
        }

        private java.awt.image.BufferedImage getFireImage(int offset) {
            return switch (offset) {
                case 1 -> AssetManager.fire1;
                case 2 -> AssetManager.fire2;
                default -> AssetManager.fire3;
            };
        }

        private void drawHealthBar(Graphics2D g2) {
            if (!battleStarted || phase == BossPhase.DEAD) {
                return;
            }
            int width = 520;
            int height = 26;
            int x = (gp.getWidth() - width) / 2;
            int y = 26;
            g2.setColor(new Color(30, 10, 10, 210));
            g2.fillRoundRect(x, y, width, height, 12, 12);
            g2.setColor(new Color(255, 214, 102));
            g2.drawRoundRect(x, y, width, height, 12, 12);

            int hpWidth = Math.max(0, (width - 8) * health / MAX_HEALTH);
            g2.setColor(new Color(190, 30, 30));
            g2.fillRoundRect(x + 4, y + 4, hpWidth, height - 8, 8, 8);

            g2.setFont(new java.awt.Font("Serif", java.awt.Font.BOLD, 18));
            String text = "SNAKE BOSS  " + health + "/" + MAX_HEALTH;
            g2.setColor(Color.WHITE);
            g2.drawString(text, x + (width - g2.getFontMetrics().stringWidth(text)) / 2, y + 20);
        }

        private enum BossPhase {
            IDLE,
            EMERGE,
            WAIT_HEAD,
            CHARGE,
            STUN_FIRE,
            RETREAT,
            SUBMERGE,
            DEAD
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
        private final int state1;
        private final int state2;

        private ObjectSnapshot(String type, int row, int col, boolean active) {
            this(type, row, col, active, 0, 0);
        }

        private ObjectSnapshot(String type, int row, int col, boolean active, int state1, int state2) {
            this.type = type;
            this.row = row;
            this.col = col;
            this.active = active;
            this.state1 = state1;
            this.state2 = state2;
        }
    }

    private static final class EnemySnapshot {
        private final int x;
        private final int y;
        private final boolean horizontalMovement;
        private final int snakeVariant;
        private final int moveDirection;
        private final int moveCounter;
        private final int stunFrames;
        private final int deathFrames;

        private EnemySnapshot(int x, int y, boolean horizontalMovement, int snakeVariant, int moveDirection,
                              int moveCounter, int stunFrames, int deathFrames) {
            this.x = x;
            this.y = y;
            this.horizontalMovement = horizontalMovement;
            this.snakeVariant = snakeVariant;
            this.moveDirection = moveDirection;
            this.moveCounter = moveCounter;
            this.stunFrames = stunFrames;
            this.deathFrames = deathFrames;
        }
    }
}
