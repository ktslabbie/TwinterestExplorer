var twitterWebController = angular.module('twitterWeb.controller', [])

.controller('TwitterController', ['$scope', '$timeout', 'TwitterUser', 'UserTweets', 'UserOntology', 'UserNetwork',
                                  function($scope, $timeout, TwitterUser, UserTweets, UserOntology, UserNetwork) {
	
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
	
	$scope.ldaTopics = 3;
	$scope.ldaMinimumMembership = 0.5;
	
	$scope.generalityBias = 0;
	$scope.concatenation = 50;
	$scope.minimumSimilarity = 0.2;
	$scope.relevanceScores = {};
	
	$scope.legend = {};
	var vertices = [];
	var edges = [];
	
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
				//console.log((1 + parseFloat($scope.generalityBias)));
				//console.log((1 - parseFloat($scope.generalityBias)));
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
	
	$scope.calculateSimilarityGraph = function() {
		
		$scope.showSimilarityGraph = true;
		$scope.loadingSimilarityGraph = true;
		$scope.finishedSimilarityGraph = false;
		
		var worker = new Worker("js/similarity_worker.js");
		var refreshCnt = 0;

		worker.addEventListener('message', function(e) {
			if(e.data.finished) {
				$scope.$apply(function () {
					start();
					$scope.loadingSimilarityGraph = false;
					$scope.finishedSimilarityGraph = true;
					$scope.calculateDCG();
					$scope.clusterNetwork();
		        });
				
				console.log("Similarity worker terminating!")
				worker.terminate();
			} else {
				var aNode = { name: $scope.users[e.data.i].screenName, group: 1, userIndex: e.data.i };
				var bNode = { name: $scope.users[e.data.j].screenName, group: 1, userIndex: e.data.j };

				var aIndex = addNode(aNode), bIndex = addNode(bNode); 
				vertices[aIndex] = aNode; vertices[bIndex] = bNode;

				addLink({ source: aIndex, target: bIndex, value: e.data.similarity});
				edges.push({ source: aIndex, target: bIndex, value: e.data.similarity});
				
				refreshCnt++;
				if(refreshCnt % 10 == 0) start();
			}		
		}, false);
		
		var ev = {};
		ev.minSim = $scope.minimumSimilarity;
		ev.users = $scope.users;
		
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
			rankingStr += vertices[sortedDCGEdges[i][1]].name + "\t" + $scope.relevanceScores[vertices[sortedDCGEdges[i][1]].name].trim() + "\t" + sortedDCGEdges[i][2] + "\n";
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
			var evalClusters = [];

			for(var i = 0; i < e.data.clusters.length; i++) {
				var clusterSplit = e.data.clusters[i].split('-');
				$scope.groups[i+1] = {};
				$scope.groups[i+1].users = [];
				$scope.groups[i+1].sortedYagotypes = [];
				var group = $scope.groups[i+1];
				var evalGroup = [];
				
				for(var j = 0; j < clusterSplit.length; j++) {
					
					includedNodes[clusterSplit[j]] = true;
					setGroup(clusterSplit[j], i+1);
					
					var user = $scope.users[nodes[clusterSplit[j]].userIndex];
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

			for(var i = nodes.length-1; i >= 0; i--) {
				if(!includedNodes[i]) {
					removeNodeLinks(i);
					removeNodeByIndex(i);
				}
			}
			
			var groupCFIUFMap = {};
			
			var labelString = "\nTopic labels:\n\n";
			
			for(var i = 1; i <= e.data.clusters.length; i++) {
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
			force.linkDistance(60);

			start();
			
			evaluateClusteringAccuracy(evalClusters, $scope.users.length);

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
	        
	        //$(reader).on('load', convertGTToJSON);
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
	
	function applyLDA() {
		var ldaWorker = new Worker("js/lda_worker.js");

		//console.log("analysing "+sentences.length+" sentences...");
		var documents = new Array();
		var f = {};
		var vocab = new Array();
		var docCount = 0;
		
		_.each($scope.users, function(user) {
			var document = "";
			
			_.each(user.tweets, function (tweet) {
				document += tweet.content.replace(/(\r\n|\n|\r)/gm,"") + " ";
			});
			
			var words = document.split(/[\s,\"]+/);
			if(!words) return;
			var wordIndices = new Array();
			for(var wc = 0; wc < words.length; wc++) {
				var w = words[wc].toLowerCase().replace(/[^a-z\'A-Z0-9 ]+/g, '');
				//TODO: Add stemming
				if (w=="" || w.length==1 || stopwords[w] || w.indexOf("http")==0) continue;
				if (f[w]) {
					f[w] = f[w]+1;
				} 
				else if(w) { 
					f[w] = 1; 
					vocab.push(w); 
				};	
				wordIndices.push(vocab.indexOf(w));
			}
			if (wordIndices && wordIndices.length > 0) {
				documents[docCount++] = wordIndices;
			}
		});
		
		var V = vocab.length;
		var M = documents.length;
		var K = $scope.ldaTopics;
		var alpha = 50 / K;  // per-document distributions over topics (0.1)
		var beta = 200 / V;  // per-topic distributions over words (0.01)
		
		var ev = {};
		ev.configure = {docs: documents, v: V, iterations: 2000, burnIn: 400, thinInterval: 100, sampleLag: 10};
		ev.gibbs = { K: K, alpha: alpha, beta: beta };
		ldaWorker.postMessage(ev);
		
		ldaWorker.addEventListener('message', function(e) {
			if(e.data.finished) {
				$scope.$apply(function () {
					
					var theta = e.data.theta;
					var phi = e.data.phi;
					var output = "";
					
					//topics
					var topTerms = 20;
					
					for (var k = 0; k < phi.length; k++) {
						var tuples = new Array();
						for (var w = 0; w < phi[k].length; w++) {
							 tuples.push("" + phi[k][w] + "_" + vocab[w]);
						}
						tuples.sort().reverse();
						if(topTerms > vocab.length) topTerms = vocab.length;
						
						for (var t = 0; t < topTerms; t++) {
							var topicTerm = tuples[t].split("_")[1];
							var prob = parseFloat(tuples[t].split("_")[0]);
							
							if (prob < 0.00000001) continue;
							output += "topic " + k + ": " + topicTerm + " = " + (prob*100)  + "%\n";
						}
					}
					
					console.log(output);
					
					var output = "Topics/users:\n\n";
					var topicUsers = new Array();
					for (var i = 0; i < K; i++)  {
						topicUsers[i] = new Array();
					}
					
					$scope.ldaMinimumMembership = 1/($scope.ldaTopics - ( $scope.ldaTopics / 3) )
					
					for (var m = 0; m < theta.length; m++) {
						
						var topTopic = 0;
						var topPercent = 0;
						
						output += $scope.users[m].screenName + " - ";
						
						for (var k = 0; k < theta[m].length; k++) {
							var topicPercent = theta[m][k];
							
							output += "topic " + k + ": " + topicPercent + "\t";
							
							if(topicPercent > topPercent) {
								topPercent = topicPercent;
								topTopic = k;
							}

						}
						
						if(topPercent > $scope.ldaMinimumMembership) topicUsers[topTopic].push($scope.users[m].screenName);
						
						output += '\n';
					}
					
					console.log(output);
					
					evaluateClusteringAccuracy(topicUsers, $scope.users.length);
		        });
				
				console.log("LDA worker terminating!")
				ldaWorker.terminate();
			}	
		}, false);
	}
}]);

