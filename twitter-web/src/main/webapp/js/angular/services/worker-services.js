var workerService = angular.module('twitterWeb.WorkerServices', []);

workerService.factory("CFIUFService", ['$q',  function($q) {

	var worker = new Worker("js/workers/cf-iuf_worker.js");
	var defer = $q.defer();

	worker.addEventListener('message', function(e) {
		defer.resolve(e.data);
	});
	
	return {
        doWork: function(event) {
            defer = $q.defer();
            worker.postMessage(event); // Send data to our worker.
            return defer.promise;
        },
        
        //cancel: function() {
        	//worker.postMessage({ cancel: true }); // Tell our worker we want to cancel the current calculation.
        //},
        
        clear: function() {
            worker.postMessage({ clear: true }); // Tell our worker we want to clear its data.
        }
    };
}])

.factory("SimilarityService", ['$rootScope', '$q',  function($rootScope, $q) {

	var worker;
	var defer;
	
	function createWorker() {
		worker = new Worker("js/workers/similarity_worker.js");
		defer = $q.defer();
		
		worker.addEventListener('message', function(e) {
			if(e.data.finished)
				defer.resolve(e.data);
			else
				$rootScope.$broadcast('simGraphUpdate', e.data);
		}, false);
	}

	createWorker();
	
	return {
        doWork: function(ev){
            defer = $q.defer();
            worker.postMessage(ev); // Send data to our worker. 
            return defer.promise;
        },
        
        restart: function() {
        	worker.terminate();
        	createWorker();
        }
    };
}])

.factory("HCSService", ['$rootScope', '$q',  function($rootScope, $q) {

	var worker = new Worker("js/workers/hcs_kruskal_worker.js");	
	var defer = $q.defer();

	worker.addEventListener('message', function(e) {
		if(e.data.finished)
			defer.resolve(e.data);
		else
			$rootScope.$broadcast('hcsUpdate', e.data);
		
	}, false);
	
	return {
        doWork : function(ev){
            defer = $q.defer();
            worker.postMessage(ev); // Send data to our worker. 
            return defer.promise;
        }
    };
}]);