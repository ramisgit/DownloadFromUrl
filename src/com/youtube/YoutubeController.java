package com.youtube;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert.AlertType;

public class YoutubeController {
	@FXML TextField url_input;
	@FXML TextField optional_file_name;
	@FXML Button download_btn;
	@FXML AnchorPane panel;
	@FXML VBox vbox_container;
	private URL downloadURL;
	public void download_btn_event(ActionEvent event) {
		init();
		sendHTTPRequest.restart(); 


	}
	public void initialize() {
		
		
		Button add_btn = new Button("+");
		vbox_container.getChildren().add(add_btn);
		
		add_btn.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				int size_of_vbox = vbox_container.getChildren().size();
				Button hold_btn = (Button) vbox_container.getChildren().get(size_of_vbox - 1);
				vbox_container.getChildren().remove(hold_btn);
				
				
				TextField temp_field = new TextField();
				vbox_container.getChildren().addAll(temp_field, hold_btn);
				
				
			}
		});
		
	}
	public void init() {
		ProgressBar pbar = new ProgressBar(0);
		pbar.setPrefWidth(200);
		panel.getChildren().add(pbar);
		/*Event is triggered when the sendHTTP request service completed successfully*/  
			sendHTTPRequest.setOnSucceeded((WorkerStateEvent we) -> {  
			try {  
				downloadURL = new URL(getURLS(sendHTTPRequest.getValue()));  
				pbar.progressProperty().unbind();  
				pbar.setProgress(0);  
				pbar.progressProperty().bind(VideoDownload.progressProperty());  
				pbar.setVisible(true);  
				/*if everything goes right then it will start a new service to download the video*/  
				VideoDownload.restart();  
			} catch (MalformedURLException ex) {  
				Alert msg = new Alert(AlertType.INFORMATION);  
				msg.setTitle("Message from Youtube Downloader");  
				msg.setContentText("Invalid Url");  
				msg.showAndWait();  
	            }  
	        });  
	        /*Event is fired when videDownload service is completed successfully*/  
	        VideoDownload.setOnSucceeded((WorkerStateEvent we) -> {  
	            boolean val = VideoDownload.getValue();  
	            System.out.println(val);  
	            if (val) {  
	                Alert msg = new Alert(AlertType.INFORMATION);  
	                msg.setTitle("Message from Youtube Downloader");  
	                msg.setContentText("Download complete");  
	                msg.showAndWait();
	            } else {  
	                Alert msg = new Alert(AlertType.INFORMATION);  
	                msg.setTitle("Message from Youtube Downloader");  
	                msg.setContentText("Download Failed");  
	                msg.showAndWait();  
	            }  
	            pbar.setVisible(false);  
	        });  
			
	      //add url_input to pane && optional_file_name && download_btn && pbar
		
	}
	private String getVideoID(String url) {  
        int index = url.indexOf("v=");  
        String id="";
        index += 2;  
        for (int i = index; i < url.length(); i++) id += url.charAt(i);  
        return id;  
    }  
    /*This service send the HTTP Request to the youtube server. In response the youtube server 
    sends the video information. This information contains the url in the encoded format. This 
    method decode the url return it as a StringBuilder Object*/  
    final private Service < StringBuilder > sendHTTPRequest = new Service < StringBuilder > () {  
        @Override  
        protected Task < StringBuilder > createTask() {  
            return new Task < StringBuilder > () {  
                @Override  
                protected StringBuilder call() {  
                    String response;  
                    StringBuilder res = new StringBuilder();  
                    StringBuilder refinedres = new StringBuilder();  
                    try {  
                        URL url = new URL("https://www.youtube.com/get_video_info?&video_id=" + getVideoID(url_input.getText().trim()));  
                        System.out.println(url.toString());  
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
                        conn.setRequestMethod("GET");  
                        System.out.println(conn.getResponseMessage());  
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));  
                        while ((response = in .readLine()) != null) {
                        	res.append(response);  
                        }
                        refinedres.append(URLDecoder.decode(URLDecoder.decode(res.toString(), "UTF-8"), "UTF-8")); in .close();  
                        return refinedres;  
                    } catch (MalformedURLException ex) {} catch (IOException ex) {}  
                    return null;  
                }  
            };  
        }  
    };  
    /*This service will download the videos using the URL*/  
    Service < Boolean > VideoDownload = new Service < Boolean > () {  
        @Override  
        protected Task < Boolean > createTask() {  
            return new Task < Boolean > () {  
                @Override  
                protected Boolean call() throws Exception {  
                    long length;  
                    boolean completed = false;  
                    int count = 0;  
                    try (BufferedInputStream bis = new BufferedInputStream(downloadURL.openStream()); FileOutputStream fos = new FileOutputStream(optional_file_name.getText().length() == 0 ? "video.mp4" : optional_file_name.getText().concat(".mp4"))) {  
                        length = downloadURL.openConnection().getContentLength();  
                        int i = 0;  
                        final byte[] data = new byte[1024];  
                        while ((count = bis.read(data)) != -1) {  
                            i += count;  
                            fos.write(data, 0, count);  
                            updateProgress(i, length);  
                        }  
                        completed = true;  
                    } catch (IOException ex) {}  
                    return completed;  
                }  
            };  
        }  
    };  
    /*This methid receives refined response as a paramter and extract the url from the 
    response which will be used to download the video from the youtube*/  
    private String getURLS(StringBuilder response) {  
        StringBuilder temp1 = new StringBuilder();  
        String[] temp2, temp3, temp4;  
        try {  
            int index = response.indexOf("url_encoded_fmt_stream_map");  
            for (int i = index; i < response.length(); i++) {  
                temp1.append(response.charAt(i));  
            }  
            temp2 = temp1.toString().split("&url=");  
            if (temp2.length > 0) {  
                temp3 = temp2[1].split(";");  
                if (temp3.length > 0) {  
                    temp4 = temp3[0].split(",");  
                    if (temp4.length > 0) return temp4[0];  
                    else return temp3[0];  
                } else return temp2[1];  
            }  
        } catch (Exception e) {  
            Alert msg = new Alert(AlertType.INFORMATION);  
            msg.setTitle("Message form youtube Downloader");  
            msg.setContentText("Error in downloading");  
            msg.showAndWait();  
        }  
        return null;  
    }  
	
	
	
	
	
	
	
	
}
