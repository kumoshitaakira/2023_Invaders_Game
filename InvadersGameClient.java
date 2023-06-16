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
    private static final int INVADER_SPEED = 4;
    private static final int GAME_CLEAR_DELAY = 10;
    private static final int NUM_INVADERS = 20;

    // 描画に関わるものの宣言
    private Canvas canvas;
    private GraphicsContext gc;

    // ゲームの実行に関するフラグの宣言
    private boolean running = true;
    private boolean gameStarted = false;

    // ゲームの終了に用いる変数とフラグの宣言
    private long startTime;
    private boolean gameClearTriggered = false;

    // 自機１つ・敵機複数(破壊判定付き)・球1つの宣言
    private Invader player;
    private List<Invader> invaders;
    private boolean[] invaderDestroyed;
    private double bulletX = WIDTH * 2;
    private double bulletY = 0;

    // 爆発のエフェクト用リストの宣言
    private List<Explosion> explosions;

    // クリア判定用の整数を宣言
    private int clear = 0;
    
    // 取得したスコアと名前と送信用スコアの宣言
    private static int score = 0;
    private static int sendScore = 0;
    private static String name;

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
            } else if(running) {
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

        //最初のスタート画面
        renderStartScreen();
    }

    // ゲームプレイ時のキーボド設定
    private void handleGameInput(KeyEvent e) {
        if (e.getCode() == KeyCode.LEFT) {
            player.setX(player.getX() - 10);
        } else if (e.getCode() == KeyCode.RIGHT) {
            player.setX(player.getX() + 10);
        } else if (e.getCode() == KeyCode.SPACE) {
            shoot();
        } else if (e.getCode() == KeyCode.UP) {
            player.setY(player.getY() - 10);
        } else if (e.getCode() == KeyCode.DOWN) {
            player.setY(player.getY() + 10);
        }

        // 枠外に行かないようにする
        if (player.getX() < 0) {
            player.setX(0);
        } else if (player.getX() > WIDTH) {
            player.setX(WIDTH);
        }
        if (player.getY() < 0) {
            player.setY(0);
        } else if (player.getY() > HEIGHT) {
            player.setY(HEIGHT);
        }
    }

    // ゲーム終了時のキーボード設定
    private void handleGameOverInput(KeyEvent e, Stage primaryStage) {
        if(sendScore < score) sendScore = score;
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
        initializeGame();
        gameStarted = true;
        running = true;
        startTime = System.nanoTime();
        
        // ゲーム内のアニメーションスタート
        gameLoop = new AnimationTimer() {

            public void handle(long currentNanoTime) {
                // 経過時間の計測 --ボスが実装できたらここは消す予定
                long elapsedTime = (currentNanoTime - startTime) / 1_000_000_000; // 経過時間（秒)
                if (elapsedTime >= GAME_CLEAR_DELAY && !gameClearTriggered) {
                    gameClearTriggered = true;
                }

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

        // 球の座標初期化
        bulletX = WIDTH * 2;
        bulletY = 0;

        // 自機の宣言
        player = new Invader((WIDTH / 2), (HEIGHT - 50), 20);

        // 敵機の宣言
        invaders = new ArrayList<>();
        invaderDestroyed = new boolean[NUM_INVADERS];
        // 今は座標をランダムにしているが、今後の実装ではパターン化させる予定
        for (int i = 0; i < NUM_INVADERS; i++) {
            Random random = new Random();
            double invaderX = WIDTH / 2 + ((i % 10) - NUM_INVADERS / 2) * random.nextInt(51);
            random = new Random();
            double invaderY = ((i % 10) + 1) * random.nextInt(10) * 10 + NUM_INVADERS;
            invaders.add(new Invader(invaderX, invaderY, 3));
            invaderDestroyed[i] = false;
        }

        // ゲームクリアのトリガーの初期化
        gameClearTriggered = false;

        // 爆発エフェクトの宣言
        explosions = new ArrayList<>();
    }

    // ゲーム中の各オブジェクトの座標の更新
    private void update() {
        // 終了しているならreturn
        if (!running) {
            return;
        }

        // 各敵機についてフレームごとに敵機を左に動かす
        for (int i = 0; i < NUM_INVADERS; i++) {
            Invader invader = invaders.get(i);
            if (!invaderDestroyed[i]) {
                invader.move(INVADER_SPEED);
                // 左端に行ったら一段下げて右端から再スタート
                if (invader.getX() >= WIDTH) {
                    invader.setX(0);
                    invader.setY(invader.getY() + 50);
                    // 一番下まで行ったら上に戻す
                    if (invader.getY() >= HEIGHT) {
                        invader.setY(0);
                    }
                }
            }

            // 球と敵機が衝突したか判定
            if (checkCollision(invader)) {
                if (!invaderDestroyed[i]) {
                    createExplosion(invader.getX(), invader.getY()); // 爆発エフェクトを出す
                    // 敵機の体力が0になったら破壊判定をtrueにして、スコアを100追加
                    if(invader.getHealth() == 0) {
                        score += 100;
                        invaderDestroyed[i] = true;
                    }
                }
                // ゲームクリアならクリア画面へ
                if (gameClearTriggered) {
                    gameClear();
                }
            }

            // 自機と敵機の衝突判定
            if(damaged(invader)) {
                createExplosion(player.getX(), player.getY()); // 爆発エフェクトを出す
                // 自機の体力が0になったらゲームオーバー
                if(player.getHealth() == 0) {
                    gameOver();
                    break;
                }
            }
        }

        // ゲームクリアならクリア画面へ
        if(gameClearTriggered) gameClear();

        // ゲーム進行中、制限時間に達したらゲームクリアへ
        if (!gameClearTriggered && ((System.nanoTime() - startTime) / 1_000_000_000) >= GAME_CLEAR_DELAY) {
            gameClearTriggered = true;
        }
    }

    // 球と敵機の衝突メソッド
    private boolean checkCollision(Invader invader) {
        double distance = Math.sqrt(Math.pow(invader.getX() - bulletX, 2) + Math.pow(invader.getY() - bulletY, 2));
        if (distance < 30) {  // 衝突判定の閾値を設定
            invader.hit(invader.getHealth() - 1);  // 体力を1減らす
            return true;
        }
        return false;
    }

    // 自機と敵機の衝突メソッド
    private boolean damaged(Invader invader) {
        double distance = Math.sqrt(Math.pow(player.getX() - invader.getX(), 2) + Math.pow(player.getY() - invader.getY(), 2));
        if (distance < 10) {  // 衝突判定の閾値を設定
            player.hit(player.getHealth() - 1);  // 体力を1減らす
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
        for(i = 0; i < ranking.size(); i++) {
            if(i >= 5) break; // とりあえず上位5くらいまで
            ScoreEntry entry = ranking.get(i);
            if(entry.getName().equals(name)) {
                if(newScore) {
                    // 新記録ならnew
                    gc.fillText("new " + (i + 1) + ". " + entry.getName() + " " + entry.getScore(), WIDTH / 2 - 110, HEIGHT / 2 - 80 + i * 40);
                } else {
                    // 記録更新してないなら、プレイヤーの最高得点を表示
                    gc.fillText("-> " + (i + 1) + ". " + entry.getName() + " " + entry.getScore(), WIDTH / 2 - 110, HEIGHT / 2 - 80 + i * 40);
                }
            } else {
                // プレイヤー以外の得点
                gc.fillText((i + 1) + ". " + entry.getName() + " " + entry.getScore(), WIDTH / 2 - 110, HEIGHT / 2 - 80 + i * 40);
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
            if (clear == 1){
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
            gc.fillText("Press E to exit    Press R to restart", WIDTH / 2 - 120, HEIGHT / 2 + 160);
            return;
        } else { // 実行時
            // 自機の描画(緑)
            gc.setFill(Color.GREEN);
            gc.fillRect(player.getX() - 25, player.getY() - 12.5, 50, 25);
            
            // 敵機の描画(赤)
            for (int i = 0; i < NUM_INVADERS; i++) {
                if (!invaderDestroyed[i]) {
                    Invader invader = invaders.get(i);
                    gc.setFill(Color.RED);
                    gc.fillRect(invader.getX() - 25, invader.getY() - 25, 50, 25);
                }
            }
            
            // 球の描画(青)
            gc.setFill(Color.BLUE);
            bulletY = bulletY - 10;
            gc.fillOval(bulletX, bulletY, 50, 20);
        }

        // 爆発エフェクトがあるなら
        for (Explosion explosion : explosions) {
            explosion.draw(gc);
        }
    }

    // 球を出すメソッド
    private void shoot() {
        bulletX = player.getX() - 25;
        bulletY = player.getY() - 25;
    }

    // ゲームオーバ時の処理
    private void gameOver() {
        score -= 100;
        updateRanking(ranking, name, score);

        running = false;
    }

    // ゲームクリア時の処理
    private void gameClear(){
        score += 100;
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
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            // 名前の送信
            out.println(name);

            //　ゲーム開始可能の合図を受信
            String startMs = in.readLine();
            System.out.println(startMs);

            // ランキングデータの受信
            int oldRanking = Integer.parseInt(in.readLine());
            String nameData = "null";
            int scoreData = 0;
            for (int i = 0; i < oldRanking * 2; i++) {
                String response = in.readLine();
                // name->scoreの順で送られてくる
                if(i % 2 == 0) {
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
            String  resultScore = in.readLine();
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
