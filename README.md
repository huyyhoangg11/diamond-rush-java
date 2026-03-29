classDiagram
%% Định nghĩa các lớp trừu tượng (Lớp cha)
class Entity {
<<abstract>>
+int x, y
+int speed
+Rectangle solidArea
+update()*
+draw(Graphics2D)*
}

    class GameObject {
        <<abstract>>
        +int worldX, worldY
        +boolean collision
        +draw(Graphics2D)*
    }

    %% Định nghĩa các lớp con và quan hệ kế thừa
    class Player {
        +KeyHandler keyH
        +update()
        +draw(Graphics2D)
    }
    
    class Enemy {
        +update()
        +draw(Graphics2D)
    }
    
    class Diamond {
        +draw(Graphics2D)
    }
    
    class Rock {
        +draw(Graphics2D)
    }

    %% Mũi tên thể hiện sự kế thừa (extends)
    Entity <|-- Player : Kế thừa
    Entity <|-- Enemy : Kế thừa
    
    GameObject <|-- Diamond : Kế thừa
    GameObject <|-- Rock : Kế thừa

graph TD
%% Điểm bắt đầu
Start([main.Game]) -->|1. Khởi tạo| GP(core.GamePanel)
Start -->|2. Khởi tạo & Chạy| GL(core.GameLoop)

    %% Vòng lặp Game
    GL -->|3. Gọi 60 lần/giây| GP
    
    %% Bên trong GamePanel
    subgraph GamePanel [Khu vực core.GamePanel]
        Update[Hàm update]
        Draw[Hàm paintComponent]
    end
    
    GP --> Update
    GP --> Draw
    
    %% Chi tiết hàm update() (Cập nhật logic)
    Update -->|Kiểm tra phím| KeyH(input.KeyHandler)
    Update -->|Tính toán tọa độ| PlayerU(Player.update)
    Update -->|Tính toán AI| EnemyU(Enemy.update)
    
    PlayerU -.->|Kiểm tra vật cản| Col(util.Collision)
    EnemyU -.->|Kiểm tra vật cản| Col
    
    %% Chi tiết hàm paintComponent() (Vẽ đồ họa)
    Draw -->|1. Vẽ nền gạch| MapD(MapLoader.draw)
    Draw -->|2. Vẽ đá, kim cương| ObjD(GameObject.draw)
    Draw -->|3. Vẽ nhân vật| PlayerD(Player.draw)
    Draw -->|4. Vẽ điểm số| UID(UI.draw)
    
    %% Ghi chú thứ tự vẽ
    style MapD fill:#f9f,stroke:#333,stroke-width:2px
    style UID fill:#bbf,stroke:#333,stroke-width:2px