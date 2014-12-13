var twitterWebController = angular.module('twitterWeb.controller', [])

.controller('TwitterController', ['$scope', '$timeout', 'TwitterUser', 'UserTweets', 'UserOntology', 'UserNetwork', 'SimilarityService', 
                                  'HCSService', 'EvaluationService', 'LDAService', 'Graph',
                                  function($scope, $timeout, TwitterUser, UserTweets, UserOntology, UserNetwork, SimilarityService, 
                                		  	HCSService, EvaluationService, LDAService, Graph) {
	
	$scope.loadingUser = false;
	$scope.noUserFound = false;
	$scope.loadingOntology = false;
	$scope.loadingNetwork = false;
	$scope.processing = false;
	$scope.finished = false;
	$scope.showSimilarityGraph = false;
	$scope.loadingSimilarityGraph = false;
	$scope.finishedSimilarityGraph = false;
	$scope.clusteringNetwork = false;
	
	$scope.users = [];
	$scope.screenName = "bnmuller";
	$scope.pageSize = 0;
	$scope.refreshCnt = 0;
	
	$scope.ldaTopics = 3;
	
	$scope.generalityBias = 0;
	$scope.concatenation = 50;
	$scope.minimumSimilarity = 0.2;
	
	var graph = new Graph(1280, 960, "#graph");
	$scope.legend = {};
	//var vertices = [];
	//var edges = [];
	
	$scope.groups = {};
	$scope.cfMap = {};
	$scope.groupCFMap = {};
	
	$scope.yagoTypeBlackList = {"Abstraction": true, "Group": true, "YagoLegalActorGeo": true, "YagoPermanentlyLocatedEntity": true, "PhysicalEntity": true,
								"Object": true, "YagoGeoEntity": true, "YagoLegalActor": true, "Whole": true, "Region": true, "Musician": true };
	
	/* Function to check if the active user has changed (exact name no longer in input box). */
	$scope.userChanged = function() {
		if(!$scope.screenName || $scope.users.length === 0 || !$scope.users[0].screenName) return false;
		return $scope.screenName.toLowerCase() != $scope.users[0].screenName.toLowerCase();
	}
	
	/* Extract retweets, hashtags and mentions from a user's tweets. */
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
	
	var normalizeCFIUF = function(user, userCFIUFMap, euclidLength) {
		var map = {};
		var tuples = [];

		for (var key in userCFIUFMap) {
			var cfIufNorm = (euclidLength == 0) ? 0 : userCFIUFMap[key] / euclidLength;
			tuples.push([ key, cfIufNorm ]);
			map[key] = cfIufNorm;
		}

		tuples.sort(function(a, b) {
		    a = a[1]; b = b[1];
		    return a > b ? -1 : (a < b ? 1 : 0);
		});
		
		user.topTypes = [];
		
		for(var i = 0; i < 10; i++) {
			user.topTypes.push(tuples[i]);
		}

		return map;
	}
	
	var updateCFMap = function(user) {
		
		for(type in user.ontology.yagotypes) {
			// TODO: should be a display thing!
			keyStr = type.split(":")[1];
			var wordnetCode = keyStr.substr(keyStr.length - 9);
			if(!isNaN(wordnetCode)) {
				keyStr = keyStr.substr(0, keyStr.length - 9);
			}
			
			user.ontology.sortedYagotypes.push([keyStr, user.ontology.yagotypes[type]]);
			$scope.cfMap[keyStr] = $scope.cfMap[keyStr] ? $scope.cfMap[keyStr] + 1 : 1;
			if($scope.cfMap[keyStr] > $scope.users.length) $scope.cfMap[keyStr] = $scope.users.length;
		}
	}
	
	var updateCFIUF = function(index) {
		var userCFIUFMap = {};
		
		for(var i = 0; i <= index; i++) {
			var currentUser = $scope.users[i];
			
			userCFIUFMap = {};
			var cfIufSum = 0;
			
			_.each(currentUser.ontology.sortedYagotypes, function(type) {
				var cf = type[1];
				var iuf = Math.log($scope.users.length / $scope.cfMap[type[0]]);
				var cfIuf = (Math.pow(cf, 1 + parseFloat($scope.generalityBias)))*(Math.pow(iuf, 1 - parseFloat($scope.generalityBias)));
				
				cfIufSum += Math.pow(cfIuf, 2);
				userCFIUFMap[type[0]] = cfIuf;
			});
			
			var euclidLength = Math.sqrt(cfIufSum);
			
			if(currentUser.ontology.cfIufMap) {
				var newMap = normalizeCFIUF(currentUser, userCFIUFMap, euclidLength);
				
				for(var key in newMap) {
					if(!currentUser.ontology.cfIufMap[key]) {
						currentUser.ontology.cfIufMap[key] = newMap[key];
					} else {
						var a = Math.round(currentUser.ontology.cfIufMap[key] * 100) / 100;
						var b = Math.round(newMap[key] * 100) / 100;
						if(a != b) currentUser.ontology.cfIufMap[key] = newMap[key];
					}
				}
				
			} else {
				
				currentUser.ontology.cfIufMap = normalizeCFIUF(currentUser, userCFIUFMap, euclidLength);
			}
		}
	}
	
	/**
	 * Function to start similarity graph calculation and visualization.
	 */
	$scope.calculateSimilarityGraph = function() {
		
		$scope.showSimilarityGraph = true;
		$scope.loadingSimilarityGraph = true;
		$scope.finishedSimilarityGraph = false;
		
		$scope.refreshCnt = 0;
		
		var ev = {};
		ev.minSim = $scope.minimumSimilarity;
		ev.users = $scope.users;
		
		SimilarityService.doWork(ev).then(function(data) {
			
			// We've finished calculating the similarity graph. Render the final version.
			graph.start();
			$scope.loadingSimilarityGraph = false;
			$scope.finishedSimilarityGraph = true;
			
			// Calculate the DCG scores for this network (if available).
			if(EvaluationService.getRelevanceScores()) EvaluationService.dcg(graph.getNodes(), graph.getLinks());
			
			// Finally, cluster the similarity graph.
			$scope.clusterNetwork();
		});
	}
	
	/**
	 * Function to catch updates and push them to the similarity graph.
	 */
	$scope.$on('simGraphUpdate', function(event, data) {
		// Create the new, ungrouped nodes.
		var aNode = { name: $scope.users[data.i].screenName, group: 0, userIndex: data.i };
		var bNode = { name: $scope.users[data.j].screenName, group: 0, userIndex: data.j };
		
		// Add the nodes (if not yet existing) and get their index.
		var aIndex = graph.addNode(aNode);
		var bIndex = graph.addNode(bNode);
	
		// Add the link between the nodes.
		graph.addLink({ source: aIndex, target: bIndex, value: data.similarity});
		
		// Refresh SVG graph only one in 25 updates, to make rendering smoother.
		$scope.refreshCnt++;
		//graph.start();
		if($scope.refreshCnt % 25 == 0) graph.start();
	});
	
	$scope.clusterNetwork = function() {
		$scope.clusteringNetwork = true;
		
		var e = {};
		e.nodes = _.cloneDeep(graph.getNodes());
		e.links = _.cloneDeep(graph.getLinks());
		
		_.each(e.nodes, function(node) {
			_.pick(node, ['name', 'group', 'userIndex']);
		});
		
		_.each(e.links, function(link) {
			link.source = _.pick(link.source, ['name', 'group', 'userIndex']);
			link.target = _.pick(link.target, ['name', 'group', 'userIndex']);
			_.pick(link, ['source', 'target', 'value']);
		});
		
		HCSService.doWork(e).then(function(data) {
			
			// We've finished calculating the cluster graph. Render the final version.
			$scope.clusteringNetwork = false;
			
			// First, clear the graph in its entirety.
			graph.clearGraph();

			// Add the clusters to the graph and group users accordingly.
			_.each(data.clusters, function(cluster, i) {
				var clusterNodes = cluster.nodes;
				var clusterEdges = cluster.edges;
				var groupIndex = i+1;
				
				var group = $scope.groups[groupIndex];
				group.users = [];
				group.sortedYagotypes = [];
				
				_.each(clusterNodes, function(node) {
					var user = $scope.users[node.userIndex];
					group.users.push(user);
					group.sortedYagotypes.concat(user.ontology.sortedYagotypes);
				});
				
				
				graph.addCluster(clusterNodes, clusterEdges, groupIndex);
			});
			
			// Update force settings and refresh.
			graph.getForce().charge(-120).linkDistance(90);
			graph.start();

			
			
			
			
			/*var includedNodes = {};
			var evalClusters = [];

			for(var i = 0; i < data.clusters.length; i++) {
				var clusterNodes = data.clusters[i].cluster;
				
				$scope.groups[i+1] = {};
				$scope.groups[i+1].users = [];
				$scope.groups[i+1].sortedYagotypes = [];
				
				var group = $scope.groups[i+1];
				var evalGroup = [];
				
				for(var j = 0; j < clusterNodes.length; j++) {
					var node = clusterNodes[j];
					includedNodes[node] = true;
					graph.setGroup(node, i+1);
					
					var user = $scope.users[graph.getNodes()[node].userIndex];
					group.users.push(user);
					evalGroup.push(user.screenName);
					
					
					for(var k = 0; k < user.ontology.sortedYagotypes.length; k++) {
						
						var added = false;
						
						for(var l = 0; l < group.sortedYagotypes.length; l++) {
							if(group.sortedYagotypes[l][0] == user.ontology.sortedYagotypes[k][0]) {
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
				
				evalClusters.push(evalGroup);
			}
			
			var i = graph.getNodes().length;
			while(i--) {
				if(!includedNodes[i]) {
					graph.removeNodeLinks(i);
					graph.removeNodeByIndex(i);
				}
			}
			
			var groupCFIUFMap = {};
			
			var labelString = "\nTopic labels:\n\n";
			
			for(var i = 1; i <= data.clusters.length; i++) {
				var currentGroup = $scope.groups[i];
				groupCFIUFMap = {};
				var cfIufSum = 0;
				var types = currentGroup.sortedYagotypes;

				for(var j = 0; j < types.length; j++) {
					var type = types[j];
					var cf = type[1];
					var iuf = Math.log(Object.keys($scope.groups).length / $scope.groupCFMap[type[0]]);
					var cfIuf = (Math.pow(cf, 1 + $scope.generalityBias))*(Math.pow(iuf, 1 - $scope.generalityBias));
					
					cfIufSum += Math.pow(cfIuf, 2);
					
					groupCFIUFMap[type[0]] = cfIuf;
				}
				
				var euclidLength = Math.sqrt(cfIufSum);
				
				currentGroup.cfIufMap = normalizeCFIUF(currentGroup, groupCFIUFMap, euclidLength);
				var legendText = currentGroup.topTypes[0][0];
				
				for(var j = 1; j < 5; j++) {
					legendText += ", " + currentGroup.topTypes[j][0];
				}
				
				$scope.legend[i] = legendText;
				
				labelString += "Topic " + (i-1) + ": ";
				labelString += legendText;
				
				for(var j = 5; j < 10; j++) {
					labelString += ", " + currentGroup.topTypes[j][0];
				}
				
				labelString += "\n";
			}
			
			console.log(labelString);
			
			//force.charge(-60);
			graph.getForce().linkDistance(60);

			graph.start($scope.legend);*/
			
			//evaluateClusteringAccuracy(evalClusters, $scope.users.length);
		});
	}
	
	$scope.getUser = function() {
		$scope.cfMap = {};
		$scope.processing = false;
		$scope.finished = false;
		$scope.showSimilarityGraph = false;
		

		
		//start();
		
		$scope.noUserFound = false;
		$scope.users = [];
		$scope.loadingUser = true;
		var userJSON = TwitterUser.user({ name: $scope.screenName }, function() {
			if(userJSON.user) {
				$scope.users.push(userJSON.user);
				$scope.users[0].profileImageURL = userJSON.user.profileImageURL;
				
				// Cancel old timeout.
			    if ($scope.users[0].timeoutId) {
			        $timeout.cancel($scope.users[0].timeoutId);
			    }

			    // Hide images to start with (to prevent overlap).
			    $scope.users[0].imageVisible = false;

			    $scope.users[0].timeoutId = $timeout(function() {
			        $scope.users[0].imageVisible = true;
			        $scope.users[0].timeoutId = undefined;
			    }, 10);
				
				if($scope.users[0].tweets.length > 0) {
					getTweetStats($scope.users[0]);
					$scope.getOntology($scope.users[0]);
				} else {
					$scope.getTweets($scope.users[0]);
				}
			} else {
				$scope.noUserFound = true;
				$scope.users = [];
			}
			
			$scope.loadingUser = false;
		});
	};
	
	$scope.getTweets = function(user) {
		user.loadingTweets = true;
		var tweetsJSON = UserTweets.tweets({ name: user.screenName }, function() {
			user.englishRate = tweetsJSON.englishRate;
			user.tweets = tweetsJSON.tweets;
			
			getTweetStats(user);
			$scope.getOntology(user);
			
			user.loadingTweets = false;
		});
	};
	
	$scope.getOntology = function(user) {
		user.loadingOntology = true;
		var ontologyJSON = UserOntology.ontology({ name: user.screenName, concatenation: $scope.concatenation }, function() {
			user.ontology = ontologyJSON.user.userOntology;
			user.ontology.sortedYagotypes = [];
			
			updateCFMap(user);
			updateCFIUF(0);
			
			user.loadingOntology = false;
		});
	};
	
	$scope.getTargetUserNetwork = function(user) {
		$scope.loadingNetwork = true;
		var networkJSON = UserNetwork.network({ name: user.screenName }, function() {
			_.each(networkJSON.network, function(user, i) {
				if(i > 0) $scope.users.push(user);
			});
			$scope.loadingNetwork = false;
			$scope.pageSize = 10;
		});
	};
	
	$scope.getDCGNetwork = function(userRelevanceList) {
		$scope.loadingNetwork = true;
		var networkJSON = UserNetwork.network({ list: userRelevanceList }, function() {
			_.each(networkJSON.network, function(user) {
				$scope.users.push(user);
			});
			$scope.loadingNetwork = false;
			$scope.pageSize = 10;
		});
	};
	
	$scope.processNetwork = function(index) {
		
		$scope.processing = true;

		$scope.users[index].loadingTweets = true;
		var tweetsJSON = UserTweets.tweets({ name: $scope.users[index].screenName }, function() {
			
			$scope.users[index].englishRate = tweetsJSON.englishRate;
			$scope.users[index].tweets = tweetsJSON.tweets;
			
			getTweetStats($scope.users[index]);
			
			$scope.users[index].loadingTweets = false;
			$scope.users[index].loadingOntology = true;
			var ontologyJSON = UserOntology.ontology({ name: $scope.users[index].screenName }, function() {
				
				$scope.users[index].ontology = ontologyJSON.user.userOntology;
				$scope.users[index].ontology.sortedYagotypes = [];
				
				$scope.users[index].loadingOntology = false;
				
				updateCFMap($scope.users[index]);
				
				if(index % 50 == 0) {
					updateCFIUF(index);
				}
				
				index += 1;
				
				if(index < $scope.users.length) {
					$scope.processNetwork(index);
				} else {
					if((index-1) % 50 != 0) {
						updateCFIUF(index-1);
					}
					$scope.finished = true;
					//applyLDA();
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
	        var relevanceScores = {};
	        
	        _.each(results, function(result) {
	        	var res = result.split("\t");
	        	csvList += res[0] + ",";
	        	relevanceScores[res[0]] = res[1];
	        });
	        
	        EvaluationService.setRelevanceScores(relevanceScores);
	        $scope.getDCGNetwork(csvList.substring(0, csvList.length - 1));
	    }
	}
	
	function applyLDA() {
		console.log("Applying LDA!");
		
		// Initialize the LDA vocabulary given the users and number of topics.
		LDAService.init($scope.users, $scope.ldaTopics);
		
		// Remaining configuration parameters, including base values for the Gibbs sampling. These will be divided by K and V, respectively.
		// alpha: per-document distributions over topics (0.1)
		// beta: per-topic distributions over words (0.01)
		config = { iterations: 2000, burnIn: 400, thinInterval: 100, sampleLag: 10, gibbsAlphaNum: 50, gibbsBetaNum: 200 };
		
		// Execute the LDA algorithm, then evaluate the result.
		LDAService.doWork(config).then(function(topicUsers) {
			evaluateClusteringAccuracy(topicUsers, $scope.users.length);
        });
	}
}]);

