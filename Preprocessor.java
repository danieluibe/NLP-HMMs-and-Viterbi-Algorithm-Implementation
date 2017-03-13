package cs4740p2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

//import cc.mallet.fst.*;
public class Preprocessor {
	private int weaselSentences;
	private int sentenceCount;
	private int B;
	private int I;
	private int O;
	private static final String output_file = "C:/Cornell/cs4740/project2/nlp_project2_uncertainty/preprocessed/train_docs_small.txt";
	private static final String filepath = "C:/Cornell/cs4740/project2/nlp_project2_uncertainty/train/";
	public static final String validation_file = "C:/Cornell/cs4740/project2/nlp_project2_uncertainty/preprocessed/validation.txt";
	public static HashMap<String,Double> dict;
	public static HashMap<String, Double> wordtype;

	//	private static double[][] transitionProbs = new double[][]{
	//		  { 0.3044, 0.3089, 0.3867},
	//		  { 0.0474, 0.8645, 0.0881},
	//		  { 0.0000, 0.0131, 0.9869},
	//		};
	private static double[][] myTransitionProbs = new double[][]{
		{ 0, 0, 0},
		{ 0, 0, 0},
		{ 0, 0, 0},
	};

	public Preprocessor(){
		sentenceCount = 0;
		B = 0;
		I = 0;
		O = 0;
		dict = new HashMap<String, Double>();
		wordtype = new HashMap<String, Double>();
	}

	public double getLogProbWordGivenTag(String word, char tag){
		double denom = 0.0;
		double numerator = 0.0;
		if (tag == 'B' || tag == 'b')
			denom = B;
		if (tag == 'I' || tag == 'i')
			denom = I;
		if (tag == 'O' || tag == 'o')
			denom = O;
		if(dict.containsKey(word.toLowerCase() + "\t" + tag)){
			numerator = dict.get(word.toLowerCase() + "\t" + tag);
		}else{
			return Math.log(Double.MIN_VALUE);
		}

		if (denom == 0.0)
			return Math.log(Double.MIN_VALUE);
		else
			return Math.log(numerator) - Math.log(denom);
	}

	public double getProbWordGivenTag(String word, char tag){
		return Math.exp(getLogProbWordGivenTag(word,tag));
	}

	public double getTransitionProb(int i, int j){
		//		int i = 0;
		//		int j = 0;
		//		if (tagFrom == 'B' || tagFrom == 'b')
		//			i = 0;
		//		if (tagFrom == 'I' || tagFrom == 'i')
		//			i = 1;
		//		if (tagFrom == 'O' || tagFrom == 'o')
		//			i = 2;
		//		if (tagTo == 'B' || tagTo == 'b')
		//			j = 0;
		//		if (tagTo == 'I' || tagTo == 'i')
		//			j = 1;
		//		if (tagTo == 'O' || tagTo == 'o')
		//			j = 2;

		return myTransitionProbs[i][j];
	}

	public void addOneTransitionVal(char tagFrom, char tagTo){
		//System.out.println(tagFrom + "" + tagTo);
		int i = 0;
		int j = 0;
		if (tagFrom == 'B' || tagFrom == 'b')
			i = 0;
		if (tagFrom == 'I' || tagFrom == 'i')
			i = 1;
		if (tagFrom == 'O' || tagFrom == 'o')
			i = 2;
		if (tagTo == 'B' || tagTo == 'b')
			j = 0;
		if (tagTo == 'I' || tagTo == 'i')
			j = 1;
		if (tagTo == 'O' || tagTo == 'o')
			j = 2;

		myTransitionProbs[i][j] += 1;
	}

	public void normalizeProbs(){
		System.out.println("before");
		for (int i = 0; i < 3; i ++)
			for (int j = 0; j < 3; j++)
				System.out.println("i: " + i + "j: " + j + "//" + myTransitionProbs[i][j]);
		for (int i = 0; i < 3; i ++){
			double denominator = (myTransitionProbs[i][0] +myTransitionProbs[i][1] +myTransitionProbs[i][2]);
			for (int j = 0; j < 3; j++){
				myTransitionProbs[i][j] /= denominator;
			}
		}
		System.out.println("after");
		for (int i = 0; i < 3; i ++)
			for (int j = 0; j < 3; j++)
				System.out.println("i: " + i + "j: " + j + "//" + myTransitionProbs[i][j]);
	}

	public double getLogTransitionProb(char tagFrom, char tagTo){
		return Math.log(getTransitionProb(tagFrom, tagTo));
	}

	public void preprocess(){
		preprocess(-1.0,-1.0, 0.8);
	}

	public void preprocess(double percentBI, double percentage){
		preprocess(percentBI,-1, percentage);
	}
	public void preprocess(double percentBI, double percentWeasel, double percentage){
		String[] r = connectFileWithPercentage(filepath, percentage, percentBI, percentWeasel);
		printToFile(r[0],output_file);
		printToFile(r[1],validation_file);
		normalizeProbs();
	}

	public String txt2StringBIO(File file) {
		StringBuilder result = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String s = null;
			String currentCue = "";
			ArrayList<String[]> words = new ArrayList<String[]>();
			while ((s = br.readLine()) != null) {
				if (s.equals("")){
					sentenceCount++;
					weaselSentences += (! currentCue.equals("")) ? 1 : 0;
					processString(words,currentCue,result);
					words.clear();
					currentCue = "";
					words.clear();
				} else {
					String[] str = s.split("\t");
					if (! str[2].equals("_"))
						currentCue = str[2];
					words.add(str);
				}
				//				result.append(System.lineSeparator() + s);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.toString();
	}

	public String simpletxt2StringBIO(File file) {
		StringBuilder result = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String s = null;
			String currentCue = "";
			while ((s = br.readLine()) != null) {
				if (s.equals("")){
					currentCue = "";
				} else {
					String[] str = s.split("\t");
					if (str.length == 3){
						if (str[2].equals("_")){
							str[2] = "O";
						}
						else if(str[2].equals(currentCue)){
							str[2] = "I";
						}
						else {
							currentCue = str[2];
							str[2] = "B";
						}
						s = str[0]  + "\t" + str[2];
					}else {
						System.out.print("Bad Split!");
					}
				}
				result.append(System.lineSeparator() + s);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.toString();
	}

	// d= 0.5 : ~50% (B+I) words
	public String resampleToBalanceBIO(File file, double d){
		boolean done = false;
		StringBuilder result = new StringBuilder();
		int index = 0;
		while (!done) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String s = null;
				String currentCue = "";
				while ((s = br.readLine()) != null) {
					index++;
					//breaks?
					//System.out.println("(B+I)/(double)(B+I+O):" + (B+I)/(double)(B+I+O));
					if (d - (B+I)/(double)(B+I+O) < 0 ) {
						
						done = true;
						break;
					}
					if(index >= 10000){
						done = true;
						break;
					}
					if (s.equals("")){
						//						sentenceCount++;
						//						weaselSentences += (! currentCue.equals("")) ? 1 : 0;
						currentCue = "";
					} else {
						String[] str = s.split("\t");
						if (str.length == 3){
							if (str[2].equals("_")){
								//							str[2] = "O";
								//							O++;
								continue;
							}
							else if(str[2].equals(currentCue)){
								str[2] = "I";
								I++;
							}
							else {
								currentCue = str[2];
								str[2] = "B";
								B++;
							}
							s = str[0].toLowerCase()  + "\t" + str[2];
							if (wordtype.containsKey(str[0].toLowerCase()))
								wordtype.put(str[0].toLowerCase(), wordtype.get(str[0].toLowerCase()) + 1);
							else
								wordtype.put(str[0].toLowerCase(), 1d);
							
							if (dict.containsKey(s))
								dict.put(s, dict.get(s) + 1);
							else
								dict.put(s, 1d);
						}else {
							System.out.print("Bad Split!");
						}
					}
					if (!s.equals(""))
						result.append(System.lineSeparator() + s);
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//System.out.println("index"+index);
		return result.toString();
	}
	// d= 0.5 : ~50% weasel sentences
	public String resampleToBalanceWeaselSentences(File file, double d){
		boolean done = false;
		int index = 0;
		StringBuilder result = new StringBuilder();
		while (!done) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String s = null;
				String currentCue = "";
				ArrayList<String[]> words = new ArrayList<String[]>();
				while ((s = br.readLine()) != null) {
					index++;
					if (d - weaselSentences/(double)sentenceCount <= 0 ) {
						done = true;
						break;
					}
					if(index >= 10000){
						done = true;
						break;
					}
					if (s.equals("") ){

						if (!currentCue.equals("")){

							sentenceCount++;
							weaselSentences ++;
							processString(words,currentCue,result);
							//							for (String[] word: words)
							//								for (String i: word)
							//									System.out.println(i);
						}
						words.clear();
						currentCue = "";
					} else {
						String[] str = s.split("\t");
						if (! str[2].equals("_"))
							currentCue = str[2];
						words.add(str);
					}
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result.toString();
	}

	//	take in an arraylist of arrays of length 3 where each array is a word, and the list is a sentence
	private void processString(ArrayList<String[]> words, String currentCue,StringBuilder result){
		processString(words,currentCue,result,true);
	}

	//addOutsides means that it will add O tagged words to the trainins set.
	// useful for balancing between B/I/O tags.
	private void processString(ArrayList<String[]> words, String currentCue,StringBuilder result,boolean addOutsides){		
		//		System.out.println("processString");
		String lastTag = "";
		currentCue = "";
		for (String[] word: words) {
			String s = "";
			if (word.length == 3){
				if (word[2].equals("_")){
					if (! addOutsides) {
						continue;
					} else {
						word[2] = "O";
						O++;
					}
				}
				else if(word[2].equals(currentCue)){
					word[2] = "I";
					I++;
				}
				else {
					currentCue = word[2];
					word[2] = "B";
					B++;
				}
				if (wordtype.containsKey(word[0].toLowerCase()))
					wordtype.put(word[0].toLowerCase(), wordtype.get(word[0].toLowerCase()) + 1);
				else
					wordtype.put(word[0].toLowerCase(), 1d);
				
				s = word[0].toLowerCase()  + "\t" + word[2];

				if (!lastTag.equals("")) {
					addOneTransitionVal(lastTag.charAt(0), word[2].charAt(0));
				}
				lastTag = word[2];


				if (dict.containsKey(s))
					dict.put(s, dict.get(s) + 1);
				else
					dict.put(s, 1d);


			}else {
				System.out.print("Bad Split!");
				System.out.println(word[0]);
			}					
			result.append(System.lineSeparator() + s);
		}
	}


	public String[] FilePathGet(String s) {
		File file = new File(s);
		String filepath[];
		filepath = file.list();
		return filepath;
	}

	public String[] connectFileWithPercentage(String f, double percentageTraining, double percentBI, double percentWeasel){
		String re = "";
		String FilePath[] = FilePathGet(f);
		//		for (String str: FilePath)
		//			System.out.println(str);
		if (FilePath == null) System.err.println("Train docs not found at " + f);
		for (int i = 0; i < (int) (FilePath.length * percentageTraining); i++) {
			//System.out.println("train set:" + FilePath[i]);
			if (FilePath[i].equals(".DS_Store")) continue; // sorry I have a mac
			File File = new File(f + FilePath[i]);
			re = re + " " + txt2StringBIO(File);
			if (percentWeasel > 0)			
				re = re + " " + resampleToBalanceWeaselSentences(File, percentWeasel);
			if (percentBI > 0){
				//System.out.println("here");
				//System.out.println("(B+I)/(double)(B+I+O):" + (B+I)/(double)(B+I+O));
				re = re + " " + resampleToBalanceBIO(File, percentBI);
			}

			//			System.out.println("file length: " + (int) (FilePath.length * d)+"file: \"" + FilePath[i] + "\" was input.");
		}
		String va = "";
		for (int i = (int) (FilePath.length * percentageTraining); i < FilePath.length; i++) {
			//System.out.println("validation set:" + FilePath[i]);
			if (FilePath[i].equals(".DS_Store")) continue; // sorry I have a mac
			File File = new File(f + FilePath[i]);
			va = va + " " + simpletxt2StringBIO(File);
		}
		String[] r = new String[]{re, va};
		return r;
	}

	public String[] connectfileWithPercentage(String f,  double d) {
		return connectFileWithPercentage(f,d,-1,-1);
	}

	public void printToFile(String s, String outputpath){
		try( PrintWriter out = new PrintWriter(outputpath)){
			out.println(s);
		} catch (FileNotFoundException e) {
			System.out.println("output folder not found: " + outputpath);
			e.printStackTrace();
		}
	}

	public double getBIOprob(int i){
		double re = 0;
		if(i == 0) re = B/(double)(B+I+O);
		else if(i == 1) re = I/(double)(B+I+O);
		else re = O/(double)(B+I+O);
		return re;
	}



	public String toString(){
		String str = "";
		str += "Read from: " + filepath + "\n";
		str += "Wrote to: " + output_file + "\n";
		str += "Sentence Count: " + sentenceCount + "\n";
		str += "B: " + B + ", " + (int)(0.5+100.0*B/(double)(B+I+O)) + "% \n";
		str += "I: " + I + ", " + (int)(0.5+100.0*I/(double)(B+I+O)) + "% \n";
		str += "O: " + O + ", " + (int)(0.5+100.0*O/(double)(B+I+O)) + "% \n";
		return str;
	}
	public static void main(String[] args){
		Preprocessor hm = new Preprocessor();
		double BIvsOpercent = -0.1;
		double weaselSentencePercent = -0.5;
		hm.preprocess(BIvsOpercent,weaselSentencePercent,0.8);
		System.out.println(hm);
		//		System.out.println(hm.getLogProbWordGivenTag("most",'B'));
		//		System.out.println(hm.getLogTransitionProb('I','B'));
		System.out.println( (int)(100.0*hm.weaselSentences/(1.0*hm.sentenceCount))+ "% weasel sentences");
		//	      c
		System.out.println("the b" + dict.get("the" + "\t" + 'B'));
		System.out.println("the I" + dict.get("the" + "\t" + 'I'));
		System.out.println("the O" + dict.get("the" + "\t" + 'O'));
		System.out.println("the map" + wordtype.get("the"));
		System.out.println("The b" + dict.get("The" + "\t" + 'B'));
		System.out.println("The I" + dict.get("The" + "\t" + 'I'));
		System.out.println("The O" + dict.get("The" + "\t" + 'O'));
		System.out.println("the map" + wordtype.get("The"));
		System.out.println(hm.getProbWordGivenTag("the",'B'));		
		System.out.println(hm.getProbWordGivenTag("the",'O'));
		System.out.println("");
		System.out.println(hm.getProbWordGivenTag("the",'B'));		
		System.out.println("allows" + wordtype.get("allows"));
	}
}
