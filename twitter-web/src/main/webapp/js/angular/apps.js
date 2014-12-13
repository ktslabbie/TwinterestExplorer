var twitterWebApp = angular.module('twitterWeb.app', 
		['twitterWeb.APIService', 'twitterWeb.SimilarityService', 'twitterWeb.ClusteringServices', 'twitterWeb.EvaluationServices', 
		 'twitterWeb.LDAService', 'twitterWeb.GraphService', 'twitterWeb.controller', 'infinite-scroll', 'ngAnimate']);