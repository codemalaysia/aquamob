#!/usr/bin/env bash
APPLICATION_ID=STYqMP4SSyzwpMVCMSPMzfBt1aJco0T0w8SWgUAV
API_KEY=bSqkwck6I6PGg3AC10PDIGfn0ylKSHJPlErlGLn9

assembleType=$1
if [ -z ${assembleType} ]; then
   assembleType="assembleDebug";
fi

version_type=${RELEASE_TYPE};
if [ -z ${version_type} ]; then
   echo "RELEASE_TYPE is missing";
   exit;
fi

version_file=app/src/main/java/com/polluxlab/aquamob/utils/Version.java
git checkout ${version_file}
version_trunk="trunk-SNAPSHOT"

version_major=$(grep MAJOR ${version_file} | grep -oEi "\".*\"" | sed s/\"// | sed s/\"//)
version_minor=$(grep MINOR ${version_file} | grep -oEi "\".*\"" | sed s/\"// | sed s/\"//)
version_build=$(date +"%Y%m%d%H%M")

code=$(sed s/${version_trunk}/${version_build}/ ${version_file})
echo ${code} > ${version_file}

type_default="nOnE"
code=$(sed s/${type_default}/${version_type}/ ${version_file})
echo ${code} > ${version_file}

./gradlew $assembleType
release_path=app/build/outputs/apk
release_apk=${release_path}/app-developer-release.apk
if [ "$version_type" == "master" ]; then
  release_apk=${release_path}/app-master-release.apk ;
fi

apk_name="lobster-${version_major}.${version_minor}.${version_build}.apk"
echo "APK NAME: ${apk_name}"
result=$(curl -H "X-Parse-Application-Id: ${APPLICATION_ID}" -H "X-Parse-REST-API-Key: ${API_KEY}" \
  -H "Content-Type: application/vnd.android.package-archive" \
  --data-binary @${release_apk} \
  -X POST "https://api.parse.com/1/files/${apk_name}")

echo ${result}
echo ${result} | python -m json.tool
uploaded_file_name=$(echo ${result} | php -r 'echo json_decode(fgets(STDIN))->name;')
echo ${uploaded_file_name}

version_json="{
    \"buildNumber\": \"${version_build}\",
    \"enabled\": true,
    \"major\": \"${version_major}\",
    \"minor\": \"${version_minor}\",
    \"type\": \"${version_type}\",
    \"apk\": {
      \"name\": \"${uploaded_file_name}\",
      \"__type\": \"File\"
    }
}"

curl -i -H "X-Parse-Application-Id: ${APPLICATION_ID}" -H "X-Parse-REST-API-Key: ${API_KEY}" \
    -H "Content-Type: application/json" \
    -d "${version_json}" \
    -X POST https://api.parse.com/1/classes/AquamobApps

git checkout ${version_file}
cp "${release_apk}" "${release_path}/$apk_name"
echo -e "Backup apk created: ${release_path}/$apk_name"
