var simService = angular.module('twitterWeb.SimilarityService', []);

simService.factory("SimilarityService",['$rootScope', '$q',  function($rootScope, $q) {

	var worker = new Worker("js/similarity_worker.js");	
	var defer = $q.defer();

	worker.addEventListener('message', function(e) {
		if(e.data.finished) {
			defer.resolve(e.data);
			
			console.log("Similarity worker terminating!")
			worker.terminate();
		} else {
			$rootScope.$broadcast('simGraphUpdate', e.data);
		}
	}, false);
	
	return {
        doWork : function(ev){
            defer = $q.defer();
            worker.postMessage(ev); // Send data to our worker. 
            return defer.promise;
        }
    };
}]);