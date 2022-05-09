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
//https://api.nasdaq.com/api/quote/AAPL/info?assetclass=stocks
curl_setopt($ch,CURLOPT_RETURNTRANSFER,true);
curl_setopt($ch,CURLOPT_HEADER, false);
$jsonString=curl_exec($ch);

if (curl_errno($ch)) {
    $error_msg = curl_error($ch);
    echo $error_msg;
}

curl_close($ch);

$jsonObject = json_decode($jsonString,true);

$result = [];
if($jsonObject['data']['secondaryData']!=null){
    $result['price']=substr($jsonObject['data']['secondaryData']['lastSalePrice'],1);
}else{
    $result['price']=substr($jsonObject['data']['primaryData']['lastSalePrice'],1);
}
echo json_encode($result);
/*
api.nasdaq.com

/api/quote/([A-Za-z0-9]+)/info

request.getHeaders().clear();
request.addHeader("Accept-Encoding","gzip, deflate");
request.addHeader("Accept-Language","en-US,en;q=0.9");
request.addHeader("User-Agent","Java-http-client/");

return true;*/