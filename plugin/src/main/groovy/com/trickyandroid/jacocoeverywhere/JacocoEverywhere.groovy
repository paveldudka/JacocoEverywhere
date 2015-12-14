package com.trickyandroid.jacocoeverywhere

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Exec

class JacocoEverywhere implements Plugin<Project> {
    final static String TASK_GROUP = "jacoco-everywhere"
    final static String LOG_TAG = "[Jacoco-Everywhere]"
    final static String JACOCO_REPORT_DOWNLOADER_TASK_NAME = "downloadJacocoReport"
    final static String GET_FIRST_CONNECTED_DEVICE_TASK_NAME = "getFirstConnectedDevice"

    private Task deviceCheckerTask

    @Override
    void apply(Project project) {
        resolveAndroidSDKLocation(project)
        createTasks(project)

        project.tasks.matching { it.name == "testDebugUnitTest" }.all {
            it.mustRunAfter JACOCO_REPORT_DOWNLOADER_TASK_NAME
            def append = "append=true"
            def destFile = "destfile=${project.buildDir}/outputs/code-coverage/connected/coverage.ec"
            it.jvmArgs "-javaagent:${project.buildDir}/intermediates/jacoco/jacocoagent.jar=$append,$destFile"
            it.dependsOn "unzipJacocoAgent"
        }

        project.tasks.matching { it.name == "createDebugAndroidTestCoverageReport" }.all {
            it.dependsOn JACOCO_REPORT_DOWNLOADER_TASK_NAME
            it.dependsOn "testDebugUnitTest"
        }
    }

    void createTasks(Project project) {

        createDeviceCheckerTask(project)
        createJacocoReportDownloaderTask(project)
    }

    /**
     * Since this plugin changes the Jacoco report location on device, Android plugin will not
     * be able to find it anymore, so we need to pull report from device manually
     * @param project
     */
    void createJacocoReportDownloaderTask(Project project) {
        def task = project.task(JACOCO_REPORT_DOWNLOADER_TASK_NAME, type: Exec) {
            dependsOn GET_FIRST_CONNECTED_DEVICE_TASK_NAME
            commandLine(
                    "${project.sdkPath}/platform-tools/adb",
                    '-s',
                    "${-> deviceCheckerTask.device()}",
                    'pull',
                    '/sdcard/coverage.ec',
                    "$project.buildDir/outputs/code-coverage/connected/coverage.ec")
        }
        task.group = TASK_GROUP
        task.description = "Pull Jacoco coverage report from SD card"
        task.dependsOn("connectedDebugAndroidTest")
    }

    void createDeviceCheckerTask(Project project) {
        def task = project.task(GET_FIRST_CONNECTED_DEVICE_TASK_NAME, type: Exec) {
            commandLine("${project.sdkPath}/platform-tools/adb", 'devices')
            standardOutput = new ByteArrayOutputStream()
            ext.device = {
                if (standardOutput.toString()) {
                    def connectedDeviceId = standardOutput.toString().split('\n')[1].find("[\\w-]+")
                    project.logger.debug "$LOG_TAG found connected device: $connectedDeviceId"
                    return connectedDeviceId
                } else {
                    project.logger.debug "$LOG_TAG could not execute adb devices command :("
                }
            }
        }
        task.group = TASK_GROUP
        task.description = "Find connected device id"

        deviceCheckerTask = task
    }

    static void resolveAndroidSDKLocation(Project project) {
        def sdkPath = getSdkLocationLocalProps(project)
        if (!sdkPath) sdkPath = getSdkLocationFromEnvVars(project)
        sdkPath

        if (!sdkPath) {
            throw new GradleException("SDK location not found. " +
                    "Define location with sdk.dir in the local.properties file " +
                    "or with an ANDROID_HOME environment variable.")
        } else {
            project.logger.debug("$LOG_TAG Found Android SDK: $sdkPath")
        }
        project.ext.sdkPath = sdkPath
    }

    static def getSdkLocationFromEnvVars(Project project) {
        project.logger.debug("$LOG_TAG Looking ANDROID_HOME env var")
        def sdkDir = System.getenv("ANDROID_HOME")
        if (!sdkDir) {
            project.logger.debug "$LOG_TAG ANDROID_HOME environment variable is not specified"
        }
        sdkDir
    }

    static def getSdkLocationLocalProps(Project project) {
        project.logger.debug("$LOG_TAG Looking for sdk.dir in local.properties...")
        Properties defaultProps = new Properties()
        def result = null
        try {
            FileInputStream input = new FileInputStream("${project.rootDir}/local.properties")
            defaultProps.load(input);
            input.close();
            result = defaultProps.getProperty("sdk.dir")
        } catch (Exception e) {
            project.logger.debug("$LOG_TAG Unable to read local.properties: $e")
        }

        if (result == null) {
            project.logger.debug "$LOG_TAG could not find sdk.dir in local.properties"
        }
        result
    }
}
