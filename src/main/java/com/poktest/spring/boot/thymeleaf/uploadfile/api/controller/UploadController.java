package com.poktest.spring.boot.thymeleaf.uploadfile.api.controller;

import com.poktest.spring.boot.thymeleaf.uploadfile.api.Model.UploadModel;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class UploadController {
    private static final Logger log = LoggerFactory.getLogger(UploadController.class);
    private static String UPLOADED_FOLDER = "D:\\upload\\";

    @PostMapping("/api/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile uploadFile){
        log.info("single file uplaod");
        if(uploadFile.isEmpty()){
            return new ResponseEntity("please select a file!", HttpStatus.OK);
        }
        try {
            saveUploadedFiles(Arrays.asList(uploadFile));
        }catch (IOException e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity("SuccessFully upload - "+uploadFile.getOriginalFilename(),new HttpHeaders(),HttpStatus.OK);
    }


    @PostMapping("/api/upload/multi")
    public ResponseEntity<?> uploadFileMulti(@RequestParam("extraField") String extraField
            , @RequestParam("files") MultipartFile[] uploadFiles ,
                                             HttpServletRequest servletRequest){
        log.info("multi file upload");
        List<String> uploadedFileName = Arrays.stream(uploadFiles).map(x->x.getOriginalFilename())
                .filter(x->!StringUtils.isEmpty(x)).collect(Collectors.toList());
        if(uploadedFileName==null || uploadedFileName.size()<=0){
            return new ResponseEntity("please select a File!",HttpStatus.OK);
        }
        try {
            saveUploadedFiles(Arrays.asList(uploadFiles));
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Map<String,List<String>> map = new HashMap<>();
        map.put("uploadSuccessName",uploadedFileName);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @PostMapping("/api/upload/multi/model")
    public ResponseEntity<?> multiUploadFileModel(@ModelAttribute UploadModel model){
        log.info("Multiple file upload! with UploadModel");
        try {
            saveUploadedFiles(Arrays.asList(model.getFiles()));
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity("Successfully uploaded!", HttpStatus.OK);
    }

//    *** this method has show error getOutputStream() has already been called for this response
//    @GetMapping("/files/{filename}")
//    public HttpServletResponse getFile(HttpServletResponse response,@PathVariable("filename") String filename ){
//        Path path = Paths.get(UPLOADED_FOLDER+filename);
//        File file = path.toFile();
//        OutputStream out = null;
//        try  {
//            out = response.getOutputStream();
//            Files.copy(path, out);
//            out.flush();
//        } catch (IOException e) {
//            // handle exception
//        } finally {
//            if(null != out) {
//                try {
//                    out.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        String mimeType = URLConnection.guessContentTypeFromName(file.getName());
//        String contentDisposition = String.format("attachment; filename=%s", file.getName());
//        int fileSize = Long.valueOf(file.length()).intValue();
//
//        response.setContentType(mimeType);
//        response.setHeader("Content-Disposition", contentDisposition);
//        response.setContentLength(fileSize);
//        return response;
//    }
    @GetMapping( value="/download/{filename}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadFile(@PathVariable("filename") String filename) throws IOException {
//        File file = new File(UPLOADED_FOLDER+filename);
//        //init array with file length
//        byte[] bytesArray = new byte[(int) file.length()];

        // Nio Method
        byte[] bFile = Files.readAllBytes(Paths.get(UPLOADED_FOLDER+filename));
        return new ResponseEntity<>(bFile, HttpStatus.OK);
    }

    @GetMapping("/file/{filename}")
    public void  getFile(HttpServletResponse response ,@PathVariable("filename") String filename) throws IOException {
        File file = new File(UPLOADED_FOLDER+filename);
        InputStream in = new FileInputStream(file);
        response.setContentType(HttpHeaders.CONTENT_DISPOSITION);
        IOUtils.copy(in, response.getOutputStream());
    }

    private void saveUploadedFiles(List<MultipartFile> files) throws IOException {
        File directory = new File(UPLOADED_FOLDER);
        if (!directory.exists()){
            directory.mkdirs();
        }
        for (MultipartFile file : files) {
            if(file.isEmpty()){
                continue; // next process
            }
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOADED_FOLDER+file.getOriginalFilename());
            Files.write(path,bytes);
        }
    }

}
