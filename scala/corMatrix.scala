// Starting Spark
// ./bin/spark-shell --conf "spark.mongodb.input.uri=mongodb://admin:admin@prometheus-0.genband-poc.4183.mongodbdns.com/counties.paCensus?authSource=admin" --conf "spark.mongodb.output.uri=mongodb://admin:admin@prometheus-0.genband-poc.4183.mongodbdns.com/counties.test?authSource=admin" --packages org.mongodb.spark:mongo-spark-connector_2.10:0.1

// ./bin/spark-shell --conf "spark.mongodb.input.uri=mongodb://admin:admin@prometheus-0.genband-poc.4183.mongodbdns.com/counties.paCensus?authSource=admin" --conf "spark.mongodb.output.uri=mongodb://admin:admin@prometheus-0.genband-poc.4183.mongodbdns.com/counties.testR?authSource=admin" --packages org.mongodb.spark:mongo-spark-connector_2.11:2.0.0

// ./bin/spark-shell --conf "spark.mongodb.input.uri=mongodb://admin:admin@localhost/counties.paCensus?authSource=admin" --conf "spark.mongodb.output.uri=mongodb://admin:admin@localhost/counties.testR?authSource=admin" --packages org.mongodb.spark:mongo-spark-connector_2.11:2.0.0

// ./bin/spark-shell --conf "spark.mongodb.input.uri=mongodb://localhost/counties.paCensus" --conf "spark.mongodb.output.uri=mongodb://localhost/counties.testR" --packages org.mongodb.spark:mongo-spark-connector_2.11:2.0.0

println("[Demo] Loading libraries...")

import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.rdd.RDD

import org.bson.Document
import com.mongodb.spark._
import com.mongodb.spark.sql._
import com.mongodb.spark.config._

println("[Demo] Starting processing...")

/*
val table:String = "paCensus"
val fields:Array[String] = Array("male", "female", "medianAgeTotal", "1-personhhld")

val table:String = "paCrime"

val fields:Array[String] = Array("Months", "Population", "Violent crime total", "Murder and nonnegligent Manslaughter", "Forcible rape", "Robbery", "Aggravated assault", "Property crime total", "Burglary", "Larceny-theft", "Motor vehicle theft", "Violent Crime rate", "Murder and nonnegligent manslaughter rate", "Forcible rape rate", "Robbery rate", "Aggravated assault rate", "Property crime rate", "Burglary rate", "Larceny-theft rate", "Motor vehicle theft rate")


val sqlSelectStr = "SELECT " +  fields.map(col => s"`$col`").mkString(", ") + " FROM " + table
*/

println("[Demo] Reading data from MongoDB...")

val censusDFAll = spark.loadFromMongoDB()
val censusDF = censusDFAll.drop(censusDFAll.col("_id"))
//val censusDF = sqlContext.read.mongo()

//censusDF.registerTempTable(table)

//sqlContext.sql("SELECT county, `1-personhhld`, `2-personhhld`, `3-personhhld` FROM paCensus").show()

//val data = sqlContext.sql(sqlSelectStr)

println("[Demo] Building correlation matrix...")
//val dataRDD = data.rdd
val dataRDD = censusDF.rdd

val dataVectors = dataRDD.map(row => {
  Vectors.dense(row.toSeq.toArray.map({
    case s: String => 0.0    //s.toDouble
    case d: Double => d
    case l: Long => l.toDouble
    case i: Int => i.toDouble
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

val header = censusDF.columns.filter(! _.contains("_id"))
//Document.parse(s"{${header(0)} : ${first(0)}, ${header(1)}: ${first(1)}, ${header(2)} : ${first(2)}}")

println("[Demo] Transforming correlation matrix to documents...")
//zip doesn't work do to unequal partitions
//val headers = sc.parallelize((0 to res.count().toInt).map{nn => header})
val merged = res.map{row => (row, header)}
//val merged = res.zip(headers)

val resPlusHeader = merged.map{case (a, b) => b.zip(a.toArray)}

/*
def mapToDocument(str : String) : Document = {
   new Document(str)
}
*/

val theFinal = resPlusHeader.map{arr => Document.parse(arr.map{case(a, b) => s"'$a': $b"}.mkString("{", ", ", "}"))}

val outDocs = theFinal.zipWithIndex().map{case (doc, i) => {
    doc.put("field", header(i.toInt))
    doc
    }
}

println("[Demo] Saving results to MongoDB...")
outDocs.saveToMongoDB()







