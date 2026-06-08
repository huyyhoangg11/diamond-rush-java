package main.ui;

import main.input.KeyHandler;
import main.util.AssetManager;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

public class WinScreen implements Screen {

    private static final int SPARK_COUNT = 28;

    private final GameStateManager gsm;
    private final int[] sx = new int[SPARK_COUNT];
    private final int[] sy = new int[SPARK_COUNT];
    private final int[] sp = new int[SPARK_COUNT];
    private boolean keyWasDown;
    private int fadeAlpha;

    public WinScreen(GameStateManager gsm) {
        this.gsm = gsm;
        for (int i = 0; i < SPARK_COUNT; i++) {
            sx[i] = (int) (Math.random() * 1200);
            sy[i] = (int) (Math.random() * 960);
            sp[i] = 1 + (int) (Math.random() * 3);
        }
    }

    @Override
    public void update(KeyHandler key) {
        if (fadeAlpha < 255) {
            fadeAlpha = Math.min(fadeAlpha + 4, 255);
        }

        int h = gsm.getGamePanel().getHeight();
        int w = gsm.getGamePanel().getWidth();
        for (int i = 0; i < SPARK_COUNT; i++) {
            sy[i] += sp[i];
            if (sy[i] > h + 20) {
                sy[i] = -20;
                sx[i] = (int) (Math.random() * w);
            }
        }

        boolean keyDown = key.enterPressed;
        if (keyDown && !keyWasDown && fadeAlpha > 150) {
            fadeAlpha = 0;
            gsm.setState(GameState.WORLD_MAP);
        }
        keyWasDown = keyDown;
    }

    @Override
    public void draw(Graphics2D g2) {
        int w = gsm.getGamePanel().getWidth();
        int h = gsm.getGamePanel().getHeight();
        int a = fadeAlpha;
        boolean finalLevel = gsm.getGamePanel().isFinalLevel();

        drawBackground(g2, w, h, a);
        if (a < 45) {
            return;
        }

        drawSparks(g2, a);
        drawPanel(g2, w, h, a, finalLevel);
    }

    private void drawBackground(Graphics2D g2, int w, int h, int a) {
        g2.drawImage(AssetManager.uiMenu0, 0, 0, w, h, null);
        g2.drawImage(AssetManager.uiMenu1, 0, 0, w, h, null);
        g2.setColor(new Color(255, 236, 160, Math.min(95, a / 2)));
        g2.fillRect(0, 0, w, h);
    }

    private void drawSparks(Graphics2D g2, int a) {
        g2.setFont(new Font("Dialog", Font.BOLD, 20));
        g2.setColor(new Color(105, 235, 255, Math.min(a, 170)));
        for (int i = 0; i < SPARK_COUNT; i++) {
            g2.drawString("◆", sx[i], sy[i]);
        }
    }

    private void drawPanel(Graphics2D g2, int w, int h, int a, boolean finalLevel) {
        int panelW = 680;
        int panelH = finalLevel ? 390 : 340;
        int x = (w - panelW) / 2;
        int y = (h - panelH) / 2 + 18;

        g2.setColor(new Color(28, 16, 7, Math.min(a, 210)));
        g2.fillRoundRect(x + 8, y + 10, panelW, panelH, 28, 28);
        GradientPaint paint = new GradientPaint(x, y,
                new Color(103, 66, 28, Math.min(a, 238)),
                x, y + panelH,
                new Color(38, 23, 11, Math.min(a, 238)));
        g2.setPaint(paint);
        g2.fillRoundRect(x, y, panelW, panelH, 28, 28);
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(255, 214, 102, a));
        g2.drawRoundRect(x, y, panelW, panelH, 28, 28);
        g2.setColor(new Color(255, 248, 210, Math.min(a, 120)));
        g2.drawRoundRect(x + 10, y + 10, panelW - 20, panelH - 20, 22, 22);

        String title = finalLevel ? "ESCAPED!" : "LEVEL CLEAR";
        g2.setFont(new Font("Georgia", Font.BOLD, finalLevel ? 58 : 54));
        drawOutlined(g2, title, getCenterX(g2, title, w), y + 86,
                new Color(255, 232, 120, a), new Color(58, 30, 8, a));

        g2.drawImage(finalLevel ? AssetManager.diamondPre : AssetManager.uiDiamond,
                x + panelW / 2 - 42, y + 112, 84, 84, null);

        g2.setFont(new Font("Georgia", Font.BOLD, 25));
        if (finalLevel) {
            String success = "Thoát khỏi hầm mỏ thành công";
            drawOutlined(g2, success, getCenterX(g2, success, w), y + 230,
                    new Color(255, 248, 220, a), new Color(55, 31, 10, a));
            String total = "Tổng kim cương thu được: " + gsm.getGamePanel().getJourneyDiamondCount();
            drawOutlined(g2, total, getCenterX(g2, total, w), y + 272,
                    new Color(110, 235, 255, a), new Color(20, 36, 46, a));
        } else {
            String stat = "Diamonds collected: " + gsm.getGamePanel().player.score;
            drawOutlined(g2, stat, getCenterX(g2, stat, w), y + 232,
                    new Color(255, 248, 220, a), new Color(55, 31, 10, a));
        }

        if (a > 150) {
            g2.setFont(new Font("Georgia", Font.BOLD, 19));
            String hint = "Press Enter to return to World Map";
            drawOutlined(g2, hint, getCenterX(g2, hint, w), y + panelH - 42,
                    new Color(255, 220, 130, a), new Color(48, 25, 8, a));
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
