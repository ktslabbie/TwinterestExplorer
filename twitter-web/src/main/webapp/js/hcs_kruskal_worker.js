/**
 * Web Worker for the HCS algorithm to cluster a network.
 */
importScripts('//cdnjs.cloudflare.com/ajax/libs/lodash.js/2.4.1/lodash.min.js');

var allClusters = [];

function kruskal(nodes, edges) {
    var mst = [];
    var forest = _.map(nodes, function(node) { return [node]; });
    var sortedEdges = _.sortBy(edges, function(edge) { return edge[2]; });
    
    //console.log("Current forest: " + JSON.stringify(forest));
    //console.log("sortedEdges: " + JSON.stringify(sortedEdges));
    
    while(forest.length > 2 && sortedEdges.length > 0) {
        var edge = sortedEdges.pop();
        
        var n1 = edge[0],
            n2 = edge[1];
 
        var t1 = _.filter(forest, function(tree) {
            return _.include(tree, n1);
        });
            
        var t2 = _.filter(forest, function(tree) {
            return _.include(tree, n2);
        });
 
        if (t1 != t2) {
            forest = _.without(forest, t1[0], t2[0]);
            forest.push(_.union(t1[0], t2[0]));
            mst.push(edge);
        }
    }
    
    return { mst: mst, forest: forest };
}

var isHighlyConnected = function(nodes, edges) {
	var k = edges.length;
	var n = nodes.length;
	if(n < 3) return false; // cluster must have at least 3 vertices (our choice)
	//if(n == 2) return true;
	
	console.log("It seems # of edges = " + k + " and # of nodes: " + n);
	
	// Check the degrees of all nodes. Remember the lowest degree.
	for (var i = 0; i < nodes.length; i++) {
		var node = nodes[i];
		var degree = 0;
		
		for (var j = 0; j < edges.length; j++) {
			if(edges[j][0] == node || edges[j][1] == node) {
				degree++;
			}
		}
		
		if(degree < k) k = degree;
	}
	//if(k > n/15) console.log("Highly connected!")
	return (k > n/26);
}

var hcs = function(nodes, edges) {
	
	// A graph with one or two nodes is ignored.
	if(edges.length <= 2) {
		return;
	}
	
	//console.log("Nodes: " + nodes);
	//console.log("Edges: " + edges);
	
	var mstObj = kruskal(nodes, edges);
	
	//console.log("Forest: " + JSON.stringify(mstObj.forest));
	
	var clusters = mstObj.forest;
	
	// Split clusters into subgraphs.
	_.each(clusters, function(cluster) {
		var clusterSize = cluster.length;
		
		if(clusterSize == 1) return;	// Drop single isolated nodes from the result.
		
		var subEdges = [];
		
		var i = mstObj.mst.length;
		while(i--) {
			var edge = mstObj.mst[i];
			if(cluster.indexOf(edge[0]) > -1 && cluster.indexOf(edge[1]) > -1) {
				subEdges.push(edge);
			}
		}
		
		// Check for highly-connectedness. If so, we're done with this cluster, else call this function again with the subgraph.
		if(isHighlyConnected(cluster, subEdges)) {
			allClusters.push(cluster);
		} else {
			hcs(cluster, subEdges);
		}
	});
}

self.addEventListener('message', function(e) {
	
	var nodes = [], edges = [];
	
	// Initialize the node set.
	for(var i = 0; i < e.data.nodes.length; i++) {
		nodes.push(i);
	}
	
	var i = e.data.links.length;
	while(i--) edges.push([e.data.links[i].source, e.data.links[i].target, e.data.links[i].value]);
	
	//console.log("Sending nodes: " + JSON.stringify(nodes));
	//console.log("Sending edges: " + JSON.stringify(edges));
	
	//allLinks = [].concat(JSON.parse(JSON.stringify(e.data.links)));
	
	// First iteration. Input is the full network.
	hcs(nodes, edges);
	
	var ret = { finished: false,  clusters: allClusters };
	
	//console.log("Returning: " + JSON.stringify(ret));
	self.postMessage(ret);
}, false);
