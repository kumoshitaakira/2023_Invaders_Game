import java.io.*;
import java.net.*;
import java.util.*;

public class InvadersGameServer {
    public static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started: " + serverSocket);

        List<ScoreEntry> scoreList = new ArrayList<>(); // スコアを保存するリスト

        while(true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Connection accepted: " + clientSocket);

            Thread cliThread = new Thread(() -> {
                try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);

                // 名前とスコアの受信
                String name = in.readLine();
                System.out.println("Name: " + name);
                out.println("インベーダーゲーム");

                sendRankingData(out, scoreList);
                
                int score = Integer.parseInt(in.readLine());
                System.out.println("Score: " + score);

                // ランキングの更新
                updateScoreList(scoreList, name, score);

                // 受信した名前とスコアをクライアントに送信
                out.println("Name: " + name);
                out.println("Score: " + score);

                out.println("ゲーム終了"); // 終了を示すためのラベル

                System.out.println("Data sent to client.");

            }catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            });

            cliThread.start();
        }
    }

    private static void updateScoreList(List<ScoreEntry> scoreList, String name, int score) {
        // 同じ名前のエントリーが存在するかチェック
        for (ScoreEntry entry : scoreList) {
            if (entry.getName().equals(name)) {
                // スコアが新記録なら上書きして終了
                if(entry.getScore() < score) entry.setScore(score);
                return;
            }
        }

        // 新しいエントリーとして追加
        scoreList.add(new ScoreEntry(name, score));
    }

    private static void sendRankingData(PrintWriter out, List<ScoreEntry> scoreList) {
        // スコアを降順にソート
        Collections.sort(scoreList, Collections.reverseOrder());
        
        out.println(scoreList.size());
        // ランキングデータをnameとscoreの二行で送信
        for (int i = 0; i < scoreList.size(); i++) {
            ScoreEntry entry = scoreList.get(i);
            out.println(entry.getName());
            out.println(entry.getScore());
        }
    }
}
