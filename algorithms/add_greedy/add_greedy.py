import json
import time


class AdditionalGreedy:
    sir_programs = ["MavenProjectJ4", "MavenProjectJ5", "MavenProjectJ6", "MavenProjectJ7"]
    sir_programs_tests_number = {"MavenProjectJ4": 52, "MavenProjectJ5": 52, "MavenProjectJ6": 52, "MavenProjectJ7": 52}
    sir_programs_rep_values = {"MavenProjectJ4": 1, "MavenProjectJ5": 1, "MavenProjectJ6": 1, "MavenProjectJ7": 1}
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
        with open("../../data/merged/test_coverage_line_by_line_all_programs.json", "r") as file:
            # dictionary that, for each sir program, associates at each TEST of that program the LIST of LINES COVERED by it
            self.test_coverage_line_by_line = self.json_keys_to_int(json.load(file))  # {program1:{tc1:[linei,linej,...,linek],tc2:...}
        with open("../../data/merged/test_cases_costs_all_programs.json", "r") as file:
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
                        # FIRST FORMULATION: such as the notebook algorithm, but without weights and faults  ++++
                        # FIRST OPERAND: percentage of added coverage by the current testcase
                        # DIVIDE PER
                        # SECOND OPERAND: percentage of the added execution cost by the current test case
                        fitness_func[test_case] = ((len(added_coverage) / len(self.test_coverage_line_by_line[sir_program][test_case]))
                                                   /
                                                   (self.test_cases_costs[sir_program][test_case] / max_cost))
                        # SECOND FORMULATION: such as the algorithm 6 from the DIV-GA paper ----
                        # FIRST OPERAND: added coverage
                        # DIVIDE PER
                        # SECOND OPERAND: execution cost of the current test case
                        #fitness_func[test_case] = (len(added_coverage) / self.test_cases_costs[sir_program][test_case])
                    except:
                        pass
            # take the test case that minimizes the function
            # print(fitness_func)
            # key = fitness_func.get
            # print(key)
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
            with open(f"../../results/add-greedy/{sir_program}_data.json", "w") as file:
                json_data = {}
                start = time.time()
                json_data["pareto_front"] = self.additional_greedy(sir_program)
                end = time.time()
                json_data["resolution_time(ms)"] = (end - start) * 1000
                json.dump(json_data, file)


def main():
    additionalgreedy = AdditionalGreedy()
    additionalgreedy.load_files_content()
    additionalgreedy.run_algo()

if __name__ == "__main__":
    main()