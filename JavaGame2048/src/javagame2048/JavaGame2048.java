package javagame2048;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.io.*;
import java.util.logging.*;

/**
 *
 * @author vladt
 */
public class JavaGame2048 extends JFrame {

    private final JFrame gameFrame;
    private JScrollPane gameScrollPanes[][];
    private JLabel gameScore;
    private JLabel gameStatus;
    private JLabel gameHowToPlay[];
    private JTextField gameTextScoreToReach;
    private JButton gameButtonScoreToReach;
    private int gameTilesPoints[][];
    private int gameScoreInt;
    private int gameScoreToReach = 2048;
    private boolean wonGame = false;
    private boolean lostGame = false;

    public JavaGame2048() throws IOException {
        //initializing frame for the GUI components
        gameFrame = new JFrame();

        //initializing the GUI and then playing the game
        //using content pane for adding the components
        initGuiAndPlay(gameFrame, gameFrame.getContentPane());
    }

    //check if there is any 0 in the array
    private boolean isFullCheck() {
        for (int i = 0; i < gameTilesPoints.length; i++) {
            for (int j = 0; j < gameTilesPoints[0].length; j++) {
                if (gameTilesPoints[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean canMoveCheck() {
        //if array is not full, then tiles can still move
        if (!isFullCheck()) {
            return true;
        }

        //if it is full, check for nearby tiles with same value
        //if found, tiles can still move
        //checking rows
        for (int i = 0; i < gameTilesPoints.length; i++) {
            for (int j = 0; j < gameTilesPoints[0].length - 1; j++) {
                if (gameTilesPoints[i][j] == gameTilesPoints[i][j + 1]) {
                    return true;
                }
            }
        }

        //checking columns
        for (int i = 0; i < gameTilesPoints.length - 1; i++) {
            for (int j = 0; j < gameTilesPoints[0].length; j++) {
                if (gameTilesPoints[i][j] == gameTilesPoints[i + 1][j]) {
                    return true;
                }
            }
        }

        //return false if array is full and nothing can move
        return false;
    }

    //if the desired tile value is reached (default:2048), return true
    //else return false
    private boolean winCheck() {
        for (int i = 0; i < gameTilesPoints.length; i++) {
            for (int j = 0; j < gameTilesPoints[0].length; j++) {
                if (gameTilesPoints[i][j] >= gameScoreToReach) {
                    return true;
                }
            }
        }
        return false;
    }

    //if the game hasn't been won and nothing can move, game has been lost
    private boolean loseCheck() {
        return !winCheck() && !canMoveCheck();
    }

    //update status about game (won/lost)
    private void updateGameStatus(String status) {
        gameStatus.setText("Status: " + status);
    }

    //update score by adding a value to the total and setting the new text
    private void updateScore(int valueToAdd) {
        gameScoreInt += valueToAdd;
        gameScore.setText("Score: " + Integer.toString(gameScoreInt));
    }

    //call this whenever you need to update or set a value for the tiles
    private void updateScrollPanes(Container pane, GridBagConstraints c, int x, int y, int gameValue) {
        //update array for tiles' value with gameValue
        gameTilesPoints[y][x] = gameValue;

        //remove pane containing old info from GUI
        pane.remove(gameScrollPanes[y][x]);

        //initializing the pane with new info (value and its' certain linked color)
        gameScrollPanes[y][x] = new JScrollPane(new paintScrollPanes(gameValue));

        //setting up size for the new panes
        gameScrollPanes[y][x].setPreferredSize(new Dimension(75, 75));
        gameScrollPanes[y][x].setMinimumSize(new Dimension(75, 75));

        //aligning the new panes
        c.gridy = y;
        c.gridx = x;
        c.weightx = 0;
        c.insets = new Insets(15, 15, 15, 15);

        //adding the new panes
        pane.add(gameScrollPanes[y][x], c);

        //validate changes and repaint the frame holding the new panes
        gameFrame.validate();
        gameFrame.repaint();
    }

    //all pushZeros*Direction* functions check if there are zeroes on a line or column
    //if true, push them to the end or beginning of line or column
    //example: pushZerosRight pushes all the zeroes of a line to its' end
    //example: pushZerosUpwards pushes all the zeroes of a column to its' beginning
    private void pushZerosRight(Container pane, GridBagConstraints c, int gameTilesPoints[][], int index) {
        int count = 0;
        for (int j = 0; j < gameTilesPoints[index].length; j++) {
            if (gameTilesPoints[index][j] != 0) {
                gameTilesPoints[index][count++] = gameTilesPoints[index][j];
                updateScrollPanes(pane, c, count - 1, index, gameTilesPoints[index][count - 1]);
            }
        }
        while (count < gameTilesPoints[index].length) {
            gameTilesPoints[index][count++] = 0;
            updateScrollPanes(pane, c, count - 1, index, 0);
        }
    }

    private void pushZerosLeft(Container pane, GridBagConstraints c, int gameTilesPoints[][], int index) {
        int i, j;
        for (j = i = gameTilesPoints[index].length - 1; i >= 0; i--) {
            if (gameTilesPoints[index][i] == 0) {
                continue;
            }
            gameTilesPoints[index][j] = gameTilesPoints[index][i];
            updateScrollPanes(pane, c, j, index, gameTilesPoints[index][j]);
            j--;
        }
        while (j >= 0) {
            gameTilesPoints[index][j] = 0;
            updateScrollPanes(pane, c, j, index, 0);
            j--;
        }
    }

    private void pushZerosUpwards(Container pane, GridBagConstraints c, int gameTilesPoints[][], int index) {
        int i, j;
        for (j = i = gameTilesPoints.length - 1; i >= 0; i--) {
            if (gameTilesPoints[i][index] == 0) {
                continue;
            }
            gameTilesPoints[j][index] = gameTilesPoints[i][index];
            updateScrollPanes(pane, c, index, j, gameTilesPoints[j][index]);
            j--;
        }
        while (j >= 0) {
            gameTilesPoints[j][index] = 0;
            updateScrollPanes(pane, c, index, j, 0);
            j--;
        }
    }

    private void pushZerosDownwards(Container pane, GridBagConstraints c, int gameTilesPoints[][], int index) {
        int count = 0;
        for (int i = 0; i < gameTilesPoints.length; i++) {
            if (gameTilesPoints[i][index] != 0) {
                gameTilesPoints[count++][index] = gameTilesPoints[i][index];
                updateScrollPanes(pane, c, index, count - 1, gameTilesPoints[count - 1][index]);
            }
        }
        while (count < gameTilesPoints.length) {
            gameTilesPoints[count++][index] = 0;
            updateScrollPanes(pane, c, index, count - 1, 0);
        }
    }

    //all the combineSimilarTiles*Direction* functions check if there are nearby tiles with same value
    //if true, combine them to the respective direction
    //updates values, panes, score
    //pushes any remaining zeros to the opposite direction
    private void combineSimilarTilesLeft(Container pane, GridBagConstraints c, int gameTilesPoints[][], int index) {
        for (int j = 0; j < gameTilesPoints[index].length - 1; j++) {
            if (gameTilesPoints[index][j] == gameTilesPoints[index][j + 1]) {
                gameTilesPoints[index][j] *= 2;
                gameTilesPoints[index][j + 1] = 0;
                updateScrollPanes(pane, c, j, index, gameTilesPoints[index][j]);
                updateScrollPanes(pane, c, j + 1, index, 0);
                updateScore(gameTilesPoints[index][j]);
                pushZerosRight(pane, c, gameTilesPoints, index);
            }
        }
    }

    private void combineSimilarTilesRight(Container pane, GridBagConstraints c, int gameTilesPoints[][], int index) {
        for (int j = gameTilesPoints[index].length - 1; j > 0; j--) {
            if (gameTilesPoints[index][j] == gameTilesPoints[index][j - 1]) {
                gameTilesPoints[index][j] *= 2;
                gameTilesPoints[index][j - 1] = 0;
                updateScrollPanes(pane, c, j, index, gameTilesPoints[index][j]);
                updateScrollPanes(pane, c, j - 1, index, 0);
                updateScore(gameTilesPoints[index][j]);
                pushZerosLeft(pane, c, gameTilesPoints, index);
            }
        }
    }

    private void combineSimilarTilesUp(Container pane, GridBagConstraints c, int gameTilesPoints[][], int index) {
        for (int i = 0; i < gameTilesPoints.length - 1; i++) {
            if (gameTilesPoints[i][index] == gameTilesPoints[i + 1][index]) {
                gameTilesPoints[i][index] *= 2;
                gameTilesPoints[i + 1][index] = 0;
                updateScrollPanes(pane, c, index, i, gameTilesPoints[i][index]);
                updateScrollPanes(pane, c, index, i + 1, 0);
                updateScore(gameTilesPoints[i][index]);
                pushZerosDownwards(pane, c, gameTilesPoints, index);
            }
        }
    }

    private void combineSimilarTilesDown(Container pane, GridBagConstraints c, int gameTilesPoints[][], int index) {
        for (int i = gameTilesPoints.length - 1; i > 0; i--) {
            if (gameTilesPoints[i][index] == gameTilesPoints[i - 1][index]) {
                gameTilesPoints[i][index] *= 2;
                gameTilesPoints[i - 1][index] = 0;
                updateScrollPanes(pane, c, index, i, gameTilesPoints[i][index]);
                updateScrollPanes(pane, c, index, i - 1, 0);
                updateScore(gameTilesPoints[i][index]);
                pushZerosUpwards(pane, c, gameTilesPoints, index);

            }
        }
    }

    //all the move*Direction* functions move zeros to the opposite direction
    //and then combine similar nearby tiles to the respective direction
    private void moveLeft(Container pane, GridBagConstraints c) {
        for (int i = 0; i < gameTilesPoints.length; i++) {
            pushZerosRight(pane, c, gameTilesPoints, i);
            combineSimilarTilesLeft(pane, c, gameTilesPoints, i);
        }
    }

    private void moveRight(Container pane, GridBagConstraints c) {
        for (int i = 0; i < gameTilesPoints.length; i++) {
            pushZerosLeft(pane, c, gameTilesPoints, i);
            combineSimilarTilesRight(pane, c, gameTilesPoints, i);
        }
    }

    private void moveUp(Container pane, GridBagConstraints c) {
        for (int j = 0; j < gameTilesPoints[0].length; j++) {
            pushZerosDownwards(pane, c, gameTilesPoints, j);
            combineSimilarTilesUp(pane, c, gameTilesPoints, j);
        }
    }

    private void moveDown(Container pane, GridBagConstraints c) {
        for (int j = 0; j < gameTilesPoints[0].length; j++) {
            pushZerosUpwards(pane, c, gameTilesPoints, j);
            combineSimilarTilesDown(pane, c, gameTilesPoints, j);
        }
    }

    //play sound by finding its' path
    private void playSound(String filename) {
        File soundFile = new File(filename);
        AudioInputStream audioIn = null;
        try {
            audioIn = AudioSystem.getAudioInputStream(soundFile);
        } catch (UnsupportedAudioFileException | IOException ex) {
            Logger.getLogger(JavaGame2048.class.getName()).log(Level.SEVERE, null, ex);
        }
        Clip clip = null;
        try {
            clip = AudioSystem.getClip();
        } catch (LineUnavailableException ex) {
            Logger.getLogger(JavaGame2048.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            clip.open(audioIn);
        } catch (LineUnavailableException | IOException ex) {
            Logger.getLogger(JavaGame2048.class.getName()).log(Level.SEVERE, null, ex);
        }
        clip.start();
    }

//requesting focus when frame is clicked, so game can still be played
    private void requestFrameFocus(JFrame frame) {
        frame.toFront();
        frame.requestFocus();
    }

    private int randNumber(int nr) {
        //return a number between 0 and nr, including 0 and less than nr
        return (int) (Math.random() * nr);
    }

    private void addRandomTile(Container pane, GridBagConstraints c) {
        //if the board is not full, add a random tile on the board and update frame
        if (!isFullCheck()) {
            int randX;
            int randY;
            //choosing random coordinates until it finds a free tile (with a value of 0)
            do {
                randX = randNumber(gameTilesPoints[0].length);
                randY = randNumber(gameTilesPoints.length);
            } while (gameTilesPoints[randY][randX] != 0);
            updateScrollPanes(pane, c, randX, randY, 2);
        }
    }

    //changing score to reach
    private void changeScore(Container pane, GridBagConstraints c) {
        String parseText = gameTextScoreToReach.getText();
        //if text in field is numeric and bigger or equal than 4,
        //change score, change title and reset game
        if (!"".equals(parseText) && parseText.matches("[+]?\\d*\\.?\\d+")
                && Integer.parseInt(parseText) >= 4
                && Integer.parseInt(parseText) <= Integer.MAX_VALUE) {
            gameScoreToReach = Integer.parseInt(gameTextScoreToReach.getText());
            gameFrame.setTitle(Integer.toString(gameScoreToReach) + " Game");
            try {
                resetGame(pane, c);

            } catch (IOException ex) {
                Logger.getLogger(JavaGame2048.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }

        //set text to blank after pressing button
        gameTextScoreToReach.setText("");

        //requesting focus when frame is clicked, so game can still be played
        requestFrameFocus(gameFrame);
    }

    private void resetGame(Container pane, GridBagConstraints c) throws IOException {

        //reset all the panes' value to 0
        for (int i = 0; i < gameTilesPoints.length; i++) {
            for (int j = 0; j < gameTilesPoints[0].length; j++) {
                updateScrollPanes(pane, c, j, i, 0);
            }
        }

        //adding random tiles to the board
        initPlay(pane, c, gameTilesPoints.length, gameTilesPoints[0].length);

        //updating score back to 0 by decreasing it by itself
        updateScore(-gameScoreInt);

        //updating game status to be blank
        updateGameStatus("Playing");

        //update label text on the current score to reach
        gameHowToPlay[1].setText("<html>Press ESC to restart. Change score to reach below.<br/>Reach " + gameScoreToReach + " to win!</html>");
        gameHowToPlay[1].setForeground(Color.WHITE);
        gameHowToPlay[1].setFont(new Font("Arial", Font.PLAIN, 12));

        //resetting bools for game status
        wonGame = false;
        lostGame = false;
    }

    private void initPlay(Container pane, GridBagConstraints c, int gameRows, int gameColumns) {
        //initializing a 2d array for keeping track of the tiles' values
        //using gameTilesPoints.length for number of rows
        //and gameTilesPoints[index].length for number of columns
        gameTilesPoints = new int[gameRows][gameColumns];

        //choosing how many random tiles to add depending on the number of tiles
        int nrOfRandomTiles;
        if (gameRows + gameColumns > 3) {
            nrOfRandomTiles = 2;
        } else {
            nrOfRandomTiles = 1;
        }

        //add a number of random tiles to the board
        for (int i = 0; i < nrOfRandomTiles; i++) {
            addRandomTile(pane, c);
        }

        //un-comment those if you want to add a tile with a certain value,
        //in a certain position
        //3rd parameter: column
        //4th parameter: row
        //5th parameter: value
        //updateScrollPanes(pane, c, 0, 0, 1024);
        //updateScrollPanes(pane, c, 0, 1, 1024);
    }

    private void play(JFrame frame, Container pane, GridBagConstraints c) {

        //required for the key listener to work
        frame.setFocusable(true);

        //adding key listener for arrows and escape
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                //if ESC is pressed, reset the game
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    try {
                        resetGame(pane, c);

                    } catch (IOException ex) {
                        Logger.getLogger(JavaGame2048.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                }

                //if the game hasn't been won or lost, listen for arrows (movement)
                if (!winCheck() && !loseCheck()) {
                    switch (e.getKeyCode()) {
                        //move and combine tiles to chosen direction and then add a random tile
                        case KeyEvent.VK_LEFT: //left arrow
                            moveLeft(pane, c);
                            playSound("Assets/Sounds/moveSound.wav");
                            addRandomTile(pane, c);
                            break;
                        case KeyEvent.VK_RIGHT: //right arrow
                            moveRight(pane, c);
                            playSound("Assets/Sounds/moveSound.wav");
                            addRandomTile(pane, c);
                            break;
                        case KeyEvent.VK_DOWN: //down arrow
                            moveDown(pane, c);
                            playSound("Assets/Sounds/moveSound.wav");
                            addRandomTile(pane, c);
                            break;
                        case KeyEvent.VK_UP: //up arrow
                            moveUp(pane, c);
                            playSound("Assets/Sounds/moveSound.wav");
                            addRandomTile(pane, c);
                            break;
                    }
                }

                //if there is a tile with a certain value (default: 2048), update status about game
                if (winCheck()) {
                    if (wonGame == false) {
                        playSound("Assets/Sounds/winSound.wav");
                        wonGame = true;
                    }
                    updateGameStatus("Won");

                } else {
                    //if no tile can be moved, update status about game
                    if (!canMoveCheck()) {
                        if (lostGame == false) {
                            playSound("Assets/Sounds/loseSound.wav");
                            lostGame = true;
                        }
                        updateGameStatus("Lost");
                    }
                }
            }
        });
    }

    private void initGuiAndPlay(JFrame gameFrame, Container pane) throws IOException {

        //initializing number of rows and columns for the game board
        int gameRows = 4;
        int gameColumns = 4;

        //setting up the frame
        ImageIcon gameIcon = new ImageIcon("Assets/Images/gameIcon.png");
        gameFrame.setIconImage(gameIcon.getImage());
        gameFrame.setTitle(Integer.toString(gameScoreToReach) + " Game");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setSize(400, 400);
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setResizable(false);

        //requesting focus when frame is clicked, so game can still be played
        gameFrame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFrameFocus(gameFrame);
            }
        });

        //setting up the pane and the layout
        pane.setLayout(new GridBagLayout());
        pane.setBackground(new Color(0x776e65));
        GridBagConstraints c = new GridBagConstraints();

        //initializing scroll panes for showing tiles' values
        gameScrollPanes = new JScrollPane[gameRows][gameColumns];

        //initializing label for score tracking
        gameScore = new JLabel();
        gameScore.setForeground(Color.WHITE);
        gameScore.setFont(new Font("Arial", Font.PLAIN, 12));
        updateScore(0);

        //initializing label for game status (won/lost)
        gameStatus = new JLabel();
        gameStatus.setForeground(Color.WHITE);
        gameStatus.setFont(new Font("Arial", Font.PLAIN, 12));
        updateGameStatus("Playing");

        //initializing text field to input and modify score
        gameTextScoreToReach = new JTextField();
        gameTextScoreToReach.setColumns(10);
        //implementing a listener for enter button
        gameTextScoreToReach.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    changeScore(pane, c);
                }
            }
        });

        //creating a button to modify the game score
        gameButtonScoreToReach = new JButton();
        gameButtonScoreToReach.setText("Change Score");
        gameButtonScoreToReach.setFont(new Font("Arial", Font.PLAIN, 12));
        //implementing a listener for button press
        gameButtonScoreToReach.addActionListener((ActionEvent e) -> {
            changeScore(pane, c);
        });

        //initializing label for instructions on how to play the game
        gameHowToPlay = new JLabel[2];
        //creating a label with newline and reconstructing its' text and font
        gameHowToPlay[0] = new JLabel("<html>Use your arrow keys to move the tiles.<br/>When 2 tiles with the same numbers touch, they merge into one!</html>", SwingConstants.CENTER);
        gameHowToPlay[1] = new JLabel("<html>Press ESC to restart. Change score to reach below.<br/>Reach " + gameScoreToReach + " to win!</html>", SwingConstants.LEFT);
        for (int i = 0; i < gameHowToPlay.length; i++) {
            gameHowToPlay[i].setForeground(Color.WHITE);
            gameHowToPlay[i].setFont(new Font("Arial", Font.PLAIN, 12));
        }

        //setting up the fill layout
        c.fill = GridBagConstraints.HORIZONTAL;

        //set up scroll panes on the GUI
        for (int i = 0; i < gameRows; i++) {
            for (int j = 0; j < gameColumns; j++) {
                //set up each individual scroll pane with a value of 0 and its' linked color
                gameScrollPanes[i][j] = new JScrollPane(new paintScrollPanes(0));

                //setting up size for the panes
                gameScrollPanes[i][j].setPreferredSize(new Dimension(75, 75));
                gameScrollPanes[i][j].setMinimumSize(new Dimension(75, 75));

                //aligning the panes
                c.gridx = j;
                c.gridy = i;
                c.insets = new Insets(15, 15, 15, 15);

                //adding the panes
                pane.add(gameScrollPanes[i][j], c);
            }
        }

        //add remaining labels to the side of the pane
        for (int i = 0; i < gameHowToPlay.length; i++) {
            c.gridx = gameColumns;
            c.gridy = i;
            pane.add(gameHowToPlay[i], c);
        }

        c.gridx = gameColumns;
        c.gridy = 2;
        pane.add(gameScore, c);

        c.gridx = gameColumns + 1;
        c.gridy = 2;
        pane.add(gameStatus, c);

        c.gridx = gameColumns;
        c.gridy = 3;
        pane.add(gameButtonScoreToReach, c);

        c.gridx = gameColumns + 1;
        c.gridy = 3;
        pane.add(gameTextScoreToReach, c);

        //packing the components together
        gameFrame.pack();

        //show the GUI
        gameFrame.setVisible(true);

        //initialize board with random values
        initPlay(pane, c, gameRows, gameColumns);

        //directing to a key listener
        play(gameFrame, pane, c);

    }

    class paintScrollPanes extends JPanel {

        //setting up an integer for remembering a tiles' value
        private final int mTilePoints;

        //passing the value to the integer
        private paintScrollPanes(int tilePoints) {
            mTilePoints = tilePoints;
        }

        @Override
        protected void paintComponent(Graphics g) {

            //setting up string for checking the tiles' value
            String tilePoints = "";
            if (mTilePoints != 0) {
                tilePoints = Integer.toString(mTilePoints);
            }

            super.paintComponent(g);

            //set a new color for the scroll pane for each different value
            switch (mTilePoints) {
                case 2:
                    g.setColor(new Color(0xeee4da));
                    break;
                case 4:
                    g.setColor(new Color(0xede0c8));
                    break;
                case 8:
                    g.setColor(new Color(0xf2b179));
                    break;
                case 16:
                    g.setColor(new Color(0xf59563));
                    break;
                case 32:
                    g.setColor(new Color(0xf67c5f));
                    break;
                case 64:
                    g.setColor(new Color(0xf65e3b));
                    break;
                case 128:
                    g.setColor(new Color(0xedcf72));
                    break;
                case 256:
                    g.setColor(new Color(0xedcc61));
                    break;
                case 512:
                    g.setColor(new Color(0xedc850));
                    break;
                case 1024:
                    g.setColor(new Color(0xedc53f));
                    break;
                case 2048:
                    g.setColor(new Color(0xedc22e));
                    break;
                default:
                    g.setColor(new Color(0xcdc1b4));
                    break;
            }

            //drawing background of the scroll pane depending on its' value
            g.fillRect(0, 0, getWidth(), getHeight());

            //drawing the tiles' value to show in center of pane
            //using custom font for displaying tiles' value
            Font customFont = null;
            try {
                customFont = Font.createFont(Font.TRUETYPE_FONT, new File("Assets/Fonts/ClearSans-Bold.ttf")).deriveFont(20f);
            } catch (FontFormatException | IOException ex) {
                Logger.getLogger(JavaGame2048.class.getName()).log(Level.SEVERE, null, ex);
            }
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            try {
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("Assets/Fonts/ClearSans-Bold.ttf")));
            } catch (FontFormatException | IOException ex) {
                Logger.getLogger(JavaGame2048.class.getName()).log(Level.SEVERE, null, ex);
            }
            g.setColor(Color.BLACK);
            //drawing in the center of the square using metrics
            FontMetrics metrics = g.getFontMetrics(customFont);
            int x = (getWidth() - metrics.stringWidth(tilePoints)) / 2;
            int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();
            g.setFont(customFont);
            g.drawString(tilePoints, x, y);
        }
    }

    public static void main(String[] args) throws IOException {
        //Dispatching an instance for creating and showing the GUI 
        SwingUtilities.invokeLater(() -> {
            try {
                JavaGame2048 javaGame2048 = new JavaGame2048();

            } catch (IOException ex) {
                Logger.getLogger(JavaGame2048.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        });

    }
}
