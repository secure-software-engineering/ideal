IDE/AL - Alias-Aware Framework for Interprocedural Dataflow Analysis
===============================================================

This project is an extension to the [Heros](https://github.com/Sable/heros) framework 
and allows automatic reasoning  of aliases during an object tracking static analysis. Two examples 
of such an analysis are a taint or typestate analysis.

We implemented a typestate analysis in IDE/AL. A typestate analysis reasons about object and their states.
One such property is: A file must always be closed at the end of its lifetime. 

```
File file = new File();
File alias = file;
file.open();
alias.close();
```

In the example above, an alias to the object is created. There are two accessors to the same file object (```file``` and ```alias```). While
one of them receives the open call, the ```close``` call is triggered on the other accessor. IDE/AL automatically reasons
about the alias relationship internally and then propagates the typestate states over an objects flow graph.
In the example the typestate analysis implemented in IDE/AL can correctly reason about the object being correctly used.
The typestate analysis therefore must only specify the seed with is the accessor ```file``` at the statement ```file.open()``` , and the edge functions. The edge functions dictate the transformation of the environments along the flow path (e.g. when the object is switched from state open to closed). The seed and the edge functions are both specified through the interface [AnalysisProblem](src/ideal/AnalysisProblem.java).


# Examples

For examples on how to use IDE/AL have a look at the test cases. They can be found [here](tests/typestate/tests).

# Project Structure

Import the project into eclipse. The project contians four different source folders:

* *src*: The main source code of IDE/AL. IDE/AL can be used for any data-flow analysis based on IDE that also tracks object references.  
* *typestate*: The implementation of a typestate analysis in IDE/AL. This source code mainly implements the edge functions over the object flow graph retrieved from IDE/AL.
* *targets*: The program code the typestate analysis analyses. 
* *tests*: JUnit tests for the typestate analysis. 

# Licencse
IDE/AL is released under LGPL - see [LICENSE.txt](LICENSE.txt) for details.

# Authors
IDE/AL has been developed by [Johannes Späth](mailto:joh.spaeth@gmail.com), [Karim Ali](http://karimali.ca) and [Eric Bodden](http://bodden.de).
