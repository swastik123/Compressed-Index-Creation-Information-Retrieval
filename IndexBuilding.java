import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class IndexBuilding {

	static Map<String, DictionaryEntryTerm> dictionary = new HashMap<String, DictionaryEntryTerm>();
	static Map<String, DictionaryEntryTerm> lemmaDictionary = new HashMap<String, DictionaryEntryTerm>();
	static HashSet<String> stopwordsDictionary = new HashSet<String>();

	static Map<String, DocumentFrequency> maxFreqStems = new HashMap<String, DocumentFrequency>();

	public static void main(String[] args) {

		if(args.length>2){
			buildIndex(args);
		}
		else{
			System.out.println("Please enter arguments as mentioned below:");
			System.out.println("<Usage>: IndexBuilding <dataset-path> <stopwords file-path> <output-directory path>");
		}
	}

	public static void buildIndex(String[] arr){

		try{		
			String outputPath = arr[2];			
			IndexBuilding indexBuilding = new IndexBuilding();
			indexBuilding.deleteAllFilesInADirectory(outputPath);
			stopwordsDictionary = indexBuilding.getListOfStopWords(arr[1]);

			long startTimeV1 = System.currentTimeMillis();
			Map<String, DictionaryEntryTerm> uncompressedIndexv1 = indexBuilding.buildIndexVersion1(arr[0], stopwordsDictionary);
			long endTimeV1 = System.currentTimeMillis();

			long timeV1 = endTimeV1 - startTimeV1;
			System.out.println("Time taken to build Version 1 of Index (ms): "+timeV1);
			Map<String, DictionaryEntryTerm> uncompressedIndexv1Sorted = new TreeMap<String, DictionaryEntryTerm>(uncompressedIndexv1);
			indexBuilding.printIndex(uncompressedIndexv1Sorted, outputPath+File.separator+"Index_Version1.uncompressed");


			long startTimeV2 = System.currentTimeMillis();
			Map<String, DictionaryEntryTerm> uncompressedIndexv2 = indexBuilding.buildIndexVersion2(arr[0], stopwordsDictionary);
			long endTimeV2 = System.currentTimeMillis();
			long timeV2 = endTimeV2 - startTimeV2;
			System.out.println("Time taken to build Version 2 of Index (ms): "+timeV2);
			Map<String, DictionaryEntryTerm> uncompressedIndexv2Sorted = new TreeMap<String, DictionaryEntryTerm>(uncompressedIndexv2);			
			indexBuilding.printIndex(uncompressedIndexv2Sorted, outputPath+File.separator+"Index_Version2.uncompressed");

			Map<String, Compresser.CompressedDictionaryEntry> compressedIndexv1 = Compresser.createCompressedIndexVersion1(uncompressedIndexv1);
			Map<String, Compresser.CompressedDictionaryEntry> compressedIndexv1Sorted = new TreeMap<String, Compresser.CompressedDictionaryEntry>(compressedIndexv1);

			Compresser compresser = new Compresser();
			compresser.printCompressedIndex(compressedIndexv1Sorted, outputPath+File.separator+"Index_Version1.compressed");

			Map<String, Compresser.CompressedDictionaryEntry> compressedIndexv2 = Compresser.createCompressedIndexVersion2(uncompressedIndexv2);
			Map<String, Compresser.CompressedDictionaryEntry> compressedIndexv2Sorted = new TreeMap<String, Compresser.CompressedDictionaryEntry>(compressedIndexv2);	

			compresser.printCompressedIndex(compressedIndexv2Sorted, outputPath+File.separator+"Index_Version2.compressed");


			System.out.println("Size of Index Version1 uncompressed (in bytes): "+getFileSizeInKb(outputPath+File.separator+"Index_Version1.uncompressed"));
			System.out.println("Size of Index Version1 compressed (in bytes): "+getFileSizeInKb(outputPath+File.separator+"Index_Version1.compressed"));
			System.out.println("Size of Index Version2 uncompressed (in bytes): "+getFileSizeInKb(outputPath+File.separator+"Index_Version2.uncompressed"));
			System.out.println("Size of Index Version2 compressed (in bytes): "+getFileSizeInKb(outputPath+File.separator+"Index_Version2.compressed"));

			System.out.println("Number of inverted Lists in index V1: "+uncompressedIndexv1.size());

			System.out.println("Number of inverted Lists in index V2: "+uncompressedIndexv2.size());

			Stemmer stemmer = new Stemmer();

			String[] terms = { "Reynolds", "NASA", "Prandtl", "flow",
					"pressure", "boundary", "shock" };

			System.out.println("Analysis for following terms:");
			for(String term:terms){
				System.out.println(term);
			}

			System.out.println(String.format(
					"\n\t %-10s  %-10s %-10s %-10s ", "Lemma", "Doc Frequency",
					"Term Frequency", "Length of inverted list in bytes"));


			for (String term : terms) {
				String lemmaTerm = Lemmatizer.getInstance().getLemma(
						term.toLowerCase());

				lemmaTerm = lemmaTerm.replace(" ", "");
				DictionaryEntryTerm entry = uncompressedIndexv1
						.get(lemmaTerm);
				System.out
				.println(String.format(
						"\t %-10s  \t %-10d \t %-10d \t %-10d",
						lemmaTerm,
						entry.docFrequency,
						entry.termFrequency,
						Compresser
						.getCompressedPostingListSize(compressedIndexv1
								.get(lemmaTerm).postingList)));
			}




			System.out.println(String.format(
					"\n\t %-10s  %-10s %-10s %-10s ", "Term (Stem)", "Doc Frequency",
					"Term Frequency", "Length of inverted list in bytes"));

			for (String term : terms) {
				String stemmedTerm = stemmer.stem(term.toLowerCase());
				DictionaryEntryTerm entry = uncompressedIndexv2
						.get(stemmedTerm);
				System.out
				.println(String.format(
						"\t %-10s \t %-10d \t %-10d \t %-10d",
						stemmedTerm,
						entry.docFrequency,
						entry.termFrequency,
						Compresser
						.getCompressedPostingListSize(compressedIndexv2
								.get(stemmedTerm).postingList)));
			}


		}
		catch(Exception e){
			e.printStackTrace();
		}

	}






	public  Map<String, DictionaryEntryTerm> buildIndexVersion1(String folderPath, HashSet<String> stopWordsDict){

		Lemmatizer lemmatizer = new Lemmatizer();
		File folder = new File(folderPath);

		for(File file: folder.listFiles()){

			if(file.isFile()){
				Map<String, Integer> termFreqMap = lemmatizer.buildLemmaDictionary(file, stopWordsDict);

				buildIndexesVersion1(file.getName(),termFreqMap);
			}
		}

		return lemmaDictionary;
	}

	public  void buildIndexesVersion1(String docId, Map<String,Integer> lemmaTermFreqDict){

		long maxTermFreq = 0;
		long docLen = 0;


		for (String term : lemmaTermFreqDict.keySet()) {
			int termFreq = lemmaTermFreqDict.get(term);
			docLen += termFreq;
			if (!(stopwordsDictionary.contains(term))) {
				if (termFreq > maxTermFreq) {
					maxTermFreq = termFreq;
				}
				updatePostingValuesVersion1(docId, term, lemmaTermFreqDict.get(term));
			}
		}


		/*DocumentFrequency entry = new DocumentFrequency(docLen, maxTermFreq);
		maxFreqStems.put(docId, entry);*/


	}





	private  void updatePostingValuesVersion1(String docId, String term,
			Integer termFrequency) {
		DictionaryEntryTerm entry = null;
		if (lemmaDictionary != null)
			entry = lemmaDictionary.get(term);

		if (entry == null) {
			entry = new DictionaryEntryTerm(term, 0, 0,
					new LinkedList<PostingFileEntry>());
			entry.postingList = new LinkedList<PostingFileEntry>();

		}
		entry.postingList.add(new PostingFileEntry(docId, termFrequency));
		entry.docFrequency += 1;
		entry.termFrequency += termFrequency;
		lemmaDictionary.put(term, entry);

	}






	public  Map<String, DictionaryEntryTerm> buildIndexVersion2(String folderPath, HashSet<String> stopWordsDict){

		Stemmer stemmer = new Stemmer();
		File folder = new File(folderPath);

		for(File file: folder.listFiles()){

			if(file.isFile()){
				Map<String, Integer> termFreqMap = stemmer.buildTermDictionary(file, stopWordsDict);

				buildIndexes(file.getName(),termFreqMap);
			}
		}

		return dictionary;
	}

	public  void buildIndexes(String docId, Map<String,Integer> termFreqDict){

		long maxTermFreq = 0;
		long docLen = 0;


		for (String term : termFreqDict.keySet()) {
			int termFreq = termFreqDict.get(term);
			docLen += termFreq;
			if (!(stopwordsDictionary.contains(term))) {
				if (termFreq > maxTermFreq) {
					maxTermFreq = termFreq;
				}
				updatePostingValues(docId, term, termFreqDict.get(term));
			}
		}


		DocumentFrequency entry = new DocumentFrequency(docLen, maxTermFreq);
		maxFreqStems.put(docId, entry);
	}





	private  void updatePostingValues(String docId, String term,
			Integer termFrequency) {
		DictionaryEntryTerm entry = null;
		if (dictionary != null)
			entry = dictionary.get(term);

		if (entry == null) {
			entry = new DictionaryEntryTerm(term, 0, 0,
					new LinkedList<PostingFileEntry>());
			entry.postingList = new LinkedList<PostingFileEntry>();

		}
		entry.postingList.add(new PostingFileEntry(docId, termFrequency));
		entry.docFrequency += 1;
		entry.termFrequency += termFrequency;
		dictionary.put(term, entry);

	}



	public  HashSet<String> getListOfStopWords(String filePath){

		HashSet<String> stopWordsDict = new HashSet<String>();

		FileInputStream fileInputStream = null;
		DataInputStream dataInputStream = null;
		BufferedReader bufferedReader = null;

		try{
			File stopwordsFile = new File(filePath);
			fileInputStream = new FileInputStream(stopwordsFile);
			dataInputStream = new DataInputStream(fileInputStream);
			bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));

			String line = null;


			while((line=bufferedReader.readLine())!=null){

				stopWordsDict.add(line.toLowerCase());
			}
		}
		catch(Exception e){
			try {
				fileInputStream.close();
				dataInputStream.close();
				bufferedReader.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			e.printStackTrace();
		}
		return stopWordsDict;
	}



	public void printIndex(Map<String, DictionaryEntryTerm> indexMap,
			String Filename) {
		BufferedWriter writer = null;
		try {

			File newTextFile = new File(Filename);

			writer = new BufferedWriter(new FileWriter(newTextFile, true));
			writer.write("Key --> Term Frequency --> Doc Freq --> Postinglist");
			writer.newLine();
			Set keys = indexMap.keySet();
			Iterator i = keys.iterator();
			while (i.hasNext()) {
				String key = (String) i.next();
				Integer termFreq = (Integer) indexMap.get(key).termFrequency;

				Integer docFreq = (Integer) indexMap.get(key).docFrequency;

				writer.write(key + "-->" + termFreq + "-->" + docFreq + "-->");

				Iterator ite = indexMap.get(key).postingList.iterator();
				while (ite.hasNext()) {
					writer.write(ite.next().toString() + ",");
				}
				writer.newLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}



	public void deleteAllFilesInADirectory(String directoryPath){
		try{

			File file = new File(directoryPath);      
			String[] myFiles;    
			if(file.isDirectory()){
				myFiles = file.list();
				for (int i=0; i<myFiles.length; i++) {
					File myFile = new File(file, myFiles[i]); 
					myFile.delete();
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}



	public static double getFileSizeInKb(String filePath){

		double kb=0;
		try{
			File file = new File(filePath);
			if(file.exists()){
				double bytes = file.length();
				//kb = (bytes / 1024);
				kb=bytes;
			}
			else{
				throw new Exception();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return kb;
	}
}




