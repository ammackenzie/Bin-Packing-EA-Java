package binPackingEAExample;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.Random;

public class SolveProblemBinPacking {
	
	//int n;
	int numOfBins;
	int binCapacity;
	int population[][];
        int binsCount[];	
	int maxIterations;
	String inputFile;
	String outputFile;
	
	int testItemCount;
	int[] itemWeights;
	int populationSize;
	double mutationRate;
	int tournamentSize;
	int crossoverMethod;
	int selectionMethod;

	
	int[] fitnessTarget;
	
	
	Random randomNum;
	
	public SolveProblemBinPacking() {
		//set parameters from parameters.java
		this.maxIterations = ParametersBinPacking.maxIterations;
		this.populationSize = ParametersBinPacking.populationSize;
		this.mutationRate = ParametersBinPacking.mutationRate;
		this.tournamentSize = ParametersBinPacking.tournamentSize;
		this.crossoverMethod = ParametersBinPacking.crossoverMethod;
		this.binCapacity = ParametersBinPacking.binCapacity;
		this.numOfBins = ParametersBinPacking.numOfBins;
		this.inputFile = ParametersBinPacking.inputFile;
		this.outputFile = ParametersBinPacking.outputFile;
		this.binsCount = new int[ParametersBinPacking.numOfBins];
		
		//this.randomNum = new Random(Parameters.seed); //will use clock millisecond as seed
		this.randomNum = new Random(); 
		
		loadProblem(this.inputFile);
		
	}
	
	public void runEA() {
		initalise();

		int bestFitness = 0;
		int bestID = 0;
		int counter = 0;
		
		//run program until set max iterations
		for(int x = 1; x < this.maxIterations; x++) {
			int firstParentIndex, secondParentIndex;
			//tournament selection
			firstParentIndex = tournamentSelect();
			secondParentIndex = tournamentSelect();
			
			int[] child;

			switch(this.crossoverMethod) { //determined by choice in Parameters.java
			case 0:
				child = uniformCrossover(firstParentIndex, secondParentIndex);
				break;
			case 1:
				child = onepointCrossover(firstParentIndex, secondParentIndex);
				break;
			case 2:
				child = twopointCrossover(firstParentIndex, secondParentIndex);
				break;
			default:
				child = uniformCrossover(firstParentIndex, secondParentIndex);
				break;
			}
			
			child = mutate(child); //mutate new child
			
			replaceWorstParent(child, evaluate(child), firstParentIndex, secondParentIndex);
			
			bestID = findBest();
			bestFitness = evaluate(this.population[bestID]);
			
			System.out.println("Iteration: " + (x+1) +  " best fitness: " + bestFitness + ", best member: " + memberToString(this.population[bestID]));
			if(bestFitness == 0) {
				counter = x + 1; //note what iteration we are on
				break; //break if optimal solution has been found
			}
		}
		System.out.println(outputResults(bestID, bestFitness, counter));//save result to file);
	}
	
	private void initalise() {
		int[] tempBins = new int[this.numOfBins];
		for(int i=0; i< this.numOfBins; i++) {
			tempBins[i] = 0; //make new temp array representing space in all bins
		}
		
		int[] alreadySelectedIndexes = new int[this.testItemCount];
		
		for(int x = 0; x < this.testItemCount; x++) {
			alreadySelectedIndexes[x] = 0;
		}
		//current issue is population prioritises utilising first items in population
		this.population = new int[this.populationSize][this.testItemCount];
		for(int i = 0; i < this.populationSize; i++) { 
			for(int j = 0; j < this.testItemCount; j++) {
				//first check if any bins have space for current item
				int randomIndex = this.randomNum.nextInt(this.testItemCount);
				while(alreadySelectedIndexes[randomIndex] != 0) {
					randomIndex = this.randomNum.nextInt(this.testItemCount);
				}
				int currentItemWeight = this.itemWeights[randomIndex];
				boolean spaceAvailable = false;
				for(int x : tempBins) {
					if(this.binCapacity - x < currentItemWeight) {
						//do nothing
					} else {
						spaceAvailable = true;
					}
				}
				//if no space available, make gene 0 for not used
				if(spaceAvailable) {
					//at least one bin can fit the current item
					int binChoice = this.randomNum.nextInt(this.numOfBins);
					while(this.binCapacity - tempBins[binChoice] < currentItemWeight) {
						//keep looping until a bin with space is chosen
						binChoice = this.randomNum.nextInt(this.numOfBins); 
					}
					this.population[i][randomIndex] = binChoice + 1; //plus one to represent what number of bin is chosen
					tempBins[binChoice] += currentItemWeight;
				} else {
					this.population[i][randomIndex] = 0; //no bins can fit the current item
				}
				
				alreadySelectedIndexes[randomIndex] = 1; //note that this index has been targeted already
			}
			//reset bins after each population member has been initialised
			for(int x = 0; x < this.numOfBins; x++) {
				tempBins[x] = 0;
			}
			for(int x = 0; x < this.testItemCount; x++) {
				alreadySelectedIndexes[x] = 0;
			}
		}
	}
	
	private int[] mutate(int[] child) {
		populateCurrentBins(child); //fill a class-wide bin array with weights of items chosen in child
		
		for(int i = 0; i < child.length; i++) {
			if(this.randomNum.nextDouble() < this.mutationRate) {
				//below code ensures that mutation does not result in an invalid child
				int binChoice = this.randomNum.nextInt(this.numOfBins);
				if(this.binCapacity - this.binsCount[binChoice] > this.itemWeights[i]) {
					int lowest = this.binCapacity;
					for(int x : this.binsCount) {
						if(x < lowest){
							lowest = x;
						}
					}
					if(this.binCapacity - lowest < this.itemWeights[i]) {
						//all bins too full for item
						child[i] = 0;
					} else {
						//should only trigger when once bin still has space
						while(this.binCapacity - this.binsCount[binChoice] < this.itemWeights[i]) {
							binChoice = this.randomNum.nextInt(this.numOfBins);
						}
					}
				} else {
					child[i] = binChoice + 1;
				}
			}
		}
		resetBins();//empty class-wide bin array for next use
		return child;
	}
	
	private void populateCurrentBins(int[] popMember) {
		for(int i = 0; i < this.numOfBins; i++) {
			if(popMember[i] == 0) {
				//do nothing
			}else {
				this.binsCount[popMember[i] -1] += this.itemWeights[i];
			}
			
		}
	}
	
	private void resetBins() {
		for(int i = 0; i < this.numOfBins; i++) {
			this.binsCount[i] = 0;
		}
	}
	
	private int tournamentSelect() {
		//select n random members of population - determined by tournamentSize
		int tempID = this.randomNum.nextInt(this.populationSize);
		int winnerID = tempID;
		int tempFitness = evaluate(this.population[tempID]);
		int bestFitness = tempFitness;
		
		for(int x = 0; x < this.tournamentSize - 1; x++) {
			tempID = this.randomNum.nextInt(this.populationSize); //
			tempFitness = evaluate(this.population[tempID]);
			if(tempFitness > bestFitness) {
				bestFitness = tempFitness;
				winnerID = tempID;
			}
		}
		return winnerID;
	}
	
	//not effective for current configuration
	/*private int proportionateSelect() {
		//selects parent based on random chance with higher fitness members more likely to succeed
		int totalFitness = 0, partialSum = 0, parentID = 0, chosenParent = 0;
		double cutOff = this.randomNum.nextDouble() * totalFitness;
		
		for(int i = 0; i < this.populationSize; i++) {
			totalFitness =+ evaluate(this.population[i]); 
		}
		
		while(cutOff > partialSum) {
			partialSum += evaluate(this.population[parentID]);
			parentID++;
		}
		
		chosenParent = parentID;
		
		return chosenParent;
	}*/ 
	
	private int[] onepointCrossover(int aFirstParentIndex, int aSecondParentIndex) {
		int[] child = new int[population[aFirstParentIndex].length];
		//pick one random index as a crosspoint
		int crossPoint = this.randomNum.nextInt(this.testItemCount);
		System.out.println("crosspoint: " + crossPoint);
		//copy parentOne into child up until crosspoint
		for(int i = 0; i < crossPoint; i++) {
			child[i] = population[aFirstParentIndex][i];
		}
		
		//copy parentTwo into child from crosspoint until end
		for(int i = crossPoint; i < this.testItemCount; i++) {
			child[i] = population[aSecondParentIndex][i];
		}
		return child;
	}
	
	
	private int[] twopointCrossover(int aFirstParentIndex, int aSecondParentIndex) {
		int[] child = new int[population[aFirstParentIndex].length];
		
		//choose two random cross over points
		int crossPoint1 = this.randomNum.nextInt(this.testItemCount);
		int crossPoint2 = this.randomNum.nextInt(this.testItemCount);
		
		if(crossPoint1 > crossPoint2) {
			int temp = crossPoint1;
			crossPoint1 = crossPoint2;
			crossPoint2 = temp;
		}
		
		//copy parentOne up to p1
		for(int i = 0; i < crossPoint1; i++) {
			child[i] = population[aFirstParentIndex][i];
		}
		
		//copy parentTwo from p1-p2
		for(int i = crossPoint1; i < crossPoint2; i++) {
			child[i] = population[aSecondParentIndex][i];
		}
		
		//copy parentOne from p2-end
		for(int i = crossPoint2; i < this.testItemCount; i++) {
			child[i] = population[aFirstParentIndex][i];
		}
		return child;
	}
	
	private int[] uniformCrossover(int aFirstParentIndex, int aSecondParentIndex) {
		int[] child = new int[population[aFirstParentIndex].length];
		for (int index = 0; index < population[aFirstParentIndex].length; index++) {
			if (randomNum.nextBoolean())
				child[index] = population[aFirstParentIndex][index];
			else
				child[index] = population[aSecondParentIndex][index];
		}
		return child;
	}
	
	private int evaluate(int[] populationMember) {
		int result = 0;
		int[] binCounts = new int[this.numOfBins];
		//measure total unutilised space in bins
		for(int i = 0; i < this.testItemCount; i++) {
			if(populationMember[i] != 0) {
				binCounts[populationMember[i] -1] += this.itemWeights[i];
			}
			
		}
		for(int x : binCounts) {
			result += this.binCapacity - x;
		}
		
		if(result < 0) {
			result *= -1;
		}
		
		return result;
	}
	
	
	private void replaceWorstParent(int[] child, int childFitness, int parentOneID, int parentTwoID) {
		int worstFitness = evaluate(this.population[parentTwoID]);
		int worstID = parentTwoID;
		if(evaluate(this.population[parentOneID]) > worstFitness) {
			worstFitness = evaluate(this.population[parentOneID]);
			worstID = parentOneID;
		}
		
		//replace worst parent with child if child is fitter
		if(evaluate(child) < worstFitness) {
			for(int i = 0; i < this.testItemCount; i++) {
				this.population[worstID][i] = child[i];
			}
		}
			
	}
	
	public int findBest() {
		//start with assumption best is first element in pop
		int bestFitness = evaluate(this.population[0]);
		int bestID = 0;
		
		for(int i = 0; i < this.populationSize; i++) {
			int tempFitness = evaluate(this.population[i]);
			if(tempFitness < bestFitness){
				bestFitness = tempFitness;
				bestID = i;
			}
		}
		return bestID;
	}
	
	public String memberToString(int[] popMember) {
		String output = "";
		
		for(int x = 0; x < this.testItemCount; x++) {
			output += popMember[x];
			if(x < this.testItemCount-1) {
				output +=",";
			}
		}
		return output;
	}
	
	private int findNumberOfItemsUsed(int[] popMember) {
		int count = 0;
		for(int x : popMember) {
			if( x != 0) {
				count++;
			}
		}
		
		return count;
	}
	
	private void loadProblem(String aFileName) {
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(
				new FileInputStream(aFileName)));
			StreamTokenizer streamTokenizer = new StreamTokenizer(input);
			streamTokenizer.parseNumbers();
			streamTokenizer.nextToken();
			//read first int value in file - this is set to number of total items following
			this.testItemCount = (int) streamTokenizer.nval;  
			this.itemWeights = new int[this.testItemCount];
			for (int tokens = 0; tokens < this.testItemCount; tokens++) {
				streamTokenizer.nextToken();
				this.itemWeights[tokens] = (int) streamTokenizer.nval;
			}
			
			System.out.println("testItemCount = " + this.testItemCount);
		} catch (Exception e) {
			System.out.println("Error reading problem file " + aFileName);
		}
	}
	
	public String outputResults(int bestID, int bestFitness, int counter) {
		String output = "";
		try {
			BufferedWriter logFile = new BufferedWriter(new FileWriter(this.outputFile,
				true));
			logFile.write("Input File: " + this.inputFile + ", num of bins: " + this.numOfBins + ", bin capacity: " + this.binCapacity + "\n");
			output += "Input File: " + this.inputFile + ", num of bins: " + this.numOfBins + ", bin capacity: " + this.binCapacity + "\n";
			if(bestFitness == 0) {
				logFile.write("SUCESSFUL run, total iterations: " + counter + "\n");
				output += "SUCESSFUL run, total iterations: " + counter + "\n";
			}else {
				logFile.write("FAILED to find optimal solution");
				output += "FAILED to find optimal solution";
			}
			
			//determine which crossover operator was used
			String crossOver = "";
			if(this.crossoverMethod == 0) {
				crossOver += "Uniform";
			}else if(this.crossoverMethod == 1) {
				crossOver += "One-Point";
			} else if(this.crossoverMethod ==2) {
				crossOver += "Two-Point";
			} else {
				crossOver += "Default";
			}
			
			logFile.write("Best Individual Fitness: " + bestFitness + " Total Items utilised: " + findNumberOfItemsUsed(this.population[bestID]) + " with values: " + memberToString(this.population[bestID]) + "\n");
			output += "Best Individual Fitness: " + bestFitness + " Total Items utilised: " + findNumberOfItemsUsed(this.population[bestID]) + " with values: " + memberToString(this.population[bestID]) + "\n";
			
			logFile.write("Crossover Method: " + crossOver + "\nPopulation size: " + this.populationSize + "\nTournament Size: " + this.tournamentSize + "\nMutation Rate: " + this.mutationRate + "\n_______________________________________________________________________________________\n");
			output += "Crossover Method: " + crossOver + "\nPopulation size: " + this.populationSize + "\nTournament Size: " + this.tournamentSize + "\nMutation Rate: " + this.mutationRate;
			logFile.close();
		} catch (IOException e) {
			System.out.println("log file error");
		}
		return output;
	}
}
