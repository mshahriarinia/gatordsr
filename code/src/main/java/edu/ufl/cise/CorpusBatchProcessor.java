package edu.ufl.cise;

import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransportException;

import streamcorpus.Sentence;
import streamcorpus.StreamItem;
import streamcorpus.Token;
import edu.ufl.cise.pipeline.Entity;
import edu.ufl.cise.pipeline.Pipeline;
import edu.ufl.cise.pipeline.Preprocessor;

/**
 * check http://sourceforge.net/projects/faststringutil/ structured graph
 * learning sgml icml, online lda, stremaing
 * 
 * Some issues: <br>
 * 
 * memory usage is ok, not much io<br>
 * Time: <br>
 * decrypt nonblocking<br>
 * split streamitem body and add it via softrefernce. in O(1) do comparison via
 * cache. ORRRR use the tokens they already provide
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

	public final static String			CORPUS_DIR_SERVER	= "/media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";
	public final static String			CORPUS_DIR_LOCAL	= "/home/morteza/2013Corpus/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";
	final String										FILTER						= "";
	final String										query							= "president";
	AtomicLong											fileCount					= new AtomicLong(0);
	AtomicLong											siCount						= new AtomicLong(0);
	AtomicLong											siFilteredCount		= new AtomicLong(0);
	AtomicLong											processedSize			= new AtomicLong(0);

	public static final DateFormat	format						= new SimpleDateFormat("yyyy-MM-dd-HH");
	public static final DateFormat	logTimeFormat			= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	final int												indexOfThisProcess;
	final int												totalNumProcesses;

	Pipeline												pipe							= Pipeline.getPipeline(Pipeline.patterns(),
																												Pipeline.queries(), Pipeline.dirs());
	final Pattern										pattern						= Pattern.compile(query);

	/**
	 * gets the index of thus process and total # of processes that this process
	 * is a member of to avoid duplicate process of corpus files.
	 * 
	 */
	public CorpusBatchProcessor(int indexOfThisProcess, int totalNumProcesses) {
		this.indexOfThisProcess = indexOfThisProcess;
		this.totalNumProcesses = totalNumProcesses;
	}

	public CorpusBatchProcessor() {
		indexOfThisProcess = -1;
		this.totalNumProcesses = -1;
	}

	private static InputStream grabGPGLocal(String date, String fileName, String fileStr) {
		// System.out.println(date + "/" + fileName);
		String command = "gpg -q --no-verbose --no-permission-warning --trust-model always --output - --decrypt "
				+ fileStr;
		// + fileStr + " | xz --decompress";
		return FileProcessor.runBinaryShellCommand(command);
	}

	private void getStreams(String day, int hour, String fileName, InputStream is) throws Exception {
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

				SIWrapper siw = new SIWrapper(day, hour, fileName, index, si);
				process(siw);

				// si.clear();
				index = index + 1;
			} catch (TTransportException e) {
				tTransportExceptionPrintString(e);
				exception = true;
			}
		}
		transport.close();
	}

	private static void tTransportExceptionPrintString(TTransportException e) {
		switch (e.getType()) {
		case TTransportException.ALREADY_OPEN:
			System.err.println("Error reading StreamItem: ALREADY_OPEN");
			break;
		case TTransportException.END_OF_FILE:
			// System.err.println("Error reading StreamItem: END_OF_FILE");
			break;
		case TTransportException.NOT_OPEN:
			System.err.println("Error reading StreamItem: NOT_OPEN");
			break;
		case TTransportException.TIMED_OUT:
			System.err.println("Error reading StreamItem: TIMED_OUT");
			break;
		case TTransportException.UNKNOWN:
			System.err.println("Error reading StreamItem: UNKNOWN");
			break;
		}
	}

	DecimalFormat	numberFormatter	= new DecimalFormat("00");

	List<Entity>	listEntity;

	private void process(SIWrapper siw) {
		boolean printedFileName = false;
		if (siw.getStreamItem().getBody() != null) {

			List<Sentence> listSentence = siw.getStreamItem().getBody().getSentences().get("lingpipe");

			// match all entities
			for (Entity entity : listEntity) {
				boolean matchedEntity = false;
				// match all aliases of entity
				for (int ientity = 0; ientity < entity.names().size(); ientity++) {
					// for all sentences
					for (int isentence = 0; isentence < listSentence.size() && !matchedEntity; isentence++) {
						Sentence s = listSentence.get(isentence);
						// pipe.transform(s.tokens.toArray(Token.class));
						// for all tokens
						for (int itokens = 0; itokens < s.tokens.size() && !matchedEntity; itokens++) {
							String token = s.tokens.get(itokens).getToken();
							if (token != null) {
								token = token.toLowerCase();

								String alias = entity.names().get(ientity);
								if (token.equals(alias)) {
									if (!printedFileName) {
										System.out.print(">" + siw.day + " | " + siw.fileName + " | " + siw.getIndex()
												+ " | " + siw.getStreamItem().getDoc_id() + " | ");
										printedFileName = true;
										matchedEntity = true;
										
									}

									System.out.print(entity.topic_id() + ", ");
									siFilteredCount.incrementAndGet();
								}
							}
						}
					}
				}
			}
			if (printedFileName)
				System.out.println(); // final new line
		}

		// .replaceAll("[^A-Za-z0-9\\p{Punct}]", " ")
		// .replaceAll("\\s+", " ").replaceAll("(\r\n)+",
		// "\r\n").replaceAll("(\n)+", "\n")
		// .replaceAll("(\r)+", "\r").toLowerCase();
	}

	private void process(StreamItem si) {
		Map<String, List<Sentence>> sentencesMap = si.getBody().getSentences();
		Set<String> sentenceSetKeys = sentencesMap.keySet();
		for (Iterator iterator = sentenceSetKeys.iterator(); iterator.hasNext();) {
			String sentenceKey = (String) iterator.next();

			List<Sentence> listSentence = sentencesMap.get(sentenceKey);
			for (Sentence s : listSentence) {
				List<Token> listToken = s.tokens;
				for (Token t : listToken) {
					// System.out.println(si.getBody().getClean_visible());
					// if (si.getBody().getClean_visible().toLowerCase().contains(query))
					// System.out.println(t.getLemma());
					if (t.getToken() != null) {

						// t.setLemma(t.getLemma().toLowerCase());
						if (t.getToken().toLowerCase().contains(query)) {
							// siFilteredCount.incrementAndGet();
							return;
						}
					}
				}
			}
		}
	}

	/**
	 * 
	 * @throws ParseException
	 */
	private void process() throws ParseException {

		int threadCount;

		Calendar c = Calendar.getInstance();
		Calendar cEnd = Calendar.getInstance();

		File f = new File(CORPUS_DIR_LOCAL);
		boolean localRun = f.exists();
		final String DIRECTORY = (localRun) ? CORPUS_DIR_LOCAL : CORPUS_DIR_SERVER;
		if (localRun) {
			System.out.println("Local run.");
			// c.setTime(format.parse("2011-10-05-00"));
			c.setTime(format.parse("2011-10-07-13"));
			cEnd.setTime(format.parse("2011-10-07-14"));
			threadCount = 2;
		} else {
			System.out.println("Server run.");
			// c.setTime(format.parse("2011-10-05-00"));
			c.setTime(format.parse("2012-08-18-01"));
			cEnd.setTime(format.parse("2012-08-18-01"));
			threadCount = 32;
		}

		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		while (!(c.getTime().compareTo(cEnd.getTime()) > 0)) {
			try {
				final String date = format.format(c.getTime());

				final List<String> fileList = DirList.getFileList(DIRECTORY + date, FILTER);
				for (final String fileStr : fileList) {
					final int hour = c.get(Calendar.HOUR_OF_DAY);
					final String fileName = fileStr.substring(fileStr.lastIndexOf('/') + 1);

					//
					// Runnable worker = new Thread(fileCount + " " + date + "/" +
					// fileName) {
					// public void run() {
					//

					try {
						InputStream is = grabGPGLocal(date, fileName, fileStr);
						getStreams(date, hour, fileName, is);
						is.close();

						fileCount.incrementAndGet();
						long size = FileProcessor.getLocalFileSize(fileStr);
						processedSize.addAndGet(size);
						report(logTimeFormat, date + "/" + fileName);
					} catch (Exception e) {
						e.printStackTrace();
					}

					//
					//
					// };
					// };
					// executor.execute(worker);
					//
					//

				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			if (totalNumProcesses == -1)
				c.add(Calendar.HOUR, 1);
			else
				c.add(Calendar.HOUR, totalNumProcesses);
		}

		//
		executor.shutdown();
		while (!executor.isTerminated()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//

		report(logTimeFormat, "Finished all threads");
	}

	private void report(DateFormat df, String message) {
		System.out.println(df.format(new Date()) + " Total " + fileCount + " Files "
				+ FileProcessor.fileSizeToStr(processedSize.get(), "MB") + " SIs: " + siCount.get()
				+ " +SIs: " + siFilteredCount + " " + message);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws ParseException {
		Preprocessor
				.initEntityList("resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json");

		if (args.length == 0) {
			CorpusBatchProcessor cps = new CorpusBatchProcessor();
			cps.listEntity = Preprocessor.entity_list();
			cps.process();
		} else if (args.length == 2) {
			CorpusBatchProcessor cps = new CorpusBatchProcessor(Integer.parseInt(args[0]),
					Integer.parseInt(args[0]));
			cps.listEntity = Preprocessor.entity_list();
			cps.process();
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
