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
        }
    };
});