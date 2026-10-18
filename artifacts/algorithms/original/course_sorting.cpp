#include <algorithm>        // for sort
#include <cctype>           // for toupper
#include <fstream>          // for file input
#include <iostream>         // for input/output
#include <sstream>          // for stringstream
#include <string>
#include <unordered_map>    // hash table 
#include <vector>

using namespace std;

// structure to hold course information
struct Course {
    string courseNumber;            // course id 
    string courseName;              // course title
    vector<string> prerequisites;   // list of prerequisite course numbers
};

// removes leading and trailing whitespace from a string
string trim(const string& str) {
    size_t first = str.find_first_not_of(" \t\r\n");
    size_t last  = str.find_last_not_of(" \t\r\n");
    if (first == string::npos || last == string::npos) {
        return "";
    }
    return str.substr(first, last - first + 1);
}

// converts a string to uppercase 
string toUpper(const string& str) {
    string result = str;
    for (char& c : result) {
        c = static_cast<char>(toupper(c));
    }
    return result;
}

// splits a csv line into fields using commas
vector<string> splitCSV(const string& line) {
    vector<string> fields;
    string value;
    stringstream ss(line);

    while (getline(ss, value, ',')) {
        fields.push_back(trim(value));
    }
    return fields;
}

// loads course data from file into a hash table
bool loadCourses(const string& filename,
                 unordered_map<string, Course>& courseTable) {

    courseTable.clear();  // clear existing data

    ifstream file(filename);
    if (!file.is_open()) {
        return false;     // file could not be opened
    }

    string line;

    // read each line from the input file
    while (getline(file, line)) {
        if (line.empty()) {
            continue;    // skip blank lines
        }

        vector<string> fields = splitCSV(line);

        // must have at least course number and name
        if (fields.size() < 2) {
            cout << "Error: invalid line format" << endl;
            continue;
        }

        Course course;
        course.courseNumber = toUpper(fields[0]);
        course.courseName   = fields[1];

        // remaining fields are prerequisites
        for (size_t i = 2; i < fields.size(); i++) {
            course.prerequisites.push_back(toUpper(fields[i]));
        }

        // insert course into hash table using course number as key
        courseTable[course.courseNumber] = course;
    }

    file.close();
    return true;
}

// prints the menu options
void printMenu() {
    cout << endl;
    cout << "1. Load Data Structure." << endl;
    cout << "2. Print Course List." << endl;
    cout << "3. Print Course." << endl;
    cout << "9. Exit" << endl;
}

// prints all courses in alphanumeric order
void printCourseList(const unordered_map<string, Course>& courseTable) {

    vector<string> courseNumbers;

    // copy course numbers from hash table into vector
    for (const auto& pair : courseTable) {
        courseNumbers.push_back(pair.first);
    }

    // sort course numbers alphabetically
    sort(courseNumbers.begin(), courseNumbers.end());

    cout << "Here is a sample schedule:" << endl;

    // print sorted list
    for (const string& number : courseNumbers) {
        const Course& course = courseTable.at(number);
        cout << course.courseNumber << ", " << course.courseName << endl;
    }
}

// searches for and prints a single course with prerequisites and titles
void printCourse(const unordered_map<string, Course>& courseTable,
                 const string& courseNumber) {

    string key = toUpper(trim(courseNumber));

    // attempt to find course in hash table
    auto it = courseTable.find(key);
    if (it == courseTable.end()) {
        cout << "Course not found." << endl;
        return;
    }

    const Course& course = it->second;

    // print course information
    cout << course.courseNumber << ", " << course.courseName << endl;

    if (course.prerequisites.empty()) {
        cout << "Prerequisites: None" << endl;
        return;
    }

    cout << "Prerequisites: ";

    // print each prerequisite with its title if available
    for (size_t i = 0; i < course.prerequisites.size(); i++) {
        string preNum = course.prerequisites[i];

        auto preIt = courseTable.find(preNum);
        if (preIt != courseTable.end()) {
            cout << preIt->second.courseNumber
                 << " (" << preIt->second.courseName << ")";
        } else {
            cout << preNum;
        }

        if (i < course.prerequisites.size() - 1) {
            cout << ", ";
        }
    }

    cout << endl;
}

int main() {
    unordered_map<string, Course> courseTable;
    bool dataLoaded = false;

    cout << "Welcome to the course planner." << endl;

    int choice = 0;

    // main menu loop
    while (choice != 9) {
        printMenu();
        cout << "What would you like to do? ";
        cin >> choice;
        cin.ignore();   // clear newline from input buffer

        if (choice == 1) {
            string filename;
            cout << "Enter the file name: ";
            getline(cin, filename);

            if (loadCourses(filename, courseTable)) {
                dataLoaded = true;
                cout << "Data loaded successfully." << endl;
            } else {
                cout << "Error: file could not be opened." << endl;
            }
        }
        else if (choice == 2) {
            if (!dataLoaded) {
                cout << "Please load the data first." << endl;
            } else {
                printCourseList(courseTable);
            }
        }
        else if (choice == 3) {
            if (!dataLoaded) {
                cout << "Please load the data first." << endl;
            } else {
                string courseNumber;
                cout << "What course do you want to know about? ";
                getline(cin, courseNumber);
                printCourse(courseTable, courseNumber);
            }
        }
        else if (choice == 9) {
            cout << "Thank you for using the course planner!" << endl;
        }
        else {
            cout << choice << " is not a valid option." << endl;
        }
    }

    return 0;
}