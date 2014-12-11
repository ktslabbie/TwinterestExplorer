/**
 * Web Worker for the HCS algorithm to cluster a network.
 */
var allClusters = [];
var KARGER_ITERATIONS = 1;

var contract = function(pNodeAdjMap, N, t) {
	
	//console.log("DEBUG: nodes of graph we need to contract: " + JSON.stringify(nodeSet));
	
	// Initialize the what will be the minimum cut and nodeSet.
	var nodeAdjMap = [].concat(JSON.parse(JSON.stringify(pNodeAdjMap)));
	
	//console.log("DEBUG: N and t: " + N + " " + t);
	//console.log("DEBUG: confirm minCut length: " + minCut.length);
	
	
	// Karger's algorithm: while N > 2 (and there are links left)...
	while(N > t && nodeAdjMap.length > 0) {
		
		// Choose a link uniformly at random.
		var sourceNode = nodeAdjMap[Math.floor( Math.random() * (nodeAdjMap.length-0.00001) )];
		var targetNode = nodeAdjMap[Math.floor( Math.random() * (nodeAdjMap.length-0.00001) )];
		
		var link = minCut[linkIndex];
		
		//console.log("DEBUG: linkIndex and link: " + linkIndex + " " + JSON.stringify(link));
		//console.log("Current minCut: " + JSON.stringify(minCut));
		
		// The nodes v and w connected by the link.
		var v = link.source; var w = link.target;
		
		// The new node vw obtained by contracting the nodes connected by the link.
		var vw = '' + v + '-' + w;
		
		// Add the combined node to the new node set.
		nodeSet[vw] = true;
		
		// Delete the previous ones.
		delete nodeSet[v];
		delete nodeSet[w];
		
		//console.log("Got nodes: " + v + " and " + w);
		
		// We have one less node now.
		N--;
		
		// Remove the link we chose from the graph.
		minCut.splice(linkIndex, 1);
		
		//console.log("DEBUG: Do we still have the link? " + JSON.stringify(link));
		
		// Loop through the graph to set any other links that connected to v or w to vw.
		var i = minCut.length;
		while(i--) {
			var l = minCut[i];
			//console.log("Current link to check: " + JSON.stringify(l));
			
			// Update nodes for links that linked to either v or w.
			if(l.source == v || l.source == w) {
				minCut[i].source = vw;
				
				// If they linked both, remove the link entirely.
				if(l.target == v || l.target == w) {
					
					//console.log("Cut seems to have a link with both source and target nodes: " + JSON.stringify(l));
					minCut.splice(i, 1);
					continue;
				}
			}
			
			if(l.target == v || l.target == w)
				minCut[i].target = vw;
		}
		
		//console.log("DEBUG: new MinCut length: " + minCut.length);
	}
	
	//console.log("DEBUG: new minCut length: " + minCut.length);
	
	// We should only have t nodes with links left, or more than t nodes with no links. This is the minimum cut.
	return { minCut: minCut, nodeSet: nodeSet, N: N };
}

var isHighlyConnected = function(cluster, links) {
	var k = links.length;
	var nodes = cluster.split('-');
	var n = nodes.length;
	if(n < 2) return false; // cluster must have at least 3 vertices (our choice)
	if(n == 2) return true;
	
	console.log("It seems # of edges = " + k + " and # of nodes: " + n);
	
	// Simple version.
	//return (k > n/2);
	
	// Check the degrees of all nodes. Remember the lowest degree.
	for (var i = 0; i < nodes.length; i++) {
		var node = nodes[i];
		var degree = 0;
		
		for (var j = 0; j < links.length; j++) {
			if(links[j].source == node || links[j].target == node) {
				degree++;
			}
		}
		
		if(degree < k) k = degree;
	}
	
	return (k > n/2);
}

var mincut = function(nodeAdjMap, N) {
	
	//console.log("DEBUG: mincut: Applying regular Karger on nodes: " + JSON.stringify(nodeSet));
	//console.log("DEBUG: mincut: Applying regular Karger on edges: " + JSON.stringify(links));
	//console.log("DEBUG: mincut: N is less than 6. Right? " + N + ". If not, then cutSize is 0: " + links.length);
	
	// Initialize the minumum cut to all the links we have.
	var minCutSize = links.length;
	var minCutSet = {};
	
	if(minCutSize <= 1) {
		return { minCut: links, nodeSet: nodeSet, N: N };
	}
	
	//console.log("DEBUG: mincut: Starting Karger iterations with minCutSize of " + minCutSize + ".");
	
	// Contract the graph a few times to find a minimum cut.
	for(var i = 0; i < KARGER_ITERATIONS; i++) {
		//var currentMinCutSet = contract(nodes, links, N);
		var currentMinCutSet = contract(nodeSet, links, N, 2);
		var currentMinCutSize = currentMinCutSet.minCut.length;
			
		if(currentMinCutSize < minCutSize) {
			minCutSet = currentMinCutSet;
			minCutSize = currentMinCutSize;
			if(currentMinCutSize == 1) break; // Not going to get smaller than this.
		}
	}
	
	//console.log("DEBUG: mincut: Karger iterations finished! Returning: " + JSON.stringify(minCutSet));
	
	return minCutSet;
}

var fastmincut = function(nodeAdjMap, N) {
	
	if(N < 6 || nodeAdjMap.length == 0) {
		//console.log("DEBUG: fastmincut: N = " + N + " (< 6) and/or no links. Apply regular mincut.");
		return mincut(nodeAdjMap, N);
	}
	
	var t = Math.ceil(N/Math.sqrt(2));
	
	//console.log("DEBUG: fastmincut: Current N: " + N + ", t: " + t);
	
	//var nodeSet1 = JSON.parse(JSON.stringify(nodeSet));
	//var links1 = [].concat(JSON.parse(JSON.stringify(links)));
	//var nodeSet2 = JSON.parse(JSON.stringify(nodeSet));
	//var links2 = [].concat(JSON.parse(JSON.stringify(links)));
	
	//console.log("DEBUG: fastmincut: Contracting nodes: " + JSON.stringify(nodeSet));
	//console.log("DEBUG: fastmincut: Contracting edges: " + JSON.stringify(links));
	
	
	var g1 = contract(nodeAdjMap, N, t);
	if(g1.nodeAdjMap.length == 0) return g1;
	
	//console.log("DEBUG: fastmincut: Contracting 2nd n: " + JSON.stringify(nodeSet));
	//console.log("DEBUG: fastmincut: Contracting 2nd e: " + JSON.stringify(links));
	
	var g2 = contract(nodeAdjMap, N, t);
	if(g2.nodeAdjMap.length == 0) return g2;
	
	//console.log("DEBUG: g1: " + JSON.stringify(g1));
	//console.log("DEBUG: g1 minCut length: " + g1.minCut.length);
	
	//console.log("DEBUG: fastmincut: Contracted nodeSet: " + JSON.stringify(g1.nodeSet));
	//console.log("DEBUG: fastmincut: minCut: " + JSON.stringify(g1.minCut));
	//console.log("DEBUG: fastmincut: new N: " + JSON.stringify(g1.N));
	//console.log("DEBUG: fastmincut: Recursion step.");
	
	var f1 = fastmincut(g1.nodeAdjMap, g1.N);
	var f2 = fastmincut(g2.nodeAdjMap, g2.N);
	
	//console.log("DEBUG: fastmincut: Returning: " + JSON.stringify(f1));
	//console.log("DEBUG: fastmincut: Or return: " + JSON.stringify(f2));
	
	//return f1;
	if(f1.minCut.length < f2.minCut.length) return f1;
	else return f2;
}

var hcs = function(nodeAdjMap, N) {
	
	//var N = Object.keys(nodeSet).length;
	
	//console.log("DEBUG: HCS: N is: " + N);
	
	// A graph with one or two nodes is ignored.
	if(N <= 2) {
		/*for(var cluster in nodeSet)
			allClusters.push(cluster);*/
		return;
	}
	
	var minCutSet = fastmincut(nodeAdjMap, N);
	
	var minCut = minCutSet.minCut;
	var clusters = minCutSet.nodeSet;
	
	//if(clusters.length < 2) console.log("SUM TING WONG");
	
	// Get all the links in the minimum cut (if any; and they should link the same nodes).
	if(minCut && minCut.length > 0) {
		var source = '' + minCut[0].source;
		var target = '' + minCut[0].target;
		var sources = source.split('-');
		var targets = target.split('-');
		
		//console.log("allLinks: " + JSON.stringify(allLinks));
		
		// Remove the cut from the full graph (i.e. all links in minCut).
		for(var i = 0; i < sources.length; i++) {
			var source = sources[i];
			for(var j = 0; j < targets.length; j++) {
				var target = targets[j];
				delete allLinks[source + "-" + target];
				delete allLinks[target + "-" + source];
			}
		}
		
		//console.log("allLinks after cutting: " + JSON.stringify(allLinks));
		
		// Remove the cut from the full graph (i.e. all links in minCut).
		/*for(var i = 0; i < minCut.length; i++) {
			var j = allLinks.length;
			
			while(j--) {
				if( (sources.indexOf(allLinks[j].source) > -1 && targets.indexOf(allLinks[j].target)  > -1) ||
						(sources.indexOf(allLinks[j].target)  > -1 && targets.indexOf(allLinks[j].source)  > -1) ) {
					//console.log("DEBUG: do we even get here? Removing: " + JSON.stringify(allLinks[j]));
					allLinks.splice(j, 1);
				}
			}
		}*/
	}
	
	// Split clusters into subgraphs.
	for (var cluster in clusters) {
		var subNodes = cluster.split('-');
		var subN = subNodes.length;
		
		if(subN == 1) continue;	// Drop single isolated nodes from the result.
		
		var subNodeSet = {};
		var subLinks = [];
		
		//console.log("DEBUG: HCS: subNodes: " + JSON.stringify(subNodes));
		
		subNodeSet[subNodes[0]] = true;
		
		//console.log("allLinks cluster: " + JSON.stringify(allLinks));
		
		for (var i = 0; i < subNodes.length-1; i++) {
			var nodeA = subNodes[i];
			
			for(var j = i+1; j < subNodes.length; j++) {
				var nodeB = subNodes[j];
				subNodeSet[nodeB] = true;
				
				if(allLinks[nodeA + "-" + nodeB]) {
					subLinks.push(allLinks[nodeA + "-" + nodeB]);
				} else if(allLinks[nodeB + "-" + nodeA]) {
					subLinks.push(allLinks[nodeB + "-" + nodeA]);
				}
			}
			
			/*for(var j = 0; j < allLinks.length; j++) {
				//console.log("DEBUG: allLinks[" + j + "]: " + JSON.stringify(allLinks[j]));
				if(allLinks[j].source == node || allLinks[j].target == node) {
				//if(subNodeSet[allLinks[j].source] && subNodeSet[allLinks[j].target]) {
					subLinks.push(allLinks[j]);
				}
			}*/
		}
		
		//console.log("allLinks after cluster: " + JSON.stringify(allLinks));
		
		//console.log("DEBUG: subLinks: " + JSON.stringify(subLinks));
		
		// Check for highly-connectedness. If so, we're done with this cluster, else call this function again with the subgraph.
		if(isHighlyConnected(cluster, subLinks)) {
			//console.log("DEBUG: HCS: cluster " + JSON.stringify(cluster) + " is highly connected! Done.");
			allClusters.push(cluster);
		} else {
			//console.log("Calling again on: " + JSON.stringify(subLinks));
			//console.log("Nodes: " + JSON.stringify(subNodeSet));
			hcs(subNodeSet, subLinks, subN);
		}
	}
	
	//console.log("How's them allLinks look? " + JSON.stringify(allLinks));
}

self.addEventListener('message', function(e) {
	
	var N = e.data.nodes.length;
	var nodeAdjMap = {};
	
	// Make all nodes and links available for all functions.
	for(var i = 0; i < N; i++) {
		nodeAdjMap[e.data.nodes[i].userIndex] = [];
	}
	
	//console.log("DEBUG: nodes: " + JSON.stringify(e.data.nodes));
	
	var i = e.data.links.length;
	while(i--) nodeAdjMap[e.data.links[i].source].push(e.data.links[i].target);
	
	//allLinks = [].concat(JSON.parse(JSON.stringify(e.data.links)));
	
	// First iteration. Input is the full network.
	hcs(nodeAdjMap, N);
	
	var ret = { finished: false,  clusters: allClusters };
	
	//console.log("Returning: " + JSON.stringify(ret));
	self.postMessage(ret);
}, false);
