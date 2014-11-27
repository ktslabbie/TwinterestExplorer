console.log("Worker starting!")

self.addEventListener('message', function(e) {

	var ret = { finished: false };
	
	/* Calculate cosine similarity wrt. all previous users. */
	for(var i = -1; i < e.data.N-2; i++) {
		var prevUserMap = (i == -1) ? e.data.targetUser.ontology.cfIufMap : e.data.network[i].ontology.cfIufMap;

		for(var j = i+1; j < e.data.N-1; j++) {
			var curUserMap = e.data.network[j].ontology.cfIufMap;
			var similarity = 0;

			for(k = 0; k < prevUserMap.length; k++) {
				for(l = 0; l < curUserMap.length; l++) {
					if(curUserMap[l][0] == prevUserMap[k][0]) {
						similarity += curUserMap[l][1]*prevUserMap[k][1];
						break;
					}
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
