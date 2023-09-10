/* *****************************************************************************
 *  Name:              Aksel Sloan
 *  Coursera User ID:  EE602
 *  Last modified:     9/6/23
 *  IMPORTANT NOTE: API call access a 1-n grid, under the hood the grid is 0-n-1
 **************************************************************************** */

import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.WeightedQuickUnionUF;

public class Percolation {
    private static final int BLOCKED = 0;    // The site is blocked.
    private static final int OPEN = 1;       // The site is open.
    private int[][] grid; // Assuming you use a 2D grid to represent the percolation system
    private int gridSize; // Size of the grid
    private int openSites;
    private WeightedQuickUnionUF uf; // Instance variable for WeightedQuickUnionUF
    private WeightedQuickUnionUF ufFull; // Used only to determine if full
    // idea: use this weight quick union as is, but add a second to determine is full

    public Percolation(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Grid size must be greater than 0.");
        }
        // Initialize the grid with size gridSize
        //!!The size of the grid is nxn, but you refer to the columns and rows starting from 1 to n. you could either translate the row and column numbers to fit the grid, or have the grid be (n+1)x(n+1) and ignore the 0th row and column. either way is a solution.

        this.gridSize = n;
        grid = new int[n][n];
        openSites = 0;

        uf = new WeightedQuickUnionUF(n * n + 2);   // Create it with n * n elements
        // +2 for the virtual top and bottom roots
        ufFull = new WeightedQuickUnionUF(n * n + 1); // only virtual top


        // Initialize the grid with zeros (BLOCKED)
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                grid[row][col] = BLOCKED;
            }
        }
    }

    private int gridToUnionIndex(int row,
                                 int col) { // translate a grid point (x,y) to a weighted union index (i)
        return (row * this.gridSize) + col;
    }

    // using 0-(n-1)
    private boolean pointIsValid(int row, int col) { // check if a point is in the grid
        if (row < 0 || col < 0) {
            return false;
        }
        if (row >= this.gridSize || col >= this.gridSize) {
            return false;
        }
        return true;
    }


    public void open(int i, int j) {

        int row = i - 1;
        int col = j - 1;
        int virtualTop = gridSize * gridSize;
        int virtualBottom = gridSize * gridSize + 1;

        if (!pointIsValid(row, col)) { //
            throw new IllegalArgumentException("Outside of valid grid index");
        }
        if (grid[row][col] == OPEN) {
            return;
        }
        grid[row][col] = OPEN;
        openSites++;
        // north point
        if (pointIsValid(row - 1, col)) {
            if (isOpen(i - 1, j)) { // needs to be 1 - n
                uf.union(gridToUnionIndex(row, col),
                         gridToUnionIndex(row - 1, col));
                ufFull.union(gridToUnionIndex(row, col),
                             gridToUnionIndex(row - 1, col));
            }
        }
        // south point
        if (pointIsValid(row + 1, col)) {
            if (isOpen(i + 1, j)) { // needs to be 1 - n
                uf.union(gridToUnionIndex(row, col),
                         gridToUnionIndex(row + 1, col));
                ufFull.union(gridToUnionIndex(row, col),
                             gridToUnionIndex(row + 1, col));
            }
        }
        // east point
        if (pointIsValid(row, col + 1)) {
            if (isOpen(i, j + 1)) { // needs to be 1 - n
                uf.union(gridToUnionIndex(row, col),
                         gridToUnionIndex(row, col + 1));
                ufFull.union(gridToUnionIndex(row, col),
                             gridToUnionIndex(row, col + 1));

            }
        }
        // west point
        if (pointIsValid(row, col - 1)) {
            if (isOpen(i, j - 1)) { // needs to be 1 - n
                uf.union(gridToUnionIndex(row, col),
                         gridToUnionIndex(row, col - 1));
                ufFull.union(gridToUnionIndex(row, col),
                             gridToUnionIndex(row, col - 1));

            }
        }
        // union to virtual top
        if (row == 0) {
            if (isOpen(i, j)) {
                uf.union(gridToUnionIndex(row, col), virtualTop);
                // System.out.printf("row = %d, col = %d\n", row, col);
                ufFull.union(gridToUnionIndex(row, col), virtualTop);

            }
        }
        // union to virtual bottom
        if (row == (gridSize - 1)) {
            if (isOpen(i, j)) {
                uf.union(gridToUnionIndex(row, col), virtualBottom);
                // System.out.printf("row = %d, col = %d\n", row, col);
            }
        }

    }

    public boolean isOpen(int i, int j) {
        int row = i - 1;
        int col = j - 1;
        if (!pointIsValid(row, col)) {
            throw new IllegalArgumentException("Outside of valid grid index");
        }
        if (grid[row][col] == 1) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean isFull(int i, int j) { // checks union with virtualTop
        int row = i - 1;
        int col = j - 1;
        int virtualTop = gridSize * gridSize;
        if (!pointIsValid(row, col)) {
            throw new IllegalArgumentException("Outside of valid grid index");
        }
        if (!(ufFull.find(gridToUnionIndex(row, col)) == ufFull.find(virtualTop))) {
            // System.out.printf("find = %d\n ", uf.find(gridToUnionIndex(row, col)));
            return false;
        }
        return true;
    }


    public boolean percolates() {
        int virtualTop = gridSize * gridSize;
        int virtualBottom = gridSize * gridSize + 1;
        return (uf.find(virtualBottom) == uf.find(virtualTop));
    }

    public int numberOfOpenSites() {
        return openSites;
    }

    public static void main(String[] args) {

        // Test Group (1): opening random sites with the following n's until it percolates
        StdOut.println("Test group 1"); // Print it here
        int[] nRandom = { 3, 5, 10, 20, 50, 250, 500, 1000, 2000 };
        for (int myi = 0; myi < nRandom.length; myi++) {
            int counter = 0;
            Percolation myPerRandomSites = new Percolation(nRandom[myi]);
            while (!myPerRandomSites.percolates()) {
                // generate a random site (row, col) to open
                int row = (int) (Math.random() * nRandom[myi]) + 1;
                int col = (int) (Math.random() * nRandom[myi]) + 1;
                if (!myPerRandomSites.isOpen(row, col)) {
                    myPerRandomSites.open(row, col);
                    counter++;
                }
            }
            StdOut.println("n = " + nRandom[myi] + " : Percolated after opening " + counter
                                   + " random sites");
        }

        // Test Group (2): catch exceptions
        StdOut.println("Test group 2");
        // @TODO: Problem with exception here
        Percolation myPerException = new Percolation(10);
        int[][] invalidSites = {
                { -1, 5 }, { 11, 5 }, { 0, 5 }, { 5, -1 },
                { -2147483648, -2147483648 }, { 2147483647, 2147483647 }
        };
        boolean exceptionsDetected = false; // Initialize a flag to false

        for (int myi = 0; myi < invalidSites.length; myi = myi + 3) {
            try {
                myPerException.open(invalidSites[myi][0], invalidSites[myi][1]);
            }
            catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                StdOut.println("open() causes an exception: IndexOutOfBoundsException.");
                exceptionsDetected = true; // Set the flag to true if an exception is caught
            }
        }

        if (!exceptionsDetected) {
            StdOut.println("No exceptions");
        }

    }
}
