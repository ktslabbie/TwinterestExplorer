var graphService = angular.module('twitterWeb.GraphService', []);

graphService.factory('Graph', ['$rootScope', function($rootScope) {	
	
	/**
	 * Constructor for a graph.
	 * 
	 * @param width
	 * @param height
	 * @param id The HTML id tag
	 */
	function Graph(width, height, id) {
		
		var that = this;
		
		var color = d3.scale.category20();
		var nodes = []; 					// Ex. [ { name: somename, group: 0, userIndex: 0, index: 0 } ]
		var links = []; 					// Ex. [ { source: nodeAName, target: nodeBName, value: 0.00 } ]
		var nodeNameMap = {};				// A name -> nodeIndex map to make node lookups faster.
		var linkSigMap = {};				// A i-j -> linkIndex map to make link lookups faster. i < j always.
		
		var force = d3.layout.force()
			.nodes(nodes)
			.links(links)
			.charge(-180)
			.chargeDistance(300)
			.linkStrength(0.5)
			.friction(0.8)
			.linkDistance(60)
			.gravity(0.2)
			.size([width, height])
			.on("tick", tick);

		var svg = d3.select(id).append("svg")
			.attr("width", width)
			.attr("height", height);
	
		var node = svg.selectAll(".node");
		var link = svg.selectAll(".link");
		var text = svg.selectAll(".node-text");
		
		this.getColors = function() { return color.domain(); }
		this.getNodes = function() { return nodes; }
		this.getLinks = function() { return links; }
		this.getLinkSigMap = function() { return linkSigMap; }
		this.getForce = function() { return force; }
		
		/**
		 *  Clone the maps, so we can tell what remaining nodes/links need to be deleted after updating the graph (if any).
		 */
		/*this.cloneMaps = function() {
			prevNodeNameMap = _.clone(nodeNameMap);
			prevLinkSigMap = _.clone(linkSigMap);
		}*/
		
		/**
		 * Zoom into the graph by clustering the double-clicked cluster (group).
		 */
		/*this.zoom = function(group) {
			that.clearLinks();
			nodeNameMap = {};
			
			for(var i = nodes.length-1; i >= 0; i--) {
				if(nodes[i].group != group) {
					nodes.splice(i, 1);
				}
			}
			
			for(var i = nodes.length-1; i >= 0; i--) {
				nodeNameMap[nodes[i].name] = i;
				console.log("zoom: adding to nodeNameMap: " + nodes[i].name);
			}
			
			//legend.remove();
			
			that.start();
			
			console.log("zoom: Remaining nodes #: " + nodes.length);
			console.log("zoom: Remaining links #: " + links.length);
			
			$rootScope.$broadcast('graphZoom', {
				  group: group,
			});
		}*/
		
		/**
		 * Add a node to the graph if not there yet. Return the new or old node.
		 */
		this.addNode = function(pNode) {
			var nd = nodeNameMap[pNode.name];
			
			if(!nd) {
				nodes.push(pNode);
				nodeNameMap[pNode.name] = pNode;
				nodeNameMap[pNode.name].index = nodes.length-1;
				
				return nodeNameMap[pNode.name].index;
			} else {
				nd.group = pNode.group;
				return nd.index;
			}
		}
		
		/** 
		 * Adds a link to the graph.
		 */
		this.addLink = function(sourceIndex, targetIndex, value) {
			var sig = nodes[sourceIndex].name + "-" + nodes[targetIndex].name;
			var ln = linkSigMap[sig];

			if(!ln) {
				var pLink = { source: sourceIndex, target: targetIndex, value: value };
				
				links.push(pLink);
				linkSigMap[sig] = pLink;
				nodeNameMap[nodes[sourceIndex].name].degree++;
				nodeNameMap[nodes[targetIndex].name].degree++;
			} else {
				ln.value = value;
			}
		}

		/** 
		 * Set a node to a group given the name node.
		 */
		this.setGroup = function(name, group) {
			nodeNameMap[name].group = group;
		}

		/** 
		 * Removes a node given a name.
		 */
		this.removeNode = function(name) {
			var nd = nodeNameMap[name];
			
			if(nd) {
				nodes.splice(nd.index, 1);
				delete(nodeNameMap[name]);
			}
		}
		
		/** 
		 * Removes a link given a signature.
		 */
		this.removeLink = function(sig) {
			var ln = linkSigMap[sig];
			
			if(ln) {
				links.splice(ln.index, 1);
				delete(linkSigMap[sig]);
			}
		}
		
		/** 
		 * Removes all nodes and their links belonging to a certain group.
		 */
		this.removeGroup = function(group) {
			for(var i = nodes.length-1; i >= 0; i--) {
				if(nodes[i].group === group) {
					var nm = nodes[i].name;
					that.removeNodeLinks(nm);
					that.removeNode(nodes[i].name);
				}
			}
		}
		
		/** 
		 * Removes all links connected to a certain node.
		 */
		this.removeNodeLinks = function(name) {
			for(var i = links.length-1; i >= 0; i--) {
				if(links[i].source.name === name || links[i].target.name === name) {
					links[i].source.degree--;
					links[i].target.degree--;
					var sig = links[i].source.name + "-" + links[i].target.name;
					links.splice(i, 1);
					delete(linkSigMap[sig]);
				}
			}
		}
		
		/** 
		 * Removes all nodes and links from the graph.
		 */
		this.clearNodes = function() {
			while(nodes.length > 0) nodes.pop();
			nodeNameMap = {};
		}
		
		/** 
		 * Removes all nodes and links from the graph.
		 */
		this.clearLinks = function() {
			while(links.length > 0) links.pop();
			linkSigMap = {};
		}
		
		/** 
		 * Removes all nodes and links from the graph.
		 */
		this.clearGraph = function() {
			that.clearNodes();
			that.clearLinks();
		}
		
		/** 
		 * Adds a cluster (given nodes and links) to the graph and assigns it a group number.
		 */
		this.addCluster = function(pNodes, pLinks, group) {
			_.each(pLinks, function(pLink) {
				pLink.source.group = group;
				pLink.target.group = group;
				that.addNode(pLink.source);
				that.addNode(pLink.target);
				that.addLink(pLink.source.index, pLink.target.index, pLink.value);
			});
		}
		
		/*this.trim = function() {
			var linkKeys = Object.keys(prevLinkSigMap);
			var nodeKeys = Object.keys(prevNodeNameMap);
			
			_.each(linkKeys, function(sig) {
				that.removeLink(sig);
			});
			
			_.each(nodeKeys, function(name) {
				that.removeNode(name);
			});
		}*/
		
		this.start = function() {
			
			link = link.data(force.links(), function(d) { return d.value; });
			link.enter().insert("line", ".node").attr("class", "link");
			link.exit().remove();
			link.style("stroke-width", function(d) { var width = (Math.pow(d.value*3, 2)); return (width < 0.5) ? 0.5 : (width > 3) ? 3 : width; });
			
			node = node.data(force.nodes(), function(d) {  return d.name; } );
			node.enter().append("circle").attr("class", function(d) { return "node " + d.name; }).attr("r", 9);
			node.exit().remove();
			node.style("fill", function(d) { return color(d.group); });
			node.call(force.drag);
			//node.on("dblclick",function(d){ that.zoom(d.group); });

			text = text.data(force.nodes());
			text.enter().append("text");
			text.exit().remove();
			text.attr("x", 9);
			text.attr("y", ".31em");
			text.attr("style", "font-weight: 300; font-size: 14px;");
			text.text(function(d) { return "@" + d.name; });
			
			force.start();
		}
		
		function tick() {
			node.attr("cx", function(d) { return d.x; })
				.attr("cy", function(d) { return d.y; })

			/*link.attr("x1", function(d) { return d.source.x; })
				.attr("y1", function(d) { return d.source.y; })
				.attr("x2", function(d) { return d.target.x; })
				.attr("y2", function(d) { return d.target.y; });*/

			text.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
		}
	}
	
	/**
   * Return the constructor function
   */
	return Graph;
}]);