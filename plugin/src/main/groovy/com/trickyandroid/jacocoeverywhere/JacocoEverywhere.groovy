package com.trickyandroid.jacocoeverywhere

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger

class JacocoEverywhere implements Plugin<Project> {
    private Logger logger
    final static String LOG_TAG = "[Jacoco-Everywhere]"
    String JACOCO_AGENT_UNZIPPER_TASK_NAME = "unzipJacocoAgent"

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin("com.android.application") &&
                !project.plugins.hasPlugin("com.android.library")) {
            throw new GradleException("jacoco-everywhere plugin can be applied only to Android projects, so make sure that either com.android.application " +
                    "or com.android.library plugin applied")
        }

        this.logger = project.getLogger()

        project.afterEvaluate {
            project.android.applicationVariants.all {
                configureVariant(project, it)
            }
        }
    }

    void configureVariant(Project project, def variant) {
        logD("Configuring variant: ${variant.name}")
        if (variant.buildType.isTestCoverageEnabled()) {
            logD("Coverage enabled. Applying modifications")
            Task unitTestTask = getTask(project, getUnitTestTaskName(variant))
            Task integrationTestTask = getTask(project, getIntegrationTaskName(variant))
            Task coverageReportTask = getTask(project, getCoverageReportTaskName(variant))

            logD("Unit test task: $unitTestTask")
            logD("Integration test task: $integrationTestTask")
            logD("Coverage report task: $coverageReportTask")
            logD("Coverage binary location: ${getCoverageFileDestination(project, variant)}")


            def append = "append=true"
            def destFile = "destfile=${getCoverageFileDestination(project, variant)}"
            unitTestTask?.jvmArgs "-javaagent:${project.buildDir}/intermediates/jacoco/jacocoagent.jar=$append,$destFile"
            unitTestTask?.dependsOn JACOCO_AGENT_UNZIPPER_TASK_NAME
            unitTestTask?.mustRunAfter integrationTestTask
            coverageReportTask?.dependsOn unitTestTask

            project.gradle.taskGraph.whenReady {
                //invalidate existing unit test results if we gather coverage.
                if (coverageReportTask != null && unitTestTask != null) {
                    unitTestTask.outputs.upToDateWhen { false }
                }
            }
        } else {
            logD("Coverage is disabled. Skipping modifications")
        }
    }


    static def getTask(Project project, String name) {
        project.getTasksByName(name, false)?.getAt(0)
    }

    String getIntegrationTaskName(def variant) {
        //app:connectedRegularDebugAndroidTest
        String name = "connected${variant.name.capitalize()}AndroidTest"
        logD("Resolved Integration test task name: $name")
        name
    }

    String getUnitTestTaskName(def variant) {
        //:app:testRegularReleaseUnitTest
        String name = "test${variant.name.capitalize()}UnitTest"
        logD("Resolved Unit test task name: $name")
        name
    }

    String getCoverageReportTaskName(def variant) {
        //:app:createRegularDebugAndroidTestCoverageReport
        String name = "create${variant.name.capitalize()}AndroidTestCoverageReport"
        logD("Resolved coverage report task name: $name ")
        name
    }

    String getCoverageFileDestination(Project project, def variant) {
        String variantBranch = variant.flavorName ? "flavors/${variant.flavorName.toUpperCase()}/" : ""
        String path = "${project.buildDir}/outputs/code-coverage/connected/${variantBranch}coverage.ec"
        logD("Resolved Coverage file destination: $path")
        path
    }

    private void logD(String msg) {
        logger.debug("$LOG_TAG: $msg")
    }
}
