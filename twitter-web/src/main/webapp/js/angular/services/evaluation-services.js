var evalService = angular.module('twitterWeb.EvaluationServices', []);

/**
 *  Service for performing experiments and evaluation.
 */
evalService.factory("EvaluationService", function() {
	
	var relevanceScores = {};
	var GT_USERS = {};
	var GT_TOPICS = {};
	var GT_SUBTOPICS = {};
	var GT_CLUSTERS = [];
	var GT_SUBCLUSTERS = [];
	var GT_GROUPS = [];
	var GT_SUBGROUPS = [];

    /**
     * Calculate the Normalized Mutual Information given:
     * @param groups the groups found
     * @param gtClusters ground truth clusters
     * @param clusters discovered clusters
     * @param ka number of topics in ground truth
     * @param userCount number of users found
     */
    function NMI(groups, gtClusters, clusters, ka, userCount) {
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
                var hi = nhl[h][l]*Math.log( (userCount*nhl[h][l]) / (GT_SUBGROUPS[h]*groups[l].users.length) );
                if(!isNaN(hi)) nmiNum += hi;
            }
        }

        var nmiLeft = 0, nmiRight = 0;

        for (var h = 0; h < ka; h++)  {
            var hi = GT_SUBGROUPS[h]*(-Math.log( GT_SUBGROUPS[h] / userCount));
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
			var cnt = 0;

			if (file && file.length) {
				results = file.split("\n");

				_.each(results, function(result) {
					result = result.trim();

					if(result == "") return;

					else if(result.charAt(0) == ":") {
						currentTopic = result.substring(1,result.length-1);
						GT_TOPICS[currentTopic] = {};
						GT_GROUPS.push(0);
						topicIndex++;
					} else if(result.charAt(0) == "-") {
						currentSubTopic = result.substring(1,result.length-1);
						GT_SUBTOPICS[currentSubTopic] = {};
						GT_SUBGROUPS.push(0);
						subTopicIndex++;
					} else {
						var currentUser = result.split(",")[0];
						var currentScore = result.split(",")[1];
						GT_CLUSTERS.push(topicIndex); cnt++;
						GT_SUBCLUSTERS.push(subTopicIndex);
						GT_GROUPS[topicIndex-1]++;
						GT_SUBGROUPS[subTopicIndex-1]++;

						if(GT_USERS[currentUser] == null) {
							GT_USERS[currentUser] = {};
							//userCount++;
						}

						GT_USERS[currentUser][currentTopic] = currentScore;
						GT_USERS[currentUser][currentSubTopic] = currentScore;
						GT_TOPICS[currentTopic][currentUser] = currentScore;
						GT_SUBTOPICS[currentSubTopic][currentUser] = currentScore;
					}
				});
			}

			//console.log("Added " + cnt + " users in total.");
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
        	
        	var dcgEdges = _.sortBy(_.filter(edges, function(edge) { edge.source.userIndex == 0 }), function(e) { return e.value; });
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
        clusterEvaluation: function(groups, userCount) {

        	var output = "";

        	if(_.isEmpty(GT_USERS) || _.isEmpty(GT_TOPICS)) {
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

        	_.each(GT_USERS, function(val, user) {
        		if(!(user in clusterUserSet)) {
        			count++;
        			nullCluster.users.push({ screenName: user });
        			clusters.push((groups.length+1));
        		}
        	});
        	
            if(nullCluster.users.length > 0) groups.push(nullCluster);
        	
        	var gtClusters = _.clone(GT_SUBCLUSTERS);
        	//console.log("Total users: " + count);
        	//console.log("GT_CLUSTERS length: " + GT_CLUSTERS.length);
        	//console.log("GT_SUBCLUSTERS length: " + gtClusters.length);
        	//console.log("clusters length: " + clusters.length);
        	
        	//var ka = Object.keys(GT_TOPICS).length;
        	var ka = Object.keys(GT_SUBTOPICS).length;
        	var n = userCount;
        	
        	//console.log("NMI: we have " + n + " users, gtGroups: " + ka + ", groups: " + groups.length);
        	//console.log("NMI: GT_CLUSTERS: " + JSON.stringify(GT_CLUSTERS));
        	//console.log("NMI: GT_SUBCLUSTERS: " + JSON.stringify(gtClusters));
        	//console.log("NMI: clusters: " + JSON.stringify(clusters));

        	var nmi = NMI(groups, gtClusters, clusters, ka, n);
        	output += "NMI: NMI final: " + nmi + "\n";

        	var tp = 0, fp = 0, fn = 0, tn = 0;
            var done = false;

        	_.each(groups, function(group) {
        		for(var i = 0; i < group.users.length-1; i++) {
        			var userA = group.users[i].screenName;
        			for(var j = i+1; j < group.users.length; j++) {
        				var userB = group.users[j].screenName;

        				for(var topic in GT_SUBTOPICS) {
        					if(GT_SUBTOPICS[topic][userA] && GT_SUBTOPICS[topic][userB]) {
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
        					for(var topic in GT_SUBTOPICS) {
        						if(GT_SUBTOPICS[topic][userA.screenName] && GT_SUBTOPICS[topic][userB.screenName]) {
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
        	output += "Accuracy: " + ((tp+tn)/(tp+tn+fn+tn)) + "\n";

        	var mcc = (tp*tn - fp*fn) / Math.sqrt( (tp+fp)*(tp+fn)*(tn+fp)*(tn+fn) );
        	var precision = tp / (tp+fp);
        	var recall = tp / (tp+fn);
        	var fScore = 2*((precision*recall)/(precision+recall));

        	//output += "ClusterUsers / AllUsers: " + clusterUserCount + " / " + userCount + "\n";
        	var corr = clusterUserCount / userCount;
        	
        	output += "Precision: " + precision + ", corrected for user count: " + precision*corr + "\n";
        	output += "Recall: " + recall + ", corrected for user count: " + recall*corr + "\n";
			output += "F-score: " + fScore + ", corrected for user count: " + fScore*corr + "\n";
			output += "MCC: " + mcc + ", corrected for user count: " + mcc*corr + "\n";

        	console.log(output);
        	return mcc;
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