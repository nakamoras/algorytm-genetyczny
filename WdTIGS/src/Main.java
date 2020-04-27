import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class Main {

public static Random random = new Random();
public static int citiesInFile = 0;
public static int[][] data;
public static String selectionMethod = "";
public static int tournamentSize = 2;
public static double mutationChance = 0.3d;
public static int numberOfCrossovers = 1000;
public static int generations = 1000;
public static int populationSize = 1000;
public static String fileName = "kroC100.txt";

public static void main(String[] args) throws FileNotFoundException {
	
	if(args.length>0) {
		try {
			selectionMethod = args[0];
			tournamentSize = Integer.parseInt(args[1]);
			mutationChance = Double.parseDouble(args[2]);
			numberOfCrossovers = Integer.parseInt(args[3]);
			generations = Integer.parseInt(args[4]);
			populationSize = Integer.parseInt(args[5]);
			fileName = args[6];
		}catch(NumberFormatException e) {
			System.err.println("Argument must be an integer");
			System.exit(1);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	

long startTimeMilliSecond = System.currentTimeMillis();


int[] populationScore = new int[populationSize];
int[] populationFitness = new int[populationSize];
int[] selectedPopulation = new int[populationSize];

data = loadCitiesFromFile(fileName);
//printCities(data,"Tablica z miastami");
int[][] generatedPopulation = generatePopulation(populationSize);
//printCities(generatedPopulation, "Wygenerowana populacja");


for(int i=0; i<generations;i++) {

//System.out.println("GENERATION:" + (i+1));
//System.out.println("-----------------------------------------------------------------------------------------");


populationScore = populationScore(generatedPopulation, data);
//printCities(populationScore,"Population score");
populationFitness = populationFitness(populationScore);
if("r".equals(selectionMethod)) {
	selectedPopulation = rouletteMethodSelection(populationFitness);
}else {
	selectedPopulation = tournamentMethodSelection(populationFitness, tournamentSize);
}
//printCities(selectedPopulation, "Population selected by tournament method");
//generatedPopulation = removeDuplicatesFromPopulation(generatedPopulation, populationFitness, selectedPopulation);
//printCities(generatedPopulation, "Population without duplicates");
generatedPopulation = getPopulationFromSelectedPopulation(generatedPopulation, populationFitness, selectedPopulation);
//printCities(generatedPopulation, "Population selected");
generatedPopulation = crossoverPMX(generatedPopulation,numberOfCrossovers);
//printPopulationAndScore(generatedPopulation,"Population after crossover");
generatedPopulation = mutate(generatedPopulation, mutationChance);
//printPopulationAndScore(generatedPopulation,"Population after mutation");
generatedPopulation = sortPopulationAsc(generatedPopulation);
//printPopulationAndScore(generatedPopulation, "Population after sort");


//System.out.println("-----------------------------------------------------------------------------------------");
//System.out.println("GENERATION:" + (i+1) + " FINISHED");
}
System.out.print("Best Score:" + populationScore[1]+"\t");
for(int i=0;i<generatedPopulation[0].length;i++) {
	System.out.print(generatedPopulation[0][i] + "-");
}
System.out.println();
long endTimeMilliSecond = System.currentTimeMillis();
System.out.println("Time Taken in " + (endTimeMilliSecond - startTimeMilliSecond) + "ms");

}

public static int[][] loadCitiesFromFile(String filePath) throws FileNotFoundException{

File file = new File(filePath);
Scanner in = new Scanner(file);
if (in.hasNext()) {
citiesInFile = in.nextInt();
}
System.out.printf("Cities loaded: %d \n", citiesInFile);
int index1 = 0;
int index2 = 0;
String line = null;
int number;
int[][] data = new int[citiesInFile][citiesInFile];
int iterator = 0;

while(in.hasNextLine()) {
	line = in.nextLine();
	if(!line.isEmpty()) {
		String[] values = line.split(" ");
		for(int i=0; i< values.length;i++) {
			data[iterator][i] = Integer.parseInt(values[i]);
		}
		
		iterator++;
	}
	
	
}

in.close();


int[][] dataMirrored = new int[citiesInFile][citiesInFile];
for (int i = 0; i < data.length; i++) {
for (int j = 0; j < data[i].length; j++) {
dataMirrored[i][j] = data[i][j];
dataMirrored[j][i] = dataMirrored[i][j];
}
}

return dataMirrored;
}


/**
 * Zwraca wylosowana populacje i usuwa duplikaty
 *
 * @param generatedPopulation - Wygenerowana populacja
 * @param populationFitness - Wyniki populacji
 * @param selectedPopulation - Wylosowana populacja
 * @return
 */
public static int[][] removeDuplicatesFromPopulation(int[][] generatedPopulation, int[] populationFitness, int[] selectedPopulation){
   
Map<Integer, int[]> map = new HashMap<Integer , int[]>();

    for(int i=0;i<selectedPopulation.length;i++){
       for(int j=0;j<populationFitness.length;j++) {
      if(selectedPopulation[i] == populationFitness[j]) {
      map.put(selectedPopulation[i], generatedPopulation[j]);
      break;
      }
       }
    }
    int[][] population  = new int[map.size()][generatedPopulation.length];
    int iterator = 0;
    for(Map.Entry<Integer, int[]> mapEntry : map.entrySet()) {
    population[iterator] = mapEntry.getValue();
    iterator++;
    }
   
    return population;
}

public static int[][] getPopulationFromSelectedPopulation(int[][] generatedPopulation,int[] populationFitness,int[] selectedPopulation){
	
	int[][] newPopulation = new int[populationSize][generatedPopulation[0].length];
	
	for(int i=0;i<selectedPopulation.length;i++){
		
		for(int j=0;j<populationFitness.length;j++) {
			if(selectedPopulation[i] == populationFitness[j]) {
				newPopulation[i] = generatedPopulation[j];
				break;
			}
		}
	
	}
	
	return newPopulation;
	
}

/**
* Generuje populacje
*
* @param populationSize - wielkosc populacji
* @return wylosowana populacja
*/
public static int[][] generatePopulation(int populationSize){
System.out.println("Randomizing population");
List<Integer> numberRange = new ArrayList<Integer>();
for(int i=0;i<citiesInFile;i++){
           numberRange.add(i);
}

int[][] generatedPopulation = new int[populationSize][citiesInFile];

for(int i=0;i<generatedPopulation.length;i++){
           Collections.shuffle(numberRange);
           for(int j=0;j<generatedPopulation[i].length;j++){
               generatedPopulation[i][j] = numberRange.get(j);
           }    
}



return generatedPopulation;
       
}

/**
 * Oblicza wynik populacji
 *
 * @param population - Populacja
 * @return Tablice z wynikami populacji
 */
public static int[] populationScore(int[][] population, int[][] data) {
int sum=0;
int cityA = 0;
int cityB = 0;
int[] populationScore = new int[population.length];
for(int i=0;i<population.length;i++) {
for(int j=1;j<population[i].length;j++) {

cityA = population[i][j-1];
cityB = population[i][j];
sum += data[cityA][cityB];
}
cityA = population[i][0];
cityB = population[i][population[i].length - 1];
sum += data[cityA][cityB];
populationScore[i] = sum;
sum=0;
}


return populationScore;
}


public static int[] populationFitness(int[] populationScore) {

int max=0;
int[] populationFitness = new int[populationScore.length];

for(int i=0;i<populationScore.length;i++){
   max = Math.max(max, populationScore[i]);
}

for(int i=0;i<populationScore.length;i++){
   populationFitness[i] = max - populationScore[i] + 1;
}

return populationFitness;

}

/**
 * Losuje populacje metoda ruletki
 *
 * @param populationFitness - Tablica z ocena populacji
 * @return Wylosowana populacja
 */
public static int[] rouletteMethodSelection(int[] populationFitness){

int scoreSum = 0;
for(int i=0;i<populationFitness.length;i++) {
scoreSum += populationFitness[i];
}

double[] tab = new double[populationFitness.length];
double[] randomValues = new double[populationFitness.length];
int[] selectedPopulation = new int[populationFitness.length];

for(int i=0;i<tab.length;i++) {
randomValues[i] = random.nextDouble();
if(i>=1) {
tab[i] = ((double)populationFitness[i]/(double)scoreSum) + tab[i-1];
}else {
tab[i] = ((double)populationFitness[i]/(double)scoreSum);
}
}

for(int i=0;i<randomValues.length;i++) {
for(int j=1;j<tab.length;j++) {
if(randomValues[i] <= tab[0] ) {
selectedPopulation[i] = populationFitness[0];
break;
}else if(tab[j-1] < randomValues[i] && randomValues[i] <= tab[j]) {
selectedPopulation[i] = populationFitness[j];
break;
}
}
}
//printCities(tab, "Probability to select route");
//printCities(randomValues, "Generated random values");
return selectedPopulation;
}

/**
 * Losuje populacje metoda turniejowa
 *
 * @param population - tablica z populacja
 * @param populationFitness - tablica z wynikami populacji
 * @param tournamentSize - rozmiar turnieju
 * @return Wylosowana populacja
 */
public static int[] tournamentMethodSelection(int[] populationFitness, int tournamentSize) {

	int[] randomNumbers = new int[tournamentSize];
	int[] selectedPopulation = new int[populationSize];
	int max = 0;
	
	for(int i = 0;i<populationSize;i++) {
		
		for(int j=0;j<tournamentSize;j++) {
			
			randomNumbers[j] = random.nextInt(populationSize);
			max = Math.max(max, populationFitness[randomNumbers[j]]);
			
		}
		
		for(int j=0;j<tournamentSize;j++) {
			if(max == populationFitness[randomNumbers[j]]) {
				selectedPopulation[i] = populationFitness[randomNumbers[j]];
				break;
			}
		}
		
		max = 0;

	}


return selectedPopulation;
}

/**
 * Mutuje elementy losowo wybrane elementy populacji poprzez ich zamiane
 *
 * @param population - Wskazana populacja
 * @param mutationChance - Szansa na wystapienie mutacji
 * @return Populacja po mutacji
 */
public static int[][] mutate(int[][] population, double mutationChance) {

int[][] populationMutated = new int[population.length][population[0].length];
int r1;
int r2;
populationMutated = population;

for(int i = 0; i<populationMutated.length;i++) {
double randomValue = random.nextDouble();
if(randomValue>0 && randomValue<mutationChance) {
r1 = random.nextInt(populationMutated[i].length);
do {
	r2 = random.nextInt(populationMutated[i].length);
}while(r1 == r2);

int temp = populationMutated[i][r1];
populationMutated[i][r1] = populationMutated[i][r2];
populationMutated[i][r2] = temp;
}
}
return populationMutated;


}

/**
 * Wyswietla wskazana tablice dwuwymiarowa
 *
 * @param tab - Tablica do wyswietlenia
 * @param text - Opis
 */
public static void printCities(int[][] tab, String text) {
System.out.println(text);
for (int i = 0; i < tab.length; i++) {
for (int j = 0; j < tab[i].length; j++) {
System.out.print(tab[i][j] + "\t");
}
System.out.println();
}
System.out.println();
}



/**
 * Wyœwietla wskazana tablice w konsoli
 *
 * @param tab - Tablica do wyswietlenia
 * @param text - Opis
 */
public static void printCities(int[] tab, String text) {
System.out.println(text);
for(int i=0;i<tab.length;i++) {
System.out.println((i+1)+". " + tab[i]);
}
}

/**
 * Wyœwietla tablice w konsoli
 *
 * @param tab - Tablica do wyœwietlenia
 * @param text - Opis
 */
public static void printCities(double[] tab, String text) {
System.out.println(text);
for(int i=0;i<tab.length;i++) {
System.out.println((i+1)+". " + tab[i]);
}
}
/**
 * Tworzy nowa populacja potomkow krzyzyjac operatorem PMX
 *
 * @param population - Populacja na ktorej zostanie przeprowadzone krzyzowanie
 * @return Nowa populacja potomkow
 */
public static int[][] crossoverPMX(int[][] population, int chanceForCrossover){

int k1 = 0;
int k2 = 0;
int randomIndex1 = 0;
int randomIndex2 = 0;
	
	ArrayList<int[]> newPopulation = new ArrayList<int[]>();
	
	for(int i=0; i<population.length;i++) {
		newPopulation.add(population[i]);
	}
	

     for(int a=0;a<numberOfCrossovers;a++) {
     
     ArrayList<Integer> notCopiedValues = new ArrayList<Integer>();
    	 
     k1 = random.nextInt(population[0].length/2);
     k2 = random.nextInt(population[0].length/2) + population[0].length/2;
     
     randomIndex1 = random.nextInt(population.length);
     int[] parent1 = population[randomIndex1];
     do {
     randomIndex2 = random.nextInt(population.length);
     }while(randomIndex1 == randomIndex2);
     
     int[] parent2 = population[randomIndex2];
     
     int[] child = new int[population[0].length];
     
     for(int i=0;i<child.length;i++) {
    	 child[i] = -1;
     }
     
     for(int i=k1;i<k2;i++) {
    	 child[i] = parent1[i];
    	 boolean exist = false;

    	 for(int j=k1;j<k2;j++) {

    	 if(parent2[i] == parent1[j]) {
    	 exist = true;
    	 break;
    	 }
    	 }

    	 if(!exist) {
    	 notCopiedValues.add(parent2[i]);
    	 }
     }
     
     for(int i=k1;i<k2;i++) {
    	 
    	 if(!notCopiedValues.isEmpty() && notCopiedValues.contains(parent2[i])) {
    		 int pom = parent1[i];
    		 for(int j=0;j<child.length;j++) {
    			 if(pom == parent2[j]) {
    				 pom = parent1[j];
    				 if(j<k1 || j>=k2) {
    					 child[j] = parent2[i];
    					 break;
    				 }else {
    					 j = -1;
    				 }
    			 }
    		 }
    	 }
     }
     
     for(int i=0;i<child.length;i++) {
    	 if(child[i] == -1) {
    		 child[i] = parent2[i];
    	 }
     }

     
     
     newPopulation.add(child);
     
     }
     
     int[][] population2 = new int[newPopulation.size()][population[0].length];
     
     for(int i=0;i<population2.length;i++) {
    	 population2[i] = newPopulation.get(i);
     }
     
     return population2;
}



public static int[][] sortPopulationAsc(int[][] population){


int[] score = populationScore(population, data);
int[][] populationSorted = new int[populationSize][population[0].length];

HashMap<Integer,int[]> scoreAndPopulation = new HashMap<Integer, int[]>();

for(int i=0;i<population.length;i++) {
scoreAndPopulation.put(score[i], population[i]);
}
Arrays.sort(score);
for(int i=0;i<populationSorted.length;i++) {
populationSorted[i] = scoreAndPopulation.get(score[i]);
}


return populationSorted;
}

public static void printPopulationAndScore(int[][] population, String text){

int[] score = populationScore(population, data);
System.out.println(text);
System.out.println("----------------------------------PRINTING POPULATION AND SCORE-----------------------------------------");

for(int i=0;i<population.length;i++) {
System.out.print((i+1) + ":" + "(" + score[i] + ")" + "\t");
for(int j=0;j<population[i].length;j++) {
System.out.print(population[i][j] + "-");
}
System.out.println();
}
System.out.println("---------------------------------END---------------------------------------");

}



}
