/* D3 graph stuff */
var width = 1280, height = 960;
var color = d3.scale.category10();
var nodes = []; // { name: "", group: 0, userIndex: 0 }
var nodeNameMap = {};
var links = []; // { source: 0, target: 1, value: 0.00 }

var force = d3.layout.force()
	.nodes(nodes)
	.links(links)
	.charge(-240)
	.linkDistance(240)
	.size([width, height])
	.on("tick", tick);

var svg = d3.select("#graph").append("svg")
	.attr("width", width)
	.attr("height", height);

var node = svg.selectAll(".node");
var link = svg.selectAll(".link");
var text = svg.selectAll(".node-text");
var legend = svg.selectAll(".legend");


/* Adds a node to the graph if not there yet.
 * Returns the index of the new or existing node. */
function addNode(pNode) {
	var index = nodeNameMap[pNode.name];
	if(index != null) return index;
	nodes.push(pNode);
	index = nodes.length-1;
	nodeNameMap[pNode.name] = index;
	return index;
}

/* Adds a link to the graph. */
var addLink = function(pLink) {
	links.push(pLink);
}

function setGroup(index, group) {
	nodes[index].group = group;
}

function removeNodeByIndex(index) {
	nodes.splice(index, 1);
	return nodes.length-1;
}

function removeNodeLinks(nodeIndex) {
	for(var i = links.length-1; i >= 0; i--) {
		if(links[i].source.index === nodeIndex || links[i].target.index === nodeIndex) {
			links.splice(i, 1);
		}
	}
}

function start(scopeLegend) {
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

	legend = legend.data(color.domain())
		.enter().append("g")
		.attr("class", "legend")
		.attr("transform", function(d, i) { return "translate(0," + i * 20 + ")"; });

	legend.append("rect")
		.attr("x", width - 136)
		.attr("width", 18)
		.attr("height", 18)
		.style("fill", color);

	if(scopeLegend) {
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
		if(o.group % 4 == 0) {
			o.x += o.group*k;
		} else if(o.group % 3 == 0) {
			o.y += o.group*k;
		} else if(o.group % 2 == 0) {
			o.x += -o.group*k;
		} else {
			o.y += -o.group*k;
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