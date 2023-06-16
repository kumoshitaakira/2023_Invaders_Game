import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class InvadersGameClient extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int INVADER_SPEED = 5;
    private static final int GAME_CLEAR_DELAY = 10;
    private static final int NUM_INVADERS = 20;

    private Canvas canvas;
    private GraphicsContext gc;
    private boolean running = true;
    private boolean gameStarted = false;

    private long startTime;
    private boolean gameClearTriggered = false;

    private double playerX;
    private double playerY;

    private List<Invader> invaders;
    private boolean[] invaderDestroyed;

    private double bulletX = WIDTH * 2;
    private double bulletY = 0;

    private List<Explosion> explosions;

    private int clear = 0;
    private static int score;
    private static String name;
    private static List<ScoreEntry> ranking = new ArrayList<>();
    private static boolean newScore = false;

    public void start(Stage primaryStage) {
        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();

        BorderPane root = new BorderPane(canvas);
        root.setStyle("-fx-background-color: black;");
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        scene.setOnKeyPressed(e -> {
            if (!gameStarted && e.getCode() == KeyCode.SPACE) {
                startGame();
            } else if(running) {
                if (e.getCode() == KeyCode.LEFT) {
                    playerX -= 10;
                } else if (e.getCode() == KeyCode.RIGHT) {
                    playerX += 10;
                } else if (e.getCode() == KeyCode.SPACE) {
                    shoot();
                } else if (e.getCode() == KeyCode.UP) {
                    playerY -= 10;
                } else if (e.getCode() == KeyCode.DOWN) {
                    playerY += 10;
                }
            } else if (!running && e.getCode() == KeyCode.E) {
                exitGame(primaryStage);
            }
            
            // 枠外に行かないようにする
            if (playerX < 0) {
                playerX = 0;
            } else if (playerX > WIDTH) {
                playerX = WIDTH;
            }
            
            if (playerY < 0) {
                playerY = 0;
            } else if (playerY > HEIGHT) {
                playerY = HEIGHT;
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Invaders Game");
        primaryStage.setResizable(false);
        primaryStage.show();

        renderStartScreen();
    }

    private void renderStartScreen() {
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
        gc.fillText("Press SPACE to start", WIDTH / 2 - 120, HEIGHT / 2);
    }

    private void startGame() {
        initializeGame();
        gameStarted = true;
        running = true;
        startTime = System.nanoTime();
        new AnimationTimer() {

            public void handle(long currentNanoTime) {
                long elapsedTime = (currentNanoTime - startTime) / 1_000_000_000; // 経過時間（秒)
                if (elapsedTime >= GAME_CLEAR_DELAY && !gameClearTriggered) {
                    gameClearTriggered = true;
                }

                update();
                render();
            }
        }.start();
    }

    private void initializeGame() {
        playerX = WIDTH / 2;
        playerY = HEIGHT - 50;

        invaders = new ArrayList<>();
        invaderDestroyed = new boolean[NUM_INVADERS];
        for (int i = 0; i < NUM_INVADERS; i++) {
            Random random = new Random();
            double invaderX = WIDTH / 2 + ((i % 10) - NUM_INVADERS / 2) * random.nextInt(51);
            random = new Random();
            double invaderY = ((i % 10) + 1) * random.nextInt(10) * 10;
            invaders.add(new Invader(invaderX, invaderY));
            invaderDestroyed[i] = false;
        }

        gameClearTriggered = false;
        explosions = new ArrayList<>();
    }

    private void update() {
        if (!running) {
            return;
        }

        for (int i = 0; i < NUM_INVADERS; i++) {
            Invader invader = invaders.get(i);
            if (!invaderDestroyed[i]) {
                invader.move(INVADER_SPEED);

                if (invader.getX() >= WIDTH) {
                    invader.setX(0);
                    invader.setY(invader.getY() + 50);

                    if (invader.getY() >= HEIGHT) {
                        invader.setY(0);
                    }
                }
            }

            if (checkCollision(invader)) {
                if (!invaderDestroyed[i]) {
                    createExplosion(invader.getX(), invader.getY());
                    score += 100;
                    invaderDestroyed[i] = true;
                }
                if (gameClearTriggered) {
                    gameClear();
                }
            }
        }

        if(gameClearTriggered) gameClear();

        if (!gameClearTriggered && ((System.nanoTime() - startTime) / 1_000_000_000) >= GAME_CLEAR_DELAY) {
            gameClearTriggered = true;
        }
    }

    private boolean checkCollision(Invader invader) {
        double distance = Math.sqrt(Math.pow(invader.getX() - bulletX, 2) + Math.pow(invader.getY() - bulletY, 2));
        return distance < 30; // 衝突判定の閾値を設定
    }

    private void createExplosion(double x, double y) {
        explosions.add(new Explosion(x, y));
    }

    private void displayRanking() {
        Collections.sort(ranking, Collections.reverseOrder());
        gc.fillText("----- Ranking -----", WIDTH / 2 - 130, HEIGHT / 2 - 120);
        int i = 0;
        for(i = 0; i < ranking.size(); i++) {
            if(i >= 5) break;
            ScoreEntry entry = ranking.get(i);
            if(entry.getName().equals(name)) {
                if(newScore) {
                    gc.fillText("new " + (i + 1) + ". " + entry.getName() + " " + entry.getScore(), WIDTH / 2 - 110, HEIGHT / 2 - 80 + i * 40);
                } else {
                    gc.fillText("-> " + (i + 1) + ". " + entry.getName() + " " + entry.getScore(), WIDTH / 2 - 110, HEIGHT / 2 - 80 + i * 40);
                }
            } else {
                gc.fillText((i + 1) + ". " + entry.getName() + " " + entry.getScore(), WIDTH / 2 - 110, HEIGHT / 2 - 80 + i * 40);
            }
        }
        gc.fillText("--------------------", WIDTH / 2 - 130, HEIGHT / 2 - 80 + i * 40);
    }

    private void render() {
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        if (!running) {
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Verdana", FontWeight.BOLD, 48));
            if (clear == 1){
                gc.fillText("Game Clear", WIDTH / 2 - 150, HEIGHT / 2 - 200);
            } else {
                gc.fillText("Game Over", WIDTH / 2 - 150, HEIGHT / 2 - 200);
            }
            gc.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
            gc.fillText("Name: " + name + "  Score: " + score, WIDTH / 2 - 150, HEIGHT / 2 - 160);
            displayRanking();
            gc.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
            gc.fillText("Press E to exit", WIDTH / 2 - 120, HEIGHT / 2 + 160);
            return;
        } else {
            gc.setFill(Color.GREEN);
            gc.fillRect(playerX - 25, playerY - 12.5, 50, 25);

            // if(!invaderDestroyed) {
            //     gc.setFill(Color.RED);
            //     gc.fillRect(invaderX - 25, invaderY - 25, 50, 25);
            // }
            for (int i = 0; i < NUM_INVADERS; i++) {
                if (!invaderDestroyed[i]) {
                    Invader invader = invaders.get(i);
                    gc.setFill(Color.RED);
                    gc.fillRect(invader.getX() - 25, invader.getY() - 25, 50, 25);
                }
            }
            
            gc.setFill(Color.BLUE);
            bulletY = bulletY - 10;
            gc.fillOval(bulletX, bulletY, 50, 20);
        }

        for (Explosion explosion : explosions) {
            explosion.draw(gc);
        }
    }

    private void shoot() {
        bulletY = playerY - 25;
        bulletX = playerX - 25;
    }

    private void gameOver() {
        score -= 100;
        updateRanking(ranking, name, score);

        running = false;
    }

    private void gameClear(){
        score += 100;
        updateRanking(ranking, name, score);
        clear = 1;

        running = false;
    }

    private void exitGame(Stage primaryStage) {
        primaryStage.close();
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Usage: InvadersGameClient <hostname> <port> <name>");
            System.exit(1);
        }

        final String hostname = args[0];
        final int port = Integer.parseInt(args[1]);
        name = args[2];

        InetAddress addr = InetAddress.getByName(hostname);
        System.out.println("addr = " + addr);
        Socket socket = new Socket(addr, port);

        InvadersGameClient game = new InvadersGameClient();

        try {
            System.out.println("socket = " + socket);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            // 名前の送信
            out.println(name);

            String startMs = in.readLine();
            System.out.println(startMs);

            // ランキングデータの受信
            int oldRanking = Integer.parseInt(in.readLine());
            String nameData = "null";
            int scoreData = 0;
            for (int i = 0; i < oldRanking * 2; i++) {
                String response = in.readLine();
                if(i % 2 == 0) {
                    nameData = response;
                } else {
                    scoreData = Integer.parseInt(response);
                    ranking.add(new ScoreEntry(nameData, scoreData));
                }
            }

            // ゲーム処理
            game.launch(args);
            
            out.println(score);

            String resultUser = in.readLine();
            System.out.println(resultUser);
            String  resultScore = in.readLine();
            System.out.println(resultScore);

            out.println("END"); // 終了を示すラベルの送信

        } finally {
            System.out.println("Closing...");
            socket.close();
        }
    }

    private static void updateRanking(List<ScoreEntry> scoreList, String name, int score) {
        // 同じ名前のエントリーが存在するかチェック
        for (ScoreEntry entry : scoreList) {
            if (entry.getName().equals(name)) {
                // スコアが新記録なら上書きして終了
                if(entry.getScore() < score) {
                    entry.setScore(score);
                    newScore = true;
                }
                return;
            }
        }

        // 新しいエントリーとして追加
        scoreList.add(new ScoreEntry(name, score));
        newScore = true;
    }
}
