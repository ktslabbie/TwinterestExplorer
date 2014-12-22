var graphService = angular.module('twitterWeb.GraphService', []);

graphService.factory('Graph', function() {	
	
	/**
	 * Constructor, with class name
	 */
	function Graph(width, height, id) {
		
		var that = this;
		
		var color = d3.scale.category10();
		var nodes = []; 					// Ex. { name: "", group: 0, userIndex: 0, index: 0 }
		var links = []; 					// Ex. { source: nodeA, target: nodeB, value: 0.00 }
		var nodeNameMap = {};				// A screenName -> nodeIndex map to make node lookups faster (from O(n) to O(log n)).
		
		var force = d3.layout.force()
			.nodes(nodes)
			.links(links)
			.charge(-240)
			.linkDistance(120)
			.size([width, height])
			.on("tick", tick);

		var svg = d3.select(id).append("svg")
			.attr("width", width)
			.attr("height", height);
	
		var node = svg.selectAll(".node");
		var link = svg.selectAll(".link");
		var text = svg.selectAll(".node-text");
		var legend = svg.selectAll(".legend");
		
		this.getNodes = function() { return nodes; }
		this.getLinks = function() { return links; }
		this.getNodeNameMap = function() { return nodeNameMap; }
		this.getForce = function() { return force; }

		/**
		 * Add a node to the graph if not there yet. Returns the index of the new or existing node.
		 */
		this.addNode = function(pNode) {
			var index = nodeNameMap[pNode.name];
			if(index >= 0) return index;
			
			// Node not contained. Add it.
			nodes.push(pNode);
			index = nodes.length-1;
			nodeNameMap[pNode.name] = index;
			return index;
		}
		
		/** 
		 * Adds a link to the graph.
		 */
		this.addLink = function(pLink) {
			links.push(pLink);
		}

		/** 
		 * Set a node to a group given an index of the node.
		 */
		this.setGroup = function(nodeIndex, group) {
			nodes[nodeIndex].group = group;
		}

		/** 
		 * Removes a node given an index. Returns the new number of nodes in the graph.
		 */
		this.removeNodeByIndex = function(nodeIndex) {
			nodes.splice(nodeIndex, 1);
			return nodes.length-1;
		}
		
		/** 
		 * Removes all links connected to a certain node.
		 */
		this.removeNodeLinks = function(nodeIndex) {
			if(nodeIndex > -1) {
				for(var i = links.length-1; i >= 0; i--) {
					if(links[i].source.index === nodeIndex || links[i].target.index === nodeIndex) {
						links.splice(i, 1);
					}
				}
			}
		}
		
		/** 
		 * Removes all nodes and links from the graph.
		 */
		this.clearGraph = function() {
			nodeNameMap = {};
			while(links.length > 0) links.pop();
			while(nodes.length > 0) nodes.pop();
		}
		
		/** 
		 * Removes all nodes and links from the graph, as well as the graph itself.
		 */
		this.killGraph = function() {
			that.clearGraph();
			svg.selectAll("").remove();
		}
		
		/** 
		 * Adds a cluster (given nodes and links) to the graph and assigns it a group number.
		 */
		this.addCluster = function(pNodes, pLinks, group) {
			_.each(pLinks, function(pLink) {
				pLink.source.group = group;
				pLink.target.group = group;
				var aIndex = that.addNode(pLink.source);
				var bIndex = that.addNode(pLink.target);
				that.addLink({ source: aIndex, target: bIndex, value: pLink.value });
			});
		}
		
		this.removeLink = function(s, t) {
			var i = links.length;
			while(i--) {
				if( (s === links[i].source.userIndex && t === links[i].target.userIndex) || (t === links[i].source.userIndex && s === links[i].target.userIndex) ) {
					links.splice(i, 1);
					return;
				}
			}
		}
		

		this.start = function(scopeLegend) {
			link = link.data(force.links(), function(d) { return d.value; });
			link.enter().insert("line", ".node").attr("class", "link");
			link.exit().remove();
			link.style("stroke-width", function(d) { return (Math.pow(d.value*3.5, 2)); });

			node = node.data(force.nodes(), function(d) { return d.name;});
			node.enter().append("circle").attr("class", function(d) { return "node " + d.name; }).attr("r", 8);
			node.exit().remove();
			node.style("fill", function(d) { return color(d.group); });
			node.call(force.drag);

			text = text.data(force.nodes());
			text.enter().append("text");
			text.exit().remove();
			text.attr("x", 8);
			text.attr("y", ".31em");
			text.text(function(d) { return "@" + d.name; });
			
			if(scopeLegend) {
				legend = legend.data(color.domain())
					.enter().append("g")
					.attr("class", "legend")
					.attr("transform", function(d, i) { return "translate(0," + i * 20 + ")"; });

				legend.append("rect")
					.attr("x", width - 136)
					.attr("width", 18)
					.attr("height", 18)
					.style("fill", color);
			
				legend.append("text")
					.attr("x", width - 140)
					.attr("y", 9)
					.attr("dy", ".35em")
					.style("text-anchor", "end")
					.style("font-size","12px")
					.text(function(d) { return scopeLegend[d]; });
			}

			force.start();
		}
		
		function tick() {
			var k = 0.02;

			nodes.forEach(function(o, i) {
				if(o.group % 5 == 0) {
					o.y += -o.group*k;
				}
				if(o.group % 4 == 0) {
					o.x += o.group*k;
				}
				if(o.group % 3 == 0) {
					o.y += o.group*k;
				}
				if(o.group % 2 == 0) {
					o.x += -o.group*k;
				} 
				//o.x += (o.group % 2 == 0) ? o.group*k : -o.group*k;
				//o.y += (o.group % 3 == 0) ? o.group*k : -o.group*k;
			});

			node.attr("cx", function(d) { return d.x; })
				.attr("cy", function(d) { return d.y; })

			link.attr("x1", function(d) { return d.source.x; })
				.attr("y1", function(d) { return d.source.y; })
				.attr("x2", function(d) { return d.target.x; })
				.attr("y2", function(d) { return d.target.y; });

			text.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
		}
	}
	
	/**
   * Return the constructor function
   */
	return Graph;
});