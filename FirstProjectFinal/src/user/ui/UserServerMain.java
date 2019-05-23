package user.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class UserServerMain extends Application {
	ExecutorService executorService;					//submit 받은 스레드들을 스케쥴링 하면서 적절하게 일을 처리한다.
	ServerSocket serverSocket;							//클라이언트의 소켓 연결 요청을 처리하는 서버소켓 객체
	List<Client> connections = new ArrayList<Client>();	//연결된 클라이언트를 담는 List 객체
	
	
	//1.accept스레드 : 클라이언트 연결 요청을 수락
	//ACCEPT의 연결만 받고 바로 다시 WHILE문으로 들어가서 ACCEPT 대기
	public void startServer() {			
		
		//runtime에서 현재 가지고 있는 CPU만큼 thread를 생성
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		try {
			serverSocket = new ServerSocket();		
			serverSocket.bind(new InetSocketAddress("192.168.168.22", 5001));	//로컬 ip주소와 5001 포트와 연결
		} catch(Exception e) {
			if(!serverSocket.isClosed()) { stopServer(); }					//에러 발생시 소켓을 닫음
			return;
		}
		
		Runnable runnable = new Runnable() {								//스레드에 구현할 task를 작성 후 executer에 제출
			@Override
			public void run() {
				Platform.runLater(new Runnable(){				//이벤트큐에 저장된 runnable을 먼저 실행하고 이 runnable작업 실행
					@Override									//UI에 작업할 내용을 구현
					public void run() {							//이후에는 람다식을 활용하여 쉽게 구현
						displayText("[SIRIAI 서버 시작]");
						btnStartStop.setText("stop");						
					}
				});		
				
				while(true) {
					try {
						Socket socket = serverSocket.accept();	//클라이언트의 연결 요청을 받기 위한 대기 상태 및 연결된 소켓 객체를 리턴
						String message = "[Siriai가 연결을 수락합니다 : " + socket.getRemoteSocketAddress()+" ]";
						Platform.runLater(()->displayText(message));
		
						Client client = new Client(socket);
						connections.add(client);
						Platform.runLater(()->displayText("[연결 개수: " + connections.size() + "]"));
					} catch (Exception e) {
						if(!serverSocket.isClosed()) { stopServer(); }
						break;
					}
				};
			}
		};
		executorService.submit(runnable);
	}
	
	//서버를 멈추는 메소드
	public void stopServer() {
		try {
			
			//Iterator를 통해 각 client 객체의 소켓을 닫는다.
			Iterator<Client> iterator = connections.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			
			//서버 소켓을 닫는다.
			if(serverSocket!=null && !serverSocket.isClosed()) { 
				serverSocket.close(); 
			}
			
			//스레드풀을 관리하는 객체를 닫는다.
			if(executorService!=null && !executorService.isShutdown()) { 
				executorService.shutdown(); 
			}
			Platform.runLater(()->{
				displayText("[SIRIAI 서버 종료]");
				btnStartStop.setText("start");
			});
		} catch (Exception e) { }
	}	

	// 소켓을 가진 클라이언트 객체
	class Client {
		Socket socket;
		
		Client(Socket socket) {
			this.socket = socket;
			receive();			//소켓을 생성하면서 서버에 연결된 클라이언트의 대화 내용을 RECEIVE
		}
		

		//2. receive 스레드 : 클라이언트의 대화 내용을 받는 기능.
		public void receive() {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						while(true) {
							byte[] byteArr = new byte[300];
							InputStream inputStream = socket.getInputStream();
							
							//클라이언트가 비정상 종료를 했을 경우 IOException 발생
							int readByteCount = inputStream.read(byteArr);
							
							//클라이언트가 정상적으로 Socket의 close()를 호출했을 경우
							if(readByteCount == -1) {  throw new IOException(); }
							
							String message = "[요청 처리 : " + socket.getRemoteSocketAddress() + " ]";
							Platform.runLater(()->displayText(message));
							
							String data = new String(byteArr, 0, readByteCount, "UTF-8");
							
							for(Client client : connections) {	//connections 리스트에 담긴 클라이언트 객체들에게 input내용을 전달
								client.send(data); 		//inputstream으로 받아온 메세지를 send 메서드로 각 클라이언트에 전달
							}
						}
					} catch(Exception e) {
						try {
							connections.remove(Client.this);	//오류 발생시 커넨셕에서 이 클라이언트 제거	
							String message = "[클라이언트 통신 안됨: " + socket.getRemoteSocketAddress() + "]";
							Platform.runLater(()->displayText(message));
							socket.close();						//오류 발생시 해당 클라이언트 접속을 종료
						} catch (IOException e2) {}
					}
				}
			};
			executorService.submit(runnable);	//위에서 작업한 runnable의 task 스레드 내용을 executorService에 제출
		}
	
		
		//3. send스레드 : 클라이언트에게서 받은 메세지를 모든 클라이언트들에게 전달.
		public void send(String data) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						byte[] byteArr = data.getBytes("UTF-8");
						//현재 만들어지는 Client 객체의 outputstream 빨대를 get하여 파라미터로 전달받은 data내용을 전달
						OutputStream outputStream = socket.getOutputStream();
						outputStream.write(byteArr);
						outputStream.flush();
					} catch(Exception e) {
						try {
							String message = "[클라이언트 통신 안됨: " + socket.getRemoteSocketAddress() + "]";
							Platform.runLater(()->displayText(message));
							connections.remove(Client.this);
							socket.close();
						} catch (IOException e2) {}
					}
				}
			};
			executorService.submit(runnable);
		}
	}
	
	//////////////////////////////////////////////////////

	TextArea txtDisplay;	//연결됨을 표시하는 TextArea 생성
	Button btnStartStop;	//연결 시작, 멈춤을 다루는 버튼 생성
	
	@Override
	public void start(Stage primaryStage) throws Exception {
	
		BorderPane root = new BorderPane();
		root.setPrefSize(500, 300);

		//연결됨을 표시하는 textarea 설정
		txtDisplay = new TextArea();
		txtDisplay.setEditable(false);
		BorderPane.setMargin(txtDisplay, new Insets(0,0,2,0));
		root.setCenter(txtDisplay);
		
		//연결 시작, 멈춤을 다루는 버튼 설정
		btnStartStop = new Button("start");
		btnStartStop.setPrefHeight(30);
		btnStartStop.setMaxWidth(Double.MAX_VALUE);
		btnStartStop.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent e) {
				if(btnStartStop.getText().equals("start")) {
					startServer();
				} else if(btnStartStop.getText().equals("stop")){
					stopServer();
				}			
			}
		});
		root.setBottom(btnStartStop);
		
		//scene에 borderpane을 전달하여 stage에 전달
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("app.css").toString());
		primaryStage.setScene(scene);
		primaryStage.setTitle("Siriai 서버");
		primaryStage.setOnCloseRequest(event->stopServer());
		primaryStage.show();
		
	}

	//textarea에 text내용을 붙이기.
	public void displayText(String text) {
		txtDisplay.appendText(text + "\n");
	}	
	
	public static void main(String[] args) {
		launch(args);
	}
}