call mvn clean install
call mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install -Pcoverage-per-test
call mvn sonar:sonar