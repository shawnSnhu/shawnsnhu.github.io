# Enhanced Course Planner

This program loads course data from a CSV file and creates a valid course order based on prerequisites. It also checks for invalid course data, missing prerequisites, and circular prerequisite relationships.

## Build and Run

Run these commands from the `enhanced` directory:

```
clang++ -std=c++17 -Wall -Wextra -Wpedantic main.cpp course_planner.cpp -o course_planner
./course_planner
```

Choose option `1` and enter the sample file:

```
test_data/valid_courses.csv
```

Choose option `2` to print a valid course order or option `3` to look up a specific course.

## Run the Tests

```
clang++ -std=c++17 -Wall -Wextra -Wpedantic course_planner_tests.cpp course_planner.cpp -o course_planner_tests
./course_planner_tests
```

## CSV Format

Each line contains a course number, course title, and any prerequisites:

```
CSCI100,Introduction to Programming
CSCI200,Data Structures,CSCI100
MATH201,Discrete Mathematics
CSCI300,Algorithms,CSCI200,MATH201
```
