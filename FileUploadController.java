package com.mypackage.fileuploader.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;


import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.mypackage.fileuploader.model.UploadedFile;

@Controller
public class FileUploadController {

	@RequestMapping("/")
	public String home() {

		return "fileUploader";
	}

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public @ResponseBody
	List<UploadedFile> upload(MultipartHttpServletRequest request,
			HttpServletResponse response) throws IOException {

		// Getting uploaded files from the request object
		Map<String, MultipartFile> fileMap = request.getFileMap();

		// Maintain a list to send back the files info. to the client side
		List<UploadedFile> uploadedFiles = new ArrayList<UploadedFile>();

		// Iterate through the map
		for (MultipartFile multipartFile : fileMap.values()) {

			// Save the file to local disk
			saveFileToLocalDisk(multipartFile);

			UploadedFile fileInfo = getUploadedFileInfo(multipartFile);

			// adding the file info to the list to be shown as server response
			uploadedFiles.add(fileInfo);
		}

		return uploadedFiles;
	}

	@RequestMapping(value = { "/list" })
	public String listBooks(Map<String, Object> map) throws IOException {

		File folder = new File(getDestinationLocation());
		List<UploadedFile> fileList = new ArrayList<UploadedFile>();
		for (File fileEntry : folder.listFiles()) {
			if (!fileEntry.isDirectory()) {
				fileList.add(mapFileToPojo(fileEntry));
				System.out.println(fileEntry.getName());
			}
		}
		map.put("fileList", fileList);
		return "/listFiles";
	}

	@RequestMapping(value = "/get/{fileName}", method = RequestMethod.GET)
	public void getFile(HttpServletResponse response,
			@PathVariable String fileName) throws IOException {

		UploadedFile dataFile = null;
		File folder = new File(getDestinationLocation());
		for (File fileEntry : folder.listFiles()) {
			if (!fileEntry.isDirectory()) {
				if(fileEntry.getName().contains(fileName))
					dataFile = mapFileToPojo(fileEntry);
			}
		}

		File file = new File(getDestinationLocation(), dataFile.getName());
		if(file.exists()){
			System.out.println("File found at location*******"+file.getCanonicalPath());
		}
		try {
			response.setContentType(dataFile.getType());
			response.setHeader("Content-disposition", "attachment; filename=\""
					+ dataFile.getName() + "\"");

			FileCopyUtils.copy(FileUtils.readFileToByteArray(file),
					response.getOutputStream());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveFileToLocalDisk(MultipartFile multipartFile)
			throws IOException, FileNotFoundException {

		String outputFileName = getOutputFilename(multipartFile);

		FileCopyUtils.copy(multipartFile.getBytes(), new FileOutputStream(
				outputFileName));
	}

	private String getOutputFilename(MultipartFile multipartFile) {

		return getDestinationLocation() + multipartFile.getOriginalFilename();
	}

	private UploadedFile getUploadedFileInfo(MultipartFile multipartFile)
			throws IOException {

		UploadedFile fileInfo = new UploadedFile();
		fileInfo.setName(multipartFile.getOriginalFilename());
		fileInfo.setSize(multipartFile.getSize());
		fileInfo.setType(multipartFile.getContentType());
		fileInfo.setLocation(getDestinationLocation());

		return fileInfo;
	}

	private String getDestinationLocation() {
		String rootPath = System.getProperty("catalina.home");
		return rootPath + "\\temp\\";
	}

	private UploadedFile mapFileToPojo(File fileEntry) throws IOException {
		MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		UploadedFile fileInfo = new UploadedFile();
		fileInfo.setName(fileEntry.getName());
		fileInfo.setSize(fileEntry.length());
		fileInfo.setType(mimeTypesMap.getContentType(fileEntry));
		fileInfo.setLocation(fileEntry.getCanonicalPath());

		return fileInfo;
	}
}
//frenmanoj/dropzonejs-springmvc
