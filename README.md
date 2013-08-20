Veloscope
=========

MVC Framework on top of Spring MVC
=========

I'd like to introduce to you a small framework for convient executing of many everyday task when you are working with Spring MVC. Veloscope is very small and intuitive (at least I want it to be). 

In the middle of it there are two main notions: Scope and Resource.

Scope
=====

Scope is an object which represent some domain-base query. Metaphorically speaking this is a window through which you are looking on database table. You can narrow that window by building a new scope from this one. Inside it uses hibernate Criteria to store query state. To create a scope you should invoke resource's `buildScope()` method.

You can execute a scope anytime with first(), list(page, perPage) or count(). Scopes can be created in chains one from another. Let's asume that you have a 'Book' entity and 'books' resource (read about resources below) and you just want all of the books that are read by a user.

`
List<Book> readBooks = books.buildScope()
                            .restr(Restrictions.eq('read', Boolean.TRUE))
                            .restr(Restrictions.eq('user.id', userId))
                            .list(0, 20);
`

Alright, not very exciting so far. But you can give to that piece of code a name by wrapping it into a resource method. Now it looks like

`List<Book> readBooks = books.readBy(userId).list(0, 20);`,
which is much more readable and concise;

To give a names to pieces of code is a very good practice which leads to a better code, musch closer to DSL. Just take a notice how easy to read this code. You just wrote "books read by...". Just an natural english you see.
