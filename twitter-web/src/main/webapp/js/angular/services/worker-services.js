var workerService = angular.module('twitterWeb.WorkerServices', [])

.factory("CFIUFService", ['$q',  function($q) {

	var worker = new Worker("js/workers/cf-iuf_worker.js");
	var defer = $q.defer();

	worker.addEventListener('message', function(e) {
		defer.resolve(e.data);
	});
	
	return {
        doWork: function(event) {
        	defer = $q.defer();
        	
        	if(event.ontologies.length > 0) worker.postMessage(event);
        	else defer.resolve(event);
        	
        	return defer.promise;
        },
        
        clear: function() {
            worker.postMessage({ clear: true }); // Tell our worker we want to clear its data.
        }
    };
}])

.factory("CFIUFGroupService", ['$q',  function($q) {

	var worker = new Worker("js/workers/cf-iuf_worker.js");
	var defer = $q.defer();

	worker.addEventListener('message', function(e) {
		defer.resolve(e.data);
	});
	
	return {
        doWork: function(event) {
            defer = $q.defer();
            worker.postMessage(event);
            return defer.promise;
        },
        
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
            worker.postMessage(ev);
            return defer.promise;
        },
        
        restart: function() {
        	console.log("Terminate simworker.");
        	worker.terminate();
        	console.log("Terminated?");
        	createWorker();
        }
    };
}])

.factory("HCSService", ['$rootScope', '$q',  function($rootScope, $q) {

	var worker;
	var defer;
	
	function createWorker() {
		worker = new Worker("js/workers/hcs_kruskal_worker.js");
		defer = $q.defer();
		
		worker.addEventListener('message', function(e) {
			if(e.data.finished)
				defer.resolve(e.data);
			else
				$rootScope.$broadcast('hcsUpdate', e.data);
		}, false);
	}

	createWorker();
	
	return {
        doWork : function(ev){
            defer = $q.defer();
            worker.postMessage(ev);
            return defer.promise;
        },
        
        restart: function() {
        	worker.terminate();
        	createWorker();
        }
    };
}]);