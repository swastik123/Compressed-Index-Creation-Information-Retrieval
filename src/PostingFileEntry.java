
public class PostingFileEntry {

	String docId;
	Integer frequency;

	public PostingFileEntry(String doc, Integer freq){
		this.docId=doc;
		this.frequency=freq;
	}
	
	
	
	
	@Override
	public String toString() {
		return docId + " : " + frequency;
	}
}
