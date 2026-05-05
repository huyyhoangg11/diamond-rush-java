package main.object;

import java.awt.Graphics2D;

import main.core.GamePanel;
import main.util.AssetManager;

public class Rock extends GameObject {

    public Rock(int worldX, int worldY) {
        this.name = "Rock";
        this.worldX = worldX;
        this.worldY = worldY;
        this.collision = true;
    }

    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        if (!active) {
            return;
        }
        g2.drawImage(AssetManager.rock, worldX, worldY, gp.tileSize, gp.tileSize, null);
    }
}
