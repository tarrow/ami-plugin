# AMI Tutorial

## Quickstart

`ami` generally runs as 

a `<plugin>` with some `<pluginOptions>` reading some `<input>` and creating some `<results>`. A typical example is:
```
ami2-regex -q temp1 -i scholarly.html --context 25 40 --r.regex regex/phylotree.xml
``` 
This runs the `regex` plugin on the input file `scholarly.html` in the `temp1` contentMine directory using the option `phylotree` (a collection of regular expressions). The results will be in `results/regex/phylotree/results.xml`. The results are surrounded by 25 prefix characters and 40 postfix characters of context. (Arguments can be in any order). That's all there is to running `ami`.

However if you want to modify `ami` or write a new plugin, you need to know the organization...

## Overview 

AMI is highly modular and consists of Java code, XML control, data and tests. 

 * Unless you are writing a plugin you don't need to know about the code. 
 * The XML (`args.xml`) controls what options each plugin has and how the system decides what to do and when
 * the data suports the plugin (e.g. `stopwords.txt` for `ami-word`)
 * the tests help development, but also check the correctness of the code and make sure that bugs don't creep in ("regression"). Tests are also useful in showing some of the options - e.g. for tutorials.
 
 AMI is built as a `maven` framework. This shouldn't worry you - just accept that many of the filenames are conventional. Here's the current structure:
 
``` .
├── LICENSE
├── README.md
├── docs
```
`docs` contains documentation in `*.md` files (markdown) 
```
│   ├── AMI.md
│   ├── COCHRANE_20150316.md
│   ├── PLUGINS.md
│   ├── REGEX.md
│   ├── SEARCHING.md
│   ├── WORDS.md
...
```
Then a directory with some example files (here clinical trials).
```
├── examples
│   ├── http_www.trialsjournal.com_content_16_1_1
│   ├── http_www.trialsjournal.com_content_16_1_11
...
│   ├── http_www.trialsjournal.com_content_16_1_2
│   └── http_www.trialsjournal.com_content_16_1_3
```
`pom.xml` is a magic file that tells  Maven how to build the system automatically. It may need tweaking if we add a plugin.
```
├── pom.xml
```
There's a communal directory of CompoundRegex files (for use by `ami-regex`) and we'd expect people to edit this or add new ones.
```
├── regex
│   ├── common.xml
│   ├── consort0.xml
│   ├── figure.xml
│   ├── phylotree.xml
│   └── publication.xml
```
`src` contains the source code and supporting files
```
├── src
```
`deb` contains the file to control the `*.deb` creation.
```
│   ├── deb
```
`main` is for building the deployable system (`*.jar`, `*.deb`, etc)
```
│   ├── main
```
`test` is for testing and does not get distributed
```
│   └── test
```
`target` is temporary. It is deleted (`clean`ed) when the system is rebuilt. It contains the latest distributables and also a range of temporary files.
```
└── target
```
`target` contains the distributable executable files (these change with every build).
```
    ├── ami2-0.1-SNAPSHOT-bin.tar.gz
    ├── ami2-0.1-SNAPSHOT-bin.zip
    ├── ami2-0.1-SNAPSHOT.jar
    ├── ami2_0.1~SNAPSHOT_all.changes
    ├── ami2_0.1~SNAPSHOT_all.deb
    ├── appassembler
```
The rest only matters to a Java programmer.    
 
## the `src` trees

The overall structure is:
```
src
├── deb
│   └── control
├── main
│   ├── assembly
│   ├── java
│   └── resources
└── test
    ├── java
    └── resources
```

If you are tweaking a plugin you may need to know some of this
```
src
```
`main` is for the deployed system
```
├── main
```
it contains the source code. You shouldn't have to deal with it unless you create a plugin ...
```
│   ├── java
```
... and configuration and modification of the common data for the program (i.e. independent of a particular problem). 
```
│   └── resources
```
`test` is valuable for exploring what the program does. Also any new functionality should be test-driven (TDD) where possible. 
```
└── test
```
In an ideal world every `src/main/java` class should have a set of tests in `src/test/java`
```
    ├── java
```
and `resources` holds any data the tests need.
```    
    └── resources
```

## anatomy of a plugin

Each plugin is split across some or all of the 4 branches. In the future we'll have tools to create them, but at present you need to do this yourself. Every java class should have a unique name ("fully qualified name") which normally uses a domain name. Our plugins (we'll use the dummy name `foo`) have the form:
```
package org.xmlcml.ami2.plugins.foo;

public class FooPlugin extends AMIPlugin { ...
```
[The packagename  `org.xmlcml...` arises from the extensive use of chemistry (xml-cml.org) in much of PMR's code. You may also see `uk.ac.cam.ch...` in some places.] So our current plugins have the tree structure:
```
src
└── main
    └── java
		└── org
		    └── xmlcml
		        └── ami2
		            ├── lookups
		            	...
		            └── plugins
		                ...
		                ├── identifier
		                ├── regex
		                ├── sequence
		                ├── simple
		                ├── species
		                └── word

```

## `src/main/java` tree

Ignoring `lookups` (and some general classes) we can see 6 plugins (`identifier`, ... `word`). They all have a similar structure:
```
src/main/java/org/xmlcml/ami2/plugins/
├── identifier
│   ├── IdentifierArgProcessor.java
│   ├── IdentifierPlugin.java
│   ├── IdentifierSearcher.java
├── regex
│   ├── CompoundRegex.java
│   ├── CompoundRegexList.java
│   ├── RegexArgProcessor.java
│   ├── RegexComponent.java
│   ├── RegexPlugin.java
│   └── RegexSearcher.java
├── sequence
│   ├── SequenceArgProcessor.java
│   ├── SequencePlugin.java
│   ├── SequenceResultElement.java
│   ├── SequenceSearcher.java
├── simple
│   ├── SimpleArgProcessor.java
│   └── SimplePlugin.java
├── species
│   ├── LinneanName.java
│   ├── LinneanNamer.java
│   ├── SpeciesArgProcessor.java
│   ├── SpeciesPlugin.java
│   ├── SpeciesResultElement.java
│   ├── SpeciesResultsElement.java
│   ├── SpeciesSearcher.java
└── word
    ├── WordArgProcessor.java
    ├── WordCollectionFactory.java
    ├── WordPlugin.java
    ├── WordResultElement.java
    ├── WordResultsElement.java
    ├── WordResultsElementList.java
    └── WordSetWrapper.java
```
Note that almost every plugin has:
 * an `ArgProcessor` (`IdentifierArgProcessor.java`) which scanns the input and interprets and executes the arguments in the commandline
 * a `Plugin` (`IdentifierPlugin.java`) which manages overall control for the plugin
 * a `Searcher` (`IdentifierSearcher.java`) which searches the input for matches, records and processes them. This is the heart of the logic of the plugin. It may be as simple as a default regeular expression (almost a dummy) or a complete NLP-based chemistry matching system (OSCAR).
 * a `ResultElement` and maybe `ResultsElement` which manage the post-match logic (e.g. transforming the raw match into a semantic object. Thus `SpeciesResultsElement` tries to expand abbreviations such as `V. harveyi` to `Vibrio Harveyi`. Note the helper classes such as `LinneanNamer`.
 
## the `src/main/resources` tree

The `resources` tree contaoins configuration files which are independent of the input files. Note that they use the same class/package naming structure
```
src
└── main
	└── resources
		├── log4j.properties
		└── org
		    └── xmlcml
		        └── ami2
		            ├── plugins
		            │   ├── args.xml
		            │   ├── identifier
		            │   │   └── args.xml
		            │   ├── regex
		            │   │   └── args.xml
		            │   ├── sequence
		            │   │   └── args.xml
		            │   ├── simple
		            │   │   └── args.xml
		            │   ├── species
		            │   │   └── args.xml
		            │   └── word
		            │       ├── args.xml
		            │       ├── clinicaltrials200.txt
		            │       └── stopwords.txt
		            └── tagger // obsolete
```
 * `log4j.properies controls the `debug` output from the `log4j` logging tool. 
 * `org/xmlcml/ami2/plugins/args.xml` configuration applies to all plugins.
 * `org/xmlcml/ami2/plugins/identifier/args.xml` configuration applies to just the `identifier` plugins (and so on)
 
 The `args.xml` define what arguments are allowed and how they are used. In addition some plugins use extra files. In this case `ami-word` uses lists of "stopwords" - words to be excluded from final lists. Here there are 2 - one general (`stopwrds.txt`) and one for 200 common words in clinical trials (`clinicaltrials200.txt`). (We'll examine the args in detail later).
 
 ## the `src/test/java` tree 
 
 This mirrors the plugin classes:
 ```
src/test/java
└── org
    └── xmlcml
        └── ami2
            ├── ClinicalTrialsDemo.java
            ├── Fixtures.java
            ├── lookups
            │   └── WikipediaLookupTest.java
            └── plugins
                ├── AMIPluginTest.java
                ├── RegressionDemoTest.java
                ├── identifiers
                │   └── IdentifierArgProcessorTest.java
                ├── regex
                │   └── RegexPluginTest.java
                ├── sequence
                │   └── SequenceArgProcessorTest.java
                ├── simple
                │   └── SimplePluginTest.java
                ├── species
                │   └── SpeciesArgProcessorTest.java
                └── word
                    └── WordTest.java
 ```
  * `Fixtures.java` defines files and routines used by all tests.
  * each class has a test. Reading these should give some idea of how the class is used. 
  
  Results come out in `results` and will be described elsewhere.
  

