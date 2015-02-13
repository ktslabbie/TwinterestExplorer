/**
 * Web Worker for the HCS algorithm to cluster a network.
 */
importScripts('../vendor/lodash.min.js');

function kruskal(nodes, edges) {
    var forest = _.map(nodes, function(node) { return [node]; });
    var i = edges.length;
    
    while(i--) {
    	if(forest.length <= 2) break;
    	var edge = edges[i];
        var t1 = _.find(forest, function(tree) { return _.includes(tree, edge[0]); });
        var t2 = _.find(forest, function(tree) { return _.includes(tree, edge[1]); });
        
        if (!_.isEqual(t1, t2)) {
            forest = _.without(forest, t1, t2);
            forest.push(_.union(t1, t2));
        }
    }
    
    return forest;
}

function isHighlyConnected(degreeObj, edges) {
	var degrees = _.values(degreeObj);
	var n = degrees.length;
	var k = edges.length;
	
	// Get the node with the lowest degree.
	var minDegree = _.min(degrees);
	console.log("# of edges: " + k + ", # of nodes: " + n + ", min. degree: " + minDegree);

	return (minDegree >= n/3);
}

function hcs(nodes, edges) {
	
	// A graph with one or two nodes is ignored.
	if(edges.length <= 1) return;
	
	var clusters = kruskal(nodes, edges);
	
	// Split clusters into subgraphs.
	_.each(clusters, function(cluster, i) {
		var clusterSize = cluster.length;
		if(clusterSize <= 2) return;	// Drop single, isolated nodes from the result.
		
		var clusterEdges = [];
		var degrees = {};

		while(clusterSize--) degrees[cluster[clusterSize]] = 0;
		
		_.each(edges, function(edge) {
			var s = _.indexOf(cluster, edge[0]);
			if(s >= 0) {
				var t = _.indexOf(cluster, edge[1]);
				if(t >= 0) {
					clusterEdges.push(edge);
					degrees[cluster[s]]++;
					degrees[cluster[t]]++;
				}
			}
		});
		
		// Check for highly-connectedness. If so, we're done with this cluster, else call this function again with the subgraph.
		if(isHighlyConnected(degrees, clusterEdges))
			self.postMessage( { finished: false, nodes: cluster, edges: clusterEdges } );
		else
			hcs(cluster, clusterEdges);
	});
}

self.addEventListener('message', function(e) {	
	
	// Generate the nodes.
	var nodes = _.range(e.data.nodeCount);
	// Sort the edges by similarity score.
	var sortedEdges = _.sortBy(e.data.links, function(ln) { return ln[2]; });
	
	// First iteration. Input is the full network.
	hcs(nodes, sortedEdges);
	
	// We're done. Return this fact.
	self.postMessage( { finished: true } );
}, false);
