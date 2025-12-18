import csv
import json
import os
import sys

from bs4 import BeautifulSoup
import warnings
warnings.filterwarnings("ignore", category=DeprecationWarning)

class DataPrep:
    def __init__(self):
        self.rawDataInitialPath = ""
        self.processedDataInitialPath = ""
        self.mergedDataInitialPath = ""
        self.programs = None
        self.program = ""
        self.testTool = ""

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

    def create_coverage_matrix(self):
        classes = os.listdir(f"{self.rawDataInitialPath}jacoco-{self.testTool}-xml/")
        coverage_data = dict()
        coverage_matrix = dict()

        for clas in classes:
            methods = os.listdir(f"{self.rawDataInitialPath}jacoco-{self.testTool}-xml/" + str(clas))
            for method in methods:
                with open(f"{self.rawDataInitialPath}jacoco-{self.testTool}-xml/" + str(clas) + "/" + str(method) + "/jacoco.xml",
                          "r") as f:
                    data = f.read()
                    if self.testTool == "junit":
                        ind = method.find("#")
                        if ind != -1:
                            method = method[0:ind] + "[" + method[ind + 1:] + "]"
                            method = method.replace("-", "=")
                            method = method.replace("#", ", ")
                        fulllMethodName = clas + "." + method
                        coverage_data[fulllMethodName] = data
                    else:
                        fulllMethodName = clas + "." + method
                        coverage_data[fulllMethodName] = data

        for testMethodData in coverage_data:
            bs_data = BeautifulSoup(coverage_data[testMethodData], "xml")
            coverage_matrix[testMethodData] = list()
            sourcefiles = bs_data.findAll("sourcefile")
            classes = bs_data.findAll("class")
            for sourcefile in sourcefiles:
                package = ""
                sourcefilename = sourcefile.get("name")
                for clas in classes:
                    if clas.get("sourcefilename") == sourcefilename:
                        clasname = clas.get("name")
                        package = clasname[0:clasname.rfind("/")].replace("/", ".")
                fullClassName = package + "." + sourcefilename[0:sourcefilename.rfind(".")]
                lines = sourcefile.findAll("line")
                for line in lines:
                    if line.get("mi") == "0":
                        classLine = fullClassName + "{" + line.get("nr") + ";}"
                        coverage_matrix[testMethodData].append(classLine)

        coverage_matrix_sorted = dict(sorted(coverage_matrix.items()))

        self.pretty_line_print(f"{self.processedDataInitialPath}coverage_matrix.json", coverage_matrix_sorted)

    def create_time_matrix(self):
        if self.testTool == "junit":
            self.create_time_matrix_junit()
        elif self.testTool == "jmh":
            self.create_time_matrix_jmh()

    def create_time_matrix_junit(self):
        baseDir = f"{self.rawDataInitialPath}surefire-reports/"

        files = os.listdir(baseDir)

        files_data = list()

        for file in files:
            with open(f"{baseDir}/{file}", "r") as f:
                files_data.append(f.read())

        time_matrix = dict()

        for file_data in files_data:
            bs_data = BeautifulSoup(file_data, "xml")
            className = bs_data.find("testsuite").get("name")
            tests = bs_data.findAll("testcase")
            for test in tests:
                if not test.findAll("skipped"):
                    testFullName = className + "." + test.get("name")
                    time = test.get("time")
                    if time == "0" or time == "0.0" or time == "0.00" or time == "0.000":
                        time = "3600.0"
                    time_matrix[testFullName] = time

        min_time = min(time_matrix.values())

        for testFullName in time_matrix:
            if time_matrix[testFullName] == "3600.0":
                time_matrix[testFullName] = min_time

        time_matrix_sorted = dict(sorted(time_matrix.items()))

        self.pretty_line_print(f"{self.processedDataInitialPath}time_matrix.json", time_matrix_sorted)

    def create_time_matrix_jmh(self):
        baseDir = f"{self.rawDataInitialPath}time-reports/"

        classes = os.listdir(baseDir)

        time_matrix = dict()

        for clas in classes:
            try:
                reports = os.listdir(baseDir+clas)
            except NotADirectoryError:
                print("Not a directory")
                continue
            for report in reports:
                path = f"{baseDir}{clas}/{report}"
                files = json.load(open(path, "r"))
                if len(files) == 1:
                    file = files[0]
                    benchmark = file.get("benchmark")
                    score = str(file.get("primaryMetric")["score"])
                    time_matrix[benchmark] = score
                else:
                    for file in files:
                        benchmark = file.get("benchmark")
                        params = file.get("params")
                        score = str(file.get("primaryMetric")["score"])
                        parameters = "#"
                        if params:
                            for param in params:
                                parameters += param + "=" + params[param] + "_"
                            benchmarkFull = benchmark+parameters[:-1]
                        else:
                            benchmarkFull = benchmark
                        time_matrix[benchmarkFull] = score

        time_matrix_sorted = dict(sorted(time_matrix.items()))

        self.pretty_line_print(f"{self.processedDataInitialPath}time_matrix.json", time_matrix_sorted)

    def coverage_matrix_reverse(self):
        coverage_matrix = json.load(open(f"{self.processedDataInitialPath}coverage_matrix.json"))

        methodLinesAllList = list()
        for testkey in coverage_matrix.keys():
            for coveredMethodLine in coverage_matrix[testkey]:
                methodLinesAllList.append(coveredMethodLine)

        methodLinesAllDictUnique = dict.fromkeys(methodLinesAllList)

        for methodLinesAllDictUniqueKey in methodLinesAllDictUnique:
            methodLinesAllDictUnique[methodLinesAllDictUniqueKey] = list()

        for testkey in coverage_matrix.keys():
            for methodLinesAllDictUniqueKey in methodLinesAllDictUnique:
                if methodLinesAllDictUniqueKey in coverage_matrix[testkey]:
                    methodLinesAllDictUnique[methodLinesAllDictUniqueKey].append(testkey)

        self.pretty_line_print(f"{self.processedDataInitialPath}coverage_matrix_reversed.json", methodLinesAllDictUnique)

    def map_testcases_to_number(self):
        tests = json.load(open(f"{self.processedDataInitialPath}time_matrix.json"))

        test_number = dict()
        i = 0

        for test in tests.keys():
            test_number[test] = i
            i += 1

        self.pretty_line_print(f"{self.processedDataInitialPath}testcases_number.json", test_number)

    def maps_classlines_to_number(self):
        coverage_matrix = json.load(open(f"{self.processedDataInitialPath}coverage_matrix.json"))

        lines_set = set()

        for test in coverage_matrix.keys():
            lines = coverage_matrix[test]
            for line in lines:
                lines_set.add(line)

        lines_set_ordered = sorted(list(lines_set))

        lines_dict = dict()

        i = 1
        for line in lines_set_ordered:
            lines_dict[line] = i
            i += 1

        self.pretty_line_print(f"{self.processedDataInitialPath}classlines_number.json", lines_dict)

    def map_coverage_matrix_keys_to_testcase_number(self):
        coverage_matrix = json.load(open(f"{self.processedDataInitialPath}coverage_matrix.json"))
        testcase_to_number = json.load(open(f"{self.processedDataInitialPath}testcases_number.json"))

        coverage_matrix_numbers = dict()

        for test in coverage_matrix.keys():
            number = testcase_to_number[test]
            coverage_matrix_numbers[number] = coverage_matrix[test]

        self.pretty_line_print(f"{self.processedDataInitialPath}test_coverage_line_by_line_str.json", coverage_matrix_numbers)

    def map_coverage_matrix_element_entries_to_classline_number(self):
        coverage_matrix = json.load(open(f"{self.processedDataInitialPath}test_coverage_line_by_line_str.json"))
        classline_to_number = json.load(open(f"{self.processedDataInitialPath}classlines_number.json"))

        coverage_matrix_element_entries = dict()

        for test in coverage_matrix.keys():
            classlines_entry = coverage_matrix[test]
            classline_to_number_list = list()
            for line in classlines_entry:
                num = classline_to_number[line]
                classline_to_number_list.append(num)
            coverage_matrix_element_entries[test] = classline_to_number_list

        self.pretty_line_print(f"{self.processedDataInitialPath}test_coverage_line_by_line.json", coverage_matrix_element_entries)

    def map_coverage_matrix_reverse_keys_to_classline_number(self):
        coverage_matrix_reverse = json.load(open(f"{self.processedDataInitialPath}coverage_matrix_reversed.json"))
        classline_to_number = json.load(open(f"{self.processedDataInitialPath}classlines_number.json"))

        coverage_matrix_reverse_numbers = dict()

        for line in coverage_matrix_reverse.keys():
            number = classline_to_number[line]
            coverage_matrix_reverse_numbers[number] = coverage_matrix_reverse[line]

        self.pretty_line_print(f"{self.processedDataInitialPath}executed_lines_test_by_test_str.json", coverage_matrix_reverse_numbers)

    def map_coverage_matrix_reverse_element_entries_to_testcase_number(self):
        coverage_matrix_reverse = json.load(open(f"{self.processedDataInitialPath}executed_lines_test_by_test_str.json"))
        testcase_to_number = json.load(open(f"{self.processedDataInitialPath}testcases_number.json"))

        coverage_matrix_element_entries = dict()

        for line in coverage_matrix_reverse.keys():
            test_entry = coverage_matrix_reverse[line]
            test_to_number_list = list()
            for test in test_entry:
                num = testcase_to_number[test]
                test_to_number_list.append(num)
            coverage_matrix_element_entries[line] = test_to_number_list

        self.pretty_line_print(f"{self.processedDataInitialPath}executed_lines_test_by_test.json", coverage_matrix_element_entries)

    def map_time_matrix_keys_to_testcase_number(self):
        times = json.load(open(f"{self.processedDataInitialPath}time_matrix.json"))
        testcase_to_number = json.load(open(f"{self.processedDataInitialPath}testcases_number.json"))

        test_times_to_number = dict()

        for testcase in times.keys():
            number = testcase_to_number[testcase]
            test_times_to_number[number] = float(times[testcase])

        self.pretty_line_print(f"{self.processedDataInitialPath}test_cases_costs.json", test_times_to_number)

    def plain_from_coverage_matrix(self):
        coverage_matrix = json.load(open(f"{self.processedDataInitialPath}test_coverage_line_by_line.json"))

        prog_name = self.program[0:self.program.find("_")]
        if prog_name.count("-") > 0:
            prog_name = prog_name[0:prog_name.find("-")]

        fix_status = self.program[self.program.find("_p")+1:self.program.rfind("-")]

        file_name = prog_name + self.testTool + fix_status + "_coverage.txt"

        print(file_name)

        with open(f"{self.processedDataInitialPath}{file_name}", "w") as text_file:
            for coverage_matrix_key in coverage_matrix.keys():
                coverage_matrix_row = coverage_matrix[coverage_matrix_key]

                lineString = ""
                for coverage_matrix_row_element in coverage_matrix_row:
                    lineString += str(coverage_matrix_row_element) + ","
                lineString = lineString[:-1]
                text_file.write(lineString + "\n")

    def plain_from_time_matrix(self):
        time_matrix = json.load(open(f"{self.processedDataInitialPath}time_matrix.json"))

        prog_name = self.program[0:self.program.find("_")]
        if prog_name.count("-") > 0:
            prog_name = prog_name[0:prog_name.find("-")]

        fix_status = self.program[self.program.find("_p")+1:self.program.rfind("-")]

        file_name = prog_name + self.testTool + fix_status + "_cost.txt"

        print(file_name)

        with open(f"{self.processedDataInitialPath}{file_name}", "w") as text_file:
            times = list(time_matrix.values())
            timeString = ""
            for time in times:
                timeD = float(time)
                timeString += str(timeD) + ","
            timeString = timeString[:-1]
            text_file.write(timeString)

    def csv_from_coverage_matrix_and_time_matrix(self):
        test_time_report = json.load(open(f"{self.processedDataInitialPath}time_matrix.json"))
        test_coverage_report = json.load(open(f"{self.processedDataInitialPath}coverage_matrix.json"))

        line_covered = test_coverage_report.values()
        line_covered_all = list()
        for line in line_covered:
            for li in line:
                line_covered_all.append(li)
        line_covered_nodup = list(dict.fromkeys(line_covered_all))
        suiteCov = len(line_covered_nodup)

        with open(f"{self.processedDataInitialPath}{self.program}.csv", "w", newline="", encoding="utf-8") as f:
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
            self.rawDataInitialPath = f"raw/{program}/{self.testTool}/"
            self.processedDataInitialPath = f"processed/{program}/{self.testTool}/"
            executed_lines_test_by_test = json.load(open(f"{self.processedDataInitialPath}executed_lines_test_by_test.json"))
            test_coverage_line_by_line = json.load(open(f"{self.processedDataInitialPath}test_coverage_line_by_line.json"))
            test_cases_costs = json.load(open(f"{self.processedDataInitialPath}test_cases_costs.json"))
            total_program_lines = json.load(open(f"{self.rawDataInitialPath}total_lines.json"))
            executed_lines_test_by_test_all_programs[program] = executed_lines_test_by_test.copy()
            test_coverage_line_by_line_all_programs[program] = test_coverage_line_by_line.copy()
            test_cases_costs_all_programs[program] = test_cases_costs.copy()
            total_program_lines_all_programs[program] = total_program_lines["Java"]["code"]

        self.pretty_line_print(f"{self.mergedDataInitialPath}executed_lines_test_by_test_all_programs.json", executed_lines_test_by_test_all_programs)
        self.pretty_line_print(f"{self.mergedDataInitialPath}test_coverage_line_by_line_all_programs.json", test_coverage_line_by_line_all_programs)
        self.pretty_line_print(f"{self.mergedDataInitialPath}test_cases_costs_all_programs.json", test_cases_costs_all_programs)
        self.pretty_line_print(f"{self.mergedDataInitialPath}total_program_lines_all_programs.json", total_program_lines_all_programs)

    def search_covered_method_lines(self, method_lines_to_search):
        executed_lines_test_by_test = json.load(open(f"{self.processedDataInitialPath}coverage_matrix_reversed.json"))

        executed_lines_test_by_test_filtered= dict()

        for line in method_lines_to_search:
            if line in executed_lines_test_by_test.keys():
                filtered = executed_lines_test_by_test[line]
                executed_lines_test_by_test_filtered[line] = list(filtered)
            else:
                executed_lines_test_by_test_filtered[line] = list()
                executed_lines_test_by_test_filtered[line].append("no tests")

        self.pretty_line_print(f"{self.processedDataInitialPath}executed_lines_test_by_test_filtered.json", executed_lines_test_by_test_filtered)

    def filter_testcases_with_no_coverage(self):
        coverage_matrix = json.load(open(f"{self.processedDataInitialPath}coverage_matrix.json"))

        testcases_with_no_coverage = dict()

        for testcaseName in coverage_matrix:
            covered_lines = coverage_matrix[testcaseName]

            if len(covered_lines) == 0:
                testcases_with_no_coverage[testcaseName] = list()
                testcases_with_no_coverage[testcaseName].append("no coverage")

        if len(testcases_with_no_coverage) == 0:
            testcases_with_no_coverage["all_tests"] = list()
            testcases_with_no_coverage["all_tests"].append("with coverage")

        self.pretty_line_print(f"{self.processedDataInitialPath}testcases_with_no_coverage.json", testcases_with_no_coverage)

def main():
    method_lines_to_search_dict = {
        "hive-standalone-metastore-common": [ "org.apache.hadoop.hive.metastore.HiveMetaStoreClient{565;}",
                                              "org.apache.hadoop.hive.metastore.HiveMetaStoreClient{566;}",
                                              "org.apache.hadoop.hive.metastore.HiveMetaStoreClient{567;}",
                                              "org.apache.hadoop.hive.metastore.HiveMetaStoreClient{568;}",
                                              "org.apache.hadoop.hive.metastore.HiveMetaStoreClient{569;}",
                                              "org.apache.hadoop.hive.metastore.HiveMetaStoreClient{570;}",
                                              "org.apache.hadoop.hive.metastore.HiveMetaStoreClient{571;}",
                                              "org.apache.hadoop.hive.metastore.HiveMetaStoreClient{572;}",
                                              "org.apache.hadoop.hive.metastore.HiveMetaStoreClient{573;}",
                                              "org.apache.hadoop.hive.metastore.HiveMetaStoreClient{574;}",
                                              "org.apache.hadoop.hive.metastore.HiveMetaStoreClient{575;}",
                                              "org.apache.hadoop.hive.metastore.HiveMetaStoreClient{576;}",
                                              "org.apache.hadoop.hive.metastore.HiveMetaStoreClient{577;}",
                                              "org.apache.hadoop.hive.metastore.HiveMetaStoreClient{578;}",
                                              "org.apache.hadoop.hive.metastore.HiveMetaStoreClient{579;}",
                                              "org.apache.hadoop.hive.metastore.HiveMetaStoreClient{580;}",
                                              "org.apache.hadoop.hive.metastore.HiveMetaStoreClient{581;}",
                                              "org.apache.hadoop.hive.metastore.HiveMetaStoreClient{582;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{802;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{803;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{804;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{805;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{806;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{807;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{808;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{809;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{810;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{811;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{812;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{813;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{814;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{815;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{816;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{817;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{818;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{819;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{820;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{821;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{822;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{823;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{824;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{825;}",
                                              "org.apache.hadoop.hive.metastore.utils.MetaStoreUtils{825;}" ],
        "avro": [ "org.apache.avro.data.RecordBuilderBase{76;}",
                  "org.apache.avro.data.RecordBuilderBase{77;}",
                  "org.apache.avro.util.Utf8{64;}",
                  "org.apache.avro.util.Utf8{65;}",
                  "org.apache.avro.util.Utf8{116;}",
                  "org.apache.avro.util.Utf8{117;}",
                  "org.apache.avro.util.Utf8{118;}",],
        "MavenProjectJ4": [ "org.example.utente.Utente{50;}",
                            "org.example.utente.Utente{51;}",
                            "org.example.utente.Utente{60;}",
                            ],
        "MavenProjectJ5": ["org.example.utente.Utente{50;}",
                           "org.example.utente.Utente{51;}",
                           "org.example.utente.Utente{60;}",
                           ],
        "MavenProjectJ6": ["org.example.utente.Utente{50;}",
                           "org.example.utente.Utente{51;}",
                           "org.example.utente.Utente{60;}",
                           ],
        "MavenProjectJ7": ["org.example.utente.Utente{50;}",
                           "org.example.utente.Utente{51;}",
                           "org.example.utente.Utente{60;}",
                           ]
    }

    if not os.path.exists("processed") and not os.path.exists("merged"):
        os.mkdir("processed")
        os.mkdir("merged")

    try:
        arg = sys.argv[1]
        if arg:
            dataprep = DataPrep()
            dataprep.testTool = arg
            # dataprep.programs = ["MavenProjectJ4_pre-fix", "MavenProjectJ4_post-fix", "MavenProjectJ5_pre-fix", "MavenProjectJ5_post-fix"]
            dataprep.programs = ["avro_pre-fix", "avro_post-fix", "hive-standalone-metastore-common_pre-fix", "hive-standalone-metastore-common_post-fix"]
            for program in dataprep.programs:
                print(program[0:program.find("_")])
                dataprep.program = program
                print("Processing the data for: " + program)
                dataprep.program = program
                if(not os.path.exists(f"processed/{dataprep.program}")):
                    os.mkdir(f"processed/{dataprep.program}")
                    os.mkdir(f"processed/{dataprep.program}/junit")
                    os.mkdir(f"processed/{dataprep.program}/jmh")
                dataprep.rawDataInitialPath = f"raw/{dataprep.program}/{dataprep.testTool}/"
                dataprep.processedDataInitialPath = f"processed/{dataprep.program}/{dataprep.testTool}/"
                dataprep.mergedDataInitialPath= f"merged/{dataprep.testTool}/"
                # # methods that create coverage and time matrix from jacoco, junit, jmh reports
                # dataprep.create_coverage_matrix()
                # dataprep.create_time_matrix()
                # # # methods that create input json files for Add-Greedy and first two files for Select-QAOA
                # dataprep.coverage_matrix_reverse()
                # # methods that create remaining input json files for Select-QAOA
                # dataprep.map_testcases_to_number()
                # dataprep.maps_classlines_to_number()
                # dataprep.map_coverage_matrix_keys_to_testcase_number()
                # dataprep.map_coverage_matrix_element_entries_to_classline_number()
                # dataprep.map_coverage_matrix_reverse_keys_to_classline_number()
                # dataprep.map_coverage_matrix_reverse_element_entries_to_testcase_number()
                # dataprep.map_time_matrix_keys_to_testcase_number()
                # # methods that create input text files for DIV-GA
                dataprep.plain_from_coverage_matrix()
                dataprep.plain_from_time_matrix()
                # # methods that create input csv file for IGDec-QAOA
                dataprep.csv_from_coverage_matrix_and_time_matrix()
                # # # extra methods for insight
                # dataprep.search_covered_method_lines(method_lines_to_search_dict[program[0:program.find("_")]])
                # dataprep.filter_testcases_with_no_coverage()
            # method that merge all the json files program per program, for Select-QAOA and Add-Greedy
            # dataprep.merge()
    except IndexError:
        print("Please provide a valid test tool name as an argument: junit or jmh")
        exit(1)

if __name__ == "__main__":
    main()
