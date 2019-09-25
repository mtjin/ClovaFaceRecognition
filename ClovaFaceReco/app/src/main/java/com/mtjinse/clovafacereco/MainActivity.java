package com.mtjinse.clovafacereco;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.makeramen.roundedimageview.RoundedImageView;
import com.mtjinse.clovafacereco.network.Celebrity;
import com.mtjinse.clovafacereco.network.Face;
import com.mtjinse.clovafacereco.network.NaverResult;
import com.mtjinse.clovafacereco.network.NaverService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    //승인받고자하는 카메라와 저장소에 사용 권한들을 먼저 변수에 담아둡니다.
    private String[] cameraPermissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    //카메라, 저장소 권한 코드
    private static final int CAMERA_PERMISSIONS = 101;

    //카메라앱권한코드
    private static final int CAMERA_PICK = 0;
    //포스터를 저장할 파일의 경로를 담을 임시 변수
    String currentPosterPath;
    //저장한 포스터의 파일의 이름을 담을 변수
    String currentPosterName;

    //앱 내부저장소의 파일경로
    String dirPath;
    //앱 내부저장소의 이미지를 저장할 폴더이름
    static final String PHOTO_FOLDER = "/photo";

    Retrofit retrofit;
    NaverService naverService;

    private RoundedImageView photo_image;
    private TextView face_result;
    private TextView guide_line;
    private Button photo_add_btn;
    private LinearLayout refresh_btn;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createDirectoryFolder();
        //사진이 저장될 폴더의 경로를 담아주는 코드
        dirPath = getFilesDir().getAbsolutePath() + PHOTO_FOLDER;

        retrofit = new Retrofit.Builder()
                .baseUrl("https://openapi.naver.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        naverService = retrofit.create(NaverService.class);

        //다이얼로그
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("서버통신중~~");

        photo_add_btn = findViewById(R.id.photo_add_btn);
        face_result = findViewById(R.id.face_result);
        guide_line = findViewById(R.id.guide_line);
        photo_image = findViewById(R.id.photo_image);

        refresh_btn = findViewById(R.id.refresh_btn);

        photo_add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCameraPermissions(CAMERA_PERMISSIONS) == true) {
                    takePhoto();
                } else {
                    showNoPermissionToastMassage(CAMERA_PERMISSIONS);
                }
            }
        });

        refresh_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCameraPermissions(CAMERA_PERMISSIONS) == true) {
                    takePhoto();
                } else {
                    showNoPermissionToastMassage(CAMERA_PERMISSIONS);
                }
            }
        });


    }

    // 카메라 및 저장소 권한을 사용자로부터 승인받았는지 확인
    private boolean checkCameraPermissions(int permissionsCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissionList = new ArrayList<>();

            for (String permission : cameraPermissions) {
            /*checkSelfPermission()은 권한이 승인이 되었는지 여부를 확인하는 메서드
              context : 어떤 Activity에서 권한승인여부를 체크하는지에 대한 정보값
              permission : 승인되었는지 여부를 확인할 권한
              result는 권한이 승인되었는지 여부에 대한 결과값을 담을 변수*/
                int result = ActivityCompat.checkSelfPermission(MainActivity.this, permission);
                //PackageManager : 권한이 승인되었는지 여부의 값을 확인해서 리턴해주는 클래스입니다.
                if (result != PackageManager.PERMISSION_GRANTED) {
                    //승인되지 않은 권한이 있으면 미리 만들어 두었던 permissionList에 추가
                    permissionList.add(permission);
                }
            }

            //승인되지 않은 권한(permission)을 사용자에게 승인해달라고 요청
            if (!permissionList.isEmpty()) {

         /* activity : 어떤 Activity에서 권한을 승인요청하는지의 값
            permissions [] : 권한을 승인받고자하는 권한들의 배열
            requestCode : 어떤 권한에 대해 승인을 요청했는지 결과값을 돌려을때 구분하기 위한 값
            requestPermissions()는 승인되지 않은 권한을 승인해달라고 요청하는 메서드*/
                ActivityCompat.requestPermissions(this, permissionList.toArray(new String[]{}), permissionsCode);
                //모든 권한이 승인되지 않은 경우에는 false를 리턴
                return false;
            }
            //모든 권한이 승인된 경우에는 true를 리턴
            return true;
        } else {
            //앱을 설치한 스마트폰의 sdk버전이 마시멜로우(6.0)버전 미만인 경우에 실행되는 로직
            // 6.0 이상인 경우에는 권한이 이미 승인이 되었기 때문에 무조건 true을 리턴
            return true;
        }
    }

    /**
     * @param requestCode  : 권한을 승인할때 어떤 권한에 대해 승인 요청을 했는지 구분을 위해 넣었던 값
     * @param permissions  : 권한 승인을 요청했던 권한
     * @param grantResults : 승인을 요청한 권한에 대해서 승인여부의 결과값
     */
    //Permission 승인여부를 리턴받는 코드
    //위에서 요청한 requestPermissions()의 결과를 받으면 실행되는 메서드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_PERMISSIONS:
                //요청권한 다 승낙했을 경우 실행
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    //다 권한을 수락했기 때문에 저장소 및 카메라 접근 가능
                    takePhoto();
                } else {
                    //Dialog에서 사용자가 위험권한 승인을 거부한 경우에 실행
                    showNoPermissionToastMassage(CAMERA_PERMISSIONS);
                }
                break;
        }
    }

    /**
     * @param type : 카메라와 갤러리에 해당하는 권한인지, 아니면 위치정보에 대한 권한인지 구분하기 위한 값
     */
    private void showNoPermissionToastMassage(int type) {
        switch (type) {
            case CAMERA_PERMISSIONS:
                Toast.makeText(MainActivity.this, "카메라 기능을 사용하기 위해서는 권한 동의가 필요합니다!", Toast.LENGTH_SHORT).show();
                break;
         /*   case GALLERY_PERMISSIONS:
                Toast.makeText(MainActivity.this, "갤러리 기능을 사용하기 위해서는 권한 동의가 필요합니다!", Toast.LENGTH_SHORT).show();
                break;*/
        }
    }

    //사진이 저장될 파일을 생성하는 메서드
    public File createImageFile() throws IOException {
        //createDirPath();

        //파일명으로 사진을 찍은 시점의 시간을 지정
        String fileName = new SimpleDateFormat("yyyyMMddHHss").format(new Date());

        //사진을 저장할 파일을 생성
        /**
         * @param parent : 생성한 파일을 저장할 상위폴더의 경로입니다.
         * @param child : 생성할 파일의 명입니다.
         */
        File imageFile = new File(dirPath, fileName + ".jpg");
        //파일이 생성이 안될수도 있어서 다시한번 createNewFile메소드를 통해서 다시한번 생성하는 코드입니다.
        imageFile.createNewFile();
        return imageFile;
    }

    //사진을 저장할 디렉토리 폴더를 생성하는 메서드
    private void createDirectoryFolder() {
        String dirPath = getFilesDir().getAbsolutePath();
        String createFolderPath = dirPath + PHOTO_FOLDER;
        File createFolder = new File(createFolderPath);

        //폴더가 생성이 안되는 경우도 있어서 생성이 안되었으면 생성하라는 코드를 추가작성
        if (!createFolder.exists()) {
            createFolder.mkdirs();
        }
    }

    //기본 카메라 앱을 열어 촬영하고 사진을 저장하는 메서드
    private void takePhoto() {
        //카메라 기능을 가진 앱을 열어달라고 요청하기 위한 암시적 인텐트를 생성
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            //찍은 사진을 저장할 파일을 미리 생성
            //4번에서 만들었던 메서드를 사용하여 파일을 생성
            File imgFile = createImageFile();

            //파일이 이미 존재하는 경우에 실행되는 코드
            if (imgFile.exists()) {
                //Uri를 다른앱에 파일의 경로를 전달하고자 할때 전체 파일의 경로를 전달하면 에러가 발생합니다.
                //임시로 파일에 접근할 수 있는 경로(Uri)를 전달해줘야 합니다.
                Uri photoUri;
                /*안드로이드 7.0버전 이상인 경우에만 FileProvider를 사용해서 생성한 파일에 대한 임시접근권한을 부여합니다. 스마트폰의 버전이 7.0이상인지를 체크하는 코드입니다.*/
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                /* 메서드에서 필요한 매개변수에 대해서 알아보겠습니다.
                   context : 어떤 Activity에서 FileProvider를 사용하는지에 대한 정보값
                   authority : 패키지명 + .fileprovider 입니다.
                   file : 접근권한을 부여할 파일.*/
                    //생성한 파일의 경로를 임시로 접근할 수 있는 경로(Uri)로 변환하는 코드
                    photoUri = FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName() + ".fileprovider", imgFile);
                } else {
                    //안드로이드 7.0버전 미만인 경우에는 바로 파일경로를 전달합니다.
                    photoUri = Uri.fromFile(imgFile);
                }

                //미리 선언해두었던 포스터를 저장할 파일의 경로를 담기 위한 변수에 경로를 대입
                currentPosterPath = imgFile.getAbsolutePath();
                //미리 선언해두었던 저장한 사진의 파일의 이름을 담을 변수에 파일명을 대입
                currentPosterName = "/" + imgFile.getName();
                //EXTRA_OUTPUT : 전달하는 파일의 경로에 사진을 저장해달라고 요청
                // uri : 저장하고자하는 파일의 경로
                // 내가 생성해서 전달한 파일에 사진을 저장
                camera.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                // 기본 카메라 앱을 여는 메서드
                // CAMERA_PICK은 카메라 앱을 사용했다가 돌아왔다는 것을 구분하게 해줄 값
                startActivityForResult(camera, CAMERA_PICK);
            }
        } catch (IOException e) {
            //파일을 생성하지 못하는 경우
            e.printStackTrace();
            //파일을 생성하지 못했을 때 사용자에게 실패했다고 알려주는 메시지를 띄우는 코드
            Toast.makeText(MainActivity.this, "사진을 저장할 파일을 생성하지 못했습니다", Toast.LENGTH_SHORT).show();
        }
    }

    // 다른 Activity에서 현재 Activity로 화면이 전환되면 실행되는 메서드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CAMERA_PICK:
                if (resultCode == RESULT_OK) {

                    //사진압축하고 사진경로받아오기
                    currentPosterPath = compressPhoto(currentPosterPath, dirPath);
                    //파일의 경로로 파일을 Activity에서 사용할 수 있게 객체로 가지고 오는 코드
                    File file = new File(currentPosterPath); //currenPosterPath에 내가 담은 사진이 들어가있다.
                    Glide.with(this)
                            .load(file)
                            .into(photo_image);

                    //네이버에 사진파일을 압축해서 보내는 코드
                    RequestBody reBody = RequestBody.create(MediaType.parse("image/jpeg"), file); //이거와 밑에줄 코드는 이렇게 멀티바디파트로 보내라고 api에서 써있다
                    MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), reBody);

                    progressDialog.show();

                    naverService.postImage("wq8iJ9fP29nfVpJlKkH1", "hJFt1AlWrO", body).enqueue(new Callback<NaverResult>() {
                        @Override
                        public void onResponse(Call<NaverResult> call, Response<NaverResult> response) {
                            if (response.isSuccessful()) {
                                List<Face> faceList = response.body().getFaces();
                                if (faceList != null && faceList.size() != 0) {
                                    //첫번쨰로 닮은 연예인 정보를 가져오는 코드
                                    Celebrity celebrity = faceList.get(0).getCelebrity();
                                    String name = celebrity.getValue();
                                    double confidence = celebrity.getConfidence() * 100;
                                    double percent = Double.parseDouble(String.format("%.2f", confidence));

                                    String result = "당신은 " + name + "와 " + percent + "% 닮았습니다.!!";
                                    face_result.setText(result);

                                    progressDialog.dismiss();
                                } else {
                                    face_result.setText("닮은 유명인을 찾지 못하였습니다.");
                                    progressDialog.dismiss();
                                }
                            } else {
                                face_result.setText("닮은 유명인을 찾지 못하였습니다.");
                                progressDialog.dismiss();
                            }
                        }

                        @Override
                        public void onFailure(Call<NaverResult> call, Throwable t) {
                            progressDialog.dismiss();
                        }
                    });

                    photo_add_btn.setVisibility(View.GONE);
                    guide_line.setVisibility(View.GONE);

                    refresh_btn.setVisibility(View.VISIBLE);
                }
                break;
        }
    }


    /**
     * @param originalPhotoPath : 사진을 회전시키고 압축시킬 원본 사진 파일의 경로
     * @param dircetoryPath     : 압축시켜 저장할 원본사진의 폴더경로
     * @return currentPosterPath : 압축하고 회전시킨 사진파일의 경로
     */
    //원본 사진파일을 가지고와서 사진을 회전시키고 압축하여 원하는 파일 경로에 저장
    private String compressPhoto(String originalPhotoPath, String dircetoryPath) {
        //원본사진 파일을 가지고 와서 사진을 압축시켜 저장한 후
        //압축된 사진 파일로부터 가져온 비트맵을 회전시켜서 압축된 사진파일에 덮어씀
        Bitmap originalPhoto = null;
        Bitmap resizePhoto = null;

        //원본 사진파일을 찾아서 가져옴
        File file = new File(originalPhotoPath);

        //원본 사진의 메타정보를 가지고와서 사진을 회전된 각도에 맞게 회전
        try {

            //압축하여 저장할 사진파일의 이름을 생성
            String imgFileName = System.currentTimeMillis() + ".jpg";

            //찍은 사진을 압축해서 다른 사진파일로 저장하는 코드
            File compressFile = new Compressor(this)
                    .setMaxWidth(500)
                    .setMaxHeight(750)
                    .setQuality(85)
                    .setCompressFormat(Bitmap.CompressFormat.WEBP)
                    .setDestinationDirectoryPath(dircetoryPath)
                    .compressToFile(file, imgFileName);


            //사진을 압축하여 저장한 후 원본 사진파일을 찾아서 삭제
            File originfile = new File(originalPhotoPath);
            if (originfile.exists()) {
                originfile.delete();
            }

            //압축하여 저장한 사진파일이 있으면 찾아서 변수에 파일 경로와 이름을 재지정
            if (compressFile.exists()) {
                currentPosterPath = compressFile.getAbsolutePath();
                currentPosterName = "/" + compressFile.getName();

                //회전을 시키기전 원본 사진파일로부터 비트맵을 생성
                originalPhoto = BitmapFactory.decodeFile(currentPosterPath);

                //원본 사진의 정보를 찾아서 가져오는 코드
                ExifInterface exifInterface = new ExifInterface(currentPosterPath);

                //사진이 얼만큼 회전되어있는지의 정보값 가져오는 코드
                // defaultValue : 0이면 회전이 안된 것
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                // 사진을 회전시켜야 할 각도
                int rotationAngle = 0;

                //비트맵을 회전시키기 위한 Matrix클래스
                Matrix matrix = new Matrix();
                //가지고온 사진이 회전되어있는 각도에 따라서 얼마만큼 bitmap을 회전시켜야되는지 계산
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotationAngle = 90;
                        matrix.postRotate(rotationAngle);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotationAngle = 180;
                        matrix.postRotate(rotationAngle);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotationAngle = 270;
                        matrix.postRotate(rotationAngle);
                        break;
                }

                /** @param source  원본이 될 비트맵
                 * @param x        원본 비트맵의 최초의 x좌표
                 * @param y        원본 비트맵의 최초의 y좌표
                 * @param width    원본 비트맵의 넓이의 px수
                 * @param height   원본 비트맵의 높이의 px수
                 * @param m        적용해줄 정보를 가지고있는 matrix
                 * @param filter   넣어준 값을 적용해줄지의 여부*/
                //비트맵을 회전시키는 코드
                resizePhoto = Bitmap.createBitmap(originalPhoto, 0, 0, originalPhoto.getWidth(), originalPhoto.getHeight(), matrix, true);

                //회전시킨 비트맵을 저장할 사진파일에 덮어쓸 준비
                //FileOutPutStream으로 덮어쓸, 저장하고자 하는 파일을 지정
                FileOutputStream outputStream = new FileOutputStream(currentPosterPath);

                /**
                 * @param format   어떤 형태로 파일을 압축할지의 여부를 지정
                 * @param quality  얼마만큼 압축을 할지의 여부, 100은 원본의 품질, 0은 최대한 작게 압축
                 * @param stream   덮어쓰기할 파일에 접근할 수 있는 FileOutputStream 객체
                 */
                //회전시킨 비트맵을 지정한 사진파일에 덮어쓰는 코드
                resizePhoto.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            } else {
                //압축되지 못한 사진파일의 경로를 리턴
                return currentPosterPath;
            }

        } catch (IOException e) {
            //사진의 정보를 찾아온 못한 경우
            e.printStackTrace();
        }
        //압축하고 사진을 회전시켜 저장한 파일의 경로를 리턴
        return currentPosterPath;
    }
}
