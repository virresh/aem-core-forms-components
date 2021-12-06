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
package com.adobe.cq.forms.core.components.models.adaptiveforms;

import org.osgi.annotation.versioning.ConsumerType;

import com.adobe.cq.wcm.core.components.models.Component;

/**
 * Defines the sling model for {@code /apps/core/fd/components/review} component.
 *
 * @since com.adobe.cq.forms.core.components.models.adaptiveforms 1.0.0
 */
@ConsumerType
public interface Review extends Component {
    /**
     * Returns the title of Review Component
     *
     * @return Title of component
     * @since com.adobe.cq.forms.core.components.models.adaptiveforms 1.0.0
     */
    default String getTitle() {
        throw new UnsupportedOperationException();
    }

    default String getRuntimeType() {
        throw new UnsupportedOperationException();
    }
}
