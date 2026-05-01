package main.entity;

<<<<<<< HEAD
public class Enemy {
} //Thái làm phần này
=======
import java.awt.Graphics2D;

public abstract class Enemy extends Character {

    public int baseDamage;

    public Enemy() {
        super();
    }
    
    // Bắt buộc các quái vật cụ thể phải tự định nghĩa trí tuệ nhân tạo (AI) của mình
    public abstract void aiBehavior();

    @Override
    public void update() {
        aiBehavior();
        // Các logic tính toán di chuyển tiếp theo giống nhau của mọi con quái
    }

    @Override
    public void takeDamage(int amount) {
        this.life -= amount;
        if (this.life <= 0) {
            die();
        }
    }

    @Override
    public boolean isDead() {
        return this.life <= 0;
    }

    @Override
    public void die() {
        // Có thể cộng điểm cho Player hoặc rơi ra chìa khóa khi quái chết
    }
}
>>>>>>> cb0a03e (test)
