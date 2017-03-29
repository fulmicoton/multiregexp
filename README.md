multiregexp
===========

`multiregexp` is a more efficient way to match several regular expressions.

It offers a more performant alternative to the anti-pattern of looping on patterns. (This anti-pattern often happens when routing urls, building reports out of logs, searching for terms in a text, etc.)

```java
    for (final Pattern pattern: patterns) {
        final Matcher matcher = pattern.matcher(txt);
        // ...
    }
```

When using `multiregexp`, your regular expressions are compiled in once single (possible big) deterministic automaton. 

This library relies on [dk.brics.automaton](http://www.brics.dk/automaton/) for all the heavy lifting. 

More explanation is available in [this blog post](http://fulmicoton.com/posts/multiregexp/).


Disclaimer
--------------------

At the moment, MultiRegexp relies on [dk.brics.automaton](http://www.brics.dk/automaton/) to compile the individual regular expressions. 

It therefore presents the same limitations as this library :

* Only a subset of the java regular expression language is handled. 
* Some character are required to be escaped, even though it is not an issue for Java's pattern.
* *The library does not handle groups.*

This may change in the future.


Benchmark
--------------------

Following the benchmark described in `http://lh3lh3.users.sourceforge.net/reb.shtml`, we searched for URIs, email and dates using the following patterns.

Groups have been exchanged for anonymous groups to make sure Java was not penalized. The pattern chosen here are not especially pathological.

    URI (protocol://server/path): [a-zA-Z][a-zA-Z0-9]*://[^ /]+(?:/[^ ]*)?
    Email (name@server): [^ \@]+\@[^ \@]+
    Date (month/day/year): [0-9][0-9]?/[0-9][0-9]?/[0-9][0-9](?:[0-9][0-9])?

We measured the time to search for these 3 regular expression in a 40MB file :

    
    Java's Pattern
    build time (ms): 2ms
    match time (ms): 2.5s
    
    dku.brics.automaton
    build time (ms): 35ms
    match time (ms): 1s
    
    multipattern
    build time (ms): 60ms
    match time (ms): 105ms



Using it in your maven project
--------------------------------------------

Add the following lines in the dependencies section of your `pom.xml` file.

```xml    
    <dependency>
        <groupId>com.fulmicoton</groupId>
        <artifactId>multiregexp</artifactId>
        <version>0.5.1</version>
    </dependency>
```    



Matching
--------------------
	
```java
	import com.fulmicoton.multiregexp.MultiPatternMatcher;
	import com.fulmicoton.multiregexp.MultiPattern;


	// This is an equivalent to Pattern.compile(...)
	// You need to cache this object if you want to 
	// search on more than one string.
    MultiPatternMatcher matcher = MultiPattern.of(
            "ab+",     // 0
            "abc+",    // 1
            "ab?c",    // 2
            "v",       // 3
            "v.*",     // 4
            "(def)+"   // 5
    ).matcher();
    int[] matching = multiPattern.match("abc"); // return {1, 2}
```

Searching 
---------------------

```java
	import com.fulmicoton.multiregexp.MultiPatternSearcher;
	import com.fulmicoton.multiregexp.MultiPattern;

	// The searcher will 
    MultiPatternSearcher matcher = MultiPattern.of(
            "ab+",     // 0
            "abc+",    // 1
            "ab?c",    // 2
            "v",       // 3
            "v.*",     // 4
            "(def)+"   // 5
    ).searcher();
    MultiPatternSearcher.Cursor cursor = multiPatternSearcher.search("ab abc vvv");
    while (cursor.next()) {
    	int[] pattern cursor.matches();   // array with the pattern id which match ends at this position
        int start = cursor.start();
        int end = cursor.end();
    }
```
