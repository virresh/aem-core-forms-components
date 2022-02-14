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

    const REGISTER_EVENT = 'core-forms-review-register-view';
    var NS = 'cmp',
        IS = 'review',
        ATTR_PREFIX = "data-" + NS + "-hook-" + IS + '-template';

    var SELECTORS = {
        TEMPLATE: "[" + ATTR_PREFIX + "~=\"default\"]",
        LABEL: "[" + ATTR_PREFIX + "~=\"label\"]",
        VALUE: "[" + ATTR_PREFIX + "~=\"value\"]",
        ACTION: "[" + ATTR_PREFIX + "~=\"action\"]",
        CONTAINER: "[" + "data-" + NS + "-is" + "~=\"container\"]"
    };

    var generateFromTemplate = function (guideNode, componentConfig) {
        if (!guideNode) {
            return;
        }
        else if (guideNode.className === "guideButton") {
            return;
        }
        // if this clientlib is loaded, then the corresponding template script must be already available
        var template = document.querySelector(SELECTORS.TEMPLATE);
        var bridge = componentConfig.guidebridge;

        if(!template || !guideNode.visible || !guideNode.enabled || !guideNode._isItem || !guideNode.parent.className==="guideTermsAndConditions") {
            // a view should not be generated
            return document.createElement("div");
        } 

        // choice of tag doesn't matter here because it's unwrapped
        var el = document.createElement("span");
            el.innerHTML = template.innerHTML;
        
        var row = el.querySelector(SELECTORS.CONTAINER),
            title = row.querySelector(SELECTORS.LABEL),
            value = row.querySelector(SELECTORS.VALUE),
            action = row.querySelector(SELECTORS.ACTION);

        if (guideNode.title && title) {
            title.textContent = guideNode.title;
        }

        if (guideNode.formattedValue && value) {
            if (guideNode.allowRichText) {
                value.innerHTML = guideNode.formattedValue;
            }
            else {
                value.textContent = guideNode.formattedValue;
            }
        }
        else if (guideNode.value && value) {
            if (guideNode.allowRichText) {
                value.innerHTML = guideNode.value;
            }
            else {
                value.textContent = guideNode.value;
            }
        }
        else if (guideNode._isItem && guideNode.className !== 'guidePanel' && value) {
            value.textContent = "-";
        }

        if (guideNode._isItem && guideNode.className !== 'guidePanel') {
            var btn = document.createElement("button");
            btn.textContent = "Edit";
            btn.addEventListener("click", function (event) {
                event.preventDefault();
                bridge.setFocus(guideNode.somExpression);
            });
            action.appendChild(btn);
        }

        if (guideNode.className === 'guidePanel') {
            if (guideNode.parent.className === 'rootPanelNode') {
                row.classList.add('cmp-review-form__panel');
            }
            else {
                row.classList.add('cmp-review-form__sub-panel');
            }
            value.remove();
            action.remove();
        }

        if (guideNode.className === 'guideTextDraw') {
            action.remove();
            title.remove();
            value.innerHTML = value.textContent;
        }

        return row;
    };

    var viewConfig = {
        className: "default",
        viewInjector: generateFromTemplate
    }

    $(function() {
        $(window).trigger(REGISTER_EVENT, viewConfig);
    });
}(jQuery));