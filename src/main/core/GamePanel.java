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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
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
    private static final int RESET_SPAWN_ZOOM_FRAMES = 54;
    private static final double RESET_SPAWN_ZOOM_SCALE = 1.75;
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
    private final Set<GameObject> fallingRocks = Collections.newSetFromMap(new IdentityHashMap<>());
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
    private boolean resetSpawnZoomActive;
    private int resetSpawnZoomFrame;
    private SnakeBossController snakeBossController;
    private TutorialPrompt activeTutorialPrompt;
    private boolean tutorialEnterWasDown;
    private int tutorialFrame;
    private boolean hammerTutorialShown;
    private boolean suppressLevelTutorialPrompts;

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
        hammerTutorialShown = false;
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
            suppressLevelTutorialPrompts = true;
            try {
                loadLevel(currentLevelIndex);
                player.lives = currentLives;
            } finally {
                suppressLevelTutorialPrompts = false;
            }
        }
        rockPressureFrames = 0;
        gsm.setState(GameState.PLAYING);
        keyH.clearActionKeys();
        keyH.clearMovementKeys();
        updateCamera();
        playSfx(SoundManager.SFX_CHECKPOINT);
        startResetSpawnZoom();
    }

    public void startFinalDeathZoom() {
        resetSpawnZoom();
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

    private void startResetSpawnZoom() {
        resetSpawnZoomActive = true;
        resetSpawnZoomFrame = 0;
    }

    private void resetSpawnZoom() {
        resetSpawnZoomActive = false;
        resetSpawnZoomFrame = 0;
    }

    private void advanceResetSpawnZoom() {
        if (!resetSpawnZoomActive) {
            return;
        }
        resetSpawnZoomFrame++;
        if (resetSpawnZoomFrame >= RESET_SPAWN_ZOOM_FRAMES) {
            resetSpawnZoom();
        }
    }

    private double getFinalDeathZoomScale() {
        if (!finalDeathZoomActive) {
            return 1.0;
        }
        double progress = Math.min(1.0, finalDeathZoomFrame / (double) FINAL_DEATH_ZOOM_FRAMES);
        double eased = 1.0 - Math.pow(1.0 - progress, 3);
        return 1.0 + (FINAL_DEATH_ZOOM_SCALE - 1.0) * eased;
    }

    private double getResetSpawnZoomScale() {
        if (!resetSpawnZoomActive) {
            return 1.0;
        }
        double progress = Math.min(1.0, resetSpawnZoomFrame / (double) RESET_SPAWN_ZOOM_FRAMES);
        double eased = 1.0 - Math.pow(1.0 - progress, 3);
        return RESET_SPAWN_ZOOM_SCALE - (RESET_SPAWN_ZOOM_SCALE - 1.0) * eased;
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
        resetSpawnZoom();
        objects.clear();
        enemies.clear();
        spawnPoints.clear();
        fallingRocks.clear();
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
        showFirstLevelTutorialIfNeeded(index);

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
        showHammerTutorialIfNeeded();
    }

    public boolean hasKey() {
        return hasKey;
    }

    public void acquireKey() {
        hasKey = true;
        updateKeyPickupsForOwnership();
    }

    private void showFirstLevelTutorialIfNeeded(int levelIndex) {
        if (!suppressLevelTutorialPrompts && gsm != null && levelIndex == 0) {
            showGameplayTutorial(TutorialPrompt.BASIC_CONTROLS);
        }
    }

    private void showHammerTutorialIfNeeded() {
        if (!hammerTutorialShown) {
            hammerTutorialShown = true;
            showGameplayTutorial(TutorialPrompt.HAMMER);
        }
    }

    private void showBossTutorial() {
        showGameplayTutorial(TutorialPrompt.BOSS);
    }

    public void showFinalDiamondCelebration() {
        showGameplayTutorial(TutorialPrompt.FINAL_DIAMOND);
    }

    private void showGameplayTutorial(TutorialPrompt prompt) {
        activeTutorialPrompt = prompt;
        tutorialEnterWasDown = true;
        tutorialFrame = 0;
        keyH.clearActionKeys();
        keyH.clearMovementKeys();
    }

    private void handleGameplayTutorialInput() {
        tutorialFrame++;
        boolean enterDown = keyH.enterPressed;
        if (enterDown && !tutorialEnterWasDown) {
            TutorialPrompt closedPrompt = activeTutorialPrompt;
            activeTutorialPrompt = null;
            tutorialEnterWasDown = true;
            keyH.clearActionKeys();
            keyH.clearMovementKeys();
            if (closedPrompt == TutorialPrompt.BOSS && snakeBossController != null) {
                snakeBossController.confirmBossIntro();
            } else if (closedPrompt == TutorialPrompt.FINAL_DIAMOND) {
                levelComplete = true;
                completeCurrentLevel();
            }
            return;
        }
        tutorialEnterWasDown = enterDown;
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
            if (isPersistentlyCollectedDiamond(object) && !object.isActive()) {
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
            if (isPersistentlyCollectedDiamond(object)
                    && collectedDiamondKeys.contains(objectKey(object.getRow(this), object.getCol(this)))) {
                object.setActive(false);
                collected++;
            } else if (object instanceof FinalDiamondPre) {
                collectedDiamondKeys.remove(objectKey(object.getRow(this), object.getCol(this)));
            }
        }
        player.score = collected;
    }

    private boolean isPersistentlyCollectedDiamond(GameObject object) {
        return object instanceof Diamond && !(object instanceof FinalDiamondPre);
    }

    private String objectKey(int row, int col) {
        return row + ":" + col;
    }

    public void update() {
        if (activeTutorialPrompt != null && gsm != null && gsm.getState() == GameState.PLAYING) {
            handleGameplayTutorialInput();
            advanceResetSpawnZoom();
            return;
        }

        gsm.update(keyH);
        handleResetShortcut();
        advanceResetSpawnZoom();

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
                boolean hitEnemy = getEnemyAt(belowRow, col) != null;
                if (moveRockTo(object, belowRow, col)) {
                    updateRockFallSoundAfterMove(object, hitEnemy);
                }
            } else {
                int slideDirection = getRockSlideDirection(row, col);
                if (slideDirection != 0) {
                    boolean hitEnemy = getEnemyAt(belowRow, col + slideDirection) != null;
                    if (moveRockTo(object, belowRow, col + slideDirection)) {
                        updateRockFallSoundAfterMove(object, hitEnemy);
                    }
                } else {
                    finishRockFall(object);
                }
            }
        }
    }

    private void updateRockFallSoundAfterMove(GameObject object, boolean hitEnemy) {
        if (!(object instanceof Rock)) {
            return;
        }

        if (hitEnemy) {
            playSfx(SoundManager.SFX_ROCK_FALL);
            fallingRocks.remove(object);
            return;
        }

        if (canRockKeepFalling(object)) {
            fallingRocks.add(object);
            return;
        }

        playSfx(SoundManager.SFX_ROCK_FALL);
        fallingRocks.remove(object);
    }

    private void finishRockFall(GameObject object) {
        if (object instanceof Rock && fallingRocks.remove(object)) {
            playSfx(SoundManager.SFX_ROCK_FALL);
        }
    }

    private boolean canRockKeepFalling(GameObject object) {
        int row = object.getRow(this);
        int col = object.getCol(this);
        return canRockOccupy(row + 1, col) || getRockSlideDirection(row, col) != 0;
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

    private boolean moveRockTo(GameObject rock, int row, int col) {
        if (snakeBossController != null && snakeBossController.hitWithRock(row, col, rock)) {
            return false;
        }
        if (snakeBossController != null) {
            snakeBossController.trackPushedRock(rock, row, col);
        }
        Enemy enemy = getEnemyAt(row, col);
        if (enemy != null) {
            enemy.crushByRock();
        }
        rock.setGridPosition(this, row, col);
        return true;
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
        fallingRocks.clear();
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

    private void drawGameplayTutorialOverlay(Graphics2D g2) {
        if (activeTutorialPrompt == null) {
            return;
        }

        int w = getWidth();
        int h = getHeight();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(new Color(0, 0, 0, activeTutorialPrompt == TutorialPrompt.HAMMER ? 72 : 46));
        g2.fillRect(0, 0, w, h);

        if (activeTutorialPrompt == TutorialPrompt.FINAL_DIAMOND) {
            drawFinalDiamondCelebrationOverlay(g2, w, h);
            return;
        }

        if (activeTutorialPrompt == TutorialPrompt.BOSS) {
            drawBossIntroOverlay(g2, w, h);
            return;
        }

        if (activeTutorialPrompt == TutorialPrompt.HAMMER) {
            drawHammerPickupFocus(g2, w, h);
        }

        int barMargin = 44;
        int barW = w - barMargin * 2;
        int barH = activeTutorialPrompt == TutorialPrompt.BASIC_CONTROLS ? 206 : 190;
        int x = barMargin;
        int y = h - barH - 34;

        g2.setColor(new Color(18, 11, 5, 225));
        g2.fillRoundRect(x + 7, y + 8, barW, barH, 24, 24);
        GradientPaint panelPaint = new GradientPaint(x, y,
                new Color(105, 68, 28, 242),
                x, y + barH,
                new Color(36, 21, 10, 242));
        g2.setPaint(panelPaint);
        g2.fillRoundRect(x, y, barW, barH, 24, 24);
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(255, 214, 102, 235));
        g2.drawRoundRect(x, y, barW, barH, 24, 24);
        g2.setColor(new Color(255, 248, 210, 92));
        g2.drawRoundRect(x + 10, y + 10, barW - 20, barH - 20, 18, 18);

        drawTutorialContent(g2, activeTutorialPrompt, x, y, barW, barH);
    }

    private void drawFinalDiamondCelebrationOverlay(Graphics2D g2, int w, int h) {
        double settle = Math.min(1.0, tutorialFrame / 28.0);
        double eased = 1.0 - Math.pow(1.0 - settle, 3);
        double pulse = Math.sin(tutorialFrame / 8.0) * 0.05;

        g2.setColor(new Color(0, 0, 0, 132));
        g2.fillRect(0, 0, w, h);

        int panelW = 760;
        int panelH = 330;
        int x = (w - panelW) / 2;
        int y = h - panelH - 58;

        int cx = w / 2;
        int cy = Math.max(150, y - 132);
        int diamondSize = (int) Math.round(tileSize * (1.0 + 2.55 * eased + pulse));

        for (int i = 7; i >= 1; i--) {
            int glowSize = diamondSize + i * 34;
            int alpha = Math.max(14, 86 - i * 9);
            g2.setColor(new Color(95, 230, 255, alpha));
            g2.fillOval(cx - glowSize / 2, cy - glowSize / 2, glowSize, glowSize);
        }
        g2.setColor(new Color(255, 255, 230, 135));
        g2.setStroke(new BasicStroke(4f));
        g2.drawOval(cx - diamondSize / 2 - 16, cy - diamondSize / 2 - 16, diamondSize + 32, diamondSize + 32);
        g2.drawImage(AssetManager.diamondPre, cx - diamondSize / 2, cy - diamondSize / 2,
                diamondSize, diamondSize, null);

        g2.setColor(new Color(11, 8, 5, 226));
        g2.fillRoundRect(x + 8, y + 10, panelW, panelH, 28, 28);
        GradientPaint paint = new GradientPaint(x, y,
                new Color(38, 75, 90, 244),
                x, y + panelH,
                new Color(24, 15, 9, 246));
        g2.setPaint(paint);
        g2.fillRoundRect(x, y, panelW, panelH, 28, 28);
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(155, 242, 255, 232));
        g2.drawRoundRect(x, y, panelW, panelH, 28, 28);
        g2.setColor(new Color(255, 248, 205, 86));
        g2.drawRoundRect(x + 12, y + 12, panelW - 24, panelH - 24, 20, 20);

        g2.setFont(new Font("Georgia", Font.BOLD, 42));
        String title = "CHIẾN THẮNG!";
        drawOutlinedText(g2, title, getCenterX(g2, title, w), y + 82,
                new Color(255, 232, 122), new Color(36, 22, 9));

        g2.setFont(new Font("Georgia", Font.BOLD, 22));
        drawTutorialParagraph(g2,
                "Bạn đã đánh bại con rắn cổ, lấy lại viên kim cương cuối cùng và mở được lối thoát khỏi hầm ngục.",
                x + 80, y + 142, panelW - 160, 30, 22);

        g2.setFont(new Font("Georgia", Font.BOLD, 23));
        String escaped = "Thoát khỏi hầm ngục thành công";
        drawOutlinedText(g2, escaped, getCenterX(g2, escaped, w), y + 232,
                new Color(128, 242, 255), new Color(18, 38, 47));

        g2.setFont(new Font("Georgia", Font.BOLD, 20));
        String hint = "Nhấn ENTER để tiếp tục";
        drawOutlinedText(g2, hint, getCenterX(g2, hint, w), y + panelH - 36,
                new Color(255, 245, 205), new Color(45, 25, 10));
    }

    private void drawBossIntroOverlay(Graphics2D g2, int w, int h) {
        int panelW = 860;
        int panelH = 520;
        int x = (w - panelW) / 2;
        int y = (h - panelH) / 2 + 10;

        g2.setColor(new Color(0, 0, 0, 118));
        g2.fillRect(0, 0, w, h);

        for (int i = 5; i >= 1; i--) {
            g2.setColor(new Color(255, 190, 60, 15 + i * 4));
            g2.drawRoundRect(x - i * 5, y - i * 5, panelW + i * 10, panelH + i * 10, 34, 34);
        }

        g2.setColor(new Color(12, 7, 4, 232));
        g2.fillRoundRect(x + 9, y + 11, panelW, panelH, 30, 30);
        GradientPaint paint = new GradientPaint(x, y,
                new Color(92, 42, 24, 246),
                x, y + panelH,
                new Color(22, 13, 8, 248));
        g2.setPaint(paint);
        g2.fillRoundRect(x, y, panelW, panelH, 30, 30);
        g2.setStroke(new BasicStroke(3.2f));
        g2.setColor(new Color(255, 206, 92, 238));
        g2.drawRoundRect(x, y, panelW, panelH, 30, 30);
        g2.setColor(new Color(255, 248, 200, 75));
        g2.drawRoundRect(x + 12, y + 12, panelW - 24, panelH - 24, 22, 22);

        int pad = 42;
        int artX = x + pad;
        int artY = y + 116;
        int artW = 202;
        int artH = 246;
        int contentX = x + 294;
        int contentW = panelW - 336;

        drawBossSnakeEmblem(g2, artX, artY, artW, artH);

        g2.setFont(new Font("Georgia", Font.BOLD, 42));
        drawOutlinedText(g2, "RẮN CANH CỬA", contentX, y + 76,
                new Color(255, 226, 112), new Color(48, 22, 8));
        g2.setFont(new Font("Georgia", Font.BOLD, 20));
        drawOutlinedText(g2, "Lời nguyền dưới hầm mỏ", contentX + 4, y + 110,
                new Color(255, 246, 210), new Color(48, 22, 8));

        int storyY = y + 158;
        drawTutorialParagraph(g2,
                "Ngày xưa, lõi kim cương ở tầng sâu nhất đã đánh thức một con rắn cổ. Nó nuốt ánh sáng của hầm mỏ, lớn dần qua từng năm và cuộn mình trước cánh cửa cuối cùng.",
                contentX, storyY, contentW, 24, 19);
        drawTutorialParagraph(g2,
                "Khi cửa đá khép lại, nó sẽ trồi lên từ lòng đất. Muốn thoát khỏi hầm ngục, hãy dùng chính những viên đá phía trên để giáng xuống đầu nó.",
                contentX, storyY + 96, contentW, 24, 19);

        int cardY = y + 354;
        drawBossTipCard(g2, x + pad, cardY, "NÉ ĐÒN", "Tránh đầu rắn và luồng lửa khi nó lao lên.", 365);
        drawBossTipCard(g2, x + panelW - pad - 365, cardY, "ĐÁNH BẠI", "Đẩy đá rơi xuống đầu rắn khi nó trồi lên.", 365);

        g2.setFont(new Font("Georgia", Font.BOLD, 21));
        String hint = "Nhấn ENTER để bước vào trận chiến";
        drawOutlinedText(g2, hint, getCenterX(g2, hint, w), y + panelH - 35,
                new Color(255, 245, 205), new Color(45, 25, 10));
    }

    private void drawBossSnakeEmblem(Graphics2D g2, int x, int y, int width, int height) {
        int pulse = (int) Math.round(Math.sin(tutorialFrame / 9.0) * 8);

        g2.setColor(new Color(255, 190, 60, 30));
        g2.fillRoundRect(x - 16 - pulse / 2, y - 16 - pulse / 2, width + 32 + pulse, height + 32 + pulse, 28, 28);
        g2.setColor(new Color(13, 8, 5, 188));
        g2.fillRoundRect(x, y, width, height, 24, 24);
        g2.setStroke(new BasicStroke(2.4f));
        g2.setColor(new Color(255, 209, 95, 140));
        g2.drawRoundRect(x, y, width, height, 24, 24);

        int imageH = height - 42;
        int halfW = Math.max(1, AssetManager.snakePre.getWidth() * imageH / AssetManager.snakePre.getHeight());
        int centerX = x + width / 2;
        int drawY = y + 22;
        int leftX = centerX - halfW;
        int rightX = centerX;

        g2.drawImage(AssetManager.snakePre, leftX, drawY, halfW, imageH, null);
        g2.drawImage(AssetManager.snakePre, rightX + halfW, drawY, -halfW, imageH, null);

        g2.setColor(new Color(255, 70, 54, 90));
        g2.fillOval(centerX - 38, y + 74, 28, 18);
        g2.fillOval(centerX + 10, y + 74, 28, 18);
        g2.setColor(new Color(255, 226, 120, 78));
        g2.drawLine(centerX, y + 46, centerX, y + height - 28);
    }

    private void drawBossTipCard(Graphics2D g2, int x, int y, String title, String text, int cardW) {
        int cardH = 82;
        g2.setColor(new Color(255, 255, 255, 26));
        g2.fillRoundRect(x, y, cardW, cardH, 14, 14);
        g2.setColor(new Color(25, 12, 7, 132));
        g2.fillRoundRect(x + 12, y + 12, 104, cardH - 24, 10, 10);

        g2.setFont(new Font("Georgia", Font.BOLD, 17));
        drawOutlinedText(g2, title, x + 22, y + 48,
                new Color(255, 226, 110), new Color(42, 22, 7));
        g2.setFont(new Font("Georgia", Font.BOLD, 16));
        drawTutorialParagraph(g2, text, x + 134, y + 32, cardW - 152, 20, 14);
    }

    private void drawHammerPickupFocus(Graphics2D g2, int w, int h) {
        double settle = Math.min(1.0, tutorialFrame / 24.0);
        double eased = 1.0 - Math.pow(1.0 - settle, 3);
        double pulse = Math.sin(tutorialFrame / 7.0) * 0.055;
        int size = (int) Math.round(tileSize * (1.0 + 2.75 * eased + pulse));
        int cx = w / 2;
        int cy = h / 2 - 76;

        for (int i = 5; i >= 1; i--) {
            int glowSize = size + i * 28;
            int alpha = Math.max(14, 72 - i * 10);
            g2.setColor(new Color(255, 218, 82, alpha));
            g2.fillOval(cx - glowSize / 2, cy - glowSize / 2, glowSize, glowSize);
        }

        g2.setColor(new Color(255, 250, 190, 145));
        g2.setStroke(new BasicStroke(4f));
        g2.drawOval(cx - size / 2 - 14, cy - size / 2 - 14, size + 28, size + 28);
        g2.drawImage(AssetManager.hammer, cx - size / 2, cy - size / 2, size, size, null);
    }

    private void drawTutorialContent(Graphics2D g2, TutorialPrompt prompt, int x, int y, int panelW, int panelH) {
        switch (prompt) {
            case BASIC_CONTROLS -> {
                drawTutorialTitle(g2, "BẮT ĐẦU", "Điều khiển cơ bản", x, y, panelW);
                int col1 = x + 52;
                int col2 = x + 345;
                int col3 = x + 638;
                int row1 = y + 100;
                int row2 = y + 154;
                drawTutorialKeyRow(g2, col1, row1, "W / ↑", "Đi lên", 250);
                drawTutorialKeyRow(g2, col1, row2, "S / ↓", "Đi xuống", 250);
                drawTutorialKeyRow(g2, col2, row1, "A / ←", "Đi trái", 250);
                drawTutorialKeyRow(g2, col2, row2, "D / →", "Đi phải", 250);
                drawTutorialKeyRow(g2, col3, row1, "R", "Về spawn", 250);
                drawTutorialKeyRow(g2, col3, row2, "P / ESC", "Tạm dừng", 250);
            }
            case HAMMER -> {
                drawTutorialTitle(g2, "NHẶT ĐƯỢC BÚA", "Cách dùng búa", x, y, panelW);
                drawTutorialKeyRow(g2, x + 70, y + 115, "F", "Đập 4 ô kề cạnh nhân vật", 410);
                drawTutorialKeyRow(g2, x + 510, y + 115, "BÚA", "Phá bụi, phá nhựa, làm choáng rắn", 480);
                drawTutorialLine(g2, "Đứng sát mục tiêu rồi nhấn F đúng lúc.", x + 70, y + 160, panelW - 140, 21);
            }
            case BOSS -> {
                drawTutorialTitle(g2, "RẮN CANH CỬA", "Lối thoát đã bị chặn", x, y, panelW);
                drawTutorialLine(g2, "Cánh cửa đá vừa khép lại. Một con rắn khổng lồ đang canh lối thoát khỏi hầm ngục.",
                        x + 58, y + 92, panelW - 116, 20);
                drawTutorialLine(g2, "Né đầu rắn và luồng lửa của nó. Khi rắn trồi lên, hãy đẩy đá rơi xuống đúng đầu rắn.",
                        x + 58, y + 124, panelW - 116, 20);
                drawTutorialKeyRow(g2, x + 70, y + 166, "ĐÁ", "Đẩy đá xuống đầu rắn", 410);
                drawTutorialKeyRow(g2, x + 510, y + 166, "MỤC TIÊU", "Đánh bại nó để mở đường thoát", 480);
            }
        }

        g2.setFont(new Font("Georgia", Font.BOLD, 20));
        String hint = "Nhấn ENTER để tiếp tục";
        drawOutlinedText(g2, hint, getCenterX(g2, hint, getWidth()), y + panelH - 34,
                new Color(255, 245, 205), new Color(45, 25, 10));
    }

    private void drawTutorialTitle(Graphics2D g2, String title, String subtitle, int x, int y, int panelW) {
        g2.setFont(new Font("Georgia", Font.BOLD, 30));
        drawOutlinedText(g2, title, x + 40, y + 47,
                new Color(255, 226, 110), new Color(52, 28, 8));

        g2.setFont(new Font("Georgia", Font.BOLD, 18));
        drawOutlinedText(g2, subtitle, x + 42 + g2.getFontMetrics(new Font("Georgia", Font.BOLD, 30)).stringWidth(title),
                y + 47,
                new Color(255, 248, 220), new Color(52, 28, 8));
    }

    private void drawTutorialKeyRow(Graphics2D g2, int x, int y, String key, String text) {
        drawTutorialKeyRow(g2, x, y, key, text, 250);
    }

    private void drawTutorialKeyRow(Graphics2D g2, int x, int y, String key, String text, int rowW) {
        int rowH = 46;
        int keyW = 86;
        g2.setColor(new Color(255, 255, 255, 26));
        g2.fillRoundRect(x, y - 34, rowW, rowH, 12, 12);
        g2.setColor(new Color(26, 15, 7, 145));
        g2.fillRoundRect(x + 9, y - 27, keyW, rowH - 14, 9, 9);

        g2.setFont(new Font("Georgia", Font.BOLD, 18));
        FontMetrics keyMetrics = g2.getFontMetrics();
        drawOutlinedText(g2, key, x + 9 + (keyW - keyMetrics.stringWidth(key)) / 2, y - 5,
                new Color(255, 226, 110), new Color(42, 22, 7));

        g2.setFont(new Font("Georgia", Font.BOLD, 16));
        drawFittedTutorialText(g2, text, x + 110, y - 5, rowW - 124, 13,
                new Color(245, 237, 210), new Color(42, 24, 10));
    }

    private void drawTutorialLine(Graphics2D g2, String text, int x, int y, int maxWidth, int fontSize) {
        g2.setFont(new Font("Georgia", Font.BOLD, fontSize));
        drawFittedTutorialText(g2, text, x, y, maxWidth, 16,
                new Color(245, 237, 210), new Color(42, 24, 10));
    }

    private void drawTutorialParagraph(Graphics2D g2, String text, int x, int y, int maxWidth, int lineHeight,
                                       int fontSize) {
        g2.setFont(new Font("Georgia", Font.BOLD, fontSize));
        FontMetrics metrics = g2.getFontMetrics();
        StringBuilder line = new StringBuilder();
        int drawY = y;
        for (String word : text.split(" ")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (line.length() > 0 && metrics.stringWidth(candidate) > maxWidth) {
                drawOutlinedText(g2, line.toString(), x, drawY,
                        new Color(245, 237, 210), new Color(42, 24, 10));
                line = new StringBuilder(word);
                drawY += lineHeight;
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (line.length() > 0) {
            drawOutlinedText(g2, line.toString(), x, drawY,
                    new Color(245, 237, 210), new Color(42, 24, 10));
        }
    }

    private void drawFittedTutorialText(Graphics2D g2, String text, int x, int y, int maxWidth, int minSize,
                                        Color fill, Color outline) {
        Font originalFont = g2.getFont();
        Font fittedFont = originalFont;
        while (fittedFont.getSize() > minSize && g2.getFontMetrics(fittedFont).stringWidth(text) > maxWidth) {
            fittedFont = fittedFont.deriveFont((float) fittedFont.getSize() - 1f);
        }
        g2.setFont(fittedFont);
        drawOutlinedText(g2, text, x, y, fill, outline);
        g2.setFont(originalFont);
    }

    private void drawOutlinedText(Graphics2D g2, String text, int x, int y, Color fill, Color outline) {
        g2.setColor(outline);
        g2.drawString(text, x - 2, y);
        g2.drawString(text, x + 2, y);
        g2.drawString(text, x, y - 2);
        g2.drawString(text, x, y + 2);
        g2.setColor(fill);
        g2.drawString(text, x, y);
    }

    private int getCenterX(Graphics2D g2, String text, int w) {
        return (w - g2.getFontMetrics().stringWidth(text)) / 2;
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
        drawGameplayTutorialOverlay(g2);
        g2.dispose();
    }

    private void applyGameplayTransform(Graphics2D g2) {
        double zoom = Math.max(getFinalDeathZoomScale(), getResetSpawnZoomScale());
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
        private boolean waitingForIntroConfirm;

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
            waitingForIntroConfirm = false;
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

            if (!battleStarted && !waitingForIntroConfirm && gp.player.getCol() > getFirstLockCol()) {
                waitingForIntroConfirm = true;
                setLocksClosedInstantly();
                gp.showBossTutorial();
                return;
            }

            updateLocks(!battleStarted && !waitingForIntroConfirm);
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
            waitingForIntroConfirm = false;
            chooseNextSpawn();
            startPhase(BossPhase.EMERGE);
        }

        private void confirmBossIntro() {
            if (waitingForIntroConfirm && !battleStarted) {
                startBattle();
            }
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

        private void setLocksClosedInstantly() {
            for (LockBolt lock : controlledLocks) {
                lock.restoreState(0, 0);
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

    private enum TutorialPrompt {
        BASIC_CONTROLS,
        HAMMER,
        BOSS,
        FINAL_DIAMOND
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
