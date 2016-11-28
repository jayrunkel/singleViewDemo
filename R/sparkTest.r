schema <- structType(
            structField("county", "string"),
            structField("totalPop", "integer"),
            structField("male", "integer"),
	    structField("female", "integer"),
	    structField("medianAgeTotal", "double"),
	    structField("medianAgeMale", "double"),
	    structField("medianAgeFemale", "double"),
	    structField("age0-2", "integer"),
	    structField("age3-4", "integer"),
	    structField("age5-18", "integer"),
	    structField("age60-61", "integer"),
	    structField("age62-64", "integer"),
	    structField("age65plus", "integer"),
	    structField("totalHousingUnits", "integer"),
	    structField("occupiedHousingTotal", "integer"),
	    structField("percentOwnedLoan", "double"),
	    structField("percentOwnedNoLoan", "double"),
	    structField("percentRenter", "double"),
	    structField("vacantHousingTotal", "integer"),
	    structField("percentVacant", "double"),
	    structField("percentSeasonal", "double"),
	    structField("percentVacantNotSeasonal", "double"),
	    structField("householdsTotal", "integer"),
	    structField("1-personhhld", "integer"),
	    structField("2-personhhld", "integer"),
	    structField("3-personhhld", "integer"),
	    structField("4-personhhld", "integer"),
	    structField("5-personhhld", "integer"),
	    structField("6-personhhld", "integer"),
	    structField("7-personPlus", "integer")
)

//countiesDF <- read.df(sqlContext, source="com.mongodb.spark.sql.DefaultSource")
censusDF <- read.df(sqlContext,
                    source = "com.mongodb.spark.sql.DefaultSource",
		    uri = "mongodb://admin:admin@prometheus-0.genband-poc.4183.mongodbdns.com/counties.census?authSource=admin",
		    schema = schema)
printSchema(censusDF)

registerTempTable(censusDF, "census")
list <- sql(sqlContext, "SELECT * FROM census WHERE county = \"Montgomery County\"")

