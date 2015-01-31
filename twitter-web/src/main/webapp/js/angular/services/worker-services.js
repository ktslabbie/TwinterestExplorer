var workerService = angular.module('twitterWeb.WorkerServices', []);

workerService.factory("CFIUFService", ['$q',  function($q) {

	var worker = new Worker("js/workers/cf-iuf_worker.js");	
	var defer = $q.defer();

	worker.addEventListener('message', function(e) {
		console.log("got message");
		defer.resolve(e.data);
		console.log("resolved");
	}, false);
	
	return {
        doWork: function(event) {
            defer = $q.defer();
            worker.postMessage(event); // Send data to our worker.
            return defer.promise;
        },
        
        clear: function() {
            worker.postMessage({ clear: true }); // Send data to our worker.
        }
    };
}])

.factory("SimilarityService", ['$rootScope', '$q',  function($rootScope, $q) {

	var worker = new Worker("js/workers/similarity_worker.js");
	var defer = $q.defer();

	worker.addEventListener('message', function(e) {
		if(e.data.finished)
			defer.resolve(e.data);
		else
			$rootScope.$broadcast('simGraphUpdate', e.data);
	}, false);
	
	return {
        doWork : function(ev){
            defer = $q.defer();
            worker.postMessage(ev); // Send data to our worker. 
            return defer.promise;
        }
    };
}])

.factory("HCSService", ['$q',  function($q) {

	var worker = new Worker("js/workers/hcs_kruskal_worker.js");	
	var defer = $q.defer();

	worker.addEventListener('message', function(e) {
		defer.resolve(e.data);
		
	}, false);
	
	return {
        doWork : function(ev){
            defer = $q.defer();
            worker.postMessage(ev); // Send data to our worker. 
            return defer.promise;
        }
    };
}]);