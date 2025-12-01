# ERUPT

**ERUPT** (**E**xploring the **R**ole of **U**nit tests for **P**erformance regression **T**esting optimization)
is a study that analyzes how unit tests metrics (statement coverage and execution times) can support the optimization 
of a regression testing campaign in the context of Performance Testing.

## Methodology Workflow

### 0. Configuration Setup
In order to execute the next phases of the workflow, a setup of the execution environment is necessary. 

For the Java classes, there is a configuration file inside the **java/** folder, for each build system.
Starting from the initial configuration file, it has to be adapted to the one of the analyzed software. 
The Java package structure described on the next phase, is working for a Maven Project.

For the Python modules and algorithms, there is a requirements.txt inside the root of this repository.

DIV-GA algorithms requires MATLAB R2025a and Global Optimization Toolbox.

### 1. Data Collection
Some software will be chosen to retrieve the data and compute the metrics. The **java/** folder contains all the
necessary code, organized in packages, that is necessary to add to the software to analyze it and collect the data:
- **benchmarks/**: contains all the micro-benchmarks created;
  - **profiler/**: contains the classes to collect data from micro-benchmarks;
    - **JaCoCoSplit.java**: write the execution data of JaCoCo from the JMX Agent Stream to an .exec file, for each test method;
    - **JaCoCoProfiler.java**: intercept the end of an iteration of a micro-benchmark method to call JaCoCoSplitter;
- **test/**: contains all the unit-test created;
  - **listener/**: contains the classes to collect data from unit-tests;
    - **JaCoCoSplit.java**: write the execution data of JaCoCo from the JMX Agent Stream to an .exec file, for each test method;
    - **JaCoCoListener.java**: intercept the end of an execution of a unit-test method to call JaCoCoSplitter;

Doing a complete build (mvn clean install) of the system during this phase, gives in output the following files
for each analyzed software and placed in sub folders of the target directory:
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

This phase is supported by the scripts located in the **scripts/** folder:
- **generate_reports.sh**: generates a JaCoCo report for each single .exec file of the test methods;
- **merge_execs_reports.sh**: generate an overall JaCoCo report, merging all the .exec files of the test methods;
- **custom_jar.sh**: creates a custom jar, in case the shade plugin did not add BenchmarkList and CompilerHints file;
- **run_benchs_for_coverage.sh**: run the micro-benchmarks to collect coverage data;
- **prepare_system.sh**: stops unnecessary services of the operating system, to run micro-benchmarks in best conditions;
- **run_benchs_for_times.sh**: run the micro-benchmarks to collect the execution times data;
- **restore_system.sh**: restore the services previously stopped.

Before executing these script, they must be copied inside the root folder of the software to be analyzed. The firs two
scripts are intended to be executed after each test run for the coverage (after the build of the system and after
executing the run_benchs_for_coverage.sh script)

### 2. Data Preparing
Once obtained the initial data, the data are prepared to the correct format used by the algorithms that optimize 
the selection. In this phase, 'Program' is used as a synonymum for 'Software'. In order to correctly prepare data
the raw data needs to be placed inside the **data/raw/** folder:
- **ProgramName/**:
  - **junit/**:
    - **jacoco-junit-xml/**: contains the .xml report for every unit test method coverage data;
    - **surefire-reports/**: contains the .xml report for every unit test method execution time;
    - **total-lines.json**: contains the number of source code line for the analyzed software component.
  - **jmh/**:
    - **jacoco-jmh-xml/**: contains the .xml report for every micro-benchmark method coverage data;
    - **time-reports/**: contains the .xml report for every micro-benchmark method execution time;
    - **total-lines.json**: contains the number of source code line for the analyzed software component.

The **dataprep.py** module contains different functions to prepare the data:
1. **create_coverage_matrix**: given the raw data for coverage, create a coverage matrix json file, such that a key
represents the test case method and the value a list of class lines covered by the test case method;
2. **create_time_matrix**: given the raw data for execution times, creat a time matrix json file, such that a key
represents the test case method and the value the execution time of that test case method;
3. **coverage_matrix_reverse**: given the coverage matrix, reverses it to have a new representation that for 
every class line, shows the correspondent test cases;
3. **map_testcases_to_number**: given the time matrix, maps every test case name to a numerical identifier;
4. **maps_classlines_to_number**: given the coverage matrix, maps every class line to a numerical identifier;
4. **map_coverage_matrix_keys_to_testcase_number**: given the coverage matrix, changes the test case name key to the 
mapped numerical identifiers;
5. **map_time_matrix_keys_to_testcase_number**: given the time matrix, changes the test case name key to the mapped
numerical identifiers;
6. **plain_from_coverage_matrix**: given the coverage matrix, converts it in a plain text (.txt) format, where every 
line number is the numerical identifier of a test case and the line content is the list of covered class lines;
7. **plain_from_time_matrix**: given the time matrix, converts it in a plain text file (.txt), where every element is an
execution time and its position maps the numerical identifier of the test case;
8. **csv_from_coverage_matrix_and_time_matrix**: given the coverage matrix and time matrix, creates a table format file
(.csv), with the execution times of every test case and the correspondent line coverage percentage;
9. **merge**: each output of the previous functions (except the .txt and .csv ones) and the **total-lines.json** file 
are merged to create various final json files with every software data.
10. **search_covered_method_lines**: given the coverage matrix reversed and a list of class lines, searches if a line
is covered by a test case, filtering the coverage matrix reversed.

This step give in output the following files for each analyzed program and placed in **data/processed/** folder:
- **ProgramName/**:
  - **coverage_matrix.json**;
  - **time_matrix.json**;
  - **coverage_matrix_reversed.json**
  - **testcases_number.json**
  - **classlines_number.json**;
  - **test_coverage_line_by_line.json**
  - **test_cases_costs.json**;
  - **ProgramName_costs.txt**
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

### 3. Algorithms Execution
The data are ready to be used as input for the chosen algorithms that are placed in **algorithms/** folder:

**add_greedy/**: Contains the Additional Greedy implementation. This algorithm creates a subset of the original test 
suite, selecting a test case to insert in the subset by how "convenient" is to add it, given the additional statement 
coverage and additional execution time that gives to the subset. 
The input files for this algorithm are:
- **test_coverage_line_by_line_all_programs.json**;
- **test_cases_costs_all_programs.json**.

To execute this algorithm run this command inside the folder:
> python add_greedy.py

**div_ga/**: Contains the DIV-GA implementation. This algorithm creates a subset of the original test suite, selecting 
a test case to insert in the subset by the best fitness function, based on statement coverage and execution time
computed at every iteration of the genetic routine.
The input files for this algorithm are:
- **\<ProgramName>_costs.txt**;
- **\<ProgramName>_coverage.txt**.

To execute this algorithm run this command inside the folder:
> matlab -nodisplay -nosplash -nodesktop -r "run('DIVGA.m');exit;"

**igdec_qaoa**: Contains the IGDec-QAOA implementation. This algorithm creates a subset of the original test suite, 
selecting a test case to insert in the subset by how it "impacts" the fitness function value.
The input files for this algorithm are:
- **\<ProgramName>.csv**.

To execute this algorithm run these commands inside the folder:
> python loch_qaoa_tcs.py #ideal simulator
> 
> python noise_loch_qaoa_tcs.py #noise simulator

**select_qaoa/**: Contains the Select-QAOA implementation. This algorithm creates a subset of the original test suite 
selecting a test case to insert in the subset from the created clusters, by a similarity function.
The input files for this algorithm are:
  - **executed_lines_test_by_test_all_programs.json**;
  - **test_coverage_line_by_line_all_programs.json**;
  - **test_cases_costs_all_programs.json**;
  - **total_program_lines_all_programs.json**

To execute this algorithm run these command inside the folder:
> python select_qaoa.py ideal #ideal simulator
> 
> python select_qaoa.py noise #noise simulator

This phase can be automated by the **run_algorithms.sh** script located in the **scripts/** folder, that also automates
the data processing phase.

### 4. Statistical Analysis
All the previous phases are applied on two real systems. These systems have known performance issues and the goal is
to apply test case selection and see if the selected tests can detect the issue and then the fix. The systems are analyzed
on two states doing a checkout on some precise commits:
- **Pre-fix** commit: At this commit, the performance issue is still present in the system; 
- **Post-fix** commit: At this commit, the performance has just been fixed;

The chosen software are from the Apache Software Foundation:
- **Avro**: the **avro** module is analyzed;
- **Hive**: the **standalone-metastore-common** module is analyzed;

The collected data are statistically compared after executing the algorithms in two different ways:
- **"A monte" execution**: The algorithms are executed on the junit data, to obtain a subset of unit tests and then
converted to micro benchmarks with ju2jmh;
- **"A valle" execution**: The micro-benchmarks are directly generated from source code with LLMs. The algorithms are
executed on the jmh data, to obtain a subset of micro-benchmarks.