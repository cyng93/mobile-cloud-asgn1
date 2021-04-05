/*
 * 
 * Copyright 2014 Jules White
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
 * 
 */
package org.magnum.dataup;

import java.io.*;	// we need InputStream & OutputStream
import java.util.*;	// an easier way to import all container

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class VideoController {
	
	private Map<Long, Video> Videos = new HashMap<Long, Video>();
	private Long count = 0L;

	@ResponseBody /* ResponseBody is necessary or else we'll get a 500 Internal Server Error*/
	@GetMapping("/")
	public String getRoot() {
		String docs = "1st Coding Assignment for Coursera Course 'Building Cloud Services with the Java Spring Framework'";
		docs += "<br>Below API is supported:";
		docs += "<br> * GET /video";
		return docs;
	}

	@ResponseBody
	@GetMapping("/dummy")
	public Collection<Video> dummyNewVideoToList(){
		Video testVideo = Video.create().withContentType("video/mp4").withDuration(123).withSubject(UUID.randomUUID().toString()).withTitle(UUID.randomUUID().toString()).build();
		testVideo.setId(++count);
		Videos.put(testVideo.getId(), testVideo);
		return Videos.values();
	}

	@ResponseBody
	@GetMapping("/video")
	public Collection<Video> getVideo(){
		return Videos.values();
	}

	@ResponseBody
	@PostMapping("/video")
	public Video addVideo(@RequestBody Video v){
		v.setId(++count);
		String videoUrl = "http://localhost:8080/video/" + v.getId() + "/data";
		v.setDataUrl(videoUrl);
		Videos.put(v.getId(), v);
		return v;
	}

	@ResponseBody
	@PostMapping("/video/{id}/data")
	public VideoStatus setVideoData(@PathVariable Long id, @RequestBody MultipartFile videoData){
		Boolean videoExisted = Videos.containsKey(id);
		if (!videoExisted) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "video not found");
		}
		VideoStatus videoStatus = new VideoStatus(VideoState.PROCESSING);
		Video v = Videos.get(id);
		try {
			VideoFileManager videoFileManager = VideoFileManager.get();
			InputStream inStream = videoData.getInputStream();
			videoFileManager.saveVideoData(v, inStream);
			inStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		videoStatus.setState(VideoState.READY);
		return videoStatus;
	}

	@ResponseBody
	@GetMapping("/video/{id}/data")
	public ResponseEntity<Video> getVideoData(@PathVariable Long id){
		Boolean videoExisted = Videos.containsKey(id);
		if (!videoExisted) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "video not found");
		}
		return null;
	}
}