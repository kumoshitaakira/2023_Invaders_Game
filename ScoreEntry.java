public class ScoreEntry implements Comparable<ScoreEntry> {
    private String name;
    private int score;

    // 名前とスコアを持ったインスタンスの生成
    public ScoreEntry(String name, int score) {
        this.name = name;
        this.score = score;
    }

    // 名前を返す
    public String getName() {
        return name;
    }

    // スコアを返す
    public int getScore() {
        return score;
    }

    // スコアを更新する
    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public int compareTo(ScoreEntry other) {
        // スコアの昇順で比較
        return Integer.compare(this.score, other.score);
    }
}