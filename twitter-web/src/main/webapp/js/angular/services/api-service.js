var twitterAPIService = angular.module('twitterWeb.APIService', ['ngResource']);

twitterAPIService.factory('TwitterUser', ['$resource', function($resource) {
	return $resource('/api/get-twitter-user', { }, {
						user: { isArray: false, method: 'get'}
	});
}]);

twitterAPIService.factory('UserTweets', ['$resource', function($resource) {
	return $resource('/api/get-user-tweets', { }, {
						tweets: { isArray: false, method: 'get'}
	});
}]);

twitterAPIService.factory('UserOntology', ['$resource', function($resource) {
	return $resource('/api/get-user-ontology', { }, {
						ontology: { isArray: false, method: 'get'}
	});
}]);

twitterAPIService.factory('UserNetwork', ['$resource', function($resource) {
	return $resource('/api/get-user-network', { }, {
						network: { isArray: false, method: 'get'}
	});
}]);

twitterAPIService.factory('UserList', ['$resource', function($resource) {
	return $resource('/api/get-user-list', { }, {
						list: { isArray: false, method: 'get'}
	});
}]);

twitterAPIService.factory('KeywordUsers', ['$resource', function($resource) {
	return $resource('https://twitter.com/search', { }, {
						list: { isArray: false, method: 'JSONP'}
	});
}]);