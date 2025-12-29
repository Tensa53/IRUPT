import json
import os
import sys
import time
import matplotlib.pyplot as plt
import numpy as np
import statistics
import warnings
import plotly.graph_objects as go

from qiskit_optimization import QuadraticProgram
from qiskit_algorithms.optimizers import COBYLA
from qiskit_algorithms import QAOA
from qiskit_aer.primitives import Sampler as AerSampler
from qiskit_aer.noise import NoiseModel
from qiskit_ibm_runtime.fake_provider import FakeBrisbane
from qiskit_optimization.converters import QuadraticProgramToQubo
from sklearn.preprocessing import StandardScaler
from scipy.cluster.hierarchy import linkage, fcluster
from collections import defaultdict
from matplotlib import MatplotlibDeprecationWarning

warnings.filterwarnings("ignore", category=MatplotlibDeprecationWarning)
warnings.filterwarnings("ignore", category=DeprecationWarning)

class SelectQAOA:
    sir_programs_tests_number = {}
    test_tool = ""
    mode = ""

    def __init__(self, test_tool, mode, sir_programs_tests_number):
        self.test_tool = test_tool
        self.mode = mode
        self.sir_programs_tests_number = sir_programs_tests_number

    # example programs
    # sir_programs = ["MavenProjectJ4_pre-fix","MavenProjectJ4_post-fix","MavenProjectJ5_pre-fix","MavenProjectJ5_post-fix"]
    # sir_programs_rep_values = {"MavenProjectJ4_pre-fix": 1, "MavenProjectJ4_post-fix": 1, "MavenProjectJ5_pre-fix": 1, "MavenProjectJ5_post-fix": 1}
    # penalties_dictionary = {"MavenProjectJ4_pre-fix": None, "MavenProjectJ4_post-fix": None, "MavenProjectJ5_pre-fix": None, "MavenProjectJ5_post-fix": None}
    # qubos_dictionary = {"MavenProjectJ4_pre-fix": [], "MavenProjectJ4_post-fix": [], "MavenProjectJ5_pre-fix": [], "MavenProjectJ5_post-fix": []}

    # real programs
    sir_programs = ["avro_pre-fix", "avro_post-fix", "hive_pre-fix", "hive_post-fix"]
    sir_programs_rep_values = {"avro_pre-fix": 1, "avro_post-fix": 1, "hive_pre-fix": 1, "hive_post-fix": 1}
    penalties_dictionary = {"avro_pre-fix": None, "avro_post-fix": None, "hive_pre-fix": None, "hive_post-fix": None}
    qubos_dictionary = {"avro_pre-fix": [], "avro_post-fix": [], "hive_pre-fix": [], "hive_post-fix": []}

    alpha = 0.5
    executed_lines_test_by_test = dict()
    test_coverage_line_by_line = dict()
    test_cases_costs = dict()
    total_program_lines = dict()
    clusters_dictionary = dict()

    def to_json_safe(self, obj):
        if isinstance(obj, defaultdict):
            obj = dict(obj)
        if isinstance(obj, dict):
            return {str(self.to_json_safe(k)): self.to_json_safe(v) for k, v in obj.items()}
        if isinstance(obj, list):
            return [self.to_json_safe(v) for v in obj]
        if isinstance(obj, np.generic):  # np.int32, np.int64, etc.
            return int(obj)
        return obj

    def json_keys_to_int(self, d):
        """This method correctly converts the data"""
        if isinstance(d, dict):
            return {int(k) if k.isdigit() else k: self.json_keys_to_int(v) for k, v in d.items()}
        elif isinstance(d, list):
            return [self.json_keys_to_int(i) for i in d]
        else:
            return d

    def load_file_contents(self):
        # example programs data
        # executed_lines_test_by_test_json_filepath = f"../../data_example/merged/{self.test_tool}/executed_lines_test_by_test_all_programs.json"
        # test_coverage_line_by_line_json_filepath = f"../../data_example/merged/{self.test_tool}/test_coverage_line_by_line_all_programs.json"
        # test_cases_cost_json_filepath= f"../../data_example/merged/{self.test_tool}/test_cases_costs_all_programs.json"
        # total_program_lines_json_filepath = f"../../data_example/merged/{self.test_tool}/total_program_lines_all_programs.json"

        # real programs data
        executed_lines_test_by_test_json_filepath = f"../../data/merged/{self.test_tool}/executed_lines_test_by_test_all_programs.json"
        test_coverage_line_by_line_json_filepath = f"../../data/merged/{self.test_tool}/test_coverage_line_by_line_all_programs.json"
        test_cases_cost_json_filepath= f"../../data/merged/{self.test_tool}/test_cases_costs_all_programs.json"
        total_program_lines_json_filepath = f"../../data/merged/{self.test_tool}/total_program_lines_all_programs.json"

        with open(executed_lines_test_by_test_json_filepath, "r") as file:
            # dictionary that, for each sir program, associates at each LINE of that program the LIST of TESTS COVERING it
            self.executed_lines_test_by_test = json.load(file) # {program1:{line:[tci,tcj,...,tck],line2:...}
        with open(test_coverage_line_by_line_json_filepath, "r") as file:
            # dictionary that, for each sir program, associates at each TEST of that program the LIST of LINES COVERED by it
            self.test_coverage_line_by_line = self.json_keys_to_int(json.load(file)) # {program1:{tc1:[linei,linej,...,linek],tc2:...}
        with open(test_cases_cost_json_filepath, "r") as file:
            # dictionary that, for each sir program, associates at each TEST its EXECUTION COST
            self.test_cases_costs = self.json_keys_to_int(json.load(file)) # {program1:{tc1:ex_cost1,tc2:ex_cost2,...,tcn:ex_costn},program2:...}
        with open(total_program_lines_json_filepath, "r") as file:
            # dictionary which associates at each SIR PROGRAM its size in terms of the NUMBER OF ITS LINES
            self.total_program_lines = json.load(file) # {program1:tot_lines_program1,program2:tot_lines_program2,program3:...}

    def num_of_covered_lines(self, sir_program, test_cases):
        """This method returns the number of covered lines (no redundancy)"""
        covered_lines = set()

        for test_case in test_cases:
            try:
                for covered_line in self.test_coverage_line_by_line[sir_program][test_case]:
                    covered_lines.add(covered_line)
            except:
                continue

        return len(covered_lines)

    def process_clusters(self):
        for sir_program in self.sir_programs:

            dir_path = f"../../results/qaoa_tcs/{self.mode}/clusters/{self.test_tool}"
            if not os.path.exists(dir_path):
                os.makedirs(dir_path)

            # print("Processing clusters for: " + sir_program)

            test_suite_stmt_cov = set()

            for line in self.executed_lines_test_by_test[sir_program].keys():
                    test_suite_stmt_cov.add(int(line))

            # print(len(test_suite_stmt_cov))

            test_cases_stmt_cov = []
            for test_case in self.test_coverage_line_by_line[sir_program].keys():
                # test_cases_stmt_cov.append(len(self.test_coverage_line_by_line[sir_program][test_case]))
                test_cases_stmt_cov.append(len(self.test_coverage_line_by_line[sir_program][test_case])/len(test_suite_stmt_cov))

            # Normalize data
            data = np.column_stack((list(self.test_cases_costs[sir_program].values()), test_cases_stmt_cov))
            scaler = StandardScaler()
            normalized_data = scaler.fit_transform(data)

            num_clusters = 50

            max_cluster_dim = 7

            # Step 2: Perform K-Means Clustering
            start = time.time()
            linkage_matrix = linkage(normalized_data, method='ward')
            clusters = fcluster(linkage_matrix, t=num_clusters, criterion='maxclust')

            clust_list = list(enumerate(clusters))
            # print(len(clust_list))
            # print(clust_list)

            # Organize test cases by cluster
            clustered_data = defaultdict(list)
            for idx, cluster_id in enumerate(clusters):
                clustered_data[cluster_id].append(idx)

            # Process clusters to ensure none exceed max_cluster_dim
            new_cluster_id = max(clustered_data.keys()) + 1  # Start new IDs after existing ones
            to_add = []  # Collect new smaller clusters

            for cluster_id, elements in list(clustered_data.items()):  # Avoid modifying dict during iteration
                if len(elements) > max_cluster_dim:
                    num_splits = -(-len(
                        elements) // max_cluster_dim)  # Ceiling division to get the required number of splits
                    split_size = -(-len(elements) // num_splits)  # Recalculate to distribute elements evenly

                    # Split while keeping sizes balanced
                    parts = [elements[i:i + split_size] for i in range(0, len(elements), split_size)]

                    # Ensure all new clusters are within max_cluster_dim
                    for part in parts:
                        if len(part) > max_cluster_dim:
                            raise ValueError(f"A split cluster still exceeds max_cluster_dim ({len(part)} > {max_cluster_dim})!")

                    # Add new parts to the new clusters
                    to_add.extend(parts)

                    # Remove original large cluster
                    del clustered_data[cluster_id]

            # Assign new IDs to split parts
            for part in to_add:
                if part:  # Only add if the part is non-empty
                    clustered_data[new_cluster_id] = part
                    new_cluster_id += 1
            end = time.time()
            # print("SelectQAOA Decomposition Time(ms): " + str((end - start) * 1000))

            self.clusters_dictionary[sir_program] = clustered_data

            # Step 3: Calculate the metrics for each cluster and validate
            cluster_metrics = {}
            for cluster_id in clustered_data.keys():
                tot_cluster_exec_cost = 0
                tot_cluster_stmt_cov = 0
                for test_case in clustered_data[cluster_id]:
                    tot_cluster_exec_cost += self.test_cases_costs[sir_program][test_case]
                tot_cluster_stmt_cov = self.num_of_covered_lines(sir_program, clustered_data[cluster_id]) / self.total_program_lines[sir_program]
                cluster_metrics[cluster_id] = {
                    "tot_exec_cost": tot_cluster_exec_cost,
                    "tot_stmt_cov": tot_cluster_stmt_cov  # Avg stmt coverage per test case in cluster
                }

            # Extracting data for plotting
            exec_costs = np.array(list(self.test_cases_costs[sir_program].values()))
            stmt_covs = np.array(test_cases_stmt_cov)

            # Plotting the clusters in 2D space
            fig, ax = plt.subplots(figsize=(12, 8))

            # Plot each cluster with a different color
            colors = plt.cm.get_cmap("tab10", num_clusters)
            for cluster_id in clustered_data.keys():
                cluster_indices = clustered_data[cluster_id]

                # Plot each cluster's points
                ax.scatter(
                    exec_costs[cluster_indices],
                    stmt_covs[cluster_indices],
                    color=colors(cluster_id),
                    label=f"Cluster {cluster_id + 1}",
                    alpha=0.6,
                    s=50
                )

            # Label the axes
            ax.set_xlabel("Execution Cost", fontsize=12)
            ax.set_ylabel("Statement Coverage", fontsize=12)
            ax.legend(bbox_to_anchor=(1.05, 1), loc='upper left', fontsize=8)
            ax.set_title("Test Case Clustering Visualization for Program: " + sir_program, fontsize=14)
            ax.grid(True, alpha=0.3)

            cluster_path = dir_path + "/" + sir_program + "-clusters.json"

            json_safe_data = self.to_json_safe(clustered_data)

            # plot_path = dir_path + sir_program + "-clusters.pdf"
            #
            # # Save the plot as PDF with bbox_inches to handle legend outside plot area
            # plt.savefig(plot_path, bbox_inches='tight', dpi=100)
            #
            with open(cluster_path, "w") as f:
                json.dump(json_safe_data, f)

            # Save the plot as interactive HTML
            plot_path_html = dir_path + "/" + sir_program + "-clusters.html"

            # Create interactive Plotly plot
            plotly_fig = go.Figure()

            colors_list = plt.cm.tab10.colors
            for cluster_id in clustered_data.keys():
                cluster_indices = clustered_data[cluster_id]
                color_idx = cluster_id % len(colors_list)
                rgb_color = f'rgb({int(colors_list[color_idx][0]*255)},{int(colors_list[color_idx][1]*255)},{int(colors_list[color_idx][2]*255)})'

                plotly_fig.add_trace(go.Scatter(
                    x=exec_costs[cluster_indices],
                    y=stmt_covs[cluster_indices],
                    mode='markers',
                    name=f'Cluster {cluster_id + 1}',
                    marker=dict(
                        size=8,
                        color=rgb_color,
                        opacity=0.6,
                        line=dict(width=0.5, color='white')
                    ),
                    hovertemplate='<b>Cluster %{fullData.name}</b><br>Execution Cost: %{x:.2f}<br>Statement Coverage: %{y}<extra></extra>'
                ))

            plotly_fig.update_layout(
                title=f"Test Case Clustering Visualization for Program: {sir_program}",
                xaxis_title="Execution Cost",
                yaxis_title="Statement Coverage",
                hovermode='closest',
                template='plotly_white',
                width=1200,
                height=800
            )

            plotly_fig.write_html(plot_path_html)

    def make_linear_terms(self, sir_program, cluster_test_cases, alpha):
        """Making the linear terms of QUBO"""
        max_cost = max(self.test_cases_costs[sir_program].values())

        estimated_costs = []

        # linear coefficients, that are the diagonal of the matrix encoding the QUBO
        for test_case in cluster_test_cases:
            # MODIFIED coefficients: removed alpha weights and removed fault objective, considering only costs ++++
            estimated_costs.append(self.test_cases_costs[sir_program][test_case] / max_cost)

        return np.array(estimated_costs)

    def make_quadratic_terms(self, sir_program, variables, cluster_test_cases, linear_terms, penalty):
        """Making the quadratic terms of QUBO"""
        quadratic_terms = {}

        # k is a stmt
        for k in self.executed_lines_test_by_test[sir_program].keys():
            # k_test_cases is the list of test cases covering k
            k_test_cases = self.executed_lines_test_by_test[sir_program][k]
            for i in k_test_cases:
                if i not in cluster_test_cases or i not in variables:
                    continue
                for j in k_test_cases:
                    if j not in cluster_test_cases or j not in variables:
                        continue
                    if i < j:
                        linear_terms[variables.index(i)] -= penalty
                        try:
                            quadratic_terms[variables.index(i), variables.index(j)] += 2 * penalty
                        except:
                            quadratic_terms[variables.index(i), variables.index(j)] = 2 * penalty

        return quadratic_terms

    def create_QUBO_problem(self, linear_terms, quadratic_terms):
        """This function is the one that has to encode the QUBO problem that QAOA will have to solve. The QUBO problem specifies the optimization to solve and a quadratic binary unconstrained problem"""
        qubo = QuadraticProgram()

        for i in range(0, len(linear_terms)):
            qubo.binary_var('x%s' % (i))

        qubo.minimize(linear=linear_terms, quadratic=quadratic_terms)

        return qubo

    def specify_penalties(self):
        # to get a QUBO problem from a quadratic problem with constraints, we have to insert those constraints into the Hamiltonian to solve (which is the one encoded by the QUBO problem). When we insert constraint into the Hamiltonian, we have to specify also penalties
        for sir_program in self.sir_programs:
            max_penalty = 0
            max_cost = max(self.test_cases_costs[sir_program].values())
            for i in range(self.sir_programs_tests_number[sir_program]):
                cost = (self.test_cases_costs[sir_program][i]/max_cost)
                if cost > max_penalty:
                    max_penalty = cost
            self.penalties_dictionary[sir_program] = max_penalty + 1

    def save_QUBO_in_dict(self):
        converter = QuadraticProgramToQubo()
        # make a dictionary that saves, for each program, the correspondent QUBO
        for sir_program in self.sir_programs:
            for cluster_id in self.clusters_dictionary[sir_program]:
                variables = []
                for idx in range(0, len(self.clusters_dictionary[sir_program][cluster_id])):
                    variables.append(idx)
                linear_terms = self.make_linear_terms(sir_program, self.clusters_dictionary[sir_program][cluster_id], self.alpha)
                quadratic_terms = self.make_quadratic_terms(sir_program, variables,
                                                       self.clusters_dictionary[sir_program][cluster_id], linear_terms,
                                                       self.penalties_dictionary[sir_program])
                qubo = self.create_QUBO_problem(linear_terms, quadratic_terms)
                self.qubos_dictionary[sir_program].append(qubo)

    def covered_lines(self, sir_program, test_cases_list):
        """Number of covered lines (no redundancy)"""
        covered_lines = set()

        for test_case in test_cases_list:
            try:
                for covered_line in self.test_coverage_line_by_line[sir_program][test_case]:
                    covered_lines.add(covered_line)
            except:
                continue

        return len(covered_lines)

    def build_pareto_front(self, sir_program, selected_tests):
        """This method builds the pareto front additionally from a subtest suite solution"""
        pareto_front = []
        max_stmt_coverage = 0

        for index in range(1, len(selected_tests) + 1):
            # exract the first index selected tests
            candidate_solution = selected_tests[:index]
            candidate_solution_stmt_coverage = 0
            for selected_test in candidate_solution:
                candidate_solution_stmt_coverage += self.covered_lines(sir_program, candidate_solution)
            # if the actual pareto front dominates the candidate solution, get to the next candidate
            if max_stmt_coverage >= candidate_solution_stmt_coverage:
                continue
            # eventually update the pareto front information
            if candidate_solution_stmt_coverage > max_stmt_coverage:
                max_stmt_coverage = candidate_solution_stmt_coverage
            # add the candidate solution to the pareto front
            pareto_front.append(candidate_solution)

        return pareto_front

    def run_ideal_simulator(self):
        sampling_noise_sampler = AerSampler()
        sampling_noise_sampler.options.shots = None

        for sir_program in self.sir_programs:
            qaoa = QAOA(sampler=sampling_noise_sampler, optimizer=COBYLA(500),reps=self.sir_programs_rep_values[sir_program])
            # the fronts will be saved into files
            print("Executing Ideal Simulator for Program: " + sir_program)
            dir_path = f"../../results/qaoa_tcs/ideal/data/{self.test_tool}"
            if not os.path.exists(dir_path):
                os.makedirs(dir_path)
            file_path = dir_path + "/" + sir_program + "-data.json"
            json_data = {}
            qpu_run_times = []
            pareto_fronts_building_times = []
            experiments = 10

            for i in range (experiments):
                final_selected_tests = []
                cluster_dict_index = 0
                for qubo in self.qubos_dictionary[sir_program]:
                    print("QUBO Problem: " + str(qubo) + "\n Cluster Number: " + str(cluster_dict_index))
                    print("Cluster's Test Cases: " + str(list(self.clusters_dictionary[sir_program].values())[cluster_dict_index]))
                    # for each iteration get the result
                    operator, offset = qubo.to_ising()
                    print("Linear QUBO: " + str(qubo))
                    # for each iteration get the result
                    s = time.time()
                    qaoa_result = qaoa.compute_minimum_eigenvalue(operator)
                    e = time.time()
                    # print("QAOA Result: " + str(qaoa_result))
                    qpu_run_times.append((e - s) * 1000)

                    eigenstate = qaoa_result.eigenstate
                    most_likely = max(eigenstate.items(), key=lambda x: x[1])[0]

                    # Convert to bitstring format
                    if isinstance(most_likely, int):
                        n = qubo.get_num_binary_vars()
                        bitstring = [int(b) for b in format(most_likely, f'0{n}b')[::-1]]
                    elif isinstance(most_likely, str):
                        bitstring = [int(b) for b in most_likely[::-1]]
                    else:
                        raise ValueError(f"Unsupported eigenstate key type: {type(most_likely)}")

                    indexes_selected_tests = [index for index, value in enumerate(bitstring) if value == 1]
                    print("Indexes of selected tests to convert. " + str(indexes_selected_tests))
                    selected_tests = []
                    for index in indexes_selected_tests:
                        selected_tests.append(list(self.clusters_dictionary[sir_program].values())[cluster_dict_index][index])
                    print("Selected tests: " + str(selected_tests))
                    print("Experiment Number: " + str(i))
                    cluster_dict_index += 1
                    for selected_test in selected_tests:
                        if selected_test not in final_selected_tests:
                            final_selected_tests.append(selected_test)

                # now we have to build the pareto front
                print("Final Selected Test Cases: " + str(final_selected_tests))
                print("Length of the final list of selected test cases: " + str(len(final_selected_tests)))
                start = time.time()
                pareto_front = self.build_pareto_front(sir_program, final_selected_tests)
                end = time.time()
                json_data["pareto_front_" + str(i)] = pareto_front
                pareto_front_building_time = (end - start) * 1000
                pareto_fronts_building_times.append(pareto_front_building_time)

            # compute the average time needed for the construction of a pareto frontier and run time
            mean_qpu_run_time = statistics.mean(qpu_run_times)
            mean_pareto_fronts_building_time = statistics.mean(pareto_fronts_building_times)
            json_data["mean_qpu_run_time(ms)"] = mean_qpu_run_time
            json_data["stdev_qpu_run_time(ms)"] = statistics.stdev(qpu_run_times)
            json_data["all_qpu_run_times(ms)"] = qpu_run_times
            json_data["mean_pareto_fronts_building_time(ms)"] = mean_pareto_fronts_building_time

            with open(file_path, "w") as file:
                json.dump(json_data, file)

    def run_noise_simulator(self):
        noise_model = NoiseModel.from_backend(FakeBrisbane())
        fake_sampler = AerSampler(backend_options={'noise_model': noise_model})
        fake_sampler.options.shots = 2048

        # I want to run the sampler 30 times to obtain different results for each sir program
        for sir_program in self.sir_programs:
            qaoa = QAOA(sampler=fake_sampler, optimizer=COBYLA(500), reps=self.sir_programs_rep_values[sir_program])
            # the fronts will be saved into files
            print("Executing Noise Simulator for Program: " + str(sir_program))
            dir_path = f"../../results/qaoa_tcs/noise/data/{self.test_tool}"
            if not os.path.exists(dir_path):
                os.makedirs(dir_path)
            file_path = dir_path + "/" + sir_program + "-data.json"
            json_data = {}
            qpu_run_times = []
            pareto_fronts_building_times = []
            experiments = 10

            for i in range(experiments):
                final_selected_tests = []
                cluster_dict_index = 0
                for qubo in self.qubos_dictionary[sir_program]:
                    print("Experiment Number: " + str(i))
                    print("QUBO Problem: " + str(qubo) + "\nCluster Number: " + str(cluster_dict_index))
                    print("Cluster's Test Cases: " + str(list(self.clusters_dictionary[sir_program].values())[cluster_dict_index]))
                    # for each iteration get the result
                    operator, offset = qubo.to_ising()
                    print("Linear QUBO: " + str(qubo))
                    # for each iteration get the result
                    s = time.time()
                    qaoa_result = qaoa.compute_minimum_eigenvalue(operator)
                    e = time.time()
                    # print("QAOA Result: " + str(qaoa_result))
                    qpu_run_times.append((e - s) * 1000)

                    eigenstate = qaoa_result.eigenstate
                    most_likely = max(eigenstate.items(), key=lambda x: x[1])[0]

                    # Convert to bitstring format
                    if isinstance(most_likely, int):
                        n = qubo.get_num_binary_vars()
                        bitstring = [int(b) for b in format(most_likely, f'0{n}b')[::-1]]
                    elif isinstance(most_likely, str):
                        bitstring = [int(b) for b in most_likely[::-1]]
                    else:
                        raise ValueError(f"Unsupported eigenstate key type: {type(most_likely)}")

                    indexes_selected_tests = [index for index, value in enumerate(bitstring) if value == 1]
                    print("Indexes of selected tests to convert. " + str(indexes_selected_tests))
                    selected_tests = []
                    for index in indexes_selected_tests:
                        selected_tests.append(list(self.clusters_dictionary[sir_program].values())[cluster_dict_index][index])
                    print("Selected tests: " + str(selected_tests))
                    cluster_dict_index += 1
                    for selected_test in selected_tests:
                        if selected_test not in final_selected_tests:
                            final_selected_tests.append(selected_test)

                # now we have to build the pareto front
                print("Final Selected Test Cases: " + str(final_selected_tests))
                print(len(final_selected_tests))
                start = time.time()
                pareto_front = self.build_pareto_front(sir_program, final_selected_tests)
                end = time.time()
                json_data["pareto_front_" + str(i)] = pareto_front
                pareto_front_building_time = (end - start) * 1000
                pareto_fronts_building_times.append(pareto_front_building_time)

            # compute the average time needed for the construction of a pareto frontier and run time
            mean_qpu_run_time = statistics.mean(qpu_run_times)
            mean_pareto_fronts_building_time = statistics.mean(pareto_fronts_building_times)
            json_data["mean_qpu_run_time(ms)"] = mean_qpu_run_time
            json_data["stdev_qpu_run_time(ms)"] = statistics.stdev(qpu_run_times)
            json_data["all_qpu_run_times(ms)"] = qpu_run_times
            json_data["mean_pareto_fronts_building_time(ms)"] = mean_pareto_fronts_building_time

            with open(file_path, "w") as file:
                json.dump(json_data, file)

def main():
    mode = sys.argv[1]
    testTool = sys.argv[2]
    sir_programs_tests_number = {}
    if testTool == "junit":
        # junit tests
        # sir_programs_tests_number = {"MavenProjectJ4_pre-fix": 30, "MavenProjectJ4_post-fix": 30, "MavenProjectJ5_pre-fix": 30, "MavenProjectJ5_post-fix": 30}
        sir_programs_tests_number = {"avro_pre-fix": 130, "avro_post-fix": 130, "hive_pre-fix": 306, "hive_post-fix": 306}
    elif testTool == "jmh":
        # jmh benchs
        # sir_programs_tests_number = {"MavenProjectJ4_pre-fix": 52, "MavenProjectJ4_post-fix": 52, "MavenProjectJ5_pre-fix": 52, "MavenProjectJ5_post-fix": 52}
        sir_programs_tests_number = {"avro_pre-fix": 333, "avro_post-fix": 333, "hive_pre-fix": 414, "hive_post-fix": 414}
    selectQAOA = SelectQAOA(testTool, mode, sir_programs_tests_number)
    selectQAOA.load_file_contents()
    selectQAOA.process_clusters()
    selectQAOA.specify_penalties()
    selectQAOA.save_QUBO_in_dict()
    if mode == "ideal":
        # run the ideal simulator
        selectQAOA.run_ideal_simulator()
    elif mode == "noise":
        # run the noise simulator
        selectQAOA.run_noise_simulator()
    else:
        print("Unsupported mode: " + mode + ". Available modes: ideal, noise")
        exit(1)

if __name__ == '__main__':
    main()