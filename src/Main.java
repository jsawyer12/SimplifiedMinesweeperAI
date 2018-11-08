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

    /**
     * probes cell and uncovers cell and adjacent cells if cell = "0"
     * @param world
     * @param agent
     * @param coords
     */
    public static void probeCell(World world, Agent agent, int[] coords) { // x, y
        if (world.map[coords[1]][coords[0]] == "0") {
            int[] stspCoords = agent.getStartStopCoords(coords[0], coords[1]); // get top left and bottom left cell coords for search
            for (int i = stspCoords[2]; i <= stspCoords[3]; i++) {
                for (int j = stspCoords[0]; j <= stspCoords[1]; j++) {
                    int[] tempCoords = new int[]{j, i}; //x, y
                    if (world.map[i][j] == "0" && agent.getCell(tempCoords) == "+") {// && (i != coords[0] || j != coords[1])) {
                        agent.adjustMap(tempCoords, world.map[i][j]);
                        probeCell(world, agent, tempCoords); // use for uncovering all adjacent cells with 0 for adjacent 0's
                    }
                    else
                        agent.adjustMap(tempCoords, world.map[i][j]);
                }
            }
        }
        else
            agent.adjustMap(coords, world.map[coords[1]][coords[0]]);
    }

    /**
     * parses user input to get coordinates
     * @param in
     * @return
     */
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

    /**
     * Returns 0 to continue game, 1 to end lost game, or 2 to end won game
     * @param agent
     * @return
     */
    public static int gameIsOver(Agent agent) {
        int gameOver = 0;
        if (agent.getLives() == 0) {
            gameOver = 1;
            agent.printWorld();
            System.out.println("You died!");
        }
        if (agent.getCellsLeft() == 0) {
            gameOver = 2;
            System.out.println("YOU WIN");
        }
        return gameOver;
    }

    /**
     * game loop
     * @param world
     * @param agent
     * @return
     */
    public static boolean playGame(World world, Agent agent) {
        Scanner userIn = new Scanner(System.in);
        probeCell(world, agent, new int[]{0, 0});
        while (gameIsOver(agent) == 0) {
            agent.printWorld();
            System.out.println("Player lives: " +agent.getLives());
            System.out.println("Moves left: " +agent.getCellsLeft());

//            System.out.print("Input coords: ");
//            String move = userIn.nextLine();
//            System.out.println();
//            int[] coords = getCoords(move); // for user input and game testing

//            int[] coords = agent.randomProbingStrategy();
//            int[] coords = agent.singlePointStrategy();
            int[] coords = agent.satisfiabilityTestStrategy();

            System.out.println("Trying x = " +coords[0] +", y = " +coords[1]);
            probeCell(world, agent, coords);
            System.out.println();
        }
        userIn.close();
        boolean won = false;
        if (gameIsOver(agent) == 2)
            won = true;
        return won;
    }

    public static void main(String[] args) {

        ArrayList<World> worlds = setUpWorldArray();

        int userInWorldChoice = 0;
        try {
            userInWorldChoice = Integer.parseInt(args[0]);
        } catch (Exception e) {
            System.out.println("Incorrect input for world choice (input between 0-29), using EASY1");
        }

        World world = worlds.get(userInWorldChoice);

        Agent stupidAgent = new Agent(world.length);

        playGame(world, stupidAgent);

        System.out.println("SPS Used = " +stupidAgent.spsUse +", Rand Used = " +stupidAgent.randUse);

        /******************* STATISTICAL USE *******************/

//        for (int k = 0; k < worlds.size(); k++) {
//            World worldTest = worlds.get(k);
//            int won = 0;
//            for (int i = 0; i < 100; i++) {
//                Agent agentTest = new Agent(worldTest.length);
//                if (playGame(worldTest,agentTest))
//                    won++;
//            }
//            System.out.println("World" +k +": won " +won +" out of 100");
//
//        }

        /******************* TESTING USE *******************/

//        probeCell(world, stupidAgent, new int[] {0, 0});
//        probeCell(world, stupidAgent, new int[] {11, 11});
//        probeCell(world, stupidAgent, new int[] {2, 4});
//
//        stupidAgent.printWorld();
//        int[] coords = stupidAgent.markNFree(8,8,1);
//        coords = stupidAgent.markNFree(8,7,1);
//
//        if (coords[1] != 0 && coords[0] != 0) {
//            System.out.println(Arrays.toString(coords));
//        }
//        stupidAgent.printWorld();

//
//
//        stupidAgent.printWorld();
//
//        System.out.println("Top practice = " +stupidAgent.satisfiedHere(2,3));
//
//
//        String rules = stupidAgent.getRules(2,3);
//        rules = "((D20 & ~D21 & ~D22) | (~D20 & D21 & ~D22) | (~D20 & ~D21 & D22) & " +
//                "(D20 & ~D21) | (~D20 & D21) & (D21 & ~D22) | (~D21 & D22)) & D21 ";
//        rules += " & (D23)";
//        System.out.println("Rules: " +rules);
//        String cnf = stupidAgent.makeCNF(rules);
//        System.out.println("CNF: " +cnf);
//        ISolver solver = stupidAgent.makeDIMACS(cnf);
//        try {
//            solver.addClause(new VecInt(new int[] {22,24}));
//            solver.addClause(new VecInt(new int[] {-23}));
//        } catch (ContradictionException e) {
//            e.printStackTrace();
//        }

//        final int MAXVAR = 7; //max number of variables
//        final int NBCLAUSES = 3; // number of clauses
//        ISolver solver = SolverFactory.newDefault();
//        solver.newVar(MAXVAR);
//        solver.setExpectedNumberOfClauses(NBCLAUSES);
//
//        // 1 = 20, 2 = 21, 3 = 22
//        int[] clause1=new int[] {3, 2, 1};
//        int[] clause2=new int[] {-2, -3, 1};
//        int[] clause3=new int[] {-1, -2};
//        int[] clause4=new int[] {3, -1, -2};
//        int[] clause5=new int[] {-2, -3};
//        int[] clause6=new int[] {-2, -3, -1};
//        int[] clause7=new int[] {-2};
//        try {
//            solver.addClause(new VecInt(clause1));
//            solver.addClause(new VecInt(clause2));
//            solver.addClause(new VecInt(clause3));
//            solver.addClause(new VecInt(clause4));
//            solver.addClause(new VecInt(clause5));
//            solver.addClause(new VecInt(clause6));
//            solver.addClause(new VecInt(clause7));
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

    /**
     * for jar args to decide world
     * @return
     */
    public static ArrayList<World> setUpWorldArray() {
        ArrayList<World> worlds = new ArrayList<>();
        World world1 = World.EASY1;
        World world2 = World.EASY2;
        World world3 = World.EASY3;
        World world4 = World.EASY4;
        World world5 = World.EASY5;
        World world6 = World.EASY6;
        World world7 = World.EASY7;
        World world8 = World.EASY8;
        World world9 = World.EASY9;
        World world10 = World.EASY10;
        World world11 = World.MEDIUM1;
        World world12 = World.MEDIUM2;
        World world13 = World.MEDIUM3;
        World world14 = World.MEDIUM4;
        World world15 = World.MEDIUM5;
        World world16 = World.MEDIUM6;
        World world17 = World.MEDIUM7;
        World world18 = World.MEDIUM8;
        World world19 = World.MEDIUM9;
        World world20 = World.MEDIUM10;
        World world21 = World.HARD1;
        World world22 = World.HARD2;
        World world23 = World.HARD3;
        World world24 = World.HARD4;
        World world25 = World.HARD5;
        World world26 = World.HARD6;
        World world27 = World.HARD7;
        World world28 = World.HARD8;
        World world29 = World.HARD9;
        World world30 = World.HARD10;
        worlds.add(world1);
        worlds.add(world2);
        worlds.add(world3);
        worlds.add(world4);
        worlds.add(world5);
        worlds.add(world6);
        worlds.add(world7);
        worlds.add(world8);
        worlds.add(world9);
        worlds.add(world10);
        worlds.add(world11);
        worlds.add(world12);
        worlds.add(world13);
        worlds.add(world14);
        worlds.add(world15);
        worlds.add(world16);
        worlds.add(world17);
        worlds.add(world18);
        worlds.add(world19);
        worlds.add(world20);
        worlds.add(world21);
        worlds.add(world22);
        worlds.add(world23);
        worlds.add(world24);
        worlds.add(world25);
        worlds.add(world26);
        worlds.add(world27);
        worlds.add(world28);
        worlds.add(world29);
        worlds.add(world30);
        return worlds;
    }
}
