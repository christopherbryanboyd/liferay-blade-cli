#!/bin/bash

daemonFlag="--no-daemon"
nexusOpt=""
releaseType=""
debug="true"
bladeVersion=""
tmpDir=""
timestamp=$(date +%s)
localRepoHost="http://localhost:8081"
remoteRepoHost="https://repository.liferay.com"
constLocal="--local"
constRemote="--remote"
constDebug="--debug"
constSnapshots="snapshots"
constRelease="release"
constNoDaemon="--no-daemon"

# check the arguments first
while [ $# -gt 0 ]; do
    if [ "$1" = "${constSnapshots}" ] || [ "$1" = "${constRelease}" ]; then
        releaseType="$1"
    elif [ "$1" = "${constLocal}" ]; then
        nexusOpt="-PlocalNexus"
    elif [ "$1" = "${constRemote}" ]; then
        nexusOpt="-PremoteNexus"
    elif [ "$1" = "${constNoDaemon}" ]; then
        daemonFlag="--no-daemon"
    elif [ "$1" = "${constDebug}" ]; then
        debug="true"
    fi
    shift
done

## Clean up any script resources.
script_end() {
    if [ "${debug}" = "true" ]; then
        echo "${FUNCNAME[0]} begin"
    fi
    
    clean_temp_directory 
    
    local returnCode="$?"
    
    if [ "$debug" = "true" ]; then
        echo "${FUNCNAME[0]} ended with exit code ${returnCode}"
    fi
    
    return "${returnCode}"
}

# Run script_end when the script finishes.
trap script_end EXIT

## Create the temporary directory.
create_temp_directory() {
    if [ "${debug}" = "true" ]; then
        echo "${FUNCNAME[0]} begin"
    fi
    
    # Setup the temporary directory.
    tmpDir="/tmp/$timestamp/"

    # Create the temp directory.
    mkdir -p $tmpDir
    
    local returnCode="$?"
    
    if [ ! -d "$tmpDir" ] && [ "${returnCode}" -eq "0" ]; then
        # Didn't create the directory, set proper exit code.
        returnCode="1"
    fi
    
    if [ "${debug}" = "true" ]; then
        echo "${FUNCNAME[0]} ended with exit code ${returnCode}"
    fi
    
    return "${returnCode}"
}

## Clean the temporary directory.
clean_temp_directory() {
    if [ "${debug}" = "true" ]; then
        echo "${FUNCNAME[0]} begin"
    fi
    
    local returnCode="0"
    
    # Make sure the temporary directory exists first.
    if [ -d "${tmpDir}"]; then
    
        if [ "${debug}" = "true" ]; then
            echo "${FUNCNAME[0]} deleting ${tmpDir}"
        fi
        
        rm -rf "${tmpDir}"
        
        returnCode="$?"
        
        # Ensure the directory doesn't exist.
        if [ ! -d "${tmpDir}"]; then
            if [ "${debug}" = "true" ]; then
                echo "${FUNCNAME[0]} successfully deleted ${tmpDir}"
            fi
        else # Couldn't delete temporary directory, this is an error condition.
        
            if [ "${debug}" = "true" ]; then
                echo "${FUNCNAME[0]} unable to delete ${tmpDir}"
            fi
        fi
    elif [ "${debug}" = "true" ]; then # Temporary directory doesn't exist.
        echo "${FUNCNAME[0]} ${tmpDir} does not exist"
	fi
	
    if [ "${debug}" = "true" ]; then
        echo "${FUNCNAME[0]} ended with exit code ${returnCode}"
    fi
	
    return "${returnCode}"
}

## Check the arguments and return 1 if they are invalid.
check_args() {
    if [ "${debug}" = "true" ]; then
        echo "${FUNCNAME[0]} begin"
    fi
    
    local returnCode="0"
    
    # We didn't find a valid argument, so fail.
    if [ "$releaseType" != "release" ] && [ "$releaseType" != "snapshots" ]; then
        echo "check_args() Error: Must have one argument, either \"release\" or \"snapshots\"."
        returnCode="1"
    fi
    
    if [ "${debug}" = "true" ]; then
        echo "${FUNCNAME[0]} ended with exit code ${returnCode}"
    fi
    
    return "${returnCode}"
}

## Installs blade.
install_blade() {
    if [ "${debug}" = "true" ]; then
        echo "${FUNCNAME[0]} begin"
    fi
    
    local commandString="curl -L https://raw.githubusercontent.com/liferay/liferay-blade-cli/master/cli/installers/local | sh"
    
    if [ "${debug}" = "true" ]; then
        echo "${FUNCNAME[0]} preparing to invoke: ${commandString}"
    fi
    
    local bladeInstall=$(eval "${commandString}")
    
    local returnCode="$?"
    
    if [ ! "${returnCode}" -eq 0 ]; then
        echo "${FUNCNAME[0]} failed"
    fi
    
    if [ "${debug}" = "true" ]; then
        echo "${FUNCNAME[0]} output: \n${bladeInstall}"
        echo "${FUNCNAME[0]} ended with exit code ${returnCode}"
    fi
    
    return "${returnCode}"
}

## Adds jpm to the path
add_jpm_to_path() {
    if [ "${debug}" = "true" ]; then
        echo "${FUNCNAME[0]} begin"
    fi
    
    local jpmBinDir="${HOME}/jpm/bin"
    
    local returnCode="0"
    
    if [ ! -d "${jpmBinDir}" ]; then
        returnCode="1"
    else
        export PATH="${PATH}:${jpmBinDir}"
        
        command -v "jpm"
        
        returnCode="$?"

        if [ ! "${returnCode}" -eq 0 ]; then
            echo "${FUNCNAME[0]} failed"
            echo "${FUNCNAME[0]} jpm does not exist"
        fi
    fi
    
    if [ "${debug}" = "true" ]; then
        echo "${FUNCNAME[0]} ended with exit code ${returnCode}"
    fi
    
    return "${returnCode}"
}

## Check (and possibly set) the repoHost variable
## and can also set the nexusOpt variable.
check_repo_host() {

    if [ "${debug}" = "true" ]; then
        echo "${FUNCNAME[0]} begin"
    fi
	
    if [ -z "$repoHost" ]; then
        if [ "$nexusOpt" = "-PlocalNexus" ]; then
            repoHost="$localRepoHost"
        else
            repoHost="$remoteRepoHost"
        fi
    elif [ "$repoHost" = "http://localhost:8081" ]; then
        nexusOpt="-PlocalNexus"
    fi
    
    
    if [ "${debug}" = "true" ]; then
        echo "check_repo_host() Error: var repoHost = $repoHost"
	fi
	echo "check_repo_host() Error: Failed with $?"
}

## Checks that the blade version command succeeds.
check_blade_version() {
    if [ "$debug" = "true" ]; then
        echo "check_blade_version()"
	fi
    bladeVersion=$(blade version)
}

gradlew_clean() {
    if [ "${debug}" = "true" ]; then
        echo "${FUNCNAME[0]} begin"
    fi
    
    local commandString="./gradlew -q --no-daemon --console=plain clean"
    
    if [ "${debug}" = "true" ]; then
        echo "${FUNCNAME[0]} preparing to invoke: ${commandString}"
    fi
    
    local gradlewCheck=$(eval "${commandString}")
    
    local returnCode="$?"
    
    if [ ! "${returnCode}" -eq 0 ]; then
        echo "${FUNCNAME[0]} failed"
    fi
    
    if [ "${debug}" = "true" ]; then
        echo "${FUNCNAME[0]} output: \n${gradlewCheck}"
        echo "${FUNCNAME[0]} ended with exit code ${returnCode}"
    fi
    
    return "${returnCode}"
}

gradlew_check() {
    if [ "${debug}" = "true" ]; then
        echo "${FUNCNAME[0]} begin"
    fi
    
    local commandString="./gradlew --stacktrace --scan --no-daemon jar"
    
    if [ "${debug}" = "true" ]; then
        echo "${FUNCNAME[0]} preparing to invoke: ${commandString}"
    fi
    
    local gradlewCheck=$(eval "${commandString}")
    
    local returnCode="$?"
    
    if [ ! "${returnCode}" -eq 0 ]; then
        echo "${FUNCNAME[0]} failed"
    fi
    
    if [ "${debug}" = "true" ]; then
        echo "${FUNCNAME[0]} output: \n${gradlewCheck}"
        echo "${FUNCNAME[0]} ended with exit code ${returnCode}"
    fi
    
    return "${returnCode}"
}

publish_remote_deploy_command() {
    if [ "${debug}" = "true" ]; then
        echo "${FUNCNAME[0]} begin"
    fi
    
    local commandString="./gradlew -q --no-daemon --console=plain $nexusOpt -P${releaseType} :extensions:remote-deploy-command:publish --info"
    
    local remoteDeployCommandPublishCommand=$(eval "${commandString}")

    if [ "$?" != "0" ] || [ -z "$remoteDeployCommandPublishCommand" ]; then
        echo Failed :extensions:remote-deploy-command:publish
        return 1
    fi
}

publish_maven_profile() {
    if [ "${debug}" = "true" ]; then
        echo "${FUNCNAME[0]} begin"
    fi
    
    local commandString="./gradlew -q --no-daemon --console=plain $nexusOpt -P${releaseType} :extensions:maven-profile:publish -x :cli:bladeExtensionsVersions -x :cli:processResources --info --scan | tee /tmp/$timestamp/maven-profile-publish-command.txt
mavenProfilePublishCommand=$(cat /tmp/$timestamp/maven-profile-publish-command.txt)"

    if [ ! "$?" -eq 0 ] || [ -z "${mavenProfilePublishCommand}" ]; then
        echo Failed :extensions:maven-profile:publish
        return 1
    fi

}

# Ensure that the arguments are valid.
check_args || exit $?

# Ensure that the repository host is configured correctly.
check_repo_host || exit $?

# Install blade.
install_blade || exit $?

# Add jpm to the path and ensure it can be invoked.
add_jpm_to_path || exit $?

# Ensure that the blade version command succeeds.
check_blade_version || exit $?

# First clean local build folder to try to minimize variants.
gradlew_clean || exit $?

# Run check to ensure that the build is correct.
gradlew_check || exit $?

# Create temporary directory to use for testing.
create_temp_directory || exit $?

# Publish the Remote Deploy Command jar.
publish_remote_deploy_command || exit $?

# Publish the Maven Profile jar.
publish_maven_profile || exit $?

# Grep the output of the previous command to find the url of the published jar
mavenProfilePublishUrl=$(echo "$mavenProfilePublishCommand" | grep Uploading | grep '.jar ' | grep -v -e '-sources' -e '-tests' | cut -d' ' -f2)

if [ "$?" != "0" ] || [ -z "$mavenProfilePublishUrl" ]; then
   echo Failed grepping for mavenProfilePublishUrl
   #rm -rf /tmp/$timestamp
   exit 1
fi

# Download the just published jar in order to later compare it to the embedded maven profile that is in blade jar
mavenProfileJarUrl="$repoHost/nexus/content/groups/public/$mavenProfilePublishUrl"

curl -s "$mavenProfileJarUrl" -o /tmp/$timestamp/maven_profile.jar

if [ "$?" != "0" ]; then
   echo Downloading maven.profile jar failed.
   rm -rf /tmp/$timestamp
   exit 1
else
   echo "Published $mavenProfileJarUrl"
fi

# Test the blade cli jar locally, but don't publish.
./gradlew -q --no-daemon --console=plain $nexusOpt -P${releaseType} --refresh-dependencies clean jar --info --scan | tee /tmp/$timestamp/blade-cli-jar-command.txt
bladeCliJarCommand=$(cat /tmp/$timestamp/blade-cli-jar-command.txt)

if [ "$?" != "0" ] || [ -z "$bladeCliJarCommand" ]; then
   echo Failed :cli:jar
   rm -rf /tmp/$timestamp
   exit 1
fi

# now that we have the blade jar just built, lets extract the embedded maven profile jar and compare to the maven profile downloaded from nexus

embeddedMavenProfileJar=$(jar -tf cli/build/libs/blade.jar | grep "maven.profile-")

if [ -z "$embeddedMavenProfileJar" ]; then
   echo Failed to find embedded maven.profile jar in blade jar
   rm -rf /tmp/$timestamp
   exit 1
fi

unzip -p cli/build/libs/blade.jar "$embeddedMavenProfileJar" > /tmp/$timestamp/myExtractedMavenProfile.jar

diff -s /tmp/$timestamp/myExtractedMavenProfile.jar /tmp/$timestamp/maven_profile.jar

if [ "$?" != "0" ]; then
   echo Failed local blade.jar diff with downloaded maven profile jar.  The embedded maven profile jar and nexus maven profile jar are not identical
   rm -rf /tmp/$timestamp
   exit 1
fi

# Now lets go ahead and publish the blade cli jar for real since the embedded maven profile was correct

./gradlew -q --no-daemon --console=plain $nexusOpt -P${releaseType} --refresh-dependencies :cli:publish --info --scan | tee /tmp/$timestamp/blade-cli-publish-command.txt
bladeCliPublishCommand=$(cat /tmp/$timestamp/blade-cli-publish-command.txt)

if [ "$?" != "0" ] || [ -z "$bladeCliPublishCommand" ]; then
   echo Failed :cli:publish
   rm -rf /tmp/$timestamp
   exit 1
fi

# Grep the output of the blade jar publish to find the url
bladeCliJarUrl=$(echo "$bladeCliPublishCommand" | grep Uploading | grep '.jar ' | grep -v -e '-sources' -e '-tests' | cut -d' ' -f2)

# download the just published jar in order to extract the embedded maven profile jar to compare to previously downloaded version from above (just to be double sure)
bladeCliUrl="$repoHost/nexus/content/groups/public/$bladeCliJarUrl"

curl -s "$bladeCliUrl" -o /tmp/$timestamp/blade.jar

if [ "$?" != "0" ]; then
   echo Downloading blade jar failed.
   rm -rf /tmp/$timestamp
   exit 1
else
   echo "Published $bladeCliUrl"
fi

unzip -p /tmp/$timestamp/blade.jar "$embeddedMavenProfileJar" > /tmp/$timestamp/myExtractedMavenProfile.jar

diff -s /tmp/$timestamp/myExtractedMavenProfile.jar /tmp/$timestamp/maven_profile.jar

if [ "$?" != "0" ]; then
   echo Failed local blade.jar diff with downloaded maven profile jar.  The embedded maven profile jar and nexus maven profile jar are not identical
   rm -rf /tmp/$timestamp
   exit 1
fi

localBladeVersion=$(java -jar /tmp/$timestamp/blade.jar version)

# Already handled in the dockerfile
# mkdir ~/.blade

echo "$repoHost/nexus/content/groups/public/com/liferay/blade/com.liferay.blade.cli/" > ~/.blade/update.url

if [ "$releaseType" = "snapshots" ]; then
    bladeUpdate=$(blade update --snapshots)
else
    bladeUpdate=$(blade update)
fi

if [ "$?" != "0" ]; then
   echo Failed blade update.
   echo $bladeUpdate
   rm -rf /tmp/$timestamp
   exit 1
fi

updatedBladeVersion=$(blade version)

echo $bladeUpdate
echo $localBladeVersion
echo $updatedBladeVersion

if [ "$localBladeVersion" != "$updatedBladeVersion" ]; then
	echo After blade updated versions do not match.
	echo "Built blade version = $localBladeVersion"
	echo "Updated blade version = $updatedBladeVersion"
	rm -rf /tmp/$timestamp
	exit 1
fi

rm -rf /tmp/$timestamp
