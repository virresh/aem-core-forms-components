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
 
(function($, guidelib) {

    /**
     * COMPONENT['<viewName>'] = {
     *      className: string,
     *      viewInjector: function(guideNode, guideBridge) {}
     * }
     */

    var COMPONENT = {};
    const UPDATE_EVENT = "cmp-review-update-event";
    const REGISTER_EVENT = 'core-forms-review-register-view';
    
    const RUNTIME_TYPE_ATTR = "cmp-runtime-type";

    const SELECTORS = {
        'container': ".cmp-review-container"
    };

    COMPONENT.init = function () {
        // called only after guideBridge is available
        COMPONENT.bridge.on("elementNavigationChanged" , function(event, payload) {
            var component = payload.target;
            console.log("element navigation event triggered!");
        });
    };

    COMPONENT.getHTMLView = function (node, componentID) {
        if (COMPONENT[node.className]) {
            return COMPONENT[node.className].viewInjector(node, COMPONENT[componentID]);
        }

        if (COMPONENT['default']) {
            return COMPONENT['default'].viewInjector(node, COMPONENT[componentID]);
        }

        var nodeView = $("<div/>").text("Could not find view!");
        return nodeView;
    };

    COMPONENT.buildViews = function (node, elementViews) {
        var currentView = elementViews[node.somExpression];
        if (currentView) {
            if (node.items) {
                node.items.forEach(element => {
                    var childview = COMPONENT.buildViews(element, elementViews);
                    if (childview) {
                        currentView.append(childview);
                    }
                });
            }
            return currentView;
        }
    };

    COMPONENT.update = function (componentID) {
        var parentContainer = $("#"+componentID);
        COMPONENT[componentID] = COMPONENT[componentID] || { 'id': componentID, 'guidebridge': COMPONENT.bridge };
        COMPONENT[componentID][RUNTIME_TYPE_ATTR] = parentContainer.data(RUNTIME_TYPE_ATTR);

        // ToDo: Modify below logic to somehow fetch components from specific panels defined
        // in component authoring
        var elementViews = {};
        /**
         * Structure of a node:
         * 'som' : {
         *      som: string,
         *      view: jQuery Object containing it's review HTML
         * }
         * 'som': jquery object
         */
        COMPONENT.bridge.visit(function(node) {
            if (node.className === "rootPanelNode") {
                COMPONENT[componentID].rootNode = node.somExpression;
            }
            if (node.className === "guideNode" && node.jsonModel && node.jsonModel['sling:resourceType'] === COMPONENT[componentID][RUNTIME_TYPE_ATTR]) {
                // this is review component node itself. Clear out it's parent view
                delete elementViews[node.parent.somExpression];
                return;
            }
            else if (node.parent && (node.parent instanceof guidelib.model.GuideCompositeField)) {
                // don't iterate on children of composite fields
                return;
            }
            else {
                elementViews[node.somExpression] = COMPONENT.getHTMLView(node, componentID);
            }
            // Note: View Appending is intentionally not done here to honor the deleted parent view
            // otherwise we will end up rendering the parent panel of review component without any body
        });
        COMPONENT[componentID].html = elementViews;

        // Re-Render the component with updated data. ToDo: implement just view updates to prevent re-rendering
        parentContainer.empty();
        parentContainer.append(COMPONENT.buildViews(COMPONENT.bridge.resolveNode(COMPONENT[componentID].rootNode), elementViews));
 
        // trigger event for component update
        $("#" + componentID).trigger(UPDATE_EVENT, COMPONENT[componentID]);
    };

    if (window.guideBridge) {
        COMPONENT.bridge = window.guideBridge;
        COMPONENT.init();
    }
    else {
        window.addEventListener("bridgeInitializeStart", function(evnt) {
            COMPONENT.bridge = evnt.detail.guideBridge;
            COMPONENT.init();
        });
    }

    $(function () {
        $(SELECTORS.container).on("focus", function (event) {
            // ToDo: add a check to update only if relevant panels have been updated
            var tgt_id = event.target.id;
            // if (!COMPONENT[tgt_id]) {
            // }
            COMPONENT.update(tgt_id);
        });
    });
    
    $(window).on(REGISTER_EVENT, function(event, viewConfig) {
        if (viewConfig && viewConfig.className) {
            COMPONENT[viewConfig.className] = viewConfig;
        }
    });

    // ToDo: remove this
    window.rcmp = COMPONENT;
}(jQuery, guidelib));