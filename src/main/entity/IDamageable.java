package main.entity;

/**
 * Interface dành cho bất kỳ thực thể nào có thể nhận sát thương (có thanh máu).
 */
public interface IDamageable {
    /**
     * Hàm gọi khi thực thể bị nhận sát thương.
     * @param amount Lượng sát thương nhận vào.
     */
    void takeDamage(int amount);

    /**
     * Kiểm tra xem thực thể đã chết chưa (máu <= 0).
     * @return true nếu đã chết
     */
    boolean isDead();

    /**
     * Logic khi thực thể tử vong.
     */
    void die();
}
