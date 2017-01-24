db.paCrime.aggregate(

	// Pipeline
	[
		// Stage 1
		{
			$addFields: {
			    "county" : {"$cond" : {"if" : {"$ne" : [{ "$indexOfCP" : [ "$agency", "("] }, -1]},
			                           "then" : {"$arrayElemAt" : [{"$split" : [{"$arrayElemAt" : [{ "$split" : [ "$agency", "(" ] }, 1]}, " "]}, 0]},
			                           "else" : null}},
			    "township" : {"$substrCP" : [{"$reduce" : {"input" : {"$filter" : {"input" : {"$slice" : [{"$split" : ["$agency", " "]}, 2]},
			                                                                      "as" : "word",
			                                                                      "cond" : {$not : {"$in" : ["$$word" , ["Twp", "City", "Boro", "Town", "Regional", "Township", "County", "Municipality", "Police"]]}}}},
			                                              "initialValue" : "",
			                                              "in" : {"$concat" : ["$$value", " ", "$$this"]}}},
			                                  1, 99]}
			}
		},

		// Stage 2
		{
			$lookup: {
			   "from" : "tspToCounty", "localField" : "township", "foreignField" : "township", "as" : "twp" 
			}
		},

		// Stage 3
		{
			$addFields: {
			    "twp" : {"$cond" : {"if" : {"$ne" : ["$county", null]}, 
			                           "then" : null,
			                           "else" : {"$cond" : {"if" : {"$eq" : [{"$size" : "$twp"}, 1]},
			                             				   "then" : {"$arrayElemAt" : ["$twp", 0]},
			                             				   "else" : "$twp"}}}}
			}
		},

		// Stage 4
		{
			$addFields: {
			  "county" : {"$cond" : {"if" : {"$ne" : ["$county", null]}, 
			                           "then" : "$county",
			                           "else" : "$twp.county"}}
			  
			}
		},

		// Stage 5
		{
			$group: {
				"_id" : "$county",
				"townships": {"$addToSet" : "$township"},
				"voilentcrimeTotal" : { "$sum" : "$violentCrimeTotal" }, 
				"totalPopulation" : { "$sum" : "$population" }, 
				"murderManslaughterTotal" : { "$sum" : "$murderManslaughter" }, 
				"forcibleRapeTotal" : { "$sum" : "$forcibleRape" }, 
				"robbertyTotal" : { "$sum" : "$robbery" }, 
				"aggravatedAssaultTotal" : { "$sum" : "$aggravatedAssault" },
				"propCrimeTotal" : { "$sum" : "$propertyCrimeTotal" },
				"burglaryTotal" : { "$sum" : "$burglary" },
				"larcenyTheftTotal" : { "$sum" : "$larcenyTheft" },
				"motorVehicleTheftTotal" : { "$sum" : "$motorVehicleTheft" },
				"voilentCrimeRate" : { "$avg" : "$violentCrimeRate" },
				"manslaughterRate" : { "$avg" : "$murderManslaughterRate" },
				"forcibleRapeRate" : { "$avg" : "$forcibleRapeRate" },
				"robberyRate" : { "$avg" : "$robberyRate" },
				"aggravatedAssaultRate" : { "$avg" : "$aggravatedAssaultRate" },
				"propertyCrimeRate" : { "$avg" : "$propertyCrimeRate" },
				"burglaryRate" : { "$avg" : "$burglaryRate" },
				"larcenyTheftRate" : { "$avg" : "$larcenyTheftRate" },
				"motorVehicleTheftRate" : { "$avg" : "$motorVehicleTheftRate" }
			}
		},
	],

	// Options
	{
		cursor: {
			batchSize: 50
		}
	}

	// Created with 3T MongoChef, the GUI for MongoDB - https://3t.io/mongochef

);
