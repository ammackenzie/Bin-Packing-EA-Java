package binPackingEAExample;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Main {

	public static void main(String[] args) {
		SolveProblemBinPacking myEA = new SolveProblemBinPacking();
		long startTime = System.nanoTime();
		myEA.runEA();
		
		long endTime = System.nanoTime();
		long durationInMs = TimeUnit.NANOSECONDS.toMillis(endTime-startTime);
		System.out.println("Time taken: "+ durationInMs + " ms"); 
		
		//code for populating test file
		/*Random random = new Random();
		
		int[] data = new int[500];
		for(int x = 0; x < data.length; x++) {
			data[x] = random.nextInt(500)+10;
		}
		
		try {
			BufferedWriter logFile = new BufferedWriter(new FileWriter("problems/test4.epp",
				true));
			for(int x : data) {
				logFile.write("" + x + "\n");
			}
			logFile.close();
		} catch (IOException e) {
			System.out.println("log file error");
		}*/
	}
}
