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
package com.adobe.cq.forms.core.graphql.datafetchers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Session;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.graphql.api.SlingDataFetcher;
import org.apache.sling.graphql.api.SlingDataFetcherEnvironment;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;

@Component(
    service = SlingDataFetcher.class,
    property = { "name=formsportal/asset" })
public class FormsAssetFetcher implements SlingDataFetcher<Object> {
    @Reference
    QueryBuilder queryBuilder;

    private Map<String, String> fetchResourceProperties(Resource r) {
        Map<String, String> result = new HashMap<>();
        ValueMap valueMap = r.getValueMap();
        if (valueMap.containsKey("jcr:title")) {
            result.put("title", valueMap.get("jcr:title", String.class));
        }
        if (valueMap.containsKey("jcr:description")) {
            result.put("description", valueMap.get("jcr:description", String.class));
        }
        result.put("path", r.getPath());
        return result;
    }

    @Override
    public Object get(SlingDataFetcherEnvironment env) throws Exception {
        List<Object> queryResult = new ArrayList<>();
        ResourceResolver serviceResourceResolver = env.getCurrentResource().getResourceResolver();
        Map<String, String> queryMap = new HashMap<String, String>();

        Session session = serviceResourceResolver.adaptTo(Session.class);
        queryMap.put("type", "nt:unstructured");
        queryMap.put("1_property", "cq:template");
        queryMap.put("1_property.value", "/conf/core-components-examples/settings/wcm/templates/content-page");

        PredicateGroup predicateGroup = PredicateGroup.create(queryMap);
        Query query = queryBuilder.createQuery(predicateGroup, session);
        SearchResult searchResult = query.getResult();

        // Query builder has a leaking resource resolver, so the following work around is required.
        ResourceResolver leakingResourceResolver = null;
        try {
            Iterator<Resource> resourceIterator = searchResult.getResources();
            while (resourceIterator.hasNext()) {
                Resource resource = resourceIterator.next();
                if (leakingResourceResolver == null) {
                    // Get a reference to QB's leaking resource resolver
                    leakingResourceResolver = resource.getResourceResolver();
                }
                queryResult.add(fetchResourceProperties(resource));
            }
        } finally {
            if (leakingResourceResolver != null) {
                // Always close the leaking query builder resource resolver
                leakingResourceResolver.close();
            }
        }
        return queryResult;
    }
}
