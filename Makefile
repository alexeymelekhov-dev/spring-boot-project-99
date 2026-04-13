run:
	SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun

build:
	./gradlew build

test:
	./gradlew test

clean:
	./gradlew clean