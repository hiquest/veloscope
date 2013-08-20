Veloscope
=========

MVC Framework on top of Spring MVC
=========

I'd like to introduce to you a small framework for convient executing of many everyday task when you are working with Spring MVC. Veloscope is very small and intuitive (at least I want it to be). 

In the middle of it there is two main notions: Scope and Resource.

Scope
=====

Scope is an object which represent some domain-base query. Metaphorically speaking this is a window through which you are looking on database table. You can narrow that window by building a new scope from this one. Inside it uses hibernate Criteria to store query state. 

You can execute a scope anytime with first() or list(). Scopes can be created in chains one from another.
