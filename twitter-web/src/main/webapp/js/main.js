var GT_USERS = {};
var GT_TOPICS = {};
var GT_SUBTOPICS = {};
var userCount = 0;

var gtFileInput = $('#gtfile');
var gtUploadButton = $('#gtupload');

var utils = {
		/* Converts an uploaded ground truth file to a collection of sets for easy lookup. */
		convertGTToJSON: function(e) {
			var file = e.target.result;
			var results;

			var currentTopic = "";
			var currentSubTopic = "";

			if (file && file.length) {
				results = file.split("\n");

				_.each(results, function(result) {
					result = result.trim();

					if(result == "") return;

					else if(result.charAt(0) == ":") {
						currentTopic = result.substring(1,result.length-1);
						GT_TOPICS[currentTopic] = {};
					} else if(result.charAt(0) == "-") {
						currentSubTopic = result.substring(1,result.length-1);
						GT_SUBTOPICS[currentSubTopic] = {};
					} else {
						var currentUser = result.split("\t")[0];
						var currentScore = result.split("\t")[1];

						if(GT_USERS[currentUser] == null) {
							GT_USERS[currentUser] = {};
							userCount++;
						}
						GT_USERS[currentUser][currentTopic] = currentScore;
						GT_USERS[currentUser][currentSubTopic] = currentScore;
						GT_TOPICS[currentTopic][currentUser] = currentScore;
						GT_SUBTOPICS[currentSubTopic][currentUser] = currentScore;
					}
				});
			}
		},
}

//Function to upload a DCG file.
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
		alert("Ground truth loaded!");
	} else {
		alert("Please upload a file before continuing.");
	} 
});



/* 
 * Function to evaluate communities based on accuracy.
 * Input: array of user arrays (clusters with members).
 * 
 * */
function evaluateClusteringAccuracy(clusters, userCount) {
	var a = 0, b = 0, c = 0, d = 0;
	var done = false;
	var accuracy = 0;
	var precision = 0;
	var output = "";

	if(_.isEmpty(GT_USERS) || _.isEmpty(GT_TOPICS)) {
		console.log("ERROR: cannot evaluate clusters. Upload a ground truth first!");
		return;
	}

	var clusterUserCount = 0;
	var clusterUserSet = {};

	output += "Users per topic:\n\n";
	for(var i = 0; i < clusters.length; i++) {
		var tUsers = clusters[i];
		output += "Topic " + i + ": ";

		for(var j = 0; j < tUsers.length; j++) {
			output += tUsers[j] + ", ";
			clusterUserCount++;
			clusterUserSet[tUsers[j]] = true;
		}

		output += "\n";
	}

	var nullCluster = [];

	/*for(var user in GT_USERS) {
		if(!(user in clusterUserSet)) {
			nullCluster.push(user);
		}
	}

	clusters.push(nullCluster);*/

	_.each(clusters, function(cluster) {
		for(var i = 0; i < cluster.length-1; i++) {
			var userA = cluster[i];
			for(var j = i+1; j < cluster.length; j++) {
				var userB = cluster[j];

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

	for(var i = 0; i < clusters.length-1; i++) {
		var clusterA = clusters[i];
		for(var j = i+1; j < clusters.length; j++) {
			var clusterB = clusters[j];
			_.each(clusterA, function(userA) {
				_.each(clusterB, function(userB) {
					for(var topic in GT_TOPICS) {
						if(GT_TOPICS[topic][userA] && GT_TOPICS[topic][userB]) {
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
}
