/**
 * Web Worker for calculating the similarity graph of users.
 */
self.addEventListener('message', function(e) {
	var ret = { finished: false };
	
	/* Calculate cosine similarity wrt. all previous users. */
	for(var i = 0; i < e.data.users.length-1; i++) {
		
		var prevUserMap = e.data.users[i].ontology.cfIufMap;

		for(var j = i+1; j < e.data.users.length; j++) {
			var curUserMap = e.data.users[j].ontology.cfIufMap;
			var similarity = 0;

			for(var key in prevUserMap) {
				if(curUserMap[key]) {
					similarity += curUserMap[key]*prevUserMap[key];
				}
			}

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
