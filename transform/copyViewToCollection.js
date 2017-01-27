
db.countySingleView.drop()

db.countyCrimeAlcoholCensus.find({}).forEach(function(doc) {db.countySingleView.insert(doc)})
