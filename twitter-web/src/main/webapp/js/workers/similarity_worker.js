/**
 * Web Worker for calculating the similarity graph of users.
 */
self.addEventListener('message', function(e) {
	var ret = { finished: false };
	var N = e.data.cfiufMaps.length;
	
	// Calculate cosine similarity wrt. all previous users.
	for(var i = 0; i < N-1; i++) {
		var prevUserMap = e.data.cfiufMaps[i];

		for(var j = i+1; j < N; j++) {
			var curUserMap = e.data.cfiufMaps[j];
			var similarity = 0;
			
			//if(Object.keys(prevUserMap).length > 0 && Object.keys(curUserMap).length > 0) {
			for(var key in prevUserMap) {
				if(curUserMap[key]) {
					similarity += curUserMap[key]*prevUserMap[key];
					//console.log(key + " contained! (" + curUserMap[key] + " and " + prevUserMap[key] + "). New similarity: " + similarity);
				}
			}
			//}

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
