var twitterWebController = angular.module('twitterWeb.controller', [])

.controller('TwitterController', ['$scope', '$timeout', 'TwitterUser', 'UserTweets', 'UserOntology', 'UserNetwork',
                                  function($scope, $timeout, TwitterUser, UserTweets, UserOntology, UserNetwork) {
	
	$scope.loadingUser = false;
	$scope.noUserFound = false;
	$scope.loadingTweets = false;
	$scope.loadingOntology = false;
	$scope.loadingNetwork = false;
	$scope.targetUser = null;
	$scope.screenName = "bnmuller";
	$scope.pageSize = 0;
	$scope.N = 0;
	$scope.processing = false;
	$scope.finished = false;
	$scope.showSimilarityGraph = false;
	$scope.loadingSimilarityGraph = false;
	$scope.finishedSimilarityGraph = false;
	$scope.clusteringNetwork = false;
	$scope.concatenation = 50;
	$scope.relevanceScores = {};
	$scope.legend = {};
	
	var vertices = [];
	var edges = [];
	
	$scope.groups = {};
	$scope.cfMap = {};
	$scope.groupCFMap = {};
	$scope.minimumSimilarity = 0.2;
	
	$scope.yagoTypeBlackList = ["Abstraction", "Group", "YagoLegalActorGeo", "YagoPermanentlyLocatedEntity", 
	                            "PhysicalEntity", "Object", "YagoGeoEntity", "YagoLegalActor", "Whole", "Region", "Musician"];
	
	$scope.userChanged = function() {
		if(!$scope.screenName || !$scope.targetUser || !$scope.targetUser.screenName) {
			return false;
		}
		return $scope.screenName.toLowerCase() != $scope.targetUser.screenName.toLowerCase();
	}
	
	var width = 1280,
	height = 960;
	
	var color = d3.scale.category10();
	
	var nodes = [];
	var links = [];
	
	var force = d3.layout.force()
		.nodes(nodes)
		.links(links)
		.charge(-180)
		.linkDistance(180)
		.size([width, height])
		.on("tick", tick);
	
	var svg = d3.select("#graph").append("svg")
		.attr("width", width)
		.attr("height", height)
		//.attr("viewBox", "0 0 500 500");
	
	var node = svg.selectAll(".node");
	var link = svg.selectAll(".link");
	var text = svg.selectAll(".node-text");
	var legend = svg.selectAll(".legend");
	
	function hasNode(node) {
		for(var i = 0; i < nodes.length; i++) {
			if(node.name == nodes[i].name) {
				return i;
			}
		}
		
		return -1;
	}
	
	function addNode(pNode) {
		nodes.push(pNode);
		return nodes.length-1;
	}
	
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
			//console.log("hello is" + JSON.stringify(links[i].source) + " ni" + nodeIndex);
			
			if(links[i].source.index == nodeIndex || links[i].target.index == nodeIndex) {
				
				links.splice(i, 1);
			}
		}
	}
	
	function start() {
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

	  	legend.append("text")
			.attr("x", width - 140)
			.attr("y", 9)
			.attr("dy", ".35em")
			.style("text-anchor", "end")
			.style("font-size","12px")
			.text(function(d) { return $scope.legend[d]; });
		
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
	
	var getTweetStats = function(user) {
		var retweets = 0, hashtags = {}, mentions = {};
		
		_.each(user.tweets, function(tweet) {
			_.each(tweet.hashtags, function(tag) {
				hashtags[tag] = hashtags[tag] ? hashtags[tag]+1 : 1;
			});
			
			_.each(tweet.userMentions, function(men) {
				mentions[men] = mentions[men] ? mentions[men]+1 : 1;
			});
			
			if(tweet.retweet) retweets++;
		});
		
		var htList = [];
		mList = [];
		
		for(key in hashtags) htList.push({ tag: key, occ: hashtags[key] });
		for(key in mentions) mList.push({ mention: key, occ: mentions[key] });
		
		user.retweets = retweets;
		user.hashtags = htList;
		user.userMentions = mList;
	}
	
	var sortCFIUF = function(userCFIUFMap, euclidLength) {
		var tuples = [];

		for (var key in userCFIUFMap) {
			var cfIufNorm = (euclidLength == 0) ? 0 : userCFIUFMap[key] / euclidLength;
			tuples.push([ key, cfIufNorm ]);
		}

		tuples.sort(function(a, b) {
		    a = a[1]; b = b[1];
		    return a > b ? -1 : (a < b ? 1 : 0);
		});

		return tuples;
	}
	
	var updateCFMap = function(user) {
		
		for(type in user.ontology.yagotypes) {
			
			keyStr = type.split(":")[1];
			var wordnetCode = keyStr.substr(keyStr.length - 9);
			if(!isNaN(wordnetCode)) {
				keyStr = keyStr.substr(0, keyStr.length - 9);
			}
			
			user.ontology.sortedYagotypes.push([keyStr, user.ontology.yagotypes[type]]);
			$scope.cfMap[keyStr] = $scope.cfMap[keyStr] ? $scope.cfMap[keyStr] + 1 : 1;
			if($scope.cfMap[keyStr] > $scope.N) $scope.cfMap[keyStr] = $scope.N;
		}
	}
	
	var updateCFIUF = function(index, user) {
		
		//$scope.N = index + 2;

		var userCFIUFMap = {};

		for (var i = -1; i <= index; i++) {
			userCFIUFMap = {};
			var cfIufSum = 0;
			var currentUser = (i == -1) ? $scope.targetUser : $scope.targetUser.network[i];
			var types = currentUser.ontology.sortedYagotypes;

			for(var j = 0; j < types.length; j++) {
				var type = types[j];
				var cf = type[1];
				var iuf = Math.log($scope.N / $scope.cfMap[type[0]]);
				var cfIuf = (Math.pow(cf, 1 + 0))*(Math.pow(iuf, 1 - 0));
				
				//console.log("N: " + $scope.N + ", global cf: " + $scope.cfMap[type[0]]);
				
				
				cfIufSum += Math.pow(cfIuf, 2);
				
				//console.log("cf: " + cf + ", iuf: " + iuf + ", cfiuf: " + cfIuf + ", cfiufSum: " + cfIufSum);
				
				userCFIUFMap[type[0]] = cfIuf;
				//currentUser.ontology.cfIufMap[type[0]] = cfIuf;
			}
			
			var euclidLength = Math.sqrt(cfIufSum);
			
			if(currentUser.ontology.cfIufMap) {
				var newMap = sortCFIUF(userCFIUFMap, euclidLength);
				
				for(var j = 0; j < newMap.length; j++) {
					if(currentUser.ontology.cfIufMap[j][0] != newMap[j][0] ) {
						currentUser.ontology.cfIufMap[j] = newMap[j];
					} else {
						var a = Math.round(currentUser.ontology.cfIufMap[j][1] * 100) / 100;
						var b = Math.round(newMap[j][1] * 100) / 100;
						if(a != b) currentUser.ontology.cfIufMap[j] = newMap[j];
					}
				}
			} else {
				currentUser.ontology.cfIufMap = sortCFIUF(userCFIUFMap, euclidLength);
			}
		}
	}
	
	$scope.calculateSimilarityGraph = function() {
		
		$scope.showSimilarityGraph = true;
		$scope.loadingSimilarityGraph = true;
		
		var worker = new Worker("js/similarity_worker.js");

		worker.addEventListener('message', function(e) {
			if(e.data.finished) {
				$scope.$apply(function () {
					$scope.loadingSimilarityGraph = false;
					$scope.finishedSimilarityGraph = true;
					$scope.calculateDCG();
					$scope.clusterNetwork();
		        });
				
				console.log("Similarity worker terminating!")
				worker.terminate();
			} else {
				var aName = (e.data.i == -1) ? $scope.targetUser.screenName : $scope.targetUser.network[e.data.i].screenName;
				var bName = $scope.targetUser.network[e.data.j].screenName;

				var aNode = { name: aName, group: 1, userIndex: e.data.i };
				var bNode = { name: bName, group: 1, userIndex: e.data.j };

				var aIndex = hasNode(aNode), bIndex = hasNode(bNode); 

				if(aIndex == -1) {
					aIndex = addNode(aNode);
					vertices[aIndex] = aNode;
				}

				if(bIndex == -1) {
					bIndex = addNode(bNode);
					vertices[bIndex] = bNode;
				}

				addLink({ source: aIndex, target: bIndex, value: e.data.similarity});
				edges.push({ source: aIndex, target: bIndex, value: e.data.similarity});
				
				start();
			}		
		}, false);
		
		var ev = {};
		ev.N = $scope.N;
		ev.minSim = $scope.minimumSimilarity;
		ev.targetUser = $scope.targetUser;
		ev.network = $scope.targetUser.network;
		
		worker.postMessage(ev);
	}
	
	$scope.calculateDCG = function() {
		var dcgEdges = [];
		_.each(edges, function(edge) {
			if(edge.source == 0) {
				dcgEdges.push(edge);
			}
		});
		
		var sortedDCGEdges = _.map(_.sortBy(dcgEdges, 'value'), _.values);
		
		var rankingStr = "Similarity ranking (" + sortedDCGEdges.length + " entries):\n";
		for(var i = sortedDCGEdges.length; i--;) {
			rankingStr += vertices[sortedDCGEdges[i][1]].name + "\t" + $scope.relevanceScores[vertices[sortedDCGEdges[i][1]].name] + "\t" + sortedDCGEdges[i][2] + "\n";
		}
		
		console.log(rankingStr);
		var firstEdge = sortedDCGEdges[sortedDCGEdges.length-1];
		var topK = [3, 5, 10, 25, 50];
		var evaluationString = "";
		
		_.each(topK, function(k) {
			if(k <= sortedDCGEdges.length) {
			
				var reli = 0.0;
				var binreli = 0.0;
				var dcg = parseFloat($scope.relevanceScores[vertices[firstEdge[1]].name]);
				var binDCG = (parseFloat($scope.relevanceScores[vertices[firstEdge[1]].name]) > 0) ? 2 : 0;
				var idcg = 2;
				var index = 1;
				
				for(var i = sortedDCGEdges.length-1; i--;) {
					var edge = sortedDCGEdges[i];
					
					if(index < k) {
						reli = parseFloat($scope.relevanceScores[vertices[edge[1]].name]);
						binreli = (parseFloat($scope.relevanceScores[vertices[edge[1]].name]) > 0) ? 2 : 0;
						
						dcg 	+= reli * 	Math.log(2) / Math.log(index+1);
						binDCG 	+= binreli * 	Math.log(2) / Math.log(index+1);
						idcg 	+= 2 	* 	Math.log(2) / Math.log(index+1);
						index++;
					}
				}
				
				var nDCG = dcg / idcg;
				var binnDCG = binDCG / idcg;
				
				evaluationString += "Top-" + k + "\t" + nDCG + "\t" + binnDCG + "\n";
			}
		});
		
		console.log(evaluationString);
		
	}
	
	$scope.clusterNetwork = function() {
		$scope.clusteringNetwork = true;
		
		var worker = new Worker("js/clustering_worker.js");
		console.log("Starting clustering worker from controller!");

		worker.addEventListener('message', function(e) {
			if(e.data.finished) {
				$scope.$apply(function () {
					$scope.clusteringNetwork = false;
				});
			}
			
			//console.log(JSON.stringify(e.data));
			var includedNodes = {};

			for(var i = 0; i < e.data.clusters.length; i++) {
				var clusterSplit = e.data.clusters[i].split('-');
				$scope.groups[i+1] = {};
				$scope.groups[i+1].users = [];
				$scope.groups[i+1].sortedYagotypes = [];
				var group = $scope.groups[i+1];
				
				for(var j = 0; j < clusterSplit.length; j++) {
					
					includedNodes[clusterSplit[j]] = true;
					setGroup(clusterSplit[j], i+1);
					var user;
					if(nodes[clusterSplit[j]].userIndex == -1) {
						user = $scope.targetUser;
					} else {
						user = $scope.targetUser.network[nodes[clusterSplit[j]].userIndex];
					}
					
					group.users.push(user);
					
					for(var k = 0; k < user.ontology.sortedYagotypes.length; k++) {
						
						var added = false;
						
						for(var l = 0; l < group.sortedYagotypes.length; l++) {
							if(user.ontology.sortedYagotypes[k][0] == group.sortedYagotypes[l][0]) {
								group.sortedYagotypes[l][1] += user.ontology.sortedYagotypes[k][1];
								added = true;
								break;
							}
						}
						
						if(!added) {
							group.sortedYagotypes.push(user.ontology.sortedYagotypes[k]);
							$scope.groupCFMap[user.ontology.sortedYagotypes[k][0]] = $scope.groupCFMap[user.ontology.sortedYagotypes[k][0]] ? $scope.groupCFMap[user.ontology.sortedYagotypes[k][0]] + 1 : 1;
						}
					}
				}
			}

			for(var i = nodes.length-1; i >= 0; i--) {
				if(!includedNodes[i]) {
					removeNodeLinks(i);
					removeNodeByIndex(i);
				}
			}
			
			var groupCFIUFMap = {};
			
			for(var i = 1; i <= e.data.clusters.length; i++) {
				var currentGroup = $scope.groups[i];
				groupCFIUFMap = {};
				var cfIufSum = 0;
				var types = currentGroup.sortedYagotypes;

				for(var j = 0; j < types.length; j++) {
					var type = types[j];
					var cf = type[1];
					var iuf = Math.log(Object.keys($scope.groups).length / $scope.groupCFMap[type[0]]);
					var cfIuf = (Math.pow(cf, 1 + 0))*(Math.pow(iuf, 1 - 0));
					
					cfIufSum += Math.pow(cfIuf, 2);
					
					groupCFIUFMap[type[0]] = cfIuf;
				}
				
				var euclidLength = Math.sqrt(cfIufSum);
				
				currentGroup.cfIufMap = sortCFIUF(groupCFIUFMap, euclidLength);
				var legendText = currentGroup.cfIufMap[0][0];
				
				for(var j = 1; j < 5; j++) {
					legendText += ", " + currentGroup.cfIufMap[j][0];
				}
				
				$scope.legend[i] = legendText;
			}
			
			//force.charge(-60);
			force.linkDistance(60);

			start();

			console.log("Clustering worker terminating!");
			worker.terminate();
		
		}, false);
		
		var message = {};
		message.nodes = vertices;
		message.links = edges;
		
		worker.postMessage(message);
	}
	
	$scope.getUser = function() {
		$scope.cfMap = {};
		$scope.processing = false;
		$scope.finished = false;
		$scope.showSimilarityGraph = false;
		
		//while(nodes.length > 0) nodes.pop();
		//while(links.length > 0) links.pop();
		
		//start();
		
		$scope.noUserFound = false;
		$scope.targetUser = null;
		$scope.loadingUser = true;

		$scope.targetUser = TwitterUser.user({ name: $scope.screenName }, function() {
			if($scope.targetUser.user) {
				$scope.targetUser = $scope.targetUser.user;
				$scope.targetUser.profileImageURL = $scope.targetUser.profileImageURL;
				$scope.N = 1;
				
				// Cancel old timeout.
			    if ($scope.targetUser.timeoutId) {
			        $timeout.cancel($scope.targetUser.timeoutId);
			    }

			    // Hide images to start with (to prevent overlap).
			    $scope.targetUser.imageVisible = false;

			    $scope.targetUser.timeoutId = $timeout(function() {
			        $scope.targetUser.imageVisible = true;
			        $scope.targetUser.timeoutId = undefined;
			    }, 10);
				
				if($scope.targetUser.tweets.length > 0) {
					getTweetStats($scope.targetUser);
					//$scope.targetUser.tweets = null;
					$scope.getOntology($scope.targetUser.screenName);
				} else {
					$scope.getTweets($scope.targetUser.screenName);
				}
			} else {
				$scope.noUserFound = true;
				$scope.targetUser = null;
			}
			
			$scope.loadingUser = false;
		});
	};
	
	$scope.getTweets = function(name) {
		$scope.targetUser.loadingTweets = true;
		$scope.targetUser.tweets = UserTweets.tweets({ name: name }, function() {
			$scope.targetUser.englishRate = $scope.targetUser.tweets.englishRate;
			$scope.targetUser.tweets = $scope.targetUser.tweets.tweets;
			
			getTweetStats($scope.targetUser);
			//$scope.targetUser.tweets = null;
			$scope.getOntology($scope.targetUser.screenName);
			
			$scope.targetUser.loadingTweets = false;
		});
	};
	
	$scope.getOntology = function(name) {
		$scope.targetUser.loadingOntology = true;
		$scope.targetUser.ontology = UserOntology.ontology({ name: name, concatenation: $scope.concatenation }, function() {
			$scope.targetUser.ontology = $scope.targetUser.ontology.user.userOntology;

			//$scope.targetUser.ontology.sortedYagotypes = sortYagoTypes($scope.targetUser.ontology.yagotypes);
			$scope.targetUser.ontology.sortedYagotypes = [];
			//$scope.nodes.push( { name: $scope.targetUser.screenName, group: 1 } );
			//addNode( { name: $scope.targetUser.screenName, group: 1 } );
			//start();
			
			updateCFMap($scope.targetUser);
			updateCFIUF(-1, $scope.targetUser);
			
			$scope.targetUser.loadingOntology = false;
		});
	};
	
	$scope.getTargetUserNetwork = function(name) {
		$scope.loadingNetwork = true;
		$scope.targetUser.network = UserNetwork.network({ name: name }, function() {
			$scope.targetUser.network = $scope.targetUser.network.network;
			$scope.loadingNetwork = false;
			$scope.pageSize = 10;
			$scope.targetUser.network.shift();
		});
	};
	
	$scope.getDCGNetwork = function(userRelevanceList) {
		$scope.loadingNetwork = true;
		$scope.targetUser.network = UserNetwork.network({ list: userRelevanceList }, function() {
			$scope.targetUser.network = $scope.targetUser.network.network;
			$scope.loadingNetwork = false;
			$scope.pageSize = 10;
			$scope.targetUser.network.shift();
		});
	};
	
	$scope.processNetwork = function(pIndex) {
		
		$scope.processing = true;
		var index = parseInt(pIndex);

		$scope.targetUser.network[index].loadingTweets = true;
		$scope.targetUser.network[index].tweets = UserTweets.tweets({ name: $scope.targetUser.network[index].screenName }, function() {
			
			$scope.targetUser.network[index].englishRate = $scope.targetUser.network[index].tweets.englishRate;
			$scope.targetUser.network[index].tweets = $scope.targetUser.network[index].tweets.tweets;
			
			getTweetStats($scope.targetUser.network[index]);
			
			//$scope.targetUser.network[index].tweets = null;
			
			$scope.targetUser.network[index].loadingTweets = false;
			$scope.targetUser.network[index].loadingOntology = true;
			$scope.targetUser.network[index].ontology = UserOntology.ontology({ name: $scope.targetUser.network[index].screenName }, function() {
				
				$scope.targetUser.network[index].ontology = $scope.targetUser.network[index].ontology.user.userOntology;
				//$scope.targetUser.network[index].ontology.sortedYagotypes = sortYagoTypes($scope.targetUser.network[index].ontology.yagotypes);
				$scope.targetUser.network[index].ontology.sortedYagotypes = [];
				$scope.N += 1;
				
				$scope.targetUser.network[index].loadingOntology = false;
				
				//addNode( { name: $scope.targetUser.network[index].screenName, group: 1 } );
				
				updateCFMap($scope.targetUser.network[index]);
				
				//console.log(JSON.stringify($scope.targetUser.network[index]));
				
				if(index % 100 == 0) {
					updateCFIUF(index, $scope.targetUser.network[index]);
				}
				
				index += 1;
				
				if(index < $scope.targetUser.network.length) {
					$scope.processNetwork(index);
				} else {
					if((index-1) % 100 != 0) {
						updateCFIUF(index-1, $scope.targetUser.network[index-1]);
					}
					$scope.finished = true;
				}
			});
			
		});
	}
	
	$scope.loadMore = function() {
		$scope.pageSize = $scope.pageSize + 10;
	}
	
	var fileInput = $('#files');
	var uploadButton = $('#upload');

	// Function to upload a DCG file.
	uploadButton.on('click', function() {
	    if (!window.FileReader) {
	        alert("Your browser is not supported.");
	        return false;
	    }
	    
	    var input = fileInput.get(0);
	    var reader = new FileReader();
	    
	    if (input.files.length) {
	        var textFile = input.files[0];
	        reader.readAsText(textFile);
	        
	        $(reader).on('load', processFile);
	    } else {
	        alert("Please upload a file before continuing.");
	    } 
	});

	function processFile(e) {
	    var file = e.target.result;
	    var results;
	    var csvList = "";
	    
	    if (file && file.length) {
	        results = file.split("\n");
	        
	        _.each(results, function(result) {
	        	var res = result.split("\t");
	        	csvList += res[0] + ",";
	        	$scope.relevanceScores[res[0]] = res[1];
	        });
	        
	        $scope.getDCGNetwork(csvList.substring(0, csvList.length - 1));
	    }
	}
}]);

