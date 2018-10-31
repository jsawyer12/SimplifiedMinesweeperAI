import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        World world = World.EASY1;
        Part1 part1 = new Part1(world);
//        World.EASY1.printWorld();
        playGame(world);
    }

    public static int[] getCoords(String in) {
        String[] moveSpl = in.split(" ");
        int[] coords = new int[2];
        coords[0] = Integer.parseInt(moveSpl[0]); // y coordinate
        coords[1] = Integer.parseInt(moveSpl[1]); // x coordinate
        return coords;
    }

    public static World uncoverCells(World world, int[] coords) {
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
                    world.map[i][j] = "X";
                    uncoverCells(world, new int[]{i, j});
                }
                System.out.print(" " +world.map[i][j]);
            }
            System.out.println();
        }
        System.out.println();
        return world;
    }

    public static void playGame(World world) {
        Scanner userIn = new Scanner(System.in);
        boolean gameOver = false;
        while (!gameOver) {
            world.printWorld();
            System.out.print("Input coords: ");
            String move = userIn.nextLine();
            System.out.println();
            int[] coords = getCoords(move);
            uncoverCells(world, coords);
        }
        userIn.close();
    }
}
