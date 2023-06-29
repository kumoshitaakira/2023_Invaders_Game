public class MapData {
    private int[] mapData;

    public MapData(int[] data) {
        this.mapData = data;
    }

    int getStageData(int i) {
        switch (i) {
            case 0:
                return getStage1();
            case 1:
                return getStage2();
            case 2:
                return getStage3();
            case 3:
                return getStage4();
            case 4:
                return getStage5();
            default:
                return getStage1();
        }
    }

    // 1 * 6 か 2 * 3 の選択
    int getStage1() {
        switch (mapData[0] / 5) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 2;
            default:
                return 1;
        }
    }

    // 1 * 10 か 2 * 5 の選択
    int getStage2() {
        switch (mapData[1] / 3) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 1;
            case 3:
                return 2;
            default:
                return 1;
        }
    }

    // 2 * 10 か 4 * 5 か 5 * 4 の選択
    int getStage3() {
        switch (mapData[1] / 4) {
            case 0:
                return 2;
            case 1:
                return 4;
            case 2:
                return 5;
            case 3:
                return 5;
            default:
                return 2;
        }
    }

    // 3 * 10 か 5 * 6 か 6 * 5 の選択
    int getStage4() {
        switch (mapData[1] % 3) {
            case 0:
                return 3;
            case 1:
                return 5;
            case 2:
                return 6;
            default:
                return 6;
        }
    }

    // 4 * 10 か 5 * 8 か 8 * 5 の選択
    int getStage5() {
        switch (mapData[1] % 4) {
            case 0:
                return 4;
            case 1:
                return 5;
            case 2:
                return 8;
            case 3:
                return 4;
            default:
                return 5;
        }
    }
}
