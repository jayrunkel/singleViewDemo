# Spark 1.6
# ./bin/sparkR --conf "spark.mongodb.input.uri=mongodb://admin:admin@prometheus-0.genband-poc.4183.mongodbdns.com/counties.paCensus?authSource=admin" --conf "spark.mongodb.output.uri=mongodb://admin:admin@prometheus-0.genband-poc.4183.mongodbdns.com/counties.testR?authSource=admin" --packages org.mongodb.spark:mongo-spark-connector_2.10:0.1

# Spark 2.0
# ./bin/sparkR --conf "spark.mongodb.input.uri=mongodb://admin:admin@prometheus-0.genband-poc.4183.mongodbdns.com/counties.crimeCountyAlcoholCensus?authSource=admin" --conf "spark.mongodb.output.uri=mongodb://admin:admin@prometheus-0.genband-poc.4183.mongodbdns.com/counties.testR?authSource=admin" --packages org.mongodb.spark:mongo-spark-connector_2.11:2.0.0

# ./bin/sparkR --conf "spark.mongodb.input.uri=mongodb://admin:admin@localhost/counties.countyCrimeAlcoholCensus?authSource=admin" --conf "spark.mongodb.output.uri=mongodb://admin:admin@localhost/counties.testR?authSource=admin" --packages org.mongodb.spark:mongo-spark-connector_2.11:2.0.0



# sc <- sparkR.init(appName="test")
# sqlContext <- sparkRSQL.init(sc)

# Install statistical packages. Only needs to be done once.
# install.packages("Hmisc") //takes a long time. downloads and compiles a lot of stuff

# Read data from MongoDB in Spark Dataframe
# censusDF <- read.df(spark,
#                     source = "com.mongodb.spark.sql.DefaultSource",
# 		    uri = "mongodb://admin:admin@prometheus-0.genband-poc.4183.mongodbdns.com/counties.paCensus?authSource=admin")

censusDF <- loadDF("",
                    source = "com.mongodb.spark.sql.DefaultSource",
		    uri = "mongodb://admin:admin@prometheus-0.genband-poc.4183.mongodbdns.com/counties.countySingleView?authSource=admin")

# censusDF <- read.df(spark,
                    source = "com.mongodb.spark.sql.DefaultSource",
		    uri = "mongodb://admin:admin@localhost/counties.paCensus?authSource=admin")


# spark dataframe to R dataframe
sparkDF <- collect(censusDF)

row.names(sparkDF) <- sparkDF[["county"]]
stringCols <- c("county", "_id")
corReady <- sparkDF[ , !(names(sparkDF) %in% stringCols)]

corMatrix <- cor(corReady)
