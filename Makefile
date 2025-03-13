.PHONY: build

build:
	./gradlew build

clean:
	./gradlew clean

run:
	./gradlew run

run-dist:
	./build/install/app/bin/app

checkstyle:
	./gradlew checkstyleMain
	./gradlew checkstyleTest

test:
	./gradlew test

report:
	./gradlew jacocoTestReport