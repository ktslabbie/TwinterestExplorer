/**
 * Web Worker for the KMEANS algorithm to cluster a network.
 */
importScripts('../vendor/lodash.min.js');

function calcSim(ontologyMap, clusterMap) {
	return _.reduce(clusterMap, function(simSum, n, key) { return (ontologyMap[key]) ? simSum + ontologyMap[key]*n : simSum; });
}

self.addEventListener('message', function(e) {
	
	var nodeCount = e.data.nodeCount;
	var ontologies = e.data.ontologies;
	
	//iterate k-means
	var clusters = {},
	    step = {},
	    rand = {},
	    errorsums = {},
	    maxiter = 100;
	
	var k = _.min([e.data.k, nodeCount]);
	
	for(var iteration = 0; iteration < e.data.i; iteration++) {
		console.log("k-means iteration: " + (iteration+1));
		
		clusters = {};
		step = {};
		rand = {};
		
		//randomly initialize cluster centers
		while(Object.keys(step).length < k) {
			var r = Math.floor(Math.random()*nodeCount);
			
			if(!(r in step))
				step[r] = { nodes: [], cfiufMap: ontologies[r] };
		}
		
		var go = true,
		    iter = 0,
		    oldcent = "",
            newcent = "";
		
		// O(ndki)
		while(go) {  // i
			clusters = step;
			
			//cluster assignment step
			for(var i = 0; i < nodeCount; i++) { // n
				var cent = null;
				var sim = 0;
				
				for(var c in clusters) { // k
					var csim = calcSim(ontologies[i], clusters[c].cfiufMap); // d
					
					if(csim > sim) {
						sim = csim;
						cent = c;
					}
				}
				
				if(!cent) cent = Object.keys(clusters)[0];
				
				if(clusters[cent].nodes.indexOf(i) == -1) {
					clusters[cent].nodes.push(i);
				}
			}
			
			//centroid update step
			step = {};
			var centIndex = nodeCount;
			
			for(var cent in clusters) { // k
				var centMap = {};
				
				_.each(clusters[cent].nodes, function(node) { // n
					var cfiufMap = ontologies[node];
					
					for(key in cfiufMap) { // d
						if(centMap[key]) {
							centMap[key] += cfiufMap[key];
						} else {
							centMap[key] = cfiufMap[key];
						}
					}
				});
				
				for(key in centMap) { // d
					centMap[key] /= clusters[cent].nodes.length;
				}
				
				step[centIndex] = {};
				step[centIndex].nodes = [];
				step[centIndex].cfiufMap = centMap;
				
				newcent += "" + calcSim(clusters[cent].cfiufMap, step[centIndex].cfiufMap) + ","; // d
				centIndex++;
			}
			
			//check break conditions
			newcent = newcent.substring(0, newcent.length-1);
			console.log("newcent:" + newcent);
			
			if(oldcent === newcent) go = false;
			if(++iter >= maxiter) go = false;
			
			oldcent = newcent;
			newcent = "";
		}
		
		if(iter < maxiter) console.log("Converged in " + iter + " steps.");
		else console.log("Stopped after " + maxiter + " iterations.");

		//calculate similarity sum and map it to the clustering
		var sumsim = 0.0;
		
		for(var c in clusters) {
			var cNodes = clusters[c].nodes;
			for(var node in cNodes) {
				sumsim += calcSim(ontologies[node], clusters[c].cfiufMap);
			}
		}
		
		errorsums[sumsim] = clusters;
	}
	
	console.log("Best Convergence:");
	var bestClusters = errorsums[_.max(Object.keys(errorsums))];
	
	for(var cent in bestClusters) {
		var cluster = bestClusters[cent].nodes;
		console.log("cent.nodes: " + cluster);
		
		var clusterSize = cluster.length;
		var clusterEdges = [];
		
		// Find the edges within the cluster, as well as the degrees of the edges.
		_.each(e.data.links, function(edge) {
			var s = _.indexOf(cluster, edge[0]);
			if(s >= 0) {
				var t = _.indexOf(cluster, edge[1]);
				if(t >= 0) {
					clusterEdges.push(edge);
				}
			}
		});
		
		self.postMessage( { finished: false, nodes: cluster, edges: clusterEdges, drop: false, } );
	}
	
	// We're done. Return this fact.
	self.postMessage( { finished: true } );
}, false);
