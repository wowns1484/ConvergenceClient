package com.view;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import com.db.model.MovieDTO;
import com.main.mainGUI;
import com.protocol.Protocol;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

public class MovieSearch implements Initializable
{
	// 영화 검색 테이블 뷰를 위한 리스트
	private ObservableList<MovieDTO> movie_list;
	// 현재 상영작인지 구분
	private String is_current;
	
	@FXML
	private BorderPane bp_parent;
	
	@FXML
	private TableView<MovieDTO> tv_movie;
	
	@FXML
	private TableColumn<MovieDTO, String> tc_screening;
	
	@FXML
	private TableColumn<MovieDTO, String> tc_movie_title;
	
	@FXML
	private TableColumn<MovieDTO, String> tc_start_date;
	
	@FXML
	private TableColumn<MovieDTO, String> tc_director;
	
	@FXML
	private TableColumn<MovieDTO, String> tc_actor;
	
	@FXML
	private TableColumn<MovieDTO, String> tc_screening_time;
	
	@FXML
	private TableColumn<MovieDTO, String> tc_plot;
	
	@FXML
	private Text t_result;
	
	@FXML
	private TextField tf_title;
	
	@FXML
	private TextField tf_director;
	
	@FXML
	private TextField tf_actor;
	
	@FXML
	private CheckBox cb_current;
	
	@FXML
	private CheckBox cb_close;
	
	@FXML
	private CheckBox cb_soon;
	
	@FXML
	private DatePicker dp_start_date;
	
	@FXML
	private DatePicker dp_end_date;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		try
		{
			// 테이블 뷰에 넣을 리스트 세팅
			movie_list = FXCollections.observableArrayList();
			
			// 리스트 초기화
			initList("%", "1976-01-01", "2222-01-01", "%", "%", "%");
			
			// 테이블 뷰 초기화
			tv_movie.getItems().clear();
			
			// 각 테이블뷰 컬럼에 어떠한 값이 들어갈 것인지 세팅
			tc_screening.setCellValueFactory(cellData -> cellData.getValue().getScreeningProperty());
			tc_movie_title.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
			tc_start_date.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReleaseDate().toString()));
			tc_director.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDirector()));
			tc_actor.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getActor()));
			tc_screening_time.setCellValueFactory(cellData -> new SimpleStringProperty(Integer.toString(cellData.getValue().getMin())));
			tc_plot.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPlot()));
			
			// 테이블 뷰와 리스트를 연결
			tv_movie.setItems(movie_list);
			
			tv_movie.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<MovieDTO>()
			{
				@Override
				public void changed(ObservableValue<? extends MovieDTO> observable, MovieDTO oldValue, MovieDTO newValue)
				{
					try
					{
						// 테이블 뷰 행 클릭시 바로 상세 정보 페이지로
						FXMLLoader loader = new FXMLLoader(MovieTable.class.getResource("./xml/user_sub_page/movie_detail.fxml"));
						Parent root = loader.load();
						MovieDetail controller = loader.<MovieDetail>getController();
						controller.initData(tv_movie.getSelectionModel().getSelectedItem());
						
						UserMain.user_sub_root.setCenter(root);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					
				}
			});
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@FXML // 해당하는 영화 검색
	void getMovieSearch(ActionEvent event) throws Exception
	{
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
		// 데이터 입력 없을 시 전체 영화 검색하도록 데이터 설정
		String title = tf_title.getText().equals("") ? "%" : tf_title.getText();
		String start_date = dp_start_date.getValue() == null ? "1976-01-01" : dp_start_date.getValue().format(dateFormat);
		String end_date = dp_end_date.getValue() == null ? "2222-01-01" : dp_end_date.getValue().format(dateFormat);
		String current = is_current == null ? "%" : is_current;
		String director = tf_director.getText().equals("") ? "%" : tf_director.getText();
		String actor = tf_actor.getText().equals("") ? "%" : tf_actor.getText();
		
		tv_movie.getItems().clear();
		initList(title, start_date, end_date, current, director, actor);
		tv_movie.setItems(movie_list);
	}
	
	@FXML // 상영종료 선택
	void selectClose(ActionEvent event)
	{
		cb_current.setSelected(false);
		cb_soon.setSelected(false);
		is_current = "0";
	}
	
	@FXML // 상영중 선택
	void selectCurrent(ActionEvent event)
	{
		cb_soon.setSelected(false);
		cb_close.setSelected(false);
		is_current = "1";
	}
	
	@FXML // 상영예정 선택
	void selectSoon(ActionEvent event)
	{
		cb_current.setSelected(false);
		cb_close.setSelected(false);
		is_current = "2";
	}
	
	// 입력 필드 초기화
	@FXML
	void initBtn(ActionEvent event)
	{
		UserMain.loadPage("movie_search");
	}
	
	// 테이블뷰에 들어갈 리스트 초기화
	private void initList(String title, String start_date, String end_date, String is_current, String director, String actor)
	{
		try
		{
			// 검색하고자 하는 범위의 해당하는 영화리스트 요청
			mainGUI.writePacket(Protocol.PT_REQ_VIEW + "`" + Protocol.CS_REQ_MOVIE_VIEW + "`" + title + "`" + start_date + "`" + end_date + "`" + is_current + "`" + director + "`" + actor + "`0");
			
			while (true)
			{
				
				String packet = mainGUI.readLine(); // 요청 응답 수신
				String packetArr[] = packet.split("`"); // 패킷 분할
				String packetType = packetArr[0];
				String packetCode = packetArr[1];
				
				if (packetType.equals(Protocol.PT_RES_VIEW) && packetCode.equals(Protocol.SC_RES_MOVIE_VIEW))
				{
					String result = packetArr[2]; // 요청 결과
					
					switch (result)
					{
						case "1": // 요청 성공
						{
							String movieList = packetArr[3];
							String listArr[] = movieList.split("\\{"); // 각 영화별로 분할
							
							for (String listInfo : listArr)
							{
								String infoArr[] = listInfo.split("\\|"); // 영화 별 정보 분할
								String mv_id = infoArr[0];
								String mv_title = infoArr[1];
								String mv_release_date = infoArr[2];
								String mv_is_current = infoArr[3];
								String mv_plot = infoArr[4];
								String mv_poster_path = infoArr[5];
								String mv_stillCut_path = infoArr[6];
								String mv_trailer_path = infoArr[7];
								String mv_director = infoArr[8];
								String mv_actor = infoArr[9];
								int mv_min = Integer.parseInt(infoArr[10]);
								
								movie_list.add(new MovieDTO(mv_id, mv_title, mv_release_date, mv_is_current, mv_plot, mv_poster_path, mv_stillCut_path, mv_trailer_path, mv_director, mv_actor, mv_min));
							}
							return;
						}
						case "2": // 검색된 영화 리스트 없음
						{
							return;
						}
						case "3": // 요청 실패
						{
							mainGUI.alert("오류", "영화 리스트 요청 실패했습니다.");
							return;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
