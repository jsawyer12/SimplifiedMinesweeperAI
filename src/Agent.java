import org.logicng.datastructures.Tristate;
import org.logicng.formulas.Formula;
import org.logicng.formulas.FormulaFactory;
import org.logicng.io.parsers.ParserException;
import org.logicng.io.parsers.PropositionalParser;
import org.logicng.solvers.MiniSat;
import org.logicng.solvers.SATSolver;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

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













    //code modified from https://ide.geeksforgeeks.org/index.php
    //given an arraylist of vells containing pluses:
    static ArrayList<String> shitGold(int arr[][], int n, int r, int index, int data[][], int i, ArrayList<String> clauses) {
        // Current combination is ready to be printed, print it
        if (index == r) {
            String clause = "";
            for (int j = 0; j < arr.length; j++) {
                boolean chosenOne = false;
                for (int k = 0; k < r; k++) {
                    if (data[k][1] == arr[j][1] && data[k][0] == arr[j][0])
                        chosenOne = true;
                }
                if (!chosenOne) {
//                    System.out.print("~");
                    clause += "~";
                }
                clause += "D"+arr[j][0]+arr[j][1]+" ";
//                System.out.print("D"+arr[j][0]+arr[j][1]+" ");
                if (j != arr.length - 1) {
//                    System.out.print("& ");
                    clause += "& ";
                }
            }
//            System.out.println("|");
//            clause += "| ";
//            for (int j=0; j<r; j++)
//                System.out.print("["+data[j][1]+","+data[j][0]+"] ");
//            System.out.println();
            clauses.add(clause);
            return clauses;
        }

        // When no more elements are there to put in data[]
        if (i >= n)
            return clauses;

        // current is included, put next at next location
        data[index][0] = arr[i][0];
        data[index][1] = arr[i][1];
        shitGold(arr, n, r, index+1, data, i+1, clauses);

        // current is excluded, replace it with next (Note that
        // i+1 is passed, but index is not changed)
        shitGold(arr, n, r, index, data, i+1, clauses);
        return clauses;
    }



    private String makeRules(int x, int y, int adjDags) {
        int[] stspCoords = getStartStopCoords(x, y);
        String rule = "";
        ArrayList<int[]> covCells = new ArrayList<>(); // x, y adjacent covered cell locations
        ArrayList<int[]> dagCells = new ArrayList<>(); // x, y known adjacent dagger cell locations
        int flagNStabDags = 0; //number of flagged daggers and dagger stabbed by already aka "D" and "d"
//        int coveredCells = 0;
        for (int a = stspCoords[0]; a <= stspCoords[1]; a++) {
            for (int b = stspCoords[2]; b <= stspCoords[3]; b++) {
                if (displMap[a][b] == "+") {
                    covCells.add(new int[] {b, a});
//                    coveredCells++;
                }
                if (displMap[a][b] == "d" && displMap[a][b] == "D") {
                    flagNStabDags++;
                    dagCells.add(new int[] {b, a});
                }
            }
        }
        // if number of daggers adjacent to number cell is less than the number of flagged
        // daggers and uncovered daggers, there are still daggers to uncover out there!
        // LET'S DO SOME PROPOSITIONAL LOGIC!!!
        if (adjDags > flagNStabDags) {
            int dagsLeft = adjDags - flagNStabDags;

            // get all the possible or clauses for the pluses
            ArrayList<String> clauses = new ArrayList<>();
            int[][] covCellsArr = covCells.toArray(new int[covCells.size()][2]);
//            System.out.print("covCells from [" +x +"," +y +"]: ");
//            for (int t = 0; t < covCellsArr.length; t++) {
//                System.out.print("[" +covCellsArr[t][1] +"," +covCellsArr[t][0] +"]");
//            }
            System.out.println();
            int[][] possibleCombos = new int[covCells.size()][2];
            clauses = shitGold(covCellsArr, covCells.size(), dagsLeft, 0, possibleCombos, 0, clauses);
            for (int oh = 0; oh < clauses.size(); oh++) {
//                System.out.print(clauses.get(oh) +" ");
                rule += clauses.get(oh);
                if (oh != clauses.size() - 1) {
//                    System.out.print("| ");
                    rule += "| ";
                }
            }
//            System.out.println(rule);
            // then add the definite daggers to the mix
        }
        return rule;
    }

    private boolean satisfiedHere(int x, int y) { // find clues surrounding covered cell
        boolean satisfied = false;
        String rules = "";
        int[] stspCoords = getStartStopCoords(x, y);
        for (int k = stspCoords[0]; k <= stspCoords[1]; k++) {
            for (int l = stspCoords[2]; l <= stspCoords[3]; l++) {
                try {
                    int numAdjDags = Integer.parseInt(displMap[k][l]);
                    if (numAdjDags > 0) {
                        rules += makeRules(l,k, numAdjDags);
                        if (k != stspCoords[1] && l != stspCoords[3])
                            rules += "| ";
                    }
                } catch(Exception e) {

                }
            }
        }

        rules += "& ~D" +x +y;
        System.out.println(rules);

        final FormulaFactory f = new FormulaFactory();
        final PropositionalParser p = new PropositionalParser(f);
        try {
            final Formula formula = p.parse("(D22 & ~D23) | (~D22 & D23) & ((D22 & ~D23 & ~D24) | "
                    +"(~D22 & D23 & ~D24) | (~D22 & ~D23 & D24)) & ((~D22 & ~D24) | (~D23 & D24)) & D23");
            final Formula cnf = formula.cnf();
            System.out.println(cnf.toString());
//            final SATSolver miniSat = MiniSat.miniSat(f);
//            miniSat.add(formula);
//            final Tristate result = miniSat.sat();
//            System.out.println(result);
        } catch (ParserException e) {
            e.printStackTrace();
        }

        return satisfied;
    }

    private int[] findATS() { // finds uncovered cell and calls satisfiedHere at that cell
        int[] coords = new int[2];
        for (int i = 0; i < displMap.length; i++) {
            for (int j = 0; j < displMap.length; j++) {
                if (displMap[i][j] == "+") {
                    System.out.println("making rules for cells adjacent to [" +j+","+i+"]: ");
                    if (satisfiedHere(j,i)) {
                        coords = new int[]{j, i};
                        break;
                    }
                }
            }
        }
        return coords;
    }

    public int[] singlePointStrategy() {
        int[] coords = new int[2];
        boolean sPFound = false;

//        for (int i = 0; i < displMap.length; i++) {
//            for (int j = 0; j < displMap.length; j++) {
//                if (displMap[i][j] == "+") {
//                    findMarkedNeighbors(j, i);
//                    if (allFree(j, i)) { // consider doing this after finding all marked neighbors
//                        coords[0] = i;
//                        coords[1] = j;
//                        sPFound = true;
//                        break;
//                    }
//                }
//            }
//        }
//        boolean aTSFound = false;
//        if (!sPFound) {
            coords = findATS();
//        }
//        if (!aTSFound)
//            coords = randomProbingStrategy();
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
