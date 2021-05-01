/*******************************************************************************
 * Copyright 2021 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

(function() {
    var NS = 'cmp',
        IS = 'formssearch',
        ATTR_PREFIX = "data-" + NS + "-hook-" + IS;

    var selectors = {
        item: {
            self: "[" + ATTR_PREFIX + "=\"item\"]",
            title: "[" + ATTR_PREFIX + "=\"itemTitle\"]",
            formLink:  "[" + ATTR_PREFIX + "=\"formLink\"]"
        }
    };

    var componentConfig,
        componentNode, resultsNode, itemTemplate,
        updateSearchResults = function (queryResults) {
            console.log(queryResults);
            resultsNode.innerHtml = "";
            queryResults.data.forEach(function(item) {
                // choice of tag doesn't matter here because it's unwrapped
                var el = document.createElement("span");
                el.innerHTML = itemTemplate;
                el.querySelector(selectors.item.title).appendChild(document.createTextNode(item.title));
                el.querySelector(selectors.item.formLink).setAttribute("href", item.path);
                el.querySelector(selectors.item.formLink).appendChild(document.createTextNode("HTML"));
                resultsNode.innerHTML += el.innerHTML;
            })
        },
        queryFPAssets = function () {
            fetch(componentConfig.queryPath)
                .then(response => response.json())
                .then(data => updateSearchResults(data));
        },
        initializeSearchAndListerComponent = function (config) {
            componentConfig = config;
            componentNode = document.getElementById(config.id);
            resultsNode = componentNode.querySelector("[data-cmp-hook-formssearch=\"results\"]");
            itemTemplate = componentNode.querySelector("[data-cmp-hook-formssearch=\"itemTemplate\"]").innerHTML;
            queryFPAssets();
        },
        tmpEvent = new CustomEvent("searchnlister-onload", {
            detail: {
                searchAndLister: {
                    initializeComponent: initializeSearchAndListerComponent
                }
            }
        });

    // Using newer CustomEvent api instead of deprecated initCustomEvent
    window.dispatchEvent(tmpEvent);

}());