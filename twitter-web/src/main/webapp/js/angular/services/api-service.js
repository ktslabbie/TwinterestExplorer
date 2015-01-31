var twitterAPIService = angular.module('twitterWeb.APIService', ['ngResource']);

twitterAPIService.factory('SimpleTwitterUser', ['$resource', function($resource) {
	return $resource('/api/get-twitter-user', { }, {
						get: { isArray: false, method: 'get'}
	});
}]);

twitterAPIService.factory('Tweets', ['$resource', function($resource) {
	return $resource('/api/get-user-tweets', { }, {
						list: { isArray: true, method: 'get'}
	});
}]);

twitterAPIService.factory('TwitterUser', ['$resource', function($resource) {
	return $resource('/api/get-user-ontology', { }, {
						get: { isArray: false, method: 'get'}
	});
}]);

twitterAPIService.factory('TwitterUserNetwork', ['$resource', function($resource) {
	return $resource('/api/get-user-network', { }, {
						network: { isArray: false, method: 'get'}
	});
}]);

twitterAPIService.factory('TwitterUserList', ['$resource', function($resource) {
	return $resource('/api/get-user-list', { }, {
						list: { isArray: false, method: 'get'}
	});
}]);

twitterAPIService.factory('KeywordUsers', ['$resource', function($resource) {
	return $resource('https://twitter.com/search', { }, {
						list: { isArray: false, method: 'JSONP'}
	});
}]);