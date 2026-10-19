#include "course_planner.h"

#include <iostream>
#include <string>
#include <unordered_map>
#include <vector>

namespace {

class TestRunner {
public:
    void expect(bool condition, const std::string& description) {
        if (condition) {
            ++passed;
            std::cout << "PASS: " << description << std::endl;
        } else {
            ++failed;
            std::cout << "FAIL: " << description << std::endl;
        }
    }

    int exitCode() const {
        std::cout << std::endl
                  << passed << " checks passed; " << failed << " failed."
                  << std::endl;
        return failed == 0 ? 0 : 1;
    }

private:
    int passed = 0;
    int failed = 0;
};

bool prerequisitesComeFirst(const CourseTable& courseTable,
                            const std::vector<std::string>& courseOrder) {
    if (courseOrder.size() != courseTable.size()) {
        return false;
    }

    // Compare relative positions because more than one valid course order can exist.
    std::unordered_map<std::string, std::size_t> position;
    for (std::size_t index = 0; index < courseOrder.size(); ++index) {
        if (!position.emplace(courseOrder[index], index).second) {
            return false;
        }
    }

    for (const auto& entry : courseTable) {
        auto coursePosition = position.find(entry.first);
        if (coursePosition == position.end()) {
            return false;
        }

        for (const std::string& prerequisite : entry.second.prerequisites) {
            auto prerequisitePosition = position.find(prerequisite);
            if (prerequisitePosition == position.end() ||
                prerequisitePosition->second >= coursePosition->second) {
                return false;
            }
        }
    }

    return true;
}

bool containsText(const std::string& value, const std::string& expected) {
    return value.find(expected) != std::string::npos;
}

void testValidGraph(TestRunner& tests) {
    CourseTable courses;
    std::string errorMessage;

    bool loaded = loadCourses("test_data/valid_courses.csv",
                              courses,
                              errorMessage);
    tests.expect(loaded, "valid course data loads");
    tests.expect(courses.size() == 9,
                 "courses with and without prerequisites are retained");

    CourseGraph graph;
    bool graphBuilt = buildCourseGraph(courses, graph, errorMessage);
    tests.expect(graphBuilt && graph.indegree.at("CSCI300") == 2,
                 "indegree records both prerequisites for CSCI300");
    tests.expect(graph.dependents.at("CSCI200").size() == 3,
                 "adjacency list records courses that depend on CSCI200");

    SortResult result = generateCourseOrder(graph);
    tests.expect(!result.cycleDetected, "valid graph has no cycle");
    tests.expect(prerequisitesComeFirst(courses, result.courseOrder),
                 "every prerequisite appears before its dependent course");
    tests.expect(courses.at("HIST100").prerequisites.empty() &&
                     courses.at("MATH201").prerequisites.empty(),
                 "disconnected courses without prerequisites are supported");
}

void testMissingPrerequisite(TestRunner& tests) {
    CourseTable courses;
    std::string errorMessage;

    bool loaded = loadCourses("test_data/missing_prerequisite.csv",
                              courses,
                              errorMessage);
    tests.expect(!loaded && containsText(errorMessage, "missing prerequisite"),
                 "missing prerequisite rejects the complete dataset");
}

void testDuplicateCourse(TestRunner& tests) {
    CourseTable courses;
    std::string errorMessage;

    bool loaded = loadCourses("test_data/duplicate_course.csv",
                              courses,
                              errorMessage);
    tests.expect(!loaded && containsText(errorMessage, "duplicate"),
                 "duplicate course number is not silently overwritten");
}

void testSelfPrerequisite(TestRunner& tests) {
    CourseTable courses;
    std::string errorMessage;

    bool loaded = loadCourses("test_data/self_prerequisite.csv",
                              courses,
                              errorMessage);
    tests.expect(!loaded && containsText(errorMessage, "own prerequisite"),
                 "self-prerequisite is rejected");
}

void testCycle(TestRunner& tests) {
    CourseTable courses;
    std::string errorMessage;

    bool loaded = loadCourses("test_data/cycle.csv", courses, errorMessage);
    tests.expect(!loaded && containsText(errorMessage, "circular"),
                 "multi-course cycle is detected");
}

void testMalformedAndEmptyFields(TestRunner& tests) {
    CourseTable courses;
    std::string errorMessage;

    bool malformedLoaded = loadCourses("test_data/malformed.csv",
                                       courses,
                                       errorMessage);
    tests.expect(!malformedLoaded && containsText(errorMessage, "line 2"),
                 "malformed row is rejected with its line number");

    bool emptyNumberLoaded = loadCourses("test_data/empty_course_number.csv",
                                         courses,
                                         errorMessage);
    tests.expect(!emptyNumberLoaded &&
                     containsText(errorMessage, "empty course number"),
                 "empty course number is rejected");

    bool emptyTitleLoaded = loadCourses("test_data/empty_course_title.csv",
                                        courses,
                                        errorMessage);
    tests.expect(!emptyTitleLoaded &&
                     containsText(errorMessage, "empty course title"),
                 "empty course title is rejected");
}

void testFailedReloadPreservesData(TestRunner& tests) {
    CourseTable courses;
    std::string errorMessage;

    bool firstLoad = loadCourses("test_data/valid_courses.csv",
                                 courses,
                                 errorMessage);
    std::size_t originalSize = courses.size();

    // Failed input must not replace the last dataset that loaded successfully.
    bool secondLoad = loadCourses("test_data/does_not_exist.csv",
                                  courses,
                                  errorMessage);

    tests.expect(firstLoad && !secondLoad,
                 "missing file reports a failed reload");
    tests.expect(courses.size() == originalSize &&
                     courses.find("CSCI400") != courses.end(),
                 "failed reload preserves previously loaded course data");

    bool invalidDataLoad = loadCourses("test_data/missing_prerequisite.csv",
                                       courses,
                                       errorMessage);
    tests.expect(!invalidDataLoad && courses.size() == originalSize &&
                     courses.find("CSCI400") != courses.end(),
                 "invalid dataset also preserves previously loaded data");
}

}  // namespace

int main() {
    TestRunner tests;

    testValidGraph(tests);
    testMissingPrerequisite(tests);
    testDuplicateCourse(tests);
    testSelfPrerequisite(tests);
    testCycle(tests);
    testMalformedAndEmptyFields(tests);
    testFailedReloadPreservesData(tests);

    return tests.exitCode();
}
