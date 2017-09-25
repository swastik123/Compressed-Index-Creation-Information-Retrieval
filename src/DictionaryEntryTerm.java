import java.util.List;


public class DictionaryEntryTerm {



	String term;
	Integer docFrequency;
	Integer termFrequency;
	List<PostingFileEntry> postingList;


	public DictionaryEntryTerm(String term, Integer docFreq, Integer termFreq, List<PostingFileEntry> list){
		this.term=term;
		this.docFrequency=docFreq;
		this.termFrequency=termFreq;
		this.postingList=list;
	}

}
