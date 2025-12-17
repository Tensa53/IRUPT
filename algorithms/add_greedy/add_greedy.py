import json
import os
import sys
import time


class AdditionalGreedy:

    test_tool = ""
    sir_programs_tests_number = {}

    def __init__(self, test_tool, sir_programs_tests_number):
        self.test_tool = test_tool
        self.sir_programs_tests_number = sir_programs_tests_number

    sir_programs = ["MavenProjectJ4_pre-fix", "MavenProjectJ4_post-fix", "MavenProjectJ5_pre-fix", "MavenProjectJ5_post-fix"]

    sir_programs_rep_values = {"MavenProjectJ4_pre-fix": 1, "MavenProjectJ4_post-fix": 1, "MavenProjectJ5_pre-fix": 1, "MavenProjectJ5_post-fix": 1}
    executed_lines_test_by_test = dict()
    faults_dictionary = dict()
    test_coverage_line_by_line = dict()
    test_cases_costs = dict()
    total_program_lines = dict()

    def json_keys_to_int(self, d):
        """This method correctly converts the data"""
        if isinstance(d, dict):
            return {int(k) if k.isdigit() else k: self.json_keys_to_int(v) for k, v in d.items()}
        elif isinstance(d, list):
            return [self.json_keys_to_int(i) for i in d]
        else:
            return d

    def load_files_content(self):
        with open(f"../../data_example/merged/{self.test_tool}/test_coverage_line_by_line_all_programs.json", "r") as file:
            # dictionary that, for each sir program, associates at each TEST of that program the LIST of LINES COVERED by it
            self.test_coverage_line_by_line = self.json_keys_to_int(json.load(file))  # {program1:{tc1:[linei,linej,...,linek],tc2:...}
        with open(f"../../data_example/merged/{self.test_tool}/test_cases_costs_all_programs.json", "r") as file:
            # dictionary that, for each sir program, associates at each TEST its EXECUTION COST
            self.test_cases_costs = self.json_keys_to_int(json.load(file))  # {program1:{tc1:ex_cost1,tc2:ex_cost2,...,tcn:ex_costn},program2:...}

    def lines_to_cover_func(self, sir_program):
        """This function is needed to know which are the lines that have to be covered."""
        lines_to_cover = set()

        for covered_lines in  self.test_coverage_line_by_line[sir_program].values():
            for covered_line in covered_lines:
                lines_to_cover.add(covered_line)

        return lines_to_cover

    def additional_greedy(self, sir_program):
        """This functions implements the Additional Greedy algorithm. It incrementally builds a set of non dominated solutions, choosing each time the test case that minimizes the function described earlier."""
        c = set()
        p = self.lines_to_cover_func(sir_program)
        s = []
        pareto_front = []
        test_cases_already_selected = []
        while len(c) < len(p):
            fitness_func = {}
            for test_case in range(0, self.sir_programs_tests_number[sir_program]):
                if test_case not in test_cases_already_selected and test_case in self.test_coverage_line_by_line[sir_program].keys():
                    max_cost = max(self.test_cases_costs[sir_program].values())
                    added_coverage = [line for line in self.test_coverage_line_by_line[sir_program][test_case] if line not in c]
                    try:
                        # FITNESS FORMULATION: such as the notebook algorithm, but without weights and faults  ++++
                        # FIRST OPERAND: percentage of added coverage by the current testcase
                        # DIVIDE PER
                        # SECOND OPERAND: percentage of the added execution cost by the current test case
                        op1 = len(added_coverage) / len(self.test_coverage_line_by_line[sir_program][test_case])
                        op2 = self.test_cases_costs[sir_program][test_case] / max_cost
                        fitness_val = op1 / op2
                        fitness_func[test_case] = fitness_val
                    except Exception as e:
                        print("Exception: " + str(e) + " op1="+str(op1) +" op2="+str(op2))
            # take the test case that minimizes the function
            key = fitness_func.get
            best_test_case = min(fitness_func, key=fitness_func.get)
            test_cases_already_selected.append(best_test_case)
            for covered_line in self.test_coverage_line_by_line[sir_program][best_test_case]:
                # update the count of covered lines
                c.add(covered_line)
            s.append(best_test_case)
            # incremental building of the pareto frontier
            pareto_front.append(s.copy())

        return pareto_front

    def run_algo(self):
        for sir_program in self.sir_programs:
            print("Executing Additional Greedy Algorithm for: " + sir_program)
            if not os.path.exists(f"../../results/add-greedy/{self.test_tool}/"):
                os.makedirs(f"../../results/add-greedy/{self.test_tool}/")
            with open(f"../../results/add-greedy/{self.test_tool}/{sir_program}_data.json", "w") as file:
                json_data = {}
                start = time.time()
                json_data["pareto_front"] = self.additional_greedy(sir_program)
                end = time.time()
                json_data["resolution_time(ms)"] = (end - start) * 1000
                json.dump(json_data, file)


def main():
    testTool = sys.argv[1]
    if testTool == "junit":
        # junit tests
        sir_programs_tests_number = {"MavenProjectJ4_pre-fix": 30, "MavenProjectJ4_post-fix": 30, "MavenProjectJ5_pre-fix": 30, "MavenProjectJ5_post-fix": 30}
    elif testTool == "jmh":
        # jmh benchs
        sir_programs_tests_number = {"MavenProjectJ4_pre-fix": 52, "MavenProjectJ4_post-fix": 52, "MavenProjectJ5_pre-fix": 52, "MavenProjectJ5_post-fix": 52}
    additionalgreedy = AdditionalGreedy(testTool, sir_programs_tests_number)
    additionalgreedy.load_files_content()
    additionalgreedy.run_algo()

if __name__ == "__main__":
    main()