package main.entity;

import java.awt.Graphics2D;

import main.core.GamePanel;
import main.util.AssetManager;

public class Enemy extends Entity {

    private static final int MOVE_INTERVAL_FRAMES = 20;

    private final GamePanel gp;
    private final boolean horizontalMovement;
    private int moveDirection = 1;
    private int moveCounter;

    public Enemy(GamePanel gp, int x, int y, boolean horizontalMovement) {
        this.gp = gp;
        this.x = x;
        this.y = y;
        this.horizontalMovement = horizontalMovement;
    }

    public int getRow() {
        return y / gp.tileSize;
    }

    public int getCol() {
        return x / gp.tileSize;
    }

    @Override
    public void update() {
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
        g2.drawImage(AssetManager.snake, x, y, gp.tileSize, gp.tileSize, null);
    }
}
