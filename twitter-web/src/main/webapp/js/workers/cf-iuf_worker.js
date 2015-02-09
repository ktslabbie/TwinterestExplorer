/**
 * Web Worker for calculating the CF-IUF vectors of users.
 */
importScripts('../vendor/lodash.min.js');

var ufMap = {};
var gfMap = {};
var userOntologies = [];
var groupOntologies = [];

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
	return _.map(_.take(_.sortBy(_.pairs(map), function(tuple) { return -tuple[1]; }), k), function(tup) { tup[0] = getTypeName(tup[0]); return tup; });
}

/**
 * Update the user frequency map with new values.
 */
function updateUFMap(typeMap) {
	for(type in typeMap) ufMap[type] = ufMap[type] ? ufMap[type] + 1 : 1;
}

/**
 * Normalize the CF-IUF map to values between 0 and 1 given the Euclidian length of all CF-IUF scores.
 */
function normalizeCFIUF(userCFIUFMap, euclidLength) {
	return map = (euclidLength === 0) ? userCFIUFMap : _.mapValues(userCFIUFMap, function(val) { return val/euclidLength; });
}

self.addEventListener('message', function(e) {
	
	// If the clear flag is set, clear the maps and return.
	if(e.data.clear) {
		ufMap = {};
		gfMap = {};
		userOntologies = [];
		groupOntologies = [];
		return;
	}
	
	// TODO: proper separation between user/group ontologies so we can step back without recalcing everything.
	if(e.data.groups) {
		ufMap = {};
		userOntologies = [];
	}
	
	var ret = { ontologies: [] };
	var data = e.data;
	
	// Since we only send new ontologies, add them to the full list and update the user frequency map.
	_.each(data.ontologies, function(ontology) {
		userOntologies.push(ontology);
		updateUFMap(ontology);
	});
	
	var N = userOntologies.length;
	
	console.log("CF-IUF: N: " + N);
	
	// Calculate CF-IUF weights wrt. all previous users.
	for(var i = 0; i < N; i++) {
		var currentOntology = { ontology: userOntologies[i] };
		var types = currentOntology.ontology;
		
		entityCFIUFMap = {};
		var cfIufSum = 0;
		
		_.each(_.keys(types), function(type) {
			var iuf = Math.log(N / ufMap[type]);  // TODO: experiment with non-zero error delta thingy
			
			// Shortcut. If this is 0, the result will be 0, so no need to continue calculating.
			if(iuf != 0) {
				var cf = types[type];
				var cfIuf = (Math.pow(cf, 1 + parseFloat(data.generalityBias)))*(Math.pow(iuf, 1 - parseFloat(data.generalityBias)));
				cfIufSum += Math.pow(cfIuf, 2);
				entityCFIUFMap[type] = cfIuf;
			}
		});
		
		var euclidLength = Math.sqrt(cfIufSum);
		
		currentOntology.cfiufMap = normalizeCFIUF(entityCFIUFMap, euclidLength);
		currentOntology.topTypes = getTopTypes(currentOntology.cfiufMap, 5); // TODO: this is slow (25% slowdown). Try to do it during the loop somehow.
		//currentOntology.topTypes = [["lolewqtqwe", 0.99], ["lorweqrewrl2", 0.98], ["lofasdfdsafl", 0.99], ["lbcvxbvxcol2", 0.98], ["lfewfweewfqol", 0.99]];
		ret.ontologies.push({ cfiufMap: currentOntology.cfiufMap, topTypes: currentOntology.topTypes });
	}
	
	self.postMessage(ret);
});
