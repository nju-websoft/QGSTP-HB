#!/usr/bin/python
# -*- coding: UTF-8 -*-
from tabnanny import verbose
from pyrdf2vec.graphs import KG
from pyrdf2vec.samplers import UniformSampler
from pyrdf2vec.walkers import RandomWalker
from pyrdf2vec import RDF2VecTransformer
from pyrdf2vec.embedders import Word2Vec
from pyrdf2vec.graphs.vertex import Vertex
import rdflib
import pandas as pd
import pymysql


IP="localhost"
PORT=9961


def construct_nodevec_old(graph,nodetable,output):
    print("Reading KG...")
    kg = KG(graph, label_predicates=[])
    
    print("Construct RDF2VecTransformer...")
    transformer = RDF2VecTransformer(embedder=Word2Vec(size=10),walkers=[RandomWalker(2, 50, UniformSampler())])
    
    print("Reading notetable...")
    entities=[]
    iri=[]
    f=open(nodetable,"r",encoding='UTF-8')
    while True:
        line=f.readline()
        if not line:
            break
        else:
            entities.append( rdflib.URIRef(line[:len(line)-1]) )
            iri.append(line[:len(line)-1])
    f.close()
#print(entities)
    print("Generate embeddings...")
# Entities should be a list of URIs that can be found in the Knowledge Graph
    embeddings = transformer.fit_transform(kg, entities)
    
    print("Write out...")
    with open(output,"w",encoding='UTF-8') as f:
        for i in range(len(embeddings)):
            f.write(iri[i]+" "+" ".join(str(k) for k in embeddings[i])+'\n')

def construct_nodevec(graph,dbname,demension):
    print("Reading from database...")
    conn=pymysql.connect(
        host=IP,
        database=dbname,
        user="qkoqhh",
        password="WangXiaoQing",
        port=PORT,
        charset="utf8"
    )
    context=pd.read_sql("select id,name from nodes",conn).values
    conn.close()
    n=len(context)
    entities=[""]*n
    for row in context:
        entities[row[0]]=row[1]

    print("Reading KG...")
    kg = KG(graph)
    # for entity in entities:
    #     if not Vertex(entity) in kg._entities:
    #         print(entity)
    
    print("Construct RDF2VecTransformer...")
    transformer = RDF2VecTransformer(embedder=Word2Vec(vector_size=demension),walkers=[RandomWalker(2, 50, UniformSampler())],verbose=2)
    
    print("Generate embeddings...")
# TBD
# Entities should be a list of URIs that can be found in the Knowledge Graph
    embeddings, literals = transformer.fit_transform(kg, entities)

    print("Write back to database...")
    conn=pymysql.connect(
        host=IP,
        database=dbname,
        user="qkoqhh",
        password="WangXiaoQing",
        port=PORT,
        charset="utf8"
    )
    cur=conn.cursor()
    cur.execute(
        "create table nodevec(" +
        "    `id` int not null," +
        "    `dimension` int not null," +
        "    `value` double not null" +
        ")" +
        "engine=archive"
    )
    conn.commit()

    sql="insert into nodevec values (%s,%s,%s)"
    val=[]

    for i in range(n):
        for j in range(demension):
            val.append((i,j,float(embeddings[i][j])))
    cur.executemany(sql,val)
    cur.close()
    conn.commit()
    conn.close()
    

    

if __name__ == "__main__":
    pwd="D:\\work\\resources\\"
    # dbname="opencyc"
    # construct_nodevec(pwd+"open-cyc-mod.rdf",dbname,10)
    # dbname="lmdb"
    # construct_nodevec(pwd+"linkedmdb\\test.nt",dbname,10)
    dbname="mondial"
    construct_nodevec(pwd+"mondial-mod.nt",dbname,10)
    # dbname="dbpedia_6m"
    # construct_nodevec(pwd+"dbp\\mod-mappingbased_objects_en.ttl",dbname,10)