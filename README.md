Veloscope
======================
MVC Framework on top of Spring MVC

Overview
----------------------
I'd like to introduce to you a small framework for convient executing of many everyday task when you are working with Spring MVC. Veloscope is very small and intuitive (at least I want it to be). 

In the middle of it there are two main notions: Scope and Resource.

Impression tutorial
----------------------
Here's what I call "an impression tutorial" to highlight most important and impressive (IMO) features of the framework. If you don't think like 'wow, this is neat' while reading it, than you probably don't need it. Some details are omited for conciseness, but we will fill all of the gaps in documentation below.

Alright, now let's assume that you already have a working Spring3 project. It's a (surprize!) a blog system and you have a Blog database entity.

    @Entity
    @Table(name = "BLOG")
    public class Blog {
       private Long id;
       private User author;
       private String title;
       private String body;
       private Date created;
       /* all the getters and setters, all properly mapped */
    }
    
The first thing that you do when working with Veloscope is creating a *resource*. Resource is a singleton object (only one in spring context) which encapsulates some domain logic and come restrictions logic. This is a cornerstone notion in Velosope so let's look how it can help you.

First of all you create a resource by extending `Resource<T>` class.

    public class Blogs extends Resource<Blog> {
        public Blogs() {
            super(Blog.class);
        }
    }
    
That is all. Now you have a Blogs resource. As I said before resource encapsulates some DAO functionality. All basic methods like `T findById(...)`, `save(T obj)`, `List<T> all()` and many others are already here for your use. But this is actually not important. Let's move to important things. 

You want to have an API for blogs. This is a common task, no one blames you. Normally you would create a new controller class and do all the stuff with your hands. This is the point where Veloscope wants to be helpful. 

Let's assume that you want a list of all blogs. You just need an annotation `@List`

    @List
    public class Blogs extends Resource<Blog> {
        public Blogs() {
            super(Blog.class);
        }
    }

This is all, you see. Just try `GET /veloapi/blogs`. And you get something like `{ 'status': 'ok', 'data': {...} }` with all your Blog entries inside data array. Isn't it cool? No it isn't. Because you don't need all of the blogs, you just want the blogs by some user. Well, let's see what we can do.

    @List(by = "user.id")
    public class Blogs extends Resource<Blog> {
        public Blogs() {
            super(Blog.class);
        }
    }
    
Now you can `GET /veloapi/blogs?user_id=123` and you get your blogs filtered. That's better but... Is it ok to anyone to use this API method? You want only authorized. Just tell it to *velo*.

    @List(by = "account.id" onlyIf = "authorized")
    public class Blogs extends Resource<Blog> {
        public Blogs() {
            super(Blog.class);
        }
    }

How does it know if a user is authorized? Well, it depends on Spring Security, so be attentive here. Frankly speaking "authorize" is just a predefined rule and you can easily add your own, but that's a different story...

In the same manner you can animate a `GET /veloapi/blogs/123` route for a single REST entity retrieve.

    @Get
    @List(by = "account.id" onlyIf = "authorized")
    public class Blogs extends Resource<Blog> {
        public Blogs() {
            super(Blog.class);
        }
    }

Sometimes you just want to delete a thing. But only if it's yours. How do you do it with Velocope? Easily.

    @Get
    @List(by = "account.id" onlyIf = "authorized")
    @Delete(onlyIf = "@user = me")
    public class Blogs extends Resource<Blog> {
        public Blogs() {
            super(Blog.class);
        }
    }

Now go ahead and try `DELETE /veloapi/blogs/123`, it should work. But. You don't really want to actualy *delete* a row from a database, you just want to mark it as deleted, updating `deleted = true`, and also you want all of the DAO methods to behave as if it was really deleted. ALL. OF. THE. OTHER. DAO. METHODS. You know what? Right, just add an annotaion.

    @Get
    @List(by = "account.id" onlyIf = "authorized")
    @Delete(onlyIf = "@user = me")
    @FakeDelete
    public class Blogs extends Resource<Blog> {
        public Blogs() {
            super(Blog.class);
        }
    }
    
An exression `@user` means that the entity we are talking about has a property named user and what's more important a getter and a setter for it. Velo heavily use reflection, so you will see an exception if you forget to create these.

Now when you try to `blogs.delete(blog)` it will just update a field, and when you try to `blogs.findById(blog.getId())`
you will get null. Are you impressed? Well you should be. By now you should understand the main philosophy of Veloscope. It sounds like this. If there's a common task you want to implement, there's probably an annotation for it. So it will come as no surprise for you, when you'll want to order the response blogs by created date and you'll find this handy annotation `@DefaultOrder`.

    @Get
    @List(by = "account.id" onlyIf = "authorized")
    @Delete(onlyIf = "@user = me")
    @FakeDelete
    @DefaultOrder(by = "createdAt" order = "desc")
    public class Blogs extends Resource<Blog> {
        public Blogs() {
            super(Blog.class);
        }
    }

That was a tip of the iceberg, but by now you should understand what you are dealing with. 

Scope
----------------------
Scope is an object which represent some domain-base query. Metaphorically speaking this is a window through which you are looking on database table. You can narrow that window by building a new scope from this one. Inside it uses hibernate Criteria to store query state. To create a scope you should invoke resource's `buildScope()` method.

`Scope scope = books.buildScope()`

When you create a scope, add restriction or orders no queries are actually execute. The scope object just incapsulates all the conditions you need. You can execute a scope (fire a database request) anytime with `first(), list(page, perPage) or count()` methods. Scopes can be created in chains one from another. Let's asume that you have a 'Book' entity and 'books' resource (read about resources below) and you just want all of the books that are read by a user.

`
List<Book> readBooks = books.buildScope()
                            .restr(Restrictions.eq('read', Boolean.TRUE))
                            .restr(Restrictions.eq('user.id', userId))
                            .list(0, 20);
`

Alright, not very exciting so far. But you can give to that piece of code a name by wrapping it into a resource method. Now it looks like

`List<Book> readBooks = books.readBy(userId).list(0, 20);` which is much more readable and concise;

To give a names to pieces of code is a very good practice which leads to a better code, musch closer to DSL. Just take a notice how easy to read this code. You just wrote "books read by...". Just an natural english you see.
