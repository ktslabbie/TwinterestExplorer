console.log("Clustering worker starting!")

var allClusters = [];
var allNodes = [];
var allLinks = [];
var N;
var L;
var KARGER_ITERATIONS = 100;

var contract = function(nodes, links) {

	// |V|
	var numberOfNodes = nodes.length;
	
	// Initialize the node set This will represent our clusters at the end.
	var nodeSet = {};
	var i = numberOfNodes;
	while(i--) nodeSet[nodes[i]] = true;
	
	//console.log("DEBUG: nodes of graph we need to contract: " + JSON.stringify(nodeSet));
	
	// Initialize what will be the minimum cut.
	var minCut = [].concat(JSON.parse(JSON.stringify(links)));
	
	//console.log("DEBUG: starting minCut (should be the same 100 times): " + JSON.stringify(minCut));
	
	// Karger's algorithm: while |V| > 2 (and there are links left)...
	while(numberOfNodes > 2 && minCut.length > 0) {
		
		// Choose a link uniformly at random.
		var linkIndex = Math.floor(Math.random() * (minCut.length-1));
		var link = minCut[linkIndex];
		
		// The edges v and w connected by the link.
		var v = link.source; var w = link.target;
		
		// The new node vw obtained by contracting the nodes connected by the link.
		var vw = '' + v + '-' + w;
		
		// Add the combined node to the new node set.
		nodeSet[vw] = true;
		
		// Delete the previous ones.
		delete nodeSet[v];
		delete nodeSet[w];
		
		// We have one less node now.
		numberOfNodes--;
		
		// Remove the link we chose from the graph.
		minCut.splice(linkIndex, 1);
		
		//console.log("DEBUG: Do we still have the link? " + JSON.stringify(link));
		
		// Loop through the graph to set any other links that connected to v or w to vw.
		var i = minCut.length;
		while(i--) {
			var l = minCut[i];
			
			// Update nodes for links that linked to either v or w.
			if(l.source == v || l.source == w) {
				minCut[i].source = vw;
				
				// If they linked both, remove the link entirely.
				if(l.target == v || l.target == w) {
					minCut.splice(i, 1);
					continue;
				}
			}
			
			if(l.target == v || l.target == w) {
				minCut[i].target = vw;
			}
		}
	}
	
	// We should only have 2 nodes with links left, or more than 2 nodes with no links. This is the minimum cut.
	return { minCut: minCut, clusters: nodeSet };
}

var isHighlyConnected = function(cluster, links) {
	var k = links.length;
	var nodes = cluster.split('-');
	var n = nodes.length;
	if(n <= 2) return false; // cluster must have at least 3 vertices (according to HCS algo, at least?)
	
	console.log("It seems # of edges = " + k);
	
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
	
	
	return (k > n/3);
}

var hcs = function(nodes, links) {
	
	// A graph with one or two nodes is already an HCS.
	if(nodes.length <= 2) return;
	
	// Initialize the minumum cut to all the links we have.
	var minCutSize = links.length;
	var minCutSet = {};
	
	// Contract the graph a few times to find a minimum cut.
	for(var i = 0; i < KARGER_ITERATIONS; i++) {
		var currentMinCutSet = contract(nodes, links);
		var currentMinCutSize = currentMinCutSet.minCut.length;
		
		if(currentMinCutSize < minCutSize) {
			minCutSet = currentMinCutSet;
			minCutSize = currentMinCutSize;
		}
	}
	
	console.log("DEBUG: MinCutSet: " + JSON.stringify(minCutSet));
	
	var minCut = minCutSet.minCut;
	var clusters = minCutSet.clusters;
	
	// Get all the links in the minimum cut (if any; and they should link the same nodes).
	if(minCut && minCut.length > 0) {
		var source = '' + minCut[0].source;
		var target = '' + minCut[0].target;
		var sources = source.split('-');
		var targets = target.split('-');
		
		// Remove the cut from the full graph (i.e. all links in minCut).
		for(var i = 0; i < minCut.length; i++) {
			var j = allLinks.length;
			
			while(j--) {
				if( (sources.indexOf(allLinks[j].source) > -1 && targets.indexOf(allLinks[j].target)  > -1) ||
						(sources.indexOf(allLinks[j].target)  > -1 && targets.indexOf(allLinks[j].source)  > -1) ) {
					console.log("DEBUG: do we even get here? Removing: " + JSON.stringify(allLinks[j]));
					allLinks.splice(j, 1);
				}
			}
		}
	}
	
	// Split clusters into subgraphs.
	for (var cluster in clusters) {
		var subNodes = cluster.split('-');
		var subLinks = [];
		
		for (var i = 0; i < subNodes.length; i++) {
			var node = subNodes[i];
			
			for(var j = 0; j < allLinks.length; j++) {
				if(allLinks[j].source == node || allLinks[j].target == node) {
					subLinks.push(allLinks[j]);
				}
			}
		}
		
		console.log("DEBUG: subLinks: " + JSON.stringify(subLinks));
		
		// Check for highly-connectedness. If so, we're done with this cluster, else call this function again with the subgraph.
		if(isHighlyConnected(cluster, subLinks)) {
			allClusters.push(cluster);
		} else {
			hcs(subNodes, subLinks);
		}
	}
}

self.addEventListener('message', function(e) {
	allClusters = [];
	allNodes = [];
	allLinks = [];

	// Make all nodes and links available for all functions.
	for(var i = 0; i < e.data.nodes.length; i++) {
		allNodes.push(e.data.nodes[i].index);
	}
	
	N = allNodes.length;
	allLinks = e.data.links;
	L = allLinks.length;
	
	// First iteration. Input is the full network.
	hcs(allNodes, allLinks);
	
	var ret = { finished: false,  clusters: allClusters };
	
	//console.log("Returning: " + JSON.stringify(ret));
	self.postMessage(ret);
}, false);
