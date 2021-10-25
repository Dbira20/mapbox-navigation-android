#!/usr/bin/env bash
set -euo pipefail

usage() {
    echo "Usage: " 1>&2;
    echo "$0 arguments [optional arguments]" 1>&2;
    echo "  Arguments : " 1>&2;
    echo "   -t <TAG>                       tag of navigation, e.g. v1.0.0" 1>&2;
    echo "   -p <GITHUB APP TOKEN>          access token for pull request creation" 1>&2;
    echo "  Optional arguments : " 1>&2;
    echo "   -a <NAVIGATION_GIT_REPOSITORY> path to navigation repository, by default uses current path" 1>&2;
    exit 1;
}

readonly DOCS_BASE_BRANCH_NAME_PRODUCTION="publisher-production"
readonly DOCS_BASE_BRANCH_NAME_STAGING="publisher-staging"
NAVIGATION_GIT_REPO=$(pwd)
GITHUB_PR_ACCESS_TOKEN=""

while getopts "t:a:p:" opt; do
    case "${opt}" in
        t)
            TAG=${OPTARG}
            VERSION_NUMBER="${TAG//v}"
            BRANCH_NAME="docs-${TAG}"
            DOCS_FOLDER=${NAVIGATION_GIT_REPO}/${VERSION_NUMBER}/
            ;;
        p)
            GITHUB_PR_ACCESS_TOKEN=${OPTARG}
            ;;
        a)
            NAVIGATION_GIT_REPO=${OPTARG}
            ;;
        *)
            usage
            ;;
    esac
done

echo "Repository path=${NAVIGATION_GIT_REPO}"

checkIfGithubTokensAreProvided() {
    if [ -z "$GITHUB_PR_ACCESS_TOKEN" ]; then
        usage
    fi
}

checkIfTagExist() {
    pushd "${NAVIGATION_GIT_REPO}"
    if [ $(git tag --points-at HEAD) = TAG ]; then
        echo "tag ${TAG} exist"
    else
        echo "current commit is not point at tag ${TAG}"
        usage
    fi
    echo "VERSION_NUMBER=${VERSION_NUMBER}"

    popd > /dev/null
}

generateDocs() {
    pushd "${NAVIGATION_GIT_REPO}"
    echo "Start generating docs"

    make javadoc-dokka

    echo "Finish generating docs"
    popd > /dev/null
}

checkoutToPublisherStaging() {
    pushd "${NAVIGATION_GIT_REPO}"

    echo "checkout to ${DOCS_BASE_BRANCH_NAME_STAGING}"
    git checkout "${DOCS_BASE_BRANCH_NAME_STAGING}"

    popd > /dev/null
}

checkoutToPublisherProduction() {
    pushd "${NAVIGATION_GIT_REPO}"

    echo "checkout to ${DOCS_BASE_BRANCH_NAME_PRODUCTION}"
    git checkout "${DOCS_BASE_BRANCH_NAME_PRODUCTION}"

    popd > /dev/null
}

moveDocumentsToDocsFolders() {
    pushd "${NAVIGATION_GIT_REPO}" > /dev/null

    echo "Create DOCS folder ${DOCS_FOLDER}"
    mkdir -p $DOCS_FOLDER

    echo "Moving NAV Docs to docs folder"
    cp -r "build/kdoc/." $DOCS_FOLDER

    popd > /dev/null
}

pushDocsToStaging() {
    pushd "${NAVIGATION_GIT_REPO}"

    echo "push docs to staging"
    git add "${VERSION_NUMBER}/"
    git commit -m "Docs ${TAG}"
    git push origin HEAD --set-upstream

    popd > /dev/null
}

createBranchProduction() {
    pushd "${NAVIGATION_GIT_REPO}"

    echo "create a docs brach"
    git checkout -b ${BRANCH_NAME}
    git add "${VERSION_NUMBER}/"
    git commit -m "Docs ${TAG}"
    git push origin HEAD --set-upstream
    popd > /dev/null
}

createPullRequest() {
    pushd "${NAVIGATION_GIT_REPO}"

    echo "create pull request"

    GITHUB_TOKEN=$GITHUB_PR_ACCESS_TOKEN gh pr create \
        --title "Docs ${TAG}" \
        --body "**Docs ${TAG}**
        <br/>**Staging**:
        - github/${DOCS_BASE_BRANCH_NAME_STAGING} https://github.com/mapbox/mapbox-navigation-android/tree/publisher-staging/${VERSION_NUMBER}
        - staging docs https://docs.tilestream.net/android/navigation/api-reference/
        - staging docs ${TAG} https://docs.tilestream.net/android/navigation/api/${VERSION_NUMBER}/-modules.html
        <br/>**cc** @mapbox/navigation-android" \
        --base ${DOCS_BASE_BRANCH_NAME_PRODUCTION} \
        --head ${BRANCH_NAME}

    popd > /dev/null
}

checkIfGithubTokensAreProvided
checkIfTagExist
generateDocs
# staging
checkoutToPublisherStaging
moveDocumentsToDocsFolders
pushDocsToStaging
# production
checkoutToPublisherProduction
moveDocumentsToDocsFolders
createBranchProduction
createPullRequest