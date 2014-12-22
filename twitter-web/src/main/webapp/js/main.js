var GT_USERS = {};
var GT_TOPICS = {};
var GT_SUBTOPICS = {};
var pia = [];
var groundGroups = [];

//var userCount = 0;

var gtFileInput = $('#gtfile');
var gtUploadButton = $('#gtupload');

var utils = {
		/* Converts an uploaded ground truth file to a collection of sets for easy lookup. */
		convertGTToJSON: function(e) {
			var file = e.target.result;
			var results;

			var currentTopic = "";
			var currentSubTopic = "";
			var topicIndex = 0;
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
					} else {
						var currentUser = result.split("\t")[0];
						var currentScore = result.split("\t")[1];
						pia.push(topicIndex); cnt++;
						groundGroups[topicIndex-1]++;
						

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

