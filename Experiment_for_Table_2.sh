#!/usr/bin/env bash
mvn clean package

graphs=('mondial' 'opencyc' 'lmdb' 'yago' 'dbpedia')
queries=(39 50 200 32 330)
#graphs=("dbpedia_50k")
#queries=(183)

n=${#graphs[@]}

for ((i=0;i<n;i++)) do
  cp ./src/main/resources/config.properties my.properties
  sed -i "/DATABASE/d;1i DATABASE=${graphs[i]}" my.properties
  sed -i "/QUERY_NUM/d;1i QUERY_NUM=${queries[i]}" my.properties
  java -Xmx180g -cp target/QGSTP-jar-with-dependencies.jar:. driver.work.Run -c my.properties > Table_2_${graphs[i]}.log
done