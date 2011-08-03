import java.util.*;

public class Salesman {
	public static Scanner in;
	final static int CITIES = 1001;//1+number of cities
	static int[][] cityList = new int[CITIES][3];//holds values taken from input
	static double[][] cityDistance = new double[CITIES][CITIES];//holds distance of all paths
	static int[] setS = new int[CITIES];//holds degree of all verticies
	static Edge[] forestF = new Edge[CITIES*CITIES];//the kruskal MST
	static Edge[] matchingM = new Edge[CITIES];//collection of matches
	static Edge[][] edgeList = new Edge[CITIES][CITIES];
	static ArrayList<Edge> multigraph = new ArrayList<Edge>(1);//F + M
	static ArrayList<Edge> multigraphH = new ArrayList<Edge>(1);//F + M
	static ArrayList<Edge> eulerCycle = new ArrayList<Edge>(1);
	static ArrayList<Edge> hamiltonCycle = new ArrayList<Edge>(1);
	static ArrayList<Integer> oddVerticesList = new ArrayList<Integer>(1);//list of all odd vertices
	static ArrayList<Integer> highDegreeVerticesList = new ArrayList<Integer>(1);//list of all vertices with degree higher than 2
	static Comparator<Edge> comparator = new MinHeapComparator();
	static PriorityQueue<Edge> MST = new PriorityQueue<Edge>(CITIES*CITIES, comparator);//temp holder of the MST edges
	static PriorityQueue<Edge> minMatch = new PriorityQueue<Edge>(CITIES*CITIES, comparator);
	static Vector<ArrayList<String>> pathGroups = new Vector<ArrayList<String>>(1);
	static Vector<ArrayList<Edge>> cycleGroups = new Vector<ArrayList<Edge>>(1);

	public static void main(String[] args) {
		long startTime = System.nanoTime();
		in = new Scanner(System.in);
		solve();
		double temp = 0;
		int max = 0;
		ArrayList<String> travelPath = makeHamiltonian(eulerCycle);
		int a = 0;
		int b = 0;
		for(int i = 0; i < travelPath.size(); i++){
			System.out.print(travelPath.get(i) + "	");
			if(i != 0){
				a = Integer.parseInt(travelPath.get(i - 1));
				b = Integer.parseInt(travelPath.get(i));
				temp += edgeList[a][b].getDistance();
//				System.out.println("Added: " + a + "," + b);
			}
		}
		System.out.println();
		System.out.println("Distance traveled = " + temp);
//		System.out.println(pathGroups.get(0).toString());//prints the only pathGroup that contains all vertices (if done right)
		long estimatedTime = System.nanoTime() - startTime;
		System.out.println("Runtime: " + (double)estimatedTime/1000000000 + " seconds");
	}

	public static void solve() {
		for(int i = 0; i < CITIES; i++)	{
			setS[i] = 0;
		}
		storeCities();
		calculateDistance();
		multigraphH.addAll(multigraph);
		createEulerCycle();
	}

	public static void storeCities() {
		System.out.println("Storing Cities");
		for(int i = 1; i < CITIES; i++)	{
			if(in.hasNextInt())	{
				int index = in.nextInt();
				for(int j = 0; j < 3; j++) {
					int temp = in.nextInt();
					cityList[i][j] = temp;
				}
				if(in.hasNextLine()) {
					String str = in.nextLine();
				}
			}
		}
	}

	public static void calculateDistance() {
		System.out.println("Calculating Distances");
		Edge[][] edge = new Edge[CITIES][CITIES];
		for(int i = 1; i < CITIES; i++)	{
			for(int j = 1; j < CITIES; j++)	{
				if(i == j) {
					edge[i][j] = new Edge(i, j, 0);
					continue;
				}
				else {
					double x = (double)(cityList[j][0] - cityList[i][0]);
					double y = (double)(cityList[j][1] - cityList[i][1]);
					double z = (double)(cityList[j][2] - cityList[i][2]);
					double temp = Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2);
					double distance = Math.sqrt(temp);
					cityDistance[i][j] = distance;
					edge[i][j] = new Edge(i, j, distance);
					edgeList[i][j] = new Edge(i, j, distance);
//					System.out.println(edge[i][j].toString());
				}
			}
		}
		createMST(edge);
		createMinimumMatching(edge);
	}

	public static void createMST(Edge[][] edge) {
		System.out.println("Creating MST");
		int forestPointer = 0;
		PriorityQueue<Edge> minHeap = new PriorityQueue<Edge>(CITIES*CITIES, comparator);
		for(int i = 1; i < CITIES; i++)	{
			for(int j = 1; j < CITIES; j++)	{
				if(i > j) {
					continue;
				}
				else if(edge[i][j].getDistance() > 0 && !minHeap.contains(edge[j][i]) && edge[i][j].getDistance() < 5000) {
					minHeap.add(edge[i][j]);
				}
			}
		}
		System.out.println("Generating Forest F");
		while(minHeap.size() != 0) {
			Edge temp = minHeap.poll();
			if(containsVertexTwo(temp)) {
				continue;
			}
			else {
				MST.add(temp);
				multigraph.add(temp);
				forestF[forestPointer] = temp;
				int x = forestF[forestPointer].getVertexOne();
				int y = forestF[forestPointer].getVertexTwo();
				setS[x]++;
				setS[y]++;
				forestPointer++;
			}
		}
//		for(int i = 0; i < forestPointer; i++) {
//			System.out.println(forestF[i].toString());//prints all of the edges of the Kruskal MST
//		}
		for(int i = 1; i < setS.length; i++) {
			if(setS[i] > 2)	{
				highDegreeVerticesList.add(i);
			}
		}
	}

	public static boolean containsVertexTwo(Edge edge) {
		boolean contains = true;
		int x = edge.getVertexOne();
		int y = edge.getVertexTwo();
		String a = "" + x;
		String b = "" + y;
		ArrayList<String> pathA = getPath(a);
		ArrayList<String> pathB = getPath(b);
		if(pathA == null) {//if a is not in a pre-existing path, we are adding it
			contains = false;
			if(pathB == null) {//if a and b are both not in pre-existing paths, we are adding the new path A-B to the group of paths
				ArrayList<String> temp = new ArrayList<String>(1);
				temp.add(a);
				temp.add(b);
				pathGroups.add(temp);
			}
			else {//if a is not in a pre-existing path but b is we are adding a to the path of b
				pathB.add(a);
			}
		}
		else if(pathB == null) {//if b is not in a existing path then add it to the path of a
			contains = false;//we skip the case where a and b are both not in pre-existing paths sicne it is done above
			pathA.add(b);
		}
		else {//both a and b are in pre-existing paths
			if(!pathA.equals(pathB)){//both a and b are in different paths
				contains = false;
				pathA.addAll(pathB);
				pathGroups.remove(pathB);
			}
		}
		return contains;
	}

	public static ArrayList<String> getPath(String a) {
		for(ArrayList<String> path : pathGroups) {
			if(path.contains(a)) {
				return path;
			}
		}
		return null;
	}

	public static void createMinimumMatching(Edge[][] edge)	{
		System.out.println("Creating Minimum Matching");
		for(int i = 0; i < setS.length; i++) {
			if(setS[i]%2 != 0) {
				oddVerticesList.add(i);
			}
		}
		int matchingPointer = 0;
		oddVerticesList.trimToSize();
		while(oddVerticesList.size() > 0) {
			int tempX = oddVerticesList.get(0);
			for(int j = 0; j < oddVerticesList.size(); j++) {
				int tempY = oddVerticesList.get(j);
				if(tempX == tempY || MST.contains(edge[tempX][tempY]) || MST.contains(edge[tempY][tempX])) {
					continue;
				}
				else {
					minMatch.add(edge[tempX][tempY]);
				}
			}
			Edge temp = minMatch.poll();
			setS[temp.getVertexOne()]++;//test
			setS[temp.getVertexTwo()]++;//test
			oddVerticesList.remove((Integer)temp.getVertexOne());
			oddVerticesList.remove((Integer)temp.getVertexTwo());
			oddVerticesList.trimToSize();
			matchingM[matchingPointer] = temp;
			multigraph.add(temp);
			minMatch.clear();
		}
	}

	public static void createEulerCycle() {
		System.out.println("Creating Euler Cycle");
		ArrayList<Edge> testCycle = new ArrayList<Edge>(1);
		while(multigraphH.size() != 0) {
			testCycle = testForCycle(multigraphH);
			cycleGroups.add(testCycle);
			multigraphH.removeAll(testCycle);
		}
		while(eulerCycle.size() != multigraph.size()) {
			combine();
		}
	}

	public static ArrayList<Edge> testForCycle(ArrayList<Edge> graphG) {
		ArrayList<Edge> tempCycle = new ArrayList<Edge>(1);
		int start = 0;
		int next = 0;
		Edge temp = new Edge(0,0,0);
		Edge current = new Edge(0,0,0);
		for(int i = 0; i < graphG.size(); i++) {
			if(setS[graphG.get(i).getVertexOne()] > 2) {
				temp = graphG.get(i);
				current = temp;
				start = temp.getVertexOne();
				next = temp.getVertexTwo();
				tempCycle.add(temp);
				break;
			}
			else if(setS[graphG.get(i).getVertexTwo()] > 2) {
				temp = graphG.get(i);
				current = temp;
				start = temp.getVertexTwo();
				next = temp.getVertexOne();
				tempCycle.add(temp);
				break;
			}
		}
		while(start != next) {
			for(int i = 0; i < graphG.size(); i++) {//start at 1 since edge 0 (temp) is already added
				if(next == graphG.get(i).getVertexOne() && !tempCycle.contains(graphG.get(i))) {
					current = graphG.get(i);
					tempCycle.add(current);
					next = current.getVertexTwo();
				}
				else if(next == graphG.get(i).getVertexTwo() && !tempCycle.contains(graphG.get(i))) {
					current = graphG.get(i);
					tempCycle.add(current);
					next = current.getVertexOne();
				}
			}
		}
		return tempCycle;
	}

	public static void combine() {
		if(eulerCycle.isEmpty()) {
			eulerCycle.addAll(cycleGroups.get(0));
			cycleGroups.remove(cycleGroups.get(0));
			for(int i = 0; i < cycleGroups.size(); i++) {
			}
		}
		else {
			for(int i = 0; i < cycleGroups.size(); i++){
				int temp = 0;
				for(int j = 0; j < highDegreeVerticesList.size(); j++) {
					temp = (Integer)highDegreeVerticesList.get(j);
					if(containsVertex(eulerCycle, temp) && containsVertex(cycleGroups.get(i), temp)) {
						combineCycles(eulerCycle, cycleGroups.get(i), temp);
						cycleGroups.remove(cycleGroups.get(i));
						break;
					}
				}
			}
		}
	}

	public static void combineCycles(ArrayList<Edge> cycleA, ArrayList<Edge> cycleB, int vertex) {
		Edge tempA = null;
		Edge tempB = null;
		int startAOne = 0;
		int startATwo = 0;
		int startBOne = 0;
		int startBTwo = 0;
		boolean aOrdered = false;
		boolean bOrdered = false;
		while(!aOrdered){
			tempA = cycleA.get(0);
			startAOne = tempA.getVertexOne();
			startATwo = tempA.getVertexTwo();
			if(startAOne == vertex || startATwo == vertex){
				aOrdered = true;
			}
			else{
				cycleA.remove(tempA);
				cycleA.add(tempA);
			}
		}
		if(vertex != cycleA.get(cycleA.size() - 1).getVertexOne() && vertex != cycleA.get(cycleA.size() - 1).getVertexTwo()){
			tempA = cycleA.get(0);
			cycleA.remove(tempA);
			cycleA.add(tempA);
		}
		while(!bOrdered){
			tempB = cycleB.get(0);
			startBOne = tempB.getVertexOne();
			startBTwo = tempB.getVertexTwo();
			if(startBOne == vertex || startBTwo == vertex){
				bOrdered = true;
			}
			else{
				cycleB.remove(tempB);
				cycleB.add(tempB);
			}
		}
		if(vertex != cycleB.get(cycleB.size() - 1).getVertexOne() && vertex != cycleB.get(cycleB.size() - 1).getVertexTwo()){
			tempB = cycleB.get(0);
			cycleB.remove(tempB);
			cycleB.add(tempB);
		}
		cycleA.addAll(cycleB);
	}

	public static boolean containsVertex(ArrayList<Edge> cycle, int vertex) {
		int tempX, tempY;
		for(int i = 0; i < cycle.size(); i++) {
			tempX = cycle.get(i).getVertexOne();
			tempY = cycle.get(i).getVertexTwo();
			if(vertex == tempX || vertex == tempY) {
				return true;
			}
		}
		return false;
	}

	public static ArrayList<String> makeHamiltonian(ArrayList<Edge> path) {
		System.out.println("Making Cycle Hamiltonian");
		boolean end = false;
		ArrayList<String> traversed = new ArrayList<String>(1);
		int current = 0;
		int next = 0;
		int temp = 0;
		int tempNext = 0;
		int tempCurrent = 0;
		int tempEnd = 0;
		for(int i = 0; i < path.size() - 1; i++){
			current = path.get(i).getVertexOne();
			next = path.get(i).getVertexTwo();
			if(current == path.get(i + 1).getVertexOne() || current == path.get(i + 1).getVertexTwo()){
				temp = current;
				current = next;
				next = temp;
			}
			traversed.add("" + current);
			if(traversed.contains("" + next)){
				if(i < path.size() - 2){
					path.remove(i);
					path.remove(i);
					if(path.get(i).getVertexOne() == current){
						hamiltonCycle.add(i, edgeList[current][path.get(i).getVertexTwo()]);
						path.add(i, edgeList[current][path.get(i).getVertexTwo()]);
					}
					else{
						hamiltonCycle.add(i, edgeList[path.get(i).getVertexTwo()][current]);
						path.add(i, edgeList[path.get(i).getVertexTwo()][current]);
					}
				}
				else{
//					System.out.println(i + "," + path.get(i + 1).toString() + "," + current + "," + next);
					tempCurrent = current;
					if(next == path.get(i + 1).getVertexOne()){
						tempNext = path.get(i + 1).getVertexTwo();
					}
					else{
						tempNext = path.get(i + 1).getVertexOne();
					}
//					System.out.println(tempCurrent + "," + tempNext);
					path.remove(i);
					path.remove(i);
					path.add(edgeList[tempCurrent][tempNext]);
				}
			}
			else {
				hamiltonCycle.add(path.get(i));
			}
			path.trimToSize();
		}
		if(current == path.get(path.size() - 1).getVertexOne() && end == false){
			traversed.add("" + path.get(path.size() - 1).getVertexTwo());
		}
		else if(current == path.get(path.size() - 1).getVertexTwo() && end == false){
			traversed.add("" + path.get(path.size() - 1).getVertexOne());
		}
		hamiltonCycle.add(path.get(path.size() - 1));
		return traversed;
	}
}

class Edge {
	private int vertexOne, vertexTwo;
	private double euclidDist;

	public Edge(int vertexOne, int vertexTwo, double euclidDist) {
		this.vertexOne = vertexOne;
		this.vertexTwo = vertexTwo;
		this.euclidDist = euclidDist;
	}

	public int getVertexOne() {
		return vertexOne;
	}

	public int getVertexTwo() {
		return vertexTwo;
	}

	public double getDistance() {
		return euclidDist;
	}

	public String toString() {
//		return "(" + vertexOne + "," + vertexTwo + "), Euclidean Distance is: " + euclidDist;
		return vertexOne + "	" + vertexTwo + "	 " + euclidDist;
	}
}

class MinHeapComparator implements Comparator<Edge> {
	@Override
	public int compare(Edge a, Edge b) {
		if(a.getDistance() > b.getDistance()) {
			return 1;
		}
		if(a.getDistance() < b.getDistance()) {
			return -1;
		}
		return 0;
	}
}