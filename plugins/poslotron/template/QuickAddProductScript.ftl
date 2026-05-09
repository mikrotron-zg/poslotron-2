<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<#--
    Client-side glue for the QuickAddProduct form:
      * hide the productName row whenever the "same as internal name"
        checkbox is ticked, and keep productName in sync with
        internalName so the createProduct service receives the right
        value on submit even though productName is hidden from the user;
      * mirror productWeight into the hidden shippingWeight input so
        both columns are stored with the same gram value.
-->
<script type="text/javascript">
(function () {
    function init() {
        var form = document.querySelector('form[name="QuickAddProduct"]');
        if (!form) { return; }
        var cb = form.querySelector('input[type="checkbox"][name="useInternalAsName"]');
        var internal = form.querySelector('input[name="internalName"]');
        var pname = form.querySelector('input[name="productName"]');
        var pweight = form.querySelector('input[name="productWeight"]');
        var sweight = form.querySelector('input[name="shippingWeight"]');
        if (!cb || !internal || !pname) { return; }
        var pnameRow = pname.closest ? pname.closest('tr') : null;

        function applyName() {
            var checked = cb.checked;
            if (pnameRow) { pnameRow.style.display = checked ? 'none' : ''; }
            if (checked) { pname.value = internal.value; }
        }

        function applyWeight() {
            if (pweight && sweight) { sweight.value = pweight.value; }
        }

        cb.addEventListener('change', applyName);
        internal.addEventListener('input', function () {
            if (cb.checked) { pname.value = internal.value; }
        });
        if (pweight) {
            pweight.addEventListener('input', applyWeight);
        }
        form.addEventListener('submit', function () {
            if (cb.checked) { pname.value = internal.value; }
            applyWeight();
        });

        applyName();
        applyWeight();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
</script>
