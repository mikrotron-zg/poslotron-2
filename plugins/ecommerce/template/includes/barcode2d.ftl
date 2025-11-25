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

<head>
    <script src="/ecommerce/js/pdf417-min.js"></script>
    <script src="/ecommerce/js/bcmath-min.js"></script>


    <script language="JavaScript" type="text/javascript">
        window.onload = function() { 

            generate('${currencyUomId}', ${orderGrandTotalCents}, ${orderId}, "Narudžba " + ${orderId});

        }

        function generate(currency, value, module, description) {
            
            var val = value.toString();
            
            var textToEncode = "HRVHUB30\n" + 
                currency + "\n" +
                zeroes(15 - val.length) +
                val + "\n\n\n\n" +
                "MIKROTRON d.o.o.\n" +
                "PAKOŠTANSKA 5 K2-9\n" +
                "10000 ZAGREB\n" +
                "HR8023400091110675464\n" +
                "HR00\n" +
                module + "\n\n" +
                description;
            
            console.log(textToEncode);
            
            PDF417.init(textToEncode);             
            var barcode = PDF417.getBarcodeArray();

            // block sizes (width and height) in pixels
            var bw = 2;
            var bh = 1;

            // create canvas element based on number of columns and rows in barcode
            var container = document.getElementById('barcode');
            //container.removeChild(container.firstChild);

            var canvas = document.createElement('canvas');
            canvas.width = bw * barcode['num_cols'];
            canvas.height = bh * barcode['num_rows'];
            container.appendChild(canvas);

            var ctx = canvas.getContext('2d');                    

            // graph barcode elements
            var y = 0;
            // for each row
            for (var r = 0; r < barcode['num_rows']; ++r) {
                var x = 0;
                // for each column
                for (var c = 0; c < barcode['num_cols']; ++c) {
                    if (barcode['bcode'][r][c] == 1) {                        
                        ctx.fillRect(x, y, bw, bh);
                    }
                    x += bw;
                }
                y += bh;
            }       
        }
        
        function zeroes(numberOfZeroes) {
            var zeroes = '0';
            for (var i = 1; i < numberOfZeroes; i++) {
                zeroes += '0';
            }
            return zeroes;
        }
    </script>
</head>

<div id="barcode"></div>
