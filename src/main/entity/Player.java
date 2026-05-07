// Huy Hoàng làm
package main.entity;

import java.awt.Graphics2D;

import main.core.GamePanel;
import main.input.KeyHandler;
import main.map.MapLoader;
import main.object.Diamond;
import main.object.Door;
import main.object.GameObject;
import main.object.Rock;
import main.util.AssetManager;

public class Player extends Entity {

    private static final int MOVE_COOLDOWN_FRAMES = 8;

    GamePanel gp;
    KeyHandler keyH;
    public int score;
    public int lives;
    private int moveCooldown;
    private int invincibleFrames;
    private final int startRow;
    private final int startCol;

    public Player(GamePanel gp, KeyHandler keyH, int startRow, int startCol) {
        this.gp = gp;
        this.keyH = keyH;
        this.startRow = startRow;
        this.startCol = startCol;

        setDefaultValues();
        getPlayerImage();
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
    }

    public int getRow() {
        return y / gp.tileSize;
    }

    public int getCol() {
        return x / gp.tileSize;
    }

    public void getPlayerImage() {
        down1 = AssetManager.playerDown;
        down2 = AssetManager.playerDown;
        up1 = AssetManager.playerDown;
        up2 = AssetManager.playerDown;
        left1 = AssetManager.playerDown;
        left2 = AssetManager.playerDown;
        right1 = AssetManager.playerDown;
        right2 = AssetManager.playerDown;
    }

    public void update() {
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
            spriteCounter++;
            if (spriteCounter > 10) {
                spriteNum = (spriteNum == 1) ? 2 : 1;
                spriteCounter = 0;
            }
        }
    }

    private boolean tryMoveTo(int targetRow, int targetCol) {
        if (gp.mapLoader.isWall(targetRow, targetCol)) {
            return false;
        }

        GameObject object = gp.getObjectAt(targetRow, targetCol);
        if (object instanceof Rock) {
            if (!pushRock((Rock) object, targetRow, targetCol)) {
                return false;
            }
        } else if (object instanceof Door) {
            if (score < gp.totalDiamonds) {
                return false;
            }
            gp.levelComplete = true;
        } else if (object instanceof Diamond) {
            object.setActive(false);
            score++;
        } else if (object != null && object.collision) {
            return false;
        }

        x = targetCol * gp.tileSize;
        y = targetRow * gp.tileSize;
        gp.mapLoader.clearBushAt(targetRow, targetCol);
        return true;
    }

    private boolean pushRock(Rock rock, int rockRow, int rockCol) {
        int deltaCol;
        if ("left".equals(direction)) {
            deltaCol = -1;
        } else if ("right".equals(direction)) {
            deltaCol = 1;
        } else {
            return false;
        }

        int newCol = rockCol + deltaCol;
        if (gp.mapLoader.getTileAt(rockRow, newCol) == MapLoader.WALL || gp.getObjectAt(rockRow, newCol) != null) {
            return false;
        }

        rock.setGridPosition(gp, rockRow, newCol);
        return true;
    }

    public void loseLife() {
        if (invincibleFrames > 0) {
            return;
        }

        lives--;
        if (lives <= 0) {
            gp.gameOver = true;
            return;
        }

        x = startCol * gp.tileSize;
        y = startRow * gp.tileSize;
        invincibleFrames = 60;
    }

    public void draw(Graphics2D g2) {
        g2.drawImage(AssetManager.playerDown, x, y, gp.tileSize, gp.tileSize, null);
    }
}
