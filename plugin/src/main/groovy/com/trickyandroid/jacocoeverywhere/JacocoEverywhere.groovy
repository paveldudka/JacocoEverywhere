package com.trickyandroid.jacocoeverywhere

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec

class JacocoEverywhere implements Plugin<Project> {
    @Override
    void apply(Project project) {
        createTasks(project)
    }

    void createTasks(Project project) {
        createDeviceCheckerTask(project)
        createJacocoFixerTask(project)

        project.afterEvaluate {
            fixJacoco.dependsOn(connectedDebugAndroidTest)
            testDebugUnitTest.mustRunAfter fixJacoco
            createDebugAndroidTestCoverageReport.dependsOn fixJacoco
        }

        project.afterEvaluate {
            def append = "append=true"
            def destFile = "destfile=$buildDir/outputs/code-coverage/connected/coverage.ec"
            testDebugUnitTest.jvmArgs "-javaagent:$buildDir/intermediates/jacoco/jacocoagent.jar=$append,$destFile"

            createDebugAndroidTestCoverageReport.dependsOn testDebugUnitTest
            testDebugUnitTest.dependsOn unzipJacocoAgent
        }
    }

    /**
     * reroute Jacoco coverage executable to be stored on sdcard instead of built-in
     * /data/data/<deviceid>/ location since this location is not accessible on some Samsung devices
     * @param project
     */
    void createJacocoFixerTask(Project project) {
        def task = project.task("fixJacoco", type: Exec) {
            dependsOn getFirstConnectedDevice
            commandLine(
                    "${rootProject.getSdkLocation()}/platform-tools/adb",
                    '-s',
                    "${-> getFirstConnectedDevice.device()}",
                    'pull',
                    '/sdcard/coverage.ec',
                    "$project.buildDir/outputs/code-coverage/connected/coverage.ec")
        }
    }

    void createDeviceCheckerTask(Project project) {
        def task = project.task("getFirstConnectedDevice", type: Exec) {
            commandLine("${rootProject.getSdkLocation()}/platform-tools/adb", 'devices')
            standardOutput = new ByteArrayOutputStream()
            ext.device = {
                if (standardOutput.toString()) {
                    return standardOutput.toString().split('\n')[1].split("\\W+")[0]
                }
            }
        }
    }
}