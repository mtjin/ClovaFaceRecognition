package com.mtjinse.clovafacereco.network;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/*
* // 닮은 유명인을 찾은 경우
{
 "info": {
   "size": {
     "width": 900,
     "height": 675
   },
   "faceCount": 2
 },
 "faces": [{
   "celebrity": {
     "value": "안도하루카",
     "confidence": 0.266675
   }
 }, {
   "celebrity": {
     "value": "서효림",
     "confidence": 0.304959
   }
 }]
}

// 닮은 유명인을 찾지 못한 경우
{
	"info": {
		"size": {
			"width": 768,
			"height": 1280
		},
		"faceCount": 0
	},
	"faces": []
}*/



//요청파라미터
/*
* [HTTP Request Header]
POST /v1/vision/celebrity HTTP/1.1
Host: openapi.naver.com
Content-Type: multipart/form-data; boundary={boundary-text}
X-Naver-Client-Id: {앱 등록 시 발급받은 Client ID}
X-Naver-Client-Secret: {앱 등록 시 발급 받은 Client Secret}
Content-Length: 96703

--{boundary-text}
Content-Disposition: form-data; name="image"; filename="test.jpg"
Content-Type: image/jpeg

{image binary data}
--{boundary-text}--
*
* */
public interface NaverService {
    //https://openapi.naver.com/v1/vision/celebrity

    //Multipart,Part  파일종류(압축파일)보낼떄 쓰는 라이브러리이다.
    @Multipart
    @POST("/v1/vision/celebrity")
    Call<NaverResult> postImage(@Header("X-Naver-Client-Id") String id,
                                @Header("X-Naver-Client-Secret") String secret,
                                @Part MultipartBody.Part File);
}
