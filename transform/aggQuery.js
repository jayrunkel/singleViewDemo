db.crimeTwp.aggregate(

	// Pipeline
	[
		// Stage 1
		{
			$project: {stuff : {$split : ["$agency", " "]}, state : 1, months: 1, population: 1, violentCrimeTotal : 1, murderManslaughter: 1, forcibleRape: 1, robbery: 1, aggravatedAssault: 1, propertyCrimeTotal: 1, burglary: 1, larcenyTheft: 1, motorVehicleTheft: 1, violentCrimeRate:1, murderManslaughterRate: 1, forcibleRapeRate: 1, robberyRate: 1, aggravatedAssaultRate: 1, propertyCrimeRate: 1, burglaryRate: 1, larcenyTheftRate: 1, motorVehicleTheftRate: 1}
		},

		// Stage 2
		{
			$project: {state: 1, months: 1, population: 1, violentCrimeTotal : 1, murderManslaughter: 1, forcibleRape: 1, robbery: 1, aggravatedAssault: 1, propertyCrimeTotal: 1, burglary: 1, larcenyTheft: 1, motorVehicleTheft: 1, violentCrimeRate:1, murderManslaughterRate: 1, forcibleRapeRate: 1, robberyRate: 1, aggravatedAssaultRate: 1, propertyCrimeRate: 1, burglaryRate: 1, larcenyTheftRate: 1, motorVehicleTheftRate: 1, tsp:{$arrayElemAt:["$stuff", 0]}}
		},

		// Stage 3
		{
			$lookup: {
			  from: "tspToCounty",
			  localField:"tsp",
			  foreignField: "township",
			  as:"twp"
			}
		},

		// Stage 4
		{
			$sort: {state : 1}
		},

	]

	// Created with 3T MongoChef, the GUI for MongoDB - https://3t.io/mongochef

);
