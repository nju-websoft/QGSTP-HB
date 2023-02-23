#!/usr/bin/env bash
mvn clean package

graphs=("dbpedia" "lubm_250u")
#graphs=("dbpedia_50k")

n=${#graphs[@]}

for ((i=0;i<n;i++)) do
  cp ./src/main/resources/config.properties my.properties
  sed -i "/DATABASE/d;1i DATABASE=${graphs[i]}" my.properties
  sed -i "/woPR/d;1i woPR=TRUE" my.properties
  sed -i "/woPP/d;1i woPP=TRUE" my.properties
  sed -i "/woPI/d;1i woPI=TRUE" my.properties
  sed -i "/DEBUG/d;1i DEBUG=TRUE" my.properties
  java -Xmx180g -cp target/QGSTP-jar-with-dependencies.jar:. driver.data.Run1 -c my.properties -p GenerateHubLabel
done