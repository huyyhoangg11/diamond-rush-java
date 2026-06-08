package main.ui;

import main.input.KeyHandler;
import main.util.AssetManager;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

public class GameOverScreen implements Screen {

    private final GameStateManager gsm;
    private boolean keyWasDown;
    private int fadeAlpha;

    public GameOverScreen(GameStateManager gsm) {
        this.gsm = gsm;
    }

    @Override
    public void update(KeyHandler key) {
        if (fadeAlpha < 255) {
            fadeAlpha = Math.min(fadeAlpha + 4, 255);
        }

        boolean keyDown = key.enterPressed;
        if (keyDown && !keyWasDown && fadeAlpha > 150) {
            fadeAlpha = 0;
            gsm.setState(GameState.MENU);
        }
        keyWasDown = keyDown;
    }

    @Override
    public void draw(Graphics2D g2) {
        int w = gsm.getGamePanel().getWidth();
        int h = gsm.getGamePanel().getHeight();
        int a = fadeAlpha;

        g2.drawImage(AssetManager.uiMenu0, 0, 0, w, h, null);
        g2.drawImage(AssetManager.uiMenu1, 0, 0, w, h, null);
        g2.setColor(new Color(25, 4, 2, Math.min(a, 170)));
        g2.fillRect(0, 0, w, h);

        if (a < 45) {
            return;
        }

        int panelW = 650;
        int panelH = 340;
        int x = (w - panelW) / 2;
        int y = (h - panelH) / 2 + 20;

        g2.setColor(new Color(20, 9, 6, Math.min(a, 220)));
        g2.fillRoundRect(x + 8, y + 10, panelW, panelH, 28, 28);
        GradientPaint panelPaint = new GradientPaint(x, y,
                new Color(82, 33, 20, Math.min(a, 240)),
                x, y + panelH,
                new Color(23, 13, 10, Math.min(a, 240)));
        g2.setPaint(panelPaint);
        g2.fillRoundRect(x, y, panelW, panelH, 28, 28);

        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(210, 73, 45, a));
        g2.drawRoundRect(x, y, panelW, panelH, 28, 28);
        g2.setColor(new Color(255, 196, 104, Math.min(a, 105)));
        g2.drawRoundRect(x + 10, y + 10, panelW - 20, panelH - 20, 22, 22);

        g2.setFont(new Font("Georgia", Font.BOLD, 62));
        String title = "GAME OVER";
        drawOutlined(g2, title, getCenterX(g2, title, w), y + 92,
                new Color(255, 86, 64, a), new Color(45, 9, 4, a));

        g2.drawImage(AssetManager.uiPlayer, x + panelW / 2 - 38, y + 118, 76, 76, null);

        g2.setFont(new Font("Georgia", Font.BOLD, 24));
        String stat = "Diamonds collected: " + gsm.getGamePanel().player.score;
        drawOutlined(g2, stat, getCenterX(g2, stat, w), y + 232,
                new Color(255, 232, 170, a), new Color(48, 24, 8, a));

        if (a > 150) {
            g2.setFont(new Font("Georgia", Font.BOLD, 18));
            String hint = "Enter: Main Menu     R: Reset Checkpoint";
            drawOutlined(g2, hint, getCenterX(g2, hint, w), y + 292,
                    new Color(255, 205, 120, a), new Color(44, 20, 8, a));
        }
    }

    private void drawOutlined(Graphics2D g2, String text, int x, int y, Color fill, Color outline) {
        g2.setColor(outline);
        g2.drawString(text, x - 2, y);
        g2.drawString(text, x + 2, y);
        g2.drawString(text, x, y - 2);
        g2.drawString(text, x, y + 2);
        g2.drawString(text, x + 3, y + 3);
        g2.setColor(fill);
        g2.drawString(text, x, y);
    }

    private int getCenterX(Graphics2D g2, String text, int w) {
        return (w - g2.getFontMetrics().stringWidth(text)) / 2;
    }
}
