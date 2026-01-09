library(tidyverse)
library(stringr)
library(car)
library(dplyr)
library(lsr)
library(FSA)
library(effsize)
library(readODS)

run_tests <- function(filename, df, tool, program, algorithm) {
  print(paste("Statistic tests for",program,"and",tool,"data",sep = " "))

  df_norm_stats <- data.frame(algorithm=c(), statistic=c(), p_value=c())

  all_normal <- 0

  # SHAPIRO-WILK
  for(i in length(algorithm)) {
    tryCatch(
      expr = {
        df_norm <- shapiro.test(df[, algorithm[i]])
        if(df_norm$p.value > 0.05){
          all_normal <- all_normal + 1
        }
        print(df_norm)
        df_norm_stats <- rbind(df_norm_stats, list(algorithm[1], df_norm$statistic, df_norm$p.value))
      },
      error = function (e) {
        print(e)
        df_norm_stats <<- rbind(df_norm_stats, list(algorithm[1], "-", "-"))
        df <<- df[, -which(names(df) == algorithm[1])]
      }
    )
  }
  sheet_name <- paste(df_norm$method, tool, sep = " ")
  names(df_norm_stats) <- c("algorithm", "statistics", "p_value")
  if(file.exists(filename)){
    write_ods(df_norm_stats, filename, sheet = sheet_name, append = TRUE)
  } else {
    write_ods(df_norm_stats, filename, sheet = sheet_name)
  }

  if (all_normal == length(algorithm)) {
    print("All the groups follow a normal distribution")
    # ANOVA, TUKEY, COHEN-D
    print("To do ANOVA, TUKEY, COHEN-D")
  } else {
    print("Not all the groups follow a normal distribution")
    # decompose the dataframe subset in group - values columns to adapt the data for the tests
    df_long <- df %>%  pivot_longer(cols = everything(), names_to = "algorithm", values_to = "value")
    df_long$algorithm <- as.factor(df_long$algorithm)
    # KRUSKAL-WALLIS
    kruskal_result <- kruskal.test(value ~ algorithm, data = df_long)
    print("--- Kruskal-Wallis Test Results ---")
    print(kruskal_result)
    statistics <- c(kruskal_result$statistic)
    parameter <- c(kruskal_result$parameter)
    p_value <- c(kruskal_result$p.val)
    kruskal_df <- data.frame(statistics, parameter, p_value)
    sheet_name <- paste(kruskal_result$method, tool, sep = " ")
    write_ods(kruskal_df, filename, sheet = sheet_name, append=TRUE)
    # DUNN
    dunn_result <- dunnTest(value ~ algorithm, data= df_long, method="bonferroni")
    print("--- Dunn Test Results ---")
    print(dunn_result)
    sheet_name <- paste("Dunn (1964) Kruskal-Wallis multiple comparison", tool, sep = " ")
    write_ods(dunn_result$res, filename, sheet = sheet_name, append=TRUE)
    res_dunn <- dunn_result$res
    # VARGHA DELANEY A
    vargha_df <- data.frame(comparison=c(), estimate=c(), magnitude=c())
    for (comp in res_dunn$Comparison) {
      els <- str_split(comp, " - ")
      for (el in els) {
        print(el)
        diff <- VD.A(df[, el[1]], df[, el[2]])
        print(diff)
        print(diff$estimate)
        if (diff$estimate < 0.5){
          vargha_df <- rbind(vargha_df, list(comp, diff$estimate, diff$magnitude))
          print("Reversing Varga and Delaney A comparison due to zero estimate")
          comprev <- paste(el[2], el[1], sep = " - ")
          print(comprev)
          diffrev <- VD.A(df[, el[2]], df[, el[1]])
          print(diffrev)
          vargha_df <- rbind(vargha_df, list(comprev, diffrev$estimate, diffrev$magnitude))
        } else {
          vargha_df <- rbind(vargha_df, list(comp, diff$estimate, diff$magnitude))
        }
      }
    }
    names(vargha_df) <- c("comparison", "estimate", "magnitude")
    print(vargha_df)
    sheet_name <- paste("Vargha and Delaney A", tool, sep = " ")
    write_ods(vargha_df, filename, sheet = sheet_name, append=TRUE)
  }
}

prepare_and_run_tests <- function(csv_file, filename, program, tool) {
  algorithm <- c("add.greedy", "divga", "igdecn", "qaoan")

  sdf <- csv_file[, c(paste0(program,"_",tool,"_",algorithm[1]), paste0(program,"_",tool,"_",algorithm[2]),
                      paste0(program,"_",tool,"_",algorithm[3]), paste0(program,"_",tool,"_",algorithm[4])) ]

  names(sdf) <- algorithm

  run_tests(filename, sdf, paste0("(",tool,")"), program, algorithm)
}

# load coverage csv
means_coverage <- read.csv("means_coverage.csv", header=TRUE, stringsAsFactors=FALSE)
# statistical tests for coverage data
prepare_and_run_tests(means_coverage, "avro_coverage_stats.ods", "avro", "ju")
prepare_and_run_tests(means_coverage, "avro_coverage_stats.ods", "avro", "jmh")
prepare_and_run_tests(means_coverage, "hive_coverage_stats.ods", "hive", "ju")
prepare_and_run_tests(means_coverage, "hive_coverage_stats.ods", "hive", "jmh")

# load costs csv
means_costs <- read.csv("means_costs.csv", header=TRUE, stringsAsFactors=FALSE)
# statistical tests for execution costs data
prepare_and_run_tests(means_costs, "avro_costs_stats.ods", "avro", "ju")
prepare_and_run_tests(means_costs, "avro_costs_stats.ods", "avro", "jmh")
prepare_and_run_tests(means_costs, "hive_costs_stats.ods", "hive", "ju")
prepare_and_run_tests(means_costs, "hive_costs_stats.ods", "hive", "jmh")