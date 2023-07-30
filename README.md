# A Fast Hop-Biased Approximation Algorithm for the Quadratic Group Steiner Tree Problem
[![Contributions Welcome](https://img.shields.io/badge/Contributions-Welcome-brightgreen.svg?style=flat-square)](https://github.com/nju-websoft/QGSTP-HB/issues)
[![License](https://img.shields.io/badge/License-Apache-lightgrey.svg?style=flat-square)](https://github.com/nju-websoft/QGSTP-HB/blob/main/LICENSE)
[![language-java](https://img.shields.io/badge/Language-Java-yellow.svg?style=flat-square)](https://www.java.com)
[![Maven Central](https://img.shields.io/maven-central/v/foundation.icon/icon-sdk)](https://search.maven.org/artifact/foundation.icon/icon-sdk)

This is the source code of the paper 'A Fast Hop-Biased Approximation Algorithm for the Quadratic Group Steiner Tree Problem'.

## Table of contents

+ Directory Structure
+ Environment
+ Data
  + Hub Label Construction
+ Run the HB Algorithm
  + Efficiency Experiment (Table 2)
  + Scalability Experiment (Figure 3 and Figure 4)
  + Effectiveness Experiment (Table 3)
  + Ablation Study (Table 4)
+ Other Algorithms
+ License
+ Citation

## Directory Structure
Directory /src/main/java contains all the source code based on JDK 11.

+ Directory /src/main/java/graphtheory contains our implementation of the HB algorithm

+ Directory /src/main/java/driver/data includes some classes for generating graph data and queries

+ Directory /src/main/java/driver/work conducts the experiments for the HB algorithm
  
+ Directory /src/main/java/mytools consists of some tools

+ Directory /src/main/resources provides a sample configuration to run this project


## Environment

+ MySQL
+ JDK11
+ Maven
+ 180G Memory

## Data
Our dataset is available on [![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.7784147.svg)](https://zenodo.org/record/7784147).

Import the data to your MySQL database.


### Hub Label Construction

Unfortunately, due to the limit of space, we could not directly provide the data of hub labeling for `DBpedia` and `LUBM-250U`. You might take a long time to construct the hub labeling. Otherwise, if you don't want to do that, you may choose to run the simple script (marked by `_simple`) to run the algorithm without `DBpedia` and `LUBM-250U`.

First set the configuration `/src/main/resources/config.properties` as follows:
+ `IP`, `PORT` : the IP and the port to connect to your MySQL database,
+ `USER`,`PASS` : the username and the password to log in your MySQL database.

Then run the following script (it might cost a very long time):
```shell
bash Construct_Hub_Label.sh
```

## Run the HB Algorithm

First set the configuration `/src/main/resources/config.properties` as follows:
+ `IP`, `PORT` : the IP and the port to connect to your MySQL database,
+ `USER`,`PASS` : the username and the password to log in your MySQL database.


### Efficiency Experiment (Table 2) 
Run the following script:
```shell
bash Experiment_for_Table_2.sh
```

For each graph, the result for each query is record in file `Table_2_[graph].log`. For example, `Table_2_mondial.log` contains the results for all 39 queries, include $\alpha=0.1$, $\alpha=0.5$ and $\alpha=0.9$. And we can find the query result for query 1 like:
```
Expr 1:
Generating keyword...
Running algorithm...
Finish!
The number of graph nodes is 207631, the number of graph edges is 811556.
The number of keyword is 2.
The size of the smallest group is 100.
Algorithm: HB
alpha= 0.1
Runtime: 0.169 s.
Complete! cost(T)=0.7636719283306959.
```
After obtaining the runtime for each query, we can calculate the average runtime in Table 2.


### Scalability Experiment (Figure 3 and Figure 4)
Run the following script:
```shell
bash Experiment_for_Figure_3_and_4.sh
```

Then we get the similar result `Figure_3_and_4_[graph].log`. After obtaining the runtime, the number of groups (g) and the size of the smallest group (f), we can draw Figure 3 and 4.


### Effectiveness Experiment (Table 3)
Run the following script:
```shell
bash Experiment_for_Table_3.sh
```

Then we get the similar result `Table_3_[graph].log`. We can calculate empirical approximation ratio in Table 3 by combining the cost of the answer to each query, and the optimum answer (computed by B3F additionally).

### Ablation Study (Table 4)
Run the following scripts:
```shell
bash Experiment_for_Table_4.sh
```

The result is recorded in `Table_4_woPruning_[graph].log`, `Table_4_woPP_[graph].log`, `Table_4_woPI_[graph].log`. After obtaining the runtime for each query under each setting, we can calculate the average runtime in Table 4.

## Other Algorithms

Please refer to the following repositories if you want to run other algorithms in our experiments:

+ the EO algorithm: [QGSTP](https://github.com/nju-websoft/QGSTP),
+ the B3F algorithm: [B3F](https://github.com/nju-websoft/B3F).


## License
This project is licensed under the GPL License - see the [LICENSE](LICENSE) file for details

## Citation
If you think our algorithms or our experimental results are useful, please kindly cite our paper.


