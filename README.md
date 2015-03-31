# Twinterest Explorer

![alt text](https://github.com/ktslabbie/TwinterestExplorer/raw/master/twitter/docs/twinterest-explorer.png "Screenshot of the app in action")

Demo: http://ktslabbie.github.io/te

## What's this?

Twinterest Explorer is an academic exercise in fast, real-time topic clustering and automatic labeling of unstructured documents. The underlying
algorithms are designed to be especially useful in scenarios where the quality and quantity of text of cannot be guaranteed,
such as on the Social Web. This makes Twitter a great candidate for experimentation. Please keep in mind that this is an unpolished proof-of-concept,
and still under development. You will likely encounter bugs, or the server may be slow or not even running at all.

## How do I use it?

It's recommended you run this in *Firefox*; Ubuntu Chrome 41, at least, seems to fail to assign Web Workers to separate threads, causing the UI to block way more than it should. Windows and OSX Chrome seem to be fine though. 
Input a Twitter user or keyword you'd like to explore. Hit "Go!" and let it run! You can also try a demo set of users with the "Try demo set" button.
The topic scope of the groups that should appear in the graph can be adjusted with the slider, and you can zoom into them to divide them further into sub-topics.

## So what can it do, exactly?

It can group and visualize streams of users by topic (or interests). Users are gathered directly from Twitter through
keyword search, by collecting the neighborhood (followers/friends) of a seed user, or by providing a user-defined list of screen names.
Desired topic scope can be arbitrarily narrow or broad (e.g. *AngularJS* -> *WebFrameworks* -> *ProgrammingLanguages* -> *Structure*):
this can be adjusted in real-time with a slider.
Detected clusters are visualized as new users are processed in the background, using a colored and labeled graph.
This graph can be explored by adjusting topic scope and zooming in on clusters. Zooming in will allow a cluster to be divided into sub-clusters
(sub-topics within the broader topic). Clusters are labeled automatically with the most characteristic traits for that cluster.

## And how is that useful?

Other than the "cool" factor, on a small scale where real-time visualization is still possible such as in this application, 
real-life usefulness is perhaps somewhat limited. Still, there are some interesting things you can do with it.
Consider using it to find out which of your followers/friends post about conceptually similar things, and have them grouped and labeled by interest automatically.
Or you can discover which users are consistently interested in a given topic to get a better idea of who to follow
(is their mention of "machine learning" a one-off thing, or are they actually into it?).
For ambiguous keywords, you can separate people into concrete interest groups.
Spambots tend to get grouped together, so it could theoretically be used as a spam filter as well.

For larger document clustering-type applications where real-time visualization is not necessary, the underlying methods provide benefits in terms of speed, 
accuracy and the ability to deal with noisy and sparse documents over state-of-the-art approaches such as Latent Dirichlet Allocation.
Additionally, the output of the clustering is hierarchical, and includes automatic cluster labeling.
Outside of generating word clouds, actual semantic labeling is generally not possible with traditional probabilistic methods that rely on word co-occurrence.

You can see a complete account of the approach in our paper:  *to be published*

# Technical details

## Application structure and technology stack

The project consists of a backend ([twitter](twitter/)) and frontend ([twitter-web](twitter-web/)).

### Backend

The backend is a standard Java application RESTified with [Dropwizard](https://github.com/dropwizard/dropwizard). It talks with:

1. a [DBpedia Spotlight](https://github.com/dbpedia-spotlight/dbpedia-spotlight) server to annotate tweet content with links to DBpedia resources; 
2. a [Redis](https://github.com/antirez/redis) instance, which contains a flattened version of the YAGO class hierarchy from DBpedia (lists of classes up to the root of the hierarchy for each resource);
3. a PostgreSQL database, mostly used for caching users, and tweets and classes collected for them.

The general process flow for the backend is: 

* get a request for a user + parameters through the RESTful API
* check DB for containment of this user
  * if not contained, call Twitter API and apply named entity recognition (NER) on tweets;
  * otherwise, apply only NER or return immediately.

Applying NER means sending (concatenated) tweet text to DBpedia Spotlight and collecting and counting all classes 
(DBpedia, Schema.org, YAGO) up to the root of their hierarchies for all DBpedia resources detected.
The backend API is non-blocking and can handle many requests concurrently; the client is set up to send several requests at a time.

### Frontend

The frontend is an [Angular](https://github.com/angular) application that sends REST GET requests to the backend to obtain users and their classes.

1. As users come in, a normalized tf-idf style score we call *cf-iuf* is calculated for each unique class of each user. As a result, common, generic classes get low scores,
while classes that uniquely define (groups of) users will get high scores. We can put extra weight on one or the other using a controllable *topic scope* correction parameter.

2. The cosine similarity between all pairs of users' class collections is calculated, based on the cf-iuf scores.
This similarity is then used as edge weights for a *similarity graph*, where each node is a user and each edge the semantic similarity between these users.

3. Lastly, the similarity graph is clustered using a custom Highly Connected Subgraph (HCS) algorithm, with minimum graph cuts found using Kruskal's minimum spanning tree algorithm on the graph edges sorted by weight.

This final graph is displayed to the user using a [D3](https://github.com/mbostock/d3) force graph, and can be manipulated by altering the topic scope 
(recalculating from cf-iuf onward with a different correction parameter), or by zooming in on a cluster
(the algorithm is re-run for those isolated users; essentially a recursive step to drill down into the topic hierarchy an arbitrary number of times for increasingly narrow topics).

The three calculation steps above are performed continuously and concurrently as new users come in, 
using HTML5 Web Workers to make full use the client machines' (presumably) multi-core environments, as well as to prevent the UI thread from blocking.
One worker is used for each of steps 1 and 3; we spawn 4 parallel workers for step 2, which is the most demanding 
(naive (sparse) matrix multiplication at the moment, yielding *O*(n^2 log(n)); significant optimizations are still possible and TODO).
