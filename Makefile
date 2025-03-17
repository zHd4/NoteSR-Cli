.PHONY: build

build:
	./gradlew build

clean:
	./gradlew clean

install-dist:
	./gradlew installDist

run:
	./gradlew run

run-dist:
	./build/install/notesr-cli/bin/notesr-cli $(ARGS)

checkstyle:
	./gradlew checkstyleMain
	./gradlew checkstyleTest

test:
	./gradlew test

report:
	./gradlew jacocoTestReport