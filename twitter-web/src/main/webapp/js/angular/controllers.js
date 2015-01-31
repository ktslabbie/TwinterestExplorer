var twitterWebController = angular.module('twitterWeb.controller', [])

.controller('TwitterController', ['$scope', '$timeout', '$http', 'SimpleTwitterUser', 'Tweets', 'TwitterUser', 'TwitterUserNetwork', 'TwitterUserList', 'KeywordUsers', 'SimilarityService',
                                  'CFIUFService', 'HCSService', 'EvaluationService', 'Graph',
                                  function($scope, $timeout, $http, SimpleTwitterUser, Tweets, TwitterUser, TwitterUserNetwork, TwitterUserList, KeywordUsers, SimilarityService, 
                                		   CFIUFService, HCSService, EvaluationService, Graph) {
	
	$scope.loadingSeedUser = false;
	$scope.noUserFound = false;
	$scope.loadingUsers = false;
	
	$scope.showSimilarityGraph = false;
	$scope.clusteringNetwork = false;
	
	$scope.updatingCFIUF = false;
	$scope.updatingSimilarityGraph = false;
	
	$scope.startTime = 0;
	$scope.endTime = 0;
	
	$scope.users = [];
	$scope.validUsers = [];
	$scope.visibleUsers = [];
	$scope.cfiufMaps = [];
	$scope.groups = [];
	$scope.screenName = "iOS_blog";
	$scope.pageSize = 0;
	$scope.refreshCnt = 0;
	$scope.tweetsPerUser = 500;
	$scope.userCount = 100;
	$scope.minimumEnglishRate = 0.7;
	
	// Named Entity Recognition settings.
	$scope.nerConfidence = 0;
	$scope.nerSupport = 0;
	$scope.generalityBias = 0;
	$scope.concatenation = 25;
	
	// Minimum similarity threshold.
	$scope.minimumSimilarity = 0.1;
	
	// Twitter user restrictions.
	$scope.maxSeedUserFollowers = 9990000;
	$scope.minFollowers = 0;
	$scope.maxFollowers = 10000;
	$scope.minFollowing = 0;
	$scope.maxFollowing = 10000;
	$scope.minTweets = 100;
	$scope.maxTweets = 1000000;
	
	$scope.processIndex = 0;
	$scope.completedUserCount = 0;
	$scope.maxProcesses = 6;
	$scope.activeProcesses = 1;
	var graph = new Graph(1080, 640, "#graph");
	
	$scope.gfMap = {};
	
	$scope.legend = [];
	$scope.colors = ["#1f77b4", "#aec7e8", "#ff7f0e", "#ffbb78", "#2ca02c", "#98df8a", "#d62728", "#ff9896", "#9467bd", "#c5b0d5",
	                 "#8c564b", "#c49c94", "#e377c2", "#f7b6d2", "#7f7f7f", "#c7c7c7", "#bcbd22", "#dbdb8d", "#17becf", "#9edae5" ];
	
	/**
	 * Initialize the state of the application.
	 */
	function init() {
		$scope.loadingSeedUser = false;
		$scope.noUserFound = false;
		$scope.loadingUsers = false;
		
		$scope.showSimilarityGraph = false;
		$scope.clusteringNetwork = false;
		
		$scope.updatingCFIUF = false;
		$scope.updatingSimilarityGraph = false;
		
		while($scope.users.length > 0) $scope.users.pop();
		while($scope.validUsers.length > 0) $scope.validUsers.pop();
		while($scope.validUsers.length > 0) $scope.visibleUsers.pop();
		while($scope.cfiufMaps.length > 0) $scope.cfiufMaps.pop();
		while($scope.groups.length > 0) $scope.groups.pop();
		
		$scope.refreshCnt = 0;
		
		graph.clearGraph();
		
		CFIUFService.clear();
		$scope.zoomedUFMap = {};
		$scope.gfMap = {};
		
		$scope.processIndex = 0;
		$scope.completedUserCount = 0;
		$scope.maxProcesses = 6;
		$scope.activeProcesses = 1;
	}
	
	/* Function to check if the active user has changed (exact name no longer in input box). */
	/*$scope.userChanged = function() {
		if(!$scope.screenName || $scope.users.length === 0) return false;
		return $scope.screenName.toLowerCase() != $scope.users[0].screenName.toLowerCase();
	}*/
	
	$scope.$on('graphZoom', function (event, data) {
		$scope.visibleUsers = $scope.groups[data.group].users;
		$scope.zoomedUFMap = {};
		/*_.each($scope.visibleUsers, function(user, i) {
			updateUFMap($scope.zoomedUFMap, user.userOntology.ontology);
		});*/
		
		//console.log("Zoomed UF map: " + JSON.stringify($scope.zoomedUFMap));

		var ev = {};
		ev.entities = $scope.visibleUsers;
		ev.ufMap = $scope.zoomedUFMap;
		ev.index = $scope.visibleUsers.length-1;
		ev.generalityBias = -0.4;

		CFIUFService.doWork(ev).then(function(newOntologies) {
			_.each(newOntologies, function(newOntology, i) {
				$scope.visibleUsers[i].userOntology = newOntology;
				//console.log("NEW ONTOLOGY: " + JSON.stringify($scope.visibleUsers[i].userOntology));
			});
			//$scope.minimumSimilarity = 0.05;
			$scope.minimumSimilarity = 0.08;
			
			//updateSimilarityGraph();
		});
	});
	
	/**
	 * Watch for valid user additions. When we get a new user we may want to update the CF-IUF matrix.
	 */
	$scope.$on('userUpdated', function () {
		
		// New user has been added. Check if we're already busy updating the CF-IUF matrix or not.
		if(!$scope.updatingCFIUF) {
			// Nope. Start updating the CF-IUF matrix.
			updateCFIUF($scope.validUsers);
			
		} else if(!$scope.loadingUsers) {
			
			console.log("'userUpdated' has been broadcasted but we're no longer loading users and we're still busy calculating.");
			
			// 'userUpdated' has been broadcasted but we're no longer loading users and we're still busy calculating.
			// This means we finished gathering users. Wait until calculation has finished, then calculate the final CF-IUF matrix.
			var removeWatchUpdatingCFIUF = $scope.$watch('updatingCFIUF', function() {
				
				if(!$scope.updatingCFIUF) {
					// updatingCFIUF has changed to false. Final update.
					updateCFIUF($scope.validUsers);
					// Remove this watcher.
					removeWatchUpdatingCFIUF();
				}
			});
		}
		
		// Check if we're already busy updating the similarity graph or not, and actually have new data.
		if(!$scope.updatingSimilarityGraph && $scope.cfiufMaps.length > 0) {
			
			// Nope. Update it with the current info.
			updateSimilarityGraph($scope.cfiufMaps);
			
		} else if(!$scope.loadingUsers) {
			
			console.log("Waiting for final simgraph update.");
			
			// 'userUpdated' has been broadcasted but we're no longer loading users and we're still busy calculating.
			// This means we finished gathering users. Wait until calculation has finished, then calculate the final SimGraph.
			var removeWatchUpdatingSimGraph = $scope.$watchGroup(['updatingSimilarityGraph', 'updatingCFIUF'], function() {
				
				if(!$scope.updatingSimilarityGraph && !$scope.updatingCFIUF) {
					// updatingSimilarityGraph and updatingCFIUF are both false. Final update.
					updateSimilarityGraph($scope.cfiufMaps);
					// Remove this watcher.
					removeWatchUpdatingSimGraph();
				}
			});
		}
	});
	
	/**
	 * Update the CF-IUF scores for all entities (can be users or groups of users) up to entities[userIndex].
	 * We do this in a Web Worker (separate processing thread), which is activated by sending it an event.
	 */
	function updateCFIUF(entities) {
		
		if($scope.updatingCFIUF) return;
		$scope.updatingCFIUF = true;
		
		console.log("CF update.");
		var startTime = new Date().getTime();
		
		// Setup the event to send to the Web Worker.
		var event = {};
		event.ontologies = [];
		event.generalityBias = $scope.generalityBias;
		
		// To keep things fast, we only send new ontologies that haven't been stored on the worker side yet.
		for(var i = $scope.completedUserCount; i < entities.length; i++) {
			event.ontologies.push(entities[i].userOntology);
		}
		
		if(event.ontologies.length > 0) {
			CFIUFService.doWork(event).then(function(newOntologies) {
				// The new CF-IUF matrix has been calculated and returned. Update the users and CF-IUF maps with the new ontologies.
				console.log("then start");
				_.each(newOntologies.ontologies, function(newOntology, i) {
					$scope.validUsers[i].userOntology = newOntology;
					$scope.cfiufMaps[i] = newOntology.cfiufMap;
				});
				
				// Record until where we've processed ontologies so far.
				$scope.completedUserCount = newOntologies.ontologies.length;
				
				// Our CF-IUF Web Worker is idle for now. Make it known.
				$scope.updatingCFIUF = false;
				
				var endTime = new Date().getTime();
				console.log("CF-IUF execution time: " + (endTime - startTime));
				
				// Try to update the similarity graph, in case we weren't doing that yet.
				if(!$scope.updatingSimilarityGraph) {
					updateSimilarityGraph($scope.cfiufMaps);
				}
			});
		}
	}
	
	/**
	 * Function to update the similarity graph with new data.
	 */
	function updateSimilarityGraph(cfiufMaps) {
		
		if($scope.updatingSimilarityGraph) return;
		$scope.updatingSimilarityGraph = true;
		//console.log("cfiufmaps: " + JSON.stringify(cfiufMaps));
		
		// Get a snapshot of the current graph.
		graph.cloneMaps();
		
		// Build the event to send to the Web Worker.
		var ev = {};
		ev.minSim = $scope.minimumSimilarity;
		ev.cfiufMaps = cfiufMaps;
		
		SimilarityService.doWork(ev).then(function(data) {
			
			// We've finished calculating the similarity graph.
			$scope.updatingSimilarityGraph = false;
			
			// Remove any remaining nodes/links no longer in the similarity graph.
			graph.trim();
			
			// Render the final version.
			graph.start();
			
			// Calculate the DCG scores for this network (if available).
			//if(!_.isEmpty(EvaluationService.getRelevanceScores())) EvaluationService.dcg(graph.getNodes(), graph.getLinks());
			
			// Finally, cluster the similarity graph.
			$scope.clusterNetwork();
		});
	}
	
	/**
	 * Function to catch updates and push them to the similarity graph.
	 */
	$scope.$on('simGraphUpdate', function(event, data) {
		
		// Create the new, ungrouped nodes.
		var aNode = { name: $scope.visibleUsers[data.i].screenName, group: 0, userIndex: data.i };
		var bNode = { name: $scope.visibleUsers[data.j].screenName, group: 0, userIndex: data.j };
		
		// Add the nodes (if not yet existing) and get their index.
		var aIndex = graph.addNode(aNode);
		var bIndex = graph.addNode(bNode);
	
		// Add the link between the nodes.
		graph.addOrUpdateLink({ source: aIndex, target: bIndex, value: data.similarity });
		
		// Refresh SVG graph only one in 25 updates, to make rendering smoother.
		//$scope.refreshCnt++;
		//graph.start();
		//if($scope.refreshCnt % 50 == 0) graph.start();
	});
	
	/**
	 * Function to cluster the network, assuming we have a similarity graph.
	 */
	$scope.clusterNetwork = function() {
		$scope.clusteringNetwork = true;
		$scope.groups = [];
		$scope.gfMap = {};
		
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
		
		//console.log("Sending: " + JSON.stringify(e));
		
		HCSService.doWork(e).then(function(data) {
			// We've finished calculating the cluster graph. Render the final version.
			$scope.clusteringNetwork = false;
			
			// First, clear the graph in its entirety.
			//graph.clearGraph();

			// Add the clusters to the graph and group users accordingly.
			_.each(data.clusters, function(cluster, i) {
				var clusterNodes = cluster.nodes;
				var clusterEdges = cluster.edges;
				var group = { users: [], userOntology: { ontology: {} } };
				
				//console.log("Cluster: " + JSON.stringify(cluster));
				
				_.each(clusterNodes, function(node) {
					var user = $scope.visibleUsers[node.userIndex];
					user.tweets = [];
					group.users.push(user);
					
					// Merge YAGO types of this user into the YAGO types of the group.
					_.each(Object.keys(user.userOntology.ontology), function(type) {
						group.userOntology.ontology[type] = _.isNumber(group.userOntology.ontology[type]) ? 
								group.userOntology.ontology[type]+user.userOntology.ontology[type] : user.userOntology.ontology[type];
					});
					//group.userOntology.ontology = _.merge(user.userOntology.ontology, group.userOntology.ontology, function(a, b) { return _.isNumber(a) ? _.isNumber(b) ? a+b : a : b; });
				});
				
				// Update the group-based User Frequency map.
				//updateUFMap($scope.gfMap, group.userOntology.ontology);
				$scope.groups.push(group);
				graph.addCluster(clusterNodes, clusterEdges, i);
			});

			var ev = {};
			ev.entities = $scope.groups;
			ev.ufMap = $scope.gfMap;
			ev.index = $scope.groups.length-1;
			ev.generalityBias = $scope.generalityBias;
			
			CFIUFService.doWork(ev).then(function(newOntologies) {
				
				/*_.each(newOntologies, function(newOntology, i) {
					$scope.groups[i].userOntology = newOntology;
				})*/
				
				var labelString = "\nTopic labels:\n\n";
				$scope.legend = [];
				
				//if($scope.groups.length > 1) {
					_.each($scope.groups, function(group, i) {
						group.userOntology = newOntologies[i];
						
						var legendText = group.userOntology.topTypes[0][0];
						labelString += "Topic " + (i+1) + ": ";
						
						for(var j = 1; j < group.userOntology.topTypes.length; j++) {
							legendText += ", " + group.userOntology.topTypes[j][0];
						}
						
						$scope.legend[i] = legendText;
						labelString += legendText + "\n";
					});
				//}
					
				console.log(labelString);
				
				// Update force settings and refresh.
				graph.getForce().charge(-120).linkDistance(150);
				
				graph.start();
				
				// Evaluate the accuracy of the clustering result (if ground truth present).
				if(!_.isEmpty(EvaluationService.getRelevanceScores())) EvaluationService.mcc($scope.groups, $scope.visibleUsers.length);
			});
		});
	}
	
	$scope.getKeywordUsers = function(keyword) {
		// TODO
	};
	
	/**
	 * Check to see a user is protected or has too little tweets.
	 * Set the user error value if so.
	 */
	function isValidUser(user) {
		if(user.properties.protectedUser) {
			user.error = "This user is protected.";
			return false;
		} else if(user.properties.statusesCount < $scope.minTweets) {
			user.error = "This user does not have enough tweets.";
			return false;
		}
		
		user.valid = true;
		return true;
	}
	
	/**
	 * Check to see a user has enough English tweets.
	 * Set the user error value if not.
	 */
	function isEnglishUser(user) {
		if(user.englishRate < $scope.minimumEnglishRate) {
			user.error = "This user's English rate is too low.";
			return false;
		}
		return true;
	}
	
	/**
	 * Function to get the initial user. We want this to be fast, so we get just the user first (without tweets/traits unless already in DB).
	 * If the user is found to be valid, we update it with tweets/traits immediately after.
	 * 
	 * @param screenName
	 */
	$scope.getSeedUser = function(screenName) {
		// This is always the first function call of a run through the app, so initialize everything fist.
		init();
		
		$scope.loadingSeedUser = true;
		
		// Get a simple user: no tweets/traits unless already in DB.
		SimpleTwitterUser.get({ screenName: screenName, confidence: $scope.nerConfidence, 
						   support: $scope.nerSupport, concatenation: $scope.concatenation }, function(user) {

				user.$promise = null; // Hack to allow object cloning for Web Workers. Somehow "{}" is not cloneable?
				$scope.loadingSeedUser = false;
				
				if(user.screenName) {
					// User exists. Add to all users.
					$scope.users.push(user);
					
					if(isValidUser(user)) {
						// User is valid, even. Check if he already has traits.
						if(user.userOntology.typeCount === 0) {
							// Nope. Update the user with tweets/traits.
							updateUser(user); 
						} else {
							// Yep. Process this user.
							processUser(user);
						}
					}
				} else {
					// User doesn't exist in Twitter.
					$scope.noUserFound = true;
				}
		});
	};

	/**
	 * Function to update an existing user with tweets/traits.
	 */
	function updateUser(user) {
		// Make known to the view that we're loading tweets/traits.
		user.loading = true;
		
		// Get a full user: get tweets/traits from Twitter/DBepdia if needed.
		TwitterUser.get({ screenName: user.screenName, confidence: $scope.nerConfidence, support: $scope.nerSupport, 
			concatenation: $scope.concatenation, englishRate: $scope.minimumEnglishRate },
			function(userData) {
				// Set the basic updated user info. We can't replace the entire user object because reasons.
				user.userID = userData.userID;
				user.properties = userData.properties;
				user.tweetCount = userData.tweetCount;
				
				// Set the new English rate calculated from the user's tweets on the server.
				user.englishRate = userData.englishRate;

				// Check user validity.
				if(isValidUser(user) && isEnglishUser(user)) {
					// User is valid and has enough English tweets. Set tweets/traits found.
					user.tweets = userData.tweets;
					user.userOntology = userData.userOntology;
					
					// Finalize the user.
					processUser(user);
				}
		});
	}
	
	/**
	 * Function to integrate a newly obtained user with the rest of the app by updating the maps/lists.
	 */
	function processUser(user) {
		user.loading = false;
		
		//getTweetStats(user);
		
		$scope.validUsers.push(user);
		$scope.visibleUsers.push(user);
		
		$scope.$broadcast('userUpdated');
	}
	
	/**
	 * Function to get the all the users from a specified list of screenNames.
	 * Users are processes in parallel, with maxProcesses threads.
	 */
	$scope.updateUsers = function() {
		// Let the view know we're loading the users.
		$scope.loadingUsers = true;
		
		// Make sure we actually have as many users as max processes...
		var limit = Math.min($scope.maxProcesses, $scope.users.length);
		
		// Start by updating the first user (updateUser is an async function).
		updateUser($scope.users[0]);
		$scope.processIndex++;
		limit--;
		
		// Then, fill up the max active processes by fetching users up until that point.
		while(limit--) {
			updateUser($scope.users[$scope.processIndex]);
			$scope.processIndex++;
			$scope.activeProcesses++;
		}
		
		// Now, wait for processes being freed up (userUpdated being broadcast). Whenever this happens, process the next user.
		var removeOnUserUpdated = $scope.$on('userUpdated', function () {
			
			// Check if there are still users left to process.
			if($scope.processIndex < $scope.users.length) {
				// Yep. Fetch the next user.
				updateUser($scope.users[$scope.processIndex]);
				$scope.processIndex++;
			} else {
				// Nope. Free up the process.
				$scope.activeProcesses--;
				
				// Check if this was the last process...
				if($scope.activeProcesses === 0) {
					
					console.log("Last process detected!");
					
					// Yep. This means we're done.
					$scope.loadingUsers = false;
					// Remove this $on function by calling it.
					removeOnUserUpdated();
					
					$scope.$broadcast('userUpdated');
				}
			}
		});
	};
	
	$scope.loadMore = function() {
		$scope.pageSize = $scope.pageSize + 5;
	}
	
	var gtFileInput = $('#gtfile');
	var gtUploadButton = $('#gtupload');
	
	var ldaFileInput = $('#ldafile');
	var ldaUploadButton = $('#ldaupload');
	
	// Function to upload a GT file.
	gtUploadButton.on('click', function() {
		if (!window.FileReader) {
			alert("Your browser is not supported.");
			return false;
		}

		var input = gtFileInput.get(0);
		var reader = new FileReader();

		if (input.files.length) {
			var textFile = input.files[0];
			reader.readAsText(textFile);

			$(reader).on('load', utils.convertGTToJSON);
			$(reader).on('load', processFile);
			//alert("Ground truth loaded!");
		} else {
			alert("Please upload a file before continuing.");
		} 
	});

	// Function to upload an LDA topic result file.
	ldaUploadButton.on('click', function() {
		if (!window.FileReader) {
			alert("Your browser is not supported.");
			return false;
		}

		var input = ldaFileInput.get(0);
		var reader = new FileReader();

		if (input.files.length) {
			var textFile = input.files[0];
			reader.readAsText(textFile);
			
			$(reader).on('load', processLDAFile);
		} else {
			alert("Please upload a file before continuing.");
		} 
	});

	function processLDAFile(e) {
	    var file = e.target.result;
	    var results;
	    var csvList = "";
	    
	    if (file && file.length) {
	        results = file.split("\n");
	        var t = results[0].split("\t").length - 1;
	        var i = t;
	        var userCount = 0;
	        
	        while(i--) {
	        	$scope.groups.push( { users: [] } );
	        }
	        
	        _.each(results, function(result) {
	        	result = result.trim();
	        	if(result === "") return;
				var res = result.split("\t");
				var g = 0;
				var max = res[1];
	        	
				for(var i = 2; i <= t; i++) {
	        		if(res[i] > max) {
	        			max = res[i];
	        			g = i-1;
	        		}
	        	}
	        	
				$scope.groups[g].users.push( { screenName: res[0] } );
				userCount++;
	        });
	        
	        EvaluationService.mcc($scope.groups, userCount);
	        
	        //randomClustering($scope.groups, userCount);
	    }
	}
	
	function randomClustering(groups, userCount) {
		
		var randomGroups = [];
		var k = groups.length;
		while(k--) randomGroups.push( { users: [] });
		
		_.each(groups, function(group) {
			var users = group.users;
			_.each(users, function(user) {
				var i = Math.floor(Math.random()*randomGroups.length);
				randomGroups[i].users.push(user);
			});
		});
		
		EvaluationService.mcc(randomGroups, userCount);
	}
	
	function processFile(e) {
		init();
		
	    var file = e.target.result;
	    var results;
	    var csvList = "";
	    
	    if (file && file.length) {
	        results = file.split("\n");
	        var relevanceScores = {};
	        
	        _.each(results, function(result) {
	        	result = result.trim();
	        	if(result === "") return;
				if(result.charAt(0) != ":" && result.charAt(0) != "-") {
					var res = result.split(",");
					$scope.users.push( { screenName: res[0] } );
		        	relevanceScores[res[0]] = res[1];
				}
	        });
	        
	        EvaluationService.setRelevanceScores(relevanceScores);
	        $scope.showSimilarityGraph = true;
	        $scope.updateUsers();
	    }
	}
	
	function prepareTMTData() {
		console.log("Preparing TMT data.");
		
		var allCount = 0;
		var userCount = 0;
		var csv = "";
		var lastUser;
		
		_.each($scope.users, function(user) {
			userCount++;
			if(user.screenName === "gslgmcity") { lastUser = user; return; }
			//console.log("Tweet count for " + user.screenName + ":\t" + user.tweets.length);
			_.each(user.tweets, function (tweet) {
				allCount++;
				csv += '' + allCount + ',' + user.screenName + ',"' + tweet.content.replace(/(\r\n|\n|\r|")/gm,"").trim() + '"\n';
			});
		});
		
		_.each(lastUser.tweets, function (tweet) {
			allCount++;
			csv += '' + allCount + ',' + lastUser.screenName + ',"' + tweet.content.replace(/(\r\n|\n|\r|")/gm,"").trim() + '"\n';
		});
		
		
		console.log(csv);
		//console.log("UserCount: " + userCount);
	}
}]);

