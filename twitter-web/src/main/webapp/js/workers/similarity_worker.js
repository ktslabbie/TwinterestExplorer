/**
 * Web Worker for calculating the similarity graph of users.
 */
importScripts('../vendor/lodash.min.js');

self.addEventListener('message', function(e) {
	var ret = { finished: false };
	var N = e.data.cfiufMaps.length;
	
	// Calculate cosine similarity wrt. all previous users.
	for(var i = 0; i < N-1; i++) {
		var prevUserMap = e.data.cfiufMaps[i];

		for(var j = i+1; j < N; j++) {
			var curUserMap = e.data.cfiufMaps[j];
			var similarity = _.reduce(prevUserMap, function(simSum, n, key) { return (curUserMap[key]) ? simSum + curUserMap[key]*n : simSum; });

			if(similarity >= e.data.minSim) {
				ret.similarity = similarity;
				ret.i = i;
				ret.j = j;
				
				self.postMessage(ret);
			}
		}
	}

	ret.finished = true;
	
	self.postMessage(ret);
}, false);
