# 2023_Invaders_Game

以下の手順で実行できる。
1. vscodeのReferenceにJavafxのlib/ファイル.jarを全部入れる
2. javac ScoreEntry.java
3. java ScoreEntry
4. javac InvadersGameServer.java
5. java InvadersGameServer <PORT>

<Mac版>
6. javac --module-path /Users/<username>/<javafxディレクトリ>/lib/ --add-modules javafx.controls,javafx.fxml InvadersGameClient.java
7. java --module-path /Users/<username>/<javafxディレクトリ>/lib/ --add-modules javafx.controls,javafx.fxml InvadersGameClient localhost <PORT> <NAME>
<Windowsg版>
6. javac --module-path --module-path \"C:\\<javafxディレクトリ>\\lib\" --add-modules javafx.controls,javafx.fxml InvadersGameClient.java
7. java --module-path \"C:\\<javafxディレクトリ>\\lib\" --add-modules javafx.controls,javafx.fxml InvadersGameClient localhost <PORT> <NAME>
