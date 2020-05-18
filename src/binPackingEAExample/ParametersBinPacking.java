package binPackingEAExample;

public class ParametersBinPacking {

	//all below parameters are editable
	static int maxIterations = 10000;
	static int populationSize = 100;
	
	//arbitrary amounts to test robustness of algorithm
	//bear total bin capacity (numOfBins * binCapacity) available in mind - note test file comments below
	static int numOfBins = 150;
	static int binCapacity = 137;
	static double mutationRate = 0.05; 
	static int tournamentSize = 10;
	static int crossoverMethod = 1;
	
	static String inputFile = "problems/test2.epp"; //total weight of all items = 29778, highest member = 110
	//static String inputFile = "problems/test3.epp"; //total weight of all items = 56970, highest member = 210
	//static String inputFile = "problems/test4.epp"; //total weight of all items = 136849, highest member = 510
	static String outputFile = "results.txt";
	
	static long seed = -1; //set random number to default value to utilise clock millisecond timer if desired

	
}
