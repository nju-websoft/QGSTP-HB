#!/usr/bin/env bash
mvn clean package

graphs=('lubm_10u' 'lubm_50u' 'lubm_250u')
queries=(250 250 250)
#queries=(183)
#graphs=("dbpedia_50k")

n=${#graphs[@]}

for ((i=0;i<n;i++)) do
  cp ./src/main/resources/config.properties my.properties
  sed -i "/DATABASE/d;1i DATABASE=${graphs[i]}" my.properties
  sed -i "/QUERY_NUM/d;1i QUERY_NUM=${queries[i]}" my.properties
  java -Xmx180g -cp target/QGSTP-jar-with-dependencies.jar:. driver.work.Run -c my.properties > Figure_3_and_4_${graphs[i]}.log
done