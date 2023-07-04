plugins {
  `java-library`
  id("io.freefair.lombok") version "8.1.0"
}

group = "me.jules"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
  testImplementation("com.google.guava:guava:32.1.1-jre")
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17

  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {

  test {
    useJUnitPlatform()
  }
}