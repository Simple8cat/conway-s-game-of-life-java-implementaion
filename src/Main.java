import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

interface Drawable {
    void draw(Graphics2D g2d);
}
interface Matrix {
    public boolean getState(int row, int col);
    public void setState(int row, int col, boolean state);
}
class Grid implements Matrix {
    boolean[][] grid;
    int rows, cols;
    Grid(int rows, int cols) {
        grid = new boolean[rows][cols];
    }
    public boolean getState(int row, int col) {
        return grid[row][col];
    }
    public void setState(int row, int col, boolean state) {
        grid[row][col] = state;
    }
}
class CellGrid {
    protected int rows, cols;
    protected Grid gridMatrix;
    protected Grid nextGridMatrix;

    CellGrid(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        gridMatrix = new Grid(rows, cols);
        nextGridMatrix = new Grid(rows, cols);
    }
    public void toggleCell(int x, int y) {
        gridMatrix.setState(x, y, !gridMatrix.getState(x, y));
    }

    public int countNeighbors(int x, int y) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                int nx = x + i;
                int ny = y + j;
                if (nx >= 0 && nx < rows && ny >= 0 && ny < cols && gridMatrix.getState(nx, ny)) {
                    count++;
                }
            }
        }
        return count;
    }

    public void updateGrid() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                gridMatrix.setState(i,j,nextGridMatrix.getState(i,j));
            }
        }
    }
}
class GameOfLifeGrid extends CellGrid implements Drawable {
    int cellSize = 10 ;
    GameOfLifeGrid(int rows, int cols) {
        super(rows, cols);
    }

    public void initializeGrid() {
        for (int i = 0; i < rows; i += 2) {
            for (int j = 1; j < cols; j += 2) {
                gridMatrix.setState(i,j,true);
            }
        }
    }

    public void computeNextGeneration() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int neighbors = countNeighbors(i, j);
                if (gridMatrix.getState(i,j)) {
                    nextGridMatrix.setState(i,j,neighbors == 2 || neighbors == 3);
                } else {
                    nextGridMatrix.setState(i,j,neighbors == 3);
                }
            }
        }
        updateGrid();
    }
    public void RandomizeGrid(){
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                nextGridMatrix.setState(i,j,new Random().nextBoolean());
            }
        }
        updateGrid();
    }
    public void ClearGrid(){
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                nextGridMatrix.setState(i,j,false);
            }
        }
        updateGrid();
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, rows * cellSize, cols * cellSize);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                g2d.setColor(gridMatrix.getState(i,j) ? Color.WHITE : Color.BLACK);
                g2d.fillRect(i * cellSize, j * cellSize, cellSize - 1, cellSize - 1);
            }
        }
    }
}
class GridPanel extends JPanel {
    private final GameOfLifeGrid gameGrid;
    boolean Pattenmode = false;
    String pattern ;
    GridPanel(GameOfLifeGrid gameGrid) {
        this.gameGrid = gameGrid;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(!Pattenmode){
                    int x = e.getX() / gameGrid.cellSize;
                    int y = e.getY() / gameGrid.cellSize;
                    if (x < gameGrid.rows && y < gameGrid.cols) {
                        gameGrid.toggleCell(x, y);
                        repaint();
                    }
                }else{
                    if(pattern.equals("Glider")){
                        int x = e.getX() / gameGrid.cellSize;
                        int y = e.getY() / gameGrid.cellSize;
                        gameGrid.toggleCell(x, y);
                        gameGrid.toggleCell(x-1, y);
                        gameGrid.toggleCell(x-2, y);
                        gameGrid.toggleCell(x, y+1);
                        gameGrid.toggleCell(x-1, y+2);
                        repaint();

                    } else if (pattern.equals("GliderGeneerator")) {
                        
                    } else if (pattern.equals("spaceship")) {
                        
                    }
                }
            }
        });
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        gameGrid.draw((Graphics2D) g);
    }
}
class GameOfLifeApp {
    private final JFrame frame;
    private final GridPanel gridPanel;
    private final GameOfLifeGrid gameGrid;
    private final Timer timer;

    GameOfLifeApp() {
        gameGrid = new GameOfLifeGrid(1000, 1000);
        gameGrid.initializeGrid();
        gridPanel = new GridPanel(gameGrid);
        gridPanel.setPreferredSize(new Dimension(1000, 1000));
        frame = new JFrame("Game of Life");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(gridPanel, BorderLayout.CENTER);
        JPanel controls = createControls();
        frame.add(controls, BorderLayout.SOUTH);
        timer = new Timer(10, e -> {
            gameGrid.computeNextGeneration();
            gridPanel.repaint();
        });
        frame.pack();
        frame.setVisible(true);
    }

    private JPanel createControls() {
        JPanel panel = new JPanel();
        JButton startButton = new JButton("Start");
        JButton stopButton = new JButton("Stop");
        JButton randomizeButton = new JButton("Randomize");
        JButton clearButton = new JButton("Clear");
        JButton drawGliderButton = new JButton("Draw Glider");
        JSlider cellSizeSlider = new JSlider();
        JButton singleCellButton = new JButton("Single Cell edit");
        cellSizeSlider.setName("cellSize");
        startButton.addActionListener(e -> timer.start());
        stopButton.addActionListener(e -> timer.stop());
        randomizeButton.addActionListener(e -> {
            gameGrid.RandomizeGrid();
            gridPanel.repaint();
        });
        clearButton.addActionListener(e -> {
            gameGrid.ClearGrid();
            gridPanel.repaint();
        });
        cellSizeSlider.setMaximum(200);
        cellSizeSlider.setMinimum(5);
        cellSizeSlider.addChangeListener(e -> {
            gameGrid.cellSize = cellSizeSlider.getValue();
            gridPanel.repaint();
        });
        singleCellButton.addActionListener(e -> {
            singleCellButton.setBackground(Color.GRAY);
            drawGliderButton.setBackground(Color.WHITE);
            gridPanel.Pattenmode = false ;
        });
        drawGliderButton.addActionListener(e -> {
            drawGliderButton.setBackground(Color.GRAY);
            gridPanel.Pattenmode = true;
            gridPanel.pattern = "Glider";
        });
        panel.add(startButton);
        panel.add(stopButton);
        panel.add(randomizeButton);
        panel.add(clearButton);
        panel.add(cellSizeSlider);
        panel.add(drawGliderButton);
        panel.add(singleCellButton);
        return panel;
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameOfLifeApp::new);
    }
}
