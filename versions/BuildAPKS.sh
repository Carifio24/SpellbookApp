versionCode=${1}
BUNDLETOOL_CMD="java -jar /home/jon/src/bundletool-all-0.12.0.jar"
PASSWD="C@rifio00"

# For building the APKs
${BUNDLETOOL_CMD} build-apks --bundle=/home/jon/git/SpellbookApp/versions/AAB/${versionCode}.aab --output=/home/jon/git/SpellbookApp/versions/APKS/${versionCode}.apks --ks=/home/jon/keystores/android_key.jks --ks-pass=pass:${PASSWD} --ks-key-alias=JonsKey --key-pass=pass:${PASSWD}