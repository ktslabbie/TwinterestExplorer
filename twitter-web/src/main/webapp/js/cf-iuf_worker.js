/**
 * Web Worker for calculating the CF-IUF vectors of users.
 */
importScripts('//cdnjs.cloudflare.com/ajax/libs/lodash.js/2.4.1/lodash.min.js');

/**
 * Extracts a more readable name from a YAGO type.
 */
function getTypeName(yagoType) {
	
	var typeName = yagoType.split(":")[1];
	var wordnetCode = typeName.substr(typeName.length - 9);
	
	if(!isNaN(wordnetCode))
		typeName = typeName.substr(0, typeName.length - 9);
	
	return typeName;
}

/**
 * Return the top-k types sorted by their value.
 */
function getTopTypes(map, k) {
	return _.map(_.first(_.sortBy(_.pairs(map), function(tuple) { return -tuple[1]; }), k), function(type) { type[0] = getTypeName(type[0]); return type; });
}

/**
 * Normalize the CF-IUF map to values between 0 and 1 given the Euclidian length of all CF-IUF scores.
 */
function normalizeCFIUF(userCFIUFMap, euclidLength) {
	return map = (euclidLength === 0) ? userCFIUFMap : _.mapValues(userCFIUFMap, function(val) { return val/euclidLength; });
}

self.addEventListener('message', function(e) {
	var ret = { finished: false, ontologies: [] };
	var data = e.data;
	var entityLength = data.entities.length;
	
	/* Calculate CF-IUF similarity wrt. all previous users. */
	for(var i = 0; i <= data.index; i++) {
		var currentOntology = data.entities[i].userOntology;
		//console.log("currentEntity: " + JSON.stringify(currentEntity));
		
		if(!currentOntology) {
			console.log("No ontology? Data: " + JSON.stringify(data.entities[i]));
			continue;
		}
		var yagoTypes = currentOntology.ontology;
		
		entityCFIUFMap = {};
		var cfIufSum = 0;
		
		//for(var type in yagoTypes) {
		_.each(_.keys(yagoTypes), function(type) {
			var iuf = Math.log(entityLength / data.ufMap[type]);  // TODO: experiment with non-zero error delta thingy
			
		
			var cf = yagoTypes[type];
			var cfIuf = (Math.pow(cf, 1 + parseFloat(data.generalityBias)))*(Math.pow(iuf, 1 - parseFloat(data.generalityBias)));
			cfIufSum += Math.pow(cfIuf, 2);
			entityCFIUFMap[type] = cfIuf;
		
		});
		
		//console.log("cfIufSum: " + cfIufSum);
		
		var euclidLength = Math.sqrt(cfIufSum);
		//console.log("euclidLength: " + euclidLength);
		//console.log("Unnormed CF-IUF of " + currentEntity.screenName + ": " + JSON.stringify(currentOntology.cfIufMap));
		
		if(currentOntology.cfIufMap) {
			var newMap = normalizeCFIUF(entityCFIUFMap, euclidLength);
			
			for(var key in newMap) {
				if(!currentOntology.cfIufMap[key]) {
					currentOntology.cfIufMap[key] = newMap[key];
				} else {
					var a = Math.round(currentOntology.cfIufMap[key] * 100) / 100;
					var b = Math.round(newMap[key] * 100) / 100;
					if(a != b) currentOntology.cfIufMap[key] = newMap[key];
				}
			}
		} else {
			currentOntology.cfIufMap = normalizeCFIUF(entityCFIUFMap, euclidLength);
		}
		//console.log("Normed CF-IUF: " + JSON.stringify(currentOntology.cfIufMap));
		currentOntology.topTypes = getTopTypes(currentOntology.cfIufMap, 5);
		ret.ontologies.push(currentOntology);
	}
	
	self.postMessage(ret);
}, false);
