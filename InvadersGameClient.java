import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class InvadersGameClient extends Application {
    // 各種定数の宣言
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int INVADER_SPEED_X = 4;
    private static final double INVADER_SPEED_Y = 0.4;
    private static final int PLAYER_SPEED = 20;
    private static final int[] NUMS_INVADERS = { 6, 10, 20, 30, 40 };
    private static final int NUM_BULLETS = 100;
    private static final String[] IMAGES = { "alien.png", "alien_blue.png", "alien_green.png", "alien_orange.png",
            "alien_yellow.png" };

    // 描画に関わるものの宣言
    private Canvas canvas;
    private GraphicsContext gc;

    // ゲームの実行に関するフラグの宣言
    private boolean running = true;
    private boolean gameStarted = false;

    // ゲームの終了に用いる変数とフラグの宣言
    private long startTime;

    // 自機１つ・ステージ1~5の敵機複数とボス敵機(破壊判定付き)
    private Invader player;
    private List<List<Invader>> invaders;
    private List<List<Boolean>> invadersDestroyed;
    private Invader boss;
    private boolean bossDestroyed;
    private int bossdirection; // ボスの移動方向

    // 爆発のエフェクト用リストの宣言
    private List<Explosion> explosions;

    // 弾
    private List<Bullet> bullets;
    private static int bulletnumber = 0;

    // クリア判定用の整数を宣言
    private int clear = 0;

    // 取得したスコアと名前と送信用スコアの宣言
    private static int score = 0;
    private static int sendScore = 0;
    private static String name = "null";

    // マップ情報取得用の配列を宣言
    private static int[] rawMapData = { 0, 0, 0, 0, 0 };
    private static MapData mapData;

    // ランキング(更新可)のリストとフラグを宣言
    private static List<ScoreEntry> ranking = new ArrayList<>();
    private static boolean newScore = false;

    // アニメーションの実行・停止を管理するtimerを宣言
    private static AnimationTimer gameLoop;

    // ゲーム起動に関わるメソッド
    public void start(Stage primaryStage) {
        // Javafxxの描画設定
        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        BorderPane root = new BorderPane(canvas);
        root.setStyle("-fx-background-color: black;");
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        // キーボードの諸々設定
        scene.setOnKeyPressed(e -> {
            if (!gameStarted && e.getCode() == KeyCode.SPACE) {
                startGame();
            } else if (running) {
                handleGameInput(e);
            } else if (!running) {
                handleGameOverInput(e, primaryStage);
            }
        });

        // インベーダーゲーム画面の設定
        primaryStage.setScene(scene);
        primaryStage.setTitle("Invaders Game");
        primaryStage.setResizable(false);
        primaryStage.show();

        // 最初のスタート画面
        renderStartScreen();
    }

    // ゲームプレイ時のキーボド設定
    private void handleGameInput(KeyEvent e) {
        if (e.getCode() == KeyCode.LEFT) {
            player.setX(player.getX() - PLAYER_SPEED);
        } else if (e.getCode() == KeyCode.RIGHT) {
            player.setX(player.getX() + PLAYER_SPEED);
        } else if (e.getCode() == KeyCode.SPACE) {
            Bullet newbullet = bullets.get(bulletnumber % NUM_BULLETS);
            newbullet.setX(player.getX());
            newbullet.setY(player.getY());
            bulletnumber++;
        } else if (e.getCode() == KeyCode.UP) {
            player.setY(player.getY() - (PLAYER_SPEED - 5));
        } else if (e.getCode() == KeyCode.DOWN) {
            player.setY(player.getY() + (PLAYER_SPEED - 5));
        } else if (e.getCode() == KeyCode.B) {
            bullets.get(0).changemode();
        } else if (e.getCode() == KeyCode.V) {
            bullets.get(0).changeammo();
        } else if (e.getCode() == KeyCode.E) {
            gameOver();
        }

        // 枠外に行かないようにする
        if (player.getX() < 20) {
            player.setX(20);
        } else if (player.getX() > WIDTH - 20) {
            player.setX(WIDTH - 20);
        }
        if (player.getY() < 50) {
            player.setY(50);
        } else if (player.getY() > HEIGHT - 60) {
            player.setY(HEIGHT - 60);
        }
    }

    // ゲーム終了時のキーボード設定
    private void handleGameOverInput(KeyEvent e, Stage primaryStage) {
        if (sendScore < score)
            sendScore = score;
        if (e.getCode() == KeyCode.R) {
            restartGame();
        } else if (e.getCode() == KeyCode.E) {
            exitGame(primaryStage);
        }
    }

    // ゲームのリスタートメソッド
    private void restartGame() {
        gameStarted = true;
        gameLoop.stop();

        // ゲームの実行を開始する処理を呼び出す
        startGame();
    }

    // ゲームスタート時の画面
    private void renderStartScreen() {
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
        gc.fillText("Press SPACE to start", WIDTH / 2 - 120, HEIGHT / 2);
    }

    // ゲームの実行メソッド
    private void startGame() {
        // 初期化
        mapData = new MapData(rawMapData);
        initializeGame();
        gameStarted = true;
        running = true;
        startTime = System.nanoTime();

        // ゲーム内のアニメーションスタート
        gameLoop = new AnimationTimer() {
            public void handle(long currentNanoTime) {
                update();
                render();
            }
        };
        gameLoop.start();
    }

    // ゲーム内の各オブジェクトの初期設定
    private void initializeGame() {
        // スコアの初期化
        score = 0;
        // クリア判定の初期化
        clear = 0;

        // 自機の宣言
        player = new Invader("player.png", (WIDTH / 2), (HEIGHT - 60), 30);

        // 敵機の宣言
        invaders = new ArrayList<>();
        invadersDestroyed = new ArrayList<>();
        for (int i = 0; i < NUMS_INVADERS.length; i++) {
            invaders.add(new ArrayList<Invader>());
            invadersDestroyed.add(new ArrayList<Boolean>());
            int column = mapData.getStageData(i);
            int row = NUMS_INVADERS[i] / column;
            int indexX = 1;
            for (int j = 0; j < row; j++) {
                double invaderX = 0.0;
                double invaderY = 0.0;
                int indexY = 1;
                for (int k = 0; k < column; k++) {
                    invaderX = (double) (WIDTH / (row + 1)) * (double) indexX;
                    invaderY = k + 1 + indexY * 40;
                    invaders.get(i).add(new Invader(IMAGES[i], invaderX, invaderY, (i + 1)));
                    invadersDestroyed.get(i).add(false);
                    indexY++;
                }
                indexX++;
            }
        }

        // ボスを宣言
        boss = new Invader("boss.png", 0, 50, 20);

        // 爆発エフェクトの宣言
        explosions = new ArrayList<>();

        // 自機の打つ弾を宣言
        bullets = new ArrayList<>();
        for (int i = 0; i < NUM_BULLETS; i++) {
            bullets.add(new Bullet(0, -1000));
        }
    }

    // ゲーム中の各オブジェクトの座標の更新
    private void update() {
        // 終了しているならreturn
        if (!running) {
            return;
        }

        int currentTIme = (int) ((System.nanoTime() - startTime) / 1_000_000_000);
        // 各敵機についてフレームごとに敵機を下に動かす
        for (int i = 0; i < NUMS_INVADERS.length; i++) {
            for (int j = 0; j < NUMS_INVADERS[i]; j++) {
                Invader invader = invaders.get(i).get(j);
                Boolean invaderDestroyed = invadersDestroyed.get(i).get(j);
                if (currentTIme >= i * 10) {
                    invader.moveY((i == 4) ? INVADER_SPEED_Y / 2 : INVADER_SPEED_Y);
                    // 一番下まで行ったら強制的に破壊
                    if (invader.getY() >= (HEIGHT - 50)) {
                        invadersDestroyed.get(i).set(j, true);
                    }
                }

                // 弾と敵機が衝突したか判定
                for (int k = 0; k < NUM_BULLETS; k++) {
                    Bullet bullet = bullets.get(k);
                    if (checkCollision(invader, bullet)) {
                        if (!invaderDestroyed && currentTIme >= i * 10) {
                            createExplosion(invader.getX(), invader.getY()); // 爆発エフェクトを出す
                            bullet.hit();// 死んでる敵の当たり判定も残っている
                            // 敵機の体力が0になったら破壊判定をtrueにして、スコアを100追加
                            if (invader.getHealth() <= 0) {
                                score += 100 * bullet.getamode();
                                invadersDestroyed.get(i).set(j, true);
                                break;
                            }
                            break;
                        }
                    }
                }

                // 自機と敵機の衝突判定
                if (!invaderDestroyed) {
                    if (damaged(invader)) {
                        createExplosion(player.getX(), player.getY()); // 爆発エフェクトを出す
                        // 自機の体力が0になったらゲームオーバー
                        if (player.getHealth() == 0) {
                            gameOver();
                            break;
                        }
                    }
                }
            }
        }

        // ボスの動き
        if (!bossDestroyed && currentTIme >= 42) {
            // 左端に行ったら折り返す
            if (boss.getX() >= WIDTH) {
                boss.moveX(-INVADER_SPEED_X / 2);
                bossdirection = -1;
            }
            // 右端に行ったら折り返す
            else if (boss.getX() <= 0) {
                boss.moveX(INVADER_SPEED_X / 2);
                bossdirection = 1;
            }
            // それ以外は直前の動きに依存
            else {
                boss.moveX(INVADER_SPEED_X * bossdirection / 2);
            }
        }

        // 弾とボスの衝突判定
        for (int k = 0; k < NUM_BULLETS; k++) {
            Bullet bullet = bullets.get(k);
            if (checkCollision(boss, bullet)) {
                if (!bossDestroyed) {
                    createExplosion(boss.getX(), boss.getY()); // 爆発エフェクトを出す
                    bullet.hit();// 死んでる敵の当たり判定も残っている
                    // 敵機の体力が0になったら破壊判定をtrueにして、スコアを100追加
                    if (boss.getHealth() <= 0) {
                        score += 500;
                        bossDestroyed = true;
                        gameClear();
                    }
                }
            }
        }
    }

    // 球と敵機の衝突メソッド
    private boolean checkCollision(Invader invader, Bullet bullet) {
        double distance = Math
                .sqrt(Math.pow(invader.getX() - bullet.getX(), 2) + Math.pow(invader.getY() - bullet.getY(), 2));
        if (distance < 30) { // 衝突判定の閾値を設定
            invader.hit(invader.getHealth() - bullet.getdamage()); // 体力を1減らす
            return true;
        }
        return false;
    }

    // 自機と敵機の衝突メソッド
    private boolean damaged(Invader invader) {
        double distance = Math
                .sqrt(Math.pow(player.getX() - invader.getX(), 2) + Math.pow(player.getY() - invader.getY(), 2));
        if (distance < 15) { // 衝突判定の閾値を設定
            player.hit(player.getHealth() - 1); // 体力を1減らす
            return true;
        }
        return false;
    }

    // 爆発エフェクトを出す座標の設定
    private void createExplosion(double x, double y) {
        explosions.add(new Explosion(x, y));
    }

    // ランキングを表示するメソッド
    private void displayRanking() {
        Collections.sort(ranking, Collections.reverseOrder());
        gc.fillText("----- Ranking -----", WIDTH / 2 - 130, HEIGHT / 2 - 120);
        int i = 0;
        for (i = 0; i < ranking.size(); i++) {
            if (i >= 5)
                break; // とりあえず上位5くらいまで
            ScoreEntry entry = ranking.get(i);
            if (entry.getName().equals(name)) {
                if (newScore) {
                    // 新記録ならnew
                    gc.fillText("new " + (i + 1) + ". " + entry.getName() + " " + entry.getScore(), WIDTH / 2 - 110,
                            HEIGHT / 2 - 80 + i * 40);
                } else {
                    // 記録更新してないなら、プレイヤーの最高得点を表示
                    gc.fillText("-> " + (i + 1) + ". " + entry.getName() + " " + entry.getScore(), WIDTH / 2 - 110,
                            HEIGHT / 2 - 80 + i * 40);
                }
            } else {
                // プレイヤー以外の得点
                gc.fillText((i + 1) + ". " + entry.getName() + " " + entry.getScore(), WIDTH / 2 - 110,
                        HEIGHT / 2 - 80 + i * 40);
            }
        }
        gc.fillText("--------------------", WIDTH / 2 - 130, HEIGHT / 2 - 80 + i * 40);
    }

    // ゲーム進行中と終了時の画面の描画
    private void render() {
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        if (!running) { // 終了時
            // フォント設定
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Verdana", FontWeight.BOLD, 48));

            // クリアかゲームオーバーかの結果を表示
            if (clear == 1) {
                gc.fillText("Game Clear", WIDTH / 2 - 150, HEIGHT / 2 - 200);
            } else {
                gc.fillText("Game Over", WIDTH / 2 - 150, HEIGHT / 2 - 200);
            }

            // プレイ結果の名前とスコアを表示
            gc.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
            gc.fillText("Name: " + name + "  Score: " + score, WIDTH / 2 - 150, HEIGHT / 2 - 160);

            // ランキングの表示
            displayRanking();

            // 終了かリスタートをするかの案内の表示
            gc.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
            gc.fillText("Press E to exit    Press R to restart", WIDTH / 2 - 180, HEIGHT / 2 + 140);
            return;
        } else { // 実行時
            // 球の描画(青)
            for (Bullet bullet : bullets) {
                bullet.draw(gc);
            }

            // 自機の描画(緑)
            Image playerImage = new Image("player.png");
            gc.fillRect(player.getX() - 25, player.getY() - 12.5, 50, 25);
            gc.drawImage(playerImage, player.getX() - 25, player.getY() - 12.5, 50, 25);

            // 敵機の描画
            int currentTIme = (int) ((System.nanoTime() - startTime) / 1_000_000_000);
            for (int i = 0; i < NUMS_INVADERS.length; i++) {
                if (currentTIme >= i * 10) {
                    List<Invader> invaderList = invaders.get(i);
                    List<Boolean> booleanList = invadersDestroyed.get(i);
                    for (int j = 0; j < NUMS_INVADERS[i]; j++) {
                        Boolean invaderDestroyed = booleanList.get(j);
                        if (!invaderDestroyed) {
                            Invader invader = invaderList.get(j);
                            invader.updateFrame(gc);
                        }
                    }
                }
            }

            // FinalStageのときにボス表示
            if (!bossDestroyed && currentTIme >= 42)
                boss.updateFrame(gc);

            // 下部の説明等を表示
            bullets.get(0).showmode(gc);
            showBottomBar(gc, currentTIme, player.getHealth());

        }

        // 爆発エフェクトがあるなら
        for (Explosion explosion : explosions) {
            explosion.draw(gc);
        }
    }

    // 下部の表示するゲーム内の情報を出力
    private void showBottomBar(GraphicsContext gc, int currentTIme, int health) {
        // 経過時間によってステージの表示を変える
        String stage = "0";
        if (currentTIme < 10)
            stage = "1";
        else if (currentTIme < 20)
            stage = "2";
        else if (currentTIme < 30)
            stage = "3";
        else if (currentTIme < 40)
            stage = "4";
        else
            stage = "Final";

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        gc.fillText(
                ("Name: " + name + "   Time: " + currentTIme + "   Stage: " + stage + "   Score: " + score
                        + "   Health: " + health),
                10,
                HEIGHT - 30);
        gc.fillText(
                "↑: up,  ↓: down,  →: right,  ←: left,  Space: shot,  B: change mode,  V: change strength,  E: exit game",
                10, HEIGHT - 10);
    }

    // ゲームオーバ時の処理
    private void gameOver() {
        score = 0;
        updateRanking(ranking, name, score);

        running = false;
    }

    // ゲームクリア時の処理
    private void gameClear() {
        score = score + 1000 - (int) ((System.nanoTime() - startTime) / 1_000_000_000) * 10;
        updateRanking(ranking, name, score);
        clear = 1;

        running = false;
    }

    // ゲームを終了時の処理
    private void exitGame(Stage primaryStage) {
        primaryStage.close();
    }

    public static void main(String[] args) throws IOException {
        // 引数が合っているかの確認
        if (args.length != 1) {
            System.err.println("Usage: InvadersGameClient <name>");
            System.exit(1);
        }

        // サーバー接続準備
        final String hostname = "localhost";
        final int port = 8080;
        name = args[0];
        InetAddress addr = InetAddress.getByName(hostname);
        System.out.println("addr = " + addr);
        Socket socket = new Socket(addr, port);

        // プレイ用ゲームのインスタンスを生成
        InvadersGameClient game = new InvadersGameClient();

        try {
            // ソケットを作成し、サーバーに接続
            System.out.println("socket = " + socket);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),
                    true);

            // 名前の送信
            out.println(name);

            // マップデータの取得
            for (int i = 0; i < 5; i++) {
                rawMapData[i] = Integer.parseInt(in.readLine());
            }

            // ゲーム開始可能の合図を受信
            String startMs = in.readLine();
            System.out.println(startMs);

            // ランキングデータの受信
            int oldRanking = Integer.parseInt(in.readLine());
            String nameData = "null";
            int scoreData = 0;
            for (int i = 0; i < oldRanking * 2; i++) {
                String response = in.readLine();
                // name->scoreの順で送られてくる
                if (i % 2 == 0) {
                    nameData = response;
                } else {
                    scoreData = Integer.parseInt(response);
                    ranking.add(new ScoreEntry(nameData, scoreData));
                }
            }

            // ゲーム処理
            game.launch(args);

            // プレイの最高得点を送信
            out.println(sendScore);

            // 今回の最高プレイ結果を出力
            String resultUser = in.readLine();
            System.out.println(resultUser);
            String resultScore = in.readLine();
            System.out.println(resultScore);

            out.println("END"); // 終了を示すラベルの送信

            // 終了したら閉じる
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
                if (entry.getScore() < score) {
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