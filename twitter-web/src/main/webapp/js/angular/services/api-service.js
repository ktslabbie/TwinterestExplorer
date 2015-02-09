var twitterAPIService = angular.module('twitterWeb.APIService', ['ngResource']);

twitterAPIService.factory('SimpleUser', ['$resource', function($resource) {
	return $resource('/api/get-simple-user', { }, {
						get: { isArray: false, method: 'get'}
	});
}]);

twitterAPIService.factory('User', ['$resource', function($resource) {
	return $resource('/api/get-user', { }, {
						get: { isArray: false, method: 'get'}
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