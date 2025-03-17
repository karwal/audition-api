# Audition API

This is new ReadMe file. The original file has been kept in ReadMe_Original.md for reference.

## Following has been implemented.

1. Google code style imported into IntelliJ and Save Actions set, while development.
2. All TODOs has been resolved. All TODOs has been marked as resolved by changing TODO->TOD-Resolved for easy reference
   of reviewer.
3. Basic Authentication has been added.

# Tech-Stack Used to build application:

1. Java 17
2. SpringBoot
3. JUnit5
4. Mockito
5. Gradle

# Development Methodology / Assumptions:

1. TDD approach.
2. 100% code coverage for Integration and Web layers.
3. Upgrading dependencies versions is out of scope.
4. Fully compilable and working application

# References

1. Some references from Baeldung

# How to run this application

* Clone the git repo using following command

```
cd ..
git clone https://github.com/karwal/audition-api.git
```

This will create a folder audition-api in your current working directory.

* Execute command:

``` cd audition-api ```

* Compile code using following command

``` gradle clean test build ```