package com.agindustries.pl;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {

	private static final int COLUMNS = 7;
	private static final int ROWS = 6;
	private static final int CIRCLE_DIAMETER = 80;
	private static final String DISK_COLOUR1 = "24303E";
	private static final String DISK_COLOUR2 = "4CAA88";

	private static String PLAYER_ONE = "player one";
	private static String PLAYER_TWO = "player two";

	private boolean isPlayerOne = true;

	private Disk[][] insertedDiskArray = new Disk[ROWS][COLUMNS];

	private boolean isAllowToInsert = true;


	@FXML
	public GridPane rootGridPane;
	@FXML
	public Pane insertedDiskPane;
	@FXML
	public Label playerNameLabel;
	@FXML
	public TextField player1TextField;
	@FXML
	public TextField player2TextField;
	@FXML
	public Button setNameButton;

	public void createPlayGround(){
		Shape rectangleWithHole = createGameStructuralGrid();
		rootGridPane.add(rectangleWithHole,0,1);

		List<Rectangle> rectangleList = createClickableColumn();

		for (Rectangle rectangle : rectangleList) {
			rootGridPane.add(rectangle,0,1);
		}

	}

	public Shape createGameStructuralGrid(){
		Shape rectangleWithHole = new Rectangle((COLUMNS+1)*CIRCLE_DIAMETER,(ROWS+1)*CIRCLE_DIAMETER);

		for (int row = 0; row <= ROWS ; row++) {
			for (int column = 0; column < COLUMNS-1 ; column++) {
				Circle circle = new Circle();
				circle.setRadius(CIRCLE_DIAMETER/2);
				circle.setCenterX(CIRCLE_DIAMETER/2);
				circle.setCenterY(CIRCLE_DIAMETER/2);
				circle.setSmooth(true);

				circle.setTranslateX((CIRCLE_DIAMETER+5)*row + CIRCLE_DIAMETER/4);
				circle.setTranslateY((CIRCLE_DIAMETER+5)*column + CIRCLE_DIAMETER/4);

				rectangleWithHole = Shape.subtract(rectangleWithHole,circle);

			}

		}
		rectangleWithHole.setFill(Color.WHITE);
		return rectangleWithHole;
	}

	private List<Rectangle> createClickableColumn(){

		List<Rectangle> rectangleList = new ArrayList();

		for (int column = 0; column < COLUMNS ; column++) {
			Rectangle rectangle = new Rectangle(CIRCLE_DIAMETER,(ROWS+1)*CIRCLE_DIAMETER);
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX((CIRCLE_DIAMETER+5)* column +CIRCLE_DIAMETER/4);

			rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
			rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));
			final int col = column;
			rectangle.setOnMouseClicked(event -> {
				if(isAllowToInsert){
					isAllowToInsert = false;// when the disk is dropped the no more disk is inserted
				insertDisk(new Disk(isPlayerOne),col);
				}
			});

			rectangleList.add(rectangle);
		}
		return rectangleList;
	}

	private void insertDisk(Disk disk,int column){
		int row = ROWS - 1;
		while (row >= 0){
			if(getDiskIsPresent(row,column) == null)
				break;

			row--;
		}

		if(row < 0)
			return;

		insertedDiskArray[row][column] = disk;
		insertedDiskPane.getChildren().add(disk);
		int currentRow = row;
		 disk.setTranslateX((CIRCLE_DIAMETER+5)* column +CIRCLE_DIAMETER/4 );
		 TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5),disk);
		 transition.setToY((CIRCLE_DIAMETER+5) * row + CIRCLE_DIAMETER/4);
		 transition.setOnFinished(event -> {
		 	isAllowToInsert = true;// when disk is dropped allow next player to insert the disk

		 	if(gameEnded(currentRow,column))
		    {
		    	gameOver();
		    	return;
		    }

		 	isPlayerOne = !isPlayerOne;
		 	playerNameLabel.setText(isPlayerOne?PLAYER_ONE:PLAYER_TWO);

		 });
		 transition.play();

	}

	private boolean gameEnded(int row,int column){

		//vertical points ,assuming player inserted last disk at row=2,column=3
		//range of rows =0,1,2,3,4,5  --> Intstream
		//index of each element in column is 1,3  2,3  3,3  4,3  5,3 --> point2d x,y

		List<Point2D> verticalPoints = IntStream.rangeClosed( row -3 ,row +3)  //range of rows =0,1,2,3,4,5  --> Intstream
				                        .mapToObj(r -> new Point2D(r,column)) //1,3  2,3  3,3  4,3  5,3 --> point2d x,y
										.collect(Collectors.toList()) ;

		List<Point2D> horizontalPoints = IntStream.rangeClosed( column -3 , column +3)
				.mapToObj(col -> new Point2D(row,col))
				.collect(Collectors.toList()) ;

		Point2D startPoint1 = new Point2D(row - 3,column + 3);
		List<Point2D> diagonal1Points = IntStream.rangeClosed(0,6)
				.mapToObj(i -> startPoint1.add(i,-i))
				.collect(Collectors.toList());

		Point2D startPoint2 = new Point2D(row - 3,column - 3);
		List<Point2D> diagonal2Points = IntStream.rangeClosed(0,6)
				.mapToObj(i -> startPoint2.add(i,i))
				.collect(Collectors.toList());

		boolean isEnded = checkCombinations(verticalPoints) || checkCombinations(horizontalPoints)
							|| checkCombinations(diagonal1Points) || checkCombinations(diagonal2Points);

		return isEnded;
	}

	private boolean checkCombinations(List<Point2D> points) {

		int chain = 0;

		for (Point2D point: points) {


			int rowIndexForArray = (int) point.getX();
			int columnIndexForArray = (int) point.getY();
			Disk disk =getDiskIsPresent(rowIndexForArray,columnIndexForArray);

			if(disk != null && disk.isPlayerOneMove == isPlayerOne){
				chain++;
				if(chain == 4)
					return true;
			}else {
				chain = 0;
			}

		}
		return false;
	}

	private Disk getDiskIsPresent(int row,int column){
		if(row>=ROWS || row<0 || column>=COLUMNS || column<0)
			return null;
		return insertedDiskArray[row][column];
	}

	private void gameOver(){
		String winner = isPlayerOne ? PLAYER_ONE : PLAYER_TWO;

		Alert alert =new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("connect four");
		alert.setHeaderText("winner is "+ winner);
		alert.setContentText("want to play again ?");

		Platform.runLater(() -> {
			ButtonType yesButton = new ButtonType("Yes");
			ButtonType noButton = new ButtonType("No,Exit");
			alert.getButtonTypes().setAll(yesButton,noButton);
			Optional<ButtonType> btnClicked = alert.showAndWait();
			if(btnClicked.isPresent() && btnClicked.get() == yesButton){
				resetGame();
			}else {
				Platform.exit();
				System.exit(0);

			}
		});

	}

	public void resetGame() {
		insertedDiskPane.getChildren().clear(); // remove all inserted disk from pane

		for (int row = 0; row < insertedDiskArray.length ; row++) { //structurally // make all the element of insertedDiskArray to null
			for (int col = 0; col < insertedDiskArray[row].length; col++) {
				insertedDiskArray[row][col]=null;
			}
		}
		isPlayerOne = true; // let player one start the game
		playerNameLabel.setText(PLAYER_ONE);
		PLAYER_ONE = "player one";
		PLAYER_TWO="player two";
		player1TextField.clear();
		player2TextField.clear();
		setNameEvent();

		createPlayGround(); // prepare a fresh playground
	}

	private static class Disk extends Circle{
		private final boolean isPlayerOneMove;
		public Disk(boolean isPlayerOneMove){
			this.isPlayerOneMove=isPlayerOneMove;
			setRadius(CIRCLE_DIAMETER/2);
			setFill(isPlayerOneMove?Color.valueOf(DISK_COLOUR1):Color.valueOf(DISK_COLOUR2));
			setCenterX(CIRCLE_DIAMETER/2);
			setCenterY(CIRCLE_DIAMETER/2);
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		setNameEvent();

	}

	private void setNameEvent() {
		setNameButton.setOnAction(event -> {
			PLAYER_ONE = player1TextField.getText();
			PLAYER_TWO = player2TextField.getText();
			playerNameLabel.setText(PLAYER_ONE);
		});
	}
}
