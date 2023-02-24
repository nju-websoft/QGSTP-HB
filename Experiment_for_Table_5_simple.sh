#!/usr/bin/env bash
#mvn clean package

#graphs=('mondial')
#queries=(39)
queries=(183)
graphs=("dbpedia_50k")

n=${#graphs[@]}

for ((i=0;i<n;i++)) do
  cp ./src/main/resources/config.properties my.properties
  sed -i "/DATABASE/d;1i DATABASE=${graphs[i]}" my.properties
  sed -i "/QUERY_NUM/d;1i QUERY_NUM=${queries[i]}" my.properties
  sed -i "/woPR/d;1i woPR=TRUE" my.properties
  sed -i "/woPP/d;1i woPP=TRUE" my.properties
  sed -i "/woPI/d;1i woPI=TRUE" my.properties
  java -Xmx180g -cp target/QGSTP-jar-with-dependencies.jar:. driver.work.Run -c my.properties > Table_5_${graphs[i]}.log
done