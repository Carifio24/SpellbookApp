versionCode=${1}
BUNDLETOOL_CMD="java -jar /home/jon/src/bundletool-all-0.12.0.jar"
PASSWD="C@rifio00"

# The output location
OUTPUT=/home/jon/git/SpellbookApp/versions/APKS/${versionCode}.apks

# Delete the APKS if it already exists
if [ -f $output ]
then
    rm ${OUTPUT}
fi

# For building the APKs
${BUNDLETOOL_CMD} build-apks --bundle=/home/jon/git/SpellbookApp/versions/AAB/${versionCode}.aab --output=${OUTPUT} --ks=/home/jon/keystores/android_key.jks --ks-pass=pass:${PASSWD} --ks-key-alias=JonsKey --key-pass=pass:${PASSWD}