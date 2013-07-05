package edu.ufl.cise;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransportException;

import streamcorpus.Sentence;
import streamcorpus.StreamItem;
import streamcorpus.Token;
import edu.ufl.cise.pipeline.Entity;
import edu.ufl.cise.pipeline.Preprocessor;

/**
 * check http://sourceforge.net/projects/faststringutil/ structured graph learning sgml icml, online
 * lda, stremaing
 * 
 * Some issues: <br>
 * 
 * memory usage is ok, not much io<br>
 * Time: <br>
 * decrypt nonblocking<br>
 * split streamitem body and add it via softrefernce. in O(1) do comparison via cache. ORRRR use the
 * tokens they already provide
 * 
 * single machine single thread profiling
 * 
 * separate file path from file name by interning
 * 
 * 
 * 
 * @author morteza
 * 
 */
public class CorpusBatchProcessor {

	// Keep track of the # files, StreamItems, positive StreamItems, total file size processed.
	AtomicLong												fileCount													= new AtomicLong(0);
	AtomicLong												siCount														= new AtomicLong(0);
	AtomicLong												siFilteredCount										= new AtomicLong(0);
	AtomicLong												processedSize											= new AtomicLong(0);

	final Hashtable<String, Boolean>	toBeProcessedGPGFileHashTable			= LogReader
																																					.getToProcessFileList(SETTINGS.LOG_DIR_TO_PROCESS);

	// return hashTable; reprocess everything again
	final Hashtable<String, Boolean>	alreadyProcessedGPGFileHashTable	= new Hashtable<String, Boolean>();
	// LogReader.getPreLoggedFileList(SETTINGS.LOG_DIR_ARCHIVE);

	// for multi-process run
	final int													indexOfThisProcess;
	final int													totalNumProcesses;

	List<Entity>											listEntity;

	/**
	 * gets the index of thus process and total # of processes that this process is a member of to
	 * avoid duplicate process of corpus files.
	 * 
	 * @throws FileNotFoundException
	 */
	public CorpusBatchProcessor(int indexOfThisProcess, int totalNumProcesses) throws FileNotFoundException {
		this.indexOfThisProcess = indexOfThisProcess;
		this.totalNumProcesses = totalNumProcesses;

	}

	/**
	 * Run as a single process, go through fiels one by one, this could be a multi-thread process,
	 * taking care of deviding jobs itself..
	 * 
	 * @throws FileNotFoundException
	 */
	public CorpusBatchProcessor() throws FileNotFoundException {
		indexOfThisProcess = -1;
		this.totalNumProcesses = -1;
	}

	/**
	 * Grab content of a local GPG file.
	 * 
	 * @param date
	 * @param fileName
	 * @param fileStr
	 * @return
	 */
	private static InputStream grabGPGLocal(String fileStr) {
		// System.out.println(date + "/" + fileName);
		String command = "gpg -q --no-verbose --no-permission-warning --trust-model always --output - --decrypt " + fileStr;
		// + fileStr + " | xz --decompress";
		// System.out.println(command);
		return FileProcessor.runBinaryShellCommand(command);
	}

	/**
	 * Get Streams of a specific file name in a day-hour directory.
	 * 
	 * @param day
	 * @param hour
	 * @param fileName
	 * @param is
	 * @throws Exception
	 */
	private void getStreams(PrintWriter pw, String day, int hour, String fileName, InputStream is) throws Exception {
		XZCompressorInputStream bais = new XZCompressorInputStream(is);
		TIOStreamTransport transport = new TIOStreamTransport(bais);
		transport.open();
		TBinaryProtocol protocol = new TBinaryProtocol(transport);

		int index = 0;
		boolean exception = false;
		while (!exception) {
			try {
				StreamItem si = new StreamItem();
				if (protocol.getTransport().isOpen())
					si.read(protocol);
				siCount.incrementAndGet();
				// si.getBody().unsetRaw();

				// processTokens(si);
				// pipeline runs on the stream item

				// pipe.run(si);

				// System.out.println(day + "|" + hour+ "|" + fileName+ "|" + index);
				SIWrapper siw = new SIWrapper(day, hour, fileName, index, si);
				process(pw, siw);

				// si.clear();
				index = index + 1;
			} catch (TTransportException e) {
				RemoteGPGRetrieval.tTransportExceptionPrintString(e);
				exception = true;
			}
		}
		transport.close();
	}

	/**
	 * Process StreamItemWrapper by going through tokens and concatenating tehm to make sure we can
	 * handle multi word entities.
	 * 
	 * @param siw
	 */
	private void process(PrintWriter pw, SIWrapper siw) {

		List<String> listMatchedSenteces = new LinkedList<String>();

		boolean printedFileName = false;
		if (siw.getStreamItem().getBody() != null) {

			List<Sentence> listSentence = siw.getStreamItem().getBody().getSentences().get("lingpipe");

			if (listSentence == null) {
				System.out.println("lingpipe = Null: " + siw);

				String cleanVisible = siw.streamItem.getBody().getClean_visible();
				if (cleanVisible != null)
					matchToEntity("cleanVisible", cleanVisible, siw, pw);
				else {
					byte[] rawArr = siw.streamItem.getBody().getRaw();
					if (rawArr != null) {
						try {
							String raw = new String(rawArr, "UTF-8");
							raw = raw.replaceAll("<[^>]*>", " ");
							matchToEntity("raw", raw, siw, pw);
						} catch (UnsupportedEncodingException e) {
						}
					}
				}
			} else {
				// initiing all sentences.
				List<String> listStr = new LinkedList<String>();
				for (Sentence sentence : listSentence) {
					StringBuilder sentenceStr = new StringBuilder();
					for (Token t : sentence.getTokens()) {
						if (t.entity_type != null)
							sentenceStr.append(t.token.toLowerCase() + " ");
					}
					// / pipe.transform(sentence.tokens.toArray(new
					// Token[sentence.tokens.size()]));
					listStr.add(sentenceStr.toString());
				}

				// match all entities
				for (Entity entity : listEntity) {
					boolean matchedEntity = false;
					// match all aliases of entity
					for (int ientity = 0; ientity < entity.names().size() && !matchedEntity; ientity++) {
						// for all sentences
						for (int isentence = 0; isentence < listStr.size() && !matchedEntity; isentence++) {
							String s = listStr.get(isentence);

							String alias = entity.names().get(ientity);
							if (s.contains(alias)) {
								if (!printedFileName) {
									pw.print("ling>" + siw.day + " | " + siw.fileName + " | " + siw.getIndex() + " | "
											+ siw.getStreamItem().getDoc_id() + " || ");

									printedFileName = true;
								}

								// Match each entity only once for all sentences and all aliases. If found proceed
								// to the next entity.

								// matchedEntity = true; comented for statistics purposes to
								// count all senteces that matched.

								/**
								 * For each entity: how many documents: # rows <br>
								 * For each entity: how many sentence: # total # of entity id in the report <br>
								 * For each entity: how many sentence have slot values -> piepline For each entity:
								 * how many documents no lingpipe: we have it in matchToEntity, calculate same as
								 * above.
								 */

								pw.print(entity.topic_id() + ", ");
								siFilteredCount.incrementAndGet();

								listMatchedSenteces.add(s);
							}
						}
					}
				}
				// }
				// }
				if (printedFileName) {
					pw.println(); // final new line
					for (String s : listMatchedSenteces) {
						pw.println("Sentence>" + s);
					}
				}
			}
		}

		// .replaceAll("[^A-Za-z0-9\\p{Punct}]", " ")
		// .replaceAll("\\s+", " ").replaceAll("(\r\n)+",
		// "\r\n").replaceAll("(\n)+", "\n")
		// .replaceAll("(\r)+", "\r").toLowerCase();
		pw.flush();
	}

	private boolean matchToEntity(String logPrefix, String s, SIWrapper siw, PrintWriter pw) {
		boolean printedFileName = false;
		if (s != null) {
			for (Entity entity : listEntity) {
				// match all aliases of entity
				for (int ientity = 0; ientity < entity.names().size(); ientity++) {
					// if(cleanVisible != null && cleanVisible.contains(entity)){
					String alias = entity.names().get(ientity);
					if (s.contains(alias)) {
						if (!printedFileName) {
							pw.print(logPrefix + ">" + siw.day + " | " + siw.fileName + " | " + siw.index + " | "
									+ siw.streamItem.getDoc_id() + " || ");

							printedFileName = true;
						}
						pw.print(entity.topic_id() + ", ");
						siFilteredCount.incrementAndGet();
					}
				}
			}
		}
		return printedFileName;
	}

	private boolean isAlreadyProcessed(String fileName) {
		boolean contains = alreadyProcessedGPGFileHashTable.containsKey(fileName);
		return contains;
	}

	private boolean isToBeProcessed(String fileName) {
		boolean contains = toBeProcessedGPGFileHashTable.containsKey(fileName);
		return contains;
	}

	/**
	 * Process in multithreaded fashion. <br>
	 * process all tobe processed files one more time, because toProcess doesn't tell us which
	 * streamitem to look at.
	 * 
	 * @throws ParseException
	 */
	private void processMultiThreads() throws ParseException {
		System.out.println("processMultiThreads");

		final Calendar cStart = Calendar.getInstance();
		final Calendar cEnd = Calendar.getInstance();

		final int threadCount = (SETTINGS.localRun) ? 1 : 64;
		// final String CORPUS_DIRECTORY = (localRun) ? CORPUS_DIR_LOCAL :
		// CORPUS_DIR_SERVER;
		final String LOG_DIRECTORY = SETTINGS.LOG_DIR;
		if (SETTINGS.localRun) {
			System.out.println("Local run.");
			cStart.setTime(SETTINGS.format.parse("2011-10-05-00"));
			// cStart.setTime(format.parse("2011-10-07-13"));
			cEnd.setTime(SETTINGS.format.parse("2011-10-07-14"));
		} else {
			System.out.println("Server run.");
			cStart.setTime(SETTINGS.format.parse("2011-10-05-00"));
			// cStart.setTime(format.parse("2012-08-18-01"));
			cEnd.setTime(SETTINGS.format.parse("2013-02-13-23"));
		}

		final AtomicInteger finishedThreadTracker = new AtomicInteger(0);
		for (int i = 0; i < threadCount; i++) {
			final int threadIndex = i;
			final Calendar cTemp = Calendar.getInstance();
			cTemp.setTime(cStart.getTime());
			cTemp.add(Calendar.HOUR, threadIndex);

			Thread worker = new Thread() {// one thread per hour then add index
				public void run() {

					PrintWriter pw = null;
					try {
						pw = new PrintWriter(new File(LOG_DIRECTORY + "run" + threadIndex + "Log.txt"));

						// System.out.println(threadIndex + ": " +
						// format.format(cTemp.getTime()) + " "
						// + format.format(cEnd.getTime()));
						while (!(cTemp.getTime().compareTo(cEnd.getTime()) > 0)) {
							try {
								final String date = SETTINGS.format.format(cTemp.getTime());

								File testDirectorySDD = new File(SETTINGS.CORPUS_DIR_SERVER_SDD + date);
								File testDirectorySDE = new File(SETTINGS.CORPUS_DIR_SERVER_SDE + date);

								List<String> tempFileList = null;
								if (testDirectorySDD.exists())
									tempFileList = DirList.getFileList(SETTINGS.CORPUS_DIR_SERVER_SDD + date, SETTINGS.FILTER);
								else if (testDirectorySDE.exists())
									tempFileList = DirList.getFileList(SETTINGS.CORPUS_DIR_SERVER_SDE + date, SETTINGS.FILTER);
								else
									tempFileList = DirList.getFileList(SETTINGS.CORPUS_DIR_LOCAL + date, SETTINGS.FILTER);

								if (tempFileList == null)
									throw new RuntimeException("Corpus not found");

								final List<String> fileList = tempFileList;

								// final List<String> fileList =
								// DirList.getFileList(CORPUS_DIRECTORY + date, FILTER);
								for (final String fileStr : fileList) {
									final int hour = cTemp.get(Calendar.HOUR_OF_DAY);
									final String fileName = fileStr.substring(fileStr.lastIndexOf('/') + 1);
									String dateFile = date + "/" + fileName;

									// if (isAlreadyProcessed(dateFile)) {

									// System.out.println("@ " + dateFile);
									// Object o =
									// alreadyProcessedGPGFileHashTable.remove(dateFile);
									// if(o == null)
									// throw new Exception("Exception");
									// } else if (isToBeProcessed(fileStr)) {
									System.out.println("# " + fileStr);
									try {
										InputStream is = grabGPGLocal(fileStr);
										getStreams(pw, date, hour, fileName, is);
										is.close();

										fileCount.incrementAndGet();
										long size = FileProcessor.getLocalFileSize(fileStr);
										processedSize.addAndGet(size);
										// report(logTimeFormat, "Thread(" + threadIndex + ")" +
										// date
										// + "/" + fileName);
										pw.println(SETTINGS.logTimeFormat.format(new Date()) + " Total " + fileCount + " Files "
												+ FileProcessor.fileSizeToStr(processedSize.get(), "MB") + " SIs: " + siCount.get() + " +SIs: "
												+ siFilteredCount + " " + "Thread(" + threadIndex + ")" + date + "/" + fileName);
										pw.flush();
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
								// }
							} catch (Exception e1) {
								e1.printStackTrace();
							}

							cTemp.add(Calendar.HOUR, threadCount);
							// System.out.println(threadIndex + ": " +
							// format.format(cTemp.getTime()) + " "
							// + format.format(cEnd.getTime()));
						}

					} catch (FileNotFoundException e2) {
						e2.printStackTrace();
					}

					finishedThreadTracker.incrementAndGet();
					if (pw != null)
						pw.close();
				}
			};
			worker.start();
		}
		while (finishedThreadTracker.get() < threadCount) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		report(SETTINGS.logTimeFormat, "Finished all threads");
	}

	/**
	 * Generate a timely statistics of the # of fiels, total file size processed so far. # of
	 * StreamItems, # of Stream Items that contained an entity, current file name etc.
	 * 
	 * @param df
	 * @param message
	 */
	private void report(DateFormat df, String message) {
		System.out.println(df.format(new Date()) + " Total " + fileCount + " Files "
				+ FileProcessor.fileSizeToStr(processedSize.get(), "MB") + " SIs: " + siCount.get() + " +SIs: "
				+ siFilteredCount + " " + message);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		Preprocessor.initEntityList("resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json");

		if (args.length == 0) {
			CorpusBatchProcessor cps = new CorpusBatchProcessor();
			cps.listEntity = Preprocessor.entity_list();
			// cps.process();
			cps.processMultiThreads();
		} else if (args.length == 2) {
			// CorpusBatchProcessor cps = new
			// CorpusBatchProcessor(Integer.parseInt(args[0]),
			// Integer.parseInt(args[0]));
			// cps.listEntity = Preprocessor.entity_list();
			// cps.process();
		} else {
			System.err
					.println("Usage: CorpusBatchProcessor indexOfThisProcess totalNumProcesses   OR just   CorpusBatchProcessor");
			System.err
					.println("Where totalNumProcesses is an integer  the total # of processes of CorpusBatchProcessor that run on corpus."
							+ " indexOfThisProcess is an integer that identifies the index of this process in the total # of CorpusBatchProcessor that are running on the corpus at the same time to avoid duplicate process of corpus sections.");
		}

		// String s = new String();
		// s.intern();
	}
}
