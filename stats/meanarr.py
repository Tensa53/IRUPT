import csv
import json
from statistics import mean

class MeanArrClass:

    # total lines (with performance issues) that the complete suite was able to cover
    suite_lines_covered = {
        "avro_pre-fix-ju" : [36, 37, 803, 804, 752, 753, 754],
        "avro_post-fix-ju" : [36, 800, 751],
        "avro_pre-fix-jmh" : [30, 31, 839, 840, 809, 810, 811],
        "avro_post-fix-jmh" : [30, 836, 808],
        "hive_pre-fix-ju" : [70, 71, 72, 73, 74, 75, 76, 77, 78, 1665, 1666, 1667, 1668, 1669, 1670, 1671, 1672, 1673,
                             1674, 1675, 1676, 1677, 1678, 1679, 1680, 1681, 1682],
        "hive_post-fix-ju" : [71, 72, 73, 74, 75, 76, 77, 78, 79, 1667, 1668, 1669, 1670, 1671, 1672, 1673,
                             1674, 1675, 1676, 1677, 1678, 1679, 1680, 1681, 1682, 1683, 1684, 1685],
        "hive_pre-fix-jmh" : [68, 69, 70, 71, 72, 73, 74, 75, 76, 1406, 1407, 1408, 1409, 1410, 1411, 1412, 1413, 1414,
                              1415, 1416, 1417, 1418, 1419, 1420, 1421, 1422, 1423],
        "hive_post-fix-jmh" : [69, 70, 71, 72, 73, 74, 75, 76, 77, 1408, 1409, 1410, 1411, 1412, 1413, 1414,
                              1415, 1416, 1417, 1418, 1419, 1420, 1421, 1422, 1423, 1424, 1425, 1426, 1427]
    }

    # total number of lines with performance issue that should have been covered
    total_lines_to_cover = {
        "avro_pre-fix-ju" : 7,
        "avro_post-fix-ju" : 3,
        "avro_pre-fix-jmh" : 7,
        "avro_post-fix-jmh" : 3,
        "hive_pre-fix-ju" : 31,
        "hive_post-fix-ju" : 38,
        "hive_pre-fix-jmh" : 31,
        "hive_post-fix-jmh" : 38
    }

    total_coverage_means = dict()
    total_costs_means = dict()

    def __prepare_pareto(self, pareto_front_json_path, algo):
        pareto_front_json = json.load(open(pareto_front_json_path))
        pareto_fronts_list = list()

        if algo == "add-greedy":
            pareto_front = pareto_front_json["pareto_front"]
            for i in range(10):
                pareto_fronts_list.append(pareto_front)

        if algo == "divga":
            i = 0
            for p in pareto_front_json:
                suffix = "pareto_front_" + str(i)
                if suffix in p:
                    # print(pareto_front_json[p])
                    pareto_fronts_list.append(pareto_front_json[p])
                i += 1

        if algo == "igdeci" or algo == "igdecn":
            i = 0
            for p in pareto_front_json:
                key = "pareto_" + str(i)
                if key in p:
                    # print(pareto_front_json[p])
                    pareto_fronts_list.append(pareto_front_json[p])
                i += 1

        if algo == "qaoai" or algo == "qaoan":
            i = 0
            for p in pareto_front_json:
                key = "pareto_front_" + str(i)
                if key in p:
                    # print(pareto_front_json[p])
                    pareto_fronts_list.append(pareto_front_json[p])
                i += 1

        return pareto_fronts_list

    def __get_paretos_paths_by_algo_and_program(self, algo, program):
        if algo == "add-greedy":
            pareto_greedy_pre_ju = f"../results/add-greedy/junit/{program}_pre-fix_data.json"
            pareto_greedy_post_ju = f"../results/add-greedy/junit/{program}_post-fix_data.json"
            pareto_greedy_pre_jmh = f"../results/add-greedy/jmh/{program}_pre-fix_data.json"
            pareto_greedy_post_jmh = f"../results/add-greedy/jmh/{program}_post-fix_data.json"
            return pareto_greedy_pre_ju, pareto_greedy_post_ju, pareto_greedy_pre_jmh, pareto_greedy_post_jmh
        elif algo == "divga":
            pareto_divga_pre_ju = f"../results/divga/junit/{program}junitpre_pareto_fronts_divga.json"
            pareto_divga_post_ju = f"../results/divga/junit/{program}junitpost_pareto_fronts_divga.json"
            pareto_divga_pre_jmh = f"../results/divga/jmh/{program}jmhpre_pareto_fronts_divga.json"
            pareto_divga_post_jmh = f"../results/divga/jmh/{program}jmhpost_pareto_fronts_divga.json"
            return pareto_divga_pre_ju, pareto_divga_post_ju, pareto_divga_pre_jmh, pareto_divga_post_jmh
        elif algo == "igdeci":
            pareto_igdeci_pre_ju = f"../results/igdec_qaoa/tcs/ideal/qaoa_1/{program}_pre-fix/junit/size_7/10/pareto_fronts.json"
            pareto_igdeci_post_ju = f"../results/igdec_qaoa/tcs/ideal/qaoa_1/{program}_post-fix/junit/size_7/10/pareto_fronts.json"
            pareto_igdeci_pre_jmh = f"../results/igdec_qaoa/tcs/ideal/qaoa_1/{program}_pre-fix/jmh/size_7/10/pareto_fronts.json"
            pareto_igdeci_post_jmh = f"../results/igdec_qaoa/tcs/ideal/qaoa_1/{program}_post-fix/jmh/size_7/10/pareto_fronts.json"
            return pareto_igdeci_pre_ju, pareto_igdeci_post_ju, pareto_igdeci_pre_jmh, pareto_igdeci_post_jmh
        elif algo == "igdecn":
            pareto_igdecn_pre_ju = f"../results/igdec_qaoa/tcs/noise/qaoa_1/{program}_pre-fix/junit/size_7/10/pareto_fronts.json"
            pareto_igdecn_post_ju = f"../results/igdec_qaoa/tcs/noise/qaoa_1/{program}_post-fix/junit/size_7/10/pareto_fronts.json"
            pareto_igdecn_pre_jmh = f"../results/igdec_qaoa/tcs/noise/qaoa_1/{program}_pre-fix/jmh/size_7/10/pareto_fronts.json"
            pareto_igdecn_post_jmh = f"../results/igdec_qaoa/tcs/noise/qaoa_1/{program}_post-fix/jmh/size_7/10/pareto_fronts.json"
            return pareto_igdecn_pre_ju, pareto_igdecn_post_ju, pareto_igdecn_pre_jmh, pareto_igdecn_post_jmh
        elif algo == "qaoai":
            pareto_qaoai_pre_ju = f"../results/qaoa_tcs/ideal/data/junit/{program}_pre-fix-data.json"
            pareto_qaoai_post_ju = f"../results/qaoa_tcs/ideal/data/junit/{program}_post-fix-data.json"
            pareto_qaoai_pre_jmh = f"../results/qaoa_tcs/ideal/data/jmh/{program}_pre-fix-data.json"
            pareto_qaoai_post_jmh = f"../results/qaoa_tcs/ideal/data/jmh/{program}_post-fix-data.json"
            return pareto_qaoai_pre_ju, pareto_qaoai_post_ju, pareto_qaoai_pre_jmh, pareto_qaoai_post_jmh
        elif algo == "qaoan":
            pareto_qaoan_pre_jmh = f"../results/qaoa_tcs/noise/data/jmh/{program}_pre-fix-data.json"
            pareto_qaoan_post_jmh = f"../results/qaoa_tcs/noise/data/jmh/{program}_post-fix-data.json"
            pareto_qaoan_pre_ju = f"../results/qaoa_tcs/noise/data/junit/{program}_pre-fix-data.json"
            pareto_qaoan_post_ju = f"../results/qaoa_tcs/noise/data/junit/{program}_post-fix-data.json"
            return pareto_qaoan_pre_ju, pareto_qaoan_post_ju, pareto_qaoan_pre_jmh, pareto_qaoan_post_jmh

        return None

    def __compute_coverage_means(self, test_coverage_json_path, pareto_fronts, program):
        test_coverage_line_by_line = json.load(open(test_coverage_json_path))

        mean_cov_par_list = list()

        for pareto_front in pareto_fronts:
            mean_cov_sol_list = list()

            for solution in pareto_front:
                covered_lines = set()

                for test in solution:
                    lines = test_coverage_line_by_line[str(test)]
                    for line in lines:
                        if line in self.suite_lines_covered[program]:
                            covered_lines.add(line)

                # check if the complete suite covered all the lines with performance issues
                if len(self.suite_lines_covered[program]) == self.total_lines_to_cover[program]:
                    mean_cov_sol_list.append(len(covered_lines) / len(self.suite_lines_covered[program]))
                else:
                    mean_cov_sol_list.append(len(covered_lines) / self.total_lines_to_cover[program])

            if len(mean_cov_sol_list) > 0:
                mean_cov_par_list.append(mean(mean_cov_sol_list))
            else:
                mean_cov_par_list.append(0)

        return mean_cov_par_list

    def means_coverage_new(self, program):
        test_coverage_line_by_line_pre_ju = f"../data/processed/{program}_pre-fix/junit/test_coverage_line_by_line.json"
        test_coverage_line_by_line_post_ju = f"../data/processed/{program}_post-fix/junit/test_coverage_line_by_line.json"
        test_coverage_line_by_line_pre_jmh = f"../data/processed/{program}_pre-fix/jmh/test_coverage_line_by_line.json"
        test_coverage_line_by_line_post_jmh = f"../data/processed/{program}_post-fix/jmh/test_coverage_line_by_line.json"

        algos = ["add-greedy", "divga", "igdeci", "igdecn", "qaoai", "qaoan"]
        for algo in algos:
            pareto_pre_ju, pareto_post_ju, pareto_pre_jmh, pareto_post_jmh = self.__get_paretos_paths_by_algo_and_program(algo, program)

            # pre and post fix ju
            program_ver = f"{program}_pre-fix-ju"
            pareto_fronts = self.__prepare_pareto(pareto_pre_ju, algo)
            mean_cov_par_list_pre = self.__compute_coverage_means(test_coverage_line_by_line_pre_ju, pareto_fronts, program_ver)
            program_ver = f"{program}_post-fix-ju"
            pareto_fronts = self.__prepare_pareto(pareto_post_ju, algo)
            mean_cov_par_list_post = self.__compute_coverage_means(test_coverage_line_by_line_post_ju, pareto_fronts, program_ver)
            key = program + "_ju_" + algo
            self.total_coverage_means[key] = list(mean_cov_par_list_pre + mean_cov_par_list_post)

            # pre and post fix jmh
            program_ver = f"{program}_pre-fix-jmh"
            pareto_fronts = self.__prepare_pareto(pareto_pre_jmh, algo)
            mean_cov_par_list_pre = self.__compute_coverage_means(test_coverage_line_by_line_pre_jmh, pareto_fronts, program_ver)
            program_ver = f"{program}_post-fix-jmh"
            pareto_fronts = self.__prepare_pareto(pareto_post_jmh, algo)
            mean_cov_par_list_post = self.__compute_coverage_means(test_coverage_line_by_line_post_jmh, pareto_fronts, program_ver)
            key = program + "_jmh_" + algo
            self.total_coverage_means[key] = list(mean_cov_par_list_pre + mean_cov_par_list_post)

    def __compute_costs_means(self, test_costs_json_path, pareto_fronts, program):
        test_cases_costs = json.load(open(test_costs_json_path))

        mean_cos_par_list = list()

        for pareto_front in pareto_fronts:
            cos_sol_list = list()

            for solution in pareto_front:
                for test in solution:
                    cost = test_cases_costs[str(test)]
                    cos_sol_list.append(cost)

            if(len(cos_sol_list) > 0):
                mean_cos_par_list.append(mean(cos_sol_list))
            else:
                mean_cos_par_list.append(0)

        return mean_cos_par_list

    def means_costs_new(self, program):
        test_cases_costs_pre_ju = f"../data/processed/{program}_pre-fix/junit/test_cases_costs.json"
        test_cases_costs_post_ju = f"../data/processed/{program}_post-fix/junit/test_cases_costs.json"
        test_cases_costs_pre_jmh = f"../data/processed/{program}_pre-fix/jmh/test_cases_costs.json"
        test_cases_costs_post_jmh = f"../data/processed/{program}_post-fix/jmh/test_cases_costs.json"

        algos = ["add-greedy", "divga", "igdeci", "igdecn", "qaoai", "qaoan"]
        for algo in algos:
            pareto_pre_ju, pareto_post_ju, pareto_pre_jmh, pareto_post_jmh = self.__get_paretos_paths_by_algo_and_program(algo, program)

            # pre and post fix ju
            program_ver = f"{program}_pre-fix-ju"
            pareto_fronts = self.__prepare_pareto(pareto_pre_ju, algo)
            mean_cov_par_list_pre = self.__compute_costs_means(test_cases_costs_pre_ju, pareto_fronts, program_ver)
            program_ver = f"{program}_post-fix-ju"
            pareto_fronts = self.__prepare_pareto(pareto_post_ju, algo)
            mean_cov_par_list_post = self.__compute_costs_means(test_cases_costs_post_ju, pareto_fronts, program_ver)
            key = program + "_ju_" + algo
            self.total_costs_means[key] = list(mean_cov_par_list_pre + mean_cov_par_list_post)

            # pre and post fix jmh
            program_ver = f"{program}_pre-fix-jmh"
            pareto_fronts = self.__prepare_pareto(pareto_pre_jmh, algo)
            mean_cov_par_list_pre = self.__compute_costs_means(test_cases_costs_pre_jmh, pareto_fronts, program_ver)
            program_ver = f"{program}_post-fix-jmh"
            pareto_fronts = self.__prepare_pareto(pareto_post_jmh, algo)
            mean_cov_par_list_post = self.__compute_costs_means(test_cases_costs_post_jmh, pareto_fronts, program_ver)
            key = program + "_jmh_" + algo
            self.total_costs_means[key] = list(mean_cov_par_list_pre + mean_cov_par_list_post)

    def print_means_to_csv(self):
        with open("means_coverage.csv", "w", newline="") as outfile:
            writer = csv.writer(outfile)
            writer.writerow(self.total_coverage_means.keys())
            writer.writerows(zip(*self.total_coverage_means.values()))

        with open("means_costs.csv", "w", newline="") as outfile:
            writer = csv.writer(outfile)
            writer.writerow(self.total_costs_means.keys())
            writer.writerows(zip(*self.total_costs_means.values()))


def main():
    meanArrClass = MeanArrClass()

    programs = ["avro", "hive"]
    for program in programs:
        meanArrClass.means_coverage_new(program)
        meanArrClass.means_costs_new(program)
        meanArrClass.print_means_to_csv()

if __name__ == "__main__":
    main()