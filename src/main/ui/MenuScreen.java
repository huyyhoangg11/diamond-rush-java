package main.ui;

import main.core.GamePanel;
import main.input.KeyHandler;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

public class MenuScreen implements Screen {

    private final GameStateManager gsm;
    private int selectedOption = 0;
    private boolean keyWasDown = false;
    private int blinkCounter = 0;
    private boolean blinkOn = true;

    public MenuScreen(GameStateManager gsm) {
        this.gsm = gsm;
    }

    @Override
    public void update(KeyHandler key) {
        boolean anyKey = key.upPressed || key.downPressed || key.enterPressed;
        GamePanel gp = gsm.getGamePanel();
        int optionCount = gp.getLevelCount() + 2;

        if (anyKey && !keyWasDown) {
            if (key.upPressed) {
                selectedOption = (selectedOption - 1 + optionCount) % optionCount;
            } else if (key.downPressed) {
                selectedOption = (selectedOption + 1) % optionCount;
            } else if (key.enterPressed) {
                handleSelection(gp);
            }
        }
        keyWasDown = anyKey;

        if (++blinkCounter >= 30) {
            blinkOn = !blinkOn;
            blinkCounter = 0;
        }
    }

    private void handleSelection(GamePanel gp) {
        int levelCount = gp.getLevelCount();
        if (selectedOption < levelCount) {
            gp.selectedLevelIndex = selectedOption;
            gp.startSelectedLevel();
        } else if (selectedOption == levelCount) {
            gsm.setState(GameState.HOW_TO_PLAY);
        } else {
            System.exit(0);
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        GamePanel gp = gsm.getGamePanel();
        int w = gp.getWidth();
        int h = gp.getHeight();

        GradientPaint bg = new GradientPaint(0, 0, new Color(8, 4, 22),
                0, h, new Color(25, 12, 55));
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(255, 190, 0, 70));
        g2.drawRect(14, 14, w - 28, h - 28);

        g2.setFont(new Font("Arial Black", Font.BOLD, 64));
        String title = "DIAMOND RUSH";
        int titleX = getCenterX(g2, title, w);
        g2.setColor(new Color(140, 70, 0));
        g2.drawString(title, titleX + 4, h / 6 + 4);
        g2.setColor(new Color(255, 200, 0));
        g2.drawString(title, titleX, h / 6);

        g2.setFont(new Font("Arial", Font.BOLD, 24));
        String prompt = "SELECT MAP";
        g2.setColor(new Color(170, 210, 255));
        g2.drawString(prompt, getCenterX(g2, prompt, w), h / 6 + 52);

        drawLevelOptions(g2, gp, w, h);
        drawFooterOptions(g2, gp, w, h);

        g2.setFont(new Font("Arial", Font.PLAIN, 15));
        g2.setColor(new Color(130, 130, 170));
        String hint = "W/S or Arrow Keys: choose      Enter: select      R: reset checkpoint in game";
        g2.drawString(hint, getCenterX(g2, hint, w), h - 28);
    }

    private void drawLevelOptions(Graphics2D g2, GamePanel gp, int w, int h) {
        int levelCount = gp.getLevelCount();
        int columns = Math.min(4, Math.max(1, levelCount));
        int cellW = 185;
        int cellH = 118;
        int gap = 22;
        int gridW = columns * cellW + (columns - 1) * gap;
        int startX = (w - gridW) / 2;
        int startY = h / 3;

        for (int i = 0; i < levelCount; i++) {
            int row = i / columns;
            int col = i % columns;
            int x = startX + col * (cellW + gap);
            int y = startY + row * (cellH + gap);
            boolean selected = selectedOption == i;
            boolean unlocked = gp.isLevelUnlocked(i);
            boolean completed = gp.isLevelCompleted(i);

            g2.setColor(selected ? new Color(255, 200, 0, 45) : new Color(255, 255, 255, 18));
            g2.fillRoundRect(x, y, cellW, cellH, 12, 12);
            g2.setColor(selected ? new Color(255, 215, 0) : new Color(255, 255, 255, 70));
            g2.drawRoundRect(x, y, cellW, cellH, 12, 12);

            g2.setFont(new Font("Arial Black", Font.BOLD, 26));
            g2.setColor(unlocked ? Color.WHITE : Color.GRAY);
            String name = gp.getLevelName(i);
            g2.drawString(name, getCenterXInBox(g2, name, x, cellW), y + 44);

            g2.setFont(new Font("Arial", Font.BOLD, 18));
            String status = completed ? "COMPLETED" : unlocked ? "UNLOCKED" : "LOCKED";
            g2.setColor(completed ? new Color(100, 245, 130) : unlocked ? new Color(140, 210, 255) : Color.GRAY);
            g2.drawString(status, getCenterXInBox(g2, status, x, cellW), y + 82);

            if (selected && blinkOn) {
                g2.setColor(new Color(255, 215, 0));
                g2.drawString(">", x - 24, y + 66);
            }
        }
    }

    private void drawFooterOptions(Graphics2D g2, GamePanel gp, int w, int h) {
        int levelCount = gp.getLevelCount();
        String[] options = {"How To Play", "Quit"};
        g2.setFont(new Font("Arial Black", Font.BOLD, 27));
        int baseY = h - 150;

        for (int i = 0; i < options.length; i++) {
            int optionIndex = levelCount + i;
            boolean selected = selectedOption == optionIndex;
            String text = (selected && blinkOn ? "> " : "  ") + options[i];
            g2.setColor(selected ? new Color(255, 215, 0) : new Color(165, 165, 205));
            g2.drawString(text, getCenterX(g2, text, w), baseY + i * 42);
        }
    }

    private int getCenterX(Graphics2D g2, String text, int w) {
        return (w - g2.getFontMetrics().stringWidth(text)) / 2;
    }

    private int getCenterXInBox(Graphics2D g2, String text, int x, int width) {
        return x + (width - g2.getFontMetrics().stringWidth(text)) / 2;
    }
}
