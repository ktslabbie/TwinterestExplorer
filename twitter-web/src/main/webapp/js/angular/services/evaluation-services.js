var evalService = angular.module('twitterWeb.EvaluationServices', []);

/**
 *  Service for performing experiments and evaluation.
 */
evalService.factory("EvaluationService", function() {
	
	var relevanceScores = {};
	
	var GROUND_TRUTH = {
		USERS: {},
		TOPICS: {},
		SUBTOPICS: {},
		CLUSTERS: [],
		SUBCLUSTERS: [],
		GROUPS: [],
		SUBGROUPS: [],
	}

    /**
     * Calculate the Normalized Mutual Information given:
     * @param groups the groups found
     * @param gtClusters ground truth clusters
     * @param clusters discovered clusters
     * @param ka number of topics in ground truth
     * @param userCount number of users found
     */
    function NMI(groups, gtClusters, clusters, ka, userCount, GT) {
        var nhl = new Array();
        var kb = groups.length;

        for (var i = 0; i < ka; i++)  {
            nhl[i] = new Array();
            for (var j = 0; j < kb; j++)
                nhl[i][j] = 0;
        }

        var ix = 1, n = userCount;

        while(n > 0) {
            if(gtClusters[0] == ix && clusters[0] == ix) {
                nhl[ix-1][ix-1]++;
                gtClusters.shift();
                clusters.shift();
                n--;
            } else if(gtClusters[0] == ix) {
                var ixb = clusters[0];
                if(ixb > kb) break;
                while(gtClusters[0] == ix) {
                    nhl[ix-1][ixb-1]++;
                    gtClusters.shift();
                    clusters.shift();
                    n--;
                    if(clusters.length == 0) break;
                    if(clusters[0] != ixb) ixb++;
                }
                ix++;
            } else if(clusters[0] == ix) {
                var ixa = gtClusters[0];
                if(ixa > ka) break;
                while(clusters[0] == ix) {
                    nhl[ixa-1][ix-1]++;
                    gtClusters.shift();
                    clusters.shift();
                    n--;
                    if(gtClusters.length == 0) break;
                    if(gtClusters[0] != ixa) ixa++;
                }
                ix++;
            } else {
                ix++;
            }
        }

        var nmiNum = 0, nmiDem = 0;

        for (var h = 0; h < ka; h++)  {
            for (var l = 0; l < kb; l++) {
                var hi = nhl[h][l]*Math.log( (userCount*nhl[h][l]) / (GT.SUBGROUPS[h]*groups[l].users.length) );
                if(!isNaN(hi)) nmiNum += hi;
            }
        }

        var nmiLeft = 0, nmiRight = 0;

        for (var h = 0; h < ka; h++)  {
            var hi = GT.SUBGROUPS[h]*(-Math.log( GT.SUBGROUPS[h] / userCount));
            if(!isNaN(hi)) nmiLeft += hi;
        }


        for (var l = 0; l < kb; l++) {
            var hi = groups[l].users.length*(-Math.log( groups[l].users.length / userCount) ) ;
            if(!isNaN(hi)) nmiRight += hi;
        }

        nmiDem = Math.sqrt(nmiLeft*nmiRight);
        return nmiNum / nmiDem;
    }

	
	return {
		
		/**
		 * This is a function to generate a random evaluation sample.
		 * Pick 7 (sub-)topics uniformly at random from the ground truth.
		 * Then for each topic, pick 50% to 100% of its users uniformly at random.
		 * Then set this as the current ground truth for evaluation.
		 * 
		 * returns the random user GT
		 */
		generateEvaluationSample: function() {
			
			var GT = _.cloneDeep(GROUND_TRUTH);
			var topics = Object.keys(GT.SUBTOPICS);
			
			// We pick 7 topics. This is the same as deleting 4 from the original set.
			var t = 4;
			while(t--) {
				var pick = Math.floor(Math.random()*topics.length);
				delete GT.SUBTOPICS[topics[pick]];
				topics.splice(pick, 1);
			}
			
			// We pick 50-100% of the users of each set.
			_.each(GT.SUBTOPICS, function(topic) {
				userList = Object.keys(topic);
				var deletions = Math.floor(Math.random()*(userList.length/2+1));
				
				while(deletions--) {
					var pick = Math.floor(Math.random()*userList.length);
					delete topic[userList[pick]];
					delete GT.USERS[userList[pick]];
					userList.splice(pick, 1);
				}
			});
			
			return GT;
		},
		
		randomClustering: function(groups, userCount) {
			
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
			
			EvaluationService.clusterEvaluation(randomGroups, userCount);
		},
		
		/* Converts an uploaded ground truth file to a collection of sets for easy lookup. */
		convertGTToJSON: function(file) {
			var results;

			var currentTopic = "";
			var currentSubTopic = "";
			var topicIndex = 0, subTopicIndex = 0;

			if (file && file.length) {
				results = file.split("\n");

				_.each(results, function(result) {
					result = result.trim();

					if(result == "") return;

					else if(result.charAt(0) == ":") {
						currentTopic = result.substring(1,result.length-1);
						GROUND_TRUTH.TOPICS[currentTopic] = {};
						GROUND_TRUTH.GROUPS.push(0);
						topicIndex++;
					} else if(result.charAt(0) == "-") {
						currentSubTopic = result.substring(1,result.length-1);
						GROUND_TRUTH.SUBTOPICS[currentSubTopic] = {};
						GROUND_TRUTH.SUBGROUPS.push(0);
						subTopicIndex++;
					} else {
						var currentUser = result.split(",")[0];
						var currentScore = result.split(",")[1];
						GROUND_TRUTH.CLUSTERS.push(topicIndex);
						GROUND_TRUTH.SUBCLUSTERS.push(subTopicIndex);
						GROUND_TRUTH.GROUPS[topicIndex-1]++;
						GROUND_TRUTH.SUBGROUPS[subTopicIndex-1]++;

						if(GROUND_TRUTH.USERS[currentUser] == null) {
							GROUND_TRUTH.USERS[currentUser] = {};
							//userCount++;
						}

						GROUND_TRUTH.USERS[currentUser][currentTopic] = currentScore;
						GROUND_TRUTH.USERS[currentUser][currentSubTopic] = currentScore;
						GROUND_TRUTH.TOPICS[currentTopic][currentUser] = currentScore;
						GROUND_TRUTH.SUBTOPICS[currentSubTopic][currentUser] = currentScore;
					}
				});
			}

		},
		
		getRelevanceScores: function() {
			return relevanceScores;
		},
	
		setRelevanceScores: function(pRelevanceScores) {
			relevanceScores = pRelevanceScores;
		},

		/**
         * Function to evaluate nDCG ranking in regard to some seed user.
         * Input: similarity graph (vertices and edges).
        **/
        dcg: function(vertices, edges) {
        	console.log("Edges: " + edges);
        	var dcgEdges = _.sortBy(_.filter(edges, function(edge) { edge.source.index == 0 }), function(e) { return e.value; });
    		var rankingStr = "Similarity ranking (" + dcgEdges.length + " entries):\n";
    		
    		for(var i = dcgEdges.length; i--;) {
    			rankingStr += 	vertices[dcgEdges[i].target.index].name + "\t" +
    							relevanceScores[vertices[dcgEdges[i].target.index].name].trim() + "\t" +
    							dcgEdges[i].value + "\n";
    		}
    		
    		console.log(rankingStr);
    		
    		var firstEdge = dcgEdges[dcgEdges.length-1];
    		var topK = [3, 5, 10, 25, 50];
    		var evaluationString = "";
    		
    		_.each(topK, function(k) {
    			if(k <= dcgEdges.length) {
    			
    				var reli = 0.0;
    				var binreli = 0.0;
    				var dcg = parseFloat(relevanceScores[vertices[firstEdge.target.index].name]);
    				var binDCG = (parseFloat(relevanceScores[vertices[firstEdge.target.index].name]) > 0) ? 2 : 0;
    				var idcg = 2;
    				var index = 1;
    				
    				for(var i = dcgEdges.length-1; i--;) {
    					var edge = dcgEdges[i];
    					
    					if(index < k) {
    						reli = parseFloat(relevanceScores[vertices[edge.target.index].name]);
    						binreli = (parseFloat(relevanceScores[vertices[edge.target.index].name]) > 0) ? 2 : 0;
    						
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
        },
        
        /** 
         * Function to evaluate communities based on accuracy.
         * Input: array of user arrays (clusters with members).
         **/
        clusterEvaluation: function(groups, userCount, gt) {

        	var output = "";
        	var GT = gt || GROUND_TRUTH;
        	var scores = {};

        	if(_.isEmpty(GT.USERS) || _.isEmpty(GT.TOPICS)) {
        		console.log("ERROR: cannot evaluate clusters. Upload a ground truth first!");
        		return;
        	}

        	var clusterUserCount = 0;
        	var clusterUserSet = {};
        	var clusters = [];
        	var count = 0;

        	//output += "\nUsers per topic:\n\n";
        	_.each(groups, function(group, i) {
        		//output += "Topic " + (i+1) + ": ";

        		_.each(group.users, function(user) {
        			//output += user.screenName + ", ";
        			clusterUserCount++;
        			clusterUserSet[user.screenName] = true;
        			count++;
        			clusters.push((i+1));
        		});

        		//output += "\n";
        	});
        	
        	//output += "\n";

        	var nullCluster = { users: [] };

        	_.each(GT.USERS, function(val, user) {
        		if(!(user in clusterUserSet)) {
        			count++;
        			nullCluster.users.push({ screenName: user });
        			clusters.push((groups.length+1));
        		}
        	});
        	
            if(nullCluster.users.length > 0) groups.push(nullCluster);
        	
        	var gtClusters = _.clone(GT.SUBCLUSTERS);
        	//console.log("Total users: " + count);
        	//console.log("GT_CLUSTERS length: " + GT_CLUSTERS.length);
        	//console.log("GT_SUBCLUSTERS length: " + gtClusters.length);
        	//console.log("clusters length: " + clusters.length);
        	
        	//var ka = Object.keys(GT_TOPICS).length;
        	var ka = Object.keys(GT.SUBTOPICS).length;
        	var n = userCount;
        	
        	//console.log("NMI: we have " + n + " users, gtGroups: " + ka + ", groups: " + groups.length);
        	//console.log("NMI: GT_CLUSTERS: " + JSON.stringify(GT_CLUSTERS));
        	//console.log("NMI: GT_SUBCLUSTERS: " + JSON.stringify(gtClusters));
        	//console.log("NMI: clusters: " + JSON.stringify(clusters));

        	//scores["nmi"] = NMI(groups, gtClusters, clusters, ka, n, GT);
        	//output += "NMI: NMI final: " + scores.nmi + "\n";

        	var tp = 0, fp = 0, fn = 0, tn = 0;
            var done = false;

        	_.each(groups, function(group) {
        		for(var i = 0; i < group.users.length-1; i++) {
        			var userA = group.users[i].screenName;
        			for(var j = i+1; j < group.users.length; j++) {
        				var userB = group.users[j].screenName;

        				for(var topic in GT.SUBTOPICS) {
        					if(GT.SUBTOPICS[topic][userA] && GT.SUBTOPICS[topic][userB]) {
        						tp++;
        						done = true;
        						break;
        					}
        				}

        				if(!done) fp++;
        				done = false;
        			}
        		}
        	});

        	for(var i = 0; i < groups.length-1; i++) {
        		var clusterA = groups[i].users;
        		for(var j = i+1; j < groups.length; j++) {
        			var clusterB = groups[j].users;
        			_.each(clusterA, function(userA) {
        				_.each(clusterB, function(userB) {
        					for(var topic in GT.SUBTOPICS) {
        						if(GT.SUBTOPICS[topic][userA.screenName] && GT.SUBTOPICS[topic][userB.screenName]) {
        							fn++;
        							done = true;
        							break;
        						}
        					}

        					if(!done) tn++;
        					done = false;
        				});
        			});
        		}
        	}

        	// Alternative way to calculate tn: (n*(n-1)/2) - (a+b+c)
        	//output += "TPs: " + tp + ", FPs: " + fp + ", FNs: " + fn + ", TNs: " + tn + "\n";
        	scores["accuracy"] = (tp+tn)/(tp+tn+fn+tn);
        	
        	output += "Accuracy: " + scores.accuracy + "\n";

        	scores["mcc"] = (tp*tn - fp*fn) / Math.sqrt( (tp+fp)*(tp+fn)*(tn+fp)*(tn+fn) );
        	scores["precision"] = tp / (tp+fp);
        	scores["recall"] = tp / (tp+fn);
        	scores["fscore"] = 2*((scores.precision*scores.recall)/(scores.precision+scores.recall));

        	output += "ClusterUsers / AllUsers: " + clusterUserCount + " / " + userCount + "\n";
        	var corr = clusterUserCount / userCount;
        	
        	output += "Precision: " + scores.precision + ", corrected for user count: " + scores.precision*corr + "\n";
        	output += "Recall: " + scores.recall + ", corrected for user count: " + scores.recall*corr + "\n";
			output += "F-score: " + scores.fscore + ", corrected for user count: " + scores.fscore*corr + "\n";
			output += "MCC: " + scores.mcc + ", corrected for user count: " + scores.mcc*corr + "\n";

        	console.log(output);
        	
        	return scores;
        },
        
        prepareTMTData: function(users) {
    		console.log("Preparing TMT data.");
    		
    		var allCount = 0;
    		var userCount = 0;
    		var csv = "";
    		var lastUser;
    		
    		_.each(users, function(user) {
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
    };
});