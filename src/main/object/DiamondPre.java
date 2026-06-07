package main.object;

import java.awt.Graphics2D;

import main.core.GamePanel;
import main.util.AssetManager;

public class DiamondPre extends Diamond {

    public DiamondPre(int worldX, int worldY) {
        super(worldX, worldY);
        this.name = "DiamondPre";
    }

    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        if (!active) {
            return;
        }
        g2.drawImage(AssetManager.diamondPre, worldX, worldY, gp.tileSize, gp.tileSize, null);
    }
}
