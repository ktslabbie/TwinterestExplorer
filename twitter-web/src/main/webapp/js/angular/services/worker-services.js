var workerService = angular.module('twitterWeb.WorkerServices', []);

workerService.factory("CFIUFService", ['$q',  function($q) {

	var worker = new Worker("js/cf-iuf_worker.js");	
	var defer = $q.defer();

	worker.addEventListener('message', function(e) {
		defer.resolve(e.data.ontologies);
	}, false);
	
	return {
        doWork : function(ev) {
            defer = $q.defer();
            worker.postMessage(ev); // Send data to our worker. 
            return defer.promise;
        }
    };
}])

.factory("SimilarityService", ['$rootScope', '$q',  function($rootScope, $q) {

	var worker = new Worker("js/similarity_worker.js");	
	var defer = $q.defer();

	worker.addEventListener('message', function(e) {
		if(e.data.finished) {
			defer.resolve(e.data);
			
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
}])

.factory("HCSService", ['$q',  function($q) {

	var worker = new Worker("js/hcs_kruskal_worker.js");	
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