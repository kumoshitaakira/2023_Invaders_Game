import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Explosion {
    private double x;
    private double y;
    private int size;
    private int maxDuration = 60;
    private int duration = 0;
    private Image explosionImg; // 爆発エフェクトのスプライトシート
    private int explosionStep = 0; // 爆発のアニメーションフレーム

    private static final int EXPLOSION_W = 128; // 各フレームの幅
    private static final int EXPLOSION_H = 128; // 各フレームの高さ
    private static final int EXPLOSION_ROWS = 4; // スプライトシート内のフレームの行数
    private static final int EXPLOSION_COLS = 4; // スプライトシート内のフレームの列数
 

    public Explosion(double x, double y) {
        this.x = x;
        this.y = y;
        this.size = 100;
        this.explosionImg = new Image("explosion.png");
    }

    public void draw(GraphicsContext gc) {
        if (duration >= maxDuration) {
            return;
        }
        
        int frameX = explosionStep % EXPLOSION_COLS * EXPLOSION_W;
        int frameY = (explosionStep / EXPLOSION_ROWS) * EXPLOSION_H + 1;

        gc.drawImage(explosionImg, frameX, frameY, EXPLOSION_W, EXPLOSION_H, x - size / 2 , y - size / 2 , size, size);
        // だんだん大きくなる
        size += 2;
        duration++;
        explosionStep++;

    }
}
