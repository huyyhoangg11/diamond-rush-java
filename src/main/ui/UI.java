package main.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import main.core.GamePanel;
import main.util.AssetManager;

public class UI {
    private final GamePanel gp;
    private final Font font = new Font("Arial", Font.BOLD, 24);

    public UI(GamePanel gp) {
        this.gp = gp;
    }

    public void draw(Graphics2D g2) {
        g2.setFont(font);
        drawHealth(g2);
        drawDiamondCounter(g2);
        drawMessages(g2);
    }

    private void drawHealth(Graphics2D g2) {
        int x = 16;
        int y = gp.getHeight() - 44;
        g2.drawImage(AssetManager.uiPlayer, x, y - 6, 36, 36, null);
        g2.setColor(Color.WHITE);
        g2.drawString("HP", x + 44, y + 20);

        for (int i = 0; i < 3; i++) {
            g2.setColor(i < gp.player.lives ? Color.RED : Color.DARK_GRAY);
            g2.fillOval(x + 84 + (i * 30), y, 24, 24);
            g2.setColor(Color.WHITE);
            g2.drawOval(x + 84 + (i * 30), y, 24, 24);
        }
    }

    private void drawDiamondCounter(Graphics2D g2) {
        String text = gp.player.score + "/" + gp.totalDiamonds;
        int y = gp.getHeight() - 44;
        int textWidth = g2.getFontMetrics().stringWidth(text);
        int x = gp.getWidth() - textWidth - 58;

        g2.drawImage(AssetManager.uiDiamond, x, y - 6, 36, 36, null);
        g2.setColor(Color.WHITE);
        g2.drawString(text, x + 42, y + 20);
    }

    private void drawMessages(Graphics2D g2) {
        if (!gp.gameOver && !gp.levelComplete) {
            return;
        }

        String text = gp.gameOver ? "GAME OVER" : "LEVEL COMPLETE!";
        g2.setFont(new Font("Arial", Font.BOLD, 54));
        int textWidth = g2.getFontMetrics().stringWidth(text);
        int x = (gp.getWidth() - textWidth) / 2;
        int y = gp.getHeight() / 2;

        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(0, y - 70, gp.getWidth(), 110);
        g2.setColor(gp.gameOver ? Color.RED : Color.YELLOW);
        g2.drawString(text, x, y);
    }
}
