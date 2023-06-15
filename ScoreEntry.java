public class ScoreEntry implements Comparable<ScoreEntry> {
    private String name;
    private int score;

    public ScoreEntry(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public int compareTo(ScoreEntry other) {
        // スコアの昇順で比較
        return Integer.compare(this.score, other.score);
    }
}