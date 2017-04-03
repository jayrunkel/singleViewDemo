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


println("[Demo] Reading data from MongoDB...")

val censusDFAll = spark.loadFromMongoDB()
val censusDF = censusDFAll.drop(censusDFAll.col("_id"))

println("[Demo] Building correlation matrix...")
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
val header = censusDF.columns.filter(! _.contains("_id"))

println("[Demo] Transforming correlation matrix to documents...")
val merged = res.map{row => (row, header)}

val resPlusHeader = merged.map{case (a, b) => b.zip(a.toArray)}

val theFinal = resPlusHeader.map{arr => Document.parse(arr.map{case(a, b) => s"'$a': $b"}.mkString("{", ", ", "}"))}

val outDocs = theFinal.zipWithIndex().map{case (doc, i) => {
    doc.put("field", header(i.toInt))
    doc
    }
}

println("[Demo] Saving results to MongoDB...")
outDocs.saveToMongoDB()







