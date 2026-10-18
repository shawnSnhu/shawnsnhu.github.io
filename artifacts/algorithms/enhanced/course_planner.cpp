#include "course_planner.h"

#include <cctype>
#include <fstream>
#include <iostream>
#include <queue>
#include <utility>

namespace {

// splits the simple comma-separated format used by the course data files
// empty fields are kept so they can be reported as validation errors
std::vector<std::string> splitCSV(const std::string& line) {
    std::vector<std::string> fields;
    std::size_t start = 0;

    while (start <= line.size()) {
        std::size_t comma = line.find(',', start);

        if (comma == std::string::npos) {
            fields.push_back(trim(line.substr(start)));
            break;
        }

        fields.push_back(trim(line.substr(start, comma - start)));
        start = comma + 1;
    }

    return fields;
}

}  // namespace

std::string trim(const std::string& value) {
    std::size_t first = value.find_first_not_of(" \t\r\n");
    std::size_t last = value.find_last_not_of(" \t\r\n");

    if (first == std::string::npos || last == std::string::npos) {
        return "";
    }

    return value.substr(first, last - first + 1);
}

std::string toUpper(const std::string& value) {
    std::string result = value;

    for (char& character : result) {
        character = static_cast<char>(
            std::toupper(static_cast<unsigned char>(character)));
    }

    return result;
}

bool loadCourses(const std::string& filename,
                 CourseTable& courseTable,
                 std::string& errorMessage) {
    std::ifstream file(filename);
    if (!file.is_open()) {
        errorMessage = "file could not be opened: " + filename;
        return false;
    }

    // work in a temporary table so a failed reload does not erase valid data
    CourseTable candidateTable;
    std::string line;
    std::size_t lineNumber = 0;

    while (std::getline(file, line)) {
        ++lineNumber;

        if (trim(line).empty()) {
            continue;
        }

        std::vector<std::string> fields = splitCSV(line);
        if (fields.size() < 2) {
            errorMessage = "line " + std::to_string(lineNumber) +
                           " must contain a course number and title";
            return false;
        }

        Course course;
        course.courseNumber = toUpper(fields[0]);
        course.courseName = fields[1];

        if (course.courseNumber.empty()) {
            errorMessage = "line " + std::to_string(lineNumber) +
                           " has an empty course number";
            return false;
        }

        if (course.courseName.empty()) {
            errorMessage = "line " + std::to_string(lineNumber) +
                           " has an empty course title";
            return false;
        }

        if (candidateTable.find(course.courseNumber) != candidateTable.end()) {
            errorMessage = "duplicate course number on line " +
                           std::to_string(lineNumber) + ": " +
                           course.courseNumber;
            return false;
        }

        for (std::size_t index = 2; index < fields.size(); ++index) {
            std::string prerequisite = toUpper(fields[index]);

            if (prerequisite.empty()) {
                errorMessage = "line " + std::to_string(lineNumber) +
                               " has an empty prerequisite";
                return false;
            }

            if (prerequisite == course.courseNumber) {
                errorMessage = course.courseNumber +
                               " cannot be its own prerequisite";
                return false;
            }

            course.prerequisites.push_back(prerequisite);
        }

        candidateTable.emplace(course.courseNumber, course);
    }

    if (file.bad()) {
        errorMessage = "an error occurred while reading: " + filename;
        return false;
    }

    if (candidateTable.empty()) {
        errorMessage = "the file does not contain any courses";
        return false;
    }

    CourseGraph graph;
    if (!buildCourseGraph(candidateTable, graph, errorMessage)) {
        return false;
    }

    SortResult sortResult = generateCourseOrder(graph);
    if (sortResult.cycleDetected) {
        errorMessage = "a circular prerequisite relationship was detected";
        return false;
    }

    courseTable = std::move(candidateTable);
    errorMessage.clear();
    return true;
}

bool buildCourseGraph(const CourseTable& courseTable,
                      CourseGraph& graph,
                      std::string& errorMessage) {
    CourseGraph candidateGraph;

    // every course is a vertex, including courses with no edges
    for (const auto& entry : courseTable) {
        candidateGraph.dependents[entry.first] = {};
        candidateGraph.indegree[entry.first] = 0;
    }

    for (const auto& entry : courseTable) {
        const Course& course = entry.second;

        for (const std::string& prerequisite : course.prerequisites) {
            if (courseTable.find(prerequisite) == courseTable.end()) {
                errorMessage = course.courseNumber +
                               " references missing prerequisite " +
                               prerequisite;
                return false;
            }

            if (prerequisite == course.courseNumber) {
                errorMessage = course.courseNumber +
                               " cannot be its own prerequisite";
                return false;
            }

            // the edge points from the prerequisite to the dependent course
            candidateGraph.dependents[prerequisite].push_back(
                course.courseNumber);
            ++candidateGraph.indegree[course.courseNumber];
        }
    }

    graph = std::move(candidateGraph);
    errorMessage.clear();
    return true;
}

SortResult generateCourseOrder(const CourseGraph& graph) {
    SortResult result;
    std::unordered_map<std::string, std::size_t> remainingIndegree =
        graph.indegree;
    std::queue<std::string> readyCourses;

    for (const auto& entry : remainingIndegree) {
        if (entry.second == 0) {
            readyCourses.push(entry.first);
        }
    }

    while (!readyCourses.empty()) {
        std::string courseNumber = readyCourses.front();
        readyCourses.pop();
        result.courseOrder.push_back(courseNumber);

        auto dependentEntry = graph.dependents.find(courseNumber);
        if (dependentEntry == graph.dependents.end()) {
            continue;
        }

        for (const std::string& dependent : dependentEntry->second) {
            std::size_t& count = remainingIndegree.at(dependent);
            --count;

            if (count == 0) {
                readyCourses.push(dependent);
            }
        }
    }

    result.cycleDetected =
        result.courseOrder.size() != remainingIndegree.size();

    // partial results should not be mistaken for a valid course order
    if (result.cycleDetected) {
        result.courseOrder.clear();
    }

    return result;
}

void printCourseOrder(const CourseTable& courseTable,
                      const std::vector<std::string>& courseOrder) {
    std::cout << "Here is one valid prerequisite-based course order:"
              << std::endl;

    for (const std::string& courseNumber : courseOrder) {
        const Course& course = courseTable.at(courseNumber);
        std::cout << course.courseNumber << ", " << course.courseName
                  << std::endl;
    }
}

void printCourse(const CourseTable& courseTable,
                 const std::string& courseNumber) {
    std::string key = toUpper(trim(courseNumber));
    auto courseEntry = courseTable.find(key);

    if (courseEntry == courseTable.end()) {
        std::cout << "Course not found." << std::endl;
        return;
    }

    const Course& course = courseEntry->second;
    std::cout << course.courseNumber << ", " << course.courseName << std::endl;

    if (course.prerequisites.empty()) {
        std::cout << "Prerequisites: None" << std::endl;
        return;
    }

    std::cout << "Prerequisites: ";

    for (std::size_t index = 0; index < course.prerequisites.size(); ++index) {
        const std::string& prerequisite = course.prerequisites[index];
        auto prerequisiteEntry = courseTable.find(prerequisite);

        if (prerequisiteEntry != courseTable.end()) {
            std::cout << prerequisiteEntry->second.courseNumber << " ("
                      << prerequisiteEntry->second.courseName << ")";
        } else {
            std::cout << prerequisite;
        }

        if (index + 1 < course.prerequisites.size()) {
            std::cout << ", ";
        }
    }

    std::cout << std::endl;
}
