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
    var operationResponseHandler = function (data) {
        var result = data.operationResult.result;
        if (result.status == "success") {
            //Dor
            var bytes = new Uint8Array(result.dorData);
            var blob=new Blob([bytes], {type: result.dorContentType});
            var link=document.createElement('a');
            link.href=window.URL.createObjectURL(blob);
            link.download=result.dorName;
            link.click();
        }
    };

    var signDorOperation = {
        name: "signDor",
        handler: function (event, operationData) {
            var parameters = new URLSearchParams();
            parameters.append("operation", operationData.name);
            parameters.append("operation_model_id", operationData.operation_model_id);
            var queryPath = operationData.queryPath + "?" + parameters.toString();

            fetch(queryPath)
                .then(response => response.json())
                .then(data => operationResponseHandler(data));
        }
    };

    $(function() {
        $(window).trigger("core-forms-register-operation", signDorOperation);
    });
}(jQuery));