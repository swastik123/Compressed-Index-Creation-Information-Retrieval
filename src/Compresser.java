import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



public class Compresser {



	public static Map<String, CompressedDictionaryEntry> createCompressedIndexVersion1(
			Map<String, DictionaryEntryTerm> uncompressedIndex) {
		Map<String, CompressedDictionaryEntry> compressedIndex = new HashMap<>();
		for (String term : uncompressedIndex.keySet()) {
			DictionaryEntryTerm entry = uncompressedIndex.get(term);
			List<PostingValueEntry> postingList = new ArrayList<>(
					entry.postingList.size());
			for (PostingFileEntry postingFileEntry : entry.postingList) {

				byte[] docId = gammaCode(postingFileEntry.docId);
				byte[] frequency = deltaCode(postingFileEntry.frequency) ;
				postingList.add(new PostingValueEntry(docId, frequency));
			}
			int docFrequency = entry.docFrequency;
			int termFrequency = entry.termFrequency;
			CompressedDictionaryEntry compressedEntry = new CompressedDictionaryEntry(term,
					docFrequency, termFrequency, postingList);
			compressedIndex.put(term, compressedEntry);
		}
		return compressedIndex;

	}

	public static Map<String, CompressedDictionaryEntry> createCompressedIndexVersion2(
			Map<String, DictionaryEntryTerm> uncompressedIndex) {
		Map<String, CompressedDictionaryEntry> compressedIndex = new HashMap<>();
		for (String term : uncompressedIndex.keySet()) {
			DictionaryEntryTerm entry = uncompressedIndex.get(term);
			List<PostingValueEntry> postingList = new ArrayList<>(
					entry.postingList.size());
			for (PostingFileEntry postingFileEntry : entry.postingList) {
				byte[] docId = gammaCode(postingFileEntry.docId);
				byte[] frequency = deltaCode(postingFileEntry.frequency);
				postingList.add(new PostingValueEntry(docId, frequency));
			}
			int docFrequency = entry.docFrequency;
			int termFrequency = entry.termFrequency;
			CompressedDictionaryEntry compressedEntry = new CompressedDictionaryEntry(term,
					docFrequency, termFrequency, postingList);
			compressedIndex.put(term, compressedEntry);
		}
		return compressedIndex;

	}

	public static byte[] gammaCode(int number) {
		String gammacode = getGammaCode(number);
		return convertToByteArray(gammacode);
		// return gammacode.getBytes();
	}


	public static byte[] gammmaCode(String number) {

		String gammacode = getGammaCode(number);
		return convertToByteArray(gammacode);
		// return gammacode.getBytes();
	}

	public static byte[] gammaCode(String number) {

		String binaryNum = stringToBinary(number);
		// get gamma code of the length of binary
		String gammaCode = getGammaCode(binaryNum.length());
		// Get the offset by removing leading 1 bit
		String offset = gammaCode.substring(1);
		return convertToByteArray(gammaCode.concat(offset));
	}

	public static String stringToBinary(String str ) {
		byte[] bytes = str.getBytes();
		StringBuilder binary = new StringBuilder();
		for (byte b : bytes)
		{
			binary.append(Integer.toBinaryString((int) b));

		}
		return binary.toString();        
	}

	public static byte[] deltaCode(String num) {
		// TODO Auto-generated method stub
		// get binary code
		String binaryNum = stringToBinary(num);
		// get gamma code of the length of binary
		String gammaCode = getGammaCode(binaryNum.length());
		// Get the offset by removing leading 1 bit
		String offset = gammaCode.substring(1);
		return convertToByteArray(gammaCode.concat(offset));

	}

	public static byte[] deltaCode(int i) {
		// TODO Auto-generated method stub
		// get binary code
		String binaryNum = Integer.toBinaryString(i);
		// get gamma code of the length of binary
		String gammaCode = getGammaCode(binaryNum.length());
		// Get the offset by removing leading 1 bit
		String offset = gammaCode.substring(1);
		return convertToByteArray(gammaCode.concat(offset));

	}

	private static String getGammaCode(String number) {
		// convert To Binary
		//String binaryNumber = Integer.toBinaryString(number);

		String binaryNumber = stringToBinary(number);
		// Get the offset by removing leading 1 bit
		String offset = binaryNumber.substring(1);
		// Get unary code of the offset length
		String unaryCode = getUnaryCode(offset.length());
		String gammacode = unaryCode + "0" + offset;
		return gammacode;
	}

	private static String getGammaCode(int number) {
		// convert To Binary
		String binaryNumber = Integer.toBinaryString(number);
		// Get the offset by removing leading 1 bit
		String offset = binaryNumber.substring(1);
		// Get unary code of the offset length
		String unaryCode = getUnaryCode(offset.length());
		String gammacode = unaryCode + "0" + offset;
		return gammacode;
	}

	public static String getUnaryCode(int length) {
		// TODO Auto-generated method stub
		String unaryCode = "";
		for (int i = 0; i < length; i++) {
			unaryCode += "1";
		}
		return unaryCode;
	}



	private static byte[] convertToByteArray(String gammacode) {
		BitSet bitSet = new BitSet(gammacode.length());
		for (int i = 0; i < gammacode.length(); i++) {
			Boolean value = gammacode.charAt(i) == '1' ? true : false;
			bitSet.set(i, value);
		}
		return bitSet.toByteArray();
	}

	public  void printCompressedIndex(
			Map<String,CompressedDictionaryEntry> compressedIndex,
			String FileName) {
		try {
			Trie dict = new Trie();
			RandomAccessFile newTextFile = new RandomAccessFile(FileName, "rw");
			ArrayList<String> pointer = new ArrayList<String>();

			ArrayList<String> keyarray = new ArrayList<String>(
					compressedIndex.keySet());
			String key = keyarray.get(0);
			String prefix = "";
			String oldprefix = "";
			String suffix = "";
			if (key.length() > 2)
				dict.insert(key);
			String a = Long.toString(newTextFile.getFilePointer()+1);
			pointer.add(a);
			newTextFile.write((key.length()+key).getBytes());
			for (int len = 1; len < keyarray.size(); len++) {
				if (key.length() > 2 && keyarray.get(len).length() > 2
						&& dict != null) {
					if (dict.getMatchingPrefix(keyarray.get(len)).length() > 0) {
						prefix = dict.getMatchingPrefix(keyarray.get(len));
						if (!oldprefix.equals(prefix)
								&& oldprefix.contains(prefix)) {
							key = keyarray.get(len);
							suffix = keyarray.get(len).replace(prefix, "")
									.trim();
							String p = Long.toString(newTextFile.getFilePointer()+1);
							pointer.add(p);
							newTextFile
							.write((key.length() + prefix + "*" + suffix)
									.getBytes());

							dict = null;
						} else {
							suffix = keyarray.get(len).replace(prefix, "")
									.trim();
							String p = Long.toString(newTextFile.getFilePointer()+1);
							pointer.add(p);
							newTextFile.write((suffix.length()+"#" + suffix).getBytes());

						}
					} else {
						dict = null;
						key = keyarray.get(len);
						dict = new Trie();
						dict.insert(key);
						if (len < (keyarray.size() - 1)
								&& dict.getMatchingPrefix(keyarray.get(len + 1))
								.length() > 0) {
							prefix = dict.getMatchingPrefix(keyarray
									.get(len + 1));
							suffix = keyarray.get(len).replace(prefix, "")
									.trim();
							String p = Long.toString(newTextFile.getFilePointer()+1);
							pointer.add(p);
							newTextFile
							.write((key.length() + prefix + "*" + suffix)
									.getBytes());

						} else{
							String p = Long.toString(newTextFile.getFilePointer()+1);
							pointer.add(p);
							newTextFile.write((key.length() + key).getBytes());}

					}
				} else if (key.length() > 3 && keyarray.get(len).length() > 3
						&& dict == null) {
					dict = new Trie();
					dict.insert(key);
					if (dict.getMatchingPrefix(keyarray.get(len)).length() > 0) {
						prefix = dict.getMatchingPrefix(keyarray.get(len));
						if (!oldprefix.equals(prefix)
								&& oldprefix.contains(prefix)) {
							key = keyarray.get(len);
							suffix = keyarray.get(len).replace(prefix, "")
									.trim();
							String p = Long.toString(newTextFile.getFilePointer()+1);
							pointer.add(p);
							newTextFile.write((key.length() + prefix+"*")
									.getBytes());
							//if (suffix.length() > 0)
							newTextFile.write((suffix.length()+"#" + suffix).getBytes());
							dict = null;

						} else {
							suffix = keyarray.get(len).replace(prefix, "")
									.trim();
							String p = Long.toString(newTextFile.getFilePointer()+1);
							pointer.add(p);
							newTextFile.write((suffix.length()+"#" + suffix).getBytes());
						}
					} else {
						dict = null;
						key = keyarray.get(len);
						dict = new Trie();
						dict.insert(key);
						if (len < (keyarray.size() - 1)
								&& dict.getMatchingPrefix(keyarray.get(len + 1))
								.length() > 0) {
							prefix = dict.getMatchingPrefix(keyarray
									.get(len + 1));
							suffix = keyarray.get(len).replace(prefix, "").trim();
							String p = Long.toString(newTextFile.getFilePointer()+1);
							pointer.add(p);
							newTextFile.write((key.length() + prefix+"*" )
									.getBytes());
							//if (suffix.length() > 0)
							newTextFile.write(( suffix).getBytes());

						} else{
							String p = Long.toString(newTextFile.getFilePointer()+1);
							pointer.add(p);
							newTextFile.write((key.length() + key).getBytes());}
					}
				} else {
					key = keyarray.get(len);
					String p = Long.toString(newTextFile.getFilePointer()+1);
					pointer.add(p);
					newTextFile.write((key.length() + key).getBytes());
					dict = null;
				}
				oldprefix = prefix;
				/*String p = Long.toString(newTextFile.getFilePointer()+1);
				pointer.add(p);*/

			}

			newTextFile.write(System.getProperty("line.separator")
					.getBytes());
			Iterator postite = compressedIndex.keySet().iterator();
			int counter=1;
			for (int j = 0; j < pointer.size(); j++) {
				ArrayList<Integer> postfreq = new ArrayList<Integer>();
				if(counter>8)
					counter=1;
				if(counter==1)
					newTextFile.write((pointer.get(j) + "-").getBytes());
				if (postite.hasNext()) {
					String postkey = (String) postite.next();
					newTextFile.write(("" + compressedIndex.get(postkey).docFrequency)
							.getBytes());
					Iterator ite = compressedIndex.get(postkey).postingList.iterator();
					while (ite.hasNext()) {
						PostingValueEntry c = (PostingValueEntry) ite.next();

						newTextFile.write(c.docId);
						newTextFile.writeBytes("-");
						newTextFile.write((c.frequency));
					}


				}
				newTextFile.write(System.getProperty("line.separator")
						.getBytes());
				counter++;

			}


		} catch (Exception e) {
			e.printStackTrace();

		}
	}




	static class PostingValueEntry implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		byte[] docId;
		byte[] frequency;

		public PostingValueEntry(byte[] docID, byte[] frequency) {
			this.docId = docID;
			this.frequency = frequency;
		}

		public PostingValueEntry() {
		}

	}

	static class CompressedDictionaryEntry implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		String term;
		int docFrequency;
		int termFrequency;
		List<PostingValueEntry> postingList;

		public CompressedDictionaryEntry(String term, int docFrequency,
				int termFrequency, List<PostingValueEntry> postingList) {
			this.term = term;
			this.docFrequency = docFrequency;
			this.termFrequency = termFrequency;
			this.postingList = postingList;
		}
	}


	public static int byteArrayToInt(byte[] b) {
		if (b.length == 4)
			return b[0] << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8
					| (b[3] & 0xff);
		else if (b.length == 2)
			return 0x00 << 24 | 0x00 << 16 | (b[0] & 0xff) << 8 | (b[1] & 0xff);

		return 0;
	}



	public static long getCompressedPostingListSize(
			List<PostingValueEntry> postingList) {
		long length = 0;
		for (PostingValueEntry postingEntry : postingList) {
			length += postingEntry.docId.length + byteArrayToInt(postingEntry.frequency) ; // size(tf))
		}
		return length;
	}
}
