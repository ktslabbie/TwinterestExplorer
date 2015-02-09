var twitterWebController = angular.module('twitterWeb.controller', [])

.controller('TwitterController', ['$scope', 'SimpleUser', 'User', 'UserList', 'KeywordUsers', 'SimilarityService',
                                  'CFIUFService', 'HCSService', 'EvaluationService', 'Graph',
                                  function($scope, SimpleUser, User, UserList, KeywordUsers, SimilarityService, 
                                		   CFIUFService, HCSService, EvaluationService, Graph) {
	
	$scope.loadingSeedUser = false;
	$scope.noUserFound = false;
	$scope.loadingUsers = false;
	
	$scope.clusteringNetwork = false;
	$scope.clusteringFinished = false;
	
	$scope.updatingCFIUF = false;
	$scope.pendingFinalCFIUF = false;
	$scope.updatingSimilarityGraph = false;
	$scope.similarityGraphFinished = false;
	
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
	$scope.generalityBias = 0.4;
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
	$scope.completedSimGraphCount = 0;
	$scope.maxProcesses = 6;
	$scope.activeProcesses = 1;
	
	var graph = new Graph(1080, 640, "#graph");
	
	$scope.legend = [];
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
		$scope.pendingFinalCFIUF = false;
		$scope.updatingSimilarityGraph = false;
		$scope.similarityGraphFinished = false;
		
		while($scope.users.length > 0) $scope.users.pop();
		while($scope.validUsers.length > 0) $scope.validUsers.pop();
		while($scope.visibleUsers.length > 0) $scope.visibleUsers.pop();
		while($scope.cfiufMaps.length > 0) $scope.cfiufMaps.pop();
		while($scope.groups.length > 0) $scope.groups.pop();
		
		$scope.refreshCnt = 0;
		
		graph.clearGraph();
		
		CFIUFService.clear();
		
		$scope.processIndex = 0;
		$scope.completedCFIUFCount = 0;
		$scope.completedSimGraphCount = 0;
		$scope.maxProcesses = 6;
		$scope.activeProcesses = 1;
		lastCFIUFUsersProcessed = 0;
	}
	
	/**
	 * Function to handle the graph zoom event being fired.
	 */
	/*$scope.$on('graphZoom', function (event, data) {
		$scope.visibleUsers = $scope.groups[data.group].users;
		var ev = {};
		ev.ontologies = [];
		
		_.each($scope.visibleUsers, function(user) {
			ev.ontologies.push(user.userOntology.ontology);
		});
		
		ev.groups = true;
		ev.generalityBias = -0.6;

		CFIUFService.doWork(ev).then(function(newOntologies) {
			
			var cfiufMaps = [];
			
			_.each(newOntologies.ontologies, function(newOntology, i) {
				$scope.visibleUsers[i].userOntology = newOntology;
				cfiufMaps.push(newOntology.cfiufMap);
			});
			
			updateSimilarityGraph(cfiufMaps);
		});
	});*/
	
	/**
	 * Watch for valid user additions. When we get a new user we may want to update the CF-IUF matrix.
	 */
	$scope.$on('userUpdated', function () {
		
		// New user has been added. Check if we're already busy updating the CF-IUF matrix or not, and that we're not waiting for the final update.
		if(!$scope.updatingCFIUF && !$scope.pendingFinalCFIUF) {
			// Seems OK. Start updating the CF-IUF matrix.
			updateCFIUF($scope.validUsers);
			
		} else if(!$scope.loadingUsers) {
			
			console.log("Final 'userUpdated' has been broadcast (we're done collecting). Add a watcher for the final CF-IUF, or just do it if the final update is pending.");
			
			if($scope.pendingFinalCFIUF) {
				$scope.pendingFinalCFIUF = false;
				updateCFIUF($scope.validUsers);
			} else {
				// This means we finished gathering users. Calculate the final CF-IUF matrix. First make a watcher for the current calculation to finish.
				var removeWatchUpdatingCFIUF = $scope.$watch('updatingCFIUF', function() {
					console.log("Watcher: updating? " + $scope.updatingCFIUF);
					
					if(!$scope.updatingCFIUF) {
						// updatingCFIUF has changed to false. Final update.
						updateCFIUF($scope.validUsers);
						
						// If we were also pending, stop.
						$scope.pendingFinalCFIUF = false;
						
						// Remove this watcher.
						removeWatchUpdatingCFIUF();
					}
				});
			}
		}
		
		// Check if we're already busy updating the similarity graph or not, and actually have new data.
		if(!$scope.updatingSimilarityGraph && $scope.cfiufMaps.length > $scope.completedSimGraphCount && $scope.cfiufMaps.length > 1) {
			
			// Not busy and we have new data. Update the new count.
			$scope.completedSimGraphCount = $scope.cfiufMaps.length;
			
			// Update the graph with the current info.
			updateSimilarityGraph($scope.cfiufMaps);
		} 
	});
	
	/**
	 * Update the CF-IUF scores for all entities (can be users or groups of users) up to entities[userIndex].
	 * We do this in a Web Worker (separate processing thread), which is activated by sending it an event.
	 */
	function updateCFIUF(entities) {
		
		// If we're already updating, return (this shouldn't happen).
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
		
		// Assure we actually have any ontologies to send.
		if(event.ontologies.length > 0) {
			CFIUFService.doWork(event).then(function(data) {
				// The new CF-IUF matrix has been calculated and returned. Update the users and CF-IUF maps with the new ontologies.
				_.each(data.ontologies, function(newOntology, i) {
					$scope.validUsers[i].userOntology.cfiufMap = newOntology.cfiufMap;
					$scope.validUsers[i].userOntology.topTypes = newOntology.topTypes;
					$scope.cfiufMaps[i] = newOntology.cfiufMap;
				});
				
				// Record until where we've processed ontologies so far.
				var newCompletedCount = data.ontologies.length;
				
				// If we've completed more users in one iteration than there are left in our full set, 
				// hold out on updating CF-IUF until all users have been collected.
				if($scope.loadingUsers && (newCompletedCount - $scope.completedCFIUFCount) > ($scope.users.length - newCompletedCount)) {
					console.log("A new CF-IUF update will probably not complete before we've fetched all users. Wait until we're done before doing a final update.");
					$scope.pendingFinalCFIUF = true;
				}
				
				// Update the new completed count.
				$scope.completedCFIUFCount = newCompletedCount;
				
				// Our CF-IUF Web Worker is idle for now. Make it known.
				$scope.updatingCFIUF = false;
				
				// Get the execution time for profiling purposes.
				var endTime = new Date().getTime();
				console.log("CF-IUF execution time: " + (endTime - startTime));
				
				// If we are done loading users and have the final CF-IUF, immediately restart the current similarity graph calculation.
				if(!$scope.loadingUsers && $scope.completedCFIUFCount === $scope.validUsers.length) {
					SimilarityService.restart();
					// Reset and apply final update.
					$scope.updatingSimilarityGraph = false;
					$scope.similarityGraphFinished = true;
					
					console.log("Final simgraph update.");
					updateSimilarityGraph($scope.cfiufMaps);
				}
			});
		}
	}
	
	/**
	 * Function to update the similarity graph with new data.
	 */
	function updateSimilarityGraph(cfiufMaps) {
		
		// If we're already updating, return (this shouldn't happen).
		if($scope.updatingSimilarityGraph) return;
		$scope.updatingSimilarityGraph = true;
		
		var startTime = new Date().getTime();
		
		// Build the event to send to the Web Worker.
		var ev = {};
		ev.minSim = $scope.minimumSimilarity;
		ev.cfiufMaps = cfiufMaps;
		
		SimilarityService.doWork(ev).then(function(data) {
			
			// We've finished calculating the similarity graph.
			$scope.updatingSimilarityGraph = false;
			
			// Remove any remaining nodes/links no longer in the similarity graph.
			//graph.trim();
			
			// Render the final version.
			graph.start();
			
			var endTime = new Date().getTime();
			console.log("Similarity graph execution time: " + (endTime - startTime));
			
			// Finally, if we're done with everything, cluster the similarity graph.
			if($scope.similarityGraphFinished) {
				$scope.clusterNetwork();
			}
		});
	}
	
	/**
	 * Function to catch updates and push them to the similarity graph.
	 */
	$scope.$on('simGraphUpdate', function(event, data) {

		// Add the nodes (if not yet existing) and get their index.
		var aIndex = graph.addNode( { name: $scope.visibleUsers[data.i].screenName, group: 0, userIndex: data.i, degree: 0 } );
		var bIndex = graph.addNode( { name: $scope.visibleUsers[data.j].screenName, group: 0, userIndex: data.j, degree: 0 } );
	
		// Add the link between the nodes.
		graph.addLink(aIndex, bIndex, data.similarity);
		
		// Refresh SVG graph only one in 25 updates, to make rendering smoother.
		$scope.refreshCnt++;
		if($scope.refreshCnt % 50 == 0) graph.start();
	});
	
	/**
	 * Function to catch updates from the HCS cluster algorithm and push them to the cluster graph.
	 * cluster: { nodes: [], edges: [] }
	 */
	$scope.$on('hcsUpdate', function(event, cluster) {
		// Initialize the new group to assign this cluster to.
		var group = { users: [], userOntology: { ontology: {} } };
		
		_.each(cluster.nodes, function(node) {
			// Get the user this node represents, and add him to the group.
			var user = $scope.visibleUsers[node.userIndex];
			group.users.push(user);
			
			// Merge YAGO types of this user into the YAGO types of the group.
			_.each(Object.keys(user.userOntology.ontology), function(type) {
				group.userOntology.ontology[type] = _.isNumber(group.userOntology.ontology[type]) ? 
						group.userOntology.ontology[type]+user.userOntology.ontology[type] : user.userOntology.ontology[type];
			});
			
			graph.removeNodeLinks(node.name);
		});
		
		// Update the group-based User Frequency map.
		$scope.groups.push(group);
		graph.start();
		graph.addCluster(cluster.nodes, cluster.edges, $scope.groups.length);
	});
	
	/**
	 * Function to cluster the network, assuming we have a completed similarity graph.
	 */
	$scope.clusterNetwork = function() {
		$scope.clusteringNetwork = true;
		$scope.groups = [];
		
		// Get the nodes and links from the current graph.
		var e = {};
		e.nodes = graph.getNodes();
		e.links = graph.getLinks();
		
		graph.getForce().linkStrength(0.4);
		
		HCSService.doWork(e).then(function(data) {
			// We've finished calculating the cluster graph. Render the final version by removing any leftover nodes.
			graph.start();
			graph.removeGroup(0);
			graph.start();
			
			$scope.clusteringNetwork = false;
			
			// First, clear the graph in its entirety.
			//graph.clearGraph();
			
			// Build the event for CF-IUF.
			var ev = {};
			ev.groups = true;
			ev.ontologies = [];
			
			_.each($scope.groups, function(group) {
				ev.ontologies.push(group.userOntology.ontology);
			});
			
			ev.generalityBias = $scope.generalityBias;
			
			CFIUFService.doWork(ev).then(function(newOntologies) {
				
				var labelString = "\nTopic labels:\n\n";
				$scope.legend = [];
				
				_.each($scope.groups, function(group, i) {
					group.userOntology = newOntologies.ontologies[i];
					
					if(!_.isEmpty(group.userOntology.cfiufMap)) {
						var legendText = group.userOntology.topTypes[0][0];
						labelString += "Topic " + (i+1) + ": ";
						
						for(var j = 1; j < group.userOntology.topTypes.length; j++) {
							legendText += ", " + group.userOntology.topTypes[j][0];
						}
						
						$scope.legend[i] = legendText;
						labelString += legendText + "\n";
					}
				});
					
				console.log(labelString);
				
				$scope.clusteringFinished = true;
				
				graph.start();
				
				// Evaluate the accuracy of the clustering result (if ground truth present).
				//if(!_.isEmpty(EvaluationService.getRelevanceScores())) EvaluationService.mcc($scope.groups, $scope.visibleUsers.length);
			});
		});
	}
	
	$scope.getKeywordUsers = function(keyword) {}; // TODO
	
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
		User.get({ screenName: user.screenName, confidence: $scope.nerConfidence, support: $scope.nerSupport, 
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
	
	/**
	 * Function to update page size (used for infinite scrolling).
	 */
	$scope.loadMore = function() { $scope.pageSize = $scope.pageSize + 5; }
}]);