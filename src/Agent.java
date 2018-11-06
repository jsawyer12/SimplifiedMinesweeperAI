public class Agent {

    private String[][] displMap;
    private int lives;
    private int cellsLeft;

    private boolean tryMarking(int x, int y) {
        boolean marked = false;
        int clueCnt = Integer.parseInt(displMap[y][x]); // gets number of adjacent daggers
        int daggers = 0; // number of daggers marked or stabbed by
        int unmarkedCount = 0; // gets number of unmarked positions
        int[] stspCoords = getStartStopCoords(x, y);
        for (int k = stspCoords[0]; k <= stspCoords[1]; k++) {
            for (int l = stspCoords[2]; l <= stspCoords[3]; l++) {
                if (displMap[k][l] == "+") {
                    unmarkedCount++;
                }
                if (displMap[k][l] == "d" || displMap[k][l] == "D") {
                    daggers++;
                }
            }
        }
        if (unmarkedCount == clueCnt - daggers) {
            marked = true;
            for (int k = stspCoords[0]; k <= stspCoords[1]; k++) {
                for (int l = stspCoords[2]; l <= stspCoords[3]; l++) {
                    if (displMap[k][l] == "+") {
                        displMap[k][l] = "D";
                    }
                }
            }
            System.out.println("*************");
            printWorld();
            System.out.println("*************");
        }
        return marked;
    }

    private boolean findMarkedNeighbors(int x, int y) {
        boolean found = false;
        int[] stspCoords = getStartStopCoords(x, y);
        for (int k = stspCoords[0]; k <= stspCoords[1]; k++) {
            for (int l = stspCoords[2]; l <= stspCoords[3]; l++) {
                if (displMap[k][l] != "D" && displMap[k][l] != "d" && displMap[k][l] != "g" && displMap[k][l] != "+") { // if a number
//                    System.out.println("trying to mark " +displMap[k][l] +" at " +l +"," +k);
                    tryMarking(l, k);
                }
            }
        }
        return found;
    }

    private boolean allFree(int x, int y) {
        boolean allFree = false;
        int count = 0;
        int[] stspCoords = getStartStopCoords(x, y);
        for (int k = stspCoords[0]; k <= stspCoords[1]; k++) {
            for (int l = stspCoords[2]; l <= stspCoords[3]; l++) {
//                System.out.println("looking at "+displMap[k][l] +" at "+l +","+k);
                if (displMap[k][l] == "+") {
                    count++;
                }
            }
        }
        if (count == 1)
            allFree = true;
        return allFree;
    }

    private int[] findATS() {
        int[] coords = new int[2];

        return coords;
    }

    public int[] singlePointStrategy() {
        int[] coords = new int[2];
        boolean sPFound = false;

        for (int i = 0; i < displMap.length; i++) {
            for (int j = 0; j < displMap.length; j++) {
                if (displMap[i][j] == "+") {
                    findMarkedNeighbors(j, i);
                    if (allFree(j, i)) {
                        coords[0] = i;
                        coords[1] = j;
                        sPFound = true;
                        break;
                    }
                }
            }
        }
        boolean aTSFound = false;
        if (!sPFound) {
            coords = findATS();
        }
        if (!aTSFound)
            coords = randomProbingStrategy();
        return coords;
    }

    public int[] randomProbingStrategy() {
        int[] coords = new int[2];
        boolean coordsAreGood = false;
        while (!coordsAreGood) {
            coords[0] = (int) (Math.random() * displMap.length);
            coords[1] = (int) (Math.random() * displMap.length);
            if (displMap[coords[0]][coords[1]] == "+")
                coordsAreGood = true;
        }
        return coords;
    }

    public  int[] getStartStopCoords(int x, int y) {
        int[] stspCoords = new int[4]; // [startY, endY, startX, endX]
        stspCoords[0] = y - 1;
        stspCoords[1] = y + 1;
        if (stspCoords[0] < 0)
            stspCoords[0] = 0;
        if (stspCoords[1] >= displMap.length)
            stspCoords[1] = displMap.length - 1;
        stspCoords[2] = x - 1;
        stspCoords[3] = x + 1;
        if (stspCoords[2] < 0)
            stspCoords[2] = 0;
        if (stspCoords[3] >= displMap.length)
            stspCoords[3] = displMap.length - 1;
        return stspCoords;
    }

    public void adjustMap(int[] coords, String val) {
        if (displMap[coords[0]][coords[1]] == "+") { // if move on new position
            if (val == "g")
                lives++;
            else if (val == "d") {
                lives--;
                cellsLeft++; // more elegant solution later, but basically doesn't remove cell from cell left if on dagger
            }
            displMap[coords[0]][coords[1]] = val;
            cellsLeft--;
        }
    }

    public String getCell(int[] coords) {
        return displMap[coords[0]][coords[1]];
    }

    public void adjustLives(int val) { // val here is value of change (-1 to remove life or 1 to add)
        lives += val;
    }

    public int getLives() {
        return lives;
    }

    public int getCellsLeft() {
        return cellsLeft;
    }

    public void printWorld() {
        System.out.print("   ");
        for (int a = 0; a < displMap.length; a++) {
            System.out.print(" " +a);
        }
        System.out.println();
        System.out.print("   ");
        for (int a = 0; a < displMap.length; a++) {
            System.out.print("__");
        }
        System.out.println();
        for (int i = 0; i < displMap.length; i++) {
            System.out.print(i +" |");
            for (int j = 0; j < displMap.length; j++) {
                System.out.print(" " +displMap[i][j]);
            }
            System.out.println();
        }
    }

    public Agent (int mapLength) {
        this.displMap =  new String[mapLength][mapLength];
        for (int i = 0; i < mapLength; i++) {
            for (int j = 0; j < mapLength; j++) {
                displMap[i][j] = "+";
            }
        }

        this.cellsLeft = mapLength * mapLength;
        if (mapLength == 5)
            cellsLeft -= 4;
        else if (mapLength == 8)
            cellsLeft -= 10;
        else if (mapLength == 12)
            cellsLeft -= 25;
        this.lives = 1;
    }
}
