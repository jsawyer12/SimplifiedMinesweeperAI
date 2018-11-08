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
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    public static void uncoverCells(World world, Agent agent, int[] coords) { // x, y
        if (world.map[coords[1]][coords[0]] == "0") {
            int[] stspCoords = agent.getStartStopCoords(coords[0], coords[1]); // get top left and bottom left cell coords for search
            for (int i = stspCoords[2]; i <= stspCoords[3]; i++) {
                for (int j = stspCoords[0]; j <= stspCoords[1]; j++) {
                    int[] tempCoords = new int[]{j, i}; //x, y
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
            agent.adjustMap(coords, world.map[coords[1]][coords[0]]);
    }

    public static int[] getCoords(String in) { // parses string to get x, y
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



            System.out.println("Trying x = " +coords[0] +", y = " +coords[1]);
            uncoverCells(world, agent, coords);
            System.out.println();
        }
        userIn.close();
    }

    public static void main(String[] args) {

        World world = World.HARD1;

        Agent stupidAgent = new Agent(world.length);


        playGame(world, stupidAgent);

//        uncoverCells(world, stupidAgent, new int[] {0, 0});
//        uncoverCells(world, stupidAgent, new int[] {2, 2});
//        stupidAgent.printWorld();
//        int[] coords = stupidAgent.markNFree(1,2,1);
//        System.out.println(Arrays.toString(coords));
//        stupidAgent.printWorld();

//
//
//        stupidAgent.printWorld();
//
//        System.out.println("Top practice = " +stupidAgent.satisfiedHere(2,3));
//
//
//        final int MAXVAR = 7; //max number of variables
//        final int NBCLAUSES = 3; // number of clauses
//        ISolver solver = SolverFactory.newDefault();
//        solver.newVar(MAXVAR);
//        solver.setExpectedNumberOfClauses(NBCLAUSES);
//
//        int[] clause1=new int[] {3, 2, 1};
//        int[] clause2=new int[] {-3, -1, -2};
//        int[] clause3=new int[] {-2};
//        try {
//            solver.addClause(new VecInt(clause1));
//            solver.addClause(new VecInt(clause2));
//            solver.addClause(new VecInt(clause3));
//        } catch (ContradictionException e) {
//            e.printStackTrace();
//        }
//
//
//
//
//        IProblem problem = solver;
//        try {
//            System.out.println("Bottom practice = " +problem.isSatisfiable());
//        } catch (TimeoutException e) {
//            e.printStackTrace();
//        }
    }
}
