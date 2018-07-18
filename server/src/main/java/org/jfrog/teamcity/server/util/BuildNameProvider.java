package org.jfrog.teamcity.server.util;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import org.jetbrains.annotations.NotNull;

import static org.jfrog.teamcity.common.ConstantValues.BUILD_NAME_SEPARATOR;

public class BuildNameProvider {

    public String getFullBuildName(@NotNull final SBuild build, @NotNull final String separator) {
        return getFullBuildName(build.getBuildType(), separator);
    }

    public String getFullBuildName(@NotNull final SBuild build) {
        return getFullBuildName(build.getBuildType());
    }

    public String getFullBuildName(@NotNull final SBuildType buildType) {
        return getFullBuildName(buildType, BUILD_NAME_SEPARATOR);
    }

    public String getFullBuildName(@NotNull final SBuildType buildType, @NotNull final String separator) {
        final StringBuilder builder = new StringBuilder(buildType.getName());
        SProject project = buildType.getProject();
        while (project != null && !project.isRootProject()) {
            builder.insert(0, separator);
            builder.insert(0, project.getName());
            project = project.getParentProject();
        }

        return builder.toString();
    }
}
