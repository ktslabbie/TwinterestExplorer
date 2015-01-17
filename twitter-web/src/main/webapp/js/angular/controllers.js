var twitterWebController = angular.module('twitterWeb.controller', [])

.controller('TwitterController', ['$scope', '$timeout', 'TwitterUser', 'UserTweets', 'UserOntology', 'UserNetwork', 'UserList', 'SimilarityService',
                                  'CFIUFService', 'HCSService', 'EvaluationService', 'Graph',
                                  function($scope, $timeout, TwitterUser, UserTweets, UserOntology, UserNetwork, UserList, SimilarityService, 
                                		   CFIUFService, HCSService, EvaluationService, Graph) {
	
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
	$scope.updatingCFIUF = false;
	$scope.finalCFIUFUpdate = false;
	
	$scope.startTime = 0;
	$scope.endTime = 0;
	
	$scope.users = [];
	$scope.validUsers = [];
	$scope.groups = [];
	$scope.screenName = "bnmuller";
	$scope.pageSize = 0;
	$scope.refreshCnt = 0;
	$scope.tweetsPerUser = 300;
	$scope.userCount = 200;
	$scope.minimumEnglishRate = 0.8;
	
	// Named Entity Recognition settings.
	$scope.nerConfidence = 0;
	$scope.nerSupport = 0;
	$scope.generalityBias = 0;
	$scope.concatenation = 25;
	
	// Minimum similarity threshold.
	$scope.minimumSimilarity = 0.2;
	
	// Twitter user restrictions.
	$scope.maxSeedUserFollowers = 50000;
	$scope.minFollowers = 0;
	$scope.maxFollowers = 10000;
	$scope.minFollowing = 0;
	$scope.maxFollowing = 10000;
	$scope.minTweets = 300;
	$scope.maxTweets = 1000000;
	
	$scope.processIndex = 0;
	$scope.maxProcesses = 6;
	$scope.activeProcesses = 1;
	var graph;
	
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
		$scope.showAllUsers = false;
		$scope.showSimilarityGraph = false;
		$scope.loadingSimilarityGraph = false;
		$scope.finishedSimilarityGraph = false;
		$scope.clusteringNetwork = false;
		$scope.updatingCFIUF = false;
		
		$scope.users = [];
		$scope.validUsers = [];
		$scope.visibleUsers = [];
		$scope.groups = [];
		$scope.refreshCnt = 0;
		
		graph = new Graph(1280, 720, "#graph");
		
		$scope.ufMap = {};
		$scope.zoomedUFMap = {};
		$scope.gfMap = {};
	}
	
	/* Function to check if the active user has changed (exact name no longer in input box). */
	$scope.userChanged = function() {
		if(!$scope.screenName || $scope.users.length === 0 || !$scope.users[0].screenName) return false;
		return $scope.screenName.toLowerCase() != $scope.users[0].screenName.toLowerCase();
	}
	
	$scope.$on('graphZoom', function (event, data) {
		$scope.visibleUsers = $scope.groups[data.group].users;
		$scope.zoomedUFMap = {};
		_.each($scope.visibleUsers, function(user, i) {
			updateUFMap($scope.zoomedUFMap, user.userOntology.ontology);
		});
		
		console.log("Zoomed UF map: " + JSON.stringify($scope.zoomedUFMap));

		var ev = {};
		ev.entities = $scope.visibleUsers;
		ev.ufMap = $scope.zoomedUFMap;
		ev.index = $scope.visibleUsers.length-1;
		ev.generalityBias = -0.6;

		CFIUFService.doWork(ev).then(function(newOntologies) {
			_.each(newOntologies, function(newOntology, i) {
				$scope.visibleUsers[i].userOntology = newOntology;
				//console.log("NEW ONTOLOGY: " + JSON.stringify($scope.visibleUsers[i].userOntology));
			});
			//$scope.minimumSimilarity = 0.05;
			$scope.minimumSimilarity = 0.03;
			
			$scope.calculateSimilarityGraph();
		});
	});
	
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
	 * Update the user frequency map with new values.
	 */
	function updateUFMap(ufMap, typeMap) {
		for(type in typeMap) ufMap[type] = ufMap[type] ? ufMap[type] + 1 : 1;
	}
	
	/**
	 * Update the CF-IUF scores for all entities (can be users or groups of users) up to entities[userIndex].
	 */
	function updateCFIUF(entities, ufMap, index) {
		if(!$scope.updatingCFIUF || $scope.finalCFIUFUpdate) {
			$scope.updatingCFIUF = true;
			
			var ev = {};
			ev.entities = entities;
			ev.ufMap = ufMap;
			ev.index = index;
			ev.generalityBias = $scope.generalityBias;
			
			CFIUFService.doWork(ev).then(function(newOntologies) {
				/*if($scope.finalCFIUFUpdate) {
					console.log("Final CFIUF update...");
				}*/
				
				
				_.each(newOntologies, function(newOntology, i) {
					//$scope.users[i].userOntology = newOntology;
					$scope.validUsers[i].userOntology = newOntology;
				});
				
				$scope.updatingCFIUF = false;
				if($scope.finalCFIUFUpdate) {
					//console.log("Turning off final CFIUF update...");
					$scope.finalCFIUFUpdate = false;
				}
			});
		}
	}
	
	/**
	 * Update the CF-IUF scores for all entities (can be users or groups of users) up to entities[userIndex].
	 */
	function updateCFIUFGroup(group, ufMap, index) {
		var ev = {};
		ev.entities = group;
		ev.ufMap = ufMap;
		ev.index = index;
		ev.generalityBias = $scope.generalityBias;
		
		CFIUFService.doWork(ev).then(function(newOntologies) {
			_.each(newOntologies, function(newOntology, i) {
				$scope.groups[i].userOntology = newOntology;
			})
		});
	}
	
	/**
	 * Function to start similarity graph calculation and visualization.
	 */
	$scope.calculateSimilarityGraph = function() {
		
		$scope.showSimilarityGraph = true;
		$scope.loadingSimilarityGraph = true;
		$scope.finishedSimilarityGraph = false;
		
		var ev = {};
		ev.minSim = $scope.minimumSimilarity;
		
		//if(!users) users = $scope.validUsers;
		ev.users = $scope.visibleUsers;
		graph.clearNodes();
		
		SimilarityService.doWork(ev).then(function(data) {
			
			// We've finished calculating the similarity graph. Render the final version.
			graph.start();
			$scope.loadingSimilarityGraph = false;
			$scope.finishedSimilarityGraph = true;
			
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
		
		//console.log("Updating graph: " + JSON.stringify(data));
		
		// Create the new, ungrouped nodes.
		var aNode = { name: $scope.visibleUsers[data.i].screenName, group: 0, userIndex: data.i };
		var bNode = { name: $scope.visibleUsers[data.j].screenName, group: 0, userIndex: data.j };
		
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
			graph.clearGraph();

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
				updateUFMap($scope.gfMap, group.userOntology.ontology);
				$scope.groups.push(group);
				graph.addCluster(clusterNodes, clusterEdges, i);
			});
			
			// Update the CF-IUF maps for each group. Top types will be calculated too.
			//updateCFIUFGroup($scope.groups, $scope.gfMap, $scope.groups.length-1);

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
				var legend = {};
				
				//if($scope.groups.length > 1) {
					_.each($scope.groups, function(group, i) {
						group.userOntology = newOntologies[i];
						
						var legendText = group.userOntology.topTypes[0][0];
						labelString += "Topic " + (i+1) + ": ";
						
						for(var j = 1; j < group.userOntology.topTypes.length; j++) {
							legendText += ", " + group.userOntology.topTypes[j][0];
						}
						
						legend[i] = legendText;
						labelString += legendText + "\n";
					});
				//}
					
				console.log(labelString);
				
				// Update force settings and refresh.
				graph.getForce().charge(-90).linkDistance(90);
				graph.start(legend);

				// Evaluate the accuracy of the clustering result (if ground truth present).
				//if(!_.isEmpty(EvaluationService.getRelevanceScores())) EvaluationService.mcc($scope.groups, $scope.visibleUsers.length);
			});
		});
	}
	
	function prepareUser(user, index) {
		$scope.users.push(user);

		if(user['protected'] || user.statusesCount < $scope.minTweets) return;
		
		if(user.tweets.length > 0) {
			if(user.englishRate < $scope.minimumEnglishRate) return;
			getTweetStats(user);
			
			//console.log(JSON.stringify(user));
			
			if(_.isEmpty(user.userOntology.ontology)) {
				$scope.getOntology(user, index);
			} else {
				$scope.validUsers.push(user);
				$scope.visibleUsers.push(user);
				updateUFMap($scope.ufMap, user.userOntology.ontology);
				//updateCFIUF($scope.validUsers, $scope.ufMap, index);
			}
			
		} else {
			$scope.getOntology(user, index);
		}
		
		user.imageVisible = true;
	}
	
	$scope.getUser = function() {
		init();
		
		$scope.noUserFound = false;		
		$scope.loadingUser = true;
		
		var userJSON = TwitterUser.user({ name: $scope.screenName }, function() {
			$scope.loadingUser = false;
			
			if(userJSON.user) {
				prepareUser(userJSON.user, 0);
				//updateCFIUF($scope.validUsers, $scope.ufMap, 0);
			}
			else
				$scope.noUserFound = true;
		});
	};
	
	var getNewUser = function(index) {
		
		var userJSON = TwitterUser.user({ name: $scope.screenName }, function() {
			
			if(userJSON.user) {
				prepareUser(userJSON.user, index);
				updateCFIUF($scope.validUsers, $scope.ufMap, index);
			}
		});
	};
	
	$scope.getOntology = function(user, i) {
		user.loadingTweets = true;
		user.loadingOntology = true;
		
		var ontologyJSON = UserOntology.ontology({ name: user.screenName, concatenation: $scope.concatenation, index: i,
													c: $scope.nerConfidence, s: $scope.nerSupport}, function() {
			
			//user = ontologyJSON.user;
			user.englishRate = ontologyJSON.user.englishRate;
			user.tweets = ontologyJSON.user.tweets;
			getTweetStats(user);
			
			user.loadingTweets = false;
			
			if(user.englishRate < $scope.minimumEnglishRate) {
				user.loadingOntology = false;
				return;
			}
			
			$scope.validUsers.push(user);
			$scope.visibleUsers.push(user);
			user.userOntology = ontologyJSON.user.userOntology;
			
			updateUFMap($scope.ufMap, user.userOntology.ontology);
			updateCFIUF($scope.validUsers, $scope.ufMap, i);
			
			user.loadingOntology = false;
		});
	};
	
	$scope.getTargetUserNetwork = function(user) {
		$scope.loadingNetwork = true;
		
		var networkJSON = UserNetwork.network({ name: user.screenName, userCount: $scope.userCount }, function() {
			
			for(var i = 1; i < networkJSON.network.length; i++) {
				$scope.users.push(networkJSON.network[i]);
			}

			$scope.loadingNetwork = false;
			$scope.pageSize = 10;
			$scope.processIndex = 1;
		});
	};
	
	$scope.getDCGNetwork = function(userRelevanceList) {
		
		$scope.loadingNetwork = true;
		$scope.processing = true;
		
		/*_.each(userRelevanceList, function(screenName, i) {
			getNewUser(i+1);
			if(i % 10 === 0) updateCFIUF($scope.validUsers, $scope.ufMap, i);
		});*/
		
		var userListJSON = UserList.list({ list: userRelevanceList }, function() {
			_.each(userListJSON.twitterUserList, function(user, i) {
				
				prepareUser(user, i);
			});
			
			updateCFIUF($scope.validUsers, $scope.ufMap, $scope.validUsers.length-1);
			
			//$scope.finished = true;
			$scope.loadingNetwork = false;
			
			$scope.$watch('updatingCFIUF', function() {
				if($scope.updatingCFIUF === false) {
					//$scope.processing = false;
					$scope.finished = true;
				}
			});
			
			$scope.pageSize = 10;
			//$scope.processNetwork(1);
			
			//updateCFIUF($scope.users, $scope.ufMap, $scope.users.length-1);
			
			//prepareTMTData();
		});
	};
	
	$scope.processNetwork = function(index) {
		$scope.startTime = new Date().getTime();
		console.log("Starting time: " + $scope.startTime);
		
		$scope.processNetworkRec(index);
		
		for(var i = 2; i <= $scope.maxProcesses; i++) {
			$scope.processNetworkRec(i);
			$scope.processIndex++;
			$scope.activeProcesses++;
		}
	}
	
	$scope.processNetworkRec = function(index) {
		console.log("index: " + index);
		
		$scope.processing = true;
		
		var user = $scope.users[index];

		user.loadingTweets = true;
		//var tweetsJSON = UserTweets.tweets({ name: $scope.users[index].screenName }, function() {
			
		user.loadingOntology = true;
		
		var ontologyJSON = UserOntology.ontology({ name: user.screenName, index: index, concatenation: $scope.concatenation, 
													c: $scope.nerConfidence, s: $scope.nerSupport }, function() {
			
			
			user.englishRate = ontologyJSON.user.englishRate;
			
			if(!user['protected'] && user.englishRate >= $scope.minimumEnglishRate) {
			
				user.tweets = ontologyJSON.user.tweets;
				getTweetStats(user);
															
				user.userOntology = ontologyJSON.user.userOntology;
				
				updateUFMap($scope.ufMap, user.userOntology.ontology);
				
				$scope.validUsers.push(user);
				$scope.visibleUsers.push(user);
				//if(index % 1 == 0) {
				updateCFIUF($scope.validUsers, $scope.ufMap, $scope.validUsers.length-1);
				//}
			}
			
			user.loadingTweets = false;
			user.loadingOntology = false;
			
			if($scope.processIndex < $scope.users.length-1 && $scope.activeProcesses <= $scope.maxProcesses) {
				$scope.processIndex++;
				index = $scope.processIndex;
				
				$timeout(function() {
					$scope.processNetworkRec(index);
			    }, 0);
				
				if(index < $scope.users.length-2 && $scope.activeProcesses <= $scope.maxProcesses-1) {
					$scope.processIndex++;
					$scope.activeProcesses++;
					
					$timeout(function() {
						$scope.processNetworkRec(index+1);
				    }, 0);
				}
				
				
			} else {
				if(!$scope.finished && $scope.activeProcesses <= 1) {
					//if((index) % 10 != 0) {
					$timeout(function() {
						console.log("Final CF-IUF update at index " + ($scope.validUsers.length-1) );
						$scope.finalCFIUFUpdate = true;
						updateCFIUF($scope.validUsers, $scope.ufMap, $scope.validUsers.length-1);
					}, 4000);
						
					//}
					$scope.finished = true;
					$scope.endTime = new Date().getTime();
					console.log("Ending time: " + $scope.endTime);
					var time = $scope.endTime - $scope.startTime;
					console.log("Execution time: " + time);
					
					//console.log("UF Map: " + JSON.stringify($scope.ufMap));
				}
				
				$scope.activeProcesses--;
				//prepareTMTData();
			}
		});
		//});
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
	
	function prepareTMTData() {
		console.log("Preparing TMT data.");
		
		var allCount = 0;
		var csv = "";
		
		_.each($scope.users, function(user) {				
			_.each(user.tweets, function (tweet) {
				allCount++;
				csv += '' + allCount + ',' + user.screenName + ',"' + tweet.content.replace(/(\r\n|\n|\r|")/gm,"").trim() + '"\n';
			});
		});
		
		console.log(csv);
	}
}]);

