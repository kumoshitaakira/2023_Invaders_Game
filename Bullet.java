import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Bullet {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private double x, y;// 弾の座標
    private int speed = 10;// 弾の速さ
    private static int bmode = 0, amode = 0;// 発射モード
    private int pier = 2;// 貫通力、この回数だけヒットしたら弾が消える
    private int damage = 30;// ダメージ量、敵の体力とのバランスを考慮する必要がある
    private int range = 1000;

    // SPACEを押したら実行する
    // 爆発エフェクトと同じ感じで、インスタンスを生成
    public Bullet(double playerX, double playerY) {
        this.x = playerX;
        this.y = playerY;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public int getdamage() {
        return (this.damage <= 0) ? 1 : this.damage;
    }

    // spaceが押されると、呼び出される。弾を発射位置に動かすとともに、弾の性能を決める
    public void setX(double bulletx) {
        switch (bmode % 3) {
            case 0:
                this.speed = 15; // 2
                this.range = 300;
                break;
            case 1:
                this.speed = 5; // 4
                this.range = 100;
                break;
            case 2:
                this.speed = 30;  // 1
                this.range = 700;
                break;
        }
        this.damage = this.damage / this.speed;
        switch (amode % 3) {
            case 0:
                this.pier = 5;
                break;
            case 1:
                this.pier = 3;
                break;
            case 2:
                this.pier = 1;
                break;
        }
        this.x = bulletx;
    }

    public void setY(double bullety) {
        this.y = bullety;
    }

    // こちらも爆発エフェクト同様描画
    public void draw(GraphicsContext gc) {
        if (this.pier < 1) {
            this.x = 1000;
            return;
        } else if (this.range < 0) {
            this.x = 1000;
            return;
        }
        gc.setFill(Color.BLUE);
        gc.fillOval(this.x, this.y, 10, 20);

        this.y -= this.speed;
        this.range -= this.speed;
    }

    // インベーダークラスのhit同様、衝突時に貫通力を下げる
    public void hit() {
        this.pier--;
    }

    // Bキーを押すことで呼び出す、弾の撃ち方を変更
    public void changemode() {
        bmode = bmode + 1;
    }

    // 右下に現在の弾と射撃状態を表示
    public void showmode(GraphicsContext gc) {
        switch (bmode % 3) {
            case 0:
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                gc.fillText("Assault", WIDTH - 50, HEIGHT - 10);
                break;
            case 1:
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                gc.fillText("Shoot", WIDTH - 50, HEIGHT - 10);
                break;
            case 2:
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                gc.fillText("Snipe", WIDTH - 50, HEIGHT - 10);
                break;
        }
        switch (amode % 3) {
            case 0:
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                gc.fillText("AP", WIDTH - 100, HEIGHT - 10);
                break;
            case 1:
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                gc.fillText("BP", WIDTH - 100, HEIGHT - 10);
                break;
            case 2:
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                gc.fillText("FMJ", WIDTH - 100, HEIGHT - 10);
                break;
        }
    }

    // Vキーで呼び出す、弾の強さを変更
    public void changeammo() {
        amode++;
    }
}