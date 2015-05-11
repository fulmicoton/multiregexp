multiregexp
===========

MultiRegexp is a more efficient to match multiregexp at the same time. You might be interested by it if you have
a piece of code that looks like.

    for (final Pattern pattern: patterns) {
        final Matcher matcher = pattern.matcher(txt);
        // ...
    }

Performant Java library to check for multiple regexp. 
*This library relies on [dk.brics.automaton](http://www.brics.dk/automaton/) for all the heavy lifting.

All of your regular expressions get compiled into one big deterministic automaton. 

Check [this blog post](http://fulmicoton.com/posts/multiregexp/) to understand how this is working,
and why it is way faster than your tradition for-loop on patterns.


Disclaimer
--------------------

At the moment, MultiRegexp relies on [dk.brics.automaton](http://www.brics.dk/automaton/) to compile the individual regular expressions. 

It therefore includes the same limitation as this library.

* Only a subset of the java regular expression language is handled. 
* Some character are required to be escaped, even though it is not an issue for Java's pattern.
* The library does not handle groups.


Benchmark
--------------------

Using the benchmark described `http://lh3lh3.users.sourceforge.net/reb.shtml`, we searched for URIs, email and dates in the 40MB file using the following regular expressions.

    URI (protocol://server/path): [a-zA-Z][a-zA-Z0-9]*://[^ /]+(?:/[^ ]*)?
    Email (name@server): [^ \@]+\@[^ \@]+
    Date (month/day/year): [0-9][0-9]?/[0-9][0-9]?/[0-9][0-9](?:[0-9][0-9])?

We used three methods to match the regular expressions.
Java's Pattern, DKU brics, and MultiPattern.

    ---------------------------
    JAVA
    build time (ms): 4ms
    match time (ms): 20s
    ---------------------------
    DKU
    build time (ms): 180ms
    match time (ms): 2s
    ---------------------------
    MULTIPATTERN
    build time (ms): 200ms
    match time (ms): 120ms


Matching
--------------------
	
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


Searching 
---------------------
	
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

