package main.object;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import main.core.GamePanel;
import main.util.AssetManager;

public class Statue extends GameObject {

    private static final int FIRE_RANGE = 3;
    private static final int WAIT_FRAMES = 75;
    private static final int FIRE_STEP_FRAMES = 15;

    private int fireLength;
    private int fireTimer;

    public Statue(int worldX, int worldY) {
        this.name = "Statue";
        this.worldX = worldX;
        this.worldY = worldY;
        this.collision = true;
    }

    public void update() {
        if (!active) {
            return;
        }

        fireTimer++;
        if (fireLength == 0) {
            if (fireTimer >= WAIT_FRAMES) {
                fireLength = 1;
                fireTimer = 0;
            }
            return;
        }

        if (fireTimer >= FIRE_STEP_FRAMES) {
            fireLength++;
            fireTimer = 0;
            if (fireLength > FIRE_RANGE) {
                fireLength = 0;
            }
        }
    }

    public boolean hasFireAt(GamePanel gp, int row, int col) {
        if (!active || fireLength == 0 || row != getRow(gp)) {
            return false;
        }

        int offset = col - getCol(gp);
        return offset >= 1 && offset <= fireLength;
    }

    public int getFireLength() {
        return fireLength;
    }

    public int getFireTimer() {
        return fireTimer;
    }

    public void restoreState(int fireLength, int fireTimer) {
        this.fireLength = fireLength;
        this.fireTimer = fireTimer;
    }

    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        if (!active) {
            return;
        }

        g2.drawImage(AssetManager.statue, worldX, worldY, gp.tileSize, gp.tileSize, null);
        for (int i = 1; i <= fireLength; i++) {
            g2.drawImage(getFireImage(i), worldX + i * gp.tileSize, worldY, gp.tileSize, gp.tileSize, null);
        }
    }

    private BufferedImage getFireImage(int offset) {
        return switch (offset) {
            case 1 -> AssetManager.fire1;
            case 2 -> AssetManager.fire2;
            default -> AssetManager.fire3;
        };
    }
}
