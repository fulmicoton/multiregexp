multiregexp
===========

Performant Java library to check for multiple regexp. 
*This library relies on [dk.brics.automaton](http://www.brics.dk/automaton/) to do all the difficult stuff.*

Basically all of your regular expressions get compiled into one big deterministic automaton. 
Check [this blog post](http://fulmicoton.com/posts/multiregexp/) to understand how this is working,
and why it is way faster than your tradition for-loop on patterns.


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
    for (;cursor.found(); cursor.next()) {
    	int[] pattern cursor.matches();   // array with the pattern id which match ends at this position
    	int position = cursor.position(); // offset of the character after the match
    	// note that search is NOT greedy.
    	// ... and does not give the start offset of the match for the moment.
    }
    int[] matching = multiPattern.match("abc"); // return {1, 2}


Checkout [dk.brics.automaton](http://www.brics.dk/automaton/) to know what dialect of regular expressions is supported.
Note that groups are not handled.