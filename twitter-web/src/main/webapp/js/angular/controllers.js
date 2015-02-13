var twitterWebController = angular.module('twitterWeb.controller', [])

.controller('TwitterController', ['$scope', '$timeout', 'SimpleUser', 'User', 'FollowersList', 'KeywordUserList', 'SimilarityService',
                                  'CFIUFService', 'CFIUFGroupService', 'HCSService', 'EvaluationService', 'Graph',
                                  function($scope, $timeout, SimpleUser, User, FollowersList, KeywordUserList, SimilarityService, 
                                		   CFIUFService, CFIUFGroupService, HCSService, EvaluationService, Graph) {
	
	$scope.loadingSeedUser = false;
	$scope.noUserFound = false;
	$scope.loadingUsers = false;
	
	$scope.clusteringNetwork = false;
	$scope.clusteringFinished = false;
	
	$scope.updatingCFIUF = false;
	$scope.updatingSimilarityGraph = false;
	
	$scope.zoomed = false;
	
	$scope.users = [];
	$scope.validUsers = [];
	$scope.visibleUsers = [];
	var fullGroups = [];
	$scope.groups = [];
	$scope.screenName = "iOS_blog";
	$scope.keyword = "machine learning";
	$scope.pageSize = 0;
	$scope.refreshCnt = 0;
	$scope.tweetsPerUser = 100;
	$scope.userCount = 200;
	$scope.minimumEnglishRate = 0.7;
	
	// Named Entity Recognition settings.
	$scope.nerConfidence = 0;
	$scope.nerSupport = 0;
	$scope.generalityBias = 0;
	$scope.concatenation = 25;
	
	// Minimum similarity threshold.
	$scope.minimumSimilarity = 0.05;
	
	// Twitter user restrictions.
	$scope.maxSeedUserFollowers = 9990000;
	$scope.minFollowers = 0;
	$scope.maxFollowers = 10000;
	$scope.minFollowing = 0;
	$scope.maxFollowing = 10000;
	$scope.minTweets = 100;
	$scope.maxTweets = 1000000;
	
	$scope.processIndex = 0;
	$scope.completedCFIUFCount = 0;
	$scope.maxProcesses = 6;
	$scope.activeProcesses = 1;
	
	var graph = new Graph(1080, 640, "#graph");
	var similarityNodeCount = 0;
	var similarityLinks = [];
	
	var cfiufMaps = { users: [], groups: [] };
	
	$scope.legend = [];
	var fullLegend = [];
	$scope.colors = ["#1f77b4", "#aec7e8", "#ff7f0e", "#ffbb78", "#2ca02c", "#98df8a", "#d62728", "#ff9896", "#9467bd", "#c5b0d5",
	                 "#8c564b", "#c49c94", "#e377c2", "#f7b6d2", "#7f7f7f", "#c7c7c7", "#bcbd22", "#dbdb8d", "#17becf", "#9edae5" ];
	
	/**
	 * Initialize the state of the application.
	 */
	$scope.init = function() {
		$scope.loadingSeedUser = false;
		$scope.noUserFound = false;
		$scope.loadingUsers = false;
		
		$scope.clusteringNetwork = false;
		$scope.clusteringFinished = false;
		
		$scope.updatingCFIUF = false;
		$scope.updatingSimilarityGraph = false;
		
		$scope.zoomed = false;
		
		while($scope.users.length > 0) $scope.users.pop();
		while($scope.validUsers.length > 0) $scope.validUsers.pop();
		while($scope.visibleUsers.length > 0) $scope.visibleUsers.pop();
		while($scope.groups.length > 0) $scope.groups.pop();
		fullGroups = [];
		$scope.legend = [];
		fullLegend = [];
		
		$scope.refreshCnt = 0;
		
		graph.clearGraph();
		similarityNodeCount = 0;
		similarityLinks = [];
		cfiufMaps = { users: [], groups: [] };
		
		CFIUFService.clear();
		
		$scope.processIndex = 0;
		$scope.completedCFIUFCount = 0;
		$scope.maxProcesses = 6;
		$scope.activeProcesses = 1;
	}
	
	/**
	 * Function to handle the graph zoom event being fired.
	 */
	$scope.$on('graphZoom', function (event, data) {
		// Let the view know we're zoomed in; add a button to restore the graph to its original form.
		$scope.zoomed = true;
		
		// Update the collection of actually visible users.
		$scope.visibleUsers = $scope.groups[data.group-1].users;
		
		var ev = {};
		ev.groups = true;
		ev.generalityBias = $scope.generalityBias;
		ev.ontologies = [];
		_.each($scope.visibleUsers, function(user) { ev.ontologies.push(user.userOntology.ontology); });
		
		CFIUFGroupService.doWork(ev).then(function(newOntologies) {
			cfiufMaps.groups = [];
			
			_.each(newOntologies.ontologies, function(newOntology, i) {
				$scope.visibleUsers[i].userOntology.cfiufMap = newOntology.cfiufMap;
				$scope.visibleUsers[i].userOntology.topTypes = newOntology.topTypes;
				cfiufMaps.groups.push(newOntology.cfiufMap);
			});
			
			updateSimilarityGraph($scope.visibleUsers.length, cfiufMaps.groups);
		});
	});
	
	/**
	 * Function the go from a zoomed view of the graph back to the original view.
	 */
	$scope.restoreGraph = function() {
		graph.restoreGraph();
		$scope.zoomed = false;
		$scope.legend = fullLegend;
		$scope.visibleUsers = $scope.validUsers;
		$scope.groups = fullGroups;
		graph.start();
	}
	
	/**
	 * Watch for valid user additions. When we get a new user we may want to update the CF-IUF matrix.
	 */
	$scope.$on('userUpdated', function () {
		
		// New user has been added. Check that we're not waiting for the final update and still loading users.
		if($scope.loadingUsers && !$scope.updatingSimilarityGraph) {
			// Seems OK. Start updating the CF-IUF matrix.
			updateCFIUF($scope.validUsers);
			
		} else if(!$scope.loadingUsers) {
			
			console.log("Final 'userUpdated' has been broadcast (we're done collecting). Add a watcher for the final CF-IUF, or just do it if the final update is pending.");
			
			/*if($scope.pendingFinalCFIUF) {
				$scope.pendingFinalCFIUF = false;
				updateCFIUF($scope.validUsers);
			} else {*/
				// This means we finished gathering users. Calculate the final CF-IUF matrix. First make a watcher for the current calculation to finish.
			finalize();
				
			//}
		}
	});
	
	/**
	 * Update the CF-IUF scores for all entities (can be users or groups of users) up to entities[userIndex].
	 * We do this in a Web Worker (separate processing thread), which is activated by sending it an event.
	 */
	function updateCFIUF(entities) {
		// If we're already updating, return.
		if($scope.updatingCFIUF) return;
		$scope.updatingCFIUF = true;
		
		var startTime = new Date().getTime();
		
		// Setup the event to send to the Web Worker.
		var event = {};
		event.groups = false;
		event.ontologies = [];
		event.generalityBias = $scope.generalityBias;
		
		// To keep things fast, we only send new ontologies that haven't been stored on the worker side yet.
		for(var i = $scope.completedCFIUFCount; i < entities.length; i++) {
			event.ontologies.push(entities[i].userOntology.ontology);
		}
		
		// Send the ontologies.
		CFIUFService.doWork(event).then(function(data) {
			cfiufMaps.users = [];
			
			if(data.ontologies.length > 0) {
				// The new CF-IUF matrix has been calculated and returned. Update the users and CF-IUF maps with the new ontologies.
				_.each(data.ontologies, function(newOntology, i) {
					$scope.validUsers[i].userOntology.cfiufMap = newOntology.cfiufMap;
					$scope.validUsers[i].userOntology.topTypes = newOntology.topTypes;
					cfiufMaps.users.push(newOntology.cfiufMap);
				});
			}
			
			// Get the execution time for profiling purposes.
			var endTime = new Date().getTime();
			console.log("CF-IUF execution time: " + (endTime - startTime));
			
			// Record until where we've processed ontologies so far.
			var newCompletedCount = data.ontologies.length;
			
			// If we've completed more users in one iteration than there are left in our full set, 
			// hold out on updating CF-IUF until all users have been collected.
			if($scope.loadingUsers && (newCompletedCount - $scope.completedCFIUFCount) > ($scope.users.length - newCompletedCount)) {
				console.log("A new CF-IUF update will probably not complete before we've fetched all users. Wait until we're done before doing a final update.");
				// Update the new completed count.
				$scope.completedCFIUFCount = newCompletedCount;
			} else {
				// Update the new completed count.
				$scope.completedCFIUFCount = newCompletedCount;
				// Our CF-IUF Web Worker is idle for now. Make it known.
				$scope.updatingCFIUF = false;
			}
			
			
			
			// If we are done loading users and have the final CF-IUF, immediately restart the current similarity graph calculation.
			/*if(!$scope.finalizing && !$scope.loadingUsers && $scope.completedCFIUFCount === $scope.validUsers.length && $scope.validUsers.length > 1) {
				restartGraphs();
			} else {*/
				// Otherwise just try to update the graph up until the new count.
			updateSimilarityGraph($scope.completedCFIUFCount, cfiufMaps.users);
			//}
		});
	}
	
	/**
	 * Function to update the similarity graph with new data.
	 */
	function updateSimilarityGraph(userCount, cfiufMaps) {
		
		// If we're already updating, return (this shouldn't happen).
		if($scope.updatingSimilarityGraph) return;
		$scope.updatingSimilarityGraph = true;
		
		var startTime = new Date().getTime();
		
		// Build the event to send to the Web Worker.
		var ev = {};
		ev.minSim = $scope.minimumSimilarity;
		ev.cfiufMaps = cfiufMaps;
		similarityNodeCount = userCount;
		similarityLinks = [];
		
		console.log("userCount: " + userCount);
		//console.log("cfiufMaps: " + JSON.stringify(cfiufMaps));
		
		SimilarityService.doWork(ev).then(function(data) {
			
			// Simgraph done. Let's update the CF-IUF in parallel with clustering.
			$scope.updatingSimilarityGraph = false;
			
			var endTime = new Date().getTime();
			console.log("Similarity graph execution time: " + (endTime - startTime));
			
			// Finally, if we're done with everything, cluster the similarity graph.
			if(!$scope.clusteringNetwork) {
				$scope.clusterNetwork();
			} else {
				// Add a watcher:  we want to cluster the final graph, but must wait for the current clustering operation to finish.
				var removeWatchClusteringNetwork = $scope.$watch('clusteringNetwork', function() {
					if(!$scope.clusteringNetwork) {
						// clusteringNetwork has changed to false. Final update.
						$scope.clusterNetwork();
						
						// Remove this watcher.
						removeWatchClusteringNetwork();
					}
				});
			}
		});
	}
	
	/**
	 * Function to catch updates and push them to the similarity graph.
	 */
	$scope.$on('simGraphUpdate', function(event, data) { similarityLinks.push([data.i, data.j, data.similarity]); });
	
	/**
	 * Function to cluster the network, assuming we have a completed similarity graph.
	 */
	$scope.clusterNetwork = function() {
		
		// If we're already updating, return (this shouldn't happen).
		if($scope.clusteringNetwork) return;
		$scope.clusteringNetwork = true;
		
		// Get the nodes and links from the current graph.
		graph.initializeGraph(similarityNodeCount, similarityLinks, $scope.visibleUsers);
		$scope.groups = []; $scope.legend = [];
		
		var e = {};
		e.nodeCount = similarityNodeCount;
		e.links = similarityLinks;
		
		HCSService.doWork(e).then(function(data) {
			
			// We've finished calculating the cluster graph. Render the final version by removing any leftover nodes.
			graph.removeGroup(0);
			graph.start();
			
			// Allow some time before the next clustering instance starts (graph will be cleared).
			$timeout(function() { $scope.clusteringNetwork = false; }, 2000);
			
			// Build the event for CF-IUF.
			var ev = {};
			ev.groups = true;
			ev.generalityBias = $scope.generalityBias;
			ev.ontologies = [];
			
			_.each($scope.groups, function(group) {
				ev.ontologies.push(group.userOntology.ontology);
			});
			
			CFIUFGroupService.doWork(ev).then(function(newOntologies) {
				
				var labelString = "\nTopic labels:\n\n";
				$scope.legend = [];
				var i = $scope.groups.length;
				while(i--) {
					var group = $scope.groups[i];
					group.userOntology = newOntologies.ontologies[i];
					
					if(!_.isEmpty(group.userOntology.cfiufMap)) {
						var legendText = group.userOntology.topTypes[0][0];
						labelString += "Topic " + (i+1) + ": ";
						
						for(var j = 1; j < group.userOntology.topTypes.length; j++) {
							legendText += ", " + group.userOntology.topTypes[j][0];
						}
						
						$scope.legend.push({text: legendText, group: i+1});
						labelString += legendText + "\n";
					} else $scope.legend[0] = "Only one cluster found: topics cannot be determined."
				}
				
				if(!$scope.zoomed && !$scope.loadingUsers && !$scope.updatingCFIUF && !$scope.updatingSimilarityGraph) {
					$timeout(function() {
						graph.cloneGraph();
						fullGroups = _.cloneDeep($scope.groups);
						fullLegend = _.clone($scope.legend);
					}, 1000);		
				}
				
				console.log(labelString);
				
				$scope.clusteringFinished = true;
				
				// Evaluate the accuracy of the clustering result (if ground truth present).
				//if(!_.isEmpty(EvaluationService.getRelevanceScores())) EvaluationService.mcc($scope.groups, $scope.visibleUsers.length);
			});
		});
		
		graph.start();
	}
	
	/**
	 * Function to catch updates from the HCS cluster algorithm and push them to the cluster graph.
	 * cluster: { nodes: [], edges: [] }
	 */
	$scope.$on('hcsUpdate', function(event, cluster) {
		
		// Initialize the new group to assign this cluster to.
		var group = { users: [], userOntology: { ontology: {} } };
		
		_.each(cluster.nodes, function(nodeIndex) {
			// Get the user this node represents, and add him to the group.
			var user = $scope.visibleUsers[nodeIndex];
			group.users.push(user);
			
			// Merge YAGO types of this user into the YAGO types of the group.
			_.each(Object.keys(user.userOntology.ontology), function(type) {
				group.userOntology.ontology[type] = _.isNumber(group.userOntology.ontology[type]) ? 
						group.userOntology.ontology[type]+user.userOntology.ontology[type] : user.userOntology.ontology[type];
			});
			
			// Release the nodes from the rest of the graph (and temporarily from each other).
			graph.removeNodeLinks(nodeIndex);
		});
		
		// Update the groups.
		graph.start();
		$scope.groups.push(group);
		graph.updateGraph(cluster.nodes, cluster.edges, $scope.groups.length);
	});
	
	/**
	 * Function to cancel graphing and start from the similarity graph again. 
	 */
	function finalize() {
		console.log("Finalizing.");
		
		// Reset and apply final update.
		SimilarityService.restart();
		$scope.updatingSimilarityGraph = false;
		
		var removeWatchUpdatingCFIUF = $scope.$watch('updatingCFIUF', function() {
			if(!$scope.updatingCFIUF) {
				// updatingCFIUF has changed to false. Final update.
				updateCFIUF($scope.validUsers);
				
				// Remove this watcher.
				removeWatchUpdatingCFIUF();
			}
		});
	}
	
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
		$scope.init();
		$scope.loadingSeedUser = true;
		
		// Get a simple user: no tweets/traits unless already in DB.
		SimpleUser.get({ screenName: screenName, confidence: $scope.nerConfidence, 
						   support: $scope.nerSupport, concatenation: $scope.concatenation }, function(user) {

				user.$promise = null; // Hack to allow object cloning for Web Workers. Somehow "{}" is not cloneable?
				$scope.loadingSeedUser = false;
				
				if(user.screenName) {
					// User exists. Add to all users.
					$scope.users.push(user);
					
					// For the seed user, we want to show him even if invalid.
					$scope.visibleUsers.push(user);
					
					if(isValidUser(user)) {
						// User is valid, even. Check if he already has traits.
						if(user.userOntology.typeCount === 0) {
							// Nope. Update the user with tweets/traits.
							updateUser(user, true); 
						} else {
							// Yep. Process this user.
							$scope.validUsers.push(user);
							$scope.$broadcast('userUpdated');
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
	function updateUser(user, isSeed) {
		// Make known to the view that we're loading tweets/traits.
		user.loading = true;
		
		// Get a full user: get tweets/traits from Twitter/DBepdia if needed.
		User.get({ screenName: user.screenName, confidence: $scope.nerConfidence, support: $scope.nerSupport, 
			concatenation: $scope.concatenation, englishRate: $scope.minimumEnglishRate },
			function(userData) {
				// Set the basic updated user info. We can't replace the entire user object because reasons.
				user.userID = userData.userID;
				user.properties = userData.properties;
				user.tweetCount = userData.tweetCount;
				user.loading = false;
				
				// Set the new English rate calculated from the user's tweets on the server.
				user.englishRate = userData.englishRate;

				// Check user validity.
				if(isValidUser(user) && isEnglishUser(user)) {
					// User is valid and has enough English tweets. Set tweets/traits found.
					user.tweets = userData.tweets;
					user.userOntology = userData.userOntology;
					
					$scope.validUsers.push(user);
					
					if(!isSeed) {
						$scope.visibleUsers.push(user);
					}
				}
				
				// Broadcast that we have updated a user (whether valid or not).
				$scope.$broadcast('userUpdated');
		});
	}
	
	/**
	 * Function to get the all the users from a specified list of screenNames.
	 * Users are processes in parallel, with maxProcesses threads.
	 */
	$scope.updateUsers = function(i) {
		// Let the view know we're loading the users.
		$scope.loadingUsers = true;
		
		// Make sure we actually have as many users as max processes...
		var limit = Math.min($scope.maxProcesses, $scope.users.length-i);
		
		// Start by updating the first user (updateUser is an async function).
		updateUser($scope.users[i]);
		$scope.processIndex += (i+1);
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
	
	/**
	 * Get a list of users from a seed user's followers (and their followers (and their followers (and etc.))).
	 */
	$scope.getUserListFromSeed = function(user) {
		FollowersList.list({ screenName: user.screenName, userCount: $scope.userCount }, function(userScreenNameList) {
			_.each(userScreenNameList, function(screenName) {
				$scope.users.push({ screenName: screenName });
			});
			
			$scope.updateUsers(1);
		});
	}
	
	/**
	 * Get a list of users who mentioned some keyword from Twitter.
	 */
	$scope.getKeywordUsers = function(keyword) {
		KeywordUserList.list({ keyword: encodeURIComponent(keyword), userCount: $scope.userCount }, function(keywordUserList) {
			_.each(keywordUserList, function(screenName) {
				$scope.users.push({ screenName: screenName });
			});
			
			$scope.updateUsers(0);
		});
	};
	
	/**
	 * Function to update page size (used for infinite scrolling).
	 */
	$scope.loadMore = function() { $scope.pageSize = $scope.pageSize + 5; }
}]);