multiregexp
===========

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/4f8a29a2d286645eb180ed7b667349dc "githalytics.com")](http://githalytics.com/poulejapon/multiregexp)

Performant Java library to check for multiple regexp. 
*This library relies on [dk.brics.automaton](http://www.brics.dk/automaton/) to do all the difficult stuff.*

Basically all of your regular expressions get compiled into one big deterministic automaton. 

Usage is quite simple.

     MultiPattern.compile(
            "ab+",     // 0
            "abc+",    // 1
            "ab?c",    // 2
            "v",       // 3
            "v.*",     // 4
            "(def)+"   // 5
    );
    int[] matching = multiPattern.match("abc"); // return {1, 2}

Checkout [dk.brics.automaton](http://www.brics.dk/automaton/) to know what dialect of regular expressions is supported.
Note that groups are not handled.