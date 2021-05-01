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
package com.adobe.cq.forms.core.components.internal.servlets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.jackrabbit.vault.util.JcrConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestParameterMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.forms.core.components.internal.models.v1.formsportal.SearchAndListerImpl;
import com.day.cq.i18n.I18n;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.methods=" + HttpConstants.METHOD_GET,
        "sling.servlet.resourceTypes=" + SearchAndListerImpl.RESOURCE_TYPE,
        "sling.servlet.selectors=" + QueryFormsPortalServlet.SELECTOR,
        "sling.servlet.extensions=" + QueryFormsPortalServlet.EXTENSION
    })
public class QueryFormsPortalServlet extends SlingAllMethodsServlet {
    public static final String SELECTOR = "fpquery";
    public static final String EXTENSION = "json";

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryFormsPortalServlet.class);

    private static String METADATA_NODE_PATH = "jcr:content/metadata";
    private static String JCR_CONTENT_NODE_PATH = "jcr:content";

    private static final String QB_GROUP = "_group.";
    private static final String QB_PATH = "_path";
    private static final String DEFAULT_PATH = "/content/dam/formsanddocuments";

    @Reference
    private QueryBuilder queryBuilder;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Map<String, Object> results = new HashMap<>();
        try {
            I18n i18n = new I18n(request);
            RequestParameterMap parameterMap = request.getRequestParameterMap();
            ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(null);
            results.put("data", queryFormsAssets(i18n, parameterMap, resourceResolver));
            results.put("success", "true");
        } catch (Exception ex) {
            LOGGER.error("Exception while processing FP Query {}", ex.getMessage(), ex);
            results.put("success", "false");
        }
        new ObjectMapper().writeValue(response.getWriter(), results);
    }

    private List<Map<String, Object>> queryFormsAssets(I18n i18n, RequestParameterMap parameterMap,
        ResourceResolver serviceResourceResolver) {
        Session session = serviceResourceResolver.adaptTo(Session.class);
        List<Map<String, Object>> resultMap = new ArrayList<>();
        Map<String, String> queryMap = new HashMap<>();

        queryMap.put("type", "dam:Asset");

        int predicateID = 0;    // used across all parameters

        for (Map.Entry<String, RequestParameter[]> entry : parameterMap.entrySet()) {
            String paramName = entry.getKey();
            RequestParameter[] paramValue = entry.getValue();
            int paramCount = paramValue.length;

            if (paramName.equals("path")) {
                int pathID = 0;
                while (paramCount-- > 0) {
                    queryMap.put(predicateID + QB_GROUP + pathID + QB_PATH, paramValue[pathID].getString());
                    pathID++;
                }
                queryMap.put(predicateID + "_group.p.or", Boolean.TRUE.toString());
            }
            predicateID++;
        }

        if (!parameterMap.containsKey("path")) {
            // put a default path in case one doesn't exist
            queryMap.put(predicateID + QB_PATH, DEFAULT_PATH);
        }

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
                resultMap.add(fetchResourceProperties(resource));
            }
        } finally {
            if (leakingResourceResolver != null) {
                // Always close the leaking query builder resource resolver
                leakingResourceResolver.close();
            }
        }

        return resultMap;
    }

    private Map<String, Object> fetchResourceProperties(Resource r) {
        Map<String, Object> result = new HashMap<>();
        ValueMap valueMap = r.getValueMap();
        ValueMap metaDataMap = r.getChild(METADATA_NODE_PATH).getValueMap();
        if (metaDataMap.containsKey("title")) {
            result.put("title", metaDataMap.get("title", String.class));
        } else if (valueMap.containsKey(JcrConstants.JCR_TITLE)) {
            result.put("title", valueMap.get(JcrConstants.JCR_TITLE, String.class));
        }
        if (metaDataMap.containsKey("dc:description")) {
            result.put("description", metaDataMap.get("dc:description", String.class));
        } else if (valueMap.containsKey(JcrConstants.JCR_DESCRIPTION)) {
            result.put("description", valueMap.get(JcrConstants.JCR_DESCRIPTION, String.class));
        }
        result.put("path", r.getPath());
        return result;
    }
}
