package main.ui;

import main.core.GamePanel;
import main.input.KeyHandler;
import main.util.AssetManager;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class WorldMapScreen implements Screen {

    private final GameStateManager gsm;
    private int selectedOption = 0;
    private boolean keyWasDown = false;
    private int blinkCounter = 0;
    private boolean blinkOn = true;

    public WorldMapScreen(GameStateManager gsm) {
        this.gsm = gsm;
    }

    @Override
    public void update(KeyHandler key) {
        GamePanel gp = gsm.getGamePanel();
        int optionCount = gp.getLevelCount() + 1;
        boolean anyKey = key.upPressed || key.downPressed || key.leftPressed
                || key.rightPressed || key.enterPressed || key.escPressed;

        if (selectedOption >= optionCount) {
            selectedOption = 0;
        }

        if (anyKey && !keyWasDown) {
            if (key.escPressed) {
                gp.playSfx(SoundManager.SFX_MENU_SELECT);
                gsm.setState(GameState.MENU);
            } else if (key.upPressed) {
                moveVertical(gp, -1);
                gp.playSfx(SoundManager.SFX_MENU_MOVE);
            } else if (key.downPressed) {
                moveVertical(gp, 1);
                gp.playSfx(SoundManager.SFX_MENU_MOVE);
            } else if (key.leftPressed) {
                moveHorizontal(gp, -1);
                gp.playSfx(SoundManager.SFX_MENU_MOVE);
            } else if (key.rightPressed) {
                moveHorizontal(gp, 1);
                gp.playSfx(SoundManager.SFX_MENU_MOVE);
            } else if (key.enterPressed) {
                if (selectedOption == gp.getLevelCount() || gp.isLevelUnlocked(selectedOption)) {
                    gp.playSfx(SoundManager.SFX_MENU_SELECT);
                }
                handleSelection(gp);
            }
        }
        keyWasDown = anyKey;

        if (++blinkCounter >= 30) {
            blinkOn = !blinkOn;
            blinkCounter = 0;
        }
    }

    private void moveVertical(GamePanel gp, int direction) {
        int levelCount = gp.getLevelCount();
        if (selectedOption == levelCount) {
            selectedOption = direction < 0 ? levelCount - 1 : 0;
            return;
        }

        int next = selectedOption + direction;
        if (next < 0 || next >= levelCount) {
            selectedOption = levelCount;
        } else {
            selectedOption = next;
        }
    }

    private void moveHorizontal(GamePanel gp, int direction) {
        int levelCount = gp.getLevelCount();
        if (selectedOption >= levelCount) {
            return;
        }

        int rowsPerColumn = getRowsPerColumn(gp, gsm.getGamePanel().getHeight());
        int row = selectedOption % rowsPerColumn;
        int col = selectedOption / rowsPerColumn;
        int columns = (int) Math.ceil(levelCount / (double) rowsPerColumn);
        int targetCol = clamp(col + direction, 0, columns - 1);
        int target = targetCol * rowsPerColumn + row;
        selectedOption = Math.min(target, levelCount - 1);
    }

    private void handleSelection(GamePanel gp) {
        if (selectedOption < gp.getLevelCount()) {
            if (!gp.isLevelUnlocked(selectedOption)) {
                return;
            }
            gp.selectedLevelIndex = selectedOption;
            gp.startSelectedLevel();
        } else {
            gsm.setState(GameState.MENU);
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        GamePanel gp = gsm.getGamePanel();
        int w = gp.getWidth();
        int h = gp.getHeight();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(AssetManager.uiMenu0, 0, 0, w, h, null);
        g2.drawImage(AssetManager.uiMenu1, 0, 0, w, h, null);
        g2.setColor(new Color(0, 0, 0, 135));
        g2.fillRect(0, 0, w, h);

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(255, 190, 0, 95));
        g2.drawRect(28, 28, w - 56, h - 56);

        g2.setFont(new Font("Serif", Font.BOLD, 62));
        String title = "WORLD MAP";
        g2.setColor(new Color(35, 16, 6, 220));
        g2.drawString(title, getCenterX(g2, title, w) + 4, 116 + 4);
        g2.setColor(new Color(255, 205, 82));
        g2.drawString(title, getCenterX(g2, title, w), 116);

        drawLevelOptions(g2, gp, w, h);
        drawBackOption(g2, gp, w, h);

        g2.setFont(new Font("Serif", Font.BOLD, 18));
        g2.setColor(new Color(230, 218, 180));
        String hint = "Complete a map to unlock the next      Arrow Keys: choose      Enter: play      ESC: back";
        g2.drawString(hint, getCenterX(g2, hint, w), h - 34);
    }

    private void drawLevelOptions(Graphics2D g2, GamePanel gp, int w, int h) {
        int levelCount = gp.getLevelCount();
        int rowsPerColumn = getRowsPerColumn(gp, h);
        int columns = (int) Math.ceil(levelCount / (double) rowsPerColumn);
        int cellW = 245;
        int cellH = 76;
        int gapX = 28;
        int gapY = 14;
        int gridW = columns * cellW + (columns - 1) * gapX;
        int usedRows = Math.min(rowsPerColumn, levelCount);
        int gridH = usedRows * cellH + (usedRows - 1) * gapY;
        int startX = (w - gridW) / 2;
        int contentTop = 190;
        int contentBottom = h - 160;
        int startY = contentTop + Math.max(0, (contentBottom - contentTop - gridH) / 2);

        for (int i = 0; i < levelCount; i++) {
            int row = i % rowsPerColumn;
            int col = i / rowsPerColumn;
            int x = startX + col * (cellW + gapX);
            int y = startY + row * (cellH + gapY);
            boolean selected = selectedOption == i;
            boolean completed = gp.isLevelCompleted(i);
            boolean unlocked = gp.isLevelUnlocked(i);

            g2.setColor(getCellFill(selected, unlocked));
            g2.fillRoundRect(x, y, cellW, cellH, 12, 12);
            g2.setColor(getCellStroke(selected, unlocked));
            g2.drawRoundRect(x, y, cellW, cellH, 12, 12);

            g2.setFont(new Font("Serif", Font.BOLD, 28));
            String name = gp.getLevelName(i);
            g2.setColor(unlocked ? Color.WHITE : new Color(145, 145, 150));
            g2.drawString(name, x + 24, y + 34);

            g2.setFont(new Font("Serif", Font.BOLD, 16));
            String status = getStatusText(completed, unlocked);
            g2.setColor(getStatusColor(completed, unlocked));
            g2.drawString(status, x + 24, y + 60);

            if (!unlocked) {
                g2.setFont(new Font("Serif", Font.BOLD, 28));
                g2.setColor(new Color(170, 170, 175, 210));
                g2.drawString("LOCKED", x + cellW - 116, y + 45);
            }

            if (selected && blinkOn) {
                g2.setFont(new Font("Serif", Font.BOLD, 24));
                g2.setColor(unlocked ? new Color(255, 215, 0) : new Color(150, 150, 150));
                g2.drawString(">", x - 22, y + 48);
            }
        }
    }

    private int getRowsPerColumn(GamePanel gp, int h) {
        int levelCount = gp.getLevelCount();
        int cellH = 76;
        int gapY = 14;
        int availableH = h - 350;
        int rows = Math.max(1, (availableH + gapY) / (cellH + gapY));
        return Math.max(1, Math.min(levelCount, rows));
    }

    private Color getCellFill(boolean selected, boolean unlocked) {
        if (!unlocked) {
            return selected ? new Color(150, 150, 150, 34) : new Color(80, 80, 90, 30);
        }
        return selected ? new Color(255, 200, 0, 50) : new Color(255, 255, 255, 22);
    }

    private Color getCellStroke(boolean selected, boolean unlocked) {
        if (!unlocked) {
            return selected ? new Color(170, 170, 170, 135) : new Color(130, 130, 145, 70);
        }
        return selected ? new Color(255, 215, 0) : new Color(255, 255, 255, 85);
    }

    private String getStatusText(boolean completed, boolean unlocked) {
        if (completed) {
            return "COMPLETED";
        }
        return unlocked ? "UNLOCKED" : "FINISH PREVIOUS MAP";
    }

    private Color getStatusColor(boolean completed, boolean unlocked) {
        if (completed) {
            return new Color(100, 245, 130);
        }
        return unlocked ? new Color(140, 210, 255) : new Color(155, 155, 165);
    }

    private void drawBackOption(Graphics2D g2, GamePanel gp, int w, int h) {
        int backIndex = gp.getLevelCount();
        boolean selected = selectedOption == backIndex;
        String text = (selected && blinkOn ? "> " : "  ") + "BACK";
        g2.setFont(new Font("Serif", Font.BOLD, 32));
        g2.setColor(selected ? new Color(255, 215, 0) : new Color(205, 205, 225));
        g2.drawString(text, getCenterX(g2, text, w), h - 104);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private int getCenterX(Graphics2D g2, String text, int w) {
        return (w - g2.getFontMetrics().stringWidth(text)) / 2;
    }
}
