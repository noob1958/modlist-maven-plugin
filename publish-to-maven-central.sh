#!/bin/zsh

# Contributors have to set the new version before publishing:
VERSION="1.0.1"

COPY_TO="io/github/noob1958/modlist/$VERSION"

./mvnw clean install -DskipTests=true

echo "Signing artifacts with gpg"
cd target || exit
gpg -ab pom.xml
gpg -ab modlist-maven-plugin-"$VERSION".jar
gpg -ab modlist-maven-plugin-"$VERSION"-javadoc.jar
gpg -ab modlist-maven-plugin-"$VERSION"-sources.jar

echo "Creating and populating folder structure"
mkdir -pv "$COPY_TO"
cp pom* "$COPY_TO"
cp modlist-maven-plugin* "$COPY_TO"

echo "Zipping into a Maven Central bundle"
zip bundle -r io/
cd ..

echo "Done. Now upload bundle.zip in target/ manually to https://central.sonatype.com/publishing/deployments."
