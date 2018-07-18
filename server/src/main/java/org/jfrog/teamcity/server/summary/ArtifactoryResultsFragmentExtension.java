/*
 * Copyright (C) 2010 JFrog Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jfrog.teamcity.server.summary;

import jetbrains.buildServer.controllers.BuildDataExtensionUtil;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfrog.teamcity.common.ConstantValues;
import org.jfrog.teamcity.common.CustomDataStorageKeys;
import org.jfrog.teamcity.server.util.BuildNameProvider;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static org.jfrog.teamcity.common.ConstantValues.BUILD_NAME_SEPARATOR_LEGACY;
import static org.jfrog.teamcity.common.ConstantValues.PROP_FULL_BUILD_LINK;

/**
 * @author Noam Y. Tenne
 */
public class ArtifactoryResultsFragmentExtension extends SimplePageExtension {
    private SBuildServer server;
    private BuildNameProvider buildNameProvider;

    public ArtifactoryResultsFragmentExtension(@NotNull final PagePlaces pagePlaces,
                                               @NotNull final SBuildServer server,
                                               @NotNull final BuildNameProvider buildNameProvider) {
        super(pagePlaces, PlaceId.BUILD_RESULTS_FRAGMENT, ConstantValues.NAME,
                "artifactoryResultsFragmentExtension.jsp");
        this.server = server;
        this.buildNameProvider = buildNameProvider;
        register();
    }

    @Override
    public void fillModel(@NotNull final Map model, @NotNull final HttpServletRequest request) {
        SBuild build = getBuild(request);
        model.put("artifactoryBuildUrl", getBuildUrl(build));
    }

    @Override
    public boolean isAvailable(@NotNull HttpServletRequest request) {
        SBuild build = getBuild(request);
        if (build == null || !build.isFinished()) {
            return false;
        }
        Status buildStatus = build.getBuildStatus();
        return buildStatus.isSuccessful() && !buildStatus.isFailed() && StringUtils.isNotBlank(getBuildUrl(build));
    }

    @Nullable
    protected SBuild getBuild(final HttpServletRequest request) {
        return BuildDataExtensionUtil.retrieveBuild(request, server);
    }

    private String getBuildUrl(SBuild build) {
        if (build == null) {
            return null;
        }

        SBuildType buildType = build.getBuildType();

        if (buildType == null) {
            return null;
        }

        CustomDataStorage customDataStorage = buildType.getCustomDataStorage(CustomDataStorageKeys.RUN_HISTORY);

        final String buildIdStr = Long.toString(build.getBuildId());
        String buildUrl = customDataStorage.getValue(buildIdStr + "#" + PROP_FULL_BUILD_LINK);
        if (StringUtils.isNotBlank(buildUrl)) {
            return StringEscapeUtils.unescapeJava(buildUrl) + "/" + build.getBuildNumber();
        }

        for (SBuildRunnerDescriptor buildRunnerDescriptor : buildType.getBuildRunners()) {
             buildUrl = customDataStorage.getValue(buildIdStr + "#" + buildRunnerDescriptor.getId());
            if (StringUtils.isNotBlank(buildUrl)) {
                return buildUrl + buildNameProvider.getFullBuildName(build, BUILD_NAME_SEPARATOR_LEGACY) + "/" + build.getBuildNumber();
            } else {
                //Maintain backward compatibility for when there were no multi-runners and results were mapped per build
                buildUrl = customDataStorage.getValue(buildIdStr);
                if (StringUtils.isNotBlank(buildUrl)) {
                    return buildUrl + buildNameProvider.getFullBuildName(build, BUILD_NAME_SEPARATOR_LEGACY) + "/" + build.getBuildNumber();
                }
            }
        }
        return null;
    }
}