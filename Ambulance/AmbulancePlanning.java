import java.util.*;

public class AmbulancePlanning {

	public static Scanner in;
	static ArrayList<Patient> patientList = new ArrayList<Patient>(1);
	static ArrayList<Hospital> hospitalList = new ArrayList<Hospital>(1);
	static ArrayList<Patient> centroids = new ArrayList<Patient>(1);
	static ArrayList<Cluster> clusterList = new ArrayList<Cluster>(1);
	static ArrayList<Ambulance> ambulanceList = new ArrayList<Ambulance>(1);
	static ArrayList<Patient> savedList = new ArrayList<Patient>(1);
	static ArrayList<Patient> notSavedList = new ArrayList<Patient>(1);
	static int savedCount = 0;
	static int deadCount = 0;
	static String testString = "";

	public static void main(String[] args) {
		long startTime = System.nanoTime();
		in = new Scanner(System.in).useDelimiter("\\D+");
		genCity();
		int temporary = 9999;
		reconfigure();
		long estimatedTime = System.nanoTime() - startTime;
//		System.out.println("Runtime: " + (double)estimatedTime/1000000000 + " seconds"); //debugging
	}

	public static int reconfigure() {
		int placeHolder = 9999;
		while(placeHolder > 30) {
			initClustering();
			placeHolder = 0;
			for(int i = 0; i < clusterList.size(); i++) {
				placeHolder += clusterList.get(i).canNotSave();
			}
		}
//		System.out.println(placeHolder + " Patients cannot be saved");
//		System.out.println("Final centroids:");
		for(int i = 0; i < centroids.size(); i++) {
//			System.out.println(centroids.get(i).toString());
		}
		for(int i = 0; i < clusterList.size(); i++) {
//			System.out.print(clusterList.get(i).size() + " ");
		}
//		System.out.println();
//		for(int i = 0; i < clusterList.size(); i++) {
//			System.out.println("Can't save: " + clusterList.get(i).canNotSave());
//		}
//Clustering is done at this point and the clusters/centroids are finalized
		setHospitals();
		for(int i = 0; i < patientList.size(); i++) {
//			System.out.println(patientList.get(i).toString());
		}
//		System.out.println(patientList.size());
//		System.out.println(hospitalList.size());
		for(int i = 0; i < clusterList.size(); i++) {
//			System.out.println(clusterList.get(i).toString());
		}
		System.out.print("Hospitals ");
		for(int i = 0; i < hospitalList.size(); i++) {
			System.out.print("" + i + " " + hospitalList.get(i).toString());
		}
		System.out.println();
		for(int i = 0; i < patientList.size(); i++) {
			notSavedList.add(patientList.get(i));
		}
		for(int i = 0; i < centroids.size(); i++) {
			savedList.add(centroids.get(i));
			notSavedList.remove(centroids.get(i));
		}
		routeVehicles(4);
		ambulanceList.clear();
		routeVehicles(3);
		return placeHolder; //change this to some metric # of people saved based on greed
	}

	public static void genCity() {
		int xMin = 9999;
		int xMax = -9999;
		int yMin = 9999;
		int yMax = -9999;
		int tMin = 9999;
		int tMax = -9999;
		ArrayList<Integer> a = new ArrayList<Integer>(1);
		while(in.hasNextInt()){
			int temp = in.nextInt();
			a.add(temp);
		}
		int patientNumber = 0;
		for(int i = 0; i < a.size() - 5; i++) {
			Patient temp = new Patient(a.get(i), a.get(i+1), a.get(i+2), patientNumber);
			if(a.get(i) < xMin) {
				xMin = a.get(i);
			}
			if(a.get(i) > xMax) {
				xMax = a.get(i);
			}
			if(a.get(i+1) < yMin) {
				yMin = a.get(i+1);
			}
			if(a.get(i+1) > yMax) {
				yMax = a.get(i+1);
			}
			if(a.get(i+2) < tMin) {
				tMin = a.get(i+2);
			}
			if(a.get(i+2) > tMax) {
				tMax = a.get(i+2);
			}
			patientList.add(temp);
			i++;
			i++;
			patientNumber++;
		}
		testString = "x: (" + xMin + "," + xMax + ") y:(" + yMin + "," + yMax + ") t:(" + tMin + "," + tMax + ")";
		for(int i = 0; i < 5; i++) {
			Hospital tmp = new Hospital();
			tmp.setAmb(a.get(a.size() - (5 - i)));
			hospitalList.add(tmp);
		}
	}

	public static void initClustering() {
		centroids.clear();
		for(int i = 0; i < 5; i++) {
			int j = (int)(300*Math.random());
			centroids.add(patientList.get(j));
		}
//		System.out.println("Initial centroids:");
		for(int i = 0; i < centroids.size(); i++) {
//			System.out.println(centroids.get(i).toString());
		}
		ArrayList<Patient> centroidsTest = new ArrayList<Patient>(1);
		while(!centroidsTest.equals(centroids)) {
			centroidsTest.clear();
			for(int i = 0; i < centroids.size(); i++) {
				centroidsTest.add(centroids.get(i));
			}
			clustering();
//			System.out.println("New centroids:");
//			for(int i = 0; i < centroids.size(); i++) {
//				System.out.println(centroids.get(i).toString());
//			}
		}
	}

	public static void clustering() {
		clusterList.clear();
		clusterList.trimToSize();
		for(int i = 0; i < 5; i++) {
			Cluster temp = new Cluster(centroids.get(i));
			clusterList.add(temp);
		}
		for(int i = 0; i < patientList.size(); i++) {
			int dist = 100*100;
			int min = 5;
			for(int j = 0; j < centroids.size(); j++) {
				int tmpDist = getDistance(patientList.get(i), centroids.get(j));
				if(tmpDist < dist) {
					min = j;
					dist = tmpDist;
				}
			}
			if(!clusterList.get(min).contains(patientList.get(i))) {
				clusterList.get(min).add(patientList.get(i));
			}
		}
		for(int i = 0; i < clusterList.size(); i++) {
			int tempTotalDist = getTotalDistance(clusterList.get(i).get(0), clusterList.get(i));
			Patient placeholder = clusterList.get(i).get(0);
			for(int j = 1; j < clusterList.get(i).size(); j++) {
				if(clusterList.get(i).get(0) != centroids.get(i)) {
					System.out.println("Something is wrong ********************************");
				}
				int temp = getTotalDistance(clusterList.get(i).get(j), clusterList.get(i));
				if (temp < tempTotalDist) {
					tempTotalDist = temp;
					placeholder = clusterList.get(i).get(j);
				}
			}
			centroids.set(i, placeholder);
		}
	}

	public static void setHospitals() {
		ArrayList<Cluster> clusterTest = new ArrayList<Cluster>(1);
		for(int i = 0; i < clusterList.size(); i++) {
			clusterTest.add(clusterList.get(i));
		}
		while(!clusterTest.isEmpty()) {
			int x = findLargestCluster(clusterTest);
			int y = findMostAmbulances();
			hospitalList.get(y).setXY(clusterTest.get(x).get(0).getX(), clusterTest.get(x).get(0).getY());
//			System.out.println("X is : " + x + "|Y is : " + y);
//			System.out.println(clusterList.size());
//			System.out.println("Setting x: " + clusterTest.get(x).get(0).getX() + " Setting y: " + clusterTest.get(x).get(0).getY());
			for(int i = 0; i < clusterList.size(); i++) {
				if(clusterTest.get(x).get(0).getX() == clusterList.get(i).get(0).getX() && clusterTest.get(x).get(0).getY() == clusterList.get(i).get(0).getY()) {
					hospitalList.get(y).cluster(i);
					break;
				}
			}
			for(int i = 0; i < hospitalList.size(); i++) {
//				System.out.println(hospitalList.get(i).toString());
			}
			clusterTest.remove(x);
		}
	}

	public static void routeVehicles(int passengers) {
		int pointer = 0;
		int ambulancePointer = 0;
		int minTimeToLive = 99999;
		int tempT = 0;
		for(int i = 0; i < hospitalList.size(); i++) {
			int x = hospitalList.get(i).getX();
			int y = hospitalList.get(i).getY();
			int amb = hospitalList.get(i).getAmb();
			for(int j = 0; j < amb; j++) {
				if(ambulanceList.size() > ambulancePointer + 1) {
					tempT = ambulanceList.get(ambulancePointer).getTime();
				}
				Ambulance tempAmbulance = new Ambulance(tempT, x, y, ambulancePointer);
				ambulanceList.add(tempAmbulance);
/**				if(passengers == 4) {
					Patient tmp1 = findFurthestClusterPatient(hospitalList.get(i).getCluster(), tempAmbulance.getX(), tempAmbulance.getY());
					tempAmbulance.addPath(tmp1, getDistance(tmp1, tempAmbulance.getX(), tempAmbulance.getY()));
					savedList.add(tmp1);
					notSavedList.remove(tmp1);
				}**/
				while(tempAmbulance.pathSize() < passengers) {
					Patient tmp = findNearestPatient(tempAmbulance.getX(), tempAmbulance.getY());
					tempAmbulance.addPath(tmp, getDistance(tmp, tempAmbulance.getX(), tempAmbulance.getY()));
					savedList.add(tmp);
					notSavedList.remove(tmp);
				}
				tempAmbulance.move(x, y, getDistance(x, y, tempAmbulance.getX(), tempAmbulance.getY()));
				for(int k = 0; k < tempAmbulance.pathSize(); k++) {
					int tempTime = tempAmbulance.getTime();
					if(tempTime < tempAmbulance.get(k).getTime() + 1) {
						savedCount++;
					}
					else {
						deadCount++;
					}
				}
				ambulancePointer++;
			}
		}
		for(int i = 0; i < ambulanceList.size(); i++) {
			System.out.println(ambulanceList.get(i).toString());
			ambulanceList.get(i).unload();
		}
	}

	public static int findLargestCluster(ArrayList<Cluster> c) {
		int temp = 0;
		for(int i = 0; i < c.size(); i++) {
			if(c.get(i).size() > c.get(temp).size()) {
				temp = i;
			}
		}
		return temp;
	}

	public static int findMostAmbulances() {
		Hospital tmp = new Hospital();
		tmp.setAmb(-1);
		int temp = tmp.getAmb();
		for(int i = 0; i < hospitalList.size(); i++) {
			if(hospitalList.get(i).getAmb() > temp && hospitalList.get(i).getX() < 0 && hospitalList.get(i).getY() < 0) {
				temp = i;
			}
		}
		return temp;
	}

	public static int getDistance(Hospital h, Patient p) {
		int tempX = Math.abs(h.getX() - p.getX());
		int tempY = Math.abs(h.getY() - p.getY());
		return tempX + tempY;
	}

	public static int getDistance(Patient h, Patient p) {
		int tempX = Math.abs(h.getX() - p.getX());
		int tempY = Math.abs(h.getY() - p.getY());
		return tempX + tempY;
	}

	public static int getDistance(Patient p, int x, int y) {
		int tempX = Math.abs(x - p.getX());
		int tempY = Math.abs(y - p.getY());
		return tempX + tempY;
	}

	public static int getDistance(int x1, int y1, int x2, int y2) {
		int tempX = Math.abs(x1 - x2);
		int tempY = Math.abs(y1 - y2);
		return tempX + tempY;
	}

	public static int getTotalDistance(Patient p, Cluster c) {
		int temp = 0;
		for(int i = 0; i < c.size(); i++) {
			temp += getDistance(p, c.get(i));
		}
		return temp;
	}

	public static Patient findNearestPatient(Patient p) {
		int dist = 100*100;
		Patient temp = p;
		for(int i = 0; i < notSavedList.size(); i++) {
			if(getDistance(p, notSavedList.get(i)) < dist && notSavedList.get(i).getPatientID() != p.getPatientID()){
				temp = notSavedList.get(i);
				dist = getDistance(p, notSavedList.get(i));
			}
		}
		return temp;
	}

	public static Patient findNearestPatient(int x, int y) {
		int dist = 100*100;
		Patient temp = new Patient(0,0,0,0);
		for(int i = 0; i < notSavedList.size(); i++) {
			if(getDistance(notSavedList.get(i), x, y) < dist && !(notSavedList.get(i).getX() == x && notSavedList.get(i).getY() == y)) {
				temp = notSavedList.get(i);
				dist = getDistance(notSavedList.get(i), x, y);
			}
		}
		return temp;
	}

	public static Patient findFurthestClusterPatient(int clusterNumber, int x , int y) {
		int num = clusterNumber;
		int dist = 0;
		Patient temp = new Patient(0, 0, 0, 0);
		for(int i = 0; i < clusterList.get(num).size(); i++) {
			if(getDistance(clusterList.get(num).get(i), x, y) > dist && notSavedList.contains(clusterList.get(num).get(i))) {
				temp = clusterList.get(num).get(i);
				dist = getDistance(clusterList.get(num).get(i), x, y);
			}
		}
		return temp;
	}
}

class Cluster {
	ArrayList<Patient> cluster = new ArrayList<Patient>(1);
	ArrayList<Patient> deadPatients = new ArrayList<Patient>(1);
	Patient centroid;

	public Cluster(Patient c) {
		centroid = c;
		cluster.add(centroid);
	}

	public void add(Patient p) {
		cluster.add(p);
	}

	public Patient get(int x) {
		return cluster.get(x);
	}

	public void setCentroid(Patient p) {
		if(cluster.contains(p)) {
			centroid = p;
		}
		else {
			centroid = p;
			cluster.add(p);
		}
	}

	public void clear() {
		cluster.clear();
		cluster.trimToSize();
	}

	public boolean contains(Patient p) {
		return cluster.contains(p);
	}

	public int size() {
		return cluster.size();
	}

	public String toString() {
		String str = "{";
		for(int i = 0; i < cluster.size(); i++) {
			str += cluster.get(i).toString() + ",	";
		}
		str += "end}";
		return str;
	}

	public int canNotSave() {
		deadPatients.clear();
		for(int i = 0; i < cluster.size(); i++) {
			if(2*(getDistance(cluster.get(i))) + 2 >= cluster.get(i).getTime()) {
				deadPatients.add(cluster.get(i));
			}
		}
		return deadPatients.size();
	}

	public int getDistance(Patient p) {
		int tempX = Math.abs(centroid.getX() - p.getX());
		int tempY = Math.abs(centroid.getY() - p.getY());
		return tempX + tempY;
	}
}

class Hospital {
	private int ambulances;
	private int xloc;
	private int yloc;
	private int cluster;

	public Hospital() {
		ambulances = 0;
		xloc = -1;
		yloc = -1;
	}

	public void cluster(int x) {
		cluster = x;
	}

	public int getCluster() {
		return cluster;
	}

	public void setX(int x) {
		xloc = x;
	}

	public int getX() {
		return xloc;
	}

	public void setY(int y) {
		yloc = y;
	}

	public int getY() {
		return yloc;
	}

	public void setXY(int x, int y) {
		xloc = x;
		yloc = y;
	}

	public void setAmb(int x) {
		ambulances = x;
	}

	public int getAmb() {
		return ambulances;
	}

	public String toString(){
//		return "(" + xloc + "," + yloc + "): " + ambulances + "(" + cluster + ")";
		return "(" + xloc + "," + yloc + ")" + " ";
	}
}

class Ambulance {
	private int time;
	private int xloc;
	private int yloc;
	private int id;
	private ArrayList<Patient> path = new ArrayList<Patient>(1);

	public Ambulance(int t, int x, int y, int i) {
		time = t;
		xloc = x;
		yloc = y;
		id = i;
	}

	public void clearPath() {
		path.clear();
		path.trimToSize();
	}

	public Patient get(int i) {
		return path.get(i);
	}

	public void addPath(Patient p, int dist) {
		time += dist;
		path.add(p);
		xloc = p.getX();
		yloc = p.getY();
		time++;
	}

	public int pathSize() {
		return path.size();
	}

	public void move(int x, int y, int dist) {
		time += dist;
		xloc = x;
		yloc = y;
	}

	public void unload() {
		System.out.println("Ambulance " + id + " (" + xloc + "," + yloc + ")");
		time++;
	}

	public void load() {
		time++;
	}

	public void setX(int x) {
		xloc = x;
	}

	public int getX() {
		return xloc;
	}

	public void setY(int y) {
		yloc = y;
	}

	public int getY() {
		return yloc;
	}

	public int getTime() {
		return time;
	}

	public void setXY(int x, int y) {
		xloc = x;
		yloc = y;
	}

	public void setid(int i) {
		id = i;
	}

	public int getAmbulanceID() {
		return id;
	}

/**	public String toString() {
		String s = "";
		for(int i = 0; i < path.size(); i++) {
			s += path.get(i).getPatientID() + ",";
		}
		return s;
	}**/

	public String toString() {
		String s = "Ambulance " + id;
		for(int i = 0; i < path.size(); i++) {
			s += " " + path.get(i).getPatientID() + " " + path.get(i).toString();
		}
		return s;
	}
}

class Patient {
	private int xloc;
	private int yloc;
	private int rescueTime;
	private int id;

	public Patient(int x, int y, int t, int i) {
		xloc = x;
		yloc = y;
		rescueTime = t;
		id = i;
	}

	public int getPatientID() {
		return id;
	}

	public int getX() {
		return xloc;
	}

	public int getY() {
		return yloc;
	}

	public void setTime(int x) {
		rescueTime = x;
	}

	public int getTime() {
		return rescueTime;
	}

	public void setXY(int x, int y) {
		xloc = x;
		yloc = y;
	}

	public String toString() {
		return "(" + xloc + "," + yloc + "," + rescueTime + ")";
	}
}