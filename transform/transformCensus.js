
// int age docs
db.census.find(
{$or : [{medianAgeFemale: {$type : "int"}}, {medianAgeMale: {$type : "int"}}, {medianAgeTotal : {$type : "int"}}]}
)

// int percent docs
db.census.find(
{$or : [
    {"percentOwnedLoan" : {$type : "int"}},
    {"percentOwnedNoLoan": {$type : "int"}},
    {"percentRenter" : {$type : "int"}},
    {"percentVacant" : {$type : "int"}},
    {"percentSeasonal" : {$type : "int"}},
    {"percentVacantNotSeasonal" :  {$type : "int"}}
]}
)
