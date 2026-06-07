package main.ui;

import main.input.KeyHandler;
import main.util.AssetManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class HowToPlayScreen implements Screen {

    private static final int PAGE_CONTROLS = 0;
    private static final int PAGE_OBJECTS = 1;

    private final GameStateManager gsm;
    private boolean keyWasDown = false;
    private int page = PAGE_CONTROLS;

    public HowToPlayScreen(GameStateManager gsm) {
        this.gsm = gsm;
    }

    @Override
    public void update(KeyHandler key) {
        boolean pageKey = key.enterPressed || key.leftPressed || key.rightPressed || key.upPressed || key.downPressed;
        boolean anyKey = key.escPressed || pageKey;
        if (anyKey && !keyWasDown) {
            if (key.escPressed) {
                gsm.setState(GameState.MENU);
            } else if (pageKey) {
                page = page == PAGE_CONTROLS ? PAGE_OBJECTS : PAGE_CONTROLS;
            }
        }
        keyWasDown = anyKey;
    }

    @Override
    public void draw(Graphics2D g2) {
        int w = gsm.getGamePanel().getWidth();
        int h = gsm.getGamePanel().getHeight();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        drawBackground(g2, w, h);
        drawFrame(g2, w, h);

        if (page == PAGE_CONTROLS) {
            drawControlsPage(g2, w, h);
        } else {
            drawObjectsPage(g2, w, h);
        }

        drawFooter(g2, w, h);
    }

    private void drawBackground(Graphics2D g2, int w, int h) {
        g2.drawImage(AssetManager.uiMenu0, 0, 0, w, h, null);
        g2.drawImage(AssetManager.uiMenu1, 0, 0, w, h, null);
        g2.setColor(new Color(255, 238, 180, 28));
        g2.fillRect(0, 0, w, h);
    }

    private void drawFrame(Graphics2D g2, int w, int h) {
        int panelX = 126;
        int panelY = 96;
        int panelW = w - 252;
        int panelH = h - 178;
        g2.setColor(new Color(28, 18, 10, 188));
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 28, 28);
        g2.setColor(new Color(255, 214, 102, 185));
        g2.drawRoundRect(panelX, panelY, panelW, panelH, 28, 28);
        g2.setColor(new Color(255, 245, 195, 62));
        g2.drawRoundRect(panelX + 8, panelY + 8, panelW - 16, panelH - 16, 22, 22);
    }

    private void drawControlsPage(Graphics2D g2, int w, int h) {
        drawTitle(g2, "HOW TO PLAY", "PAGE 1 / 2 - CONTROLS", w, 166);

        int leftX = 250;
        int rightX = 650;
        int startY = 270;
        int rowH = 74;

        drawControlItem(g2, leftX, startY, "W / UP", "Move up");
        drawControlItem(g2, leftX, startY + rowH, "S / DOWN", "Move down");
        drawControlItem(g2, leftX, startY + rowH * 2, "A / LEFT", "Move left");
        drawControlItem(g2, leftX, startY + rowH * 3, "D / RIGHT", "Move right");
        drawControlItem(g2, leftX, startY + rowH * 4, "ENTER", "Confirm / switch help page");

        drawControlItem(g2, rightX, startY, "P", "Pause / resume game");
        drawControlItem(g2, rightX, startY + rowH, "ESC", "Pause or go back from screens");
        drawControlItem(g2, rightX, startY + rowH * 2, "Q", "Save and return to menu");
        drawControlItem(g2, rightX, startY + rowH * 3, "R", "Reset to latest spawn checkpoint");
        drawControlItem(g2, rightX, startY + rowH * 4, "F", "Use hammer on adjacent tiles");
    }

    private void drawObjectsPage(Graphics2D g2, int w, int h) {
        drawTitle(g2, "HOW TO PLAY", "PAGE 2 / 2 - OBJECTS", w, 166);

        int leftX = 190;
        int rightX = 628;
        int startY = 270;
        int rowH = 74;

        drawObjectItem(g2, AssetManager.diamond, leftX, startY, "Diamond", "Collect all to open door");
        drawObjectItem(g2, AssetManager.rock, leftX, startY + rowH, "Rock", "Push it; avoid falling rocks");
        drawObjectItem(g2, AssetManager.snake1, leftX, startY + rowH * 2, "Snake", "Avoid it or stun with hammer");
        drawObjectItem(g2, AssetManager.hammer, leftX, startY + rowH * 3, "Hammer", "Break bushes/plastic, stun enemies");
        drawObjectItem(g2, AssetManager.statue, leftX, startY + rowH * 4, "Statue", "Shoots fire 3 tiles right");

        drawObjectItem(g2, AssetManager.door, rightX, startY, "Door", "Enter after collecting diamonds");
        drawObjectItem(g2, AssetManager.plastic, rightX, startY + rowH, "Plastic", "Hammer shatters connected walls");
        drawObjectItem(g2, AssetManager.knob, rightX, startY + rowH * 2, "Knob", "Hold it with player or rock");
        drawObjectItem(g2, AssetManager.lockPre, rightX, startY + rowH * 3, "Special lock", "Needs key to open");
        drawObjectItem(g2, AssetManager.key, rightX, startY + rowH * 4, "Key", "Permanent special-lock item");
    }

    private void drawTitle(Graphics2D g2, String title, String subtitle, int w, int y) {
        g2.setFont(new Font("Serif", Font.BOLD, 48));
        drawOutlinedText(g2, title, getCenterX(g2, title, w), y, new Color(255, 218, 104), new Color(55, 29, 8));

        g2.setFont(new Font("Serif", Font.BOLD, 22));
        drawOutlinedText(g2, subtitle, getCenterX(g2, subtitle, w), y + 42,
                new Color(255, 245, 205), new Color(55, 29, 8));
    }

    private void drawControlItem(Graphics2D g2, int x, int y, String keyName, String text) {
        g2.setColor(new Color(255, 255, 255, 24));
        g2.fillRoundRect(x - 18, y - 42, 340, 58, 14, 14);

        g2.setFont(new Font("Serif", Font.BOLD, 24));
        drawOutlinedText(g2, keyName, x, y - 6, new Color(255, 222, 116), new Color(48, 26, 8));

        g2.setFont(new Font("Serif", Font.PLAIN, 19));
        drawOutlinedText(g2, text, x + 126, y - 6, new Color(245, 237, 210), new Color(42, 24, 10));
    }

    private void drawObjectItem(Graphics2D g2, BufferedImage image, int x, int y, String name, String text) {
        int iconSize = 48;
        g2.setColor(new Color(255, 255, 255, 24));
        g2.fillRoundRect(x - 14, y - 42, 390, 58, 14, 14);
        g2.drawImage(image, x, y - 40, iconSize, iconSize, null);

        g2.setFont(new Font("Serif", Font.BOLD, 21));
        drawOutlinedText(g2, name, x + 62, y - 19, new Color(255, 222, 116), new Color(48, 26, 8));

        g2.setFont(new Font("Serif", Font.PLAIN, 17));
        drawOutlinedText(g2, text, x + 62, y + 5, new Color(245, 237, 210), new Color(42, 24, 10));
    }

    private void drawFooter(Graphics2D g2, int w, int h) {
        g2.setFont(new Font("Serif", Font.BOLD, 18));
        String nav = "Enter / Arrow Keys: switch page      ESC: return to menu";
        drawOutlinedText(g2, nav, getCenterX(g2, nav, w), h - 42,
                new Color(255, 245, 205), new Color(45, 25, 10));
    }

    private void drawOutlinedText(Graphics2D g2, String text, int x, int y, Color fill, Color outline) {
        g2.setColor(outline);
        g2.drawString(text, x - 2, y);
        g2.drawString(text, x + 2, y);
        g2.drawString(text, x, y - 2);
        g2.drawString(text, x, y + 2);
        g2.setColor(fill);
        g2.drawString(text, x, y);
    }

    private int getCenterX(Graphics2D g2, String text, int w) {
        return (w - g2.getFontMetrics().stringWidth(text)) / 2;
    }
}
