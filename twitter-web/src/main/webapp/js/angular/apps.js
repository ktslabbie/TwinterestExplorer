var twitterWebApp = angular.module('twitterWeb.app', 
		['twitterWeb.APIService', 'twitterWeb.WorkerServices', 'twitterWeb.EvaluationServices', 'twitterWeb.directives',
		 'twitterWeb.GraphService', 'twitterWeb.controller', 'infinite-scroll', 'ngAnimate']);