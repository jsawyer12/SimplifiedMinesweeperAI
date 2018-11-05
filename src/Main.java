import java.util.Scanner;

public class Main {

    public static int[] singlePointStrategy(World world) {
        int[] coords = new int[2];
        for (int i = 0; i < world.length; i++) {
            for(int j = 0; j < world.length; j++) {
                if (world.displMap[i][j] == "+") {

                }
            }
        }
        return coords;
    }

    public static int[] randomProbingStrategy(World world) {
        int[] coords = new int[2];
        boolean coordsAreGood = false;
        while (!coordsAreGood) {
            coords[0] = (int) (Math.random() * world.length);
            coords[1] = (int) (Math.random() * world.length);
            if (world.displMap[coords[0]][coords[1]] == "+")
                coordsAreGood = true;
        }
        return coords;
    }

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
                    if (world.map[i][j] == "g") // MAKE SURE THIS WORKS ////////////////////////////////////////////
                        live++;
                    if (world.displMap[i][j] == "+") {
                        world.displMap[i][j] = world.map[i][j];
                        if (world.map[i][j] == "0" && (i != coords[0] || j != coords[1])) {
                            uncoverCells(world, new int[]{i, j}); // use for uncovering all adjacent cells with 0 for minesweeper
                        }
//                        System.out.print(" " +world.displMap[i][j]);
                    }
                }
//                System.out.println();
            }
//            System.out.println();
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
        int moves = 0;
        while (!gameIsOver(lives)) {
            world.printWorld();
            System.out.println("Player lives: " +lives);
//            System.out.print("Input coords: ");
//            String move = userIn.nextLine();
//            System.out.println();
//            int[] coords = getCoords(move); // for user input and game testing
            int[] coords = randomProbingStrategy(world);
            if (moves == 0) {
                coords[0] = 0;
                coords[1] = 0;
            }
            System.out.println("Trying x = " +coords[1] +", y = " +coords[0]);
            lives += uncoverCells(world, coords);
            moves++;
            System.out.println();
        }
        userIn.close();
    }

    public static void main(String[] args) {

        int lives = 1;
        World world = World.EASY1;
        playGame(world, lives);
    }
}
