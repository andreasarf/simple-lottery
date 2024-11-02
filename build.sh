#!/bin/zsh

source ~/.zprofile

#gjavac --enable-preview -d build src/main/java/org/andreasarf/lottery/Main3.java \
#  && gjar --create --file App3.jar --main-class org.andreasarf.lottery.Main3 -C build . \
#  && native-image --enable-preview -jar App3.jar

#native-image -march=native --enable-preview --initialize-at-build-time=org.andreasarf.lottery.Main3 \
#  -cp target/simple-lottery-1.0-SNAPSHOT.jar -o App3 org.andreasarf.lottery.Main3

#gjava --enable-preview -XX:+UnlockExperimentalVMOptions -XX:+TrustFinalNonStaticFields -dsa -XX:+UseNUMA \
#   --class-path target/simple-lottery-1.0-SNAPSHOT.jar org.andreasarf.lottery.Main3 8

native-image -march=native --enable-preview --initialize-at-build-time=org.andreasarf.lottery.Main \
  -cp target/simple-lottery-1.0-SNAPSHOT.jar -o App org.andreasarf.lottery.Main
