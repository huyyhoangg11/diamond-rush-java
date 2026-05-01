package main.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import main.config.GameConfig;

public class Snake extends Enemy {

    private int walkCounter = 0; // Đếm số ô đã đi để quay đầu
    private int pixelCounter = 0;
    
    public boolean isHorizontalPatrol; // True: đi ngang, False: đi dọc

    public Snake(int startCol, int startRow, boolean horizontal) {
        super();
        this.name = "Snake";
        this.speed = 2; // Rắn đi chậm hơn người chơi (base 4)
        this.maxLife = 1;
        this.life = this.maxLife;
        this.baseDamage = 1; // Gây 1 máu cho người chơi

        this.col = startCol;
        this.row = startRow;
        this.x = this.col * GameConfig.TILE_SIZE;
        this.y = this.row * GameConfig.TILE_SIZE;
        
        this.isHorizontalPatrol = horizontal;

        if (this.isHorizontalPatrol) {
            this.direction = "right"; // Bắt đầu tuần tra hướng phải
        } else {
            this.direction = "down";  // Bắt đầu tuần tra hướng xuống
        }
        
        this.isMoving = false;
    }

    @Override
    public void aiBehavior() {
        if (!isMoving) {
            // Khi đứng im, ra quyết định đi tiếp hướng tuần tra
            isMoving = true;
            pixelCounter = 0;
            walkCounter++;
            
            // Xẹt qua lại 3 ô. Đi đủ 3 ô thì quay đầu
            if (walkCounter > 3) {
                if (isHorizontalPatrol) {
                    if (direction.equals("right")) direction = "left";
                    else direction = "right";
                } else {
                    if (direction.equals("down")) direction = "up";
                    else direction = "down";
                }
                walkCounter = 0;
            }
        }
    }

    @Override
    public void update() {
        // Logic AI định hướng cho rắn
        aiBehavior();

        // Xử lý di chuyển lưới (Smooth grid movement giống hệt Player)
        if (isMoving) {
            switch (direction) {
                case "up": y -= speed; break;
                case "down": y += speed; break;
                case "left": x -= speed; break;
                case "right": x += speed; break;
            }

            pixelCounter += speed;

            // Xử lý đồ họa hoạt ảnh
            spriteCounter++;
            if (spriteCounter > 12) {
                if (spriteNum == 1) spriteNum = 2;
                else spriteNum = 1;
                spriteCounter = 0;
            }

            // Xử lý khi đã đi trọn 1 ô grid
            if (pixelCounter >= GameConfig.TILE_SIZE) {
                isMoving = false;
                pixelCounter = 0;

                if (direction.equals("up")) row--;
                if (direction.equals("down")) row++;
                if (direction.equals("left")) col--;
                if (direction.equals("right")) col++;

                x = col * GameConfig.TILE_SIZE;
                y = row * GameConfig.TILE_SIZE;
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // Tạm thời chọn màu tím/xanh ngọc cho Rắn
        g2.setColor(new Color(153, 0, 153)); // Purple
        
        // Vẽ thân rắn, nếu hoạt ảnh lẻ thì bụng sát đất
        if (spriteNum == 1) {
            g2.fillRect(x, y + 10, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE - 10);
        } else {
            g2.fillRect(x, y + 15, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE - 15);
        }

        // Đèn định hướng (mắt rắn đỏ)
        g2.setColor(Color.RED);
        if (direction.equals("left")) {
            g2.fillRect(x, y + 20, 8, 8);
        } else if (direction.equals("right")) {
            g2.fillRect(x + GameConfig.TILE_SIZE - 8, y + 20, 8, 8);
        } else if (direction.equals("up")) {
            g2.fillRect(x + 20, y, 8, 8);
        } else if (direction.equals("down")) {
            g2.fillRect(x + 20, y + GameConfig.TILE_SIZE - 8, 8, 8);
        }
    }
}
