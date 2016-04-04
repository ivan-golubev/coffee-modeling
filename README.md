Modeling the coffee drinking process
------------------------------------

Model a system that allows programmers to buy coffee.

Installation
-------------------------
1. Either download a zip or perform a git checkout from [github][1].
2. Install [JDK 8][2].
3. Install [Maven 3][3].
4. [Set up][4] the environment variables: JAVA\_HOME, M2\_HOME.

Usage
-----
Execute this in console or in you favourite IDE:

`> mvn test`

This will model the scenarios with 100, 200, 500 and 1000 programmers.

Open generated reports in your favourite browser:
target/report-100-programmers.html, target/report-200-programmers.html, etc.

[1]: https://github.com/ivan-golubev/coffee-modeling
[2]: http://www.oracle.com/technetwork/java/javase/downloads
[3]: http://maven.apache.org/install.html
[4]: http://www.tutorialspoint.com/maven/maven_environment_setup.htm