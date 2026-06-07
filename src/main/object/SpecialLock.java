package main.object;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import main.core.GamePanel;
import main.util.AssetManager;

public class SpecialLock extends LockBolt {

    public SpecialLock(int worldX, int worldY) {
        super(worldX, worldY);
        this.name = "SpecialLock";
    }

    public void updateWithKey(boolean shouldOpen) {
        update(shouldOpen || getFrame() > 0 || !collision);
    }

    @Override
    protected BufferedImage getImage() {
        if (!collision) {
            return AssetManager.lock2;
        }
        if (getFrame() == 1) {
            return AssetManager.lock1;
        }
        return AssetManager.lockPre;
    }

    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        if (!isActive() || !collision) {
            return;
        }
        g2.drawImage(getImage(), worldX, worldY, gp.tileSize, gp.tileSize, null);
    }
}
