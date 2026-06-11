package main.object;

import java.awt.Color;
import java.awt.Graphics2D;

import main.core.GamePanel;
import main.util.AssetManager;

public class Diamond extends GameObject {

    public Diamond(int worldX, int worldY) {
        this.name = "Diamond";
        this.worldX = worldX;
        this.worldY = worldY;
        this.collision = false;
    }

    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        if (!active) {
            return;
        }
        drawGlow(g2, gp);
        g2.drawImage(AssetManager.diamond, worldX, worldY, gp.tileSize, gp.tileSize, null);
    }

    protected void drawGlow(Graphics2D g2, GamePanel gp) {
        int pulse = (int) ((System.nanoTime() / 42_000_000L) % 28);
        int glow = pulse <= 14 ? pulse : 28 - pulse;
        int glowSize = gp.tileSize + 12 + glow;
        int glowX = worldX + gp.tileSize / 2 - glowSize / 2;
        int glowY = worldY + gp.tileSize / 2 - glowSize / 2;

        g2.setColor(new Color(90, 230, 255, 44));
        g2.fillOval(glowX, glowY, glowSize, glowSize);
        g2.setColor(new Color(225, 255, 255, 105));
        g2.drawOval(glowX + 4, glowY + 4, glowSize - 8, glowSize - 8);
    }
}
