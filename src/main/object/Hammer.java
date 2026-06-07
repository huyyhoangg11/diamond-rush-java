package main.object;

import java.awt.Graphics2D;

import main.core.GamePanel;
import main.util.AssetManager;

public class Hammer extends GameObject {

    public Hammer(int worldX, int worldY) {
        this.name = "Hammer";
        this.worldX = worldX;
        this.worldY = worldY;
        this.collision = false;
    }

    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        if (!active) {
            return;
        }
        g2.drawImage(AssetManager.hammer, worldX, worldY, gp.tileSize, gp.tileSize, null);
    }
}
