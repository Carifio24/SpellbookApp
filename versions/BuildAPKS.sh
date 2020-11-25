versionCode=${1}
BUNDLETOOL="bundletool-all-1.4.0.jar"
BUNDLETOOL_CMD="java -jar ${BUNDLETOOL}"
PASSWD="C@rifio00"

# The output location
OUTPUT=${PWD}/APKS/${versionCode}.apks

# Delete the APKS if it already exists
if [ -f ${OUTPUT} ]
then
    rm ${OUTPUT}
fi

# For building the APKs
${BUNDLETOOL_CMD} build-apks --bundle=${PWD}/AAB/${versionCode}.aab --output=${OUTPUT} --ks=/home/jon/keystores/android_key.jks --ks-pass=pass:${PASSWD} --ks-key-alias=JonsKey --key-pass=pass:${PASSWD}
