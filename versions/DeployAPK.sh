versionCode=${1}
BUNDLETOOL="bundletool-all-1.4.0.jar"
BUNDLETOOL_CMD="java -jar ${BUNDLETOOL}"
ADB="/home/jon/Android/Sdk/platform-tools/adb"

# For deploying
${BUNDLETOOL_CMD} install-apks --adb ${ADB} --apks=${PWD}/APKS/${versionCode}.apks
