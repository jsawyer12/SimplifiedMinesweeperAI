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

import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void uncoverCells(World world, Agent agent, int[] coords) {
        if (world.map[coords[0]][coords[1]] == "0") {
            int[] stspCoords = agent.getStartStopCoords(coords[1], coords[0]); // get top left and bottom left cell coords for search
            for (int i = stspCoords[0]; i <= stspCoords[1]; i++) {
                for (int j = stspCoords[2]; j <= stspCoords[3]; j++) {
                    int[] tempCoords = new int[]{i, j};
                    if (world.map[i][j] == "0" && agent.getCell(tempCoords) == "+") {// && (i != coords[0] || j != coords[1])) {
                        agent.adjustMap(tempCoords, world.map[i][j]);
                        uncoverCells(world, agent, tempCoords); // use for uncovering all adjacent cells with 0 for adjacent 0's
                    }
                    else
                        agent.adjustMap(tempCoords, world.map[i][j]);
                }
            }
        }
        else
            agent.adjustMap(coords, world.map[coords[0]][coords[1]]);
    }

    public static int[] getCoords(String in) {
        int[] coords = new int[2];
        try {
            String[] moveSpl = in.split(" ");
            coords[1] = Integer.parseInt(moveSpl[0]); // y coordinate
            coords[0] = Integer.parseInt(moveSpl[1]); // x coordinate
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Invalid input");
        } finally {
            return coords;
        }
    }

    public static boolean gameIsOver(Agent agent) {
        boolean gameOver = false;
        if (agent.getLives() == 0) {
            gameOver = true;
            agent.printWorld();
            System.out.println("You died!");
        }
        if (agent.getCellsLeft() == 0) {
            gameOver = true;
            System.out.println("YOU WIN");
        }
        return gameOver;
    }

    public static void playGame(World world, Agent agent) {
        Scanner userIn = new Scanner(System.in);
        uncoverCells(world, agent, new int[]{0, 0});
        while (!gameIsOver(agent)) {
            agent.printWorld();
            System.out.println("Player lives: " +agent.getLives());
            System.out.println("Moves left: " +agent.getCellsLeft());

//            System.out.print("Input coords: ");
//            String move = userIn.nextLine();
//            System.out.println();
//            int[] coords = getCoords(move); // for user input and game testing

//            int[] coords = agent.randomProbingStrategy();

            int[] coords = agent.singlePointStrategy();



            System.out.println("Trying x = " +coords[1] +", y = " +coords[0]);
            uncoverCells(world, agent, coords);
            System.out.println();
        }
        userIn.close();
    }

    public static void main(String[] args) {

        World world = World.EASY1;
        Agent stupidAgent = new Agent(world.length);

        playGame(world, stupidAgent);

//        FormulaFactory f = new FormulaFactory();
//        Variable a = f.variable("D22");
//        Variable b = f.variable("D23");
//        Variable c = f.variable("D24");
//        Literal notC = f.literal("C", false);
//        Formula formula = f.and(a, f.not(f.or(b, notC)));

        final FormulaFactory f = new FormulaFactory();
        final PropositionalParser p = new PropositionalParser(f);
        try {
            final Formula formula = p.parse("(D22 & ~D23) | (~D22 & D23) & ((D22 & ~D23 & ~D24) | "
                    +"(~D22 & D23 & ~D24) | (~D22 & ~D23 & D24)) & ((~D22 & ~D24) | (~D23 & D24)) & D23");
            final Formula cnf = formula.cnf();
            System.out.println(cnf.toString());
            final SATSolver miniSat = MiniSat.miniSat(f);
            miniSat.add(formula);
            final Tristate result = miniSat.sat();
            System.out.println(result);
        } catch (ParserException e) {
            e.printStackTrace();
        }

        //(D23 | D22) & (D24 | D23 | D22) & (~D23 | ~D24 | D22) & (~D22 | ~D23) & (D24 | ~D22 | ~D23) & (~D22 | ~D24 | ~D23) & (~D24 | ~D23)
        //D22 = 1, D23 = 2, D24 = 3
        ArrayList<int[]> clauses = new ArrayList<>();
        clauses.add(new int[] {2,1});
        clauses.add(new int[] {3,2,1});
        clauses.add(new int[] {-2,-3,1});
        clauses.add(new int[] {-1,-2});
        clauses.add(new int[] {3,-1,-2});
        clauses.add(new int[] {-1,-3,-2});
        clauses.add(new int[] {-3,-2});
        clauses.add(new int[] {2});


        int MAXVAR = 3;
        int NBCLAUSES = 8;
        ISolver solver = SolverFactory.newDefault();
        solver.newVar(MAXVAR);
        solver.setExpectedNumberOfClauses(NBCLAUSES);

        try {
            for (int i = 0; i < clauses.size(); i++) {
                solver.addClause(new VecInt(clauses.get(i)));
            }
            IProblem problem= solver;
            if (problem.isSatisfiable()) {
                System.out.println("No fucking way this works, wow it worked");
            }
            else {
                System.out.println("I knew it was too good to be true");
            }
        } catch (ContradictionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
