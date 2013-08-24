package edu.cise.ufl.util.treclucene

import java.util.ArrayList
import java.util.regex.Pattern

import scala.Array.canBuildFrom

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.CollectionStatistics
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TopScoreDocCollector
import org.apache.lucene.store.MMapDirectory
import org.apache.lucene.store.NIOFSDirectory
import org.apache.lucene.util.Version
import org.apache.lucene.search.PhraseQuery


import scala.collection.JavaConversions._

import edu.ufl.cise.Logging

object Searcher extends Logging {

  //var filedir = new java.io.File("/var/tmp/lucene")
  //var filedir = new java.io.File("/media/sdc/kbaindex/media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/2012-04-17-15")
  var filedir = new java.io.File("/media/sdc/optimizedindex/") //index of corpus
  //    var filedir = new java.io.File("/media/sdc/optimizedindex/") //index of wikipedia

  val directory = new NIOFSDirectory(filedir)

  def getStats(searcher: IndexSearcher): Unit = {
    val stats: CollectionStatistics = searcher.collectionStatistics("clean_visible")

    logInfo("field: %s".format(stats.field))
    logInfo("docCount: %d".format(stats.docCount))
    logInfo("maxDoc: %d".format(stats.maxDoc))
    logInfo("sumDocFreq: %d".format(stats.sumDocFreq))
    logInfo("sumTotalTermFreq: %d".format(stats.sumTotalTermFreq))
    logInfo("-" * 40)

  }

  def printAllDocs(searcher: IndexSearcher): Unit = {
    var i = 0;
    while (i <= searcher.getIndexReader.numDocs) {
      logInfo("doc(%d): %s".format(i, searcher.doc(i).toString))
      i += 1
    }

  }

  def main(args: Array[String]) {

        if (args.length < 1) {
         println("Usage: run 'My query'")
          System.exit(1)
        }

    val concatedArgs = args.map(s => {
      if (s == ",")
        "OR"
      else if (s != "AND" && s != "OR")
        "\"" + s + "\""
      else
        s
    }).reduce((s1, s2) => s1 + " " + s2)

    //      val allArgs = args.mkString(" ")
    //    logInfo("allArgs"+allArgs)

    searchTermQuery(args);

    println("All arguments: " + concatedArgs)
    searchQueryParser("", concatedArgs)
  }

  def searchTermQuery(args: Array[String]) {
    val reader = DirectoryReader.open(directory)
    val searcher = new IndexSearcher(reader)
    val query = new TermQuery(new Term("clean_visible", args(0).toLowerCase))

//    getStats(searcher)
    //printAllDocs(searcher)

    var docs = searcher.search(query, 2000)
    println("TermQuery found: " + docs.scoreDocs.length)


 val q = new PhraseQuery()
    q.add(new Term("clean_visible", args(0).toLowerCase))
    docs = searcher.search(q, 2000)
    println("PhraseQuery found: " + docs.scoreDocs.length)

    //    docs.scoreDocs foreach { docId =>
    //      val d = searcher.doc(docId.doc)
    //      logInfo("Result: %s".format(d.get("si_index")))
    //      logInfo("Result: %s".format(d.get("gpgfile")))
    //      logInfo("Result: %s".format(d.get("clean_visible")))
    //    }

    //searcher.close
    reader.close
  }

  def searchEntity(logNote: String, aliasList: ArrayList[String]) {
    val aliases = aliasList.toList.distinct

    val concatedArgs = aliases.map(s => {
      if (s == ",")
        "OR"
      else if (s != "AND" && s != "OR")
        "\"" + s + "\""
      else
        s
    }).reduce((s1, s2) => s1 + " OR " + s2)

    searchQueryParser(logNote, concatedArgs.toLowerCase().replace(" or ", " OR "))
  }
  
   val analyzer = new StandardAnalyzer(Version.LUCENE_43);

    // 1. create the index
    // val index = new RAMDirectory();
    val index = new MMapDirectory(filedir);

  def searchQueryParser(logNote: String, querystr: String) {
    //		System.out.println("\nSearching for '" + searchString + "' using QueryParser");
    //		//Directory directory = FSDirectory.getDirectory(INDEX_DIRECTORY);
    //		val indexSearcher = new IndexSearcher(directory);
    //
    //		val queryParser = new QueryParser(FIELD_CONTENTS, new StandardAnalyzer());
    //		Query query = queryParser.parse(searchString);
    //		System.out.println("Type of query: " + query.getClass().getSimpleName());
    //		Hits hits = indexSearcher.search(query);
    //		displayHits(hits);

   

    // the "title" arg specifies the default field to use
    // when no field is explicitly specified in the query.
    val q = new QueryParser(Version.LUCENE_43, "clean_visible", analyzer).parse(querystr);
              

    // 3. search
    val hitsPerPage = 1000000;
    val reader = DirectoryReader.open(index);
    val searcher = new IndexSearcher(reader);
    val collector = TopScoreDocCollector.create(hitsPerPage, true);
    searcher.search(q, collector);
    val hits = collector.topDocs().scoreDocs;

    // 4. display results
      println(hits.length + "\t hits for: " + querystr);

    hits.foreach(f => {
      val docId = f.doc;
      val d = searcher.doc(docId);
      val gpgFile = d.get("gpgfile")

      val m = p.matcher(gpgFile);
      m.find()
      val s1 = m.group(1);
      val s2 = m.group(2);

      println("ling>" + s1 + " | " + s2 + " | " + d.get("si_index") + " | " +
        //d.get("si_docid")
//        d.get("clean_visible")+
        "aab5ec27f5515cb8a0cec62d31b8654e" + " || " + logNote);
    })

    // reader can only be closed when there
    // is no need to access the documents any more.
    // reader.close();

  }

  val ps = ".*?(\\d{4}-\\d{2}-\\d{2}-\\d{2}).*/(.*)";
  val p = Pattern.compile(ps);

}
