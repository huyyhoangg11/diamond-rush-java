package main.ui;

import main.core.GamePanel;
import main.input.KeyHandler;
import main.util.AssetManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class MenuScreen implements Screen {

    private static final int OPTION_NEW_GAME = 0;
    private static final int OPTION_CONTINUE = 1;
    private static final int OPTION_HOW_TO_PLAY = 2;
    private static final int OPTION_STORY = 3;
    private static final int OPTION_EXIT = 4;

    private final GameStateManager gsm;
    private final String[] options = {"NEW GAME", "CONTINUE", "HOW TO PLAY", "STORY", "EXIT"};
    private int selectedOption = 0;
    private boolean keyWasDown = false;
    private int blinkCounter = 0;
    private boolean blinkOn = true;

    public MenuScreen(GameStateManager gsm) {
        this.gsm = gsm;
    }

    @Override
    public void update(KeyHandler key) {
        GamePanel gp = gsm.getGamePanel();
        boolean anyKey = key.upPressed || key.downPressed || key.enterPressed;

        if (!isOptionEnabled(selectedOption, gp)) {
            selectedOption = OPTION_NEW_GAME;
        }

        if (anyKey && !keyWasDown) {
            if (key.upPressed) {
                moveSelection(-1, gp);
                gp.playSfx(SoundManager.SFX_MENU_MOVE);
            } else if (key.downPressed) {
                moveSelection(1, gp);
                gp.playSfx(SoundManager.SFX_MENU_MOVE);
            } else if (key.enterPressed) {
                gp.playSfx(SoundManager.SFX_MENU_SELECT);
                handleSelection(gp);
            }
        }
        keyWasDown = anyKey;

        if (++blinkCounter >= 28) {
            blinkOn = !blinkOn;
            blinkCounter = 0;
        }
    }

    private void moveSelection(int direction, GamePanel gp) {
        int next = selectedOption;
        do {
            next = (next + direction + options.length) % options.length;
        } while (!isOptionEnabled(next, gp));
        selectedOption = next;
    }

    private boolean isOptionEnabled(int option, GamePanel gp) {
        return option != OPTION_CONTINUE || gp.hasSaveFile();
    }

    private void handleSelection(GamePanel gp) {
        switch (selectedOption) {
            case OPTION_NEW_GAME -> gp.startNewGame();
            case OPTION_CONTINUE -> {
                if (gp.loadSavedGame()) {
                    gsm.setState(GameState.WORLD_MAP);
                }
            }
            case OPTION_HOW_TO_PLAY -> gsm.setState(GameState.HOW_TO_PLAY);
            case OPTION_STORY -> gsm.setState(GameState.STORY);
            case OPTION_EXIT -> System.exit(0);
            default -> {
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        GamePanel gp = gsm.getGamePanel();
        int w = gp.getWidth();
        int h = gp.getHeight();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        drawMenuBackground(g2, w, h);
        drawOptions(g2, gp, w, h);
        drawHint(g2, w, h);
    }

    private void drawMenuBackground(Graphics2D g2, int w, int h) {
        g2.drawImage(AssetManager.uiMenu0, 0, 0, w, h, null);
        g2.drawImage(AssetManager.uiMenu1, 0, 0, w, h, null);
        g2.setColor(new Color(255, 238, 180, 34));
        g2.fillRect(0, 0, w, h);
    }

    private void drawOptions(Graphics2D g2, GamePanel gp, int w, int h) {
        int startY = h / 2 + 156;
        int rowH = 58;

        g2.setFont(new Font("Serif", Font.BOLD, 38));
        for (int i = 0; i < options.length; i++) {
            boolean enabled = isOptionEnabled(i, gp);
            boolean selected = selectedOption == i;
            String text = options[i];
            int x = getCenterX(g2, text, w);
            int y = startY + i * rowH;

            if (selected && blinkOn && enabled) {
                drawOutlinedText(g2, ">", x - 54, y, new Color(255, 234, 130), new Color(52, 28, 8));
                drawOutlinedText(g2, "<", x + g2.getFontMetrics().stringWidth(text) + 28, y,
                        new Color(255, 234, 130), new Color(52, 28, 8));
            }

            if (!enabled) {
                drawOutlinedText(g2, text, x, y, new Color(150, 150, 150, 210), new Color(55, 55, 55, 210));
            } else if (selected) {
                drawOutlinedText(g2, text, x, y, new Color(255, 232, 112), new Color(58, 29, 8));
            } else {
                drawOutlinedText(g2, text, x, y, new Color(255, 248, 220), new Color(65, 36, 12));
            }
        }
    }

    private void drawHint(Graphics2D g2, int w, int h) {
        g2.setFont(new Font("Serif", Font.BOLD, 18));
        String hint = "W/S or Arrow Keys: choose      Enter: select";
        drawOutlinedText(g2, hint, getCenterX(g2, hint, w), h - 34,
                new Color(255, 245, 205), new Color(45, 25, 10));
    }

    private void drawOutlinedText(Graphics2D g2, String text, int x, int y, Color fill, Color outline) {
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
