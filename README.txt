Stats HUD - Fabric, Minecraft Java 26.3 (client-side only)

REQUIREMENTS: JDK 25, Gradle 9.x (or a wrapper from the Fabric template)

BUILD
  gradle wrapper        (once, only if there is no gradlew yet)
  ./gradlew build       (Windows: gradlew.bat build)
Jar: build/libs/statshud-1.0.0.jar  (NOT the -sources jar)

INSTALL
  Put the jar + Fabric API 0.161.0+26.3 into .minecraft/mods with Fabric Loader 0.19.5+.
