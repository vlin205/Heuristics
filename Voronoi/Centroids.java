import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

public class Centroids {

	static int boardSize;
	static int totalStones;
	static int totalPlayers;
	static int playerNumber;//NOT zero-indexed
	static Stone[][] board = new Stone[400][400];
	static int[][] stoneBoard = new int[400][400];
	static ArrayList<Integer> stonesLeftList = new ArrayList<Integer>(1);//zero-indexed
	static ArrayList<Stone> stonesList = new ArrayList<Stone>(1);
	static ArrayList<Polygon> polygonList = new ArrayList<Polygon>(1);//zero-indexed, but correct playerNumbers

	public static void main(String[] args) throws Exception {
		Socket socket = null;
	    PrintWriter out = null;
	    BufferedReader in = null;
		String portString = args[0];
		int port = Integer.parseInt(portString);
	    try {
	        socket = new Socket("localhost", port);
	        out = new PrintWriter(socket.getOutputStream(), true);
	        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    }
	    catch (UnknownHostException e) {
	        System.err.println("Don't know about host: localhost.");
	        System.exit(1);
	    }
	    catch (IOException e) {
	        System.err.println("Couldn't get I/O for the connection to: localhost.");
	        System.exit(1);
	    }

	    BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
	    String fromServer;
	    String fromUser;

	    while ((fromServer = in.readLine()) != null) {
	        System.out.println("Server: " + fromServer);
	        if (fromServer.equals("WIN") || fromServer.equals("LOSE"))
	            break;
	        if (fromServer.equals("YOURTURN")) {
	        	if(stonesLeftList.get(playerNumber - 1) == totalStones) {
	        		int tempX = (int)(100 + 50*Math.random());
	        		int tempY = (int)(75 + 50*Math.random());
	        		out.println(tempX + " " + tempY);
	        		Stone temp = new Stone(tempX, tempY, playerNumber);
	        		stonesList.add(temp);
					stonesLeftList.set((playerNumber - 1), (stonesLeftList.get(playerNumber - 1) - 1));
	       		 	createPolygons(stoneBoard, polygonList, stonesList);
	        	}
	        	else {
	        		int lastMoveFinder = 0;
	        		for(int i = 0; i < stonesLeftList.size(); i++) {
	        			lastMoveFinder += stonesLeftList.get(i);
	        		}
	        		if(lastMoveFinder == 1){
	        			Stone tempMove = getGreedy();
	        			out.println(tempMove.getX() + " " + tempMove.getY());
	        		}
	        		else {
			        	Stone temp = findMove();
			        	stonesList.add(temp);
						stonesLeftList.set((playerNumber - 1), (stonesLeftList.get(playerNumber - 1) - 1));
			        	createPolygons(stoneBoard, polygonList, stonesList);
			        	out.println(temp.getX() + " " + temp.getY());
	        		}
	        	}
	        }
	        else {
				updateBoard(fromServer);
//	           	out.println(toRemove[0] + "," + toRemove[1]);
	        }
	    }
	    out.close();
	    in.close();
	    stdIn.close();
	    socket.close();
	}

	public static void initStoneBoard() {
		for(int i = 0; i < boardSize; i++) {
			for(int j = 0; j < boardSize; j++) {
				stoneBoard[i][j] = 0; //0 indicates border point, -1 indicates stone placed, >0 indicates player it belongs to
			}
		}
	}

	public static Stone findMove() {
		ArrayList<Stone> tempStoneHolder = stonesList;
		int opp = getLargestPolygons(tempStoneHolder);
		Stone oppStone = tempStoneHolder.get(opp);
		Point tempPoint = oppStone.findCenter();
		Stone move = new Stone(tempPoint.getX(), tempPoint.getY(), playerNumber);
		return move;
	}

	public static ArrayList<Stone> getClosestStones(int x, int y, ArrayList<Stone> tempStonesList) {
		double minDistance = 400*400;
		double tempDistance = 400*400 + 1;
		ArrayList<Stone> closestStonesList = new ArrayList<Stone>(1);
		for(int i = 0; i < tempStonesList.size(); i++) {
			Stone temp = tempStonesList.get(i);
			tempDistance = Math.sqrt(Math.pow((x - temp.getX()), 2) + Math.pow((y - temp.getY()), 2));
			if(tempDistance < minDistance) {
				closestStonesList.clear();
				closestStonesList.trimToSize();
				closestStonesList.add(temp);
				minDistance = tempDistance;
			}
			else if(tempDistance == minDistance) {
				closestStonesList.add(temp);
			}
		}
		return closestStonesList;
	}

	public static void updateBoard(String str) {
		String[] inputString = str.split("\\s");
		for(int i = 0; i < inputString.length; i++) {
			System.out.print("|" + inputString[i] + "|");
		}
		System.out.println();
		if(inputString.length == 3) {
			int x = Integer.parseInt(inputString[0]);
			int y = Integer.parseInt(inputString[1]);
			int player = Integer.parseInt(inputString[2]);
			Stone temp = new Stone(x, y, player);
			stonesLeftList.set((player - 1), (stonesLeftList.get(player - 1) - 1));
			stonesList.add(temp);
			board[x][y] = temp;
			stoneBoard[x][y] = -1;
			createPolygons(stoneBoard, polygonList, stonesList);
		}
		else if (inputString.length == 4) { //if the 400 board size is included in the first input
			boardSize = Integer.parseInt(inputString[0]);
			totalStones = Integer.parseInt(inputString[1]);
			totalPlayers = Integer.parseInt(inputString[2]);
			playerNumber = Integer.parseInt(inputString[3]);
			for(int i = 0; i < totalPlayers; i++) {
				Polygon temp = new Polygon(i + 1);
				polygonList.add(temp);
				stonesLeftList.add(totalStones);
			}
			initStoneBoard();
		}
	}

	public static void createPolygons(int[][] tempStoneBoard, ArrayList<Polygon> tempPolygonList, ArrayList<Stone> tempStonesList) {
		for(int i = 0; i < tempPolygonList.size(); i++) {
			tempPolygonList.get(i).clear();
		}
		for(int i = 0; i < tempStonesList.size(); i++) {
			tempStonesList.get(i).clearSize();
		}
		for(int i = 0; i < 400; i++) {
			for(int j = 0; j < 400; j++) {
				if(tempStoneBoard[i][j] < 0) {//if -1, indicates a stone is in that place so we don't need to do anything
					continue;
				}
				else {
					ArrayList<Stone> temp = getClosestStones(i, j, tempStonesList);
					if(temp.size() > 1) {//if there isn't a single distinct closest stone
						tempStoneBoard[i][j] = 0;
					}
					else {//there is only 1 stone closest to this point
						int tempNum = temp.get(0).getPlayer();
						tempStoneBoard[i][j] = tempNum;
						tempPolygonList.get(tempNum - 1).add();
						Point tempPoint = new Point(i, j);
						temp.get(0).addPoint(tempPoint);
					}
				}
			}
		}
	}

	public static int getMyLargestPolygons(ArrayList<Stone> tempStoneList) {
		int[][] tempStoneBoard = stoneBoard;
		ArrayList<Polygon> tempPolygonList = polygonList;
		createPolygons(tempStoneBoard, tempPolygonList, tempStoneList);
		int max = 0;
		int largest = -1;
		for(int i = 0; i < tempStoneList.size(); i++) {
			if(tempStoneList.get(i).getPoints() > max && tempStoneList.get(i).getPlayer() == playerNumber) {
				largest = i;
				max = tempStoneList.get(i).getPoints();
			}
		}
		if(largest < 0) {
			System.out.println("Error 1");
		}
		return largest;
	}

	public static int getLargestPolygons(ArrayList<Stone> tempStoneList) {
		int[][] tempStoneBoard = stoneBoard;
		ArrayList<Polygon> tempPolygonList = polygonList;
		createPolygons(tempStoneBoard, tempPolygonList, tempStoneList);
		int max = 0;
		int largest = -1;
		for(int i = 0; i < tempStoneList.size(); i++) {
			if(tempStoneList.get(i).getPoints() > max && tempStoneList.get(i).getPlayer() != playerNumber) {
				largest = i;
				max = tempStoneList.get(i).getPoints();
			}
		}
		if(largest < 0) {
			System.out.println("Error 2");
		}
		return largest;
	}

	public static double getScore(Node n) {
		int depth = n.getDepth();
		Node current = n;
		ArrayList<Stone> tempStonesList = stonesList;
		ArrayList<Stone> tempHolder = new ArrayList<Stone>(depth);
		while(depth >= 0) {
			tempHolder.set(depth, current.getMove());
			current = current.getParent();
			depth = current.getDepth();
		}
		tempStonesList.addAll(tempHolder);
		ArrayList<Polygon> tempPolygonList = polygonList;
		int[][] tempBoard = stoneBoard;
		createPolygons(tempBoard, tempPolygonList, tempStonesList);
		return tempPolygonList.get(playerNumber - 1).getScore();
	}

	public static Stone getGreedy() {
		int moveX = 0;
		int moveY = 0;
		ArrayList<Stone> tempStoneHolder = stonesList;
		int opp = getLargestPolygons(tempStoneHolder);
		Stone oppStone = tempStoneHolder.get(opp);
		Point tempPoint = oppStone.findCenter();
		int x = oppStone.getX() - tempPoint.getX();
		int y = oppStone.getY() - tempPoint.getY();
		if(x >= 0 && y >= 0) {
			moveX = -1;
			moveY = -1;
		}
		else if(x < 0 && y < 0) {
			moveX = 1;
			moveY = 1;
		}
		else if(x >= 0 && y < 0) {
			moveX = -1;
			moveY = 1;
		}
		else {
			moveX = 1;
			moveY = -1;
		}
		Stone move = new Stone((oppStone.getX() + moveX), (oppStone.getY() + moveY), playerNumber);
		return move;
	}
}

class Stone {

	private int player;
	private int number;
	private int xloc;
	private int yloc;
	private int points;
	private ArrayList<Point> pointsList = new ArrayList<Point>(1);

	public Stone(int x, int y, int player) {
		xloc = x;
		yloc = y;
		this.player = player;
	}

	public int getX() {
		return xloc;
	}

	public int getY() {
		return yloc;
	}

	public int getPlayer() {
		return player;
	}

	public void addPoint(Point p) {
		pointsList.add(p);
//		points++;
	}

	public void clearSize() {
		pointsList.clear();
//		points = 0;
	}

	public int getPoints() {
		return pointsList.size();
//		return points;
	}

	public Point findCenter() {
		int tempX = 0;
		int tempY = 0;
		for(int i = 0; i < pointsList.size(); i++) {
			tempX += pointsList.get(i).getX();
			tempY += pointsList.get(i).getY();
		}
		double avgX = tempX/pointsList.size();
		double avgY = tempY/pointsList.size();
		Point temp = new Point((int)avgX, (int)avgY);
		return temp;
	}
}

class Point {

	private int xloc;
	private int yloc;
	private int stone;

	public Point(int x, int y) {
		xloc = x;
		yloc = y;
	}

	public int getX() {
		return xloc;
	}

	public int getY() {
		return yloc;
	}
}

class Polygon {

	private int score;
	private int player;

	public Polygon(int player) {
		this.player = player;
	}

	public void add() {
		score++;
	}

	public void clear() {
		score = 0;
	}

	public int getScore() {
		return score;
	}

	public int getPlayer() {
		return player;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public void setPlayer(int player) {
		this.player = player;
	}
}

class Node {

	private Node parent;
	private ArrayList<Node> children = new ArrayList<Node>(1);
	private int depth;
	private boolean traveresed;
	Stone move;

	public Node(Stone move) {
		traveresed = false;
		this.move = move;
	}

	public void addChild(Node child) {
		children.add(child);
	}

	public void setParent(Node parent) {
		this.parent = parent;
		parent.addChild(this);
		depth = parent.getDepth();
	}

	public void traverese() {
		traveresed = true;
	}

	public boolean isTraveresed() {
		return traveresed;
	}

	public ArrayList<Node> getChildren() {
		return children;
	}

	public Node getParent() {
		return parent;
	}

	public void setMove(Stone s) {
		move = s;
	}

	public Stone getMove() {
		return move;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int x) {
		depth = x;
	}

	public boolean isMin() {
		if(depth%2 == 0) {
			return false;
		}
		else {
			return true;
		}
	}
}

class Stoneindex {

	private int index;
	private int points;

	public Stoneindex(int a, int b) {
		index = a;
		points = b;
	}

	public int getPoints() {
		return points;
	}

	public int getIndex() {
		return index;
	}
}