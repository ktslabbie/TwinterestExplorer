/**
 * Web Worker for the HCS algorithm to cluster a network.
 */
importScripts('//cdnjs.cloudflare.com/ajax/libs/lodash.js/2.4.1/lodash.min.js');

var allClusters = [];

function kruskal(nodes, edges) {
    var mst = [];
    var forest = _.map(nodes, function(node) { return [node]; });
    var sortedEdges = _.sortBy(edges, function(edge) { return edge.value; });
    
    while(forest.length > 2 && sortedEdges.length > 0) {
        var edge = sortedEdges.pop();
        var n1 = edge.source, n2 = edge.target;
 
        var t1 = _.filter(forest, function(tree) { return _.where(tree, n1).length; });
        var t2 = _.filter(forest, function(tree) { return _.where(tree, n2).length; });
        
        if (!_.isEqual(t1, t2)) {
            forest = _.without(forest, t1[0], t2[0]);
            forest.push(_.union(t1[0], t2[0]));
            mst.push(edge);
        }
    }
    
    return { mst: mst, forest: forest };
}

function isHighlyConnected(nodes, edges) {
	var k = edges.length, n = nodes.length;
	if(n < 3) return false; // cluster must have at least 3 vertices (our choice)
	//if(n == 2) return true;
	
	console.log("# of edges: " + k + ", # of nodes: " + n);
	
	// Check the degrees of all nodes. Remember the lowest degree.
	for (var i = 0; i < nodes.length; i++) {
		var node = nodes[i];
		var degree = 0;
		
		for (var j = 0; j < edges.length; j++)
			if(edges[j].source.userIndex == node.userIndex || edges[j].target.userIndex == node.userIndex)
				degree++;
		
		if(degree < k) k = degree;
	}

	return (k > n/4);
}

function hcs(nodes, edges) {
	
	// A graph with one or two nodes is ignored.
	if(edges.length <= 2) return;
	
	var mstObj = kruskal(nodes, edges);
	var clusters = mstObj.forest;
	
	// Split clusters into subgraphs.
	_.each(clusters, function(cluster) {
		var clusterSize = cluster.length;
		
		if(clusterSize == 1) return;	// Drop single, isolated nodes from the result.
		
		var subEdges = [];
		
		_.each(edges, function(edge){
			if(_.where(cluster, edge.source).length && _.where(cluster, edge.target).length)
				subEdges.push(edge);
		});
		
		// Check for highly-connectedness. If so, we're done with this cluster, else call this function again with the subgraph.
		if(isHighlyConnected(cluster, subEdges))
			allClusters.push({ nodes: cluster, edges: subEdges });
		else
			hcs(cluster, subEdges);
	});
}

self.addEventListener('message', function(e) {
	
	// First iteration. Input is the full network.
	hcs(e.data.nodes, e.data.links);
	
	// We're done. Return all clusters.
	self.postMessage( { finished: false,  clusters: allClusters } );
	
}, false);
