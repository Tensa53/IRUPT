# ERUPT

**ERUPT** (**E**xploring the **R**ole of **U**nit tests for **P**erformance regression **T**esting optimization)
is a study that analyzes how unit tests metrics (statement coverage and execution times) can support the optimization 
of a regression testing campaign in the context of Performance Testing.

## Methodology Workflow

### 0. Configuration Setup
In order to execute the next phases of the workflow, a setup of the execution environment is necessary. 

For the Java classes, there is a configuration file inside the **java/** folder, for Maven build system.
Starting from the initial configuration file, it has to be adapted to the one of the analyzed software. 
A reference implementation for a correctly configured project is available at [MavenProjectJ4](https://github.com/Tensa53/MavenProjectJ4). 

For the Python modules and algorithms, there is a requirements.txt inside the root of this repository.

DIV-GA algorithm requires MATLAB R2025a and Global Optimization Toolbox.

For the statistical analysis phase, an R installation is required.

### 1. Data Collection
Some software will be chosen to retrieve the data and compute the metrics. The **java/** folder contains all the
necessary code, organized in packages, that is necessary to add to the software to analyze it and collect the data:
- **benchmarks/**: contains all the micro-benchmarks created;
  - **profiler/**: contains the classes that collect data from micro-benchmarks;
    - **JaCoCoSplit.java**: write the execution data of JaCoCo from the JMX Agent Stream to an .exec file, for each 
micro-benchmark method;
    - **JaCoCoProfiler.java**: intercept the end of an iteration of a micro-benchmark method to call JaCoCoSplitter;
- **test/**: contains all the unit-test created;
  - **listener/**: contains the classes that collect data from unit-tests;
    - **JaCoCoSplit.java**: write the execution data of JaCoCo from the JMX Agent Stream to an .exec file, for each 
unit-test method;
    - **JaCoCoListener.java**: intercept the end of an execution of a unit-test method to call JaCoCoSplitter;

Doing a complete build (**mvn clean install**) of the system during this phase, gives in output the following files
for the analyzed software and placed in sub folders of the build target directory:
- **target/**:
  - **jacoco-junit/**: contains the .exec file, for each single unit-test method overall coverage;
  - **surefire-reports/**: contains the .xml file, for each single unit-test method execution time.

After the build is completed, the micro-benchmarks can be executed through the shaded jar created in the target 
directory. In order to collect all the data from the micro-benchmarks, two executions are necessary:
1. **Execution for coverage data**: the micro-benchmarks are executed with a minimal configuration, to collect the
coverage data;
2. **Execution for execution times**: the micro-benchmarks are executed with a complete configuration, to collect the
execution times.

For these two executions, there are scripts described below.

To obtain the total number of lines of code to analyze, [cloc](https://github.com/AlDanial/cloc) is used with the command:
> cloc directory_to_analyze --include-lang=Java --json --out=total-lines-directory-name.json

The execution of these two scripts gives in output the respective files for the analyzed software:
- **target/**:
  - **jacoco-jmh/**: contains the .exec file, for each single micro-benchmark method overall coverage;
  - **time-reports/**: contains the .json file, for each single micro-benchmark method execution time.

This phase is supported by the scripts located in the **scripts/** folder:
- **generate_reports.sh**: generates a JaCoCo report for each single .exec file of the unit-test or micro-benchmark methods;
- **merge_execs_reports.sh**: generate an overall JaCoCo report, merging all the .exec files;
- **custom_jar.sh**: creates a custom jar, in case the shade plugin did not add BenchmarkList and CompilerHints file;
- **run_benchs_for_coverage.sh**: run the micro-benchmarks to collect coverage data;
- **prepare_system.sh**: stops unnecessary services of the operating system, to run micro-benchmarks in best conditions;
- **run_benchs_for_times.sh**: run the micro-benchmarks to collect the execution times data;
- **restore_system.sh**: restore the services previously stopped.

Before executing these script, they must be copied inside the root folder of the software to be analyzed. These scripts
are intended to be executed after each test run for the coverage (after the build of the system and after executing the 
**run_benchs_for_coverage.sh** script)

### 2. Data Preparing
Once obtained the initial data, the data are prepared to the correct format used by the algorithms that optimize 
the selection. In this phase, 'Program' is used as a synonymum for 'Software', and test case is a generic name used
to identify both unit-test and micro-benchmark method. In order to correctly prepare data
the raw data needs to be placed inside the **data/raw/** folder. Due to the heavy size of all the reports file, this
folder is not uploaded on this repo. But you can reconstruct it for your executions, following the directory structure
described here:
- **ProgramName/**:
  - **junit/**:
    - **jacoco-junit-xml/**: contains the .xml report for every unit-test method coverage data;
    - **surefire-reports/**: contains the .xml report for every unit-test method execution time;
    - **total-lines.json**: contains the number of source code line for the analyzed software component.
  - **jmh/**:
    - **jacoco-jmh-xml/**: contains the .xml report for every micro-benchmark method coverage data;
    - **time-reports/**: contains the .xml report for every micro-benchmark method execution time;
    - **total-lines.json**: contains the number of source code line for the analyzed software component.

The **dataprep.py** module contains different functions to prepare the data:
1. **create_coverage_matrix**: given the raw data for coverage, create a coverage matrix json file, such that a key
represents a test case method and the value a list of class lines covered by the test case method;
2. **create_time_matrix**: given the raw data for execution times, create a time matrix json file, such that a key
represents the test case method and the value the execution time of that test case method;
3. **coverage_matrix_reverse**: given the coverage matrix, reverses it to have a new representation that for 
every class line, shows the correspondent test cases;
4. **map_testcases_to_number**: given the time matrix, maps every test case name to a numerical identifier;
5. **maps_classlines_to_number**: given the coverage matrix, maps every class line to a numerical identifier;
6. **map_coverage_matrix_keys_to_testcase_number**: given the coverage matrix, changes the test case name key to the 
mapped numerical identifiers;
7. **map_coverage_matrix_element_entries_to_classline_number**: given the coverage matrix, changes the classlines elements
to their respective numerical identifier;
8. **map_coverage_matrix_reverse_keys_to_classline_number**: given the coverage matrix reversed, changes the classlines
key to the mapped numerical identifier;
9. **map_coverage_matrix_reverse_element_entries_to_testcase_number**: given the coverage matrix reversed, changes the
testcases elements to their respective numerical identifier;
10. **map_time_matrix_keys_to_testcase_number**: given the time matrix, changes the test case name key to the mapped
numerical identifiers;
11. **plain_from_coverage_matrix**: given the coverage matrix, converts it in a plain text (.txt) format, where every 
line number is the numerical identifier of a test case and the line content is the list of covered class lines;
12. **plain_from_time_matrix**: given the time matrix, converts it in a plain text file (.txt), where every element is an
execution time and its position maps the numerical identifier of the test case;
13. **csv_from_coverage_matrix_and_time_matrix**: given the coverage matrix and time matrix, creates a table format file
(.csv), with the execution times of every test case and the correspondent line coverage percentage;
14. **merge**: each output of the previous functions (except the .txt and .csv ones) and the **total-lines.json** file 
are merged to create various final json files with every software data.
15. **search_covered_method_lines**: given the coverage matrix reversed and a list of class lines, searches if a line
is covered by a test case, filtering the coverage matrix reversed;
16. **filter_testcases_with_no_coverage**: after the data are processed, searches if a test has no coverage. This
ensures that the matrix have no "holes" that could interfere the algorithm executions. If this function finds any tests,
the data have to be re-generated.

This phase gives in output the files for each analyzed program and placed in **data/processed/** folder. The main files
necessary for the algorithm are the following:
- **ProgramName/**:
  - **coverage_matrix.json**;
  - **time_matrix.json**;
  - **coverage_matrix_reversed.json**
  - **testcases_number.json**
  - **classlines_number.json**;
  - **test_coverage_line_by_line.json**
  - **executed_lines_test_by_test.json**
  - **test_cases_costs.json**;
  - **ProgramName_cost.txt**
  - **ProgramName_coverage.txt**
  - **ProgramName.csv**

The json files are merged together for all programs and placed in **data/merged/** folder:
  - **executed_lines_test_by_test_all_programs.json**;
  - **test_coverage_line_by_line_all_programs.json**;
  - **test_cases_costs_all_programs.json**;
  - **total_program_lines_all_programs.json**.

This phase can be automated by the **dataprep.py** script located in the **data/** folder. To execute this script run
these commands inside the folder:
> python dataprep.py junit #to prepare data from junit raw data
> 
> python dataprep.py jmh #to prepare data from jmh raw data

### 3. Generation of tests and benchmarks
The software to analyze can miss some of the tests and benchmarks classes that are necessary to correctly find the 
issues. Not having a real knowledge of the system is a threat to manually writing tests and benchmarks. For these reasons
LLMs are being used to complete the suites of tests and benchmarks. A prompt engineering process has been designed for
this phase, involving the generation of an "ideal prompt" with meta-prompting techniques. Two main prompt are created
throught their respecitve conversations with Copilot Web (Claude Sonnet 4.5), one for [junit classes](https://github.com/copilot/share/482153b0-4ae4-88e3-b011-8e4b207549ba) 
and one for [jmh classes](https://github.com/copilot/share/42084032-03e0-88c5-a812-9c02003500b9). The obtained prompt
are then involved in an ablation phase, to pick the best variant of the prompt:
- **Ablation 0**: The prompt includes all the chosen prompt patterns (persona and few-shot);
- **Ablation 1**: The prompt keeps the persona pattern but removes few-shot;
- **Ablation 2**: The prompt removes the persona pattern but keeps few-shot;
- **Ablation 3**: The prompt removes all the chosen prompt patterns.

Every variant is used with Copilot CLI (Claude Sonnet 4.5), on a five generation of a test and benchmark suite for a 
chosen production class. The best variant (the ones that generates more test and benchmark methods on the five generations) 
is the picked one to generate all the other necessary benchmarks and tests. When this approach encountered issues with
the generation, a "fallback" strategy is used with Copilot IDE Plugin (GPT-5 mini) and a very simple prompt that directly
asks the LLM to generate a test class for the given production class or to convert a test class in a benchmark one.

For this phase, there are two scripts:
- **generate_ju-test.sh**: generate a junit test class given the info about the production class and the prompt variant to use;
- **generate_jmh-bench.sh**: generate a jmh bench class given the info about the production class and the prompt variant to use;

All the generated tests and benchmarks for the ablations evaluation are located inside the **ablations/** folder

### 4. Algorithms Execution
The data are ready to be used as input for the chosen algorithms that are placed in **algorithms/** folder:

**add_greedy/**: Contains the Additional Greedy implementation. This algorithm creates a subset of the original test 
suite, selecting a test case to insert in the subset by how "convenient" is to add it, given the additional statement 
coverage and additional execution time that gives to the subset. 
The input files for this algorithm are:
- **test_coverage_line_by_line_all_programs.json**;
- **test_cases_costs_all_programs.json**.

To execute this algorithm run this command inside the folder:
> python add_greedy.py junit #analyze junit data
> 
> python add_greedy.py jmh #analyze jmh data

**div_ga/**: Contains the DIV-GA implementation. This algorithm creates a subset of the original test suite, selecting 
a test case to insert in the subset by the best fitness function, based on statement coverage and execution time
computed at every iteration of the genetic routine.
The input files for this algorithm are:
- **\<ProgramName>_cost.txt**;
- **\<ProgramName>_coverage.txt**.

These files have to be copied inside the algorithm folder.

To execute this algorithm run these commands inside the folder:
> matlab -nodisplay -nosplash -nodesktop -r "run('DIVGA_junit.m');exit;" #analyze junit data
> 
> matlab -nodisplay -nosplash -nodesktop -r "run('DIVGA_jmh.m');exit;" #analyze jmh data

**igdec_qaoa**: Contains the IGDec-QAOA implementation. This algorithm creates a subset of the original test suite, 
selecting a test case to insert in the subset by how it "impacts" the fitness function value.
The input files for this algorithm are:
- **\<ProgramName>.csv**.

To execute this algorithm run these commands inside the folder:
> python noise_igdec_qaoa_tcs.py junit #noise simulator (junit data)
> 
> python noise_igdec_qaoa_tcs.py jmh #noise simulator (jmh data)

**qaoa_tcs/**: Contains the QAOA-TCS implementation. This algorithm creates a subset of the original test suite 
selecting a test case to insert in the subset from the created clusters, by a similarity function.
The input files for this algorithm are:
  - **executed_lines_test_by_test_all_programs.json**;
  - **test_coverage_line_by_line_all_programs.json**;
  - **test_cases_costs_all_programs.json**;
  - **total_program_lines_all_programs.json**

To execute this algorithm run these command inside the folder:
> python qaoa_tcs.py noise junit #noise simulator (junit data)
> 
> python qaoa_tcs.py noise jmh #noise simulator (jmh data)

This phase can be automated by the **run_algorithms.sh** script located in the **scripts/** folder, that also automates
the data processing phase.

All the algorithms output are contained in the **results/** folder. The folder also contains Plotly-generated HTML
visualization of clusters created by qaoa_tcs algorithm Note that GitHub language statistics excludes these HTML files.

### 6. Statistical Analysis
In order to compare the algorithms output, they are prepared to a csv format, where each column represent the means of 
all pareto fronts produced. This step is automated with the script **meanarr.py** located inside the **stats/** folder. 
To execute this script run this command inside the folder:

> python meanarr.py

Once the data are prepared, statistical tests are executed to formally compare the algorithms:
- **Shapiro-Wilk**: checks if all the columns follow the normal distribution. 
Then other three test are executed to check if there are significant statistical difference between the possible pair of
columns combination, which are the pairs to differ and to then quantify this difference. Depending on the obtained
distribution, two different groups of three tests are executed:
- **If Shapiro-Wilk finds a normal distribution for all the columns**: then Anova, Tukey, Cohen-d tests are executed;
- **If Shapiro-Wilk does not find a normal distribution for all the columns**: then Kruskal-Wallis, Dunn, 
Vargha-Delaney A tests are executed.

This second step is automated with **stats.r** script. To execute this script run this command inside the folder:

> Rscript stats.r
 
All the statistical data are written inside the specific sheet file for the analyzed system and the analyzed metric.

The entire phase can be automated with **run_stats.sh** script located in the **scripts/** folder.

### 7. Obtained results
All the previous phases are applied on two real systems. These systems have known performance issues and the goal is
to apply test case selection and see if the selected tests can detect the issue and then the fix. The systems are 
analyzed on two states doing a checkout on some precise commits:
- **Pre-fix** commit: At this commit, the performance issue is still present in the system; 
- **Post-fix** commit: At this commit, the performance has just been fixed;

The chosen software are from the Apache Software Foundation:
- **Avro**: the **avro** module is analyzed;
- **Hive**: the **standalone-metastore-common** module is analyzed;

The collected data have been compared after executing the algorithms, to evaluate two different approaches:
- **"A monte" execution**: The algorithms are executed on the junit data, to obtain a subset of unit tests that they
will be converted to micro benchmarks wrapping the junit tests, with [junit-to-jmh](https://github.com/alniniclas/junit-to-jmh);
- **"A valle" execution**: The micro-benchmarks are directly generated from source code with LLMs. The algorithms are
executed on the jmh data, to obtain a subset of micro-benchmarks.