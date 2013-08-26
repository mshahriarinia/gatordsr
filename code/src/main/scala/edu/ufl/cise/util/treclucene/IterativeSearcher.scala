package edu.ufl.cise.util.treclucene

import java.util.Scanner
import java.io.File
import scala.collection.JavaConversions._
import java.util.ArrayList
import edu.cise.ufl.util.treclucene.Searcher
import edu.ufl.cise.pipeline.Pipeline
import edu.ufl.cise.pipeline.Preprocessor
import edu.ufl.cise.pipeline.Entity
import org.apache.lucene.store.MMapDirectory
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.search.TopScoreDocCollector
import scala.util.Random
import java.io.PrintWriter
import java.util.regex.Pattern
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.standard.StandardAnalyzer


/**
 * Iterative search among all lucene index files. One index directory per corpus date-hour directory
 */
object IterativeSearcher {

    val analyzer = new StandardAnalyzer(Version.LUCENE_43);

   val SEARCH_INDEX_TYPE = "gpgfile"
   val queryParser = new QueryParser(Version.LUCENE_43, SEARCH_INDEX_TYPE, analyzer)


  def main(args: Array[String]): Unit = {

    val entity_list = new ArrayList[Entity]
    Preprocessor.initEntityList("resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08-wiki-alias.json", entity_list)
    lazy val entities = entity_list.toArray(Array[Entity]())
    
val FULL_PATH_GPG_REGEX_STR = ".*?(\\d{4}-\\d{2}-\\d{2}-\\d{2}).*/(.*)";
  val FULL_PATH_GPG_REGEX = Pattern.compile(FULL_PATH_GPG_REGEX_STR);

    val sc = new Scanner(new File("/media/sde/devPipeline/gatordsr/code/allLuceneIndexDirectories.txt"));

    val luceneIndexesList = new ArrayList[String]
    while (sc.hasNextLine()) {
      luceneIndexesList.add(sc.nextLine());
    }

    luceneIndexesList.toList.par.map(f => {

      val filedir = new java.io.File(f)
      val index = new MMapDirectory(filedir)

      val date = f.substring(f.lastIndexOf('/') + 1)
      val pw = new PrintWriter("/media/sde/luceneSubmission/splittedEntityIndex/oneIndexPerDateHourDir/results-" + date)

      pw.println(f)//actual index path
      entity_list.foreach(e => {
        val querystr = Searcher.aliasListToLuceneQuery(e.alias)
        val q = queryParser.parse(querystr)

        val hitsPerPage = 1000000;
        val reader = DirectoryReader.open(index);
        val searcher = new IndexSearcher(reader);
        val collector = TopScoreDocCollector.create(hitsPerPage, true);
        searcher.search(q, collector);

        val docs = collector.topDocs()
        val hits = docs.scoreDocs;

        pw.println(hits.length + "\t hits for: " + querystr);

        docs.scoreDocs foreach { docId =>
          val d = searcher.doc(docId.doc)
         val gpgFile = d.get("gpgfile") 

          pw.flush()
          val m = FULL_PATH_GPG_REGEX.matcher(gpgFile);
          m.find()
          val s1 = m.group(1);
          val s2 = m.group(2);

          pw.println("ling>" + s1 + " | " + s2 + " | " + d.get("si_index") + " | " +
            //d.get("si_docid")
            //d.get("clean_visible")+
            "aab5ec27f5515cb8a0cec62d31b8654e" + " || " + e.target_id);
        }
      })
      pw.close()
      
      ""
    })
  }
}
