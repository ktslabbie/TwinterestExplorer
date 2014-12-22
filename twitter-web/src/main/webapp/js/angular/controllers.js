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
	$scope.groups = [];
	$scope.screenName = "bnmuller";
	$scope.pageSize = 0;
	$scope.refreshCnt = 0;
	
	$scope.ldaTopics = 3;
	
	$scope.generalityBias = 0;
	$scope.concatenation = 50;
	$scope.minimumSimilarity = 0.2;
	
	var graph;
	
	$scope.legend = {};
	$scope.ufMap = {};
	$scope.gfMap = {};
	
	$scope.yagoTypeBlackList = {"Abstraction": true, "Group": true, "YagoLegalActorGeo": true, "YagoPermanentlyLocatedEntity": true, "PhysicalEntity": true,
								"Object": true, "YagoGeoEntity": true, "YagoLegalActor": true, "Whole": true, "Region": true, "Musician": true };
	
	/**
	 * Initialize the state of the application.
	 */
	function init() {
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
		$scope.groups = [];
		$scope.refreshCnt = 0;
		
		
		graph = new Graph(960, 720, "#graph");
		
		$scope.legend = {};
		$scope.ufMap = {};
		$scope.gfMap = {};
	}
	
	/* Function to check if the active user has changed (exact name no longer in input box). */
	$scope.userChanged = function() {
		if(!$scope.screenName || $scope.users.length === 0 || !$scope.users[0].screenName) return false;
		return $scope.screenName.toLowerCase() != $scope.users[0].screenName.toLowerCase();
	}
	
	/**
	 *  Extract retweets, hashtags and mentions from a user's tweets.
	 */
	function getTweetStats(user) {
		var retweets = 0, hashtags = {}, mentions = {};
		
		_.each(user.tweets, function(tweet) {
			_.each(tweet.hashtags, function(tag) { hashtags[tag] = hashtags[tag] ? hashtags[tag]+1 : 1; });
			_.each(tweet.userMentions, function(men) { mentions[men] = mentions[men] ? mentions[men]+1 : 1; });
			if(tweet.retweet) retweets++;
		});
		
		var htList = [], mList = [];
		for(key in hashtags) htList.push({ tag: key, occ: hashtags[key] });
		for(key in mentions) mList.push({ mention: key, occ: mentions[key] });
		
		user.retweets = retweets; user.hashtags = htList; user.userMentions = mList;
	}
	
	/**
	 * Extracts a more readable name from a YAGO type.
	 */
	$scope.getTypeName = function(yagoType) {
		var typeName = yagoType.split(":")[1];
		var wordnetCode = typeName.substr(typeName.length - 9);
		
		if(!isNaN(wordnetCode))
			typeName = typeName.substr(0, typeName.length - 9);
		
		return typeName;
	}
	
	/**
	 * Return the top-k types sorted by their value.
	 */
	function getTopTypes(map, k) {
		return _.map(_.first(_.sortBy(_.pairs(map), function(tuple) { return -tuple[1]; }), k), function(type) { type[0] = $scope.getTypeName(type[0]); return type; });
	}
	
	/**
	 * Normalize the CF-IUF map to values between 0 and 1 given the Euclidian length of all CF-IUF scores.
	 */
	function normalizeCFIUF(user, userCFIUFMap, euclidLength) {
		return map = (euclidLength === 0) ? userCFIUFMap : _.mapValues(userCFIUFMap, function(val) { return val/euclidLength; });
	}
	
	/**
	 * Update the user frequency map with new values.
	 */
	function updateUFMap(ufMap, typeMap) {
		for(type in typeMap) ufMap[type] = ufMap[type] ? ufMap[type] + 1 : 1;
	}
	
	/**
	 * Update the CF-IUF scores for all entities (can be users or groups of users) up to entities[userIndex].
	 */
	function updateCFIUF(entities, ufMap, index) {
		for(var i = 0; i <= index; i++) {
			var currentEntity = entities[i];
			var yagoTypes = currentEntity.ontology.yagotypes;
			
			entityCFIUFMap = {};
			var cfIufSum = 0;
			
			_.each(_.keys(yagoTypes), function(type) {
				var cf = yagoTypes[type];
				var iuf = Math.log(entities.length / ufMap[type]);
				var cfIuf = (Math.pow(cf, 1 + parseFloat($scope.generalityBias)))*(Math.pow(iuf, 1 - parseFloat($scope.generalityBias)));
				
				cfIufSum += Math.pow(cfIuf, 2);
				entityCFIUFMap[type] = cfIuf;
			});
			
			var euclidLength = Math.sqrt(cfIufSum);
			
			if(currentEntity.ontology.cfIufMap) {
				var newMap = normalizeCFIUF(currentEntity, entityCFIUFMap, euclidLength);
				
				for(var key in newMap) {
					if(!currentEntity.ontology.cfIufMap[key]) {
						currentEntity.ontology.cfIufMap[key] = newMap[key];
					} else {
						var a = Math.round(currentEntity.ontology.cfIufMap[key] * 100) / 100;
						var b = Math.round(newMap[key] * 100) / 100;
						if(a != b) currentEntity.ontology.cfIufMap[key] = newMap[key];
					}
				}
			} else {
				currentEntity.ontology.cfIufMap = normalizeCFIUF(currentEntity, entityCFIUFMap, euclidLength);
			}
			
			currentEntity.ontology.topTypes = getTopTypes(currentEntity.ontology.cfIufMap, 5);
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
	
	/**
	 * Function to cluster the network, assuming we have a similarity graph.
	 */
	$scope.clusterNetwork = function() {
		$scope.clusteringNetwork = true;
		
		var e = {};
		
		// Copy the nodes and links from the current graph.
		e.nodes = _.cloneDeep(graph.getNodes());
		e.links = _.cloneDeep(graph.getLinks());
		
		// Remove unneeded info that will prevent comparisons from the nodes and links.
		_.each(e.nodes, function(node) { _.pick(node, ['name', 'group', 'userIndex']); });
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
				var group = { users: [], ontology: { yagotypes: {} } };
				
				_.each(clusterNodes, function(node) {
					var user = $scope.users[node.userIndex];
					user.tweets = [];
					group.users.push(user);
					
					// Merge YAGO types of this user into the YAGO types of the group.
					group.ontology.yagotypes = _.merge(user.ontology.yagotypes, group.ontology.yagotypes, function(a, b) { return _.isNumber(a) ? _.isNumber(b) ? a+b : a : b; });
				});
				
				// Update the group-based User Frequency map.
				updateUFMap($scope.gfMap, group.ontology.yagotypes);
				$scope.groups.push(group);
				graph.addCluster(clusterNodes, clusterEdges, i);
			});
			
			// Update the CF-IUF maps for each group. Top types will be calculated too.
			updateCFIUF($scope.groups, $scope.gfMap, $scope.groups.length-1);

			var labelString = "\nTopic labels:\n\n";
			
			_.each($scope.groups, function(group, i) {
				var legendText = group.ontology.topTypes[0][0];
				labelString += "Topic " + (i+1) + ": ";
				
				for(var j = 1; j < group.ontology.topTypes.length; j++) {
					legendText += ", " + group.ontology.topTypes[j][0];
				}
				
				$scope.legend[i] = legendText;
				labelString += legendText + "\n";
			});
			
			console.log(labelString);
			
			// Update force settings and refresh.
			graph.getForce().charge(-90).linkDistance(90);
			graph.start($scope.legend);

			// Evaluate the accuracy of the clustering result (if ground truth present).
			EvaluationService.mcc($scope.groups, $scope.users.length);
		});
	}
	
	$scope.getUser = function() {
		init();
		
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
			
			updateUFMap($scope.ufMap, user.ontology.yagotypes);
			updateCFIUF($scope.users, $scope.ufMap, 0);
			
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
				$scope.users[index].loadingOntology = false;
				
				updateUFMap($scope.ufMap, $scope.users[index].ontology.yagotypes);
				
				if(index % 50 == 0) {
					updateCFIUF($scope.users, $scope.ufMap, index);
				}
				
				index += 1;
				
				if(index < $scope.users.length) {
					$scope.processNetwork(index);
				} else {
					if((index-1) % 50 != 0) {
						updateCFIUF($scope.users, $scope.ufMap, index-1);
					}
					$scope.finished = true;
					applyLDA();
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
			EvaluationService.mcc(topicUsers, $scope.users.length);
        });
	}
}]);

