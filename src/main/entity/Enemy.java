package main.entity;

import java.awt.Graphics2D;

import main.config.GameConfig;
import main.core.GamePanel;
import main.util.AssetManager;

public class Enemy extends Entity {

    private static final int MOVE_INTERVAL_FRAMES = 20;
    private static final int STUN_DURATION_FRAMES = 5 * GameConfig.FPS;
    private static final int DEATH_ANIMATION_FRAMES = 45;

    private final GamePanel gp;
    private final boolean horizontalMovement;
    private final int snakeVariant;
    private int moveDirection = 1;
    private int moveCounter;
    private int stunFrames;
    private int deathFrames;
    private boolean active = true;

    public Enemy(GamePanel gp, int x, int y, boolean horizontalMovement) {
        this(gp, x, y, horizontalMovement, 0);
    }

    public Enemy(GamePanel gp, int x, int y, boolean horizontalMovement, int snakeVariant) {
        this.gp = gp;
        this.x = x;
        this.y = y;
        this.horizontalMovement = horizontalMovement;
        this.snakeVariant = snakeVariant;
    }

    public int getRow() {
        return y / gp.tileSize;
    }

    public int getCol() {
        return x / gp.tileSize;
    }

    public int getMoveDirection() {
        return moveDirection;
    }

    public int getMoveCounter() {
        return moveCounter;
    }

    public int getStunFrames() {
        return stunFrames;
    }

    public int getDeathFrames() {
        return deathFrames;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isDying() {
        return deathFrames > 0;
    }

    public boolean isHorizontalMovement() {
        return horizontalMovement;
    }

    public int getSnakeVariant() {
        return snakeVariant;
    }

    public void restoreState(int x, int y, int moveDirection, int moveCounter) {
        restoreState(x, y, moveDirection, moveCounter, 0);
    }

    public void restoreState(int x, int y, int moveDirection, int moveCounter, int stunFrames) {
        restoreState(x, y, moveDirection, moveCounter, stunFrames, 0);
    }

    public void restoreState(int x, int y, int moveDirection, int moveCounter, int stunFrames, int deathFrames) {
        this.x = x;
        this.y = y;
        this.moveDirection = moveDirection;
        this.moveCounter = moveCounter;
        this.stunFrames = stunFrames;
        this.deathFrames = deathFrames;
        this.active = true;
    }

    public void stun() {
        if (isDying()) {
            return;
        }
        if (stunFrames > 0) {
            crushByRock();
            return;
        }
        stunFrames = STUN_DURATION_FRAMES;
    }

    public void crushByRock() {
        if (isDying()) {
            return;
        }
        stunFrames = 0;
        deathFrames = DEATH_ANIMATION_FRAMES;
    }

    @Override
    public void update() {
        if (!active) {
            return;
        }
        if (deathFrames > 0) {
            deathFrames--;
            if (deathFrames == 0) {
                active = false;
            }
            return;
        }
        if (stunFrames > 0) {
            stunFrames--;
            return;
        }

        moveCounter++;
        if (moveCounter < MOVE_INTERVAL_FRAMES) {
            return;
        }
        moveCounter = 0;

        int nextRow = getRow();
        int nextCol = getCol();
        if (horizontalMovement) {
            nextCol += moveDirection;
        } else {
            nextRow += moveDirection;
        }

        if (gp.isBlockedForEnemy(nextRow, nextCol)) {
            moveDirection *= -1;
            return;
        }

        x = nextCol * gp.tileSize;
        y = nextRow * gp.tileSize;
    }

    @Override
    public void draw(Graphics2D g2) {
        if (!active || (deathFrames > 0 && (deathFrames / 5) % 2 == 0)) {
            return;
        }
        g2.drawImage(snakeVariant == 1 ? AssetManager.snake2 : AssetManager.snake1,
                x, y, gp.tileSize, gp.tileSize, null);
    }
}
