.PHONY: build

run-dist:
	./build/install/app/bin/app

build:
	./gradlew build

run:
	./gradlew run

report:
	./gradlew jacocoTestReport