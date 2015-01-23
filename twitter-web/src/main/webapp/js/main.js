var GT_USERS = {};
var GT_TOPICS = {};
var GT_SUBTOPICS = {};
var pia = [];
var subPia = [];
var groundGroups = [];
var groundSubGroups = [];

//var userCount = 0;



var utils = {
		/* Converts an uploaded ground truth file to a collection of sets for easy lookup. */
		convertGTToJSON: function(e) {
			var file = e.target.result;
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
						groundGroups.push(0);
						topicIndex++;
					} else if(result.charAt(0) == "-") {
						currentSubTopic = result.substring(1,result.length-1);
						GT_SUBTOPICS[currentSubTopic] = {};
						groundSubGroups.push(0);
						subTopicIndex++;
					} else {
						var currentUser = result.split(",")[0];
						var currentScore = result.split(",")[1];
						pia.push(topicIndex); cnt++;
						subPia.push(subTopicIndex);
						groundGroups[topicIndex-1]++;
						groundSubGroups[subTopicIndex-1]++;
						

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
			console.log("Added " + cnt + " users in total.");
			//console.log("Topics: " + JSON.stringify(GT_TOPICS));
			//console.log("Sub-topics: " + JSON.stringify(GT_SUBTOPICS));
		},
}

