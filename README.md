# Diamond Rush Java

**Diamond Rush Java** là một game phiêu lưu - giải đố 2D lấy cảm hứng từ dòng game **Diamond Rush** cổ điển. Người chơi điều khiển nhà thám hiểm đi qua các khu mỏ, thu thập kim cương, tránh bẫy và quái vật, dùng búa/chìa khóa để mở đường, sau đó tìm cửa thoát để hoàn thành từng màn chơi.

Dự án được viết bằng Java thuần, sử dụng Java Swing/AWT để hiển thị giao diện, xử lý input bàn phím, vẽ bản đồ dạng tile và phát âm thanh.

---

## Mục lục

- [Công nghệ và thư viện sử dụng](#công-nghệ-và-thư-viện-sử-dụng)
- [Tính năng chính của game](#tính-năng-chính-của-game)
- [Cấu trúc dự án](#cấu-trúc-dự-án)
- [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
- [Cài đặt, chạy và build game](#cài-đặt-chạy-và-build-game)
- [Hướng dẫn điều khiển](#hướng-dẫn-điều-khiển)
- [Luật chơi cơ bản](#luật-chơi-cơ-bản)
- [Tài liệu](#tài-liệu)
- [Hạn chế hiện tại so với game gốc](#hạn-chế-hiện-tại-so-với-game-gốc)
- [Hướng phát triển](#hướng-phát-triển)

---

## Công nghệ và thư viện sử dụng

| Nhóm | Công nghệ / thư viện | Vai trò |
| --- | --- | --- |
| Ngôn ngữ | Java | Viết toàn bộ logic game |
| Giao diện | Java Swing (`JFrame`, `JPanel`) | Tạo cửa sổ game và panel render |
| Đồ họa | Java AWT / Java2D (`Graphics2D`, `BufferedImage`) | Vẽ map, nhân vật, vật phẩm, hiệu ứng và UI |
| Âm thanh | Java Sound API (`Clip`, `AudioSystem`) | Phát nhạc nền và hiệu ứng âm thanh `.wav` |
| Dữ liệu map | CSV | Lưu tile nền và object theo từng màn chơi |
| Asset | PNG, WAV | Hình ảnh tile, nhân vật, object, UI và âm thanh |
| Build | JDK CLI (`javac`, `jar`) | Biên dịch source và đóng gói file `.jar` |

Dự án hiện không phụ thuộc Maven/Gradle, nên có thể build trực tiếp bằng các lệnh đi kèm JDK.

---

## Tính năng chính của game

- **Gameplay theo ô lưới 2D**: nhân vật di chuyển từng ô bằng WASD hoặc phím mũi tên.
- **Nhiều màn chơi**: game tự quét các cặp file `mapXX_background.csv` và `mapXX_objects.csv` trong thư mục `res/maps` để tạo danh sách màn.
- **World Map**: chọn màn đã mở khóa, theo dõi màn đã hoàn thành.
- **Lưu tiến trình**: hỗ trợ Continue bằng file save tại thư mục người dùng (`~/.diamondrush_save.properties`).
- **Thu thập kim cương**: nhặt kim cương trong map để tăng điểm và hoàn thành mục tiêu màn.
- **Cửa thoát / kim cương cuối màn**: đi vào cửa hoặc nhặt vật phẩm kết thúc để qua màn.
- **Vật lý đá và kim cương rơi**: đá/kim cương có thể rơi xuống, trượt khi bị chồng, gây nguy hiểm cho người chơi và kẻ địch.
- **Đẩy đá giải đố**: dùng đá để mở công tắc, chặn đường hoặc tiêu diệt rắn.
- **Búa**: dùng phím `F` sau khi nhặt búa để phá bụi/cụm nhựa và làm choáng kẻ địch ở ô kề bên.
- **Chìa khóa và khóa đặc biệt**: nhặt key để mở một số loại khóa khi đứng gần.
- **Công tắc và khóa chốt**: người chơi hoặc đá có thể giữ công tắc để mở khóa gần đó.
- **Kẻ địch và bẫy**: có rắn di chuyển, tượng phun lửa, rắn boss, đá rơi và các chướng ngại trên map.
- **Checkpoint**: reset về điểm spawn/checkpoint gần nhất bằng phím `R`.
- **Màn hình UI đầy đủ**: menu chính, hướng dẫn chơi, cốt truyện, tạm dừng, game over, win screen và HUD trong game.
- **Âm thanh**: nhạc nền menu/stage và hiệu ứng đi bộ, nhặt kim cương, đẩy đá, chọn menu, qua màn, mất mạng.

---

## Cấu trúc dự án

```text
diamond-rush-java/
├── README.md                       # Tài liệu giới thiệu và hướng dẫn dự án
├── src/
│   └── main/
│       ├── Game.java               # Entry point: tạo cửa sổ JFrame và khởi động game loop
│       ├── config/                 # Cấu hình kích thước tile, màn hình, FPS
│       ├── core/                   # GamePanel, GameLoop và logic điều phối gameplay chính
│       ├── entity/                 # Entity, Player, Enemy
│       ├── input/                  # KeyHandler: xử lý input bàn phím
│       ├── map/                    # MapLoader: đọc CSV, quản lý tile nền
│       ├── object/                 # Các object trong game: đá, kim cương, cửa, khóa, búa, key...
│       ├── ui/                     # Menu, world map, pause, game over, win, HUD, âm thanh UI
│       └── util/                   # AssetManager, Sound helper
└── res/
    ├── maps/                       # File CSV cho background và object của từng map
    ├── tiles/                      # Sprite tile nền: đất, tường, bụi, nhựa, khóa...
    ├── objects/                    # Sprite vật phẩm, bẫy, cửa, đá, rắn...
    ├── characters/                 # Sprite nhân vật: đi, spawn, chết, dùng búa
    ├── ui/                         # Hình ảnh giao diện menu/HUD
    └── sounds/                     # Nhạc nền và hiệu ứng âm thanh WAV
```

Gợi ý: nếu sau này thêm báo cáo hoặc tài liệu PDF, có thể tạo thêm thư mục:

```text
docs/
├── report.pdf                      # Báo cáo dự án
├── features.pdf                    # Mô tả tính năng chi tiết
└── notes.pdf                       # Ghi chú phát triển / hướng dẫn bổ sung
```

---

## Yêu cầu hệ thống

- **JDK 17 trở lên** được khuyến nghị.
    - Mã nguồn dùng cú pháp `switch` dạng mũi tên (`case ... ->`), nên cần JDK tương đối mới.
- Hệ điều hành có giao diện đồ họa: Windows, macOS hoặc Linux desktop.
- Loa/tai nghe nếu muốn nghe âm thanh.

Kiểm tra Java trên máy:

```bash
java -version
javac -version
```

---

## Cài đặt, chạy và build game

### 1. Tải source code

```bash
git clone <repository-url>
cd diamond-rush-java
```

Nếu đã có source code dạng file `.zip`, hãy giải nén rồi mở terminal tại thư mục gốc của dự án.

### 2. Chạy trực tiếp từ source

Trên macOS/Linux/Git Bash:

```bash
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out:res main.Game
```

Trên Windows PowerShell:

```powershell
New-Item -ItemType Directory -Force out
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp "out;res" main.Game
```

> Lưu ý: cần thêm `res` vào classpath vì game load ảnh, âm thanh và map bằng resource path.

### 3. Build file `.jar`

Trên macOS/Linux/Git Bash:

```bash
rm -rf out diamond-rush-java.jar
mkdir -p out
javac -d out $(find src -name "*.java")
jar --create --file diamond-rush-java.jar --main-class main.Game -C out . -C res .
java -jar diamond-rush-java.jar
```

Trên Windows PowerShell:

```powershell
Remove-Item -Recurse -Force out -ErrorAction SilentlyContinue
Remove-Item -Force diamond-rush-java.jar -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force out
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
jar --create --file diamond-rush-java.jar --main-class main.Game -C out . -C res .
java -jar diamond-rush-java.jar
```

Sau khi build thành công, file `diamond-rush-java.jar` có thể được gửi cho máy khác có cài Java phù hợp để chạy bằng:

```bash
java -jar diamond-rush-java.jar
```

### 4. Dọn file build tạm

```bash
rm -rf out diamond-rush-java.jar
```

Trên Windows PowerShell:

```powershell
Remove-Item -Recurse -Force out -ErrorAction SilentlyContinue
Remove-Item -Force diamond-rush-java.jar -ErrorAction SilentlyContinue
```

---

## Hướng dẫn điều khiển

| Phím | Chức năng |
| --- | --- |
| `W` / `↑` | Di chuyển lên |
| `S` / `↓` | Di chuyển xuống |
| `A` / `←` | Di chuyển sang trái |
| `D` / `→` | Di chuyển sang phải |
| `Enter` | Chọn menu, xác nhận, chuyển trang hướng dẫn, quay lại sau khi thắng/thua |
| `P` | Tạm dừng / tiếp tục game |
| `Esc` | Tạm dừng khi đang chơi hoặc quay lại ở một số màn hình |
| `Q` | Lưu và quay về menu khi đang chơi hoặc đang pause |
| `R` | Reset về checkpoint/spawn gần nhất |
| `F` | Dùng búa lên các ô kề bên sau khi đã nhặt búa |

### Điều khiển theo màn hình

- **Menu chính**: dùng `W/S` hoặc `↑/↓` để chọn, `Enter` để xác nhận.
- **World Map**: dùng phím mũi tên/WASD để chọn màn, `Enter` để vào màn, `Esc` để quay lại menu.
- **How To Play**: dùng `Enter` hoặc phím điều hướng để đổi trang, `Esc` để quay lại menu.
- **Trong màn chơi**: dùng WASD/phím mũi tên để di chuyển, `F` dùng búa, `P`/`Esc` để pause, `R` reset checkpoint, `Q` lưu và về menu.
- **Pause Screen**: dùng `W/S` hoặc `↑/↓` để chọn Continue/Reset/Quit, `Enter` để xác nhận.

---

## Luật chơi cơ bản

1. Di chuyển nhân vật qua khu mỏ để thu thập kim cương.
2. Tránh rắn, lửa, đá rơi và các vật cản nguy hiểm.
3. Đẩy đá để mở đường, kích hoạt công tắc hoặc tiêu diệt rắn.
4. Nhặt búa để phá bụi/cụm nhựa và làm choáng kẻ địch ở gần.
5. Nhặt chìa khóa để mở khóa đặc biệt.
6. Khi đủ điều kiện, đi vào cửa hoặc nhặt kim cương kết thúc màn để hoàn thành level.
7. Nếu gặp nguy hiểm, dùng `R` để quay về checkpoint/spawn gần nhất.

---

## Tài liệu

Các tài liệu chi tiết sẽ được bổ sung sau dưới dạng PDF, ví dụ:

- **Báo cáo dự án**: phân tích yêu cầu, thiết kế game, phân công công việc, kết quả đạt được.
- **Tài liệu tính năng**: mô tả chi tiết gameplay, object, map, cơ chế save/load và UI.
- **Ghi chú phát triển**: lỗi đã biết, ý tưởng cải tiến, hướng dẫn thêm map/asset.

Đề xuất đặt các file này trong thư mục `docs/`:

```text
docs/report.pdf
docs/features.pdf
docs/notes.pdf
```

---

## Hạn chế hiện tại so với game gốc

- Số lượng màn chơi còn ít hơn game Diamond Rush gốc.
- Chưa có đầy đủ tất cả thế giới, chủ đề map, boss, vật phẩm và cơ chế đặc biệt của bản gốc.
- Animation và hiệu ứng hình ảnh còn đơn giản.
- Hệ thống âm thanh mới ở mức cơ bản, chưa có nhiều biến thể theo tình huống.
- Chưa có trình chỉnh sửa map trực quan; map hiện được tạo/chỉnh bằng file CSV.
- Chưa có màn hình tùy chỉnh cấu hình như âm lượng, độ phân giải, key binding.
- Save game còn đơn giản, lưu trong file properties tại thư mục người dùng.
- Chưa có bản đóng gói native riêng cho Windows/macOS/Linux; hiện chạy chủ yếu qua Java `.jar`.

---

## Hướng phát triển

- Thêm nhiều màn chơi, tăng độ khó và đa dạng câu đố.
- Bổ sung thêm thế giới mới: rừng, băng, dung nham, đền cổ, hang boss...
- Thêm enemy/boss mới với hành vi phức tạp hơn.
- Hoàn thiện hệ thống vật phẩm: khiên, thuốc hồi mạng, bom, cổng dịch chuyển, chìa khóa theo màu.
- Làm editor tạo map trực quan thay vì chỉnh CSV thủ công.
- Thêm menu cài đặt âm lượng, toàn màn hình, độ phân giải và đổi phím điều khiển.
- Cải thiện animation, particle effect, transition giữa màn và hiệu ứng chiến thắng/thua cuộc.
- Thêm hệ thống thành tích, điểm số cao, thời gian hoàn thành level.
- Tách cấu hình level/object ra file dễ chỉnh sửa hơn, hỗ trợ mod hoặc custom map.
- Đóng gói bản phát hành bằng `jpackage` để tạo app native cho từng hệ điều hành.

---

## Ghi chú phát triển map

- Mỗi màn nên có 2 file trong `res/maps/`:
    - `mapXX_background.csv`: dữ liệu tile nền.
    - `mapXX_objects.csv`: dữ liệu object/vật phẩm/kẻ địch.
- Tên `XX` nên đánh số 2 chữ số, ví dụ `map01`, `map02`, `map03`.
- Khi thêm đủ cả hai file background và object, game sẽ tự phát hiện màn mới trong giới hạn quét hiện tại.

---

## Trạng thái dự án

Dự án đang ở mức prototype/playable: có thể chạy, chơi qua các màn, lưu tiến trình và thử nghiệm các cơ chế chính. README này sẽ tiếp tục được cập nhật khi nhóm bổ sung báo cáo PDF, thêm màn chơi và hoàn thiện tính năng.