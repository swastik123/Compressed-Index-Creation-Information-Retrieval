# Compressed-Index-Creation-Information-Retrieval

Following are the commands to run:

javac PostingFileEntry.java
javac DocumentFrequency.java
javac DictionaryEntryTerm.java
javac Stemmer.java
javac TrieDictionary.java
javac Compresser.java
javac -classpath .:/usr/local/corenlp341/stanford-corenlp-3.4.1.jar:/usr/local/corenlp341/stanford-corenlp-3.4.1-models.jar Lemmatizer.java
javac IndexBuilding.java

IndexBuilding.java is the main file to run.
Use following command to run it:
java -classpath .:/usr/local/corenlp341/stanford-corenlp-3.4.1.jar:/usr/local/corenlp341/stanford-corenlp-3.4.1-models.jar IndexBuilding <Cranfield dataset-path> <stopwords file-path> <output-directory path>

Example:
java -classpath .:/usr/local/corenlp341/stanford-corenlp-3.4.1.jar:/usr/local/corenlp341/stanford-corenlp-3.4.1-models.jar IndexBuilding Cranfield stopwords.txt hw2_output
