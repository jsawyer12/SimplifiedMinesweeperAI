import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void uncoverCells(World world, Agent agent, int[] coords) {
        if (world.map[coords[0]][coords[1]] == "0") {
            int[] stspCoords = getStartStopCoords(coords, world); // get top left and bottom left cell coords for search
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

    public static int[] getStartStopCoords(int[] coords, World world) {
        int[] stspCoords = new int[4]; // [startY, endY, startX, endX]
        stspCoords[0] = coords[0] - 1;
        stspCoords[1] = coords[0] + 1;
        if (stspCoords[0] < 0)
            stspCoords[0] = 0;
        if (stspCoords[1] >= world.length)
            stspCoords[1] = world.length - 1;
        stspCoords[2] = coords[1] - 1;
        stspCoords[3] = coords[1] + 1;
        if (stspCoords[2] < 0)
            stspCoords[2] = 0;
        if (stspCoords[3] >= world.length)
            stspCoords[3] = world.length - 1;
        return stspCoords;
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

            int[] coords = agent.randomProbingStrategy();

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

//        DPLLSatisfiable dpllSatisfiable = new DPLLSatisfiable();
//
//        String p="D21";
//        System.out.println("ProveDanger "+p);
//        String KBU="((D20 & ~D21 & ~D22) | (~D20 & D21 & ~D22) | (~D20 & ~D21 & D22))
//                +"& ((D20 & ~D21) | (~D20 & D21)) & ((D21 & ~D22) | (~D21 & D22))";
//        String prove=KBU+" & ~"+p;
//        boolean ans = displayDPLLSatisfiableStatus(prove);
//        System.out.println("Does KBU entail "+p+"?, Test KBU & ~"+p);
//        if(!ans){//if false mark
//            System.out.println("Yes, Danger, Mark");
//        }else{
//            System.out.println("No");
//        }
    }
}
