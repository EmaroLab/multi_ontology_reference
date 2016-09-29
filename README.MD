#AMOR

**AMOR** - a Multi-Ontology Reference library, is a complete library of helper functions for OWL ontologies with some remarkable features. 

+ Support for multiple ontologies.
+ Ensures thread-safe ontology manipulations.
+ Implements a wide range of functions: from simple "add an individual" to more esoteric "modify an SWRL rule".
+ Excellent performances which can be further customized based on user needs.
+ Ontology debug GUI available (currently working but not stable).
+ Extensive logging.
+ Out-of-the-box support for most reasoners. Easy to add more.
+ Based on latest standards: Java 8 and OWL API 5.

This is the ROS version of the library, ready to be included in your ROS projects. 

For legacy support (Java 7 and OWL API 3.4.5) and more reasoner choices, use the [OWL-api3](https://github.com/EmaroLab/multi_ontology_reference/tree/OWL-api3) branch.

**master** and **OWL-api5** branches are OWL api 5 based and currently support only [*Openllet*](https://github.com/Galigator/openllet), an open source fork of the Pellet reasoner. More will be added as they are ported to OWL api 5.

Full documentation coming soon. If you need, you can refer to the code itself since most public functions already come with docstrings.
