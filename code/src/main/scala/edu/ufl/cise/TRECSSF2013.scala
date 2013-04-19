package edu.ufl.cise

object TRECSSF2013 extends Logging {

  def main(args: Array[String]): Unit = {

    Pipeline.init()

    val text = "Abraham Lincoln was the 16th President of the United States, serving from March 1861 until his assassination in April 1865."
    val query = new SSFQuery("Abraham Lincoln", "president of")
    val pipeline = Pipeline.getPipeline(text, query)
    pipeline.run(text)

//    new SSFQuery("Richard Radcliffe", "topped")
    //    logInfo("SSFQuery : %s".format(query))

    val z = StreamFaucet.getStreams("2011-10-08", 0, 23)
      //.take(100)
      .map(si => new String(si.body.cleansed.array, "UTF-8"))
      //.map(_.take(964))
      .map(pipeline.run(_).toArray())
      .flatMap(x => x)
      .filter(p =>
        p.asInstanceOf[Triple].entity0.toLowerCase.equalsIgnoreCase(query.entity.toLowerCase) //||
        //query.entity.toLowerCase.contains(p.entity0.toLowerCase()) ||
        //query.slotName.toLowerCase.contains(p.slot.toLowerCase()) ||
        //p.slot.toLowerCase.equalsIgnoreCase(query.slotName.toLowerCase)
        )
      .foreach(t => logInfo("Answer: %s".format(t.toString)))

  }

}