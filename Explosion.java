import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Explosion {
    private double x;
    private double y;
    private int size;
    private int maxDuration = 60;
    private int duration = 0;

    public Explosion(double x, double y) {
        this.x = x;
        this.y = y;
        this.size = 10;
    }

    public void draw(GraphicsContext gc) {
        if (duration >= maxDuration) {
            return;
        }

        gc.setFill(Color.YELLOW);
        gc.fillOval(x - size / 2, y - size / 2, size, size);

        size += 2;
        duration++;
    }
}
