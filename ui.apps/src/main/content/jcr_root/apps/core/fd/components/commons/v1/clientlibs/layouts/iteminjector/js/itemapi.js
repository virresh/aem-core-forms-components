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

(function($) {
    var  NS = "cmp",
         TEMPLATE = 'item-template',
         ATTR_PREFIX = "data-" + NS + "-hook-" + TEMPLATE;

    var selectors = {
        self: "[" + ATTR_PREFIX + "=\"item\"]",
        title: "[" + ATTR_PREFIX + "=\"itemTitle\"]",
        formLink:  "[" + ATTR_PREFIX + "=\"formLink\"]",
        description:  "[" + ATTR_PREFIX + "=\"description\"]",
    };

    var addTextWithTooltip = function (element, text, tooltip) {
        if (text) {
            element.appendChild(document.createTextNode(text));
            element.setAttribute("title", tooltip || text);
        }
    };

    var ItemAPI = {
        tester: function () {
            console.log("Item API is available!!!");
        },
        createAndInject: function (template, data, container) {
            /* data has schema:
                {
                    title,
                    description,
                    tooltip,
                    formLink
                }
            */
            // choice of tag doesn't matter here because it's unwrapped
            var el = document.createElement("span");
            el.innerHTML = template;

            var titleElem = el.querySelector(selectors.title),
                linkElem = el.querySelector(selectors.formLink),
                descElem = el.querySelector(selectors.description);

            addTextWithTooltip(titleElem, data.title);
            addTextWithTooltip(descElem, data.description);
            addTextWithTooltip(linkElem, "HTML", data.tooltip)
            linkElem.setAttribute("href", data.formLink);

            container.innerHTML += el.innerHTML;
        }
    };

    var onScriptLoad = function (event, additionalParams) {
        var componentApp = additionalParams.detail.searchAndLister;
        componentApp.initializeItemAPI(ItemAPI);
    };

    $(window).on("core-forms-itemapi-onload", onScriptLoad);
}(jQuery));