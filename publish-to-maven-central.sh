#!/bin/zsh

# Contributors have to set the new version before publishing:
VERSION="1.0.1"

# 1. Build
echo
./mvnw clean install -DskipTests
cp pom.xml target/ || exit

# 2. Sign artifacts manually
echo "Starting manual signing..."
cd target || exit
gpg -ab pom.xml
gpg -ab modlist-maven-plugin-"$VERSION".jar
gpg -ab modlist-maven-plugin-"$VERSION"-javadoc.jar
gpg -ab modlist-maven-plugin-"$VERSION"-sources.jar
cd ..

# 3. Publish
# ./mvnw central-publishing:publish -DskipCentralPublishing=false
