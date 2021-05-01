/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Copyright 2021 Adobe
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

package com.adobe.cq.forms.core.components.internal.models.v1.formsportal;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.adobe.cq.forms.core.components.internal.models.v1.AbstractComponentImpl;
import com.adobe.cq.forms.core.components.models.formsportal.SearchAndLister;

@Model(
    adaptables = SlingHttpServletRequest.class,
    adapters = { SearchAndLister.class, ComponentExporter.class },
    resourceType = SearchAndListerImpl.RESOURCE_TYPE)
@Exporter(
    name = ExporterConstants.SLING_MODEL_EXPORTER_NAME,
    extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class SearchAndListerImpl extends AbstractComponentImpl implements SearchAndLister {
    public static final String RESOURCE_TYPE = "core/fd/components/formsportal/searchnlister/v1/searchnlister";
    private static final String DEFAULT_TITLE = "Forms Portal";
    private static final String FOLDER_PATHS = "assetFolders";
    private static final String ASSET_SOURCES = "assetSources";
    private static final String PN_FOLDER = "folder";
    private static final String PN_TYPE = "type";

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    @Inject
    private String title;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    @Inject
    private String layout;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    @Default(booleanValues = false)
    @Inject
    private boolean disableSearch;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    @Default(booleanValues = false)
    @Inject
    private boolean disableTextSearch;

    @ChildResource(injectionStrategy = InjectionStrategy.OPTIONAL)
    @Named(FOLDER_PATHS)
    private List<Resource> folderPathChildResources;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    @Inject
    @Default(longValues = 8)
    private long limit;

    private String[] folderPathsArr;

    @Override
    public String getTitle() {
        if (StringUtils.isEmpty(title)) {
            // using isEmpty instead of isBlank to allow only whitespace in title
            return DEFAULT_TITLE;
        }
        return title;
    }

    @Override
    public String getLayout() {
        if (StringUtils.isEmpty(layout)) {
            return "Card";
        }
        return layout;
    }

    @Override
    public boolean getAdvancedSearchDisabled() {
        return disableSearch;
    }

    @Override
    public boolean getTextSearchDisabled() {
        return disableTextSearch;
    }

    @Override
    public String[] getFolderPathsArr() {
        if (folderPathsArr != null && folderPathsArr.length > 0) {
            return folderPathsArr;
        }
        if ((folderPathChildResources != null) && !folderPathChildResources.isEmpty()) {
            List<String> arr = new ArrayList<>();
            for (Resource r : folderPathChildResources) {
                arr.add(r.getValueMap().get(PN_FOLDER, String.class));
            }
            folderPathsArr = arr.toArray(new String[0]);
        }
        return folderPathsArr;
    }

    @Override
    public long getResultLimit() {
        return limit;
    }
}
