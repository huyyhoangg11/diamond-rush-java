package main.ui;

import main.input.KeyHandler;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class PauseScreen implements Screen {

    private final GameStateManager gsm;
    private final String[] options = {"Continue", "Reset to Spawn", "Quit Game"};
    private int selectedOption = 0;
    private boolean keyWasDown = false;

    public PauseScreen(GameStateManager gsm) {
        this.gsm = gsm;
    }

    @Override
    public void update(KeyHandler key) {
        boolean anyKey = key.upPressed || key.downPressed || key.enterPressed || key.rPressed;

        if (anyKey && !keyWasDown) {
            if (key.upPressed) {
                selectedOption = (selectedOption - 1 + options.length) % options.length;
                gsm.getGamePanel().playSfx(SoundManager.SFX_MENU_MOVE);
            } else if (key.downPressed) {
                selectedOption = (selectedOption + 1) % options.length;
                gsm.getGamePanel().playSfx(SoundManager.SFX_MENU_MOVE);
            } else if (key.rPressed) {
                gsm.getGamePanel().playSfx(SoundManager.SFX_MENU_SELECT);
                gsm.getGamePanel().resetToCheckpoint();
            } else if (key.enterPressed) {
                gsm.getGamePanel().playSfx(SoundManager.SFX_MENU_SELECT);
                handleSelection();
            }
        }
        keyWasDown = anyKey;
    }

    private void handleSelection() {
        switch (selectedOption) {
            case 0 -> gsm.setState(GameState.PLAYING);
            case 1 -> gsm.getGamePanel().resetToCheckpoint();
            case 2 -> gsm.getGamePanel().quitToMenuFromPause();
            default -> {
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        int w = gsm.getGamePanel().getWidth();
        int h = gsm.getGamePanel().getHeight();

        g2.setColor(new Color(0, 0, 0, 155));
        g2.fillRect(0, 0, w, h);

        int bw = 430;
        int bh = 270;
        int bx = (w - bw) / 2;
        int by = (h - bh) / 2;

        g2.setColor(new Color(18, 8, 42, 235));
        g2.fillRoundRect(bx, by, bw, bh, 22, 22);

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(255, 200, 0));
        g2.drawRoundRect(bx, by, bw, bh, 22, 22);

        g2.setFont(new Font("Arial Black", Font.BOLD, 38));
        g2.setColor(Color.WHITE);
        String title = "PAUSED";
        g2.drawString(title, getCenterX(g2, title, w), by + 58);

        g2.setFont(new Font("Arial Black", Font.BOLD, 24));
        for (int i = 0; i < options.length; i++) {
            String text = (selectedOption == i ? "> " : "  ") + options[i];
            g2.setColor(selectedOption == i ? new Color(255, 215, 0) : new Color(195, 195, 215));
            g2.drawString(text, getCenterX(g2, text, w), by + 112 + i * 42);
        }

        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.setColor(new Color(130, 210, 255));
        int col = gsm.getGamePanel().player.score;
        int live = gsm.getGamePanel().player.lives;
        String info = "Diamonds: " + col + "      Lives: " + live + "      R: reset";
        g2.drawString(info, getCenterX(g2, info, w), by + 238);
    }

    private int getCenterX(Graphics2D g2, String text, int w) {
        return (w - g2.getFontMetrics().stringWidth(text)) / 2;
    }
}
