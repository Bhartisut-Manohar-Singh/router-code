#!/bin/bash
BRANCH_NAME=${1}
MAJOR_VERSION=1
MINOR_VERSION=0
GIT_REPO=https://devops.decimaltech.net/root/tpg-vconnect-config.git
GIT_USER=devops_decimal
GIT_PASS=decimal%24devops
        
major_max=1
minor_max=0
patch_max=0
patch=0
echo "fetching branch:${BRANCH_NAME}"
branch_name="${BRANCH_NAME}"
$(git fetch --tags)

last_tag=$(git describe --abbrev=0 --tags)

if [ -n "$last_tag" ]; then
    version=$(echo $last_tag | grep -o '[^-]*$')
    echo "Found version $version"
    let major=$(echo $version | cut -d. -f1);
    let minor=$(echo $version | cut -d. -f2);
    let patch=$(echo $version | cut -d. -f3);

    if [ "$major_max" -lt "$major" ]; then
        let major_max=$major
    fi
    if [ "$minor_max" -lt "$minor" ]; then
        let minor_max=$minor
    fi
    if [ "$patch_max" -lt "$patch" ]; then
        let patch_max=$patch
    fi
    echo 'Latest version:' $major_max'.'$minor_max'.'$patch_max''
    let patch_max=$patch_max+1;
    if [ "$major_max" -ne "${MAJOR_VERSION}" ]|| [ "$minor_max" -ne "${MINOR_VERSION}" ];  then
        major_max="${MAJOR_VERSION}";
        minor_max="${MINOR_VERSION}";
    fi
   echo 'Switching to new version:' $major_max'.'$minor_max'.'$patch_max''
    $(git config --global user.email "charvee.punia@decimal.co.in")
    $(git config --global user.name "charvee.punia")
    $(git tag -a $branch_name-$major_max.$minor_max.$patch_max $branch_name -m "Version $major_max.$minor_max.$patch_max")
    echo 'Push tag to remote'
    $(git push origin $branch_name-$major_max.$minor_max.$patch_max $branch_name)
else
    tagName=$branch_name-$major_max.$minor_max.$patch_max
    echo "createing the tag $tagName on the branch ${BRANCH_NAME}"
    $(git config --global user.email "charvee.punia@decimal.co.in")
    $(git config --global user.name "charvee.punia")
    git tag -a $tagName $branch_name -m "Version $tagName"
    git push origin $tagName $branch_name
fi

export BUILD_NAME=$branch_name-$major_max.$minor_max.$patch_max
echo "build name is $BUILD_NAME"

