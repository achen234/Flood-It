import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Arrays;
import java.util.Random;

// Represents a single square of the game area
class Cell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;
  int clicks;
  Color color;
  boolean flooded;
  // the four adjacent cells to this one
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;

  // constructor
  Cell(int x, int y, Color color, boolean flooded, Cell left, Cell top, Cell right, Cell bottom) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
    this.updateLeftCell(left);
    this.updateTopCell(top);
    this.updateRightCell(right);
    this.updateBottomCell(bottom);
  }

  // constructor for cell with no links
  Cell(int x, int y, Color color) {
    this(x, y, color, false, null, null, null, null);
  }

  // updates this cell's left to the given cell and the given cell's right to this
  void updateLeftCell(Cell that) {
    if (that != null) {
      that.right = this;
    }
    this.left = that;
  }

  // updates this cell's top to the given cell and the given cell's bottom to this
  void updateTopCell(Cell that) {
    if (that != null) {
      that.bottom = this;
    }
    this.top = that;
  }

  // updates this cell's right to the given cell and the given cell's left to this
  void updateRightCell(Cell that) {
    if (that != null) {
      that.left = this;
    }
    this.right = that;
  }

  // updates this cell's bottom to the given cell and the given cell's top to this
  void updateBottomCell(Cell that) {
    if (that != null) {
      that.top = this;
    }
    this.bottom = that;
  }

  // draws this cell as a square
  WorldImage drawCell() {
    return new RectangleImage(20, 20, "solid", this.color);
  }

  // checks whether the given color is the same as this cell's color and changes
  // the flood field and the cells of this cell's links
  void flood(Color original, int boardSize) {
    if (this.color.equals(original) && !this.flooded) {
      this.flooded = true;

      // top left
      if (this.x == 0 && this.y == 0) {
        this.right.flood(original, boardSize);
        this.bottom.flood(original, boardSize);
      }
      // top right
      else if (this.x == boardSize - 1 && this.y == 0) {
        this.bottom.flood(original, boardSize);
      }
      // bottom left
      else if (this.x == 0 && this.y == boardSize - 1) {
        this.top.flood(original, boardSize);
        this.right.flood(original, boardSize);
      }
      // bottom right
      else if (this.x == boardSize - 1 && this.y == boardSize - 1) {
        this.top.flood(original, boardSize);
        this.left.flood(original, boardSize);
      }
      // top edge
      else if (this.y == 0) {
        this.left.flood(original, boardSize);
        this.bottom.flood(original, boardSize);
        this.right.flood(original, boardSize);
      }
      // left edge
      else if (this.x == 0) {
        this.top.flood(original, boardSize);
        this.right.flood(original, boardSize);
        this.bottom.flood(original, boardSize);
      }
      // right edge
      else if (this.x == boardSize - 1) {
        this.top.flood(original, boardSize);
        this.left.flood(original, boardSize);
        this.bottom.flood(original, boardSize);
      }
      // bottom edge
      else if (this.y == boardSize - 1) {
        this.top.flood(original, boardSize);
        this.right.flood(original, boardSize);
        this.left.flood(original, boardSize);
      }
      else {
        this.left.flood(original, boardSize);
        this.top.flood(original, boardSize);
        this.right.flood(original, boardSize);
        this.bottom.flood(original, boardSize);
      }
    }
  }
}

// represents the flood it world
class FloodItWorld extends World {
  int boardSize;
  int numOfColors;
  // All the cells of the game
  ArrayList<Cell> board;
  ArrayList<Color> colors;
  Random rand;
  ArrayList<Cell> toFlood;
  int currentSteps;
  int totalSteps;
  double time;

  // random world constructor
  FloodItWorld(int boardSize, int numOfColors) {
    this(boardSize, numOfColors, new Random());
  }

  // original constructor
  FloodItWorld(int boardSize, int numOfColors, Random rand) {
    this.boardSize = boardSize;
    this.numOfColors = numOfColors;
    this.rand = rand;
    this.colors = this.makeColors();
    this.board = this.makeBoard();
    this.toFlood = new ArrayList<>(Arrays.asList(this.board.get(0)));
    this.currentSteps = 0;
    this.totalSteps = this.boardSize * 3 / 2 + this.numOfColors;
    this.time = 0;
  }

  // creates an array list of colors for this world of up to 10 colors
  ArrayList<Color> makeColors() {
    if (this.numOfColors > 9) {
      throw new IndexOutOfBoundsException("Too many colors");
    }
    ArrayList<Color> curr = new ArrayList<>();
    ArrayList<Color> original = new ArrayList<>(Arrays.asList(Color.red, Color.blue, Color.yellow,
        Color.green, Color.pink, Color.orange, Color.cyan, Color.magenta, Color.gray));
    // adds a color to the current array list based on the index
    for (int i = 0; i < this.numOfColors; i += 1) {
      curr.add(original.get(i));
    }
    return curr;
  }

  // creates an array list of random colored cells for the board of this world
  ArrayList<Cell> makeBoard() {
    ArrayList<Cell> curr = new ArrayList<>();
    // make boardSize cells of rows
    for (int row = 0; row < this.boardSize; row += 1) {
      // make boardSize cells of columns
      for (int column = 0; column < this.boardSize; column += 1) {
        int num = rand.nextInt(this.colors.size());
        Cell cell = new Cell(column, row, this.colors.get(num));
        curr.add(cell);
      }
    }

    // updates all the cells top, left, bottom, and right
    for (int i = 0; i < this.boardSize * this.boardSize; i += 1) {
      if (i == 0) {
        // nothing
      }
      else if (i < this.boardSize) {
        curr.get(i).updateLeftCell(curr.get(i - 1));
      }
      else if (i % this.boardSize == 0) {
        curr.get(i).updateTopCell(curr.get(i - this.boardSize));
      }
      else {
        curr.get(i).updateLeftCell(curr.get(i - 1));
        curr.get(i).updateTopCell(curr.get(i - this.boardSize));
      }
    }
    return curr;
  }

  // draws this world
  public WorldScene makeScene() {
    int currentSec = (int) this.time % 60;
    int currentMin = currentSec / 60;
    WorldScene empty = this.getEmptyScene();
    WorldImage board = this.drawBoard();
    WorldImage text = new TextImage(
        this.currentSteps + "/" + this.totalSteps + " " + currentMin + ":" + currentSec, 20,
        Color.black);
    if (currentSec < 10) {
      text = new TextImage(
          this.currentSteps + "/" + this.totalSteps + " " + currentMin + ":0" + currentSec, 20,
          Color.black);
    }
    WorldImage boardSteps = new AboveAlignImage("left", board, text);
    int posY = this.boardSize * 10;
    int posX = posY;
    if (posX < 40) {
      posX = 35;
    }
    empty.placeImageXY(boardSteps, posX, posY + 10);
    return empty;
  }

  // draws the final scene for this world given the message
  public WorldScene lastScene(String msg) {
    int currentSec = (int) this.time % 60;
    int currentMin = currentSec / 60;
    WorldScene empty = this.getEmptyScene();
    WorldImage board = this.drawBoard();
    WorldImage text = new TextImage(
        this.currentSteps + "/" + this.totalSteps + " " + currentMin + ":" + currentSec, 20,
        Color.black);
    if (currentSec < 10) {
      text = new TextImage(
          this.currentSteps + "/" + this.totalSteps + " " + currentMin + ":0" + currentSec, 20,
          Color.black);
    }
    WorldImage boardSteps = new AboveAlignImage("left", board, text);
    WorldImage boardMsg = new OverlayImage(new TextImage(msg, 20, Color.black), boardSteps);
    int posY = this.boardSize * 10;
    int posX = posY;
    if (posX < 40) {
      posX = 35;
    }
    empty.placeImageXY(boardMsg, posX, posY + 10);
    return empty;
  }

  // draws this world's board
  public WorldImage drawBoard() {
    WorldImage board = new EmptyImage();
    // draws each cells of the rows
    for (int row = 0; row < this.boardSize; row += 1) {
      WorldImage rowImage = new EmptyImage();
      // draws each cell of the columns
      for (int column = 0; column < this.boardSize; column += 1) {
        WorldImage cell = this.board.get(row * this.boardSize + column).drawCell();
        rowImage = new BesideImage(rowImage, cell);
      }
      board = new AboveImage(board, rowImage);
    }
    return board;
  }

  // floods this world's board and changes the color of the cells based on the
  // given posn
  public void onMouseClicked(Posn pos) {
    if (!this.board.get(0).flooded) {
      Posn adjusted = new Posn(pos.x / 20, pos.y / 20);
      Color color = this.findCellColor(adjusted);
      if (color != null) {
        this.floodBoard(color);
      }
    }
  }

  // changes this world's board given a different color from the top left cell's color of this board
  public void floodBoard(Color color) {
    Color original = this.board.get(0).color;
    if (!original.equals(color)) {
      this.board.get(0).color = color;
      this.board.get(0).flooded = true;
      this.board.get(0).right.flood(original, this.boardSize);
      this.board.get(0).bottom.flood(original, this.boardSize);
      this.currentSteps += 1;
    }
  }

  // resets this world's board given a key
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      this.board = this.makeBoard();
    }
  }

  // updates the world on each tick
  public void onTick() {

    this.time += .01;

    if (!this.board.get(0).equals(this.toFlood.get(0)) || this.board.get(0).flooded) {

      for (int index = 0; index < this.toFlood.size(); index += 1) {
        if (this.toFlood.get(index).flooded) {
          this.toFlood.get(index).color = this.board.get(0).color;
          this.toFlood.get(index).flooded = false;
        }
      }

      ArrayList<Cell> temp = new ArrayList<>();

      for (int index = 0; index < this.toFlood.size(); index += 1) {
        // bottom right
        if (this.toFlood.get(index).x == this.boardSize - 1
            && this.toFlood.get(index).y == this.boardSize - 1) {
          temp = new ArrayList<>(Arrays.asList(this.board.get(0)));
        }
        // left edge excluding bottom left
        else if (this.toFlood.get(index).x == 0
            && this.toFlood.get(index).y != this.boardSize - 1) {
          temp.add(this.toFlood.get(index).right);
          temp.add(this.toFlood.get(index).bottom);
        }
        // right edge
        else if (this.toFlood.get(index).x == this.boardSize - 1) {
          // nothing
        }
        else {
          temp.add(this.toFlood.get(index).right);
        }
        this.toFlood.get(index).flooded = false;
      }
      this.toFlood = temp;
    }
    boolean sameColor = true;
    for (int index = 0; index < this.board.size(); index += 1) {
      sameColor = sameColor && this.board.get(0).color.equals(this.board.get(index).color);
    }
    Cell lastItem = this.toFlood.get(this.toFlood.size() - 1);

    if (sameColor) {
      this.endOfWorld("You win!");
    }
    else if (currentSteps == this.totalSteps && lastItem.x == this.boardSize - 1
        && lastItem.y == this.boardSize - 1) {
      if (lastItem.flooded) {
        lastItem.color = this.board.get(0).color;
      }
      this.endOfWorld("You lose!");
    }
  }

  // returns the color matching the position of this world's board given a posn
  // else null
  public Color findCellColor(Posn pos) {
    for (Cell c : this.board) {
      if (c.x == pos.x && c.y == pos.y) {
        return c.color;
      }
    }
    return null;
  }
}

// examples and tests for flood it world
class ExamplesFloodIt {

  FloodItWorld twoByTwo1Color;
  FloodItWorld twoByTwoTest;
  FloodItWorld twoByTwoTest2;
  FloodItWorld twoByTwo;
  FloodItWorld three;
  FloodItWorld four;
  FloodItWorld tenByTen;
  FloodItWorld twentySixByTwentySix;

  Cell red;
  Cell green;
  Cell blue;
  Cell yellow;
  Cell pink;
  Cell orange;
  Cell gray;

  Cell cell1;
  Cell cell2;
  Cell cell3;
  Cell cell4;

  // initializes the cells
  void initData() {
    red = new Cell(0, 0, Color.red);
    green = new Cell(0, 0, Color.green);
    blue = new Cell(0, 0, Color.blue);
    yellow = new Cell(0, 0, Color.yellow);
    pink = new Cell(0, 0, Color.pink);
    orange = new Cell(0, 0, Color.orange);
    gray = new Cell(0, 0, Color.gray);

    cell1 = new Cell(0, 0, Color.red);
    cell2 = new Cell(1, 0, Color.red, false, cell1, null, null, null);
    cell3 = new Cell(0, 1, Color.blue, false, null, cell1, null, null);
    cell4 = new Cell(1, 1, Color.green, false, cell3, cell2, null, null);

    twoByTwo1Color = new FloodItWorld(2, 1);
    twoByTwoTest = new FloodItWorld(2, 4, new Random(1));
    twoByTwoTest2 = new FloodItWorld(2, 6, new Random(10));
    twoByTwo = new FloodItWorld(2, 4);
    three = new FloodItWorld(3, 4);
    four = new FloodItWorld(4, 4);
    tenByTen = new FloodItWorld(10, 6);
    twentySixByTwentySix = new FloodItWorld(26, 8);
  }

  // test for bigbang
  void testBigBang(Tester t) {
    this.initData();
    int worldHeight = this.tenByTen.boardSize * 20;
    int worldLength = worldHeight;
    if (worldLength < 80) {
      worldLength = 80;
    }
    double tickRate = .01;
    this.tenByTen.bigBang(worldLength, worldHeight + 20, tickRate);
  }

  // test the updateLeftCell method for a Cell
  void testUpdateLeftCell(Tester t) {
    this.initData();
    this.pink.updateLeftCell(this.blue);
    t.checkExpect(this.pink.left, this.blue);
    t.checkExpect(this.blue.right, this.pink);
    this.blue.updateLeftCell(this.green);
    t.checkExpect(this.blue.left, this.green);
    t.checkExpect(this.green.right, this.blue);
    this.pink.updateLeftCell(this.green);
    t.checkExpect(this.pink.left, this.green);
    t.checkExpect(this.green.right, this.pink);
  }

  // test the updateTopCell method for a Cell
  void testUpdateTopCell(Tester t) {
    this.initData();
    this.pink.updateTopCell(this.blue);
    t.checkExpect(this.pink.top, this.blue);
    t.checkExpect(this.blue.bottom, this.pink);
    this.red.updateTopCell(this.gray);
    t.checkExpect(this.red.top, this.gray);
    t.checkExpect(this.gray.bottom, this.red);
  }

  // test the updateRightCell method for a Cell
  void testUpdateRightCell(Tester t) {
    this.initData();
    this.yellow.updateRightCell(this.blue);
    t.checkExpect(this.yellow.right, this.blue);
    t.checkExpect(this.blue.left, this.yellow);
    this.blue.updateRightCell(this.orange);
    t.checkExpect(this.blue.right, this.orange);
    t.checkExpect(this.orange.left, this.blue);
  }

  // test the updateBottomCell method for a Cell
  void testUpdateBottomCell(Tester t) {
    this.initData();
    this.gray.updateBottomCell(this.blue);
    t.checkExpect(this.gray.bottom, this.blue);
    t.checkExpect(this.blue.top, this.gray);
    this.gray.updateBottomCell(this.gray);
    t.checkExpect(this.gray.bottom, this.gray);
    t.checkExpect(this.gray.top, this.gray);
  }

  // test the drawCell method for a cell
  void testDrawCell(Tester t) {
    this.initData();
    t.checkExpect(this.pink.drawCell(), new RectangleImage(20, 20, "solid", Color.pink));
    t.checkExpect(this.blue.drawCell(), new RectangleImage(20, 20, "solid", Color.blue));
    t.checkExpect(this.gray.drawCell(), new RectangleImage(20, 20, "solid", Color.gray));
  }

  // tests the flood method for class Cell
  void testFlood(Tester t) {
    this.initData();
    this.cell1.flood(Color.red, 2);
    t.checkExpect(this.cell1.flooded, true);
    t.checkExpect(this.cell2.flooded, true);
    t.checkExpect(this.cell3.flooded, false);
    t.checkExpect(this.cell4.flooded, false);
    this.initData();
    this.cell3.color = Color.red;
    Cell cell5 = new Cell(0, 2, Color.red, false, null, this.cell3, null, null);
    Cell cell6 = new Cell(1, 2, Color.black, false, cell5, cell4, null, null);
    Cell cell7 = new Cell(2, 2, Color.red, false, cell6, null, null, null);
    Cell cell8 = new Cell(2, 1, Color.red, false, cell4, null, null, cell7);
    Cell cell9 = new Cell(2, 0, Color.blue, false, cell2, null, null, cell8);
    this.cell1.flood(Color.red, 3);
    t.checkExpect(this.cell1.flooded, true);
    t.checkExpect(this.cell2.flooded, true);
    t.checkExpect(this.cell3.flooded, true);
    t.checkExpect(this.cell4.flooded, false);
    t.checkExpect(cell5.flooded, true);
    t.checkExpect(cell6.flooded, false);
    t.checkExpect(cell7.flooded, false);
    t.checkExpect(cell8.flooded, false);
    t.checkExpect(cell9.flooded, false);
  }

  // test the makeColors method for flood it world
  void testMakeColors(Tester t) {
    this.initData();
    t.checkConstructorException(new IndexOutOfBoundsException("Too many colors"), "FloodItWorld", 2,
        10);
    t.checkExpect(this.twoByTwo.colors,
        new ArrayList<>(Arrays.asList(Color.red, Color.blue, Color.yellow, Color.green)));
    t.checkExpect(this.tenByTen.colors, new ArrayList<>(
        Arrays.asList(Color.red, Color.blue, Color.yellow, Color.green, Color.pink, Color.orange)));
    t.checkExpect(this.twentySixByTwentySix.colors,
        new ArrayList<>(Arrays.asList(Color.red, Color.blue, Color.yellow, Color.green, Color.pink,
            Color.orange, Color.cyan, Color.magenta)));
  }

  // test the makeBoard method for flood it world
  void testMakeBoard(Tester t) {
    this.initData();
    Cell red1 = new Cell(1, 0, Color.red, false, this.red, null, null, null);
    Cell red2 = new Cell(0, 1, Color.red, false, null, this.red, null, null);
    Cell red3 = new Cell(1, 1, Color.red, false, red2, red1, null, null);
    t.checkExpect(this.twoByTwo1Color.makeBoard(),
        new ArrayList<>(Arrays.asList(this.red, red1, red2, red3)));

    this.initData();
    Cell red0 = new Cell(1, 0, Color.red, false, this.yellow, null, null, null);
    Cell blue2 = new Cell(0, 1, Color.blue, false, null, this.yellow, null, null);
    Cell blue3 = new Cell(1, 1, Color.blue, false, blue2, red0, null, null);
    t.checkExpect(this.twoByTwoTest.board,
        new ArrayList<>(Arrays.asList(this.yellow, red0, blue2, blue3)));
    t.checkExpect(this.twoByTwoTest.board.get(0), this.yellow);
    t.checkExpect(this.twoByTwoTest.board.get(1), red0);
    t.checkExpect(this.twoByTwoTest.board.get(2), blue2);
    t.checkExpect(this.twoByTwoTest.board.get(3), blue3);

    this.initData();
    Cell red5 = new Cell(1, 0, Color.red, false, this.green, null, null, null);
    Cell green2 = new Cell(0, 1, Color.green, false, null, this.green, null, null);
    Cell red6 = new Cell(1, 1, Color.red, false, green2, red5, null, null);
    t.checkExpect(this.twoByTwoTest2.board,
        new ArrayList<>(Arrays.asList(this.green, red5, green2, red6)));
    t.checkExpect(this.twoByTwoTest2.board.get(0).right, red5);
    t.checkExpect(this.twoByTwoTest2.board.get(1).bottom, red6);
    t.checkExpect(this.twoByTwoTest2.board.get(2).x, 0);
    t.checkExpect(this.twoByTwoTest2.board.get(2).y, 1);
    t.checkExpect(this.twoByTwoTest2.board.get(3).x, 1);
    t.checkExpect(this.twoByTwoTest2.board.get(3).y, 1);
  }

  // test the makeScene method for flood it world
  void testMakeScene(Tester t) {
    this.initData();
    WorldScene game = this.twoByTwoTest.getEmptyScene();
    WorldImage board = new AboveAlignImage("left",
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, "solid", Color.yellow)),
                    new RectangleImage(20, 20, "solid", Color.red))),
            new BesideImage(
                new BesideImage(new EmptyImage(), new RectangleImage(20, 20, "solid", Color.blue)),
                new RectangleImage(20, 20, "solid", Color.blue))),
        new TextImage(0 + "/" + 7 + " " + 0 + ":" + 0 + 0, 20, Color.black));
    game.placeImageXY(board, 35, 30);
    t.checkExpect(this.twoByTwoTest.makeScene(), game);

    WorldScene game2 = this.twoByTwoTest2.getEmptyScene();
    WorldImage board2 = new AboveImage(
        new AboveImage(new EmptyImage(),
            new BesideImage(
                new BesideImage(new EmptyImage(), new RectangleImage(20, 20, "solid", Color.green)),
                new RectangleImage(20, 20, "solid", Color.red))),
        new BesideImage(
            new BesideImage(new EmptyImage(), new RectangleImage(20, 20, "solid", Color.green)),
            new RectangleImage(20, 20, "solid", Color.red)));
    game2.placeImageXY(board2, 20, 20);
    t.checkExpect(this.twoByTwoTest2.makeScene(), game2);
  }

  // test the lastScene method for flood it world
  void testLastScene(Tester t) {
    this.initData();
    WorldScene game = this.twoByTwoTest.getEmptyScene();
    WorldImage board = new OverlayImage(new TextImage("You win!", 20, Color.black),
        new AboveAlignImage("left",
            new AboveImage(
                new AboveImage(new EmptyImage(),
                    new BesideImage(
                        new BesideImage(new EmptyImage(),
                            new RectangleImage(20, 20, "solid", Color.yellow)),
                        new RectangleImage(20, 20, "solid", Color.red))),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, "solid", Color.blue)),
                    new RectangleImage(20, 20, "solid", Color.blue))),
            new TextImage(0 + "/" + 7 + " " + 0 + ":" + 0 + 0, 20, Color.black)));
    game.placeImageXY(board, 35, 30);
    t.checkExpect(this.twoByTwoTest.lastScene("You win!"), game);

    WorldScene game2 = this.twoByTwoTest2.getEmptyScene();
    WorldImage board2 = new OverlayImage(new TextImage("You lose!", 20, Color.black),
        new AboveAlignImage("left",
            new AboveImage(
                new AboveImage(new EmptyImage(),
                    new BesideImage(
                        new BesideImage(new EmptyImage(),
                            new RectangleImage(20, 20, "solid", Color.green)),
                        new RectangleImage(20, 20, "solid", Color.red))),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, "solid", Color.green)),
                    new RectangleImage(20, 20, "solid", Color.red))),
            new TextImage(0 + "/" + 9 + " " + 0 + ":" + 0 + 0, Color.black)));
    game2.placeImageXY(board2, 35, 30);
    t.checkExpect(this.twoByTwoTest2.lastScene("You lose!"), game2);
  }

  // test the drawBoard method for flood it world
  void testDrawBoard(Tester t) {
    this.initData();
    t.checkExpect(this.twoByTwo1Color.drawBoard(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, "solid", Color.red)),
                    new RectangleImage(20, 20, "solid", Color.red))),
            new BesideImage(
                new BesideImage(new EmptyImage(), new RectangleImage(20, 20, "solid", Color.red)),
                new RectangleImage(20, 20, "solid", Color.red))));
    t.checkExpect(this.twoByTwoTest.drawBoard(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, "solid", Color.yellow)),
                    new RectangleImage(20, 20, "solid", Color.red))),
            new BesideImage(
                new BesideImage(new EmptyImage(), new RectangleImage(20, 20, "solid", Color.blue)),
                new RectangleImage(20, 20, "solid", Color.blue))));
    t.checkExpect(this.twoByTwoTest2.drawBoard(),
        new AboveImage(
            new AboveImage(new EmptyImage(),
                new BesideImage(
                    new BesideImage(new EmptyImage(),
                        new RectangleImage(20, 20, "solid", Color.green)),
                    new RectangleImage(20, 20, "solid", Color.red))),
            new BesideImage(
                new BesideImage(new EmptyImage(), new RectangleImage(20, 20, "solid", Color.green)),
                new RectangleImage(20, 20, "solid", Color.red))));
  }

  // tests the onMouseClicked method for class FloodItWorld
  void testOnMouseClicked(Tester t) {
    this.initData();
    this.twoByTwoTest.onMouseClicked(new Posn(5, 5));
    t.checkExpect(this.twoByTwoTest.board.get(0).color, Color.yellow);
    this.twoByTwoTest.onMouseClicked(new Posn(30, 5));
    t.checkExpect(this.twoByTwoTest.board.get(0).color, Color.red);
    t.checkExpect(this.twoByTwoTest.board.get(0).flooded, true);
    for (int i = 0; i < this.twoByTwoTest.boardSize * this.twoByTwoTest.boardSize; i += 1) {
      this.twoByTwoTest.board.get(i).flooded = false;
    }
    this.twoByTwoTest.onMouseClicked(new Posn(30, 30));
    for (int i = 0; i < this.twoByTwoTest.boardSize * this.twoByTwoTest.boardSize; i += 1) {
      this.twoByTwoTest.board.get(i).color = Color.blue;
    }
    t.checkExpect(this.twoByTwoTest.board.get(0).flooded, true);
    t.checkExpect(this.twoByTwoTest.board.get(1).flooded, true);
    t.checkExpect(this.twoByTwoTest.board.get(2).flooded, false);
    t.checkExpect(this.twoByTwoTest.board.get(3).flooded, false);
  }

  // tests the floodBoard method for class FloodItWorld
  void testFloodBoard(Tester t) {
    this.initData();
    this.twoByTwo1Color.floodBoard(Color.red);
    for (int i = 0; i < this.twoByTwo1Color.boardSize * 2; i += 1) {
      t.checkExpect(this.twoByTwo1Color.board.get(i).color, Color.red);
    }
    this.twoByTwo1Color.floodBoard(Color.yellow);
    t.checkExpect(this.twoByTwo1Color.board.get(0).color, Color.yellow);
    for (int i = 0; i < this.twoByTwo1Color.boardSize * 2; i += 1) {
      t.checkExpect(this.twoByTwo1Color.board.get(i).flooded, true);
    }
    this.twoByTwoTest2.floodBoard(Color.blue);
    t.checkExpect(this.twoByTwoTest2.board.get(0).color, Color.blue);
    t.checkExpect(this.twoByTwoTest2.board.get(0).flooded, true);
    t.checkExpect(this.twoByTwoTest2.board.get(1).flooded, false);
    t.checkExpect(this.twoByTwoTest2.board.get(2).flooded, true);
    t.checkExpect(this.twoByTwoTest2.board.get(3).flooded, false);
  }

  // test the onKeyEvent method for flood it world
  void testOnKeyEvent(Tester t) {
    this.initData();
    this.twoByTwoTest.onKeyEvent("b");
    t.checkExpect(this.twoByTwoTest, new FloodItWorld(2, 4, new Random(1)));
    this.twoByTwoTest.onKeyEvent("backspace");
    t.checkExpect(this.twoByTwoTest, new FloodItWorld(2, 4, new Random(1)));
    this.twoByTwoTest.onKeyEvent("r");
    Cell red = new Cell(0, 0, Color.red);
    Cell red2 = new Cell(1, 0, Color.red, false, red, null, null, null);
    Cell blue = new Cell(0, 1, Color.blue, false, null, red, null, null);
    Cell yellow = new Cell(1, 1, Color.yellow, false, blue, red2, null, null);
    t.checkExpect(this.twoByTwoTest.board,
        new ArrayList<Cell>(Arrays.asList(red, red2, blue, yellow)));
  }

  // tests the onTick method for class FloodItWorld
  void testOnTick(Tester t) {
    this.initData();
    this.twoByTwoTest.onTick();
    t.checkExpect(this.twoByTwoTest.toFlood,
        new ArrayList<>(Arrays.asList(this.twoByTwoTest.board.get(0))));
    t.checkExpect(this.twoByTwoTest.time, .01);
    this.twoByTwoTest.onTick();
    t.checkExpect(this.twoByTwoTest.time, .02);
    this.twoByTwoTest.floodBoard(Color.red);
    t.checkExpect(this.twoByTwoTest.board.get(0).color, Color.red);
    t.checkExpect(this.twoByTwoTest.board.get(0).flooded, true);
    this.twoByTwoTest.onTick();
    t.checkExpect(this.twoByTwoTest.time, .03);
    t.checkExpect(this.twoByTwoTest.board.get(0).color, Color.red);
    t.checkExpect(this.twoByTwoTest.board.get(0).flooded, false);
    t.checkExpect(this.twoByTwoTest.toFlood, new ArrayList<>(
        Arrays.asList(this.twoByTwoTest.board.get(1), this.twoByTwoTest.board.get(2))));
    t.checkExpect(this.twoByTwoTest.currentSteps, 1);
    this.twoByTwoTest.onTick();
    t.checkExpect(this.twoByTwoTest.time, .04);
    t.checkExpect(this.twoByTwoTest.toFlood,
        new ArrayList<>(Arrays.asList(this.twoByTwoTest.board.get(3))));
    this.twoByTwoTest.onTick();
    t.checkExpect(this.twoByTwoTest.time, .05);
    t.checkExpect(this.twoByTwoTest.toFlood,
        new ArrayList<>(Arrays.asList(this.twoByTwoTest.board.get(0))));
    this.twoByTwoTest.floodBoard(Color.blue);
    t.checkExpect(this.twoByTwoTest.board.get(0).color, Color.blue);
    t.checkExpect(this.twoByTwoTest.board.get(0).flooded, true);
    t.checkExpect(this.twoByTwoTest.board.get(1).flooded, true);
    this.twoByTwoTest.onTick();
    t.checkInexact(this.twoByTwoTest.time, .06, .001);
    t.checkExpect(this.twoByTwoTest.board.get(0).color, Color.blue);
    t.checkExpect(this.twoByTwoTest.board.get(1).color, Color.red);
    t.checkExpect(this.twoByTwoTest.board.get(0).flooded, false);
    t.checkExpect(this.twoByTwoTest.board.get(1).flooded, true);
    t.checkExpect(this.twoByTwoTest.toFlood, new ArrayList<>(
        Arrays.asList(this.twoByTwoTest.board.get(1), this.twoByTwoTest.board.get(2))));
    t.checkExpect(this.twoByTwoTest.currentSteps, 2);
    this.twoByTwoTest.onTick();
    t.checkExpect(this.twoByTwoTest.time, .07);
    t.checkExpect(this.twoByTwoTest.board.get(1).flooded, false);
    t.checkExpect(this.twoByTwoTest.toFlood,
        new ArrayList<>(Arrays.asList(this.twoByTwoTest.board.get(3))));
    for (int i = 0; i < this.twoByTwoTest.boardSize * this.twoByTwoTest.boardSize; i += 1) {
      t.checkExpect(this.twoByTwoTest.board.get(i).color, Color.blue);
    }
    for (int i = 0; i < this.twoByTwoTest.boardSize * this.twoByTwoTest.boardSize; i += 1) {
      t.checkExpect(this.twoByTwoTest.board.get(i).flooded, false);
    }
    this.twoByTwoTest.onTick();
    t.checkExpect(this.twoByTwoTest.time, .08);
    t.checkExpect(this.twoByTwoTest.toFlood,
        new ArrayList<>(Arrays.asList(this.twoByTwoTest.board.get(0))));
  }

  // test the findCellColor method for flood it world
  void testFindCellColor(Tester t) {
    this.initData();
    t.checkExpect(this.twoByTwoTest.findCellColor(new Posn(1, 2)), null);
    t.checkExpect(this.twoByTwoTest.findCellColor(new Posn(2, 0)), null);
    t.checkExpect(this.twoByTwoTest.findCellColor(new Posn(1, 0)), Color.red);
    t.checkExpect(this.twoByTwoTest2.findCellColor(new Posn(0, 0)), Color.green);
    t.checkExpect(this.twoByTwoTest2.findCellColor(new Posn(1, 1)), Color.red);
  }
}
