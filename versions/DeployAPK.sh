versionCode=${1}
BUNDLETOOL_CMD="java -jar /home/jon/src/bundletool-all-0.12.0.jar"
ADB="/home/jon/Android/Sdk/platform-tools/adb"

# For deploying
${BUNDLETOOL_CMD} install-apks --adb ${ADB} --apks=${PWD}/APKS/${versionCode}.apks
