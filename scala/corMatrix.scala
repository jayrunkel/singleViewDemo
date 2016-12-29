// Starting Spark
// ./bin/spark-shell --conf "spark.mongodb.input.uri=mongodb://admin:admin@prometheus-0.genband-poc.4183.mongodbdns.com/counties.paCensus?authSource=admin" --conf "spark.mongodb.output.uri=mongodb://admin:admin@prometheus-0.genband-poc.4183.mongodbdns.com/counties.test?authSource=admin" --packages org.mongodb.spark:mongo-spark-connector_2.10:0.1

import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.rdd.RDD

//import org.bson.Document
import com.mongodb.spark._
import com.mongodb.spark.sql._
import com.mongodb.spark.config._

Val censusDF = sqlContext.read.mongo()

censusDF.registerTempTable("paCensus")

#sqlContext.sql("SELECT county, `1-personhhld`, `2-personhhld`, `3-personhhld` FROM paCensus").show()

val data = sqlContext.sql("SELECT `1-personhhld`, `2-personhhld`, `3-personhhld` FROM paCensus")
val dataRDD = data.rdd

val dataVectors = dataRDD.map(row => {
  Vectors.dense(row.toSeq.toArray.map({
    case s: String => s.toDouble
    case l: Long => l.toDouble
    case _ => 0.0
  }))
})

val correlMatrix: Matrix = Statistics.corr(dataVectors, "pearson")

def toRDD(m: Matrix): RDD[Vector] = {
  val columns = m.toArray.grouped(m.numRows)
  val rows = columns.toSeq
  val vectors = rows.map(row => new DenseVector(row.toArray))
  sc.parallelize(vectors)
}

val res = toRDD(correlMatrix)
//val pRes = sc.parallelize(res.toArray())  //deprecated?

import org.bson.Document
val header = Array("One", "Two", "Three")
Document.parse(s"{${header(0)} : ${first(0)}, ${header(1)}: ${first(1)}, ${header(2)} : ${first(2)}}")


//zip doesn't work do to unequal partitions
//val headers = sc.parallelize((0 to res.count().toInt).map{nn => header})
val merged = res.map{row => (row, header)}
//val merged = res.zip(headers)

val resPlusHeader = merged.map{case (a, b) => b.zip(a.toArray)}

def mapToDocument(str : String) : Document = {
   new Document(str)
}

val theFinal = resPlusHeader.map{arr => Document.parse(arr.map{case(a, b) => s"$a: $b"}.mkString("{", ", ", "}"))}

val outDocs = theFinal.zipWithIndex().map{case (doc, i) => {
    doc.put("field", header(i.toInt))
    doc
    }
}

outDocs.saveToMongoDB()







