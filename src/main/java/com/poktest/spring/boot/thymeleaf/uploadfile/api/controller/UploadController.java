package com.poktest.spring.boot.thymeleaf.uploadfile.api.controller;

import com.poktest.spring.boot.thymeleaf.uploadfile.api.Model.UploadModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.rowset.serial.SerialBlob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
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

//        uploadedFileName.forEach(s->{
//            try {
//                final URI locationUrl = new URI(servletRequest.getRequestURI().toString()+"/")
//                        .resolve(s);
//                System.err.println("poktest : "+locationUrl);
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }
//            System.err.println("url");
//        });

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

//    @GetMapping("/files/{filename:.+}")
//    @ResponseBody
//    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
//
//        Resource file = storageService.loadAsResource(filename);
//        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
//                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
//    }

    @GetMapping("/files/{filename}")
    public HttpServletResponse getFile(HttpServletResponse response,@PathVariable("filename") String filename ){
        Path path = Paths.get(UPLOADED_FOLDER+filename);
        File file = path.toFile();

        try (OutputStream out = response.getOutputStream()) {

            Files.copy(path, out);
            out.flush();
        } catch (IOException e) {
            // handle exception
        }
        String mimeType = URLConnection.guessContentTypeFromName(file.getName());
        String contentDisposition = String.format("attachment; filename=%s", file.getName());
        int fileSize = Long.valueOf(file.length()).intValue();

        response.setContentType(mimeType);
        response.setHeader("Content-Disposition", contentDisposition);
        response.setContentLength(fileSize);
        return response;
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
