package com.trickyandroid.jacocoeverywhere

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class JacocoEverywhere implements Plugin<Project> {
    final static String LOG_TAG = "[Jacoco-Everywhere]"

    //coverage report task supplied by Android plugin
    final static String COVERAGE_REPORT_TASK_NAME = "createDebugAndroidTestCoverageReport"
    //unit test task provided by Android plugin
    final static String UNIT_TEST_TASK_NAME = "testDebugUnitTest"
    final static String INTEGRATION_TEST_TASK_NAME = "connectedDebugAndroidTest"
    final static String JACOCO_AGENT_UNZIPPER_TASK_NAME = "unzipJacocoAgent"

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin("com.android.application") &&
                !project.plugins.hasPlugin("com.android.library")) {
            throw new GradleException("jacoco-everywhere plugin can be applied only to Android projects, so make sure that either com.android.application " +
                    "or com.android.library plugin applied")
        }

        project.ext.coverageEnabled = { project.android.buildTypes.debug.testCoverageEnabled }

        project.afterEvaluate {
            if (project.coverageEnabled()) {
                project.logger.debug("$LOG_TAG Coverage enabled. Applying modifications")
                project.tasks.matching { it.name == UNIT_TEST_TASK_NAME }.all {
                    def append = "append=true"
                    def destFile = "destfile=${project.buildDir}/outputs/code-coverage/connected/coverage.ec"
                    it.jvmArgs "-javaagent:${project.buildDir}/intermediates/jacoco/jacocoagent.jar=$append,$destFile"
                    it.dependsOn JACOCO_AGENT_UNZIPPER_TASK_NAME
                    it.mustRunAfter INTEGRATION_TEST_TASK_NAME
                }

                project.tasks.matching { it.name == COVERAGE_REPORT_TASK_NAME }.all {
                    it.dependsOn UNIT_TEST_TASK_NAME
                }
            } else {
                project.logger.debug("Coverage is disabled. Skipping modifications")
            }
        }
    }
}
