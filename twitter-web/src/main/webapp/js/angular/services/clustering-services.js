var simService = angular.module('twitterWeb.ClusteringServices', []);

simService.factory("HCSService",['$rootScope', '$q',  function($rootScope, $q) {

	var worker = new Worker("js/hcs_kruskal_worker.js");	
	var defer = $q.defer();

	worker.addEventListener('message', function(e) {
		defer.resolve(e.data);
		
		console.log("HCS worker terminating!")
		worker.terminate();	
	}, false);
	
	return {
        doWork : function(ev){
            defer = $q.defer();
            worker.postMessage(ev); // Send data to our worker. 
            return defer.promise;
        }
    };
}]);