#include "course_planner.h"

#include <iostream>
#include <limits>
#include <string>

namespace {

void printMenu() {
    std::cout << std::endl;
    std::cout << "1. Load Course Data" << std::endl;
    std::cout << "2. Print Prerequisite-Based Course Order" << std::endl;
    std::cout << "3. Print Course" << std::endl;
    std::cout << "9. Exit" << std::endl;
}

}  // namespace

int main() {
    CourseTable courseTable;
    bool dataLoaded = false;
    int choice = 0;

    std::cout << "Welcome to the course planner." << std::endl;

    while (choice != 9) {
        printMenu();
        std::cout << "What would you like to do? ";

        if (!(std::cin >> choice)) {
            std::cin.clear();
            std::cin.ignore(std::numeric_limits<std::streamsize>::max(), '\n');
            std::cout << "Please enter a number from the menu." << std::endl;
            continue;
        }

        std::cin.ignore(std::numeric_limits<std::streamsize>::max(), '\n');

        if (choice == 1) {
            std::string filename;
            std::string errorMessage;

            std::cout << "Enter the file name: ";
            std::getline(std::cin, filename);

            if (loadCourses(filename, courseTable, errorMessage)) {
                dataLoaded = true;
                std::cout << "Data loaded successfully." << std::endl;
            } else {
                std::cout << "Error: " << errorMessage << std::endl;
            }
        } else if (choice == 2) {
            if (!dataLoaded) {
                std::cout << "Please load the data first." << std::endl;
                continue;
            }

            CourseGraph graph;
            std::string errorMessage;

            if (!buildCourseGraph(courseTable, graph, errorMessage)) {
                std::cout << "Error: " << errorMessage << std::endl;
                continue;
            }

            SortResult result = generateCourseOrder(graph);
            if (result.cycleDetected) {
                std::cout << "Error: a circular prerequisite relationship "
                             "was detected."
                          << std::endl;
                continue;
            }

            printCourseOrder(courseTable, result.courseOrder);
        } else if (choice == 3) {
            if (!dataLoaded) {
                std::cout << "Please load the data first." << std::endl;
                continue;
            }

            std::string courseNumber;
            std::cout << "What course do you want to know about? ";
            std::getline(std::cin, courseNumber);
            printCourse(courseTable, courseNumber);
        } else if (choice == 9) {
            std::cout << "Thank you for using the course planner!" << std::endl;
        } else {
            std::cout << choice << " is not a valid option." << std::endl;
        }
    }

    return 0;
}
