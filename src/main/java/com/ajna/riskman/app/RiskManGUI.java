package com.ajna.riskman.app;

 
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;

import java.awt.Event;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.ajna.riskman.dao.DaoController;
import com.ajna.riskman.md.MarketDataClientInterface;
import com.ajna.riskman.md.MarketDataGW;
import com.ajna.riskman.model.Order;
import com.ajna.riskman.ui.OrderTableModel;
import com.ajna.riskman.ui.PositionTableModel;
import com.ajna.riskman.util.DateUtil;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;

import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.paint.Color;

public class RiskManGUI extends Application implements MarketDataClientInterface {

	final int GUI_HEIGHT = 500;
	final int GUI_WIDTH = 1200;
 
	final String TITLE_STRING = "Options Portfolio Monitor";

	public TableView<OrderTableModel> tableView = new TableView<OrderTableModel>();
	public ObservableList<OrderTableModel> data =  FXCollections.observableArrayList();
	
	public TableView<PositionTableModel> posTableView = new TableView<PositionTableModel>();
	public ObservableList<PositionTableModel> posData =  FXCollections.observableArrayList();
	
	GridPane mGridOrderEntry;
	
	VBox mSummaryView;
	
	SimpleDateFormat mSdf1;
	
	DaoController mDaoc;
	
	MarketDataGW mMdGW;
	
	Label mTotalDayPL, mTotalPosPL, mClosedPL;
	
	SimpleStringProperty mClsPLProperty;
	SimpleStringProperty mDayPLProperty;
	SimpleStringProperty mPosPLProperty;
	
	double dayPLsum = 0.0;
	double posPLsum = 0.0;
	double clsPLsum = 0.0;
	
	
	final Button switchBtn = new Button();
	final Label switchLbl = new Label();
	private SimpleBooleanProperty switchedOn = new SimpleBooleanProperty(true);
	Timer m_scheduler;
	
	DecimalFormat formatter = new DecimalFormat("#,###.00");
	
	//these are the components on the orderentry
	ComboBox<String> symComboBox;
	ComboBox<String> sideComboBox;
	ComboBox<String> optTypeComboBox;
	ComboBox<String> expiryComboBox;
	TextField prcTextField;
	TextField qtyTextField;
	TextField strikeTextField;
	TextField clsPrcTextField;
	
	OrderTableModel mSelectedRow = null;
	
	DatePicker mDatePicker;
	
	public RiskManGUI() {
		try {
			mDaoc = new DaoController();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mSdf1 = new SimpleDateFormat("ddMMMyyyy");
		
		mMdGW = new MarketDataGW();
	}
	@SuppressWarnings({ "restriction", "unchecked", "rawtypes" })
	@Override
	public void start(Stage primaryStage) throws Exception {
		 
		 
 
		primaryStage.setTitle("RiskMan");
		
		BorderPane borderPane = new BorderPane();
		
		mGridOrderEntry = buildOrderEntry();	
		
		buildOrderTableView();
		
		buildPositionTableView();
		
		TabPane tabPane = new TabPane();
		Tab tabOrders = new Tab("Open Positions");
		tabOrders.setClosable(false);
		Tab tabPositions = new Tab("Closed Positions");
		tabPositions.setClosable(false);
		tabOrders.setContent(tableView);
		tabPositions.setContent(posTableView);
		tabPane.getTabs().add(tabOrders);
		tabPane.getTabs().add(tabPositions);
		
		mSummaryView = buildSummaryView();

		borderPane.setTop(mSummaryView);
		
		//borderPane.setCenter(tableView);
		borderPane.setCenter(tabPane);
		
		tabPane.setSide(Side.LEFT);
		
		borderPane.setBottom(mGridOrderEntry);
		
		final Scene scene = new Scene(borderPane, GUI_WIDTH, GUI_HEIGHT);

		// show the stage.
		primaryStage.setScene(scene);
		primaryStage.show();

		Thread th1 = new Thread(() -> updateOrderTableView());
		th1.start();
		
		Thread th2 = new Thread(() -> updatePositionTableView());
		th2.start();
	}
	
	private VBox buildSummaryView() {
		VBox summaryView = new VBox();
		
		HBox title_hbox = new HBox();
		HBox pl_hbox = new HBox();
 
		title_hbox.setAlignment(Pos.CENTER);
		//title_hbox.setPadding(new Insets(15, 12, 15, 12));
		//title_hbox.setSpacing(10);
		
		pl_hbox.setAlignment(Pos.CENTER);
		//pl_hbox.setPadding(new Insets(15, 12, 15, 12));
		pl_hbox.setSpacing(10);
	    
		summaryView.setAlignment(Pos.CENTER);
		summaryView.setPadding(new Insets(15, 12, 15, 12));
		summaryView.setSpacing(10);
		
		Label labelDayPL =  new Label("Total DayPL: " );
		Label labelPosPL =  new Label("Total PosPL: ");
		
		mTotalDayPL = new Label("0.00");
		mTotalPosPL = new Label("0.00");
		mClosedPL   = new Label("0.00");
		
		mDayPLProperty = new SimpleStringProperty();
		mPosPLProperty = new SimpleStringProperty();
		mClsPLProperty = new SimpleStringProperty();
		
		mTotalDayPL.textProperty().bind(mDayPLProperty);
		mTotalPosPL.textProperty().bind(mPosPLProperty);
		mClosedPL.textProperty().bind(mClsPLProperty);

		Label refreshLabel = new Label("Auto Refresh 1 min");
		
		switchBtn.setPrefWidth(40);

		switchLbl.setUserData(switchBtn);

		Label titleLbl = new Label(TITLE_STRING);
		Font lpfont = new Font("Arial", 25);
		titleLbl.setFont(lpfont);
		
		Label realPLlabel = new Label("RealizedPL: ");
		switchBtn.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent t)
            {
                switchedOn.set(!switchedOn.get());
                
                if(switchedOn.get()){
                	System.out.println("Switched on: " + Thread.currentThread().getName());
                	System.out.println("Refresh immediately : " + Thread.currentThread().getName());
						//Thread th = new Thread(() -> refreshExpiry());
						//th.start();
            		Thread th = new Thread(() ->  { mMdGW.refreshMarketData(); }  ); 
            		th.start();
					//System.out.println("Refresh task scheduling : " + Thread.currentThread().getName());
					m_scheduler = new Timer();
                	m_scheduler.scheduleAtFixedRate(new marketDataRefresher(), 60*1000,60*1000);
                } else {
                	System.out.println("Switched off: " + Thread.currentThread().getName());
                	m_scheduler.cancel();
                	System.out.println("Refresh task Cancelling : " + Thread.currentThread().getName());
                }
            }
        });

		switchLbl.setGraphic(switchBtn);

	        switchedOn.addListener(new ChangeListener<Boolean>()
	        {
	            @Override
	            public void changed(ObservableValue<? extends Boolean> ov,
	                Boolean t, Boolean t1)
	            {
	                if (t1)
	                {
	                	switchLbl.setText("ON");
	                	switchLbl.setStyle("-fx-background-color: green;-fx-text-fill:white;");
	                	switchLbl.setContentDisplay(ContentDisplay.RIGHT);
	                }
	                else
	                {
	                	switchLbl.setText("OFF");
	                	switchLbl.setStyle("-fx-background-color: grey;-fx-text-fill:black;");
	                	switchLbl.setContentDisplay(ContentDisplay.LEFT);
	                }
	            }
	        });

	        switchedOn.set(false);
		
	        title_hbox.getChildren().addAll(titleLbl);
	        pl_hbox.getChildren().addAll(realPLlabel, mClosedPL, labelDayPL, mTotalDayPL, labelPosPL, mTotalPosPL, refreshLabel, switchLbl);
	        summaryView.getChildren().addAll(title_hbox, pl_hbox);
		
		return summaryView;
	}

	private class marketDataRefresher extends TimerTask {
		private final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        public void run() {
        	Date date = new Date(); 
        	System.out.println(sdf.format(date) + " Refresher Timer : " + Thread.currentThread().getName() );
        	
        	mMdGW.refreshMarketData();
        }
    }
 
	private void updateOrderTableView() {
		Vector<Order> ordertable = this.mDaoc.getOpenOrders();
		
		mMdGW.unSubscribeAll();
		
		data.clear();
		data.removeAll(data);
		
		for (Order ord : ordertable){
			String formattedDate = mSdf1.format(ord.getExpiryDate());
			String fmtdEntryDate = mSdf1.format(ord.getCreatedDate());
			OrderTableModel otm = new OrderTableModel(ord.getOrderID(), fmtdEntryDate, ord.getSymbol(),ord.getQuantity(), ord.getSide(), ord.getPrice(), ord.getOptType(), formattedDate, ord.getStrike()); 
			data.add(otm);
			
			this.mMdGW.subscribe(otm.getMdKey(), this);
		}
		
		tableView.setItems(data);
		 
		
		
		Thread th = new Thread(() ->  { mMdGW.refreshMarketData(); }  ); 
		th.start();
		 
	}
	
	private void updatePositionTableView() {
		Vector<Order> positiontable = this.mDaoc.getCloseOrders();
		
		clsPLsum = 0.0;
		//mMdGW.unSubscribeAll();
		
		posData.clear();
		posData.removeAll(posData);
		
		for (Order ord : positiontable){
			String formattedDate = mSdf1.format(ord.getExpiryDate());
			String fmtdEntryDate = mSdf1.format(ord.getCreatedDate());
			String fmtdCloseDate = mSdf1.format(ord.getClsDate());
			PositionTableModel ptm = new PositionTableModel(ord.getOrderID(), fmtdEntryDate, ord.getSymbol(),ord.getQuantity(), ord.getSide(), ord.getPrice(), ord.getOptType(), formattedDate, ord.getStrike(), ord.getClsPrice(), fmtdCloseDate );
			ptm.computePL();
			clsPLsum += ptm.getPosPL();
			posData.add(ptm);
			
			//this.mMdGW.subscribe(otm.getMdKey(), this);
		}
		
		posTableView.setItems(posData);
		 
		
		
		//Thread th = new Thread(() ->  { mMdGW.refreshMarketData(); }  ); 
		//th.start();
		 
	}
	
	@Override
	public void onMDUpdate(String key, double ltp, double sprc) {
		  dayPLsum = 0.0;
		  posPLsum = 0.0;
		for (OrderTableModel otm : data) {
			if (otm.getMdKey().equals(key)){
				otm.setLtprice(ltp);
				otm.setSpot(sprc);
				otm.computePL();
			}
				dayPLsum += otm.getDayPL();
				posPLsum += otm.getPosPL();
		}
		
		recheck_table();
		
		Platform.runLater(
				() -> {
					// Update UI here.
					updateSummary( );
				});
	}
	
	public void updateSummary( ){
		mDayPLProperty.set(formatter.format(dayPLsum));
		mPosPLProperty.set(formatter.format(posPLsum));
		mClsPLProperty.set(formatter.format(clsPLsum));
	}
	 
	/**
	 * 
	 */
	private void recheck_table() {
		 
		tableView.setRowFactory(new Callback<TableView<OrderTableModel>, TableRow<OrderTableModel>>() {
			@Override
			public TableRow<OrderTableModel> call(TableView<OrderTableModel> paramP) {
				return new TableRow<OrderTableModel>() {

					@Override
					protected void updateItem(OrderTableModel paramT, boolean paramBoolean) {

						super.updateItem(paramT, paramBoolean);
						//System.out.println("recheck_table: " + paramT.getSymbol() + " is a " + paramT.getSide());
						if (!isEmpty()) {
							String style = "-fx-control-inner-background: #007F0E;"
									+ "-fx-control-inner-background-alt: #007F0E;";	 
							if(paramT != null) {
								
								/*if( paramT.getSide().equalsIgnoreCase("Buy")) {
									  style = "-fx-control-inner-background: #787F00;"
											+ "-fx-control-inner-background-alt: #787F00;";
								} else {
									style = "-fx-control-inner-background: #7F0040;"
											+ "-fx-control-inner-background-alt: #7F0040;";
								}*/
								//System.out.println("recheck_table: " + paramT.getSymbol() + " is a " + paramT.getSide());
								//System.out.println("recheck_table: Style is: " + style);
							}
							 
							setStyle(style);
						}
					}
				};
			}
		});

	}
	
	
	
	private  void buildOrderTableView() {
		//use this to build the initial screen
		//mOrderTableView = new TableView<OrderTableModel>();
		//1. Symbol		String
		//2. Quantiy 	int
		//3. Side		String
		//4. Price		doube
		//5. OptionType	String
		//6. Expiry		String
		//7. Strike		Double
		
		TableColumn<OrderTableModel, String> oidColumn = new TableColumn<OrderTableModel, String>("OrdID");
		oidColumn.setCellValueFactory(new PropertyValueFactory<OrderTableModel, String>("oid"));
		//oidColumn.setPrefWidth(10);
		
		TableColumn<OrderTableModel, String> dateColumn = new TableColumn<OrderTableModel, String>("Date");
		dateColumn.setCellValueFactory(new PropertyValueFactory<OrderTableModel, String>("entry"));
		//dateColumn.setPrefWidth(10);
		
		TableColumn<OrderTableModel, String> symColumn = new TableColumn<OrderTableModel, String>("Symbol");
		symColumn.setCellValueFactory(new PropertyValueFactory<OrderTableModel, String>("symbol"));
		//symColumn.setPrefWidth(25);
		
		TableColumn<OrderTableModel, Double> qtyColumn = new TableColumn<OrderTableModel, Double>("Quantity");
		qtyColumn.setCellValueFactory(new PropertyValueFactory<OrderTableModel, Double>("quantity"));
		//qtyColumn.setPrefWidth(10);
		
		TableColumn<OrderTableModel, String> sideColumn = new TableColumn<OrderTableModel, String>("Side");
		sideColumn.setCellValueFactory(new PropertyValueFactory<OrderTableModel, String>("side"));
		//sideColumn.setPrefWidth(5);
		
		TableColumn<OrderTableModel, Double> prcColumn = new TableColumn<OrderTableModel, Double>("Price");
		prcColumn.setCellValueFactory(new PropertyValueFactory<OrderTableModel, Double>("price"));
		//prcColumn.setPrefWidth(8);
				
		prcColumn.setCellFactory(column -> {
		    return new TableCell<OrderTableModel, Double>() {
		        @Override
		        protected void updateItem(Double item, boolean empty) {
		            super.updateItem(item, empty);

		            if (item == null || empty) {
		                setText(null);
		                setStyle("");
		            } else {
		            	OrderTableModel otbl =  getTableView().getItems().get(getTableRow().getIndex());
		            	Color c = Color.BLACK;
		            	if(otbl.getSide().equalsIgnoreCase("Buy")){
		            		if (  otbl.getLtprice() > item ){
		            			c = Color.GREEN;
		            		} else {
		            			c= Color.RED;
		            		}
		            	} else { //Sell
		            		if (  otbl.getLtprice() < item ){
		            			c = Color.GREEN;
		            		} else {
		            			c= Color.RED;
		            		}
		            	}
		               setTextFill(c);
		               setText(""+item);
		            }
		        }
		    };
		});
		
		 
		//LTP : from MarketData
		TableColumn<OrderTableModel, Double> ltpColumn = new TableColumn<OrderTableModel, Double>("LTP");
		ltpColumn.setCellValueFactory(new PropertyValueFactory<OrderTableModel, Double>("ltprice"));
		
		TableColumn<OrderTableModel, String> optColumn = new TableColumn<OrderTableModel, String>("Type");
		optColumn.setCellValueFactory(new PropertyValueFactory<OrderTableModel, String>("optionType"));
		//optColumn.setPrefWidth(8);
		
		TableColumn<OrderTableModel, String> expiryColumn = new TableColumn<OrderTableModel, String>("Expiry");
		expiryColumn.setCellValueFactory(new PropertyValueFactory<OrderTableModel, String>("expiry"));
		//expiryColumn.setPrefWidth(10);
		
		TableColumn<OrderTableModel, Double> strikeColumn = new TableColumn<OrderTableModel, Double>("Strike");
		strikeColumn.setCellValueFactory(new PropertyValueFactory<OrderTableModel, Double>("strike"));
		//strikeColumn.setPrefWidth(8);
		
		strikeColumn.setCellFactory(column -> {
		    return new TableCell<OrderTableModel, Double>() {
		        @Override
		        protected void updateItem(Double item, boolean empty) {
		            super.updateItem(item, empty);

		            if (item == null || empty) {
		                setText(null);
		                setStyle("");
		            } else {
		            	OrderTableModel otbl =  getTableView().getItems().get(getTableRow().getIndex());
		            	Color c = Color.BLACK;
		            	if(otbl.getOptionType().equalsIgnoreCase("Call")){
		            		if(otbl.getSide().equalsIgnoreCase("Buy")){
		            			if (  otbl.getSpot() > item ){
		            				c = Color.GREEN;
		            			} else {
		            				c= Color.RED;
		            			}
		            		} else { //Sell
		            			if (  otbl.getSpot() < item ){
		            				c = Color.GREEN;
		            			} else {
		            				c= Color.RED;
		            			}
		            		}
		            	} else {
		            		if(otbl.getSide().equalsIgnoreCase("Buy")){
		            			if (   otbl.getSpot() < item ){
		            				c = Color.GREEN;
		            			} else {
		            				c= Color.RED;
		            			}
		            		} else { //Sell
		            			if ( otbl.getSpot() > item ){
		            				c = Color.GREEN;
		            			} else {
		            				c= Color.RED;
		            			}
		            		}
		            	}
		               setTextFill(c);
		               setText(""+item);
		            }
		        }
		    };
		});
		
		
		
		TableColumn<OrderTableModel, Double> spotColumn = new TableColumn<OrderTableModel, Double>("Spot");
		spotColumn.setCellValueFactory(new PropertyValueFactory<OrderTableModel, Double>("spot"));
		
		TableColumn<OrderTableModel, String> dayplColumn = new TableColumn<OrderTableModel, String>("DayPL");
		dayplColumn.setCellValueFactory(new PropertyValueFactory<OrderTableModel, String>("dayPLStr"));
		
		TableColumn<OrderTableModel, String> posplColumn = new TableColumn<OrderTableModel, String>("PosPL");
		posplColumn.setCellValueFactory(new PropertyValueFactory<OrderTableModel, String>("posPLStr"));
		
		tableView.getColumns().addAll(oidColumn, dateColumn, symColumn, qtyColumn, sideColumn, prcColumn, ltpColumn, optColumn, expiryColumn, strikeColumn, spotColumn, dayplColumn, posplColumn );
		
		
	    //Add change listener
		tableView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            //Check whether item is selected and set value of selected item to Label
            if (tableView.getSelectionModel().getSelectedItem() != null) {
                //System.out.println("Selected: " + newValue);
                populateOrderEntry(newValue);
                mSelectedRow = newValue;
            }
        });
        
		
		
		
		MenuItem mi1 = new MenuItem("Delete");
		
		mi1.setOnAction((ActionEvent event) -> {
		    OrderTableModel item = (OrderTableModel) tableView.getSelectionModel().getSelectedItem();
		    System.out.println("Selected item: " + item.getOid());
		    mDaoc.removeOrder(item.getOid());
		    Thread th = new Thread(() -> updateOrderTableView());
			th.start();
		});

		ContextMenu menu = new ContextMenu();
		menu.getItems().add(mi1);
		tableView.setContextMenu(menu);
	}

	/**
	 * 
	 */
	private  void buildPositionTableView() {
		//use this to build the initial screen
		//mOrderTableView = new TableView<OrderTableModel>();
		//1. Symbol		String
		//2. Quantiy 	int
		//3. Side		String
		//4. Price		doube
		//5. OptionType	String
		//6. Expiry		String
		//7. Strike		Double
		
		TableColumn<PositionTableModel, String> oidColumn = new TableColumn<PositionTableModel, String>("OrdID");
		oidColumn.setCellValueFactory(new PropertyValueFactory<PositionTableModel, String>("oid"));
		//oidColumn.setPrefWidth(10);
		
		TableColumn<PositionTableModel, String> dateColumn = new TableColumn<PositionTableModel, String>("OpenDate");
		dateColumn.setCellValueFactory(new PropertyValueFactory<PositionTableModel, String>("entry"));
		//dateColumn.setPrefWidth(10);
		
		TableColumn<PositionTableModel, String> symColumn = new TableColumn<PositionTableModel, String>("Symbol");
		symColumn.setCellValueFactory(new PropertyValueFactory<PositionTableModel, String>("symbol"));
		//symColumn.setPrefWidth(25);
		
		TableColumn<PositionTableModel, Double> qtyColumn = new TableColumn<PositionTableModel, Double>("Quantity");
		qtyColumn.setCellValueFactory(new PropertyValueFactory<PositionTableModel, Double>("quantity"));
		//qtyColumn.setPrefWidth(10);
		
		TableColumn<PositionTableModel, String> sideColumn = new TableColumn<PositionTableModel, String>("Side");
		sideColumn.setCellValueFactory(new PropertyValueFactory<PositionTableModel, String>("side"));
		//sideColumn.setPrefWidth(5);
		
		TableColumn<PositionTableModel, Double> prcColumn = new TableColumn<PositionTableModel, Double>("Price");
		prcColumn.setCellValueFactory(new PropertyValueFactory<PositionTableModel, Double>("price"));
		//prcColumn.setPrefWidth(8);
				
		prcColumn.setCellFactory(column -> {
		    return new TableCell<PositionTableModel, Double>() {
		        @Override
		        protected void updateItem(Double item, boolean empty) {
		            super.updateItem(item, empty);

		            if (item == null || empty) {
		                setText(null);
		                setStyle("");
		            } else {
		            	PositionTableModel otbl =  getTableView().getItems().get(getTableRow().getIndex());
		            	Color c = Color.BLACK;
		            	if(otbl.getSide().equalsIgnoreCase("Buy")){
		            		if (  otbl.getClsPrc() > item ){
		            			c = Color.GREEN;
		            		} else {
		            			c= Color.RED;
		            		}
		            	} else { //Sell
		            		if (  otbl.getClsPrc() < item ){
		            			c = Color.GREEN;
		            		} else {
		            			c= Color.RED;
		            		}
		            	}
		               setTextFill(c);
		                setText(""+item);
		            }
		        }
		    };
		});
		
		 
		//LTP : from MarketData
		TableColumn<PositionTableModel, Double> clsPrcColumn = new TableColumn<PositionTableModel, Double>("ClosePrice");
		clsPrcColumn.setCellValueFactory(new PropertyValueFactory<PositionTableModel, Double>("clsPrc"));
		
		TableColumn<PositionTableModel, String> optColumn = new TableColumn<PositionTableModel, String>("Type");
		optColumn.setCellValueFactory(new PropertyValueFactory<PositionTableModel, String>("optionType"));
		//optColumn.setPrefWidth(8);
		
		TableColumn<PositionTableModel, String> expiryColumn = new TableColumn<PositionTableModel, String>("Expiry");
		expiryColumn.setCellValueFactory(new PropertyValueFactory<PositionTableModel, String>("expiry"));
		//expiryColumn.setPrefWidth(10);
		
		TableColumn<PositionTableModel, Double> strikeColumn = new TableColumn<PositionTableModel, Double>("Strike");
		strikeColumn.setCellValueFactory(new PropertyValueFactory<PositionTableModel, Double>("strike"));
		//strikeColumn.setPrefWidth(8);
		
		TableColumn<PositionTableModel, Double> clsDateColumn = new TableColumn<PositionTableModel, Double>("CloseDate");
		clsDateColumn.setCellValueFactory(new PropertyValueFactory<PositionTableModel, Double>("clsDate"));
		
		TableColumn<PositionTableModel, String> posplColumn = new TableColumn<PositionTableModel, String>("PosPL");
		posplColumn.setCellValueFactory(new PropertyValueFactory<PositionTableModel, String>("posPLStr"));
		
		posplColumn.setCellFactory(column -> {
		    return new TableCell<PositionTableModel, String>() {
		        @Override
		        protected void updateItem(String item, boolean empty) {
		            super.updateItem(item, empty);

		            if (item == null || empty) {
		                setText(null);
		                setStyle("");
		            } else {
		            	PositionTableModel otbl =  getTableView().getItems().get(getTableRow().getIndex());
		            	Color c = Color.BLACK;
		            	if(otbl.getPosPL() > 0){ 
		            			c = Color.GREEN;
		            		} else {
		            			c= Color.RED;
		            		}
		               setTextFill(c);
		               setText(item);
		            }
		        }
		    };
		});
		
		posTableView.getColumns().addAll(oidColumn, dateColumn, symColumn, qtyColumn, sideColumn, prcColumn, clsPrcColumn, optColumn, expiryColumn, strikeColumn, clsDateColumn, posplColumn );
		

		
		
		MenuItem mi1 = new MenuItem("Delete");
		
		mi1.setOnAction((ActionEvent event) -> {
			PositionTableModel item = (PositionTableModel) posTableView.getSelectionModel().getSelectedItem();
		    System.out.println("Selected item: " + item.getOid());
		    mDaoc.removeOrder(item.getOid());
		    Thread th = new Thread(() -> updatePositionTableView());
			th.start();
		});

		ContextMenu menu = new ContextMenu();
		menu.getItems().add(mi1);
		posTableView.setContextMenu(menu);
	}

	
	
	
	
	public GridPane buildOrderEntry() {
		
		GridPane mGridOrderEntry = new GridPane();
		
		mGridOrderEntry.setPadding(new Insets(15, 12, 15, 12));
 
		Vector<String> allSyms  = 
				new Vector<String>(Arrays.asList("AUROPHARMA","AXISBANK","BANKBARODA",
						"BHEL","BPCL","BHARTIARTL","CIPLA","COALINDIA","GAIL","HINDALCO",
						"ITC","ICICIBANK","LICHSGFIN","NTPC","ONGC","POWERGRID","TATAMOTORS"
						,"TATAPOWER","TATASTEEL","TECHM","WIPRO","INFY","SBIN","IDEA","ADANIPORTS"));
		
		
		
		//1. Symbol
		Label symLabel = new Label("Symbol:   ");
		//TextField  symTextField = new TextField();
		symComboBox = new ComboBox<String>();
		symComboBox.setEditable(true);
		Collections.sort(allSyms);
		symComboBox.getItems().clear();
		symComboBox.getItems().addAll(allSyms);
		HBox symbolContainer = new HBox();
		symbolContainer.getChildren().addAll(symLabel, symComboBox);
		
		//2. Quantiy
		Label qtyLabel = new Label("Quantity: ");
		qtyTextField = new TextField();
		HBox qtyContainer = new HBox();
		qtyContainer.getChildren().addAll(qtyLabel, qtyTextField);
		
		//3. Side
		Label sideLabel = new Label("Side:       ");
		sideComboBox = new ComboBox();
		sideComboBox.getItems().addAll(
				"Buy",
				"Sell"
				);
		sideComboBox.getSelectionModel().selectFirst();
		
		HBox sideContainer = new HBox();
		sideContainer.getChildren().addAll(sideLabel, sideComboBox);
		
		//4. Price
		Label prcLabel = new Label("Price:    ");
		prcTextField = new TextField();
		HBox prcContainer = new HBox();
		prcContainer.getChildren().addAll(prcLabel, prcTextField);
				
		//5. OptionType
		Label optTypeLabel = new Label("OptnType:");
		optTypeComboBox = new ComboBox();
		optTypeComboBox.getItems().addAll(
				"Call",
				"Put"
				);
		optTypeComboBox.getSelectionModel().selectFirst();
		HBox optypeContainer = new HBox();
		optypeContainer.getChildren().addAll(optTypeLabel, optTypeComboBox);
		
		//6. Expiry
		Label expiryLabel = new Label("Expiry:");
		expiryComboBox  = new ComboBox();
		//expiryComboBox.setEditable(true); //temporarily to enter past data
		expiryComboBox.getItems().addAll(new DateUtil().getNext3Expiries());
 
		expiryComboBox.getSelectionModel().selectFirst();
		HBox expiryContainer = new HBox();
		expiryContainer.getChildren().addAll(expiryLabel, expiryComboBox);
		
		
		//7. Strike
		Label strikeLabel = new Label("Strike:   ");
		strikeTextField = new TextField();
		HBox strikeContainer = new HBox();
		strikeContainer.getChildren().addAll(strikeLabel, strikeTextField);

		//. Add button
		Button addButton = new Button("      Add      ");
		addButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {
				String sym = symComboBox.getValue().toString();
				int qty = Integer.parseInt(qtyTextField.getText());
				String side = sideComboBox.getValue().toString();
				double price = Double.parseDouble(prcTextField.getText());
				String optyp = optTypeComboBox.getValue().toString();
				String expiry = expiryComboBox.getValue().toString();
				double strike = Double.parseDouble(strikeTextField.getText());
				
				try {
					Date date = mSdf1.parse(expiry);
					java.sql.Date sqlExpryDate = new java.sql.Date(date.getTime()); 
					
					java.sql.Date  sqlDate;
					
					LocalDate lcld = mDatePicker.getValue();
					
					if(lcld == null){
						
						sqlDate  = new java.sql.Date(Calendar.getInstance().getTime().getTime());
							
					} else {
						 
						sqlDate = java.sql.Date.valueOf(lcld);
					}
					// add to db
					mDaoc.addOrder(sqlDate, sym, qty, side,  price, optyp, sqlExpryDate, strike);
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				

				Thread th = new Thread(() -> updateOrderTableView());
				th.start();
				
				 
			}
		});
		
		//. Clear button
				Button clrButton = new Button("      Clear      ");
				clrButton.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent e) {
						clearOrderEntry();
					}
				});
				
		//
		Label dateLabel = new Label("Date: ");
		
		// Date Picker
		mDatePicker = new DatePicker();
		mDatePicker.setOnAction(new EventHandler() {
		 
			@Override
			public void handle(javafx.event.Event event) {
				LocalDate date = mDatePicker.getValue();
		        System.out.println("Selected date: " + date);
				
			}
		});
		
		Button flatButton = new Button("Flat Position");
		flatButton.setOnAction(new EventHandler() {

			@Override
			public void handle(javafx.event.Event event) {
				System.out.println("Selected flat Position "  ); 
				if(mSelectedRow != null){
					
					int ordId = mSelectedRow.getOid();
					double clsPrc = Double.parseDouble(clsPrcTextField.getText());
					
					java.sql.Date  clsDate;
					
					LocalDate lcld = mDatePicker.getValue();
					
					if(lcld == null){
						
						clsDate  = new java.sql.Date(Calendar.getInstance().getTime().getTime());
							
					} else {
						 
						clsDate = java.sql.Date.valueOf(lcld);
					}
					mDaoc.updateOrder(clsPrc, clsDate, ordId);
					//mDaoc.addOrder(sym, qty, side,  price, optyp, sqlDate, strike);
				}
				Thread th1 = new Thread(() -> updateOrderTableView());
				th1.start();
				
				Thread th2 = new Thread(() -> updatePositionTableView());
				th2.start();
			} 
			
		});
		Label clsPrcLabel = new Label("Cls Prc:");
		clsPrcTextField = new TextField();
		
		HBox clsPriceContainer = new HBox();
		clsPriceContainer.getChildren().addAll(clsPrcLabel, clsPrcTextField);
		
		HBox dateContainer = new HBox();
		dateContainer.getChildren().addAll(dateLabel, mDatePicker);
		
		HBox flatoutContainer = new HBox();
		flatoutContainer.setSpacing(10);
		flatoutContainer.getChildren().addAll(clsPriceContainer, flatButton);
		
		HBox btnContainer = new HBox();
		btnContainer.setSpacing(10);
		btnContainer.getChildren().addAll(addButton, clrButton);
				
		VBox vbox_1 = new VBox();
		vbox_1.setPadding(new Insets(15, 12, 15, 12));
		vbox_1.setSpacing(10);
		vbox_1.getChildren().addAll(symbolContainer, qtyContainer);
		
		VBox vbox_2 = new VBox();
		vbox_2.setPadding(new Insets(15, 12, 15, 12));
		vbox_2.setSpacing(10);
		vbox_2.getChildren().addAll(strikeContainer, prcContainer);
		
		VBox vbox_3 = new VBox();
		vbox_3.setPadding(new Insets(15, 12, 15, 12));
		vbox_3.setSpacing(10);
		vbox_3.getChildren().addAll(sideContainer, optypeContainer );
		
		VBox vbox_4 = new VBox();
		vbox_4.setPadding(new Insets(15, 12, 15, 12));
		vbox_4.setSpacing(10);
		vbox_4.getChildren().addAll(expiryContainer, btnContainer);
		
		VBox vbox_5 = new VBox();
		vbox_5.setPadding(new Insets(15, 12, 15, 12));
		vbox_5.setSpacing(10);
		vbox_5.getChildren().addAll(dateContainer, flatoutContainer);
		
		mGridOrderEntry.setHgap(10);
		mGridOrderEntry.setVgap(12);
		
		mGridOrderEntry.add(vbox_1, 0, 0);
		mGridOrderEntry.add(vbox_2, 1, 0);
		mGridOrderEntry.add(vbox_3, 2, 0);
		mGridOrderEntry.add(vbox_4, 3, 0);
		mGridOrderEntry.add(vbox_5, 4, 0);
		

		return mGridOrderEntry;

	}
	
	void clearOrderEntry(){
		symComboBox.getSelectionModel().selectFirst();
		qtyTextField.clear();
		sideComboBox.getSelectionModel().selectFirst();
		prcTextField.clear();
		optTypeComboBox.getSelectionModel().selectFirst();
		expiryComboBox.getSelectionModel().selectFirst();
		strikeTextField.clear();
	}
	
	void populateOrderEntry(OrderTableModel otm){
		symComboBox.getSelectionModel().select(otm.getSymbol());
		qtyTextField.setText(""+otm.getQuantity());
		sideComboBox.getSelectionModel().select(otm.getSide());
		prcTextField.setText("" + otm.getPrice());;
		optTypeComboBox.getSelectionModel().select(otm.getOptionType());;
		expiryComboBox.getSelectionModel().select(otm.getExpiry());;
		strikeTextField.setText(""+otm.getStrike());;
	}
	
	public static void main(String[] args) throws InterruptedException {
		launch(args);
	}
	
	 
}
