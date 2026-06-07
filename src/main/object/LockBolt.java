package main.object;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import main.core.GamePanel;
import main.util.AssetManager;

public class LockBolt extends GameObject {

    private static final int RETRACTED_FRAME = 2;
    private static final int ANIMATION_INTERVAL_FRAMES = 8;

    private int frame;
    private int animationCounter;

    public LockBolt(int worldX, int worldY) {
        this.name = "LockBolt";
        this.worldX = worldX;
        this.worldY = worldY;
        this.collision = true;
    }

    public void update(boolean shouldRetract) {
        animationCounter++;
        if (animationCounter < ANIMATION_INTERVAL_FRAMES) {
            updateCollision();
            return;
        }
        animationCounter = 0;

        if (shouldRetract && frame < RETRACTED_FRAME) {
            frame++;
        } else if (!shouldRetract && frame > 0) {
            frame--;
        }
        updateCollision();
    }

    private void updateCollision() {
        collision = frame != RETRACTED_FRAME;
    }

    public int getFrame() {
        return frame;
    }

    public int getAnimationCounter() {
        return animationCounter;
    }

    public void restoreState(int frame, int animationCounter) {
        this.frame = frame;
        this.animationCounter = animationCounter;
        updateCollision();
    }

    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        if (!active || frame == RETRACTED_FRAME) {
            return;
        }
        g2.drawImage(getImage(), worldX, worldY, gp.tileSize, gp.tileSize, null);
    }

    protected BufferedImage getImage() {
        return switch (frame) {
            case 1 -> AssetManager.lock1;
            case 2 -> AssetManager.lock2;
            default -> AssetManager.lock;
        };
    }
}
