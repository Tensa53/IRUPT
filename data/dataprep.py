import csv
import json
import os


class DataPrep:
    programs = ["MavenProjectJ4","MavenProjectJ5","MavenProjectJ6","MavenProjectJ7"]
    
    def __init__(self, rawDataInitialPath, processedDataInitialPath, mergedDataInitialPath):
        self.rawDataInitialPath = rawDataInitialPath
        self.processedDataInitialPath = processedDataInitialPath
        self.mergedDataInitialPath = mergedDataInitialPath
        self.program = ""

    def pretty_line_print(self, file_path, dictionary):
        with open(file_path, "w") as text_file:
            text_file.write("{\n")
            items = list(dictionary.items())
            for i, (key, value) in enumerate(items):
                json_pair = json.dumps(str(key)) + ":" + json.dumps(value)
                if i < len(items) - 1:
                    text_file.write("  " + json_pair + ",\n")
                else:
                    text_file.write("  " + json_pair + "\n")
            text_file.write("}")

    def coverage_matrix_split_covered_lines_in_multiple_list_items(self):
        coverage_matrix = json.load(open(f"{self.rawDataInitialPath}{self.program}/coverage-matrix.json"))

        for coverage_matrix_key in coverage_matrix.keys():
            coverage_matrix_row = coverage_matrix[coverage_matrix_key]
            coverage_matrix[coverage_matrix_key] = []
            for coverage_matrix_row_element in coverage_matrix_row:
                firstbracket = coverage_matrix_row_element.find("{")
                secondbracket = coverage_matrix_row_element.find("}")
                methodSig = coverage_matrix_row_element[:firstbracket]
                methodLines = coverage_matrix_row_element[firstbracket + 1: secondbracket]
                methodLinesList = methodLines.split(";")
                methodLinesList.pop()
                for methodLine in methodLinesList:
                    newMethod = methodSig + "{" + methodLine + ";}"
                    coverage_matrix[coverage_matrix_key].append(newMethod)

        self.pretty_line_print(f"{self.processedDataInitialPath}{self.program}/test_coverage_line_by_line_str.json", coverage_matrix)

    def coverage_matrix_splitted_reverse(self):
        coverage_matrix_split = json.load(open(f"{self.processedDataInitialPath}{self.program}/test_coverage_line_by_line_str.json"))

        methodLinesAllList = list()
        for testkey in coverage_matrix_split.keys():
            for coveredMethodLine in coverage_matrix_split[testkey]:
                methodLinesAllList.append(coveredMethodLine)

        methodLinesAllDictUnique = dict.fromkeys(methodLinesAllList)

        for methodLinesAllDictUniqueKey in methodLinesAllDictUnique:
            methodLinesAllDictUnique[methodLinesAllDictUniqueKey] = list()

        for testkey in coverage_matrix_split.keys():
            for methodLinesAllDictUniqueKey in methodLinesAllDictUnique:
                if methodLinesAllDictUniqueKey in coverage_matrix_split[testkey]:
                    methodLinesAllDictUnique[methodLinesAllDictUniqueKey].append(testkey)

        self.pretty_line_print(f"{self.processedDataInitialPath}{self.program}/executed_lines_test_by_test.json", methodLinesAllDictUnique)

    def map_testcases_to_number(self):
        tests = json.load(open(f"{self.rawDataInitialPath}{self.program}/report.json"))

        test_number = dict()
        i = 0

        for test in tests.keys():
            test_number[test] = i
            i += 1

        self.pretty_line_print(f"{self.processedDataInitialPath}{self.program}/testcases-number.json", test_number)

    def map_lines_to_hash(self):
        pass

    def map_coverage_matrix_split_keys_to_testcase_number(self):
        coverage_matrix = json.load(open(f"{self.processedDataInitialPath}{self.program}/test_coverage_line_by_line_str.json"))

        testcase_to_number = json.load(open(f"{self.processedDataInitialPath}{self.program}/testcases-number.json"))

        coverage_matrix_numbers = dict()

        for test in coverage_matrix.keys():
            number = testcase_to_number[test]
            coverage_matrix_numbers[number] = coverage_matrix[test]

        self.pretty_line_print(f"{self.processedDataInitialPath}{self.program}/test_coverage_line_by_line.json", coverage_matrix_numbers)

    def map_time_report_to_testcase_number(self):
        times = json.load(open(f"{self.rawDataInitialPath}{self.program}/report.json"))
        testcase_to_number = json.load(open(f"{self.processedDataInitialPath}{self.program}/testcases-number.json"))

        test_times_to_number = dict()

        for testcase in times.keys():
            number = testcase_to_number[testcase]
            test_times_to_number[number] = float(times[testcase])

        self.pretty_line_print(f"{self.processedDataInitialPath}{self.program}/test_cases_costs.json", test_times_to_number)

    def plain_from_coverage_matrix_splitted(self):
        coverage_matrix = json.load(open(f"{self.processedDataInitialPath}{self.program}/test_coverage_line_by_line_str.json"))

        with open(f"{self.processedDataInitialPath}{self.program}/{self.program}_coverage.txt", "w") as text_file:
            for coverage_matrix_key in coverage_matrix.keys():
                coverage_matrix_row = coverage_matrix[coverage_matrix_key]

                lineString = ""
                for coverage_matrix_row_element in coverage_matrix_row:
                    lineString += coverage_matrix_row_element + ","
                lineString = lineString[:-1]
                text_file.write(lineString + "\n")

    def plain_from_time_report(self):
        report = json.load(open(f"{self.rawDataInitialPath}{self.program}/report.json"))

        with open(f"{self.processedDataInitialPath}{self.program}/{self.program}_costs.txt", "w") as text_file:
            times = list(report.values())
            timeString = ""
            for time in times:
                timeD = float(time)
                timeString += str(timeD) + ","
            timeString = timeString[:-1]
            text_file.write(timeString)

    def csv_from_coverage_matrix_splitted_and_time_report(self):
        test_time_report = json.load(open(f"{self.rawDataInitialPath}{self.program}/report.json"))
        test_coverage_report = json.load(open(f"{self.processedDataInitialPath}{self.program}/test_coverage_line_by_line_str.json"))

        line_covered = test_coverage_report.values()
        line_covered_all = list()
        for line in line_covered:
            for li in line:
                line_covered_all.append(li)
        line_covered_nodup = list(dict.fromkeys(line_covered_all))
        suiteCov = len(line_covered_nodup)

        with open(f"{self.processedDataInitialPath}{self.program}/{self.program}.csv", "w", newline="", encoding="utf-8") as f:
            writer = csv.writer(f)
            writer.writerow(["", "time", "rate"])
            for testcaseName in test_time_report:
                testTime = test_time_report[testcaseName]
                testCov = test_coverage_report[testcaseName]
                length = len(testCov)
                covP = length / suiteCov
                writer.writerow([testcaseName, testTime, covP])

    def merge(self):
        executed_lines_test_by_test_all_programs=dict()
        test_coverage_line_by_line_all_programs = dict()
        test_cases_costs_all_programs = dict()
        total_program_lines_all_programs = dict()

        for program in self.programs:
            executed_lines_test_by_test = json.load(open(f"{self.processedDataInitialPath}{self.program}/executed_lines_test_by_test.json"))
            test_coverage_line_by_line = json.load(open(f"{self.processedDataInitialPath}{self.program}/test_coverage_line_by_line.json"))
            test_cases_costs = json.load(open(f"{self.processedDataInitialPath}{self.program}/test_cases_costs.json"))
            total_program_lines = json.load(open(f"{self.rawDataInitialPath}{self.program}/total-lines.json"))
            executed_lines_test_by_test_all_programs[program] = executed_lines_test_by_test.copy()
            test_coverage_line_by_line_all_programs[program] = test_coverage_line_by_line.copy()
            test_cases_costs_all_programs[program] = test_cases_costs.copy()
            total_program_lines_all_programs[program] = total_program_lines["Java"]["code"]

        json.dump(executed_lines_test_by_test_all_programs, open(f"{self.mergedDataInitialPath}executed_lines_test_by_test_all_programs.json", "w"))
        json.dump(test_coverage_line_by_line_all_programs, open(f"{self.mergedDataInitialPath}test_coverage_line_by_line_all_programs.json", "w"))
        json.dump(test_cases_costs_all_programs, open(f"{self.mergedDataInitialPath}test_cases_costs_all_programs.json", "w"))
        json.dump(total_program_lines_all_programs, open(f"{self.mergedDataInitialPath}total_program_lines_all_programs.json", "w"))


def main():
    if not os.path.exists("processed") and not os.path.exists("merged"):
        os.mkdir("processed")
        os.mkdir("merged")
    dataprep = DataPrep("raw/","processed/", "merged/")

    for program in dataprep.programs:
        print("Processing the data for: " + program)
        dataprep.program = program
        if(not os.path.exists(f"{dataprep.processedDataInitialPath}{program}")):
            os.mkdir(f"{dataprep.processedDataInitialPath}{program}")
        # methods that create input json files for Add-Greedy and first two files for Select-QAOA
        dataprep.coverage_matrix_split_covered_lines_in_multiple_list_items()
        dataprep.coverage_matrix_splitted_reverse()
        # methods that create remaining input json files for Select-QAOA
        dataprep.map_testcases_to_number()
        dataprep.map_coverage_matrix_split_keys_to_testcase_number()
        dataprep.map_time_report_to_testcase_number()
        # methods that create input text files for DIV-GA
        dataprep.plain_from_coverage_matrix_splitted()
        dataprep.plain_from_time_report()
        # methods that create input csv file for IGDec-QAOA
        dataprep.csv_from_coverage_matrix_splitted_and_time_report()

    # method that merge all the json files program per program, for Select-QAOA and Add-Greedy
    dataprep.merge()


if __name__ == "__main__":
    main()
