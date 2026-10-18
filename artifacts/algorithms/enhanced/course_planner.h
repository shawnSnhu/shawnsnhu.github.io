#ifndef COURSE_PLANNER_H
#define COURSE_PLANNER_H

#include <cstddef>
#include <string>
#include <unordered_map>
#include <vector>

// stores the information read for one course
struct Course {
    std::string courseNumber;
    std::string courseName;
    std::vector<std::string> prerequisites;
};

using CourseTable = std::unordered_map<std::string, Course>;

// represents prerequisite relationships as an adjacency list
struct CourseGraph {
    std::unordered_map<std::string, std::vector<std::string>> dependents;
    std::unordered_map<std::string, std::size_t> indegree;
};

struct SortResult {
    std::vector<std::string> courseOrder;
    bool cycleDetected = false;
};

std::string trim(const std::string& value);
std::string toUpper(const std::string& value);

// loads and validates a complete course data file
bool loadCourses(const std::string& filename,
                 CourseTable& courseTable,
                 std::string& errorMessage);

// creates prerequisite-to-dependent edges and indegree counts
bool buildCourseGraph(const CourseTable& courseTable,
                      CourseGraph& graph,
                      std::string& errorMessage);

// uses Kahn's algorithm to create a prerequisite-valid course order
SortResult generateCourseOrder(const CourseGraph& graph);

void printCourseOrder(const CourseTable& courseTable,
                      const std::vector<std::string>& courseOrder);

void printCourse(const CourseTable& courseTable,
                 const std::string& courseNumber);

#endif
