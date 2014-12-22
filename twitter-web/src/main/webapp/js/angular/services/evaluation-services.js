var evalService = angular.module('twitterWeb.EvaluationServices', []);

evalService.factory("EvaluationService", function() {
	
	var relevanceScores = {};
	
	return {
		
		getRelevanceScores: function() {
			return relevanceScores;
		},
	
		setRelevanceScores: function(pRelevanceScores) {
			relevanceScores = pRelevanceScores;
		},
		
        dcg: function(vertices, edges) {
        	
        	var dcgEdges = [];
        	
    		_.each(edges, function(edge) {
    			if(edge.source.userIndex == 0) dcgEdges.push(edge);
    		});
    		
    		var sortedDCGEdges = _.sortBy(dcgEdges, function(e) { return e.value; })
    		
    		var rankingStr = "Similarity ranking (" + sortedDCGEdges.length + " entries):\n";
    		
    		for(var i = sortedDCGEdges.length; i--;) {
    			rankingStr += 	vertices[sortedDCGEdges[i].target.index].name + "\t" + 
    							relevanceScores[vertices[sortedDCGEdges[i].target.index].name].trim() + "\t" + 
    							sortedDCGEdges[i].value + "\n";
    		}
    		
    		console.log(rankingStr);
    		
    		var firstEdge = sortedDCGEdges[sortedDCGEdges.length-1];
    		var topK = [3, 5, 10, 25, 50];
    		var evaluationString = "";
    		
    		_.each(topK, function(k) {
    			if(k <= sortedDCGEdges.length) {
    			
    				var reli = 0.0;
    				var binreli = 0.0;
    				var dcg = parseFloat(relevanceScores[vertices[firstEdge.target.index].name]);
    				var binDCG = (parseFloat(relevanceScores[vertices[firstEdge.target.index].name]) > 0) ? 2 : 0;
    				var idcg = 2;
    				var index = 1;
    				
    				for(var i = sortedDCGEdges.length-1; i--;) {
    					var edge = sortedDCGEdges[i];
    					
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
        mcc: function(groups, userCount) {
        	var a = 0, b = 0, c = 0, d = 0;
        	var done = false;
        	var output = "";

        	if(_.isEmpty(GT_USERS) || _.isEmpty(GT_TOPICS)) {
        		console.log("ERROR: cannot evaluate clusters. Upload a ground truth first!");
        		return;
        	}

        	var clusterUserCount = 0;
        	var clusterUserSet = {};
        	var pib = [];
        	var count = 0;

        	output += "Users per topic:\n\n";
        	_.each(groups, function(group, i) {
        		output += "Topic " + (i+1) + ": ";

        		_.each(group.users, function(user) {
        			output += user.screenName + ", ";
        			clusterUserCount++;
        			clusterUserSet[user.screenName] = true;
        			count++;
        			pib.push((i+1));
        		});

        		output += "\n";
        	});

        	var nullCluster = { users: [] };

        	_.each(GT_USERS, function(val, user) {
        		if(!(user in clusterUserSet)) {
        			count++;
        			nullCluster.users.push({ screenName: user });
        			pib.push((groups.length+1));
        		}
        	});
        	
        	groups.push(nullCluster);
        	
        	console.log("Total users: " + count);
        	console.log("pia length: " + pia.length);
        	console.log("pib length: " + pib.length);
        	
        	
        	var ka = 9;
        	var kb = groups.length;
        	var n = userCount;
        	var nhl = new Array();
        	
        	console.log("NMI: we have " + n + " users, ka: " + 9 + ", kb: " + kb);
        	console.log("NMI: pia: " + JSON.stringify(pia));
        	console.log("NMI: pib: " + JSON.stringify(pib));
        	
        	for (var i = 0; i < ka; i++)  {
        		nhl[i] = new Array();
        		for (var j = 0; j < kb; j++)
        			nhl[i][j] = 0;
        	}
        	
        	var ix = 1;
        	
        	for(var i = 0; i <= n; i++) {
    			if(pia[0] == ix && pib[0] == ix) {
    				nhl[ix-1][ix-1]++;
    				pia.shift();
    				pib.shift();
    				n--;
    			} else if(pia[0] == ix) {
    				var ixb = pib[0];
    				if(ixb > kb) continue;
    				while(pia[0] == ix) {
    					nhl[ix-1][ixb-1]++;
    					pia.shift();
    					pib.shift();
    					n--;
    					if(pib.length == 0) break;
    					if(pib[0] != ixb) ixb++;
    				}
    				ix++;
    			} else if(pib[0] == ix) {
    				var ixa = pia[0];
    				if(ixa > ka) continue;
    				while(pib[0] == ix) {
    					nhl[ixa-1][ix-1]++;
    					pia.shift();
    					pib.shift();
    					n--;
    					if(pia.length == 0) break;
    					if(pia[0] != ixa) ixa++;
    				}
    				ix++;
    			} else {
    				ix++;
    			}
        	}
        	
        	console.log("NMI: nhl final: " + JSON.stringify(nhl));
        	
        	var nmiNum = 0, nmiDem = 0, n = userCount;
        	
        	for (var h = 0; h < ka; h++)  {
        		for (var l = 0; l < kb; l++) {
        			var hi = nhl[h][l]*Math.log( (n*nhl[h][l]) / (groundGroups[h]*groups[l].users.length) );
        			if(!isNaN(hi)) nmiNum += hi;
        		}
        	}
        	
        	var nmiLeft = 0, nmiRight = 0;
        	
        	for (var h = 0; h < ka; h++)  {
        		var hi = groundGroups[h]*(-Math.log( groundGroups[h] / n));
        		if(!isNaN(hi)) nmiLeft += hi;
        	}
        	
        	for (var l = 0; l < kb; l++) {
        		var hi = groups[l].users.length*(-Math.log( groups[l].users.length / n) ) ;
        		if(!isNaN(hi)) nmiRight += hi;
        	}
        	
        	nmiDem = Math.sqrt(nmiLeft*nmiRight);
        	
        	var nmi = nmiNum / nmiDem;
        	
        	console.log("NMI: NMI final: " + nmi);
        	
        	_.each(groups, function(group) {
        		for(var i = 0; i < group.users.length-1; i++) {
        			var userA = group.users[i].screenName;
        			for(var j = i+1; j < group.users.length; j++) {
        				var userB = group.users[j].screenName;

        				for(var topic in GT_TOPICS) {
        					if(GT_TOPICS[topic][userA] && GT_TOPICS[topic][userB]) {
        						a++;
        						done = true;
        						break;
        					}
        				}

        				if(!done) b++;
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
        					for(var topic in GT_TOPICS) {
        						if(GT_TOPICS[topic][userA.screenName] && GT_TOPICS[topic][userB.screenName]) {
        							c++;
        							done = true;
        							break;
        						}
        					}

        					if(!done) d++;
        					done = false;
        				});
        			});
        		}
        	}

        	//var d = (n*(n-1)/2) - (a+b+c);
        	console.log("a: " + a + ", b: " + b + ", c: " + c + ", d: " + d);
        	console.log("Accuracy: " + ((a+d)/(a+b+c+d)));

        	var mcc = (a*d - b*c) / Math.sqrt( (a+b)*(a+c)*(d+b)*(d+c) );
        	var precision = a / (a+b);
        	var recall = a / (a+c);
        	var fScore = 2*((precision*recall)/(precision+recall));

        	console.log("ClusterUsers / AllUsers: " + clusterUserCount + " / " + userCount);
        	var corr = clusterUserCount / userCount;
        	
        	console.log("Precision: " + precision + ", corrected for user count: " + precision*corr);
        	console.log("Recall: " + recall + ", corrected for user count: " + recall*corr);
        	console.log("F-score: " + fScore + ", corrected for user count: " + fScore*corr);
        	console.log("MCC: " + mcc + ", corrected for user count: " + mcc*corr);

        	console.log(output);
        },
        
        /** 
         * Function to evaluate communities based on Normalized Mutual Information.
         * Input: array of user arrays (clusters with members).
         **/
        nmi: function(groups, userCount) {
        	
        }
    };
});