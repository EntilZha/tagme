#!/usr/bin/env bash

#javac -cp lib/*:ext_lib/*:bin/:/fs/clip-quiz/entilzha/linuxbrew/share/py4j/py4j0.10.6.jar TagmeEntryPoint.java
java -cp lib/*:ext_lib/*:bin/:/fs/clip-quiz/entilzha/linuxbrew/share/py4j/py4j0.10.6.jar -Xmx22G -Dtagme.config=./config.xml TagmeEntryPoint
