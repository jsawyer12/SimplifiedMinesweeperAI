import java.util.Scanner;

public class Main {

    public static int[] getCoords(String in) {
        String[] moveSpl = in.split(" ");
        int[] coords = new int[2];
        coords[0] = Integer.parseInt(moveSpl[0]); // y coordinate
        coords[1] = Integer.parseInt(moveSpl[1]); // x coordinate
        return coords;
    }

    public static int uncoverCells(World world, int[] coords) {
        int live = 0; // either adds a life if gold is found, subtracts if dagger is found, and stays at 0 if anything else is found at current coords choice
        if (world.map[coords[0]][coords[1]] == "0") {
            int startY = coords[0] - 1, endY = coords[0] + 1;
            if (startY < 0)
                startY = 0;
            if (endY >= world.length)
                endY = world.length - 1;
            int startX = coords[1] - 1, endX = coords[1] + 1;
            if (startX < 0)
                startX = 0;
            if (endX >= world.length)
                endX = world.length - 1;
            for (int i = startY; i <= endY; i++) {
                for (int j = startX; j <= endX; j++) {
                    if (world.map[i][j] == "0") {
                        world.displMap[i][j] = world.map[i][j];
//                    uncoverCells(world, new int[]{i, j}); // use for uncovering all adjacent cells with 0 for minesweeper
                    }
                    System.out.print(" " +world.displMap[i][j]);
                }
                System.out.println();
            }
            System.out.println();
        }
        if (world.map[coords[0]][coords[1]] == "d")
            live = -1;
        if (world.map[coords[0]][coords[1]] == "g")
            live = 1;
        world.displMap[coords[0]][coords[1]] = world.map[coords[0]][coords[1]];
        return live;
    }

    public static boolean gameIsOver(int lives) {
        boolean gameOver = false;
        if (lives == 0) {
            gameOver = true;
            System.out.println("You died!");
        }
        return gameOver;
    }

    public static void playGame(World world, int lives) {
        Scanner userIn = new Scanner(System.in);
        while (!gameIsOver(lives)) {
            world.printWorld();
            System.out.print("Input coords: ");
            String move = userIn.nextLine();
            System.out.println();
            int[] coords = getCoords(move);
            lives += uncoverCells(world, coords);
        }
        userIn.close();
    }

    public static void main(String[] args) {

        int lives = 1;
        World world = World.EASY1;
        Part1 part1 = new Part1(world);
//        World.EASY1.printWorld();
        playGame(world, lives);
    }
}
