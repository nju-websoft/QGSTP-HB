#!/usr/bin/env bash
#mvn clean package

#graphs=('yago' 'dbpedia')
#queries=(32 330)
queries=(183)
graphs=("dbpedia_50k")

condiction=('PR' 'PP' 'PI')
n=${#graphs[@]}


for ((i=0;i<n;i++)) do
  for ((j=0;j<=2;j++)) do
    cp ./src/main/resources/config.properties my.properties
    sed -i "/DATABASE/d;1i DATABASE=${graphs[i]}" my.properties
    sed -i "/QUERY_NUM/d;1i QUERY_NUM=${queries[i]}" my.properties
    sed -i "/wo${condiction[j]}/d;1i wo${condiction[j]}=TRUE" my.properties
    java -Xmx180g -cp target/QGSTP-jar-with-dependencies.jar:. driver.work.Run -c my.properties > Table_6_wo${condiction[j]}_${graphs[i]}.log
  done
done