<?php

//Download https://curl.se/ca/cacert.pem
//extension=php_openssl.dll
//extension=php_curl.dll
//extension_dir = ext

$stockId = 'AAPL';

if(isset($_GET['id'])){
    $stockId = $_GET['id'];
}

$ch=curl_init();
curl_setopt($ch,CURLOPT_URL,'https://api.nasdaq.com/api/quote/'.$stockId.'/info?assetclass=stocks');
curl_setopt($ch,CURLOPT_RETURNTRANSFER,true);
curl_setopt($ch,CURLOPT_HEADER, false);
$jsonString=curl_exec($ch);

if (curl_errno($ch)) {
    $error_msg = curl_error($ch);
    echo $error_msg;
}

curl_close($ch);

$jsonObject = json_decode($jsonString,true);
echo 'Pre lastSalePrice->'.$jsonObject['data']['primaryData']['lastSalePrice']."\n";
echo '  '.$jsonObject['data']['primaryData']['lastTradeTimestamp']."\n";
echo 'Las lastSalePrice->'.$jsonObject['data']['secondaryData']['lastSalePrice']."\n";
echo '  '.$jsonObject['data']['secondaryData']['lastTradeTimestamp']."\n";
