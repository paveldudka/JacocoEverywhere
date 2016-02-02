package com.trickyandroid.jacocoeverywhere

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.slf4j.Marker
import org.slf4j.MarkerFactory

class JacocoEverywhere implements Plugin<Project> {
    private Logger logger
    private Marker marker
    final static String LOG_TAG = "[Jacoco-Everywhere]"

    //coverage report task supplied by Android plugin
    String COVERAGE_REPORT_TASK_NAME = "createDebugAndroidTestCoverageReport"
    //unit test task provided by Android plugin
    String UNIT_TEST_TASK_NAME = "testDebugUnitTest"
    String INTEGRATION_TEST_TASK_NAME = "connectedDebugAndroidTest"
    String JACOCO_AGENT_UNZIPPER_TASK_NAME = "unzipJacocoAgent"

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin("com.android.application") &&
                !project.plugins.hasPlugin("com.android.library")) {
            throw new GradleException("jacoco-everywhere plugin can be applied only to Android projects, so make sure that either com.android.application " +
                    "or com.android.library plugin applied")
        }

        this.logger = project.getLogger()
        this.marker = MarkerFactory.getMarker(LOG_TAG)

        project.afterEvaluate {
            project.android.applicationVariants.all {
                configureVariant(project, it)
            }
        }

//        project.ext.coverageEnabled = { project.android.buildTypes.debug.testCoverageEnabled }
//
//        project.gradle.taskGraph.whenReady {
//            println "taskGraph ready"
//            def coverageReportTask = project.gradle.taskGraph.getAllTasks().find {
//                it.name == COVERAGE_REPORT_TASK_NAME
//            }
//            def unitTestTask = project.gradle.taskGraph.getAllTasks().find { it.name == UNIT_TEST_TASK_NAME }
//
//            //invalidate existing unit test results if we gather coverage.
//            if (coverageReportTask != null && unitTestTask != null) {
//                unitTestTask.outputs.upToDateWhen { false }
//            }
//        }
//
//        project.afterEvaluate {
//            if (project.coverageEnabled()) {
//                logD("Coverage enabled. Applying modifications")
//                project.tasks.matching { it.name == UNIT_TEST_TASK_NAME }.all {
//                    def append = "append=true"
//                    def destFile = "destfile=${project.buildDir}/outputs/code-coverage/connected/coverage.ec"
//                    it.jvmArgs "-javaagent:${project.buildDir}/intermediates/jacoco/jacocoagent.jar=$append,$destFile"
//                    it.dependsOn JACOCO_AGENT_UNZIPPER_TASK_NAME
//                    it.mustRunAfter INTEGRATION_TEST_TASK_NAME
//                }
//
//                project.tasks.matching { it.name == COVERAGE_REPORT_TASK_NAME }.all {
//                    it.dependsOn UNIT_TEST_TASK_NAME
//                }
//            } else {
//                project.logger.debug("Coverage is disabled. Skipping modifications")
//            }
//        }
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


    def getTask(Project project, String name) {
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
