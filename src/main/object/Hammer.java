package main.object;

import java.awt.Color;
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
        int pulse = (int) ((System.nanoTime() / 45_000_000L) % 24);
        int glow = pulse <= 12 ? pulse : 24 - pulse;
        int glowSize = gp.tileSize + 10 + glow;
        int glowX = worldX + gp.tileSize / 2 - glowSize / 2;
        int glowY = worldY + gp.tileSize / 2 - glowSize / 2;

        g2.setColor(new Color(255, 222, 88, 42));
        g2.fillOval(glowX, glowY, glowSize, glowSize);
        g2.setColor(new Color(255, 248, 180, 92));
        g2.drawOval(glowX + 4, glowY + 4, glowSize - 8, glowSize - 8);
        g2.drawImage(AssetManager.hammer, worldX, worldY, gp.tileSize, gp.tileSize, null);
    }
}
