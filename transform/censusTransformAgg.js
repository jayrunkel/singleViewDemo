// ================================================================
// File: censusTransformAgg.js
//
// Description:
// Converts the census data to a format that can be consumed by SparkR. The following transformations are
// performed:
//   1. convert all medianAge* fields to Doubles. They are ints, if they happen to be whole numbers
//
// History:
//   28Nov2016 JTR - Wrote pipeline to convert all medianAge field values to doubles

// Stages that have been excluded from the aggregation pipeline query
__3t_MongoChef_disabled_aggregation_stages = [

	{
		// Stage 2 - excluded
		stage: 2,  source: {
			$match: {
			$or : [{medianAgeFemale: {$type : "int"}}, {medianAgeMale: {$type : "int"}}, {medianAgeTotal : {$type : "int"}}]
			}
		}
	},
]

db.census.aggregate(

	// Pipeline
	[
		// Stage 1
		{
			$addFields: {
			   
			    medianAgeTotal : {$multiply : [ "$medianAgeTotal", 1.0000]},
			    medianAgeMale: {$multiply : ["$medianAgeMale", 1.0000]},
			    medianAgeFemale: {$multiply : ["$medianAgeFemale", 1.0000]}
			}
		},

		// Stage 3
		{
			$out: "censusClean"
		},
	],

	// Options
	{
		allowDiskUse: true
	}

	// Created with 3T MongoChef, the GUI for MongoDB - https://3t.io/mongochef

);
