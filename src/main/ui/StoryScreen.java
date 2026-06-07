package main.ui;

import main.input.KeyHandler;
import main.util.AssetManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

public class StoryScreen implements Screen {

    private static final Color TEXT_COLOR = new Color(246, 238, 214);
    private static final Color HIGHLIGHT_COLOR = new Color(255, 218, 104);
    private static final Color OUTLINE_COLOR = new Color(47, 25, 8);

    private static final String[][] STORY_PARAGRAPHS = {
            {
                    "Diamond Rush",
                    " kể về hành trình của một nhà thám hiểm tiến vào ",
                    "khu mỏ cổ bị lãng quên",
                    " để tìm kiếm những viên ",
                    "kim cương quý hiếm",
                    ". Bên trong khu mỏ không chỉ có kho báu mà còn ẩn chứa nhiều nguy hiểm như đá chắn đường, bụi cây, rắn độc, bẫy lửa, tượng phun lửa và những cánh cửa bị khóa."
            },
            {
                    "Người chơi",
                    " phải điều khiển nhân vật di chuyển qua từng màn chơi, ",
                    "thu thập kim cương",
                    ", tìm chìa khóa, sử dụng ",
                    "búa",
                    " để tiêu diệt quái vật và giải các câu đố bằng cách ",
                    "đẩy đá vào công tắc",
                    " để mở đường. Mỗi bản đồ là một thử thách khác nhau, yêu cầu người chơi quan sát, tính toán hướng đi và lựa chọn chiến thuật hợp lý để có thể mở cửa thoát ra ngoài."
            },
            {
                    "Mục tiêu cuối cùng",
                    " của người chơi là vượt qua toàn bộ khu mỏ, thu thập đủ kim cương và ",
                    "thoát khỏi nơi nguy hiểm này",
                    " một cách an toàn."
            }
    };

    private final GameStateManager gsm;
    private boolean keyWasDown = false;

    public StoryScreen(GameStateManager gsm) {
        this.gsm = gsm;
    }

    @Override
    public void update(KeyHandler key) {
        boolean anyKey = key.enterPressed || key.escPressed;
        if (anyKey && !keyWasDown) {
            gsm.setState(GameState.MENU);
        }
        keyWasDown = anyKey;
    }

    @Override
    public void draw(Graphics2D g2) {
        int w = gsm.getGamePanel().getWidth();
        int h = gsm.getGamePanel().getHeight();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(AssetManager.uiMenu0, 0, 0, w, h, null);
        g2.drawImage(AssetManager.uiMenu1, 0, 0, w, h, null);
        g2.setColor(new Color(0, 0, 0, 188));
        g2.fillRect(0, 0, w, h);

        int panelX = 126;
        int panelY = 94;
        int panelW = w - 252;
        int panelH = h - 176;
        drawPanel(g2, panelX, panelY, panelW, panelH);

        g2.setFont(new Font("Serif", Font.BOLD, 56));
        drawOutlinedText(g2, "STORY", getCenterX(g2, "STORY", w), panelY + 76,
                HIGHLIGHT_COLOR, OUTLINE_COLOR);

        drawStory(g2, panelX + 76, panelY + 142, panelW - 152);

        g2.setFont(new Font("Serif", Font.BOLD, 19));
        String back = "Press Enter or ESC to return to Menu";
        drawOutlinedText(g2, back, getCenterX(g2, back, w), h - 40,
                new Color(255, 245, 205), OUTLINE_COLOR);
    }

    private void drawPanel(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(new Color(28, 18, 10, 205));
        g2.fillRoundRect(x, y, width, height, 28, 28);
        g2.setColor(new Color(255, 214, 102, 185));
        g2.drawRoundRect(x, y, width, height, 28, 28);
        g2.setColor(new Color(255, 245, 195, 62));
        g2.drawRoundRect(x + 8, y + 8, width - 16, height - 16, 22, 22);
    }

    private void drawStory(Graphics2D g2, int x, int y, int maxWidth) {
        Font normalFont = new Font("Serif", Font.PLAIN, 24);
        Font boldFont = new Font("Serif", Font.BOLD, 25);
        int currentY = y;

        for (String[] paragraph : STORY_PARAGRAPHS) {
            List<StoryLine> lines = wrapSegments(g2, paragraph, normalFont, boldFont, maxWidth);
            for (StoryLine line : lines) {
                drawStoryLine(g2, line, x, currentY, normalFont, boldFont);
                currentY += 36;
            }
            currentY += 24;
        }
    }

    private List<StoryLine> wrapSegments(Graphics2D g2, String[] segments,
                                         Font normalFont, Font boldFont, int maxWidth) {
        List<StoryLine> lines = new ArrayList<>();
        StoryLine currentLine = new StoryLine();
        int currentWidth = 0;

        for (int i = 0; i < segments.length; i++) {
            boolean highlight = i % 2 == 0;
            Font font = highlight ? boldFont : normalFont;
            FontMetrics metrics = g2.getFontMetrics(font);
            String[] words = segments[i].split(" ");

            for (int j = 0; j < words.length; j++) {
                String rawWord = words[j];
                if (rawWord.isEmpty()) {
                    continue;
                }
                String word = rawWord;
                boolean addLeadingSpace = currentLine.hasText() || startsWithSpace(segments[i]) || j > 0;
                String token = addLeadingSpace ? " " + word : word;
                int tokenWidth = metrics.stringWidth(token);

                if (currentLine.hasText() && currentWidth + tokenWidth > maxWidth) {
                    lines.add(currentLine);
                    currentLine = new StoryLine();
                    currentWidth = 0;
                    token = word;
                    tokenWidth = metrics.stringWidth(token);
                }

                currentLine.parts.add(new StoryPart(token, highlight));
                currentWidth += tokenWidth;
            }
        }

        if (currentLine.hasText()) {
            lines.add(currentLine);
        }
        return lines;
    }

    private boolean startsWithSpace(String text) {
        return !text.isEmpty() && Character.isWhitespace(text.charAt(0));
    }

    private void drawStoryLine(Graphics2D g2, StoryLine line, int x, int y, Font normalFont, Font boldFont) {
        int currentX = x;
        for (StoryPart part : line.parts) {
            Font font = part.highlight ? boldFont : normalFont;
            Color color = part.highlight ? HIGHLIGHT_COLOR : TEXT_COLOR;
            g2.setFont(font);
            drawOutlinedText(g2, part.text, currentX, y, color, OUTLINE_COLOR);
            currentX += g2.getFontMetrics(font).stringWidth(part.text);
        }
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

    private static final class StoryLine {
        private final List<StoryPart> parts = new ArrayList<>();

        private boolean hasText() {
            return !parts.isEmpty();
        }
    }

    private static final class StoryPart {
        private final String text;
        private final boolean highlight;

        private StoryPart(String text, boolean highlight) {
            this.text = text;
            this.highlight = highlight;
        }
    }
}
