#!/bin/bash

mvn exec:java -Dexec.mainClass=org.hsqldb.server.Server -f pom.xml 