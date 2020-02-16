versionCode=${1}
BUNDLETOOL_CMD="java -jar /home/jon/src/bundletool-all-0.12.0.jar"
ADB_LOCATION="/home/jon/Android/Sdk/platform-tools/adb"

# For deploying
${BUNDLETOOL_CMD} install-apks --adb ${ADB_LOCATION} --apks=/home/jon/git/SpellbookApp/versions/APKS/${versionCode}.apks