# IRUPT

**IRUPT** (**I**nvestigating the **R**ole of **U**nit Tests for **P**erformance Regression **T**esting Optimization)
is a study that analyzes how unit tests metrics (statement coverage and execution times) can support the optimization 
of a regression testing campaign in the context of Performance Testing.

## Methodology Workflow

### 0. Configuration Setup
In order to execute the next phases of the workflow, a setup of the execution environment is necessary. 

For the Java classes, there is a configuration file inside the **java/** folder, for each build system.
Starting from the initial configuration file, it has to be adapted to the correspondent configuration file of the analyzed software. 
The Java package structure described on the next phase, is working for a Maven Project. For a Gradle Project there are some
slightly differences and is then necessary to duplicate some classes. For more info on the Java package structure, it is
possible to replicate the structure of the example projects, available on [MavenProjectJ4](https://github.com/Tensa53/MavenProjectJ4) 
and [GradleProjectJ4](https://github.com/Tensa53/GradleProjectJ4) repos.

For the Python modules and algorithms, there is a requirements.txt inside the root of this repository.

DIV-GA algorithms requires MATLAB 2025a and Global Optimization Toolbox.

### 1. Data Collection
Some software will be chosen to retrieve the data and compute the metrics. The **java/** folder contains all the
necessary code, organized in packages, that is necessary to add to the software to analyze it and collect the data:
- **benchmarks/**: contains all the micro-benchmarks created;
  - **profiler/**: contains the classes to collect data from micro-benchmarks;
    - **JaCoCoAppender.java**: appends the execution data of JaCoCo from the JMX Agent Stream, to the .exec file;
    - **JaCoCoCoverageMatrix.java**: gets the coverage data from the JMX Agent of JaCoCo, to build a coverage matrix;
    - **JaCoCoProfiler.java**: intercept the end of an iteration of a micro-benchmarks to call JaCoCoCoverageMatrix;
    - **MethodSignatureRetriever.java**: parse the current class, to retrieve the method signature at the given line;
- **test/**: contains all the unit-test created;
  - **listener/**: contains the JUnit listeners;
    - **JaCoCoListener.java**: intercept the end of an execution of a unit-test to call JaCoCoCoverageMatrix;
- **runners/**: contains custom classes called after the test phase of the system;
  - **JaCoCoXMLUncoveredMethods.java**: reads the JaCoCo XML Report and parses the signature of uncovered methods from tests;
  - **AverageSingleShotTime.java**: reads the JMH Json Report and extracts the average single shot time of all micro-benchmarks;
  - **SurefireXMLExecutionTimes.java**: reads the Surefire XML Report and extracts the execution time of all unit tests.

This phase gives in output the following files for each analyzed program and placed in **data/raw/** folder:
- **ProgramName/**:
  - **coverage-matrix.json**;
  - **report.json**;
  - **total-lines.json**.

This phase can be automated by the scripts located in the **scripts/** folder:
- **run_coverage_gradle.sh**: automates the steps of this phase, to retrieve the data from the unit-test of a Gradle project;
- **run_coverage_maven.sh**: automates the steps of this phase, to retrieve the data from the unit-test of a Maven project;
- **prepare_system.sh**: disable all unnecessary services of the operating system, to run the micro-benchmarks in the best conditions;
- **run_microbenchmarks.sh**: automated the steps of this phase, to retrieve the data from the micro-benchmarks of a Jar file;
- **restore_system.sh**: restore all the services previously stopped.

Before executing these script, they must be copied inside the root folder of the software to be analyzed.

### 2. Data Processing
Once obtained the initial data, the data are processed to the correct format used by the algorithms that optimize the selection.
The **dataprep.py** module contains different functions to process the data:
1. **coverage_matrix_split_covered_lines_in_multiple_list_items**: given the coverage-matrix.json file, splits the elements of a row, 
to have a new representation that for every test case, shows the correspondent covered methods single line
2. **coverage_matrix_splitted_reverse**: given the output of the previous function, reverses the matrix, to have a new representation
that for every line, shows the correspondent test cases;
3. **map_testcases_to_number**: given the report.json file, maps every testcase name to a numerical identifier;
4. **map_coverage_matrix_split_keys_to_testcase_number**: given the transformed matrix by the first function, changes 
the testcases name to the mapped numerical identifiers;
5. **map_time_report_to_testcase_number**: given the report.json file and the testcases name mapping to numerical id, maps the
execution time of a test case to the numerical identifier of the test case;
6. **plain_from_coverage_matrix_splitted**: given the transformed matrix by the first function, converts the matrix in a
plain text (.txt) format, where every line number is the numerical identifier of a test case and the line content is the
list of covered method lines by the test;
7. **plain_from_time_report**: given the report.json file, converts the time report in a plain text file (.txt), where every
element is an execution time and its position maps the numerical identifier of the test case;
8. **csv_from_coverage_matrix_splitted_and_time_report**: given the report.json file and the transformed matrix by the 
first function, creates a table format file (.csv), with the execution times
of every test case and the correspondent statement coverage percentage;
9. **merge**: the **total-lines.json** file and each output of the previous functions (except the .txt and .csv ones) to create
various final json files with every programs data.

This step give in output the following files for each analyzed program and placed in **data/processed/** folder:
- **ProgramName/**:
  - **test_coverage_line_by_line_str.json**;
  - **executed_lines_test_by_test.json**;
  - **testcases-number.json**
  - **test_coverage_line_by_line.json**;
  - **test_cases_costs.json**;
  - **\<ProgramName>_costs.txt**
  - **\<ProgramName>_coverage.txt**
  - **\<ProgramName>.csv**

The json files are merged together for all programs and placed in **data/merged/** folder:
  - **executed_lines_test_by_test_all_programs.json**;
  - **test_coverage_line_by_line_all_programs.json**;
  - **test_cases_costs_all_programs.json**;
  - **total_program_lines_all_programs.json**.

This phase can be automated by the **dataprep.py** script located in the **data/** folder.

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

### 4. Obtained Results
TO-DO