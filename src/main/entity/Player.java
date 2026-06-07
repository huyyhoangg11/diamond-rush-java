// Huy Hoàng làm
package main.entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import main.core.GamePanel;
import main.input.KeyHandler;
import main.map.MapLoader;
import main.object.Diamond;
import main.object.Door;
import main.object.GameObject;
import main.object.Hammer;
import main.object.Key;
import main.object.Rock;
import main.ui.SoundManager;
import main.util.AssetManager;

public class Player extends Entity {

    private static final int MOVE_COOLDOWN_FRAMES = 8;
    private static final int INVINCIBLE_FRAMES_AFTER_HIT = 90;
    private static final int HAMMER_FRAME_INTERVAL = 5;
    private static final int SPAWN_FRAME_INTERVAL = 9;
    private static final int DIE_FRAME_INTERVAL = 16;
    private static final int DIE_ZOOM_SCALE = 3;

    GamePanel gp;
    KeyHandler keyH;
    public int score;
    public int lives;
    private int moveCooldown;
    private int invincibleFrames;
    private int walkFrame;
    private int spawnFrame;
    private int spawnTimer;
    private int dieFrame;
    private int dieTimer;
    private boolean spawning;
    private boolean dying;
    private boolean gameOverAfterDeath;
    private boolean hammerKeyWasDown;
    private final List<HammerAnimation> hammerAnimations = new ArrayList<>();
    private final int startRow;
    private final int startCol;

    public Player(GamePanel gp, KeyHandler keyH, int startRow, int startCol) {
        this.gp = gp;
        this.keyH = keyH;
        this.startRow = startRow;
        this.startCol = startCol;

        setDefaultValues();
    }

    public void setDefaultValues() {
        x = startCol * gp.tileSize;
        y = startRow * gp.tileSize;
        speed = gp.tileSize;
        direction = "down";
        score = 0;
        lives = 3;
        moveCooldown = 0;
        invincibleFrames = 0;
        walkFrame = 0;
        hammerKeyWasDown = false;
        hammerAnimations.clear();
        startSpawnAnimation();
    }

    public void restoreState(int row, int col, int score, int lives) {
        x = col * gp.tileSize;
        y = row * gp.tileSize;
        this.score = score;
        this.lives = lives;
        moveCooldown = 0;
        invincibleFrames = 0;
        walkFrame = 0;
        hammerKeyWasDown = false;
        hammerAnimations.clear();
        startSpawnAnimation();
    }

    public void moveToSpawn() {
        x = startCol * gp.tileSize;
        y = startRow * gp.tileSize;
        moveCooldown = 0;
        walkFrame = 0;
        hammerKeyWasDown = false;
        hammerAnimations.clear();
        startSpawnAnimation();
    }

    public int getRow() {
        return y / gp.tileSize;
    }

    public int getCol() {
        return x / gp.tileSize;
    }

    public void update() {
        updateHammerAnimations();
        updateSpawnAnimation();
        updateDieAnimation();
        if (spawning || dying) {
            return;
        }

        handleHammerInput();

        if (moveCooldown > 0) {
            moveCooldown--;
        }
        if (invincibleFrames > 0) {
            invincibleFrames--;
        }
        if (moveCooldown > 0 || !(keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed)) {
            return;
        }

        int targetRow = getRow();
        int targetCol = getCol();

        if (keyH.upPressed) {
            direction = "up";
            targetRow--;
        } else if (keyH.downPressed) {
            direction = "down";
            targetRow++;
        } else if (keyH.leftPressed) {
            direction = "left";
            targetCol--;
        } else if (keyH.rightPressed) {
            direction = "right";
            targetCol++;
        }

        if (tryMoveTo(targetRow, targetCol)) {
            moveCooldown = MOVE_COOLDOWN_FRAMES;
            walkFrame++;
        }
    }

    private boolean tryMoveTo(int targetRow, int targetCol) {
        if (gp.mapLoader.isWall(targetRow, targetCol)) {
            return false;
        }

        GameObject object = gp.getObjectAt(targetRow, targetCol);
        boolean pushedRock = false;
        boolean enteredDoor = false;
        if (object instanceof Rock) {
            if (!pushRock((Rock) object, targetRow, targetCol)) {
                return false;
            }
            pushedRock = true;
        } else if (object instanceof Door) {
            gp.levelComplete = true;
            enteredDoor = true;
        } else if (object instanceof Diamond) {
            object.setActive(false);
            score++;
            gp.playSfx(SoundManager.SFX_EAT_DIAMOND);
        } else if (object instanceof Hammer) {
            object.setActive(false);
            gp.acquireHammer();
            gp.playSfx(SoundManager.SFX_EAT_DIAMOND);
        } else if (object instanceof Key) {
            object.setActive(false);
            gp.acquireKey();
            gp.playSfx(SoundManager.SFX_EAT_DIAMOND);
        } else if (object != null && object.collision) {
            return false;
        }

        x = targetCol * gp.tileSize;
        y = targetRow * gp.tileSize;
        gp.mapLoader.clearBushAt(targetRow, targetCol);
        if (pushedRock) {
            gp.playSfx(SoundManager.SFX_PUSH_ROCK);
        } else if (!enteredDoor) {
            gp.playSfx(SoundManager.SFX_WALK_DIRT);
        }
        return true;
    }

    private boolean pushRock(Rock rock, int rockRow, int rockCol) {
        int deltaRow = 0;
        int deltaCol = 0;
        if ("left".equals(direction)) {
            deltaCol = -1;
        } else if ("right".equals(direction)) {
            deltaCol = 1;
        } else if ("up".equals(direction)) {
            deltaRow = -1;
        } else if ("down".equals(direction)) {
            deltaRow = 1;
        } else {
            return false;
        }

        int newRow = rockRow + deltaRow;
        int newCol = rockCol + deltaCol;
        int targetTile = gp.mapLoader.getTileAt(newRow, newCol);
        boolean canPushIntoTile = targetTile == MapLoader.DIRT
                || targetTile == MapLoader.BUSH
                || targetTile == MapLoader.SPAWN;
        if (!canPushIntoTile || gp.isObjectBlockingMovable(newRow, newCol)) {
            return false;
        }

        Enemy enemy = gp.getEnemyAt(newRow, newCol);
        if (enemy != null) {
            if (deltaRow != 1) {
                return false;
            }
            enemy.crushByRock();
        }

        rock.setGridPosition(gp, newRow, newCol);
        gp.mapLoader.clearBushAt(newRow, newCol);
        return true;
    }

    private void handleHammerInput() {
        boolean hammerKeyDown = keyH.fPressed;
        if (hammerKeyDown && !hammerKeyWasDown && gp.hasHammer()) {
            useHammer();
        }
        hammerKeyWasDown = hammerKeyDown;
    }

    private void useHammer() {
        useHammerAt(getRow(), getCol() - 1);
        useHammerAt(getRow(), getCol() + 1);
        useHammerAt(getRow() - 1, getCol());
        useHammerAt(getRow() + 1, getCol());
    }

    private void useHammerAt(int targetRow, int targetCol) {
        Enemy enemy = gp.getEnemyAt(targetRow, targetCol);
        if (enemy != null) {
            startHammerAnimation(targetRow, targetCol);
            enemy.stun();
            return;
        }

        GameObject object = gp.getObjectAt(targetRow, targetCol);
        if (object instanceof Rock || object instanceof Diamond || object instanceof Door) {
            startHammerAnimation(targetRow, targetCol);
            return;
        }
        if (object != null) {
            startHammerAnimation(targetRow, targetCol);
            return;
        }

        if (gp.mapLoader.getTileAt(targetRow, targetCol) == MapLoader.BUSH) {
            startHammerAnimation(targetRow, targetCol);
            gp.mapLoader.clearBushAt(targetRow, targetCol);
            return;
        }

        if (gp.mapLoader.clearPlasticClusterAt(targetRow, targetCol)) {
            startHammerAnimation(targetRow, targetCol);
        }
    }

    private void startHammerAnimation(int targetRow, int targetCol) {
        hammerAnimations.add(new HammerAnimation(getRow(), getCol(), targetRow, targetCol));
    }

    private void updateHammerAnimations() {
        Iterator<HammerAnimation> iterator = hammerAnimations.iterator();
        while (iterator.hasNext()) {
            HammerAnimation animation = iterator.next();
            animation.timer++;
            if (animation.timer >= HAMMER_FRAME_INTERVAL) {
                animation.timer = 0;
                animation.frame++;
                if (animation.frame >= AssetManager.playerHammerFrames.length) {
                    iterator.remove();
                }
            }
        }
    }

    public void loseLife() {
        if (dying || spawning) {
            return;
        }
        if (invincibleFrames > 0) {
            return;
        }

        lives--;
        if (lives <= 0) {
            gameOverAfterDeath = true;
            gp.stopBgm();
            gp.playSfx(SoundManager.SFX_DIE);
            startDieAnimation();
            return;
        }

        gp.playSfx(SoundManager.SFX_LOSE_LIFE);
        invincibleFrames = INVINCIBLE_FRAMES_AFTER_HIT;
    }

    private void startSpawnAnimation() {
        spawning = true;
        spawnFrame = 0;
        spawnTimer = 0;
        dying = false;
        gameOverAfterDeath = false;
        dieFrame = 0;
        dieTimer = 0;
    }

    private void updateSpawnAnimation() {
        if (!spawning) {
            return;
        }

        spawnTimer++;
        if (spawnTimer < SPAWN_FRAME_INTERVAL) {
            return;
        }
        spawnTimer = 0;
        spawnFrame++;
        if (spawnFrame >= AssetManager.playerSpawnFrames.length) {
            spawning = false;
            spawnFrame = AssetManager.playerSpawnFrames.length - 1;
        }
    }

    private void startDieAnimation() {
        dying = true;
        dieFrame = 0;
        dieTimer = 0;
        spawning = false;
        hammerAnimations.clear();
    }

    private void updateDieAnimation() {
        if (!dying) {
            return;
        }

        dieTimer++;
        if (dieTimer < DIE_FRAME_INTERVAL) {
            return;
        }
        dieTimer = 0;
        dieFrame++;
        if (dieFrame >= AssetManager.playerDieFrames.length) {
            dying = false;
            dieFrame = AssetManager.playerDieFrames.length - 1;
            if (gameOverAfterDeath) {
                gp.gameOver = true;
                gameOverAfterDeath = false;
                return;
            }
            invincibleFrames = lives > 0 ? INVINCIBLE_FRAMES_AFTER_HIT : 0;
        }
    }

    public void draw(Graphics2D g2) {
        if (invincibleFrames > 0 && (invincibleFrames / 6) % 2 == 0) {
            return;
        }
        if (dying) {
            drawZoomedDeath(g2);
            return;
        }
        if (spawning) {
            drawSpawnAnimation(g2);
            return;
        }
        drawCharacter(g2);
        drawHammerAnimations(g2);
    }

    private void drawCharacter(Graphics2D g2) {
        boolean horizontal = "left".equals(direction) || "right".equals(direction);
        boolean flip = "left".equals(direction);
        BufferedImage head = horizontal ? AssetManager.playerWalkRightHead : AssetManager.playerWalkUpHead;
        BufferedImage body = horizontal
                ? AssetManager.playerWalkRightBodies[walkFrame % AssetManager.playerWalkRightBodies.length]
                : getVerticalBody();

        int headHeight = gp.tileSize / 2;
        int bodyHeight = gp.tileSize - headHeight;
        drawMaybeFlipped(g2, head, x, y, gp.tileSize, headHeight, flip);
        drawMaybeFlipped(g2, body, x, y + headHeight, gp.tileSize, bodyHeight, flip);
    }

    private BufferedImage getVerticalBody() {
        int frame = walkFrame % AssetManager.playerWalkUpBodies.length;
        if ("down".equals(direction)) {
            frame = AssetManager.playerWalkUpBodies.length - 1 - frame;
        }
        return AssetManager.playerWalkUpBodies[frame];
    }

    private void drawMaybeFlipped(Graphics2D g2, BufferedImage image, int drawX, int drawY,
                                  int drawWidth, int drawHeight, boolean flip) {
        if (flip) {
            g2.drawImage(image, drawX + drawWidth, drawY, -drawWidth, drawHeight, null);
        } else {
            g2.drawImage(image, drawX, drawY, drawWidth, drawHeight, null);
        }
    }

    private void drawHammerAnimations(Graphics2D g2) {
        for (HammerAnimation animation : hammerAnimations) {
            if (animation.frame >= AssetManager.playerHammerFrames.length) {
                continue;
            }

            double progress = animation.frame / (double) (AssetManager.playerHammerFrames.length - 1);
            int startX = animation.startCol * gp.tileSize;
            int startY = animation.startRow * gp.tileSize;
            int targetX = animation.targetCol * gp.tileSize;
            int targetY = animation.targetRow * gp.tileSize;
            int drawX = (int) Math.round(startX + (targetX - startX) * progress);
            int drawY = (int) Math.round(startY + (targetY - startY) * progress);
            g2.drawImage(AssetManager.playerHammerFrames[animation.frame],
                    drawX, drawY, gp.tileSize, gp.tileSize, null);
        }
    }

    private void drawSpawnAnimation(Graphics2D g2) {
        BufferedImage image = AssetManager.playerSpawnFrames[spawnFrame];
        double progress = spawnFrame / (double) Math.max(1, AssetManager.playerSpawnFrames.length - 1);
        int drawY = (int) Math.round(y - gp.tileSize + gp.tileSize * progress);
        drawBottomAnchored(g2, image, x, drawY);
    }

    private void drawBottomAnchored(Graphics2D g2, BufferedImage image, int drawX, int tileY) {
        int drawWidth = gp.tileSize;
        int drawHeight = Math.max(1, image.getHeight() * drawWidth / image.getWidth());
        int drawY = tileY + gp.tileSize - drawHeight;
        g2.drawImage(image, drawX, drawY, drawWidth, drawHeight, null);
    }

    private void drawZoomedDeath(Graphics2D g2) {
        BufferedImage image = AssetManager.playerDieFrames[dieFrame];
        int zoomedTile = gp.tileSize * DIE_ZOOM_SCALE;
        int drawX = x + gp.tileSize / 2 - zoomedTile / 2;
        int drawHeight = Math.max(1, image.getHeight() * zoomedTile / image.getWidth());
        int drawY = y + gp.tileSize - drawHeight;
        g2.drawImage(image, drawX, drawY, zoomedTile, drawHeight, null);
    }

    private static final class HammerAnimation {
        private final int startRow;
        private final int startCol;
        private final int targetRow;
        private final int targetCol;
        private int frame;
        private int timer;

        private HammerAnimation(int startRow, int startCol, int targetRow, int targetCol) {
            this.startRow = startRow;
            this.startCol = startCol;
            this.targetRow = targetRow;
            this.targetCol = targetCol;
        }
    }
}
