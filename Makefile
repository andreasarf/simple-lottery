SHELL=/bin/bash -o pipefail

############################# Main targets #############################
run-basic: build-mvn exec-basic
run-exp: build-mvn exec-exp
########################################################################

build-mvn:
	@mvn clean package

build-mvn-docker:
	@docker run --rm -v "$(shell pwd):/opt/maven" -w /opt/maven maven:3.9.6-amazoncorretto-21 mvn clean package

exec-basic:
	@java --class-path target/simple-lottery-1.0-SNAPSHOT.jar dev.andreasarf.lottery.basic.Main

exec-exp:
	@java --enable-preview -XX:+UnlockExperimentalVMOptions -XX:+TrustFinalNonStaticFields -dsa -XX:+UseNUMA \
		--class-path target/simple-lottery-1.0-SNAPSHOT.jar dev.andreasarf.lottery.experimental.ExpMain

exec-basic-docker:
	@docker run --rm -v "$(shell pwd):/opt/java" -w /opt/java ghcr.io/graalvm/jdk-community:21 java \
		--class-path target/simple-lottery-1.0-SNAPSHOT.jar dev.andreasarf.lottery.basic.Main

exec-exp-docker:
	@docker run --rm -v "$(shell pwd):/opt/java" -w /opt/java ghcr.io/graalvm/jdk-community:21 java \
	 	--enable-preview -XX:+UnlockExperimentalVMOptions -XX:+TrustFinalNonStaticFields -dsa -XX:+UseNUMA \
		--class-path target/simple-lottery-1.0-SNAPSHOT.jar dev.andreasarf.lottery.experimental.ExpMain
