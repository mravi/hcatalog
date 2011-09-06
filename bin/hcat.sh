#!/usr/bin/env bash

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Resolve our absolute path                                                      
# resolve links - $0 may be a softlink                                           
this="${BASH_SOURCE-$0}"                                                         
while [ -h "$this" ]; do                                                         
    ls=`ls -ld "$this"`                                                          
    link=`expr "$ls" : '.*-> \(.*\)$'`                                           
    if expr "$link" : '.*/.*' > /dev/null; then                                  
        this="$link"                                                             
    else                                                                         
        this=`dirname "$this"`/"$link"                                           
    fi                                                                           
done                                                                             
                                                                                 
# convert relative path to absolute path                                         
bin=`dirname "$this"`                                                            
script=`basename "$this"`                                                        
bin=`unset CDPATH; cd "$bin"; pwd`                                               
this="$bin/$script"                                                              
                                                                                 
# the root of the HCatalog installation                                          
export HCAT_HOME=`dirname "$this"`/..                                            

# filter debug command line parameter
debug=false

for f in $@; do
     if [[ $f = "-secretDebugCmd" ]]; then
        debug=true
     else
        remaining="${remaining} $f"
     fi
done

# Find our hcatalog jar
HCAT_JAR=$HCAT_HOME/lib/hcatalog-*.jar

# Add all of the other jars to our classpath
for jar in $HCAT_HOME/lib/*.jar ; do
	HADOOP_CLASSPATH=$HADOOP_CLASSPATH:$jar
done

# Put our config file in the classpath
HADOOP_CLASSPATH=${HADOOP_CLASSPATH}:${HCAT_HOME}/conf

export HADOOP_CLASSPATH=$HADOOP_CLASSPATH

# run it
if [ "$debug" == "true" ]; then
	echo "Would run:"
	echo "exec $HADOOP_HOME/bin/hadoop jar $HCAT_JAR org.apache.hcatalog.cli.HCatCli $remaining"
	echo "with HADOOP_CLASSPATH set to ($HADOOP_CLASSPATH)"
	echo "and HADOOP_OPTS set to ($HADOOP_OPTS)"
else
	exec $HADOOP_HOME/bin/hadoop jar  $HCAT_JAR org.apache.hcatalog.cli.HCatCli "$@"
fi

