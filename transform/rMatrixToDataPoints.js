
db.corMatrix.drop();

function setCategory(str) {
  
  var category;
  
   if (str.startsWith("alcohol")) {
  	   category = "alcohol";
   }
   else if (str.startsWith("census")) {
  	   category = "census";
   }
   else if (str.startsWith("diabetes")) {
       category = "diabetes";
   }
    else if (str.includes("LE")) {
	category = "lifeExpectancy";
   }
   else {
  	   category = "crime";
   }
   
   return category;
}


db.testR.find({}).forEach(function(doc) {
    
    var param = doc.parameter;
    var paramCategory = setCategory(param);
  
  	
    for (att in doc) {
        var attCategory = setCategory(att);
       print("param: " + param + " category: " + paramCategory + " att: " + att + " attCategory: " + attCategory);
       
	    if ((att != "parameter") && (att != "_id")  && (att != param) && (paramCategory != attCategory)) {
	       var newDoc = {};

	       if (param < att) {
		      newDoc["p1"] = param;
		      newDoc["p2"] = att;
	       }
	       else {
		      newDoc["p1"] = att;
		      newDoc["p2"] = param;
	       }

	       newDoc["value"] = doc[att];
	    
	       var existingDoc = db.corMatrix.count({"p1" : newDoc["p1"], "p2" : newDoc["p2"]})
	       if (existingDoc == 0) {
		      db.corMatrix.insert(newDoc);
	       }
  	   }
    }
})
    

