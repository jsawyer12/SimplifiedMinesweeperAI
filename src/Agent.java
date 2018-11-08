import org.logicng.datastructures.Tristate;
import org.logicng.formulas.Formula;
import org.logicng.formulas.FormulaFactory;
import org.logicng.io.parsers.ParserException;
import org.logicng.io.parsers.PropositionalParser;
import org.logicng.solvers.MiniSat;
import org.logicng.solvers.SATSolver;
import org.sat4j.core.VecInt;
import org.sat4j.pb.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

public class Agent {

    private String[][] displMap;
    private int lives;
    private int cellsLeft;

    /*********************************  SINGLE POINT STRATEGY *********************************/


    public void markDaggers() {
        for (int y = 0; y < displMap.length; y++) {
            for (int x = 0; x < displMap.length; x++) {
                try {
                    int numCell = Integer.parseInt(displMap[y][x]);
                    if (numCell > 0) {
                        int daggers = 0; // number of daggers marked or stabbed by
                        int[] stspCoords = getStartStopCoords(x, y);
                        ArrayList<int[]> covCells = new ArrayList<>();
                        for (int k = stspCoords[2]; k <= stspCoords[3]; k++) {
                            for (int l = stspCoords[0]; l <= stspCoords[1]; l++) {
                                if (displMap[k][l] == "+") {
                                    covCells.add(new int[] {l, k}); // x, y
                                }
                                if (displMap[k][l] == "d" || displMap[k][l] == "D") {
                                    daggers++;
                                }
                            }
                        }
                        // if the number of adjacent unmarked cells equal the number in the original cell
                        // minus uncovered or covered daggers, tag those cells as daggers, ALL MARKED NEIGHBORS part
                        if (covCells.size() == numCell - daggers) {
                            for (int ay = 0; ay < covCells.size(); ay++) {
                                System.out.println("Marking dagger at " +covCells.get(ay)[0]+","+covCells.get(ay)[1]);
                                displMap[covCells.get(ay)[1]][covCells.get(ay)[0]] = "D";
                            }
                        }
                    }
                } catch (Exception e) {}
            }
        }
    }

    /**
     *
     * @return
     */
    public int[] freeCells() {
        int[] coords = new int[2];
        for (int y = 0; y < displMap.length; y++) {
            for (int x = 0; x < displMap.length; x++) {
                try {
                    int numCell = Integer.parseInt(displMap[y][x]);
                    if (numCell > 0) {
                        int daggers = 0; // number of daggers marked or stabbed by
                        int[] stspCoords = getStartStopCoords(x, y);
                        ArrayList<int[]> covCells = new ArrayList<>();
                        for (int k = stspCoords[2]; k <= stspCoords[3]; k++) {
                            for (int l = stspCoords[0]; l <= stspCoords[1]; l++) {
                                if (displMap[k][l] == "+") {
                                    covCells.add(new int[] {l, k}); // x, y
                                }
                                if (displMap[k][l] == "d" || displMap[k][l] == "D") {
                                    daggers++;
                                }
                            }
                        }
                        // if number of adjacent daggers - number specified in original cell == 0,
                        // the last covered cell[s] must be ok to probe! ALL FREE NEIGHBORS part
                        if (daggers - numCell == 0 && covCells.size() > 0) {
                            coords = covCells.get(0);
                        }
                    }
                } catch (Exception e) {}
            }
        }
        return coords;
    }


    /********************************* SAT STRATEGY **************************************/

    //code modified from https://ide.geeksforgeeks.org/index.php
    //given an arraylist of cells containing pluses:
    static ArrayList<String> clausePossibilities(int arr[][], int n, int r, int index, int data[][], int i, ArrayList<String> clauses) {
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
                    clause += "~";
                }
                clause += "D"+arr[j][0]+arr[j][1];
                if (j != arr.length - 1) {
                    clause += " & ";
                }
            }
            clause = "(" +clause +")";
            clauses.add(clause);
            return clauses;
        }

        // When no more elements are there to put in data[]
        if (i >= n)
            return clauses;

        // current is included, put next at next location
        data[index][0] = arr[i][0];
        data[index][1] = arr[i][1];
        clausePossibilities(arr, n, r, index+1, data, i+1, clauses);

        // current is excluded, replace it with next (Note that
        // i+1 is passed, but index is not changed)
        clausePossibilities(arr, n, r, index, data, i+1, clauses);
        return clauses;
    }

    private String makeRule(int x, int y, int clueCnt) {
        System.out.print("Rule for " +x+","+y +": ");
        int[] stspCoords = getStartStopCoords(x, y);
        String rule = "";
        ArrayList<int[]> covCells = new ArrayList<>(); // x, y adjacent covered cell locations
        ArrayList<int[]> dagCells = new ArrayList<>(); // x, y known adjacent dagger cell locations
        for (int a = stspCoords[2]; a <= stspCoords[3]; a++) {
            for (int b = stspCoords[0]; b <= stspCoords[1]; b++) {
                if (displMap[a][b] == "+") {
                    covCells.add(new int[] {b, a});
                }
                if (displMap[a][b] == "d" && displMap[a][b] == "D") {
                    dagCells.add(new int[] {b, a});
                }
            }
        }
        // if number of daggers adjacent to number cell is less than the number of flagged
        // daggers and uncovered daggers, there are still daggers to uncover out there!
        // LET'S DO SOME PROPOSITIONAL LOGIC!!!
        if (clueCnt > dagCells.size()) {
            int dagsLeft = clueCnt - dagCells.size();

            // get all the possible or clauses for the pluses
            ArrayList<String> clauses = new ArrayList<>();
            int[][] covCellsArr = covCells.toArray(new int[covCells.size()][2]);

            int[][] possibleCombos = new int[covCells.size()][2];
//            for (int oh = 0; oh < dagCells.size(); oh++) {
//                possibleCombos[oh][0] = dagCells.get(oh)[0];
//                possibleCombos[oh][1] = dagCells.get(oh)[1];
//            }
            clauses = clausePossibilities(covCellsArr, covCells.size(), dagsLeft, 0, possibleCombos, 0, clauses);
            for (int oh = 0; oh < clauses.size(); oh++) {
//                System.out.print(clauses.get(oh) +" ");
                rule += clauses.get(oh);
                if (oh != clauses.size() - 1) {
//                    System.out.print("| ");
                    rule += " | ";
                }
            }
//            System.out.println(rule);
            // then add the definite daggers to the mix, dont think I need this since not included in clauses anyway
//            for (int oh = 0; oh < dagCells.size(); oh++) {
//                rule += "& " +dagCells.get(oh);
//            }
        }
        System.out.println(rule);
        return rule;
    }

    private String[] cnfFilleted(String cnfStr) {
//        System.out.println("Before cleaning" +cnfStr);
        String cnfCleaned = cnfStr.replace("(", "");
        cnfCleaned = cnfCleaned.replace(")", "");
        cnfCleaned = cnfCleaned.replace(" ", "");
        cnfCleaned = cnfCleaned.replace("D", "");
        cnfCleaned = cnfCleaned.replace("~", "-");
//        System.out.println("cnfCleaned = " +cnfCleaned);
        return cnfCleaned.split("&");
    }

    public ISolver makeDIMACS(String cnfStr) {
        ISolver solver = SolverFactory.newDefault();
        try {

            String[] clauses = cnfFilleted(cnfStr);

            solver.newVar(25); // max total number of cells there could be rules for a given cell
            solver.setExpectedNumberOfClauses(clauses.length + 2);
            for (int ay = 0; ay < clauses.length; ay++) {
                String[] clauseSpl = clauses[ay].split(Pattern.quote("|"));
//                System.out.println("ClauseSPL: " +Arrays.toString(clauseSpl));
                int[] clause = new int[clauseSpl.length];
                for (int uh = 0; uh < clause.length; uh++) {
//                    System.out.println(clauseSpl[uh]);
                    clause[uh] = Integer.parseInt(clauseSpl[uh]);
                }
                System.out.print(Arrays.toString(clause) +" ");
                solver.addClause(new VecInt(clause));
            }
//            solver.addClause(new VecInt(new int[] {Integer.parseInt(curr)}));
//            System.out.println(curr);
        } catch (ContradictionException e) {
//            e.printStackTrace();
        } catch (NumberFormatException e) {
//            System.out.println("Error in LogicNG cnf conversion");
        }
        System.out.println();
        return solver;
    }

    public boolean satisfiedHere(IProblem rules) { // find clues surrounding covered cell
        boolean satisfied = true;
        try {
            if (!rules.isSatisfiable()) {
                satisfied = false;
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return satisfied;
    }

    public String getRules(int x, int y) {
        String rules = "";
        int[] stspCoords = getStartStopCoords(x, y);
        for (int k = stspCoords[2]; k <= stspCoords[3]; k++) {
            for (int l = stspCoords[0]; l <= stspCoords[1]; l++) {
                try {
                    int numAdjDags = Integer.parseInt(displMap[k][l]);
//                    System.out.println(l+","+k +" has " +numAdjDags +" daggers");
                    if (numAdjDags > 0) {
                        rules += makeRule(l,k, numAdjDags) +" | ";
                    }
                } catch(Exception e) {

                }
            }
        }
        if (rules.length() > 0) {
            rules = rules.substring(0, rules.length() - 2); // removes last "| ";
        }
        return rules;
    }

    public String makeCNF(String rules) {
        String cnfStr = "";
        final FormulaFactory f = new FormulaFactory();
        final PropositionalParser p = new PropositionalParser(f);
        ISolver solver = SolverFactory.newDefault();
        try {
            final Formula formula = p.parse(rules);
            final Formula cnf = formula.cnf();
//            System.out.println(cnf.toString());

            int numClauses = 1;
            ArrayList<String> variables = new ArrayList<>();
            cnfStr = cnf.toString();
        } catch (ParserException e) {
//            e.printStackTrace();
        }
        return cnfStr;
    }

    private int[] runATS() {
        int[] coords = new int[2];
        for (int y = 0; y < displMap.length; y++) {
            for (int x = 0; x < displMap.length; x++) {
                if (displMap[y][x] == "+") {
                    String rulesRoundHere = getRules(x, y);

                    if (rulesRoundHere.length() > 0) {
                        String rules4Danger = rulesRoundHere + "& ~D" +x +y;
//                            System.out.println("Rules4D:    " +rules4Danger);
                        String dangerCNF = makeCNF(rules4Danger);
                        IProblem dangerHere = makeDIMACS(dangerCNF);

                        if (!satisfiedHere(dangerHere))
                            displMap[y][x] = "D";

                        String rules4Safety = rulesRoundHere +"& D" +x +y;
//                            System.out.println("Rules4S:    " +rules4Safety);
                        String safetyCNF = makeCNF(rules4Safety);
                        IProblem safeHere = makeDIMACS(safetyCNF);

                        if (!satisfiedHere(safeHere)) {
                            System.out.println("ATS found");
                            coords = new int[]{x, y};
                            return coords;
                        }
                    }
                }
            }
        }
        return coords;
    }

    public int[] singlePointStrategy() {
        int[] coords = new int[2];
        markDaggers();
        coords = freeCells(); // then freeing
        if (coords[1] != 0 && coords[0] != 0) {
            System.out.println("SPS found");
//            spsUse++;
            return coords;
        }
        System.out.println("Rand found");
//        randUse++;
        coords = randomProbingStrategy();
        return coords;
    }

    public int[] satisfiabilityTestStrategy() {
        int[] coords = new int[2];

        markDaggers();

        coords = freeCells(); // then freeing
        if (coords[1] != 0 && coords[0] != 0) {
            System.out.println("SPS found");
//            spsUse++;
            return coords;
        }

        coords = runATS();
        if(coords[1] != 0 && coords[0] != 0) {
            System.out.println("ATS found");
            return coords;
        }
        System.out.println("Rand found");
//        randUse++;
        coords = randomProbingStrategy();
        return coords;
    }

    public int[] randomProbingStrategy() {
        int[] coords = new int[2];
        boolean coordsAreGood = false;
        while (!coordsAreGood) {
            coords[0] = (int) (Math.random() * displMap.length);
            coords[1] = (int) (Math.random() * displMap.length);
            if (displMap[coords[1]][coords[0]] == "+")
                coordsAreGood = true;
        }
        return coords;
    }

    /**
     * Helper method used across program to get coordinates for the top left and bottom right
     * cells adjacent to the current cell at x, y
     * @param x
     * @param y
     * @return
     */
    public  int[] getStartStopCoords(int x, int y) {
        int[] stspCoords = new int[4]; // [startX, endX, startY, endY]

        stspCoords[0] = x - 1;
        stspCoords[1] = x + 1;
        if (stspCoords[0] < 0)
            stspCoords[0] = 0;
        if (stspCoords[1] >= displMap.length)
            stspCoords[1] = displMap.length - 1;

        stspCoords[2] = y - 1;
        stspCoords[3] = y + 1;
        if (stspCoords[2] < 0)
            stspCoords[2] = 0;
        if (stspCoords[3] >= displMap.length)
            stspCoords[3] = displMap.length - 1;
        return stspCoords;
    }

    public void adjustMap(int[] coords, String val) { // x,y
        if (displMap[coords[1]][coords[0]] == "+") { // if move on new position
            if (val == "g")
                lives++;
            else if (val == "d") {
                lives--;
                cellsLeft++; // more elegant solution later, but basically doesn't remove cell from cell left if on dagger
            }
            displMap[coords[1]][coords[0]] = val;
            cellsLeft--;
        }
    }

    public String getCell(int[] coords) {
        return displMap[coords[1]][coords[0]];
    }

    public int getLives() {
        return lives;
    }

    public int getCellsLeft() {
        return cellsLeft;
    }

    public void printWorld() {
        System.out.println();
        System.out.print("    ");
        for (int a = 0; a < displMap.length; a++) {
            if (a <= 10) System.out.print(" ");
            System.out.print(" " +a);
        }
        System.out.println();
        System.out.print("    ");
        for (int a = 0; a < displMap.length; a++) {
            System.out.print("___");
        }
        System.out.println();
        for (int i = 0; i < displMap.length; i++) {
            System.out.print(i);
            if (i < 10) System.out.print(" ");
            System.out.print(" |");
            for (int j = 0; j < displMap.length; j++) {
                System.out.print("  " +displMap[i][j]);
            }
            System.out.println();
        }
    }

    // for statistical analysis
//    public int spsUse;
//    public int randUse;

    public Agent (int mapLength) {
//        spsUse = 0;
//        randUse = 0;

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
