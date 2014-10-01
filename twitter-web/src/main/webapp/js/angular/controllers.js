var twitterWebController = angular.module('twitterWeb.controller', [])

.controller('TwitterController', ['$scope', '$timeout', 'TwitterUser', 'UserTweets', 'UserOntology', 'UserNetwork', function($scope, $timeout, TwitterUser, UserTweets, UserOntology, UserNetwork) {
	
	
	$scope.loadingUser = false;
	$scope.loadingTweets = false;
	$scope.loadingOntology = false;
	$scope.loadingNetwork = false;
	$scope.targetUser = null;
	$scope.screenName = "BarackObama";
	$scope.pageSize = 0;
	$scope.N = 0;
	
	$scope.cfMap = {};
	$scope.minimumSimilarity = 0.1;
	$scope.similarities = [];
	
	$scope.yagoTypeBlackList = ["Abstraction", "Group", "YagoLegalActorGeo", "YagoPermanentlyLocatedEntity", 
	                            "PhysicalEntity", "Object", "YagoGeoEntity", "YagoLegalActor", "Whole",]
	

	var nodes = {};

	// Compute the distinct nodes from the links.
	$scope.similarities.forEach(function(link) {
	  link.a = nodes[link.a] || (nodes[link.a] = {name: link.a});
	  link.b = nodes[link.b] || (nodes[link.b] = {name: link.b});
	});

	var width = 960,
	    height = 500;

	var force = d3.layout.force()
	    .nodes(d3.values(nodes))
	    .links($scope.similarities)
	    .size([width, height])
	    .linkDistance(60)
	    .charge(-300)
	    .on("tick", tick)
	    .start();

	var svg = d3.select("body").append("svg")
	    .attr("width", width)
	    .attr("height", height);

	// Per-type markers, as they don't inherit styles.
	svg.append("defs").selectAll("marker")
	    .data(["suit"])
	  .enter().append("marker")
	    .attr("id", function(d) { return d; })
	    .attr("viewBox", "0 -5 10 10")
	    .attr("refX", 15)
	    .attr("refY", -1.5)
	    .attr("markerWidth", 6)
	    .attr("markerHeight", 6)
	    .attr("orient", "auto")
	  .append("path")
	    .attr("d", "M0,-5L10,0L0,5");

	var path = svg.append("g").selectAll("path")
	    .data(force.links())
	  .enter().append("path")
	    .attr("class", function(d) { return "link " + d.similarity; })
	    .attr("marker-end", function(d) { return "url(#" + d.similarity + ")"; });

	var circle = svg.append("g").selectAll("circle")
	    .data(force.nodes())
	  .enter().append("circle")
	    .attr("r", 6)
	    .call(force.drag);

	var text = svg.append("g").selectAll("text")
	    .data(force.nodes())
	  .enter().append("text")
	    .attr("x", 8)
	    .attr("y", ".31em")
	    .text(function(d) { return d.name; });


	// Use elliptical arc path segments to doubly-encode directionality.
	function tick() {
	  path.attr("d", linkArc);
	  circle.attr("transform", transform);
	  text.attr("transform", transform);
	}

	function linkArc(d) {
	  var dx = d.target.x - d.source.x,
	      dy = d.target.y - d.source.y,
	      dr = Math.sqrt(dx * dx + dy * dy);
	  return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
	}

	function transform(d) {
	  return "translate(" + d.x + "," + d.y + ")";
	}


	
	
	
	
	var getTweetStats = function(user) {
		var retweets = 0;
		var hashtags = {};
		var mentions = {};
		
		for(var i = 0; i < user.tweets.length; i++) {
			var tweet = user.tweets[i];
			
			for(var j = 0; j < tweet.hashtags.length; j++) {
				var tag = tweet.hashtags[j];
				hashtags[tag] = hashtags[tag] ? hashtags[tag]+1 : 1;
			}
			
			for(var j = 0; j < tweet.userMentions.length; j++) {
				var men = tweet.userMentions[j];
				mentions[men] = mentions[men] ? mentions[men]+1 : 1;
			}
			
			if(tweet.retweet) retweets++;
		}
		
		var htList = [];
		mList = [];
		
		for(key in hashtags) htList.push({ tag: key, occ: hashtags[key] });
		for(key in mentions) mList.push({ mention: key, occ: mentions[key] });
		
		user.retweets = retweets;
		user.hashtags = htList;
		user.userMentions = mList;
	}
	
/*	var sortYagoTypes = function(types) {
		var tuples = [];

		for (var key in types) {
			keyStr = key.split(":")[1];
			var wnCode = keyStr.substr(keyStr.length - 9);
			if(!isNaN(wnCode)) {
				keyStr = keyStr.substr(0, keyStr.length - 9);
			}
			
			//if($scope.yagoTypeBlackList.indexOf(keyStr) == -1) tuples.push([keyStr, types[key]]);
			tuples.push([keyStr, types[key]]);
		}

		tuples.sort(function(a, b) {
		    a = a[1];
		    b = b[1];

		    return a > b ? -1 : (a < b ? 1 : 0);
		});
		
		return tuples;
	}*/
	
	var sortCFIUF = function(index, userCFIUFMap, euclidLength) {
		var tuples = [];

		for (var key in userCFIUFMap) {
			var cfIufNorm = (euclidLength == 0) ? 0 : userCFIUFMap[key] / euclidLength;
			tuples.push([ key, cfIufNorm ]);
		}

		tuples.sort(function(a, b) {
		    a = a[1];
		    b = b[1];

		    return a > b ? -1 : (a < b ? 1 : 0);
		});

		return tuples;
	}
	
	var updateCFMap = function(user) {
		
		for(type in user.ontology.yagotypes) {
			
			keyStr = type.split(":")[1];
			var wnCode = keyStr.substr(keyStr.length - 9);
			if(!isNaN(wnCode)) {
				keyStr = keyStr.substr(0, keyStr.length - 9);
			}
			
			user.ontology.sortedYagotypes.push([keyStr, user.ontology.yagotypes[type]]);
			$scope.cfMap[keyStr] = $scope.cfMap[keyStr] ? $scope.cfMap[keyStr] + 1 : 1;
			if($scope.cfMap[keyStr] > $scope.N) $scope.cfMap[keyStr] = $scope.N;
		}
		
		/*for(var i = 0; i < user.ontology.sortedYagotypes.length; i++) {
			var type = user.ontology.sortedYagotypes[i][0];
			$scope.cfMap[type] = $scope.cfMap[type] ? $scope.cfMap[type]+1 : 1;
		}*/
	}
	
	var updateCFIUF = function(index, user) {
		
		$scope.N = index + 2;

		updateCFMap(user);
		var userCFIUFMap = {};

		for (var i = -1; i <= index; i++) {
			userCFIUFMap = {};
			var cfIufSum = 0;
			var currentUser = (i == -1) ? $scope.targetUser : $scope.targetUser.network[i];
			var types = currentUser.ontology.sortedYagotypes;

			for(var j = 0; j < types.length; j++) {
				var type = types[j];
				var cf = type[1];
				var iuf = Math.log($scope.N / $scope.cfMap[type[0]]);
				var cfIuf = (Math.pow(cf, 1 + 0))*(Math.pow(iuf, 1 - 0));
				
				//console.log("N: " + $scope.N + ", global cf: " + $scope.cfMap[type[0]]);
				
				
				cfIufSum += Math.pow(cfIuf, 2);
				
				//console.log("cf: " + cf + ", iuf: " + iuf + ", cfiuf: " + cfIuf + ", cfiufSum: " + cfIufSum);
				
				userCFIUFMap[type[0]] = cfIuf;
				//currentUser.ontology.cfIufMap[type[0]] = cfIuf;
			}
			
			var euclidLength = Math.sqrt(cfIufSum);
			
			if(currentUser.ontology.cfIufMap) {
				var newMap = sortCFIUF(index, userCFIUFMap, euclidLength);
				
				for(var j = 0; j < newMap.length; j++) {
					if(currentUser.ontology.cfIufMap[j][0] != newMap[j][0] ) {
						currentUser.ontology.cfIufMap[j] = newMap[j];
					} else {
						var a = Math.round(currentUser.ontology.cfIufMap[j][1] * 100) / 100;
						var b = Math.round(newMap[j][1] * 100) / 100;
						if(a != b) currentUser.ontology.cfIufMap[j] = newMap[j];
					}
				}
			} else {
				currentUser.ontology.cfIufMap = sortCFIUF(index, userCFIUFMap, euclidLength);
			}
		}
		
		/* Calculate cosine similarity wrt. all previous users. */
		for(var i = -1; i < index; i++) {
			
			var prevUserMap = (i == -1) ? $scope.targetUser.ontology.cfIufMap : $scope.targetUser.network[i].ontology.cfIufMap;
			var similarity = 0;
			//console.log("Previous user map: " + prevUserMap);
			//console.log("Current user map: " + userCFIUFMap);
			
			for(j = 0; j < prevUserMap.length; j++) {
				var weight = userCFIUFMap[prevUserMap[j][0]] / euclidLength;
				
				if(weight != null && !isNaN(weight)) similarity += weight*prevUserMap[j][1];
				
				//console.log(prevUserMap[j][0] + ": cf-iuf: " + prevUserMap[j][1] + " vs. " + weight + ". Total: " + similarity + "   NaN?" + isNaN(weight));
			}
			
			if(similarity >= $scope.minimumSimilarity) {
				var aName = (i == -1) ? $scope.targetUser.screenName : $scope.targetUser.network[i].screenName;
				var bName = $scope.targetUser.network[index].screenName;
				
				$scope.similarities.push( { a: aName, b: bName, sim: similarity } );
			}
		}
		
		/*console.log("Similarities: ");
		
		for(i = 0; i < $scope.similarities.length; i++) {
			console.log($scope.similarities[i].a + " - " + $scope.similarities[i].b + ": " + $scope.similarities[i].sim);
		}*/
	}
	
	$scope.getUser = function() {
		$scope.cfMap = {};
		$scope.targetUser = null;
		$scope.loadingUser = true;

		$scope.targetUser = TwitterUser.user({ name: $scope.screenName }, function() {
			if($scope.targetUser.user) {
				$scope.targetUser = $scope.targetUser.user;
				$scope.targetUser.profileImageURL = $scope.targetUser.profileImageURL;
				
				// cancel old timeout
			    if ($scope.targetUser.timeoutId) {
			        $timeout.cancel($scope.targetUser.timeoutId);
			    }

			    // hide images to start with
			    $scope.targetUser.imageVisible = false;

			    $scope.targetUser.timeoutId = $timeout(function() {
			        $scope.targetUser.imageVisible = true;
			        $scope.targetUser.timeoutId = undefined;
			    }, 10);
				
				if($scope.targetUser.tweets.length > 0) {
					getTweetStats($scope.targetUser);
					$scope.getOntology($scope.targetUser.screenName);
				} else {
					$scope.getTweets($scope.targetUser.screenName);
				}
			}
				
			$scope.loadingUser = false;
		});
	};
	
	$scope.getTweets = function(name) {
		$scope.targetUser.loadingTweets = true;
		$scope.targetUser.tweets = UserTweets.tweets({ name: name }, function() {
			$scope.targetUser.englishRate = $scope.targetUser.tweets.englishRate;
			$scope.targetUser.tweets = $scope.targetUser.tweets.tweets;
			
			getTweetStats($scope.targetUser);
			$scope.getOntology($scope.targetUser.screenName);
			
			$scope.targetUser.loadingTweets = false;
		});
	};
	
	$scope.getOntology = function(name) {
		$scope.targetUser.loadingOntology = true;
		$scope.targetUser.ontology = UserOntology.ontology({ name: name }, function() {
			$scope.targetUser.ontology = $scope.targetUser.ontology.user.userOntology;

			//$scope.targetUser.ontology.sortedYagotypes = sortYagoTypes($scope.targetUser.ontology.yagotypes);
			$scope.targetUser.ontology.sortedYagotypes = [];
			updateCFIUF(-1, $scope.targetUser);
			
			$scope.targetUser.loadingOntology = false;
		});
	};
	
	$scope.getTargetUserNetwork = function(name) {
		$scope.loadingNetwork = true;
		$scope.targetUser.network = UserNetwork.network({ name: name }, function() {
			$scope.targetUser.network = $scope.targetUser.network.network;
			$scope.loadingNetwork = false;
			$scope.pageSize = 20;
			$scope.targetUser.network.shift();
		});
	};
	
	$scope.processNetwork = function(pIndex) {
		
		var index = parseInt(pIndex);

		$scope.targetUser.network[index].loadingTweets = true;
		$scope.targetUser.network[index].tweets = UserTweets.tweets({ name: $scope.targetUser.network[index].screenName }, function() {
			
			$scope.targetUser.network[index].englishRate = $scope.targetUser.network[index].tweets.englishRate;
			$scope.targetUser.network[index].tweets = $scope.targetUser.network[index].tweets.tweets;
			
			getTweetStats($scope.targetUser.network[index]);
			
			$scope.targetUser.network[index].loadingTweets = false;
			$scope.targetUser.network[index].loadingOntology = true;
			$scope.targetUser.network[index].ontology = UserOntology.ontology({ name: $scope.targetUser.network[index].screenName }, function() {
				
				$scope.targetUser.network[index].ontology = $scope.targetUser.network[index].ontology.user.userOntology;
				//$scope.targetUser.network[index].ontology.sortedYagotypes = sortYagoTypes($scope.targetUser.network[index].ontology.yagotypes);
				$scope.targetUser.network[index].ontology.sortedYagotypes = [];
				
				$scope.targetUser.network[index].loadingOntology = false;
				
				updateCFIUF(index, $scope.targetUser.network[index]);
				
				index += 1;
				
				if(index < 99) {
					$scope.processNetwork(index);
				}
			});
			
		});
	}
	
	$scope.loadMore = function() {
		$scope.pageSize = $scope.pageSize + 20;
	}
}]);

